package net.monsterdev.automosreg.http.requests;

import net.monsterdev.automosreg.domain.Trade;
import org.apache.http.client.methods.HttpPost;

public class UpdateProposalRequest extends TraderRequest {
    private static final String URL = "https://api.market.mosreg.ru/api/Application/EditPublished";
    private static final String REFERER_URL = "https://market.mosreg.ru/Application/EditPrice?applicationId=%d";

    private HttpPost request;

    public UpdateProposalRequest(Trade trade) {
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
