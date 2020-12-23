package bdn.quantum.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bdn.quantum.QuantumConstants;
import bdn.quantum.QuantumProperties;
import bdn.quantum.model.AbstractTransaction;
import bdn.quantum.model.Asset;
import bdn.quantum.model.Position;
import bdn.quantum.model.Transaction;
import bdn.quantum.model.qplot.QChart;
import bdn.quantum.model.qplot.QPlot;
import bdn.quantum.model.qplot.QPlotPoint;
import bdn.quantum.model.qplot.QPlotSeries;
import bdn.quantum.model.util.AssetSymbolManager;
import bdn.quantum.model.util.ModelUtils;
import bdn.quantum.model.util.TransactionComparator;
import bdn.quantum.util.DateUtils;

@Service("chartService")
public class QPlotServiceImpl implements QPlotService {

	@Autowired
	private AssetService assetService;
	@Autowired
	private MarketDataService marketDataService;
	@Autowired
	private KeyvalService keyvalService;
	@Autowired
	private TransactionComparator transactionComparator;
	@Autowired
	private AssetSymbolManager assetSymbolManager;
	@Autowired
	private PortfolioSimulator portfolioSimulator;
	
	private HashMap<String, QPlotMemento> qPlotCache = new HashMap<>();
	
	
	@Override
	public QPlot getPlot(String plotName) {
		if (plotName == null || plotName.trim().equals("")) {
			return null;
		}
		
		QPlot result = null;
		String benchmarkSymbol = QuantumConstants.PLOT_STD_BENCHMARK_SYMBOL;
		
		// try to read benchmark symbol from db config
		StringBuffer key = new StringBuffer();
		key.append(QuantumProperties.PROP_PREFIX).append(QuantumProperties.QPLOT_BENCHMARK_SYMBOL);
		String benchmarkSymbolInProp = keyvalService.getKeyvalStr(key.toString());
		if (benchmarkSymbolInProp != null && ! benchmarkSymbolInProp.trim().equals("")) {
			benchmarkSymbol = benchmarkSymbolInProp.trim().toUpperCase();
		}
		
		result = getQPlotFromCache(plotName, benchmarkSymbol);
		
		if (result == null) {
			
			if (QuantumConstants.PLOT_STD_GROWTH.equals(plotName)) {
				// for Std Growth plot, we'll start with the date of the earliest transaction
				LocalDate startDate = null;
				List<Date> tranDateList = new ArrayList<>();
				List<Position> positionList = assetService.getPositions(true);
				for (Position p : positionList) {
					if (p != null && p.getTransactions() != null && p.getTransactions().size() > 0) {
						tranDateList.add(p.getTransactions().get(0).getTranDate());
					}
				}
				Date earliestTranDate = ModelUtils.getCalculatedDate(tranDateList, ModelUtils.CALC_EARLIEST_DATE);
				startDate = ModelUtils.stringToLocalDate(ModelUtils.dateToString(earliestTranDate));
				
				result = buildStdGrowthChart(positionList, benchmarkSymbol, startDate);
			}
			else if (QuantumConstants.PLOT_SIMULATED_TARGET.equals(plotName)) {
				result = buildSimTargetChart(benchmarkSymbol);
			}
			
			if (result != null) {
				addQPlotToCache(plotName, benchmarkSymbol, result);
			}
		}

		return result;
	}

	@Override
	public void clear() {
		qPlotCache.clear();
	}

