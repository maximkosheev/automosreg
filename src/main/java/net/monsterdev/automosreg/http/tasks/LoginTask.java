package net.monsterdev.automosreg.http.tasks;

import javafx.concurrent.Task;
import net.monsterdev.automosreg.exceptions.AutoMosregException;
import net.monsterdev.automosreg.http.*;
import net.monsterdev.automosreg.http.requests.GetAccountPageRequest;
import net.monsterdev.automosreg.http.requests.GetLoginPageRequest;
import net.monsterdev.automosreg.http.requests.LoginRequest;
import net.monsterdev.automosreg.http.requests.TraderResponse;
import net.monsterdev.automosreg.model.CertificateInfo;
import net.monsterdev.automosreg.services.CryptoService;
import net.monsterdev.automosreg.services.HttpService;
import net.monsterdev.automosreg.utils.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_OK;

public class LoginTask extends Task<Boolean> {
    private static final String ERROR_COMMON_LOG_MSG = "LoginTask error: %s : %s";
    private static final String LOGIN_ERROR_1 = "Ошибка при запросе страницы входа";
    private static final String LOGIN_ERROR_2 = "Ошибка авторизации";
    private static final String LOGIN_ERROR_3 = "Ошибка авторизации: ЭЦП не настроена";
    private static final String LOGIN_ERROR_4 = "Ошибка получения страницы аккаунта";

    private static final Logger LOG = LogManager.getLogger(LoginTask.class);

    private CertificateInfo certInfo;

    @Autowired
    private HttpService httpService;

    @Autowired
    private CryptoService cryptoService;

    public LoginTask(CertificateInfo certInfo) {
        this.certInfo = certInfo;
    }

    @Override
    protected Boolean call() throws Exception {
        try {
            LOG.trace("Trying to authorize");
            // 1. Получить страницу входа
            String loginPageContent = Objects.requireNonNull(httpService.sendRequest(new GetLoginPageRequest()).getEntity(), LOGIN_ERROR_1);
            /* 2. Из полученной страницы входа нужно вычленить вот такую строку window.dateDateForSign = "MTIvMTIvMjAxOCAxODoxMjowOA==" */
            String regexp = "window.dateDateForSign\\s?=\\s?\"(\\w+==)\"";
            Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(loginPageContent);
            // 3. Из полученной строки выбираем данные, которые нужно подписать
            String dataForSign = matcher.find() ? matcher.group(1) : null;
            // 4. Подписываем данные ЭЦП, относящуюся к выбранному пользователю
            byte[] signature = cryptoService.signData(certInfo, dataForSign);
            // 5. Отправка данных формы входа обратно на сервер
            // Если авторизация прошла успешно, площадка перенаправляет нас на тот URL, указанный как returnUrl.
            if (signature == null)
                throw new AutoMosregException(LOGIN_ERROR_3);
            if (httpService.sendRequest(new LoginRequest("/", certInfo.getHash(), new String(signature))).getCode() != SC_MOVED_TEMPORARILY)
                throw new AutoMosregException(LOGIN_ERROR_2);
            // 6. А мы пойдем на страницу аккаунта текущего пользователя, чтобы получить код авторизации
            TraderResponse response = httpService.sendRequest(new GetAccountPageRequest());
            if (response.getCode() != SC_OK)
                throw new AutoMosregException(LOGIN_ERROR_4);
            String authCode = StringUtil.parseForAuthCode(response.getEntity());
            Session.getInstance().setProperty("authCode", authCode);
            LOG.trace("Authorization is finished");
            return true;
        } catch (Throwable t) {
            LOG.error(String.format(ERROR_COMMON_LOG_MSG, t.getClass(), t.getMessage()));
            throw t;
        }
    }
}
