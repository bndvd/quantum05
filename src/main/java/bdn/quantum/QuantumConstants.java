package bdn.quantum;

import java.math.BigDecimal;

import bdn.quantum.service.PortfolioSimulator;

public final class QuantumConstants {

	public static final String REST_URL_BASE = "api/v1/";
	
	public static final String TABLE_TRANSACTION = "transaction";
	public static final String TABLE_SECURITY = "security";
	public static final String TABLE_BASKET = "basket";
	public static final String TABLE_KEYVAL = "keyval";
	public static final String TABLE_MKTSTATUS = "mktstatus";
	public static final String TABLE_MKTQUOTE = "mktquote";
	
	public static final String TRANSACTION_ID = "id";
	public static final String TRANSACTION_SEC_ID = "secId";
	public static final String TRANSACTION_USER_ID = "userId";
	public static final String TRANSACTION_TRAN_DATE = "tranDate";
	public static final String TRANSACTION_TRAN_TYPE = "type";
	public static final String TRANSACTION_TRAN_SHARES = "shares";
	public static final String TRANSACTION_TRAN_PRICE = "price";
	
	public static final String SECURITY_ID = "id";
	public static final String SECURITY_BASKET_ID = "basketId";
	public static final String SECURITY_SYMBOL = "symbol";

	public static final String BASKET_ID = "id";
	public static final String BASKET_NAME = "name";

	public static final String KEYVAL_KEY = "kvkey";
	public static final String KEYVAL_VALUE = "kvvalue";
	
	public static final String MKTSTATUS_MKT_DATE = "mktDate";
	public static final String MKTSTATUS_OPEN_STATUS = "openStatus";
	
	public static final String MKTQUOTE_SYMBOL = "symbol";
	public static final String MKTQUOTE_MKT_DATE = "mktDate";
	public static final String MKTQUOTE_UNADJUSTED_CLOSE = "uClose";
	public static final String MKTQUOTE_UNADJUSTED_OPEN = "uOpen";
	public static final String MKTQUOTE_UNADJUSTED_HIGH = "uHigh";
	public static final String MKTQUOTE_UNADJUSTED_LOW = "uLow";
	public static final String MKTQUOTE_UNADJUSTED_VOLUME = "uVolume";
	public static final String MKTQUOTE_CLOSE = "close";
	public static final String MKTQUOTE_OPEN = "open";
	public static final String MKTQUOTE_HIGH = "high";
	public static final String MKTQUOTE_LOW = "low";
	public static final String MKTQUOTE_VOLUME = "volume";

	// Transaction
	public static final String TRAN_TYPE_BUY = "BUY";
	public static final String TRAN_TYPE_SELL = "SEL";
	public static final String TRAN_TYPE_DIVIDEND = "DIV";
	public static final String TRAN_TYPE_SPLIT = "SPL";
	public static final String TRAN_TYPE_CONVERSION = "CNV";	// fund conversion
	
	// Miscellaneous calculation constants
	public static final int NUM_DECIMAL_PLACES_PRECISION = 20;
	public static final int MILLIS_BETWEEN_TRANSACTIONS_ON_SAME_DATE = 1000;
	public static final double THRESHOLD_DECIMAL_EQUALING_ZERO = 0.000000001;
	
	// Stock quote constants
	public static final long IEX_DATA_CACHE_LIFE_MILLIS = 600000;
	
	// User session life in milliseconds
	public static final long USER_SESSION_LIFE_MILLIS = 600000;
	
	// Plot constants
	public static final String PLOT_STD_GROWTH = "stdgrowth";
	public static final String PLOT_SIMULATED_TARGET = "simtarget";
	public static final long PLOT_CACHE_LIFE_MILLIS = 300000;
	public static final String PLOT_STD_BENCHMARK_SYMBOL = "VTI";
	
	public static final BigDecimal SIMULATED_TARGET_PRINCIPAL_INIT = new BigDecimal(10000);
	public static final BigDecimal SIMULATED_TARGET_PRINCIPAL_INCR = new BigDecimal(20);
	public static final Integer SIMULATED_TARGET_PRINCIPAL_INCR_FREQ = PortfolioSimulator.INCR_PRINCIPAL_FREQ_DAILY;
	
	
}
