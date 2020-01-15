package net.monsterdev.automosreg.http.requests;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;

public class GetCreateProposalPageRequest extends TraderRequest {
    private static final String URL = "https://market.mosreg.ru/Application/Create?tradeId=%d";
    private static final String REFERER_URI = "https://market.mosreg.ru/Trade/ViewTrade";

    private HttpGet request;

    public GetCreateProposalPageRequest(long tradeId) {
        request = new HttpGet(String.format(URL, tradeId));
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Host", "market.mosreg.ru");
        request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        request.setHeader("Accept-Encoding", "gzip, deflate, br");
        request.setHeader("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
        request.setHeader("Referer", REFERER_URI + "?id="+String.valueOf(tradeId));
        request.setHeader("Upgrade-Insecure-Requests", "1");
    }

    @Override
    public RequestType getType() {
        return RequestType.GET;
    }

    @Override
    public HttpGet getGETRequest() {
        return request;
    }
}