	private QPlot buildStdGrowthChart(Iterable<Position> positionIter, String benchmarkSymbol, LocalDate startDate) {
		if (positionIter == null || benchmarkSymbol == null || startDate == null) {
			return null;
		}
		
		Iterable<QChart> benchmarkChartChain = marketDataService.getChartChain(benchmarkSymbol, startDate);
		if (benchmarkChartChain == null) {
			return null;
		}

		List<LocalDate> dateChain = buildDateChain(benchmarkChartChain);
		if (dateChain == null) {
			return null;
		}
		
		QPlot result = new QPlot(QPlot.QCHART_STD_GROWTH);
		
		HashMap<String, List<AbstractTransaction>> symbolToTransactionListMap = getTransactionListMapFromPositions(positionIter);
		
		QPlotSeries cashSeries = buildCashChartSeriesFromTransactions(dateChain, symbolToTransactionListMap);
		if (cashSeries == null) {
			return null;
		}
		
		QPlotSeries benchmarkSeries = buildChartSeries(QPlotSeries.QCHART_SERIES_BENCHMARK, dateChain, symbolToTransactionListMap, benchmarkChartChain);
		if (benchmarkSeries == null) {
			return null;
		}
		
		QPlotSeries userPotfolioSeries = buildChartSeries(QPlotSeries.QCHART_SERIES_USER_PORTFOLIO, dateChain, symbolToTransactionListMap, null);
		if (userPotfolioSeries == null) {
			return null;
		}
		
		
		// scale all series so that userPortfolioSeries end value matches actual portfolio end value
		// this is to reduce the deltas between actual portfolio performance and simulated portfolio performance
		Iterable<Asset> actualUserPortfolio = assetService.getAssets();
		BigDecimal actualUserPortfolioLastValue = BigDecimal.ZERO;
		for (Asset a : actualUserPortfolio) {
			actualUserPortfolioLastValue = actualUserPortfolioLastValue.add(a.getLastValue());
		}
		
		List<QPlotPoint> userPotfolioSeriesPoints = userPotfolioSeries.getPoints();
		BigDecimal simulatedUserPortfolioLastValue = userPotfolioSeriesPoints.get(userPotfolioSeriesPoints.size()-1).getValue();
		BigDecimal scalar = actualUserPortfolioLastValue.divide(simulatedUserPortfolioLastValue,
				QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);
		
		// scale only security series, not the cash series (since it's exact)
		benchmarkSeries.applyProgressiveScale(scalar);
		userPotfolioSeries.applyProgressiveScale(scalar);
		

		result.addSeries(cashSeries);
		result.addSeries(benchmarkSeries);
		result.addSeries(userPotfolioSeries);

		return result;
	}
	
	
	private QPlotSeries buildCashChartSeriesFromTransactions(List<LocalDate> dateChain,
					HashMap<String, List<AbstractTransaction>> symbolToTransactionListMap) {
		if (dateChain == null || symbolToTransactionListMap == null) {
			return null;
		}
		
		QPlotSeries result = new QPlotSeries(QPlotSeries.QCHART_SERIES_CASH);
		
		List<AbstractTransaction> allTranList = combineAndSortTransactions(symbolToTransactionListMap);
		
		BigDecimal portfolioCash = BigDecimal.ZERO;
		int nextTranIndex = 0;
		int pointId = 0;
		
		for (LocalDate ld : dateChain) {
			pointId++;
			
			if (nextTranIndex < allTranList.size()) {
				BigDecimal cashDelta = BigDecimal.ZERO;
				AbstractTransaction t = allTranList.get(nextTranIndex);
				LocalDate nextTranLocalDate = DateUtils.asLocalDate(t.getTranDate());
				
				while (nextTranLocalDate.isBefore(ld) || nextTranLocalDate.isEqual(ld)) {
					
					// adjust cash value by value bought / sold / received as dividend
					if (t.getType().equals(QuantumConstants.TRAN_TYPE_BUY)) {
						BigDecimal tranValue = t.getShares().multiply(t.getPrice());
						cashDelta = cashDelta.add(tranValue);
					}
					else if (t.getType().equals(QuantumConstants.TRAN_TYPE_SELL) || 
									t.getType().equals(QuantumConstants.TRAN_TYPE_DIVIDEND)) {
						BigDecimal tranValue = t.getShares().multiply(t.getPrice());
						cashDelta = cashDelta.subtract(tranValue);
					}
					
					nextTranIndex++;
					if (nextTranIndex >= allTranList.size()) {
						break;
					}
					t = allTranList.get(nextTranIndex);
					nextTranLocalDate = DateUtils.asLocalDate(t.getTranDate());
				}
				
				portfolioCash = portfolioCash.add(cashDelta);
			}
			
			QPlotPoint point = new QPlotPoint(Integer.valueOf(pointId), ld, portfolioCash);
			result.addPoint(point);
		}
		
		// normalize all negative cash values to zero for usability simplicity
		List<QPlotPoint> points = result.getPoints();
		for (QPlotPoint p : points) {
			if (p != null && p.getValue().compareTo(BigDecimal.ZERO) < 0) {
				p.setValue(BigDecimal.ZERO);
			}
		}
		
		return result;
	}
	
