package bdn.quantum.service;

import java.math.BigDecimal;

public interface FundResolverService {

	public String getStockProxy(String fundSymbol);
	public BigDecimal convertProxyToFundValue(String fundSymbol, BigDecimal proxyValue);
	
}
