package bdn.quantum.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bdn.quantum.model.BasketEntity;
import bdn.quantum.model.PortfolioData;
import bdn.quantum.model.Security;
import bdn.quantum.model.Transaction;

@Service("portfolioService")
public class PortfolioServiceImpl implements PortfolioService {

	@Autowired
	AssetService assetService;
	@Autowired
	TransactionService transactionService;
	
	@Override
	public PortfolioData getPortfolioData() {
		Iterable<BasketEntity> basketIter = assetService.getBaskets();
		Iterable<Security> securities = assetService.getSecurities();
		List<Transaction> transactions = new ArrayList<Transaction>();
		
		Iterable<Transaction> tIter = transactionService.getTransactions();
		for (Transaction t : tIter) {
			transactions.add(t);
		}
		
		PortfolioData result = new PortfolioData(basketIter, securities, transactions);
		return result;
	}

	@Override
	public PortfolioData insertPortfolioData(PortfolioData portfolioData) {
		for (BasketEntity b : portfolioData.getBasketEntities()) {
			assetService.createBasket(b);
		}
		for (Security s : portfolioData.getSecurities()) {
			assetService.createSecurity(s);
		}
		for (Transaction t : portfolioData.getTransactions()) {
			transactionService.createTransaction(t);
		}
		
		return portfolioData;
	}

}
