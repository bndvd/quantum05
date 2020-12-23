package bdn.quantum.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import bdn.quantum.model.MarketQuote;
import bdn.quantum.model.MarketQuoteEntity;
import bdn.quantum.model.MarketStatus;
import bdn.quantum.model.MarketStatusEntity;
import bdn.quantum.model.iex.IEXChart;
import bdn.quantum.model.iex.IEXChartFull;
import bdn.quantum.model.iex.IEXTradeDay;
import bdn.quantum.model.qplot.QChart;
import bdn.quantum.model.util.ModelUtils;
import bdn.quantum.repository.MarketQuoteRepository;
import bdn.quantum.repository.MarketStatusRepository;
import bdn.quantum.service.iex.IEXCloudService;

@Service("marketDataService")
public class MarketDataServiceImpl implements MarketDataService {
	
	private Map<String, Boolean> tradeDayMapCache = null; 
	
	@Autowired
	private FundResolverService fundResolverService;
	@Autowired
	private IEXCloudService iexCloudService;
	@Autowired
	private MarketQuoteRepository marketQuoteRepository;
	@Autowired
	private MarketStatusRepository marketStatusRepository;
	
	
	@Override
	public void configChanged() {
		iexCloudService.reset();
	}

	@Override
	public BigDecimal getLastPrice(String symbol) {
		BigDecimal result = null;
		
		String querySymbol = symbol;
		String proxySymbol = fundResolverService.getStockProxy(symbol);
		if (proxySymbol != null) {
			querySymbol = proxySymbol;
		}

		try {
			result = iexCloudService.getPrice(querySymbol);
		}
		catch (Exception exc) {
			System.err.println("MarketDataServiceImpl.getLastPrice:: " + exc);
			result = BigDecimal.ZERO;
		}

		if (proxySymbol != null) {
			result = fundResolverService.convertProxyToFundValue(symbol, result);
			if (result == null) {
				result = BigDecimal.ZERO;
			}
		}
						
		return result;
	}
	
	@Override
	public List<QChart> getChartChain(String symbol, LocalDate startDate) {
		List<QChart> qChartList = null;
		
		String querySymbol = symbol;
		String proxySymbol = fundResolverService.getStockProxy(symbol);
		if (proxySymbol != null) {
			querySymbol = proxySymbol;
		}
		
		try {
			String startDateStr = null;
			if (startDate != null) {
				startDateStr = ModelUtils.localDateToString(startDate);
			}
			List<MarketQuote> quoteList = loadQuoteChain(querySymbol, startDateStr);
			
			if (quoteList != null) {		
				qChartList = new ArrayList<>();
				for (MarketQuote c : quoteList) {
					QChart qc = new QChart(symbol, c, fundResolverService);
					qChartList.add(qc);
				}
			}
		}
		catch (Exception exc) {
			System.err.println("MarketDataServiceImpl.getDateChain:: "+exc);
			exc.printStackTrace();
			qChartList = null;
		}
		
		return qChartList;
	}
	
