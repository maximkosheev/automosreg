package net.monsterdev.automosreg.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeInfoDto {
    @JsonProperty("Id")
    private Long id;
    @JsonProperty("TradeState")
    private Integer tradeState;
    @JsonProperty("TradeStateName")
    private String tradeStateName;
    @JsonProperty("CustomerFullName")
    private String customerFullName;
    @JsonProperty("TradeName")
    private String tradeName;
    @JsonProperty("InitialPrice")
    @JsonFormat(shape= JsonFormat.Shape.STRING)
    private BigDecimal initialPrice;
    @JsonProperty("IsInitialPriceDefined")
    private Boolean isInitialPriceDefined;
    @JsonProperty("FillingApplicationEndDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date fillingApplicationEndDate;
    @JsonProperty("PublicationDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date publicationDate;
    @JsonProperty("ApplicationsCount")
    private Integer applicationsCount;
    @JsonProperty("IsImmediate")
    private Boolean isImmediate;
    @JsonProperty("ParticipantHasApplicationsOnTrade")
    private Boolean participantHasApplicationsOnTrade;
    @JsonProperty("HasDealSignedOutsideEShop")
    private Boolean hasDealSignedOutsideEShop;
    @JsonProperty("LastModificationDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date lastModificationDate;
    @JsonProperty("CategoryId")
    private Long categoryId;
    @JsonProperty("CategoryName")
    private String categoryName;
    @JsonProperty("TypeOfCategory")
    private Integer typeOfCategory;
    @JsonProperty("OrganizationId")
    private Long organizationId;
}
