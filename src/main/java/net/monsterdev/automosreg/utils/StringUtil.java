package net.monsterdev.automosreg.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Date;
import net.monsterdev.automosreg.domain.ProposalProduct;
import net.monsterdev.automosreg.domain.TradeProduct;
import net.monsterdev.automosreg.model.SupplierProposal;
import net.monsterdev.automosreg.model.dto.ProductDto;
import net.monsterdev.automosreg.model.dto.ProposalEditPriceDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

  /**
   * Удаляет начальные и конечные пробельные символы
   *
   * @param source - исходная строка
   * @return измененная строка
   */
  public static String removeWhitespaces(String source) {
    StringBuilder builder = new StringBuilder();
    builder.append(source);
    while (Character.isWhitespace(builder.charAt(0))) {
      builder.deleteCharAt(0);
    }
    while (Character.isWhitespace(builder.charAt(builder.length() - 1))) {
      builder.deleteCharAt(builder.length() - 1);
    }
    return builder.toString();
  }

  /**
   * Удаляет из строки все указанные подстроки
   *
   * @param source - исходная строка
   * @param strings - подстроки, которые нужно удалить
   * @return измененная строка
   */
  public static String removeAll(String source, String... strings) {
    StringBuilder builder = new StringBuilder();
    builder.append(source);
    for (String substr : strings) {
      int pos;
      while ((pos = builder.indexOf(substr)) > 0) {
        builder.delete(pos, pos + substr.length());
      }
    }
    return builder.toString();
  }

  public static Long toLong(String str) {
    try {
      return Long.parseLong(str);
    } catch (Exception e) {
      return null;
    }
  }

  public static String fromLocalDate(LocalDateTime dt) {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    if (dt == null) {
      return "";
    }
    return dt.format(dateTimeFormatter);
  }

  public static String fromLocalDate(LocalDate dt) {
    if (dt == null) {
      return "";
    }
    return fromLocalDate(dt.atTime(0, 0, 0, 0));
  }

  public static LocalDateTime toLocalDateTime(String string) {
    try {
      DateTimeFormatter formatter = new DateTimeFormatterBuilder()
          .parseCaseInsensitive()
          .parseLenient()
          .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
          .optionalStart()
          .appendPattern(".SSS")
          .optionalEnd()
          .optionalStart()
          .appendPattern("Z")
          .optionalEnd()
          .toFormatter();
      // Все это, конечно, замечательно... Всякие DateTimeFormatter'ы и пр...
      // Вот только хрен зает как это работает. От сервера бывает приходит дата в формате
      // yyyy-MM-dd'T'HH:mm:ss.SSS'Z', бывает yyyy-MM-dd'T'HH:mm:ss'Z', бывает yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'
      // и как все эти форматы объединить... Короче берем просто первые N символов (дата и время с точностью до сек.)

      return LocalDateTime.parse(string.substring(0, 19), formatter);
    } catch (Exception e) {
      return null;
    }
  }

  public static String toNotNull(String string) {
    if (string == null) {
      return "";
    }
    return string;
  }

  public static String parseForAuthCode(String content) {
    String authRegExp = "'Authorization', '(Bearer \\S+)'";
    Pattern pattern = Pattern.compile(authRegExp, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(content);
    return matcher.find() ? matcher.group(1) : null;
  }

  public static Set<TradeProduct> parseTradeForProducts(String content) {
    String productRegExp = "product\\.init\\((('([^']*)'[,\\s]*)+)\\);";
    Pattern pattern = Pattern.compile(productRegExp, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(content);
    Set<TradeProduct> products = new HashSet<>();
    while (matcher.find()) {
      String argRegExp = "'([^']*)'(,\\s*)?";
      Pattern argPattern = Pattern.compile(argRegExp, Pattern.CASE_INSENSITIVE);
      String productInitData = matcher.group(1);
      Matcher argMatcher = argPattern.matcher(productInitData);
      List<String> productInitParams = new ArrayList<>(11);
      while (argMatcher.find()) {
        productInitParams.add(argMatcher.group(1));
      }
      TradeProduct productInfo = new TradeProduct();
      productInfo.setId(Long.parseLong(productInitParams.get(10)));
      productInfo.setOkeiCode(productInitParams.get(0));
      productInfo.setOkeiDescription(productInitParams.get(1));
      productInfo.setClassificatorCode(productInitParams.get(2));
      productInfo.setClassificatorDescription(productInitParams.get(3));
      productInfo.setClassificatorType(productInitParams.get(4));
      productInfo.setQuantity(new BigDecimal(productInitParams.get(5).replace(',', '.')));
      productInfo.setName(productInitParams.get(6));
      productInfo.setPrice(new BigDecimal(productInitParams.get(7).replace(',', '.')));
      productInfo.setPositionNumber(Integer.parseInt(productInitParams.get(8)));
      productInfo.setExternalId(Long.parseLong(productInitParams.get(9)));
      products.add(productInfo);
    }
    return products;
  }

  public static Set<ProposalProduct> parseProposalForProducts(String content) {
    Set<ProposalProduct> proposalProducts = new HashSet<>();
    String productsInfoRegExp = "ko\\.mapping\\.fromJSON\\('(.+?)', mapping, vmApplication\\)";
    Pattern pattern = Pattern
        .compile(productsInfoRegExp, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(content);
    if (!matcher.find()) {
      return proposalProducts;
    }
    String productsInfo = matcher.group(1);
    productsInfo = productsInfo.replaceAll("\\\\{3}", "~~~");
    productsInfo = productsInfo.replaceAll("\\\\\"", "\"");
    productsInfo = productsInfo.replaceAll("~~~", "\\\\");
    try {
      ObjectMapper mapper = new ObjectMapper();
      ProposalEditPriceDto dto = mapper.readValue(productsInfo, ProposalEditPriceDto.class);
      for (ProductDto product : dto.getProducts()) {
        ProposalProduct proposalProduct = new ProposalProduct();
        proposalProduct.setId(product.getId());
        proposalProduct.setExternalId(product.getExternalId());
        proposalProduct.setName(product.getName());
        proposalProduct.setPositionNumber(product.getPositionNumber());
        proposalProduct.setPrice(product.getPrice());
        proposalProduct.setQuantity(product.getQuantity());
        proposalProduct.setOkeiCode(product.getOkeiCode());
        proposalProduct.setOkeiDescription(product.getOkeiDescription());
        proposalProduct.setClassificatorCode(product.getClassificatorCode());
        proposalProduct.setClassificatorType(product.getClassificatorType());
        proposalProduct.setClassificatorDescription(product.getClassificatorDescription());
        proposalProducts.add(proposalProduct);
      }
    } catch (Exception ex) {
      proposalProducts.clear();
    }
    return proposalProducts;
  }

  public static List<SupplierProposal> parseForProposals(String content) {
    List<SupplierProposal> proposals = new ArrayList<>();
    String infoRegExp = "AddApplication\\(\\{(.+?)\\}\\);";
    String partsRegExp = "Id: (\\d+).*?Price: ([+-]?\\d*\\.\\d{2})";
    Pattern infoPattern = Pattern.compile(infoRegExp, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    Pattern partsPattern = Pattern.compile(partsRegExp, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    Matcher infoMatcher = infoPattern.matcher(content);
    while (infoMatcher.find()) {
      String proposalInfo = infoMatcher.group(1);
      Matcher partsMatcher = partsPattern.matcher(proposalInfo);
      if (partsMatcher.find()) {
        SupplierProposal proposal = new SupplierProposal();
        proposal.setId(Long.parseLong(partsMatcher.group(1)));
        proposal.setPrice(new BigDecimal(partsMatcher.group(2)));
        proposals.add(proposal);
      }
    }
    return proposals;
  }

}
