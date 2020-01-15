package net.monsterdev.automosreg.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProposalData {

  /**
   * Минимальная цена предложения, до которой поставщик считает возможным опуститься
   */
  private BigDecimal minTradeVal;
  /**
   * Время в мсек до конца приема предложений по закупке, когда необходимо подать предложение
   * Если не задано, то предложение подается сразу
   */
  private long activateTime;

  /**
   * Начальная цена предложения по закупке.
   */
  private BigDecimal startTradeVal;
}
