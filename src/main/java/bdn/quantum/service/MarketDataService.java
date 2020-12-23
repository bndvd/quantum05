package bdn.quantum.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import bdn.quantum.model.qplot.QChart;

public interface MarketDataService {

	public void configChanged();
	public BigDecimal getLastPrice(String symbol);
	public List<QChart> getChartChain(String symbol, LocalDate startDate);
	
}
