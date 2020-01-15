package net.monsterdev.automosreg.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.monsterdev.automosreg.AutoMosreg;
import net.monsterdev.automosreg.algorithms.CalculateProductsPriceAlgorithm;
import net.monsterdev.automosreg.domain.*;
import net.monsterdev.automosreg.enums.ProposalStatus;
import net.monsterdev.automosreg.enums.TradeStatus;
import net.monsterdev.automosreg.exceptions.AutoMosregException;
import net.monsterdev.automosreg.http.requests.*;
import net.monsterdev.automosreg.model.SupplierProposal;
import net.monsterdev.automosreg.model.dto.*;
import net.monsterdev.automosreg.repository.TradesRepository;
import net.monsterdev.automosreg.utils.DateUtils;
import net.monsterdev.automosreg.utils.StringUtil;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dozer.DozerBeanMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Сервис обновления информации о закупках и связанных с ними предложениях. Сервис выполняет следующие действия:
 * - обновляет информацию (наименование, дату начала и окончания подачи предложений, статус, кол-во поданных
 *   предложений, лучшее предложение);
 * - подает предложение по закупкам;
 * - проставляет индентификаторы для поданых предложений.
 */
@Slf4j
@Service
@PropertySource("classpath:application.properties")
public class UpdateTradesService extends Thread {

  private static final String ERROR_COMMON_LOG_MSG = "UpdateTradesService error: %s %s";
  private static final String GET_TRADE_INFO_ERROR_1 = "Ошибка при получении информации о закупке %d";
  private static final String GET_TRADE_INFO_ERROR_2 = "Закупка %d не найдена";
  private static final String GET_TRADE_INFO_ERROR_3 = "Ошибка при получении страницы с информацией о закупке %d";
  private static final String CREATE_PROPOSAL_ERROR_1 = "По закупке %d не найдено ни одной закупочной позиции";
  private static final String CREATE_PROPOSAL_ERROR_2 = "Ошибка при создании предложения по заявке %d : %s";
  private static final String BIND_PROPOSAL_ERROR_1 = "Ошибка при запросе страницы предложений с фильтром по заявке %d";
  private static final String BIND_PROPOSAL_ERROR_2 = "Предложений для закупки %d не найдено";
  private static final String GET_PROPOSAL_INFO_ERROR_1 = "Ошибка при запросе информации о предложении %d : %s";
  private static final String GET_PROPOSAL_INFO_ERROR_2 = "Предложение %d не найдено";
  private static final String GET_PROPOSAL_PRODUCTS_ERROR_1 = "Ошибка при получении страницы редактирования предложения %d : %s";
  private static final String GET_BEST_PROPOSAL_ERROR_1 = "Ошибка при получении информации о лучшем предложении по закупке %d (нет ни одного участника)";

  private static Logger LOG = LogManager.getLogger(UpdateTradesService.class);

  @Value("${work-thread-timeout}")
  private int timeout;
  private final DozerBeanMapper DOZZER_MAPPER = new DozerBeanMapper();
  private final CalculateProductsPriceAlgorithm calAlgorithm = new CalculateProductsPriceAlgorithm();

  @Autowired
  private UserService userService;
  @Autowired
  private HttpService httpService;
  @Autowired
  private TradesRepository tradesRepository;

  private volatile boolean bStop = false;

  public void finish() {
    bStop = true;
  }

  /**
   * Отправляет запрос по API на получение информации по закупке
   *
   * @param trade закупка
   * @return информация о закупке
   */
  private TradeInfoDto getTradeInfo(@NonNull Trade trade) throws AutoMosregException, IOException {
    TraderResponse response = httpService.sendRequest(new GetTradeInfoRequest(trade.getTradeId()));
    if (response.getCode() != HttpStatus.SC_OK) {
      throw new AutoMosregException(String.format(GET_TRADE_INFO_ERROR_1, trade.getTradeId()));
    }
    ObjectMapper mapper = new ObjectMapper();
    TradesInfoDto trades = mapper.readValue(response.getEntity(), TradesInfoDto.class);
    if (trades.getTotalrecords() == 0) {
      throw new AutoMosregException(String.format(GET_TRADE_INFO_ERROR_2, trade.getTradeId()));
    }
    return trades.getTrades().get(0);
  }

