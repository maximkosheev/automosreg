package net.monsterdev.automosreg.services;

import lombok.NonNull;
import net.monsterdev.automosreg.domain.ProposalProduct;
import net.monsterdev.automosreg.domain.TradeProduct;
import net.monsterdev.automosreg.exceptions.AutoMosregException;

import java.util.Set;

public interface ProductsService {
    /**
     * Возвращает список товаров/позиций по закупке с идентификатором tradeId
     * @param tradeId идентификатор закупки
     * @return список товаров/позиций
     */
    Set<TradeProduct> getProductsForTrade(@NonNull Long tradeId) throws AutoMosregException;

    /**
     * Возвращает список товаров/позиций по предложению
     * @param proposalId идентификатор предложения
     * @return список товаров/позиций
     */
    Set<ProposalProduct> getProductsForProposal(@NonNull Long proposalId) throws AutoMosregException;
}
