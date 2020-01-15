package net.monsterdev.automosreg.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProposalsInfoDto {
    private int totalpages;
    private int currpage;
    private int totalrecords;
    @JsonProperty("invdata")
    List<ProposalInfoDto> proposals;
}