	private QPlotSeries buildCashChartSeriesFromSimulatedPrincipals(List<LocalDate> dateChain,
			BigDecimal initPrincipal, BigDecimal incrPrincipal, Integer incrFrequency) {
		
		if (dateChain == null || initPrincipal == null || incrPrincipal == null || incrFrequency == null) {
			return null;
		}

		QPlotSeries result = new QPlotSeries(QPlotSeries.QCHART_SERIES_CASH);

		BigDecimal portfolioCash = initPrincipal;
		int pointId = 0;

		for (LocalDate ld : dateChain) {
			
			if (PortfolioSimulator.INCR_PRINCIPAL_FREQ_DAILY.equals(incrFrequency)) {
				pointId++;
				portfolioCash = portfolioCash.add(incrPrincipal);
	
				QPlotPoint point = new QPlotPoint(Integer.valueOf(pointId), ld, portfolioCash);
				result.addPoint(point);
			}
		}

		return result;
	}

	private QPlotSeries buildChartSeries(Integer seriesType, List<LocalDate> dateChain,
			HashMap<String, List<AbstractTransaction>> symbolToTransactionListMap, Iterable<QChart> benchmarkChartChain) {
		if (seriesType == null || dateChain == null || symbolToTransactionListMap == null) {
			return null;
		}

		QPlotSeries result = new QPlotSeries(seriesType);

		List<QPlotPoint> points = buildPortfolioSeriesPoints(dateChain, symbolToTransactionListMap, benchmarkChartChain);
		result.setPoints(points);

		return result;
	}

	private List<QPlotPoint> buildPortfolioSeriesPoints(List<LocalDate> dateChain,
			HashMap<String, List<AbstractTransaction>> symbolToTransactionListMap, Iterable<QChart> singlePortfolioSecChartChain) {
		if (dateChain == null || symbolToTransactionListMap == null) {
			return null;
		}
		
		List<QPlotPoint> result = new ArrayList<>();
		
		List<List<QPlotPoint>> chartPointListsBySec = new ArrayList<>();
		Set<String> symbolSet = symbolToTransactionListMap.keySet();
		
		for (String s : symbolSet) {
			List<AbstractTransaction> secTranList = symbolToTransactionListMap.get(s);
			
			if (secTranList != null && secTranList.size() > 0) {
				// if all positions collapse to a simulated portfolio represented by a single security (e.g., benchmark)
				// get the chart chain for that security once
				Iterable<QChart> secChartChain = singlePortfolioSecChartChain;
				if (secChartChain == null) {
					secChartChain = marketDataService.getChartChain(s, dateChain.get(0));
				}
				
				if (secChartChain != null) {
					List<QPlotPoint> secPoints = buildSecuritySeriesPoints(dateChain, secTranList, secChartChain);
					chartPointListsBySec.add(secPoints);
				}
				else {
					System.err.println("QCharServiceImpl.buildPortfolioSeriesPoints - Could not get chart chain for symbol: " + s +
							". Graph will not include this security and may be inaccurate.");
				}
			}
		}

		if (chartPointListsBySec.size() < 1) {
			return null;
		}

		// to create user portfolio series, add the security values across all
		// securities
		for (int i = 0; i < chartPointListsBySec.get(0).size(); i++) {
			Integer id = chartPointListsBySec.get(0).get(i).getId();
			LocalDate localDate = chartPointListsBySec.get(0).get(i).getDate();

			BigDecimal portfolioValue = BigDecimal.ZERO;
			for (int j = 0; j < chartPointListsBySec.size(); j++) {
				portfolioValue = portfolioValue.add(chartPointListsBySec.get(j).get(i).getValue());
			}

			QPlotPoint p = new QPlotPoint(id, localDate, portfolioValue);
			result.add(p);
		}

		return result;
	}

