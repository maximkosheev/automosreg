package net.monsterdev.automosreg.http.requests;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class LoginRequest extends TraderRequest {
    private static final String loginURL = "https://market.mosreg.ru/Account/SignIn";

    private HttpPost request;

    public LoginRequest(String returnUrl, String certificateHash, String signature) {
        request = new HttpPost(loginURL);
        List<NameValuePair> postParams = new ArrayList<>();
        postParams.add(new BasicNameValuePair("ReturnUrl", returnUrl));
        postParams.add(new BasicNameValuePair("CertificateHash", certificateHash));
        postParams.add(new BasicNameValuePair("Signature", signature));
        postParams.add(new BasicNameValuePair("Role", "Participant"));
        HttpEntity entity = new UrlEncodedFormEntity(postParams, Consts.UTF_8);
        request.setEntity(entity);
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
