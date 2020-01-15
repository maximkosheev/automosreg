package net.monsterdev.automosreg.services;

import net.monsterdev.automosreg.http.requests.TraderRequest;
import net.monsterdev.automosreg.http.requests.TraderResponse;

public interface HttpService {
    /**
     * Формирование и отправка HTTP запроса на сайт торговой плащадки
     * @param request - запрос
     * @return полученный ответ
     */
    TraderResponse sendRequest(TraderRequest request);
}
