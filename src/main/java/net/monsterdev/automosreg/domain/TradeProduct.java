package net.monsterdev.automosreg.domain;

import lombok.Data;
import net.monsterdev.automosreg.model.Money;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;

/**
 * Класс TradeProduct содержит информацию об отдельной позиции в некоторой закупке. В терминах торговой площадки
 * эти позиции называются продуктами. В одной закупке может быть несколько позиций.
 * Позиция может быть единичной (quantity = 1), а может быть множественной (quantity = n).
 * По поводу продуктов нужно кое что уточнить... Дело в том, что на площадке есть набор продуктов/позиций,
 * связанный с закупкой и этот набор общий для всех поставщиков. Когда поставщик создает предложение создается
 * еще ошин набор продуктов/позиций, но уже связанный с предложением. В принципе, для каждого конкретного поставщика
 * эти наборы одинаковые, за исключением идентификаторов товаров/позиций и цены. В наборе, связанном с закупкой
 * храняться товары и их цены, заданная заказчиком, а в наборе, связанном с предложением, хранятся товары и их цены,
 * заданные поставщиком
 */
@Data
@Entity
@Table(name = "trades_products")
public class TradeProduct {
    @Id
    private Long id;
    private String okeiCode;
    private String okeiDescription;
    private String classificatorCode;
    private String classificatorDescription;
    private String classificatorType;
    private BigDecimal quantity;
    private String name;
    private Integer positionNumber;
    private BigDecimal price;
    private BigDecimal summ;
    private Long externalId;

    private void calcSumm() {
        if (quantity == null || price == null) {
            summ = null;
        }
        else {
            summ = price.multiply(quantity);
        }
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        calcSumm();
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        calcSumm();
    }
}
