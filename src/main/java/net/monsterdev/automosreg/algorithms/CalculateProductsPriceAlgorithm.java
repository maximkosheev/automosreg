package net.monsterdev.automosreg.algorithms;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.monsterdev.automosreg.domain.ProposalProduct;
import net.monsterdev.automosreg.exceptions.AutoMosregException;
import net.monsterdev.automosreg.model.Money;
import net.monsterdev.automosreg.model.dto.ProductDto;

/**
 * Реализует алгорит расчет стоимости позиций закупки с учетом количества этих позиций, так чтобы итоговая суммарная
 * стоимость всех позиций была максимально близкой к заданной, но не превышала ее
 */
public class CalculateProductsPriceAlgorithm {
  private static final String ERROR_REQUIRED_PRICE_IS_TOO_LOW = "Требуемая стоимость слишком низкая";

  private final BigDecimal traderStep = new BigDecimal("0.01");

  public Set<ProductDto> doCalc(Set<ProductDto> products, BigDecimal requiredPrice) throws AutoMosregException {
    BigDecimal totalPrice;
    boolean done = false;
    // сортировка позиций по их кол-ву в порядке увеличения
    List<ProductDto> productList = products.stream()
        .sorted(Comparator.comparing(ProductDto::getQuantity))
        .collect(Collectors.toList());
    // начинаем снижение цены торговых позиций.
    for (ProductDto product : productList) {
      // снижаем цену до тех пор пока: либо не достигли минимально возможной цены, и тогда переходим на следующую
      // торговую позицию; либо не достигли требуемой стоимости всего предложения, и тогда вообще завершаем метод
      while (!done && product.getPrice().compareTo(Money.MIN_PRICE) > 0) {
        product.setPrice(product.getPrice().subtract(traderStep));
        // вычисляем стоимость всей закупки
        totalPrice = products.stream().map(ProductDto::getSumm).reduce(BigDecimal.ZERO, BigDecimal::add);
        // постоянно контролируем общую стоимость предложения
        if (totalPrice.compareTo(requiredPrice) <= 0)
          done = true;
      }
    }
    // если снизить стоимость до требуемого уровня не удалось - ошибка
    if (!done)
      throw new AutoMosregException(ERROR_REQUIRED_PRICE_IS_TOO_LOW);
    return products;
  }
}
