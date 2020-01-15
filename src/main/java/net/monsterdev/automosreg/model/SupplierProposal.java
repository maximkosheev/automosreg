package net.monsterdev.automosreg.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Класс описывающий некое предложение поставщика (текущего пользователя или другое)
 */
@Data
public class SupplierProposal {
    private Long id;
    private BigDecimal price;
}