	private List<QPlotPoint> buildSecuritySeriesPoints(List<LocalDate> dateChain, List<AbstractTransaction> secTranList,
			Iterable<QChart> secChartChain) {
		if (dateChain == null || secTranList == null || secChartChain == null) {
			return null;
		}

		List<QPlotPoint> result = new ArrayList<>();

		HashMap<LocalDate, QChart> dateToChartMap = new HashMap<>();
		for (QChart qc : secChartChain) {
			LocalDate ld = qc.getLocalDate();
			dateToChartMap.put(ld, qc);
		}

		BigDecimal secShares = BigDecimal.ZERO;
		int nextTranIndex = 0;
		int pointId = 0;

		for (LocalDate ld : dateChain) {
			pointId++;

			BigDecimal secValue = BigDecimal.ZERO;
			QChart qc = dateToChartMap.get(ld);

			if (qc != null) {
				if (nextTranIndex < secTranList.size()) {
					BigDecimal valueDelta = BigDecimal.ZERO;
					AbstractTransaction t = secTranList.get(nextTranIndex);
					LocalDate nextTranLocalDate = DateUtils.asLocalDate(t.getTranDate());

					while (nextTranLocalDate.isBefore(ld) || nextTranLocalDate.isEqual(ld)) {
						
						if (t.getType().equals(QuantumConstants.TRAN_TYPE_BUY)) {
							BigDecimal tranValue = t.getShares().multiply(t.getPrice());
							valueDelta = valueDelta.add(tranValue);
						}
						else if (t.getType().equals(QuantumConstants.TRAN_TYPE_SELL)) {
							BigDecimal tranValue = t.getShares().multiply(t.getPrice());
							valueDelta = valueDelta.subtract(tranValue);
						}

						nextTranIndex++;
						if (nextTranIndex >= secTranList.size()) {
							break;
						}
						t = secTranList.get(nextTranIndex);
						nextTranLocalDate = DateUtils.asLocalDate(t.getTranDate());
					}

					BigDecimal shareDelta = valueDelta.divide(qc.getClose(),
							QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);
					secShares = secShares.add(shareDelta);
				}

				secValue = secShares.multiply(qc.getClose());
			}

			QPlotPoint point = new QPlotPoint(Integer.valueOf(pointId), ld, secValue);
			result.add(point);
		}

		return result;
	}
	
	
	// Simulated Target Portfolio Chart
	private QPlot buildSimTargetChart(String benchmarkSymbol) {
		if (benchmarkSymbol == null) {
			return null;
		}
		
		// Read in initial and incremental principal values from settings; if not saved, fall back on defaults
		BigDecimal initPrincipal = null;
		StringBuffer key = new StringBuffer();
		key.append(QuantumProperties.PROP_PREFIX).append(QuantumProperties.QPLOT_SIM_TARGET_PRINCIPAL_INIT);
		String valueInProp = keyvalService.getKeyvalStr(key.toString());
		if (valueInProp != null && ! valueInProp.trim().equals("")) {
			try {
				initPrincipal = new BigDecimal(valueInProp.trim());
			}
			catch (Exception exc) {
				System.err.println(exc.getMessage());
				initPrincipal = null;
			}
		}
		if (initPrincipal == null) {
			initPrincipal = QuantumConstants.SIMULATED_TARGET_PRINCIPAL_INIT;
		}
		
		BigDecimal incrPrincipal = null;
		key = new StringBuffer();
		key.append(QuantumProperties.PROP_PREFIX).append(QuantumProperties.QPLOT_SIM_TARGET_PRINCIPAL_INCR);
		valueInProp = keyvalService.getKeyvalStr(key.toString());
		if (valueInProp != null && ! valueInProp.trim().equals("")) {
			try {
				incrPrincipal = new BigDecimal(valueInProp.trim());
			}
			catch (Exception exc) {
				System.err.println(exc.getMessage());
				incrPrincipal = null;
			}
		}
		if (incrPrincipal == null) {
			incrPrincipal = QuantumConstants.SIMULATED_TARGET_PRINCIPAL_INCR;
		}
		
		Integer incrFrequency = QuantumConstants.SIMULATED_TARGET_PRINCIPAL_INCR_FREQ;
		
		boolean wholeShares = false;
		key = new StringBuffer();
		key.append(QuantumProperties.PROP_PREFIX).append(QuantumProperties.QPLOT_SIM_TARGET_WHOLE_SHARES);
		valueInProp = keyvalService.getKeyvalStr(key.toString());
		if (valueInProp != null && valueInProp.trim().equals(QuantumProperties.STR_TRUE)) {
			wholeShares = true;
		}
		
		// Read setting for preferred # months for the simulated portfolio (if 0, calculate our own window)
		int simTargetWindowMonths = 0;
		key = new StringBuffer();
		key.append(QuantumProperties.PROP_PREFIX).append(QuantumProperties.QPLOT_SIM_TARGET_MONTHS);
		valueInProp = keyvalService.getKeyvalStr(key.toString());
		if (valueInProp != null && ! valueInProp.trim().equals("")) {
			try {
				simTargetWindowMonths = Integer.parseInt(valueInProp.trim());
				if (simTargetWindowMonths < 0) {
					simTargetWindowMonths = 0;
				}
			}
			catch (Exception exc) {
				System.err.println(exc.getMessage());
				simTargetWindowMonths = 0;
			}
		}
		LocalDate configuredStartDate = null;
		if (simTargetWindowMonths > 0) {
			configuredStartDate = LocalDate.now().minusMonths(simTargetWindowMonths);
		}
		
		
		HashMap<String, List<QChart>> symbolToChartChainMap = new HashMap<>();
		List<String> symbolList = new ArrayList<>();
		HashMap<String, BigDecimal> symbolToTargetRatioMap = new HashMap<>();
		List<Date> firstChartChainDates = new ArrayList<>();
		
		// Determine the start date of the 3 series by taking the latest beginning date of all securities involved
		// To do so, obtain the chart chains of all the securities involved, including the benchmark
		Iterable<Asset> assets = assetService.getAssets();
		for (Asset a : assets) {
			BigDecimal targetRatio = a.getTargetRatio();
			// only consider non-zero target ratios
			if (targetRatio != null && targetRatio.abs().doubleValue() >= QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
				String stockSymbol = assetSymbolManager.getSymbolForAsset(a.getBasketId());
				symbolToTargetRatioMap.put(stockSymbol, targetRatio);
				
				List<QChart> securityChartChain = marketDataService.getChartChain(stockSymbol, configuredStartDate);

				if (securityChartChain != null && securityChartChain.get(0) != null) {
					symbolList.add(stockSymbol);
					symbolToChartChainMap.put(stockSymbol, securityChartChain);
					firstChartChainDates.add(securityChartChain.get(0).getDate());
				}
			}
		}
		// if the benchmark chart chain was not loaded as part of the target securities, load it
		if (symbolToChartChainMap.get(benchmarkSymbol) == null) {
			List<QChart> benchmarkChartChain = marketDataService.getChartChain(benchmarkSymbol, configuredStartDate);
			if (benchmarkChartChain != null && benchmarkChartChain.get(0) != null) {
				symbolToChartChainMap.put(benchmarkSymbol, benchmarkChartChain);
				firstChartChainDates.add(benchmarkChartChain.get(0).getDate());
			}
			else {
				System.err.println("QPlotServiceImpl.buildSimTargetChart - Empty chart chain data for benchmark symbol: "+benchmarkSymbol);
				return null;
			}
		}
		// now determine the latest first date across all the chart chains
		Date latestFirstDate = ModelUtils.getCalculatedDate(firstChartChainDates, ModelUtils.CALC_LATEST_DATE);
		// clean up any QCharts in the chains before latestFirstDate, so all the chains start on the same date
		// Note: this may be a later date than the configuredStartDate (since the latter can be earlier than available chart chain data)
		for (String symbol : symbolToChartChainMap.keySet()) {
			List<QChart> qc = symbolToChartChainMap.get(symbol);
			while (!qc.isEmpty() && qc.get(0).getDate().before(latestFirstDate)) {
				qc.remove(0);
			}
		}
		
		// use the benchmark chain to build the date chain, since it's the only security guaranteed to have been populated
		List<LocalDate> dateChain = buildDateChain(symbolToChartChainMap.get(benchmarkSymbol));
		
		HashMap<String, List<AbstractTransaction>> symbolToTransactionsMap = null;
		
		// SIMULATED USER PORTFOLIO
		QPlotSeries userPortfolioSeries = null;
		
		try {
			symbolToTransactionsMap = portfolioSimulator.simulate(
					initPrincipal, 
					incrPrincipal,
					incrFrequency,
					wholeShares,
					symbolList,
					symbolToChartChainMap,
					symbolToTargetRatioMap);
		}
		catch (Exception exc) {
			System.err.println("QPlotServiceImpl.buildSimTargetChart - Exception in building simulated user portfolio: " + exc.getMessage());
			symbolToTransactionsMap = null;
		}		
		
		if (symbolToTransactionsMap != null) {
			userPortfolioSeries = buildChartSeries(QPlotSeries.QCHART_SERIES_SIM_TARGET_PORTFOLIO, dateChain, symbolToTransactionsMap, null);
		}
		if (userPortfolioSeries == null) {
			System.err.println("QPlotServiceImpl.buildSimTargetChart - Failed to create user portfolio series. Will proceed with benchmark series only.");
		}

		// SIMULATED BENCHMARK PORTFOLIO
		symbolList.clear();
		symbolList.add(benchmarkSymbol);
		symbolToTargetRatioMap.clear();
		symbolToTargetRatioMap.put(benchmarkSymbol, BigDecimal.ONE);
		
		try {
			symbolToTransactionsMap = portfolioSimulator.simulate(
					initPrincipal, 
					incrPrincipal,
					incrFrequency,
					wholeShares,
					symbolList,
					symbolToChartChainMap,
					symbolToTargetRatioMap);
		}
		catch (Exception exc) {
			System.err.println("QPlotServiceImpl.buildSimTargetChart - Exception in building simulated benchmark portfolio: " + exc.getMessage());
			symbolToTransactionsMap = null;
		}		
		
		QPlotSeries benchmarkSeries = null;
		if (symbolToTransactionsMap != null) {
			List<QChart> benchmarkChartChain = symbolToChartChainMap.get(benchmarkSymbol);
			benchmarkSeries = buildChartSeries(QPlotSeries.QCHART_SERIES_BENCHMARK, dateChain, symbolToTransactionsMap, benchmarkChartChain);
		}
		if (benchmarkSeries == null) {
			System.err.println("QPlotServiceImpl.buildSimTargetChart - Failed to create benchmark series");
			return null;
		}
		
		// CASH SERIES
		QPlotSeries cashSeries = buildCashChartSeriesFromSimulatedPrincipals(dateChain, initPrincipal, incrPrincipal, incrFrequency);
		if (cashSeries == null) {
			System.err.println("QPlotServiceImpl.buildSimTargetChart - Failed to create cash series");
			return null;
		}
		
		QPlot result = new QPlot(QPlot.QCHART_SIM_TARGET);
		result.addSeries(cashSeries);
		result.addSeries(benchmarkSeries);
		if (userPortfolioSeries != null) {
			result.addSeries(userPortfolioSeries);
		}
		
		return result;
	}
	
