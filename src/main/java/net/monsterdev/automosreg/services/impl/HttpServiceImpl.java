package net.monsterdev.automosreg.services.impl;

import net.monsterdev.automosreg.http.requests.TraderRequest;
import net.monsterdev.automosreg.http.requests.TraderResponse;
import net.monsterdev.automosreg.services.HttpService;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Service
@Qualifier("httpService")
@Scope("prototype")
public class HttpServiceImpl implements HttpService {
    private static final String ERROR_COMMON_LOG_MSG = "HttpService error: %s : %s";
    private static Logger LOG = LogManager.getLogger(HttpServiceImpl.class);

    @Autowired
    private CookieStore cookieStore;

    @Override
    public TraderResponse sendRequest(TraderRequest request) {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            SSLConnectionSocketFactory sslConnectionSocketFactory =
                    new SSLConnectionSocketFactory(sslContext);

            CloseableHttpClient httpClient = HttpClientBuilder.create()
                    .setDefaultCookieStore(cookieStore)
                    .setSSLSocketFactory(sslConnectionSocketFactory)
                    .build();

            LOG.trace("Http request << " + request.toString());
            HttpResponse httpResponse = httpClient.execute(request.getType() == TraderRequest.RequestType.POST
                    ? request.getPOSTRequest()
                    : request.getGETRequest());
            TraderResponse response = new TraderResponse();
            response.setCode(httpResponse.getStatusLine().getStatusCode());
            response.setEntity(httpResponse.getEntity() != null ? EntityUtils.toString(httpResponse.getEntity()) : null);
            Header contentHeader = httpResponse.getFirstHeader("Content-Type");
            if (contentHeader != null && contentHeader.getValue().contains("text/html"))
                LOG.trace("Http response >> BIG HTML PAGE");
            else
                LOG.trace("Http response >> " + response.getEntity());
            return response;
        } catch (Throwable t) {
            LOG.error(String.format(ERROR_COMMON_LOG_MSG, t.getClass(), t.getMessage()));
            return null;
        }
    }
}