	// Read from local database the stored history. If non-existent, populate it
	// If missing recent data, populate
	private synchronized List<MarketQuote> loadQuoteChain(String symbol, String startDate) {
		List<MarketQuote> result = null;
		String dayBeforeStartDate = null;
		if (startDate != null) {
			try {
				dayBeforeStartDate = ModelUtils.localDateToString(ModelUtils.stringToLocalDate(startDate).minusDays(1));
			}
			catch (Exception exc) {
				exc.printStackTrace();
				dayBeforeStartDate = null;
			}
		}
		
		loadTradeDayCache();
		
		// read quote history from database
		List<MarketQuoteEntity> mqeListInRepository = null;
		if (dayBeforeStartDate == null) {
			mqeListInRepository = marketQuoteRepository.findBySymbolOrderByMktDateAsc(symbol);
		}
		else {
			mqeListInRepository = marketQuoteRepository.findBySymbolAndMktDateIsGreaterThanOrderByMktDateAsc(symbol, dayBeforeStartDate);
		}
		
		// if no history in database, populate it
		if (mqeListInRepository == null || ! mqeListInRepository.iterator().hasNext()) {
			loadMaxQuoteChainIntoRepository(symbol);
			// re-query
			mqeListInRepository = marketQuoteRepository.findBySymbolOrderByMktDateAsc(symbol);
		}
		
		// Under certain conditions (it appears this happens before 7 pm CST) getChart does not return today's data and today
		// is not considered a trade day; when this happens we'll use getPrice to get the latest price for today
		// however, will not store it in the repository.
		MarketQuote todaysQuote = null;
		String todaysDateStr = ModelUtils.localDateToString(LocalDate.now());
		
		if (tradeDayMapCache != null && mqeListInRepository != null) {
			// special handling of today's date (it may not be considered a trading day yet, or the data may not be available,
			// or the market has not closed and we have an intermediate (last) price, or it has already been added)
			boolean todaysDataAdded = false;

			String firstMqeDate = null;
			Set<String> tradeDateSet = new HashSet<String>(tradeDayMapCache.keySet());
			for (MarketQuoteEntity mqe : mqeListInRepository) {
				String dateInRepository = mqe.getMktDate();
				tradeDateSet.remove(dateInRepository);
				// if first quote in history, remember it
				if (firstMqeDate == null) {
					firstMqeDate = dateInRepository;
				}
				if (dateInRepository.equals(todaysDateStr)) {
					todaysDataAdded = true;
				}
			}
			
			// go through dates not already in repository
			boolean newDataAdded = false;
			for (String nextDate : tradeDateSet) {
				// ignore all trade days before the first quote date of this security; we're only looking for recent missing data
				if (nextDate.compareTo(firstMqeDate) < 0) {
					continue;
				}
				// if start date is specified, ignore all dates prior to the start date (maxQuoteChainIntoRepository load should have loaded
				// the full past history; any errors / missing data there should not be retried every time
				if (startDate != null && nextDate.compareTo(startDate) < 0) {
					continue;
				}
				
				Boolean isTradingDay = tradeDayMapCache.get(nextDate);
				if (isTradingDay) {
					IEXChartFull cf = iexCloudService.getChart(symbol, nextDate);
					// cf can be null if the data is not (yet) available for this date (e.g., today)
					if (cf != null) {
						MarketQuoteEntity mqe = new MarketQuoteEntity(null, symbol, cf.getDate(), cf.getuClose(), cf.getuOpen(),
								cf.getuHigh(), cf.getuLow(), cf.getuVolume(), cf.getClose(), cf.getOpen(), cf.getHigh(),
								cf.getLow(), cf.getVolume());
						// before saving make sure we don't already have it saved (should not happen, but a precaution)
						// if already saved, update
						if (! marketQuoteRepository.existsBySymbolAndMktDate(symbol, cf.getDate())) {
							marketQuoteRepository.save(mqe);
						}
						else {
							System.err.println("MarketDataServiceImpl.loadQuoteChain - Refused to save quote already in db - "+symbol+":"+cf.getDate());
						}
						newDataAdded = true;
						
						if (nextDate.equals(todaysDateStr)) {
							todaysDataAdded = true;
						}
					}
				}
			}
			if (newDataAdded) {
				// re-query
				if (dayBeforeStartDate == null) {
					mqeListInRepository = marketQuoteRepository.findBySymbolOrderByMktDateAsc(symbol);
				}
				else {
					mqeListInRepository = marketQuoteRepository.findBySymbolAndMktDateIsGreaterThanOrderByMktDateAsc(symbol, dayBeforeStartDate);
				}
			}
			
			// if we were unable to get today's quote, or if today is not considered a trade day (yet),
			// use getPrice to get a quote we can use transiently (but will not store it in our repository)
			if (! todaysDataAdded) {
				BigDecimal todaysLastPrice = getLastPrice(symbol);
				todaysQuote = new MarketQuote(symbol, todaysDateStr, todaysLastPrice);
			}
		}
		
		// adjust the close values for a recent stock split, which would not be reflected in historic values 
		// in the repository
		adjustMarketQuoteChainForSplits(mqeListInRepository);
		
		// build data into array and ensure there are no duplicates (again, should not happen, but a precaution)
		String lastDateAdded = null;
		result = new ArrayList<>();
		for (MarketQuoteEntity mqe : mqeListInRepository) {
			String nextDate = mqe.getMktDate();
			if (lastDateAdded != null && lastDateAdded.equals(nextDate)) {
				System.err.println("MarketDataServiceImpl.loadQuoteChain - Found duplicate date entry & skipping add - "+mqe.getSymbol()+":"+nextDate);
				continue;
			}
			result.add(new MarketQuote(mqe));
			lastDateAdded = nextDate;
		}
		// if today's data was missing, and we have transient data, add it
		if (todaysQuote != null) {
			result.add(todaysQuote);
		}
		
		return result;
	}
	
