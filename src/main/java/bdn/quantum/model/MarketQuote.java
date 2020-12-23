package bdn.quantum.model;

import java.math.BigDecimal;

public class MarketQuote {

	private String symbol;
	private String mktDate;
	// for historical data, this is adjusted close price
	private BigDecimal quote;

	
	public MarketQuote(String symbol, String mktDate, BigDecimal quote) {
		this.symbol = symbol;
		this.mktDate = mktDate;
		this.quote = quote;
	}


	public MarketQuote(MarketQuoteEntity mqe) {
		this.symbol = mqe.getSymbol();
		this.mktDate = mqe.getMktDate();
		this.quote = mqe.getAdjustedClose();
	}


	public String getSymbol() {
		return symbol;
	}


	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}


	public String getMktDate() {
		return mktDate;
	}


	public void setMktDate(String mktDate) {
		this.mktDate = mktDate;
	}


	public BigDecimal getQuote() {
		return quote;
	}


	public void setQuote(BigDecimal quote) {
		this.quote = quote;
	}

	
}
