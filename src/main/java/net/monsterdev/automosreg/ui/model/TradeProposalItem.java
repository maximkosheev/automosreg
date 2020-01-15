package net.monsterdev.automosreg.ui.model;

import net.monsterdev.automosreg.domain.ProposalProduct;
import net.monsterdev.automosreg.enums.ProposalStatus;
import net.monsterdev.automosreg.domain.Trade;
import net.monsterdev.automosreg.enums.TradeStatus;
import net.monsterdev.automosreg.utils.DateUtils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Класс TradeProposalItem - класс-proxy, служит для отображении информации о предложении пользователя в главном окне
 * программы
 */
public class TradeProposalItem {

  private Boolean selected;
  private Trade trade;

  public TradeProposalItem(Trade trade) {
    selected = false;
    this.trade = trade;
  }

  public Boolean getSelected() {
    return selected;
  }

  public void setSelected(Boolean selected) {
    this.selected = selected;
  }

  public String getTradeId() {
    return trade.getTradeId().toString();
  }

  public String getProposalId() {
    if (trade.getProposal() == null) {
      return "Неопределено";
    }
    return trade.getProposal().getId().toString();
  }

  public String getTradeName() {
    return trade.getName();
  }

  public String getStartTradesDT() {
    DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
    return df.format(trade.getBeginDT());
  }

  public String getFinishTradesDT() {
    DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    return df.format(trade.getEndDT());
  }

  public String getNmc() {
    return trade.getNmc().toString();
  }

  public String getLimit() {
    return trade.getMinTradeVal().toString();
  }

  public ProposalStatus getStatus() {
    if (trade.getProposal() == null) {
      return ProposalStatus.UNDEFINED;
    }
    return trade.getProposal().getStatus();
  }

  public String getProposalsCount() {
    if (trade.getProposalsCount() == null) {
      return "Неопределено";
    }
    return trade.getProposalsCount().toString();
  }

  public String getWinPrice() {
    if (trade.getBestPrice() == null) {
      return "Неопределено";
    }
    return trade.getBestPrice().toPlainString();
  }

  public String getCurrentPrice() {
    if (trade.getProposal() == null || trade.getProposalProducts().size() == 0) {
      return "Неопределено";
    }
    return trade
        .getProposalProducts()
        .stream()
        .map(ProposalProduct::getSumm)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(2)
        .toPlainString();
  }

  /**
   * Возвращает признак завершения торгов по закупке
   * @return true, если торги завершены, false в противном случае
   */
  private boolean isFinished() {
    long millis = DateUtils.toMoscow(System.currentTimeMillis()).getTime();
    return trade.getStatus() == TradeStatus.CANCELED || trade.getStatus() == TradeStatus.CONTRACTED || millis > trade
        .getEndDT().getTime();
  }

  /**
   * Возвращает признак того, что информация о закупке прокисла
   * @return true, если информация прокисла
   */
  private boolean isSoured() {
    long millis = (new Date()).getTime() - trade.getUpdateDT().getTime();
    return millis > 5 * 60 * 1000;
  }

  /**
   * Возвращает CSS класс для данного предложения в зависимости от статуса.
   * - информация по закупке "прокисла" - серый
   * - закупка активна, но по ней еще не подано предложение - желтый
   * - закупка активная и по ней уже подано предложение - белый.
   * - закупка завершена и по ней не было подано предложение - красный
   * - закупка завершена и проиграна в результате торгов - красный
   * - закупка завершена и выиграна - зеленый
   */
  public String getCSSClass() {
    if (trade.getStatus() == TradeStatus.ARCHIVED) {
      return "archived";
    } else if (isSoured()) {
      return "soured";
    } else if (!isFinished()) {
      if (trade.getProposal() == null) {
        return "empty";
      } else {
        return "default";
      }
    } else {
      // предложение так и не было подано
      if (trade.getProposal() == null) {
        return "lose";
      } else {
        if (trade.getProposal().getId().equals(trade.getBestProposalId())) {
          return "win";
        } else {
          return "lose";
        }
      }
    }
  }
}
