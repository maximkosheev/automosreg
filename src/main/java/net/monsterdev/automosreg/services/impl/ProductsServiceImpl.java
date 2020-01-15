package net.monsterdev.automosreg.services.impl;

import net.monsterdev.automosreg.domain.ProposalProduct;
import net.monsterdev.automosreg.domain.TradeProduct;
import net.monsterdev.automosreg.exceptions.AutoMosregException;
import net.monsterdev.automosreg.http.requests.GetCreateProposalPageRequest;
import net.monsterdev.automosreg.http.requests.GetUpdateProposalPageRequest;
import net.monsterdev.automosreg.http.requests.TraderResponse;
import net.monsterdev.automosreg.services.HttpService;
import net.monsterdev.automosreg.services.ProductsService;
import net.monsterdev.automosreg.utils.StringUtil;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Qualifier("productsService")
public class ProductsServiceImpl implements ProductsService {
    private static final String ERROR_GET_PAGE_FAILED_1 = "Ошибка при получении страницы c товарами по заявке %d";
    private static final String ERROR_GET_PAGE_FAILED_2 = "Ошибка при получении страницы c товарами по предложению %d";

    @Autowired
    HttpService httpService;

    @Override
    public Set<TradeProduct> getProductsForTrade(Long tradeId) throws AutoMosregException {
        TraderResponse response = httpService.sendRequest(new GetCreateProposalPageRequest(tradeId));
        if (response.getCode() != HttpStatus.SC_OK) {
            throw new AutoMosregException(String.format(ERROR_GET_PAGE_FAILED_1, tradeId));
        }
        return StringUtil.parseTradeForProducts(response.getEntity());
    }

    @Override
    public Set<ProposalProduct> getProductsForProposal(Long proposalId) throws AutoMosregException {
        TraderResponse response = httpService.sendRequest(new GetUpdateProposalPageRequest(proposalId));
        if (response.getCode() != HttpStatus.SC_OK) {
            throw new AutoMosregException(String.format(ERROR_GET_PAGE_FAILED_2, proposalId));
        }
        return StringUtil.parseProposalForProducts(response.getEntity());
    }
}
