package bdn.quantum.model;

import java.math.BigDecimal;

import bdn.quantum.QuantumConstants;

public class MarketQuote {

	private String symbol;
	private String mktDate;
	// for historical data, this may be adjusted or unadjusted close price for stock splits
	private BigDecimal rawQuote;
	// this is unadjusted (determined from raw quote, based on unadjusting raw quote if necessary)
	private BigDecimal uQuote = null;
	// this is adjusted for stock splits (determined from raw quote, based on adjusting raw quote if necessary)
	private BigDecimal saQuote = null;

	
	public MarketQuote(String symbol, String mktDate, BigDecimal rawQuote) {
		this.symbol = symbol;
		this.mktDate = mktDate;
		this.rawQuote = rawQuote;
	}


	public MarketQuote(MarketQuoteEntity mqe) {
		this.symbol = mqe.getSymbol();
		this.mktDate = mqe.getMktDate();
		this.rawQuote = mqe.getClose();
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


	public BigDecimal getRawQuote() {
		return rawQuote;
	}

	
	public void setRawQuote(BigDecimal rawQuote) {
		this.rawQuote = rawQuote;
	}

	
	public BigDecimal getQuote(String adjustmentType) {
		BigDecimal result = rawQuote;
		
		if (adjustmentType != null) {
			if (adjustmentType.equals(QuantumConstants.ADJ_TYPE_UNADJUSTED)) {
				if (uQuote != null) {
					result = uQuote;
				}
			}
			else if (adjustmentType.equals(QuantumConstants.ADJ_TYPE_SPLIT_ADJUSTED)) {
				if (saQuote != null) {
					result = saQuote;
				}
			}
		}
		
		return result;
	}
	

	public void setQuote(BigDecimal quote, String adjustmentType) {
		if (adjustmentType != null) {
			if (adjustmentType.equals(QuantumConstants.ADJ_TYPE_UNADJUSTED)) {
				this.uQuote = quote;
			}
			else if (adjustmentType.equals(QuantumConstants.ADJ_TYPE_SPLIT_ADJUSTED)) {
				this.saQuote = quote;
			}
		}
	}

	
}
