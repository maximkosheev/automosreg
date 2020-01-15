package net.monsterdev.automosreg.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradesInfoDto {
    private Integer totalpages;
    private Integer currpage;
    private Integer totalrecords;
    @JsonProperty("invdata")
    private List<TradeInfoDto> trades;
}
