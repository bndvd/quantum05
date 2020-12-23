package bdn.quantum.model;

import java.util.Date;

public class PortfolioData {

	private String version = "1.0";
	private Date lastDate = new Date(0);
	private Iterable<BasketEntity> basketEntities;
	private Iterable<Security> securities;
	private Iterable<Transaction> transactions;
	
	public PortfolioData() {
	}
	
	public PortfolioData(Iterable<BasketEntity> basketEntities, Iterable<Security> securities, Iterable<Transaction> transactions) {
		setBasketEntities(basketEntities);
		setSecurities(securities);
		setTransactions(transactions);
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Date getLastDate() {
		return lastDate;
	}

	public void setLastDate(Date lastDate) {
		this.lastDate = lastDate;
	}

	public Iterable<BasketEntity> getBasketEntities() {
		return basketEntities;
	}

	public void setBasketEntities(Iterable<BasketEntity> basketEntities) {
		this.basketEntities = basketEntities;
	}

	public Iterable<Security> getSecurities() {
		return securities;
	}

	public void setSecurities(Iterable<Security> securities) {
		this.securities = securities;
	}

	public Iterable<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(Iterable<Transaction> transactions) {
		this.transactions = transactions;
		computeLastDate();
	}
	
	private void computeLastDate() {
		if (transactions != null) {
			for(Transaction t : transactions) {
				Date td = t.getTranDate();
				if (lastDate.before(td)) {
					lastDate = td;
				}
			}
		}
	}
}
