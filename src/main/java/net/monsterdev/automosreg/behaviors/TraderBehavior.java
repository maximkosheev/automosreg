package net.monsterdev.automosreg.behaviors;

import net.monsterdev.automosreg.domain.Trade;
import net.monsterdev.automosreg.exceptions.AutoMosregException;

import java.math.BigDecimal;

public interface TraderBehavior {
    /**
     * Подготавливает новое торговое предложение в зависимости от выбранной стратегии
     * @param trade закупка по которой ведутся торги
     * @param requiredPrice стоимость до которой нужно снизить предложение
     * @return закупка обновленная в соответствии со стратегий торгов, но не сохраненная
     * @throws AutoMosregException выбрасывается при всяких разных ошибках
     */
    Trade doTrade(Trade trade, BigDecimal requiredPrice) throws AutoMosregException;
}