  /**
   * Возвращает информацию о лучшем предложении по данной закупке Эту информацию мы можем получить только если
   * пользователь подал действительное предложение. Для получения этой информации нужно запросить непосредственно
   * страницу торговой площадке даннной закупки (не API)
   *
   * @param trade закупка
   * @return лучшее предложение по данной закупке
   * @throws AutoMosregException генерируется в случае ошибки при выполнении данной фазы
   */
  private BestProposalInfoDto getBestProposal(@NonNull Trade trade) throws AutoMosregException {
    TraderResponse response = httpService.sendRequest(new GetTradePageRequest(trade.getTradeId()));
    if (response.getCode() != HttpStatus.SC_OK) {
      throw new AutoMosregException(String.format(GET_TRADE_INFO_ERROR_3, trade.getTradeId()));
    }

    BestProposalInfoDto bestProposalInfo = new BestProposalInfoDto();
    String pageContent = response.getEntity();
    List<SupplierProposal> supplierProposals = StringUtil.parseForProposals(pageContent);
    bestProposalInfo.setTotalCount(supplierProposals.size());
    if (bestProposalInfo.getTotalCount() > 0) {
      bestProposalInfo.setBestProposalId(supplierProposals.get(0).getId());
      bestProposalInfo.setBestPrice(supplierProposals.get(0).getPrice());
    }
    return bestProposalInfo;
  }

  /**
   * Формирует действительное предложение по данной закупке
   *
   * @param trade закупка, по которой нужно сформировать предложение
   * @return идентификатор созданного предложения
   * @throws AutoMosregException генерируется в случае ошибки при выполнении данной фазы
   */
  private Proposal createProposal(@NonNull Trade trade) throws AutoMosregException {
    User user = userService.getCurrentUser();
    Set<ProductDto> products = trade.getTradeProducts().stream()
        .map(tradeProduct -> DOZZER_MAPPER.map(tradeProduct, ProductDto.class)).collect(Collectors.toSet());
    if (products.size() == 0) {
      log.error("По закупке {} не найдено ни одной торговой позиции", trade.getTradeId());
      throw new AutoMosregException(String.format(CREATE_PROPOSAL_ERROR_1, trade.getTradeId()));
    }
    // расчитываем стоимость позиций в закупке исходя из установленного пользователем значения стартовой цены
    calAlgorithm.doCalc(products, trade.getStartPrice());
    // получаем общую стоимость закупки (в закупке может быть несколько позиций для каждой из которых задана стоимость)
    BigDecimal totalPrice = products.stream().map(ProductDto::getSumm).reduce(BigDecimal.ZERO, BigDecimal::add);
    /* 3. Формирование http-запроса на создание предложения */
    JSONObject proposalData = new JSONObject();
    proposalData.put("ContactInfo", user.getContactInfo());
    proposalData.put("ApplicationDocuments", new ArrayList<>());
    proposalData.put("AgreeWithCustomerConditions", true);
    proposalData.put("DefineCustomPriceForEachProduct", true);
    proposalData.put("Price", totalPrice.setScale(2, RoundingMode.DOWN).toString().replace('.', ','));
    if (user.isUseNDS()) {
      proposalData.put("IncludeVatRate", true);
      proposalData.put("IncludeVatRateChecked", true);
      // если флаг "Облагается НДС" установлен, а ставка НДС не задана, то параметр VaеRateState: "0",
      // но в данном приложении такая ситуация невозможна. Т.е. если у пользователя "Облагается НДС"
      // установлен, то ставка НДС обязательно должна быть задана, а поэтому, VatRateState: "5"
      proposalData.put("VatRateState", "5");
      proposalData.put("VatRate", user.getNDS());
    } else {
      proposalData.put("IncludeVatRate", false);
      proposalData.put("IncludeVatRateChecked", false);
      proposalData.put("VatRateState", "10");
    }
    proposalData.put("OfferExpiryDate", (String) null);
    proposalData.put("NeverExpired", true);
    proposalData.put("Id", 0);
    proposalData.put("Products", products);
    PublishProposalRequest proposalRequest = new PublishProposalRequest(trade.getTradeId(), proposalData);
    TraderResponse response = httpService.sendRequest(proposalRequest);
    // Если операция прошла успешно, что ответ от площадки есть идентификатор созданного предложения,
    // иначе содержит сообщение об ошибке в формате JSON {"Message":"Текст ошибки"}
    if (response.getCode() == HttpStatus.SC_OK) {
      Proposal proposal = new Proposal();
      proposal.setId(Long.parseLong(response.getEntity()));
      log.trace("Предложение по закупке {} успешно подано. Id предложения: {}", trade.getTradeId(), proposal.getId());
      return proposal;
    } else {
      log.error("Предложение по закупке {} не подано. Код ответа: {}. Ответ площадки: {]", trade.getTradeId(),
          response.getCode(), response.getEntity());
      throw new AutoMosregException(String.format(CREATE_PROPOSAL_ERROR_2, trade.getTradeId(), response.getEntity()));
    }
  }

