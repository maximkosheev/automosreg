package net.monsterdev.automosreg.behaviors.impl;

import java.util.Set;
import net.monsterdev.automosreg.algorithms.CalculateProductsPriceAlgorithm;
import net.monsterdev.automosreg.behaviors.TraderBehavior;
import net.monsterdev.automosreg.domain.ProposalProduct;
import net.monsterdev.automosreg.domain.Trade;
import net.monsterdev.automosreg.exceptions.AutoMosregException;
import net.monsterdev.automosreg.model.Money;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.monsterdev.automosreg.model.dto.ProductDto;
import org.dozer.DozerBeanMapper;

/**
 * Класс SlowDecreaseOnAllProductsBehavior реализует стратегию постепенного снижения стоимости предложения. По этой
 * стратегии список торговых позиций сортируется по их кол-ву. Затем начинаем снижать цену по первой торговой позиции в
 * отсортированном списке до минимально возможной величины. Когда по первой торговой позиции снизить цену уже нельзя
 * (грубо говоря она равна 1 коп), начинаем снижать по второй и т.д.
 */
public class SlowDecreaseOnAllProductsBehavior implements TraderBehavior {
  private final DozerBeanMapper DOZZER_MAPPER = new DozerBeanMapper();
  private final CalculateProductsPriceAlgorithm algorithm = new CalculateProductsPriceAlgorithm();

  @Override
  public Trade doTrade(Trade trade, BigDecimal requiredPrice) throws AutoMosregException {
    Set<ProductDto> products = trade.getProposalProducts().stream()
        .map(proposalProduct -> DOZZER_MAPPER.map(proposalProduct, ProductDto.class)).collect(Collectors.toSet());
    algorithm.doCalc(products, requiredPrice);
    for (ProposalProduct proposalProduct : trade.getProposalProducts()) {
      if (proposalProduct.getId() != null) {
        for (ProductDto productDto : products) {
          if (productDto.getId().equals(proposalProduct.getId())) {
            DOZZER_MAPPER.map(productDto, proposalProduct);
          }
        }
      }
    }
    return trade;
  }
}
