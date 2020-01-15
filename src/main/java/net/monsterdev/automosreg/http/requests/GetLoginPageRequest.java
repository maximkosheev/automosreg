package net.monsterdev.automosreg.http.requests;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;

public class GetLoginPageRequest extends TraderRequest {
    private static final String loginUrl = "https://market.mosreg.ru/Account/SignInByRole";

    private HttpGet request;

    public GetLoginPageRequest() {
        try {
            URIBuilder builder = new URIBuilder(loginUrl)
                    .addParameter("returnUrl", "/")
                    .addParameter("role", "Participant");
            request = new HttpGet(builder.build());
            request.setHeader("User-Agent", USER_AGENT);
            request.setHeader("Host", "market.mosreg.ru");
            request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            request.setHeader("Accept-Encoding", "gzip, deflate, br");
            request.setHeader("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
            request.setHeader("Upgrade-Insecure-Requests", "1");
        } catch (URISyntaxException e) {
            request = null;
        }

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
