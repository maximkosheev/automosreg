package net.monsterdev.automosreg.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProposalInfoDto {
    @JsonProperty("IncomingNumber")
    private Long incomingNumber;
    @JsonProperty("TradeNumber")
    private Long tradeNumber;
    @JsonProperty("TradeName")
    private String tradeName;
    @JsonProperty("PublishDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date publishDate;
    @JsonProperty("RevokeDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date revokeDate;
    @JsonProperty("State")
    private Integer state;
    @JsonProperty("StateName")
    private String stateName;
    @JsonProperty("TradeStateName")
    private String tradeStateName;
    @JsonProperty("VatRateState")
    private Integer vatRateState;
    @JsonProperty("VatRateDescription")
    private String vatRateDescription;
    @JsonProperty("VatRate")
    @JsonFormat(shape= JsonFormat.Shape.STRING)
    private BigDecimal vatRate;
}
