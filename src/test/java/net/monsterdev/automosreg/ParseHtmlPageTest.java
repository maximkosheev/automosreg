package net.monsterdev.automosreg;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.monsterdev.automosreg.model.dto.ProposalEditPriceDto;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParseHtmlPageTest {

  @Test
  public void parseCreateProposalPageForAuthorizationHeaderTest() throws IOException {
    String authRegExp = "'Authorization', '(Bearer \\S+)'";
    Pattern pattern = Pattern.compile(authRegExp, Pattern.CASE_INSENSITIVE);
    String content = IOUtils.toString(
        this.getClass().getResourceAsStream("/net/monsterdev/automosreg/CreateProposal.html"),
        Charset.forName("UTF-8"));
    Matcher matcher = pattern.matcher(content);
    String authCode = matcher.find() ? matcher.group(1) : null;
    assertNotNull(authCode);
    System.out.println(authCode);
  }

  @Test
  public void parseTradeInfoPageForProposalsTest() throws Exception {
    String content = IOUtils.toString(
        this.getClass().getResourceAsStream("/net/monsterdev/automosreg/TradeInfo.html"),
        Charset.forName("UTF-8"));
    String proposalInfoRegExp = "AddApplication\\(\\{(.+?)\\}\\);";
    String proposalInfoPartsRegExp = "Id: (\\d+).*?Price: ([+-]?\\d*\\.\\d{2})";
    Pattern pattern = Pattern
        .compile(proposalInfoRegExp, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    Pattern patternParts = Pattern
        .compile(proposalInfoPartsRegExp, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(content);
    int count = 0;
    while (matcher.find()) {
      String proposalInfo = matcher.group(1);
      System.out.println(proposalInfo);
      Matcher matcher1 = patternParts.matcher(proposalInfo);
      if (matcher1.find()) {
        System.out.println("\tId: " + matcher1.group(1));
        System.out.println("\tPrice: " + matcher1.group(2));
      }
      count += 1;
    }
    assertTrue(count > 0);
  }

  @Test
  public void parseProposalEditPageForProductsTest() throws Exception {
    String content = IOUtils.toString(
        this.getClass().getResourceAsStream("/net/monsterdev/automosreg/viewthis/EditProposal.html"),
        Charset.forName("UTF-8"));
    String productsInfoRegExp = "ko\\.mapping\\.fromJSON\\('(.+?)', mapping, vmApplication\\)";
    Pattern pattern = Pattern
        .compile(productsInfoRegExp, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(content);
    String productsInfo = matcher.find() ? matcher.group(1) : null;
    assertNotNull(productsInfo);
    productsInfo = productsInfo.replaceAll("\\\\{3}", "~~~");
    productsInfo = productsInfo.replaceAll("\\\\\"", "\"");
    productsInfo = productsInfo.replaceAll("~~~", "\\\\");
    ObjectMapper mapper = new ObjectMapper();
    ProposalEditPriceDto dto = mapper.readValue(productsInfo, ProposalEditPriceDto.class);
    assertNotNull(dto);
  }

  @Test
  public void mapProposalEditPriceTest() throws Exception {
    String content = IOUtils
        .toString(getClass().getResourceAsStream("/ProposalEditPriceRequest.json"), Charset.forName("UTF-8"));
    ObjectMapper mapper = new ObjectMapper();
    ProposalEditPriceDto proposalEditPrice = mapper.readValue(content, ProposalEditPriceDto.class);
    assertNotNull(proposalEditPrice);
  }
}