	private synchronized void loadTradeDayCache() {
		// load initial history
		if (tradeDayMapCache == null) {
			// try reading trade day history from repository
			Iterable<MarketStatusEntity> mseListInRepository = marketStatusRepository.findAllByOrderByMktDateAsc();
			
			// if repository is empty, load from data file (initial load only)
			if (mseListInRepository == null || ! mseListInRepository.iterator().hasNext()) {
				try {
					Resource resource = new ClassPathResource("data/mkt-hist-trading-dates.json");
					InputStream is = resource.getInputStream();
					
					ObjectMapper objMapper = new ObjectMapper();
					List<MarketStatus> msList = objMapper.readValue(is, new TypeReference<List<MarketStatus>>(){});
					
					List<MarketStatusEntity> mseList = new ArrayList<>();
					for (MarketStatus ms : msList) {
						mseList.add(new MarketStatusEntity(ms));
					}
					marketStatusRepository.saveAll(mseList);
					
					mseListInRepository = marketStatusRepository.findAllByOrderByMktDateAsc();
				}
				catch (Exception exc) {
					System.err.println(exc.getMessage());
					return;
				}
			}
			// if still empty, error out
			if (mseListInRepository == null || ! mseListInRepository.iterator().hasNext()) {
				System.err.println("MarketDataServiceImpl.loadTradeDayCache - ERROR: could not laod initial trade day data. Exiting...");
				return;
			}
			
			// load initial trade day data into cache
			String todayStr = ModelUtils.localDateToString(LocalDate.now());
			tradeDayMapCache = new HashMap<>();
			for (MarketStatusEntity mse : mseListInRepository) {
				// skip adding today's date, since the previous for today may have been inaccurate
				// (e.g., it appears IEX does not acknowledge today as trade day until after 7 pm CST)
				if (mse.getMktDate().equals(todayStr)) {
					continue;
				}
				tradeDayMapCache.put(mse.getMktDate(), mse.getOpenStatus());
			}
		}
		
		// update with the latest trade day info
		String mostRecentDateInCache = ModelUtils.getMostRecentDateStr(tradeDayMapCache.keySet());
		List<String> newDatesList = ModelUtils.getDateStringsFromStartDateStr(mostRecentDateInCache);
		String todayStr = ModelUtils.localDateToString(LocalDate.now());
		
		// remove today's date, since it appears IEX does not acknowledge today as trade
		// day until after 7 pm CST
		int indexOfTodaysDate = newDatesList.indexOf(todayStr);
		if (indexOfTodaysDate >= 0) {
			newDatesList.remove(indexOfTodaysDate);
		}
		
		if (newDatesList.size() > 0) {
			Map<String, MarketStatusEntity> dateStrToMSE = new HashMap<>();
			for (String d : newDatesList) {
				MarketStatusEntity mse = new MarketStatusEntity(d, false);
				dateStrToMSE.put(d, mse);
			}
			
			// set open status to true for all days returned by IEX service
			// Note: we fetch the # of days equal to the size of the new dates list plus 1, since
			// we removed today from the list, but we need to count today in the # of days to go back
			Iterable<IEXTradeDay> tdIter = iexCloudService.getTradeDays(newDatesList.size()+1);
			for (IEXTradeDay td : tdIter) {
				String dateOpen = td.getDate();
				MarketStatusEntity mse = dateStrToMSE.get(dateOpen);
				if (mse != null) {
					mse.setOpenStatus(true);
				}
			}
			
			Iterable<MarketStatusEntity> mseIter = dateStrToMSE.values();
			marketStatusRepository.saveAll(mseIter);
			mseIter = dateStrToMSE.values();
			for (MarketStatusEntity mse : mseIter) {
				tradeDayMapCache.put(mse.getMktDate(), mse.getOpenStatus());
			}
		}
	}

	private void loadMaxQuoteChainIntoRepository(String symbol) {
		Iterable<IEXChart> iexChartIter = iexCloudService.getMaxChart(symbol);
		
		if (iexChartIter != null) {
			List<MarketQuoteEntity> mqeList = new ArrayList<>();
			for (IEXChart c : iexChartIter) {
				MarketQuoteEntity mqe = new MarketQuoteEntity(null, symbol, c.getDate(), null, null, null, null, null, c.getClose(), null, null, null, null);
				mqeList.add(mqe);
			}
			
			marketQuoteRepository.saveAll(mqeList);
		}
	}
	
	private void adjustMarketQuoteChainForSplits(List<MarketQuoteEntity> mqeList) {
		// To be implemented when there is a stock split
		// there is currently no good way to identify the split event 
		return;
	}
	
}