	private HashMap<String, List<AbstractTransaction>> getTransactionListMapFromPositions(Iterable<Position> positionIter) {
		HashMap<String, List<AbstractTransaction>> result = null;
		
		if (positionIter != null) {
			result = new HashMap<>();
			
			for (Position p : positionIter) {
				String symbol = p.getSymbol();
				List<Transaction> tList = p.getTransactions();
				List<AbstractTransaction> atList = new ArrayList<>();
				atList.addAll(tList);
				result.put(symbol, atList);
			}
		}
		
		return result;
	}

	private List<AbstractTransaction> combineAndSortTransactions(HashMap<String, List<AbstractTransaction>> symbolToTransactionListMap) {
		if (symbolToTransactionListMap == null) {
			return null;
		}
		
		List<AbstractTransaction> result = new ArrayList<>();
		
		Set<String> symbolSet = symbolToTransactionListMap.keySet();
		for (String s : symbolSet) {
			List<AbstractTransaction> tranList = symbolToTransactionListMap.get(s);
			if (tranList != null) {
				result.addAll(tranList);
			}
		}
		result.sort(transactionComparator);
		
		return result;
	}

	private List<LocalDate> buildDateChain(Iterable<QChart> chartChain) {
		if (chartChain == null) {
			return null;
		}
		
		List<LocalDate> result = new ArrayList<LocalDate>();
		for (QChart qc : chartChain) {
			LocalDate date = qc.getLocalDate();
			result.add(date);
		}
		
		return result;
	}
	
