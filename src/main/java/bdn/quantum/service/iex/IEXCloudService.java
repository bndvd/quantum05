package bdn.quantum.service.iex;

import java.math.BigDecimal;

import bdn.quantum.model.iex.IEXChartFull;
import bdn.quantum.model.iex.IEXChart;
import bdn.quantum.model.iex.IEXTradeDay;

public interface IEXCloudService {

	public BigDecimal getPrice(String symbol);
	public IEXChartFull getChart(String symbol, String date);
	public Iterable<IEXChart> getMaxChart(String symbol);
	public Iterable<IEXTradeDay> getTradeDays(int numTradeDays);
	public void reset();
	
}
