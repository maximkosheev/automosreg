package net.monsterdev.automosreg.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDto {

  @JsonProperty("Id")
  private Long id;
  @JsonProperty("OkeiCode")
  private String okeiCode;
  @JsonProperty("OkeiDescription")
  private String okeiDescription;
  @JsonProperty("ClassificatorCode")
  private String classificatorCode;
  @JsonProperty("ClassificatorDescription")
  private String classificatorDescription;
  @JsonProperty("ClassificatorType")
  private String classificatorType;
  @JsonProperty("Quantity")
  private BigDecimal quantity;
  @JsonProperty("Name")
  private String name;
  @JsonProperty("PositionNumber")
  private Integer positionNumber;
  @JsonProperty("Price")
  private BigDecimal price;
  //@JsonProperty("Summ")
  //private BigDecimal summ;
  @JsonProperty("ExternalId")
  private Long externalId;

  public BigDecimal getSumm() {
    if (quantity == null || price == null) {
      return null;
    }
    else {
      return price.multiply(quantity);
    }
  }
}
