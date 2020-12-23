package bdn.quantum.model.qplot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

import bdn.quantum.model.MarketQuote;
import bdn.quantum.model.util.ModelUtils;
import bdn.quantum.service.FundResolverService;

public class QChart {

	private String symbol;
	private MarketQuote quote;
	private FundResolverService fundResolverService;
	
	private String proxySymbol = null;
	
	
	public QChart(String symbol, MarketQuote quote, FundResolverService fundResolverService) {
		this.symbol = symbol;
		this.quote = quote;
		this.fundResolverService = fundResolverService;
		this.proxySymbol = fundResolverService.getStockProxy(symbol);
	}
	
	public LocalDate getLocalDate() {
		return ModelUtils.stringToLocalDate(quote.getMktDate());
	}
	
	public Date getDate() {
		return ModelUtils.stringToDate(quote.getMktDate());
	}
	
	public BigDecimal getClose() {
		BigDecimal result = quote.getQuote();
		if (proxySymbol != null) {
			result = fundResolverService.convertProxyToFundValue(symbol, result);
		}
		return result;
	}
	
}
