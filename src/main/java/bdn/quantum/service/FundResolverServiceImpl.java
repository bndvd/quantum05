package bdn.quantum.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bdn.quantum.QuantumProperties;

@Service("fundResolverService")
public class FundResolverServiceImpl implements FundResolverService {

	@Autowired
	private KeyvalService keyvalService;
	
	private Map<String, String> hcFundToProxyMap = new HashMap<>();
	private Map<String, BigDecimal> hcFundToFactorMap = new HashMap<>();
	
	private static Boolean readFundProxyFromDb = null;
	
	
	public FundResolverServiceImpl() {
		initProxies();
	}
	
	
	@Override
	public String getStockProxy(String fundSymbol) {
		if (fundSymbol == null || fundSymbol.trim().equals("")) {
			return null;
		}
		
		String result = null;
		
		if (shouldReadFundProxyFromDb()) {
			// check database
			String property = QuantumProperties.PROP_PREFIX + QuantumProperties.FUND_PROXY + fundSymbol;
			result = keyvalService.getKeyvalStr(property);
			if (result != null && ! result.trim().equals("")) {
				property = QuantumProperties.PROP_PREFIX + QuantumProperties.FUND_FACTOR + fundSymbol;
				String factor = keyvalService.getKeyvalStr(property);
				if (factor == null || factor.trim().equals("")) {
					result = null;
				}
			}
		}
		
		// check hardcoded values
		if (result == null) {
			result = hcFundToProxyMap.get(fundSymbol);
		}
		
		return result;
	}

	@Override
	public BigDecimal convertProxyToFundValue(String fundSymbol, BigDecimal proxyValue) {
		BigDecimal factor = null;
		
		if (fundSymbol != null && ! fundSymbol.trim().equals("") && proxyValue != null) {
			
			if (shouldReadFundProxyFromDb()) {
				// check database first
				String property = QuantumProperties.PROP_PREFIX + QuantumProperties.FUND_FACTOR + fundSymbol;
				String factorStr = keyvalService.getKeyvalStr(property);
				if (factorStr != null && ! factorStr.trim().equals("")) {
					try {
						factor = new BigDecimal(factorStr);
					}
					catch (Exception exc) {
						exc.printStackTrace();
						factor = null;
					}
				}
			}
			
			// check hardcoded values
			if (factor == null) {
				factor = hcFundToFactorMap.get(fundSymbol);
			}
		}
		
		BigDecimal result = null;
		if (factor != null) {
			result = proxyValue.multiply(factor);
		}
		
		return result;
	}
	
	private boolean shouldReadFundProxyFromDb() {
		if (readFundProxyFromDb == null) {
			readFundProxyFromDb = Boolean.FALSE;
			
			String property = QuantumProperties.PROP_PREFIX + QuantumProperties.READ_FUND_PROXIES;
			String propertyValue = keyvalService.getKeyvalStr(property);
			if (propertyValue != null && propertyValue.equalsIgnoreCase("true")) {
				readFundProxyFromDb = Boolean.TRUE;
			}
		}
		return readFundProxyFromDb.booleanValue();
	}

	
	private void initProxies() {
		// Hardcoded Proxies
		// VTI - VTSAX
		hcFundToProxyMap.put("VTSAX", "VTI");
		hcFundToFactorMap.put("VTSAX", new BigDecimal(0.486697770));
		// VTI - VTSMX
		hcFundToProxyMap.put("VTSMX", "VTI");
		hcFundToFactorMap.put("VTSMX", new BigDecimal(0.486492719));
		// VBR - VSIAX
		hcFundToProxyMap.put("VSIAX", "VBR");
		hcFundToFactorMap.put("VSIAX", new BigDecimal(0.429618283));
		// VBR - VISVX
		hcFundToProxyMap.put("VISVX", "VBR");
		hcFundToFactorMap.put("VISVX", new BigDecimal(0.239677464));
	}
}
