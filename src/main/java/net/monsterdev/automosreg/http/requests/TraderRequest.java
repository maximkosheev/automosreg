package net.monsterdev.automosreg.http.requests;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Базовый класс для всех запросов к торговой плащадке
 */
public abstract class TraderRequest {
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:63.0) Gecko/20100101 Firefox/63.0";

    /** Типы запросов */
    public enum RequestType {
        GET,
        POST
    }

    /** Возвращает тип данного запроса */
    public abstract RequestType getType();

    /**
     * Возвращает подготовленный HttpPost запрос
     * @return HttpPost-запрос
     */
    public HttpPost getPOSTRequest() {
        return null;
    }

    /**
     * Возвращает подготовленный HttpGet запрос
     * @return HttpGet-запрос
     */
    public HttpGet getGETRequest() {
        return null;
    }

    @Override
    public String toString() {
        try {
            switch (getType()) {
                case GET:
                    return "" + getClass() + " : " + getGETRequest().getURI().toString();
                case POST:
                    return "" + getClass() + " : " + EntityUtils.toString(getPOSTRequest().getEntity());
                default:
                    return "" + getClass() + " : ";
            }
        } catch (IOException ex) {
            return "";
        }
    }
}
