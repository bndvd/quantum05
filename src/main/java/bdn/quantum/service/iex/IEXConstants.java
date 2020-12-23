package bdn.quantum.service.iex;

public class IEXConstants {

	public static final String IEX_CLOUD_V1 = "https://cloud.iexapis.com/v1";
	public static final String IEX_CLOUD_V1_SANDBOX = "https://sandbox.iexapis.com/v1";
	
	public static final String VAR_SYMBOL = "\\{symbol\\}";
	public static final String VAR_DATE = "\\{date\\}";
	public static final String VAR_DAYS = "\\{days\\}";
	
	// requests
	public static final int REQ_PRICE = 0;
	public static final int REQ_CHART = 1;
	public static final int REQ_MAXCHART = 2;
	public static final int REQ_TRADEDAYS = 3;
	
	public static final String[] REQ_ENDPOINT = {
		"/stock/{symbol}/quote/latestPrice",
		"/stock/{symbol}/chart/date/{date}",
		"/stock/{symbol}/chart/max",
		"/ref-data/us/dates/trade/last/{days}"
	};
	public static final int MAX_REQ_ID = REQ_ENDPOINT.length - 1;
	
	// parameters
	public static final String PARAM_VALUE_TRUE = "true";
	public static final String PARAM_VALUE_FALSE = "false";
	public static final String PARAM_TOKEN = "token";
	public static final String PARAM_CHARTBYDAY = "chartByDay";
	public static final String PARAM_CHARTCLOSEONLY = "chartCloseOnly";
	
}
