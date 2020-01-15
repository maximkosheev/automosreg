package net.monsterdev.automosreg;

import net.monsterdev.automosreg.enums.TradeStatus;
import net.monsterdev.automosreg.repository.TradesRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CriteriaTest {
    @Autowired
    private TradesRepository tradesRepository;

    @Test
    public void filterTradesTest() throws Exception {
        Map<String, Object> filterOptions = new HashMap<>();
        filterOptions.put("TradeNum", 33);
        filterOptions.put("TradeName", "XXX");
        filterOptions.put("BeginFrom", new Date());
        filterOptions.put("BeginTo", new Date());
        filterOptions.put("FinishFrom", new Date());
        filterOptions.put("FinishTo", new Date());
        filterOptions.put("Status", TradeStatus.SUGGESTIONS);
        tradesRepository.findAll(1L, filterOptions);
    }
}