	private QPlot getQPlotFromCache(String plotName, String benchmarkSymbol) {
		if (plotName == null || plotName.trim().equals("") || 
						benchmarkSymbol == null || benchmarkSymbol.trim().equals("")) {
			return null;
		}
		
		QPlot result = null;
		
		String cacheKey = getCacheKey(plotName, benchmarkSymbol);
		QPlotMemento memento = qPlotCache.get(cacheKey);
		if (memento != null) {
			if (memento.getAgeInMillis() < QuantumConstants.PLOT_CACHE_LIFE_MILLIS) {
				result = memento.getQPlot();
			}
			else {
				qPlotCache.remove(cacheKey);
			}
		}
		return result;
	}
	
	private void addQPlotToCache(String plotName, String benchmarkSymbol, QPlot qPlot) {
		if (plotName == null || plotName.trim().equals("") || benchmarkSymbol == null
				|| benchmarkSymbol.trim().equals("") || qPlot == null) {
			return;
		}
		String cacheKey = getCacheKey(plotName, benchmarkSymbol);
		qPlotCache.put(cacheKey, new QPlotMemento(qPlot));
	}
	
	private String getCacheKey(String plotName, String benchmarkSymbol) {
		if (plotName == null || plotName.trim().equals("") || benchmarkSymbol == null
				|| benchmarkSymbol.trim().equals("")) {
			return null;
		}
		StringBuffer result = new StringBuffer();
		result.append(plotName).append("#!#").append(benchmarkSymbol);
		return result.toString();
	}

}

class QPlotMemento {
	private Date timestamp = new Date();
	private QPlot qPlot;
	
	public QPlotMemento(QPlot qPlot) {
		this.qPlot = qPlot;
	}
	
	public QPlot getQPlot() {
		return qPlot;
	}
	
	public long getAgeInMillis() {
		Date currTimestamp = new Date();
		long result = currTimestamp.getTime() - timestamp.getTime();
		return result;
	}
}
