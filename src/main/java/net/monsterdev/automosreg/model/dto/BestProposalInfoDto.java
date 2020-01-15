package net.monsterdev.automosreg.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BestProposalInfoDto {
    private Integer totalCount;
    private BigDecimal bestPrice;
    private Long bestProposalId;
}
