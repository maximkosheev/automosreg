package net.monsterdev.automosreg.repository;

import java.util.function.Consumer;
import lombok.NonNull;
import net.monsterdev.automosreg.domain.Trade;
import net.monsterdev.automosreg.enums.TradeStatus;
import net.monsterdev.automosreg.model.StatusFilterOption;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class TradesRepository {

  @PersistenceContext
  private EntityManager em;

  /**
   * Возвращаем список из count предложений пользователя с идентификатором userId начиная с позиции startIndex
   *
   * @param userId - идентификатор пользователя
   * @param filter параметры фильтра
   */
  public List<Trade> findAll(@NonNull Long userId, Map<String, Object> filter) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Trade> query = cb.createQuery(Trade.class);
    Root<Trade> tradeRoot = query.from(Trade.class);
    query.select(tradeRoot);
    query.where(cb.equal(tradeRoot.get("user").get("id"), userId));
    if (filter.containsKey("TradeNum")) {
      query.where(cb.equal(tradeRoot.get("tradeId"), filter.get("TradeNum")));
    }
    if (filter.containsKey("TradeName")) {
      query.where(cb.like(tradeRoot.get("name"), "%" + filter.get("TradeName") + "%"));
    }
    if (filter.containsKey("BeginFrom")) {
      query.where(cb.greaterThanOrEqualTo(tradeRoot.get("beginDT"), (LocalDate) filter.get("BeginFrom")));
    }
    if (filter.containsKey("BeginTo")) {
      query.where(cb.lessThanOrEqualTo(tradeRoot.get("beginDT"), (LocalDate) filter.get("BeginTo")));
    }
    if (filter.containsKey("FinishFrom")) {
      query.where(cb.greaterThanOrEqualTo(tradeRoot.get("beginDT"), (LocalDate) filter.get("FinishFrom")));
    }
    if (filter.containsKey("FinishTo")) {
      query.where(cb.lessThanOrEqualTo(tradeRoot.get("endDT"), (LocalDate) filter.get("FinishTo")));
    }
    if (filter.containsKey("Status")) {
      StatusFilterOption statusOption = (StatusFilterOption) filter.get("Status");
      if (statusOption.getCode() > StatusFilterOption.ALL) {
        switch (statusOption.getCode()) {
          case StatusFilterOption.ARCHIVED:
            query.where(cb.equal(tradeRoot.get("status"), TradeStatus.ARCHIVED));
            break;
          case StatusFilterOption.OPENED:
            query.where(cb.and(cb.notEqual(tradeRoot.get("status"), TradeStatus.ARCHIVED),
                cb.isNull(tradeRoot.get("proposal"))));
            break;
          case StatusFilterOption.CLOSED:
            query.where(cb.or(cb.equal(tradeRoot.get("status"), TradeStatus.CANCELED),
                cb.equal(tradeRoot.get("status"), TradeStatus.CONTRACTED)));
            break;
          case StatusFilterOption.ACTIVE:
            query.where(cb.and(cb.notEqual(tradeRoot.get("status"), TradeStatus.ARCHIVED),
                cb.isNotNull(tradeRoot.get("proposal"))));
            break;
        }
      }
    }
    return em.createQuery(query).getResultList();
  }

  /**
   * Возвращает список актуальных (неархивных) закупок
   *
   * @param userId идентификатор пользователя
   * @return список актуальных закупок
   */
  public List<Trade> findAll(@NonNull Long userId) {
    TypedQuery<Trade> query = em
        .createQuery("from Trade where user_id = :userId AND status <> 'ARCHIVED'", Trade.class);
    query.setParameter("userId", userId);
    return query.getResultList();
  }

  /**
   * Возвращает список закупок по которым ведется прием предложений
   *
   * @param userId идентификатор пользователя
   * @return список закупок, по которым ведется прием предложений
   */
  public List<Trade> findAllTradeable(@NonNull Long userId) {
    TypedQuery<Trade> query = em.createQuery("from Trade where user_id = :userId AND status = :status", Trade.class);
    query.setParameter("userId", userId);
    query.setParameter("status", TradeStatus.SUGGESTIONS);
    return query.getResultList();
  }

  public Trade update(Trade trade) {
    return em.merge(trade);
  }

  public Trade findTrade(Long tradeId) {
    TypedQuery<Trade> query = em.createQuery("from Trade where trade_id = :tradeId", Trade.class);
    query.setParameter("tradeId", tradeId);
    return query.getSingleResult();
  }

  public void removeAll(List<Trade> trades) {
    trades.forEach(trade -> em.remove(trade));
  }
}
