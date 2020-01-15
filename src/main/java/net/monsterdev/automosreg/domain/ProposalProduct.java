package net.monsterdev.automosreg.domain;

import lombok.Data;
import net.monsterdev.automosreg.model.Money;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Класс ProposalProduct содержит информацию об отдельной позиции в предложении к некоторой закупке.
 * В терминах торговой площадки эти позиции называются продуктами. В одном предложении может быть несколько позиций.
 * Позиция может быть единичной (quantity = 1), а может быть множественной (quantity = n).
 * Во время торгов нужно задавать цену по каждой позиции из данной закупки.
 * Например, закупка продуктов в столовую. В этой закупке могут быть следующие позиции (товары): картофель, лук, мясо...
 * При этом картоферя - 10 кг, лук - 3 кг, мясо - 4 кг. Во время торгов необходимо задать ЦЕНУ каждой позиции.
 */
@Data
@Entity
@Table(name = "proposals_products")
public class ProposalProduct {
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
    private Long externalId;

    public BigDecimal getSumm() {
        if (quantity == null || price == null) {
            return null;
        }
        else {
            return price.multiply(quantity);
        }
    }

    public BigDecimal getMinCost() {
        return Money.MIN_PRICE.multiply(quantity);
    }
}
