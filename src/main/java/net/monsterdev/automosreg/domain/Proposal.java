package net.monsterdev.automosreg.domain;

import lombok.Data;
import net.monsterdev.automosreg.enums.ProposalStatus;

import javax.persistence.*;
import java.util.Date;

/**
 * Proposal class
 */
@Data
@Embeddable
public class Proposal {
    /** Идентификатор предложения на площадке */
    private Long id;

    /** Статус данного предложения */
    @Enumerated(EnumType.STRING)
    private ProposalStatus status = ProposalStatus.INITIAL;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createDT;

    public Proposal() {
        createDT = new Date();
    }
}
