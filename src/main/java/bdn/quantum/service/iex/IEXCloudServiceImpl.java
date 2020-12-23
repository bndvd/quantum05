package bdn.quantum.service.iex;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import bdn.quantum.QuantumConstants;
import bdn.quantum.QuantumProperties;
import bdn.quantum.model.iex.IEXChart;
import bdn.quantum.model.iex.IEXChartFull;
import bdn.quantum.model.iex.IEXTradeDay;
import bdn.quantum.service.KeyvalService;

@Service("iexCloudService")
public class IEXCloudServiceImpl implements IEXCloudService {

	private RestTemplate restTemplate = new RestTemplate();
	private IEXUriBuilder uriBuilder = new IEXUriBuilder();

	private Map<String, PriceMemento> priceCache = new HashMap<>();
	private Map<String, Map<String, ChartMemento>> chartCache = new HashMap<>();

	@Autowired
	private KeyvalService keyvalService;
	@Autowired
	private IEXChartComparator iexChartComparator;
	@Autowired
	private IEXTradeDayComparator iexTradeDayComparator;

	private String iexToken = null;
	


	@Override
	public BigDecimal getPrice(String symbol) {
		BigDecimal result = null;

		synchronized (priceCache) {
			result = getPriceFromCache(symbol);

			if (result == null) {
				HashMap<String, String> varMap = new HashMap<String, String>();
				varMap.put(IEXConstants.VAR_SYMBOL, symbol);
				
				String uri = uriBuilder.build(IEXConstants.REQ_PRICE, varMap, getIEXToken());
				
				if (uri != null) {
					String strResult = restTemplate.getForObject(uri, String.class);
					result = new BigDecimal(strResult);
				}
				
				if (! result.equals(BigDecimal.ZERO)) {
					storePriceToCache(symbol, result);
				}
			}
		}
		
		return result;
	}

	@Override
	public IEXChartFull getChart(String symbol, String date) {
		IEXChartFull result = null;
		
		synchronized (chartCache) {
			result = getChartFromCache(symbol, date);

			if (result == null) {
				HashMap<String, String> varMap = new HashMap<String, String>();
				varMap.put(IEXConstants.VAR_SYMBOL, symbol);
				String inputDate = date.replaceAll("-", "");
				varMap.put(IEXConstants.VAR_DATE, inputDate);
				
				String uri = uriBuilder.build(IEXConstants.REQ_CHART, varMap, getIEXToken());
				
				if (uri != null) {
					IEXChartFull[] response = restTemplate.getForObject(uri, IEXChartFull[].class);
					if (response != null && response.length == 1) {
						result = response[0];
						storeChartToCache(symbol, date, result);
					}
					else {
						System.out.println("IEXCloudServiceImpl.getChart - Could not get a single chart response: " + symbol + " " + date);
					}
				}
			}
		}
		
		return result;
	}
	
	@Override
	public Iterable<IEXChart> getMaxChart(String symbol) {
		Iterable<IEXChart> result = null;
		
		HashMap<String, String> varMap = new HashMap<String, String>();
		varMap.put(IEXConstants.VAR_SYMBOL, symbol);
		
		String uri = uriBuilder.build(IEXConstants.REQ_MAXCHART, varMap, getIEXToken());
		
		if (uri != null) {
			IEXChart[] chartArr = restTemplate.getForObject(uri, IEXChart[].class);
			if (chartArr == null) {
				System.err.println("IEXCloudServiceImpl.getMaxChart - Error in getting a non-null response.");
			}
			else {
				List<IEXChart> list = new ArrayList<>();
				for (int i = 0; i < chartArr.length; i++) {
					list.add(chartArr[i]);
				}
				list.sort(iexChartComparator);
				result = list;
			}
		}
		return result;
	}

	@Override
	public Iterable<IEXTradeDay> getTradeDays(int numTradeDays) {
		Iterable<IEXTradeDay> result = null;
		
		HashMap<String, String> varMap = new HashMap<String, String>();
		varMap.put(IEXConstants.VAR_DAYS, String.valueOf(numTradeDays));
		
		String uri = uriBuilder.build(IEXConstants.REQ_TRADEDAYS, varMap, getIEXToken());
		
		if (uri != null) {
			IEXTradeDay[] tradeDayArr = restTemplate.getForObject(uri, IEXTradeDay[].class);
			if (tradeDayArr == null) {
				System.err.println("IEXCloudServiceImpl.getTradeDays - Error in getting a non-null response.");
			}
			else {
				List<IEXTradeDay> list = new ArrayList<>();
				for (int i = 0; i < tradeDayArr.length; i++) {
					list.add(tradeDayArr[i]);
				}
				list.sort(iexTradeDayComparator);
				result = list;
			}
		}
		return result;
	}

	@Override
	public void reset() {
		iexToken = null;
	}

	
	private String getIEXToken() {
		if (iexToken == null) {
			StringBuffer key = new StringBuffer();
			key.append(QuantumProperties.PROP_PREFIX).append(QuantumProperties.IEX_TOKEN);
			iexToken = keyvalService.getKeyvalStr(key.toString());
		}
		return iexToken;
	}

	
	private void storePriceToCache(String symbol, BigDecimal price) {
		priceCache.put(symbol, new PriceMemento(price));
	}
	
	private BigDecimal getPriceFromCache(String symbol) {
		BigDecimal result = null;
		PriceMemento memento = priceCache.get(symbol);
		if (memento != null) {
			if (memento.getAgeInMillis() < QuantumConstants.IEX_DATA_CACHE_LIFE_MILLIS) {
				result = memento.getPrice();
			}
			else {
				priceCache.remove(symbol);
			}
		}
		return result;
	}

	
	private void storeChartToCache(String symbol, String date, IEXChartFull chartFull) {
		Map<String, ChartMemento> dateToChartMementoMap = chartCache.get(symbol);
		if (dateToChartMementoMap == null) {
			dateToChartMementoMap = new HashMap<>();
			chartCache.put(symbol, dateToChartMementoMap);
		}
		dateToChartMementoMap.put(date, new ChartMemento(chartFull));
	}
	
	private IEXChartFull getChartFromCache(String symbol, String date) {
		IEXChartFull result = null;
		Map<String, ChartMemento> dateToChartMementoMap = chartCache.get(symbol);
		if (dateToChartMementoMap != null) {
			ChartMemento memento = dateToChartMementoMap.get(date);
			if (memento != null) {
				if (memento.getAgeInMillis() < QuantumConstants.IEX_DATA_CACHE_LIFE_MILLIS) {
					result = memento.getChart();
				}
				else {
					dateToChartMementoMap.remove(date);
				}
			}
		}
		return result;
	}

}


class PriceMemento {
	private Date timestamp = new Date();
	private BigDecimal price;
	
	public PriceMemento(BigDecimal price) {
		this.price = price;
	}
	
	public BigDecimal getPrice() {
		return price;
	}
	
	public long getAgeInMillis() {
		Date currTimestamp = new Date();
		long result = currTimestamp.getTime() - timestamp.getTime();
		return result;
	}
}

class ChartMemento {
	private Date timestamp = new Date();
	private IEXChartFull chartFull;
	
	public ChartMemento(IEXChartFull chartFull) {
		this.chartFull = chartFull;
	}
	
	public IEXChartFull getChart() {
		return chartFull;
	}
	
	public long getAgeInMillis() {
		Date currTimestamp = new Date();
		long result = currTimestamp.getTime() - timestamp.getTime();
		return result;
	}
}