  /**
   * Возвращает идентификатор предложения, связанного с закупкой Функция отправляет запрос "Верни мне все предложения
   * данного пользователя" на торговую площадку, при этом в качестве фильтра к данному запросу задается идентификатор
   * закупки. Торговая площадка возвращает html-страницу, которую нужно распарсить и получить идентификатор предложения,
   * если оно существует.
   * @param trade закупка
   */
  private Long getProposalId(@NonNull Trade trade) throws AutoMosregException, IOException {
    JSONObject proposalFilter = new JSONObject();
    proposalFilter.put("PublishDateFrom", "");
    proposalFilter.put("PublishDateTo", "");
    proposalFilter.put("RevokeDateFrom", "");
    proposalFilter.put("RevokeDateTo", "");
    proposalFilter.put("State", "0");
    proposalFilter.put("IncomingNumber", "");
    proposalFilter.put("TradeNumber", trade.getTradeId());
    proposalFilter.put("itemsPerPage", 10);
    proposalFilter.put("page", 1);
    proposalFilter.put("sortingParams", Collections.emptyList());

    GetProposalListRequest request = new GetProposalListRequest(proposalFilter);
    TraderResponse response = httpService.sendRequest(request);
    if (response.getCode() != HttpStatus.SC_OK) {
      throw new AutoMosregException(String.format(BIND_PROPOSAL_ERROR_1, trade.getTradeId()));
    }
    ObjectMapper mapper = new ObjectMapper();
    ProposalsInfoDto proposalsInfoDto = mapper.readValue(response.getEntity(), ProposalsInfoDto.class);
    if (proposalsInfoDto.getTotalrecords() == 0) {
      throw new AutoMosregException(String.format(BIND_PROPOSAL_ERROR_2, trade.getTradeId()));
    }
    for (ProposalInfoDto proposalInfo : proposalsInfoDto.getProposals()) {
      if (proposalInfo.getTradeNumber().equals(trade.getTradeId())) {
        return proposalInfo.getIncomingNumber();
      }
    }
    throw new AutoMosregException(String.format(BIND_PROPOSAL_ERROR_2, trade.getTradeId()));
  }

  /**
   * Возвращает статус предложения на площадке
   * @param proposal предложение
   * @return статус предложения
   * @throws AutoMosregException возникает при ошибке запроса статуса предложения
   * @throws IOException возникает при ошибке парсинга ответа
   */
  private ProposalStatus getProposalStatus(@NonNull Proposal proposal) throws AutoMosregException, IOException {
    TraderResponse response = httpService.sendRequest(new GetProposalInfoRequest(proposal.getId()));
    if (response.getCode() != HttpStatus.SC_OK) {
      throw new AutoMosregException(
          String.format(GET_PROPOSAL_INFO_ERROR_1, proposal.getId(), response.getEntity()));
    }
    ObjectMapper mapper = new ObjectMapper();
    ProposalsInfoDto proposals = mapper.readValue(response.getEntity(), ProposalsInfoDto.class);
    if (proposals.getTotalrecords() == 0) {
      throw new AutoMosregException(String.format(GET_PROPOSAL_INFO_ERROR_2, proposal.getId()));
    }
    return ProposalStatus.valueOf(proposals.getProposals().get(0).getState());
  }

