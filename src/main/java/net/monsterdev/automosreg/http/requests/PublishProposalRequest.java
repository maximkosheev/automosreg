package net.monsterdev.automosreg.http.requests;

import net.monsterdev.automosreg.http.Session;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

public class PublishProposalRequest extends TraderRequest {
    private static final String URL = "https://api.market.mosreg.ru/api/Trade/%d/Application/Publish";
    private static final String REFERER_URL = "https://market.mosreg.ru/Application/Create?tradeId=%d";

    private HttpPost request;

    public PublishProposalRequest(long tradeId, JSONObject proposal) {
        request = new HttpPost(String.format(URL, tradeId));
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader(HTTP.CONTENT_TYPE, "application/json");
        request.setHeader("Host", "api.market.mosreg.ru");
        request.setHeader("Origin", "https://market.mosreg.ru");
        request.setHeader("Referer", String.format(REFERER_URL, tradeId));
        request.setHeader("XXX-TenantId-Header", "2");
        request.setHeader("Authorization", Session.getInstance().getProperty("authCode"));
        StringEntity se = new StringEntity(proposal.toString(), ContentType.APPLICATION_JSON);
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
