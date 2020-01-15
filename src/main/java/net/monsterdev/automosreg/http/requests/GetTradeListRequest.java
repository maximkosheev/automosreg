package net.monsterdev.automosreg.http.requests;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

public class GetTradeListRequest extends TraderRequest {
    private static final String URL = "https://api.market.mosreg.ru/api/Trade/GetTradesForParticipantOrAnonymous";

    private HttpPost request;

    public GetTradeListRequest(JSONObject filter) {
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
