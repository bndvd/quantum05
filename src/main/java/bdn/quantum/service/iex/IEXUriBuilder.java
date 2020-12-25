package bdn.quantum.service.iex;

import java.util.Map;
import java.util.Set;

public class IEXUriBuilder {

	private static final String URI_BASE_PROD = IEXConstants.IEX_CLOUD_V1;
	private static final String URI_BASE_TEST = IEXConstants.IEX_CLOUD_V1_SANDBOX;
	
	
	public IEXUriBuilder() {
	}
	
	public String build(int reqId, Map<String, String> varMap, String token) {
		if (reqId < 0 || reqId > IEXConstants.MAX_REQ_ID) {
			System.err.println("IEXUrlBuilder: ERROR - Invalid request ID. Aborting bulding URL request.");
			return null;
		}
		if (token == null) {
			System.err.println("IEXUrlBuilder: ERROR - IEX Token undefined. Aborting bulding URL request.");
			return null;
		}
		
		StringBuffer result = new StringBuffer();
		
		String uriBase = URI_BASE_PROD;
		result.append(uriBase);
		
		String endpoint = IEXConstants.REQ_ENDPOINT[reqId];
		
		// replace all the variables within the endpoint string (variables are {XXXX} strings)
		Set<String> vars = varMap.keySet();
		for (String key : vars) {
			String value = varMap.get(key);
			endpoint = endpoint.replaceAll(key, value);
		}
		result.append(endpoint);
		
		String params = getParams(reqId, token);
		if (params != null) {
			result.append("?");
			result.append(params);
		}
		
		return result.toString();
	}
	
	private String getParams(int reqId, String token) {
		String result = null;
		
		StringBuffer buf = new StringBuffer();
		switch (reqId) {
		
		case IEXConstants.REQ_PRICE:
			break;
			
		case IEXConstants.REQ_CHART:
			buf.append(IEXConstants.PARAM_CHARTBYDAY);
			buf.append("=");
			buf.append(IEXConstants.PARAM_VALUE_TRUE);
			buf.append("&");
			break;
			
		case IEXConstants.REQ_MAXCHART:
			buf.append(IEXConstants.PARAM_CHARTCLOSEONLY);
			buf.append("=");
			buf.append(IEXConstants.PARAM_VALUE_TRUE);
			buf.append("&");
			break;
			
		case IEXConstants.REQ_TRADEDAYS:
			break;
		}
		
		buf.append(IEXConstants.PARAM_TOKEN);
		buf.append("=");
		buf.append(token);
		
		if (buf.length() > 0) {
			result = buf.toString();
		}
		
		return result;
	}

}
