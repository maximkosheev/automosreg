package net.monsterdev.automosreg.domain;

import lombok.Data;
import net.monsterdev.automosreg.enums.TradeStatus;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Класс описывающий сущность "Закупка", хрянящаяся в БД
 *
 * @author madmax
 */
@Data
@Entity
@DynamicUpdate
@Table(name = "trades",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "trade_id"})})
public class Trade {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * идентификатор закупки, полученный от площадки
   */
  @Column(name = "trade_id", nullable = false)
  private Long tradeId;

  /**
   * Наименование закупки
   */
  @Column(name = "name", length = 4000)
  private String name;

  /**
   * Текущий стратус закупки
   */
  @Enumerated(EnumType.STRING)
  private TradeStatus status;

  /**
   * Дата и время начала торгов по закупке
   */
  @Temporal(TemporalType.TIMESTAMP)
  private Date beginDT;

  /**
   * Дата и время окончания торгов
   */
  @Temporal(TemporalType.TIMESTAMP)
  private Date endDT;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "create_dt", updatable = false)
  private Date createDT;

  /**
   * Время последнего обновления информации по закупке
   */
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "update_dt")
  private Date updateDT;

  /**
   * Время последнего обновления информации о предлагаемой стоимости
   */
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "price_update_dt")
  private Date priceUpdateDT;

  /**
   * Начальная стоимость, заданная заказчиком
   */
  private BigDecimal nmc;

  /**
   * Начальная стоимость, задананя поставщиком.
   * Этот атрибут задается при добавлении закупки в базу, и затем, когда придет время подавать предложение по этой
   * закупке это будет начальной ценой предложения.
   */
  @Column(name = "start_price")
  private BigDecimal startPrice;

  /**
   * Количество поданных предложений по данной закупке
   */
  private Integer proposalsCount;

  /**
   * Идентификатор лучшего предложения. Лучшее предложение то, которое имеет наименьшую сумму, и находится первым в
   * списке предложений
   */
  private Long bestProposalId;

  /**
   * Сумма по лучшему предложению
   */
  private BigDecimal bestPrice;

  /**
   * Минимальное пороговое значение стоимости контракта
   */
  @Column(name = "min_val", precision = 11, scale = 2, nullable = false)
  private BigDecimal minTradeVal;

  /**
   * Время в милисекундах до окончания приема предложений когда формируется реальное предложение на площадке
   */
  @Column(name = "activate_time", nullable = false)
  private Long activateTime;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "id", column = @Column(name = "proposal_id")),
      @AttributeOverride(name = "status", column = @Column(name = "proposal_status")),
      @AttributeOverride(name = "createDT", column = @Column(name = "proposal_create_dt"))
  })
  private Proposal proposal;

  /**
   * Список товаров/позиций по данной закупке
   */
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "trade_id")
  private Set<TradeProduct> tradeProducts = new HashSet<>();

  /**
   * Список товаров/позиций по предложению, поданному по данной закупке
   */
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "trade_id")
  private Set<ProposalProduct> proposalProducts = new HashSet<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  public Trade() {
    createDT = new Date();
    updateDT = new Date();
    priceUpdateDT = new Date();
  }

  public void setTradeProducts(Set<TradeProduct> products) {
    if (tradeProducts == null) {
      tradeProducts = products;
    } else if (tradeProducts != products) {
      tradeProducts.clear();
      if (products != null) {
        tradeProducts.addAll(products);
      }
    }
  }

  public void setProposalProducts(Set<ProposalProduct> products) {
    if (proposalProducts == null) {
      proposalProducts = products;
    } else if (proposalProducts != products) {
      proposalProducts.clear();
      if (products != null) {
        proposalProducts.addAll(products);
      }
    }
  }
}
