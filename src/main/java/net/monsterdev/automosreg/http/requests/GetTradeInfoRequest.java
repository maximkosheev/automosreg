package net.monsterdev.automosreg.http.requests;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetTradeInfoRequest extends TraderRequest {
    private static final String URL = "https://api.market.mosreg.ru/api/Trade/GetTradesForParticipantOrAnonymous";

    private HttpPost request;

    public GetTradeInfoRequest(Long tradeId) {
        super();
        JSONObject filter = new JSONObject();
        filter.put("classificatorCodes", new ArrayList<String>());
        filter.put("CustomerAddress", "");
        filter.put("CustomerFullNameOrInn", "");
        filter.put("filterDateFrom", JSONObject.NULL);
        filter.put("filterDateTo", JSONObject.NULL);
        filter.put("filterFillingApplicationEndDateFrom", JSONObject.NULL);
        filter.put("FilterFillingApplicationEndDateTo", JSONObject.NULL);
        filter.put("filterPriceMin", "");
        filter.put("filterPriceMax", "");
        filter.put("filterTradeEasuzNumber", "");
        filter.put("IsImmediate", false);
        filter.put("itemsPerPage", "");
        filter.put("OnlyTradesWithMyApplications", false);
        filter.put("page", 1);
        filter.put("itemsPerPage", 10);
        filter.put("ParticipantHasApplicationsOnTrade", "");
        filter.put("showOnlyOwnTrades", false);
        filter.put("sortingParams", new ArrayList<>());
        filter.put("Id", tradeId);
        filter.put("tradeState", "");
        filter.put("UsedClassificatorType", 10);
        request = new HttpPost(URL);
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader(HTTP.CONTENT_TYPE, "application/json");
        request.setHeader("Host", "api.market.mosreg.ru");
        request.setHeader("Origin", "https://market.mosreg.ru");
        request.setHeader("Referer", "https://market.mosreg.ru/");
        request.setHeader("XXX-TenantId-Header", "2");
        StringEntity se = new StringEntity(filter.toString(), ContentType.APPLICATION_JSON);
        request.setEntity(se);
    }

    @Override
    public RequestType getType() {
        return RequestType.POST;
    }

    @Override
    public HttpPost getPOSTRequest() {
        return request;
    }

}
