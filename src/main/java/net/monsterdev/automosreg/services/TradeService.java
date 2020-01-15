package net.monsterdev.automosreg.services;

import lombok.NonNull;
import net.monsterdev.automosreg.AutoMosreg;
import net.monsterdev.automosreg.behaviors.TraderBehavior;
import net.monsterdev.automosreg.behaviors.impl.SlowDecreaseOnAllProductsBehavior;
import net.monsterdev.automosreg.domain.ProposalProduct;
import net.monsterdev.automosreg.domain.Trade;
import net.monsterdev.automosreg.enums.ProposalStatus;
import net.monsterdev.automosreg.enums.TradeStatus;
import net.monsterdev.automosreg.exceptions.AutoMosregException;
import net.monsterdev.automosreg.http.requests.TraderResponse;
import net.monsterdev.automosreg.http.requests.UpdateProposalPriceRequest;
import net.monsterdev.automosreg.repository.TradesRepository;
import net.monsterdev.automosreg.utils.DateUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Сервис TradeService в автоматическом режиме редактирует поданное пользователем предложение, постепенно снижая прайс.
 * Алгоритм работы робота следующий: 1. Если в базе данных (локальной) по некой закупке наше предложение является
 * лучшим, то ничего не делаем; 2. Снижаем стоимость предложения на некоторое значение (задается в конфигурации) 3.
 * Определяем стоимость каждой позиции в данной закупке в зависимости от суммы предложения 3.1 Снижаем до предела
 * стоимость первой позиции. Если этого не хватает, то снижаем до предела стоимость второй позиции и т.д.
 */
@Service
@PropertySource("classpath:application.properties")
public class TradeService extends Thread {

  private static final String ERROR_COMMON_LOG_MSG = "TradeService error: %s : %s";
  private static final String ERROR_REQUEST_FAILED = "Ошибка обновления предложения на торговой площадке: %s";

  private static Logger LOG = LogManager.getLogger(TradeService.class);

  private static int SLEEP_TIME = 60 * 1000;

  @Value("${work-thread-timeout}")
  private int timeout;

  private final BigDecimal tradeReduction = new BigDecimal("1.00");

  @Autowired
  private UserService userService;

  @Autowired
  private HttpService httpService;

  @Autowired
  private TradesRepository tradesRepository;

  TraderBehavior traderBehavior = new SlowDecreaseOnAllProductsBehavior();

  private volatile boolean bStop = false;

  public void finish() {
    bStop = true;
  }

  private void updateProposalPrice(@NonNull Trade trade) throws AutoMosregException {
    // Предложение пользователя по данной закупке является лучшим - ничего не делаем
    if (Objects.equals(trade.getProposal().getId(), trade.getBestProposalId())) {
      return;
    }
    // Какой-то другой пользователь сделал более лучшее предложение. Готовим новое предложение за счет снижения
    // стоимости лучшего на данный момент предложения на заданное значение.
    BigDecimal proposalPrice = trade.getBestPrice().subtract(tradeReduction);
    Set<ProposalProduct> products = trade.getProposalProducts();

    // Проверка на то, достигли ли мы установленного для данной закупки лимита
    if (proposalPrice.compareTo(trade.getMinTradeVal()) < 0) {
      throw new AutoMosregException(String.format("Ошибка торгов: по закупке %d достигнут установленный лимит %s",
          trade.getTradeId(),
          trade.getMinTradeVal().toPlainString()));
    }

    // А можно ли вообще сделать предложение с такой низкой суммарной стоимостью
    BigDecimal totalMinCost = products.stream().map(ProposalProduct::getMinCost)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (proposalPrice.compareTo(totalMinCost) < 0) {
      throw new AutoMosregException(String.format("Ошибка торгов:" +
              " невозможно снизить стоимость предложения по закупке %d ниже минимально возможного значения %s",
          trade.getTradeId(),
          totalMinCost.toPlainString()));
    }
    try {
      // применяем стратегию ведения торгов
      traderBehavior.doTrade(trade, proposalPrice);
      // отправка запроса на изменение торгового предложения
      TraderResponse response = httpService.sendRequest(new UpdateProposalPriceRequest(trade));
      if (response.getCode() != HttpStatus.SC_OK) {
        throw new AutoMosregException(String.format(ERROR_REQUEST_FAILED, response.getEntity()));
      }
    } catch (Throwable t) {
      throw new AutoMosregException(String.format("Ошибка торгов: %s", t.getMessage()));
    }
  }

  @Override
  public void run() {
    setName("TradeService");
    while (!bStop) {
      LOG.trace("Начало оперативного цикла TradeService");
      try {
        List<Trade> trades = userService.getCurrentUser().getTrades();
        LOG.trace("Будет обработано {} закупок", trades.size());
        for (Trade trade : trades) {
          Trade dbTrade = tradesRepository.findTrade(trade.getTradeId());
          try {
            long millis = dbTrade.getUpdateDT().getTime() - System.currentTimeMillis();
            // Если информация по закупке в базе свежая, состояние закупки - "Прием предложений"
            // состояние уже поданного предложения - "Действительно"
            if (millis < 0 && dbTrade.getStatus() == TradeStatus.SUGGESTIONS &&
                dbTrade.getProposal() != null &&
                dbTrade.getProposal().getStatus() == ProposalStatus.ACTIVE) {
              LOG.trace("Обновление стоимости предложения по закупке {}", dbTrade.getTradeId());
              updateProposalPrice(dbTrade);
              trade.setPriceUpdateDT(new Date());
              tradesRepository.update(dbTrade);
              LOG.trace("Обновление стоимости предложения по закупке {} завершено", dbTrade.getTradeId());
            }
          } catch (Throwable t) {
            LOG.error(String.format(ERROR_COMMON_LOG_MSG, t.getClass(), t.getMessage()));
          }
        }
        LOG.trace("Окончание оперативного цикла TradeService. Пауза {} мсек", AutoMosreg.work_thread_timeout);
        sleep(AutoMosreg.work_thread_timeout);
      } catch (InterruptedException ex) {
        //
      }
    }
  }
}
