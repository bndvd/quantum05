package bdn.quantum.service;

import bdn.quantum.model.qplot.QPlot;

public interface QPlotService {

	public void clear();
	public QPlot getPlot(String plotName);
	
}
