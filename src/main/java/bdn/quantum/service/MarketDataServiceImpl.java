package bdn.quantum.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

import bdn.quantum.QuantumConstants;
import bdn.quantum.model.MarketQuote;
import bdn.quantum.model.MarketQuoteEntity;
import bdn.quantum.model.MarketQuoteJson;
import bdn.quantum.model.MarketStatusEntity;
import bdn.quantum.model.MarketStatusJson;
import bdn.quantum.model.Security;
import bdn.quantum.model.Transaction;
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

	// the following repositories help get the stock split events, used for
	// unadjusting close prices
	@Autowired
	private AssetService assetService;
	@Autowired
	private TransactionService transactionService;
	
	
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
	
	// Get the chain of unadjusted (for splits) daily closing prices
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
		
		// if no history in database or the start date predates currently available history in database, populate it
		if (mqeListInRepository == null || mqeListInRepository.isEmpty() || 
				(startDate != null && startDate.compareTo(mqeListInRepository.get(0).getMktDate()) < 0) ) {
			
			String endDate = null;
			if (startDate != null && mqeListInRepository != null && ! mqeListInRepository.isEmpty()) {
				endDate = mqeListInRepository.get(0).getMktDate();
			}
			
			loadQuoteChainRangeIntoRepository(symbol, startDate, endDate);
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
		
		// build data into array and ensure there are no duplicates (again, should not happen, but a precaution)
		String lastDateAdded = null;
		result = new ArrayList<>();
		for (MarketQuoteEntity mqe : mqeListInRepository) {
			String nextDate = mqe.getMktDate();
			if (lastDateAdded != null && lastDateAdded.equals(nextDate)) {
				System.err.println("MarketDataServiceImpl.loadQuoteChain - Found duplicate date entry & skipping add - "+mqe.getSymbol()+":"+nextDate);
				continue;
			}
			else if (startDate != null && (nextDate.compareTo(startDate) < 0)) {
				// skip quotes before start date (such as those obtained in max chart or json loads)
				continue;
			}
			result.add(new MarketQuote(mqe));
			lastDateAdded = nextDate;
		}
		// if today's data was missing, and we have transient data, add it
		if (todaysQuote != null) {
			result.add(todaysQuote);
		}
		
		// adjust the close values for stock splits (which may or may not be reflected in historic values in the repository)
		adjustMarketQuoteChainForSplits(symbol, result);
		
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
					List<MarketStatusJson> msjList = objMapper.readValue(is, new TypeReference<List<MarketStatusJson>>(){});
					
					List<MarketStatusEntity> mseList = new ArrayList<>();
					for (MarketStatusJson ms : msjList) {
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
				System.err.println("MarketDataServiceImpl.loadTradeDayCache - ERROR: could not load initial trade day data. Exiting...");
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
			if (tdIter != null) {
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
			else {
				System.err.println("MarketDataServiceImpl.loadTradeDayCache - ERROR: could not load trade day data from IEX Service. Exiting...");
			}
		}
	}

	private void loadQuoteChainRangeIntoRepository(String symbol, String startDate, String endDate) {
		if (symbol == null) {
			System.err.println("MarketDataServiceImpl.loadQuoteChainRangeIntoRepository - Failed to load needed data as symbol is NULL.");
			return;
		}
		
		List<MarketQuoteEntity> mqeList = new ArrayList<>();
		
		// first try to get the maximum number of stock quotes from IEX service
		Iterable<IEXChart> iexChartIter = iexCloudService.getMaxChart(symbol);
		if (iexChartIter != null) {
			for (IEXChart c : iexChartIter) {
				String cDate = c.getDate();
				if (endDate != null && cDate.compareTo(endDate) >= 0) {
					// dates of endDate and onward are already in our database; do not add them
					break;
				}
				MarketQuoteEntity mqe = new MarketQuoteEntity(null, symbol, cDate, null, null, null, null, null, c.getClose(), null, null, null, null);
				mqeList.add(mqe);
			}
			
		}
		
		// add data from pre-packaged data in the quantum application, if
		// (1) IEX service did not return data, or
		// (2) start date is not included in the data returned by IEX service
		if (mqeList.isEmpty() || (startDate != null && startDate.compareTo(mqeList.get(0).getMktDate()) < 0)) {
			try {
				System.out.println("MarketDataServiceImpl.loadQuoteChainRangeIntoRepository - Failed to load data from IEX service. Loading from json file for " + symbol);
				Resource resource = new ClassPathResource("data/mkt-hist-sec-" + symbol.toUpperCase() + ".json");
				InputStream is = resource.getInputStream();
				
				ObjectMapper objMapper = new ObjectMapper();
				List<MarketQuoteJson> mqjList = objMapper.readValue(is, new TypeReference<List<MarketQuoteJson>>(){});
				
				if (mqjList != null && ! mqjList.isEmpty()) {
					if (startDate != null && mqjList.get(0).getMktDate().compareTo(startDate) > 0) {
						System.err.println("MarketDataServiceImpl.loadQuoteChainRangeIntoRepository - Json data for " + symbol +
								" did not include sufficient data for start-date: " + startDate);
					}
					
					String earliestDateInMqeList = null;
					if (! mqeList.isEmpty()) {
						earliestDateInMqeList = mqeList.get(0).getMktDate();
					}
					
					int insertIdx = 0;
					for (MarketQuoteJson mqj : mqjList) {
						String date = mqj.getMktDate();
						if ((earliestDateInMqeList == null || date.compareTo(earliestDateInMqeList) < 0) &&
								(endDate == null || date.compareTo(endDate) < 0)) {
							BigDecimal close = new BigDecimal(mqj.getClose());
							MarketQuoteEntity mqe = new MarketQuoteEntity(null, symbol, date, null, null, null, null, null,
									close, null, null, null, null);
							mqeList.add(insertIdx, mqe);
							insertIdx++;
						}
					}
				}
				else {
					System.err.println("MarketDataServiceImpl.loadQuoteChainRangeIntoRepository - Failed to load data from json file for " + symbol);
				}
			}
			catch (Exception exc) {
				System.err.println(exc.getMessage());
			}
		}
		
		if (! mqeList.isEmpty()) {
			String earliestQuoteDate = mqeList.get(0).getMktDate();

			// if our attempts to get data from IEX service as well as the json file failed include the start date,
			// add zeros for close values to all trade days starting with the start date until the first date we have
			// the data. this prevents trying to keep loading this data every time, which is expensive (IEX service has limits)
			if (startDate != null) {
				List<String> dateList = ModelUtils.getDateStringsFromStartDateStr(startDate, earliestQuoteDate, false);
				
				if (dateList != null && ! dateList.isEmpty()) {
					int insertIdx = 0;
					for (String nextDate : dateList) {
						Boolean mktStatus = tradeDayMapCache.get(nextDate);
						if (mktStatus != null && mktStatus.booleanValue() == true) {
							MarketQuoteEntity mqe = new MarketQuoteEntity(null, symbol, nextDate, null, null, null, null, null,
									BigDecimal.ZERO, null, null, null, null);
							mqeList.add(insertIdx, mqe);
							insertIdx++;
						}
					}
					
					System.err.println("MarketDataServiceImpl.loadQuoteChainRangeIntoRepository - Inserted zero quote values for " + symbol +
							" between " + startDate + " and " + earliestQuoteDate + " since IEX svc and Json data failed provide this data.");
				}
			}
			
			marketQuoteRepository.saveAll(mqeList);
		}
		else {
			System.err.println("MarketDataServiceImpl.loadQuoteChainRangeIntoRepository - Failed to load needed data for " + symbol +
					", start-date: " + startDate + ", end-date: " + endDate);
		}
	}
	
	// Use an analytical algorithm to guess which quotes have been adjusted based on stock split events
	// and the current quotes' proximity to the split vs unsplit price.
	// Unadjust & adjust all values deemed adjusted, and update them in the MarketQuote objects
	private void adjustMarketQuoteChainForSplits(String symbol, List<MarketQuote> mqList) {
		if (symbol == null || mqList == null || mqList.isEmpty()) {
			return;
		}
		
		List<Transaction> splitEvents = getStockSplitTransactions(symbol);
		if (splitEvents == null || splitEvents.isEmpty()) {
			return;
		}
		
		// we have split events for this security, we'll calculate adjusted and unadjusted from raw quote values;
		// raw quote values can either be adjusted or unadjusted (depending on the timing of acquiring the data versus
		// the timing of the split)
		// the algorithm below tries to guess which on the raw quote represents based on its being closer to the proximal
		// expected value based on the split event(s)
		// Note: multiple split events can be present, which compounds the effect

		List<BigDecimal> cumSplitFactors = new ArrayList<>();
		// we start from the end of the list, where we assume the adjusted value and unadjusted value are equal
		// and therefore have a split factor of 1. We also assume the split event happens at the beginning of the date
		// of the split event (thus close price for the day happens after the split occurred).
		cumSplitFactors.add(BigDecimal.ONE);
		
		int mqListLastIdx = mqList.size() - 1;
		BigDecimal oneFactorProximalPrice = mqList.get(mqListLastIdx).getRawQuote();
		int splitEvtIdx = splitEvents.size() - 1;
		String splitEvtDateStr = ModelUtils.dateToString(splitEvents.get(splitEvtIdx).getTranDate());
		
		for (int mqIdx = mqListLastIdx; mqIdx >= 0; mqIdx--) {
			
			// if the market quote date passed over a split event, add split factor
			if ((splitEvtIdx >= 0) && (ModelUtils.compareDateStrings(mqList.get(mqIdx).getMktDate(), splitEvtDateStr) < 0)) {
				BigDecimal factor = splitEvents.get(splitEvtIdx).getShares();
				BigDecimal cumFactor = cumSplitFactors.get(cumSplitFactors.size()-1).multiply(factor);
				cumSplitFactors.add(cumFactor);
				splitEvtIdx--;
				// optimize re-reading the split date by updating it only when it changes
				if (splitEvtIdx >= 0) {
					splitEvtDateStr = ModelUtils.dateToString(splitEvents.get(splitEvtIdx).getTranDate());
				}
			}
			// at this point (until we encounter another split event), the needed split factor to be applied to all
			// quotes on top of a totally split "adjusted" value to get unadjusted value is in the last element of cumSplitFactors
			// however, we may encounter either adjusted, unadjusted, or partially adjusted (in the case of multiple split events)
			// values in the raw quote. We test to see which cumulative they are closest to when compared to our one-factor proximal
			// value multiplied by each factor. Once we guess at the factor implied in the raw quote value, we apply the difference
			// between the needed factor (last element of cumSplitFactors) and the actual embedded to arrive at both unadjusted and
			// adjusted values
			
			MarketQuote mq = mqList.get(mqIdx);
			BigDecimal rawQuote =  mq.getRawQuote();
			// unadjusted quote
			BigDecimal uQuote =  rawQuote;
			// split adjusted quote
			BigDecimal saQuote = rawQuote;
			if (cumSplitFactors.size() > 1) {
				int impliedFactorInRawQuoteIdx = computeImpliedFactor(oneFactorProximalPrice, rawQuote, cumSplitFactors);
				
				// compute the unadjusted value if it's not already at the desired factor (last cumSplitFactor)
				int lastCumSplitFactorIdx = cumSplitFactors.size() - 1;
				if (impliedFactorInRawQuoteIdx < lastCumSplitFactorIdx) {
					BigDecimal factorDifference = cumSplitFactors.get(lastCumSplitFactorIdx);
					// avoid division by 1.0
					if (impliedFactorInRawQuoteIdx > 0) {
						factorDifference = factorDifference.divide(cumSplitFactors.get(impliedFactorInRawQuoteIdx),
							QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);
					}
					uQuote = rawQuote.multiply(factorDifference);
				}
				
				// compute the adjusted value if it's not already at a factor of 1.0
				if (impliedFactorInRawQuoteIdx > 0) {
					saQuote = rawQuote.divide(cumSplitFactors.get(impliedFactorInRawQuoteIdx),
							QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);
				}
			}
			
			mq.setQuote(uQuote, QuantumConstants.ADJ_TYPE_UNADJUSTED);
			mq.setQuote(saQuote, QuantumConstants.ADJ_TYPE_SPLIT_ADJUSTED);
			
			oneFactorProximalPrice = saQuote;
		}	
	}
	
	private int computeImpliedFactor(BigDecimal base, BigDecimal actual, List<BigDecimal> factors) {
		int result = 0;
		
		if (base != null && actual != null && factors != null && !factors.isEmpty()) {
			try {
				BigDecimal minOffMultiplier = base.
						multiply(factors.get(0)).
						divide(actual, QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP).
						subtract(BigDecimal.ONE).
						abs();
				for (int i = 1; i < factors.size(); i++) {
					BigDecimal offMultiplier = base.
							multiply(factors.get(i)).
							divide(actual, QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP).
							subtract(BigDecimal.ONE).
							abs();
					if (offMultiplier.compareTo(minOffMultiplier) < 0) {
						minOffMultiplier = offMultiplier;
						result = i;
					}
				}
			}
			catch (Exception exc) {
				System.err.println("MarketDataServiceImpl.computeImpliedFactor - Exception: " + exc.getMessage());
			}
		}
		
		return result;
	}

	private List<Transaction> getStockSplitTransactions(String symbol) {
		List<Transaction> result = new ArrayList<>();
		
		if (symbol != null) {
			Security se = assetService.getSecurityForSymbol(symbol);
			if (se != null) {
				Integer secId = se.getId();
				List<Transaction> tranList = transactionService.getTransactionsForSecurityAndType(secId, QuantumConstants.TRAN_TYPE_SPLIT);
				result.addAll(tranList);
			}
			else {
				System.err.println("MarketDataServiceImpl.getStockSplitTransactions - Error finding security for symbol " + symbol);
			}
		}
		
		return result;
	}
	
}
