package net.monsterdev.automosreg.http.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
import net.monsterdev.automosreg.exceptions.AutoMosregException;
import net.monsterdev.automosreg.http.requests.GetTradeListRequest;
import net.monsterdev.automosreg.http.requests.TraderResponse;
import net.monsterdev.automosreg.model.dto.TradesInfoDto;
import net.monsterdev.automosreg.services.HttpService;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Класс, описывающий задачу получения информации о закупках, удовлетворяющих фильтру, который задал пользователь
 * @author madmax
 */
public class GetFilteredTradesTask extends Task<TradesInfoDto> {
    private static final String GET_FILTERED_TRADES_1 = "Ошибка при получении списка закупок";

    @Autowired
    private HttpService httpService;

    private JSONObject filter;

    public GetFilteredTradesTask(JSONObject filter) {
        this.filter = filter;
    }

    @Override
    protected TradesInfoDto call() throws Exception {
        TraderResponse response = httpService.sendRequest(new GetTradeListRequest(filter));
        if (response.getCode() != HttpStatus.SC_OK) {
            throw new AutoMosregException(GET_FILTERED_TRADES_1);
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response.getEntity(), TradesInfoDto.class);
    }
}