  /**
   * Возвращает список продуктов/позиций связанных с данным предложением.
   * @param proposal предложение
   * @return список товаров/позиций.
   * @throws AutoMosregException возникает при ошибке парсинга ответа
   */
  private Set<ProposalProduct> getProposalProducts(@NonNull Proposal proposal) throws AutoMosregException {
    TraderResponse response = httpService.sendRequest(new GetUpdateProposalPageRequest(proposal.getId()));
    if (response.getCode() != HttpStatus.SC_OK) {
      throw new AutoMosregException(
          String.format(GET_PROPOSAL_PRODUCTS_ERROR_1, proposal.getId(), response.getEntity()));
    }
    return StringUtil.parseProposalForProducts(response.getEntity());
  }

  @Override
  public void run() {
    setName("UpdateTradesService");
    while (!bStop) {
      LOG.trace("Начало оперативного цикла UpdateTradeService");
      try {
        List<Trade> trades = userService.getCurrentUser().getTrades();
        LOG.trace("Будет обработано {} закупок", trades.size());
        for (Trade trade : trades) {
          Trade dbTrade = tradesRepository.findTrade(trade.getTradeId());
          LOG.trace("Обновление информации по закупке {}", dbTrade.getTradeId());
          try {
            LOG.trace("Получение информации по закупке {}", dbTrade.getTradeId());
            TradeInfoDto tradeInfo = getTradeInfo(dbTrade);
            LOG.trace("Информация по закупке {} получена", dbTrade.getTradeId());
            // обновляем информацию о закупке
            dbTrade.setName(tradeInfo.getTradeName());
            dbTrade.setBeginDT(tradeInfo.getPublicationDate());
            dbTrade.setEndDT(tradeInfo.getFillingApplicationEndDate());
            dbTrade.setNmc(tradeInfo.getInitialPrice());
            dbTrade.setStatus(TradeStatus.valueOf(tradeInfo.getTradeState()));
            // определяем сколько времени осталось до окончания торгов
            long millis = dbTrade.getEndDT().getTime() - DateUtils.toMoscow(System.currentTimeMillis()).getTime();

            // формирование предложения на закупку, если таковое еще не было подано
            if (dbTrade.getStatus() == TradeStatus.SUGGESTIONS &&
                dbTrade.getProposal() == null &&
                millis < dbTrade.getActivateTime()) {
              LOG.trace("Подача предложения по закупке {}", dbTrade.getTradeId());
              dbTrade.setProposal(createProposal(dbTrade));
              LOG.trace("Предложение по закупке {} подано", dbTrade.getTradeId());

              LOG.trace("Получение списка позиций по закупке {}", dbTrade.getTradeId());
              dbTrade.setProposalProducts(getProposalProducts(dbTrade.getProposal()));
              LOG.trace("Список предложений по закупке {} получен", dbTrade.getTradeId());
            } else if (dbTrade.getProposal() != null) {
              // получаем статус предложения по данной заявке
              dbTrade.getProposal().setStatus(getProposalStatus(dbTrade.getProposal()));
              // Пока по закупке ведутся торги, обновляем информацию о лучшем предложении
              if (dbTrade.getStatus() == TradeStatus.SUGGESTIONS
                  && dbTrade.getProposal().getStatus() == ProposalStatus.ACTIVE) {
                BestProposalInfoDto bestProposalInfo = getBestProposal(dbTrade);
                dbTrade.setProposalsCount(bestProposalInfo.getTotalCount());
                dbTrade.setBestProposalId(bestProposalInfo.getBestProposalId());
                dbTrade.setBestPrice(bestProposalInfo.getBestPrice());
              } else {
                dbTrade.setBestProposalId(null);
              }
            }
            dbTrade.setUpdateDT(new Date());
            tradesRepository.update(dbTrade);
          } catch (Throwable t) {
            LOG.error(String.format(ERROR_COMMON_LOG_MSG, t.getClass(), t.getMessage()));
          }
          LOG.trace("Обновление информации о закупке {} завершено", dbTrade.getTradeId());
        }
        LOG.trace("Окончание оперативного цикла UpdateTradeService. Пауза {} мсек", AutoMosreg.work_thread_timeout);
        sleep(AutoMosreg.work_thread_timeout);
      } catch (InterruptedException ex) {
        bStop = true;
      }
    }
  }
}
