package bdn.quantum.model.qplot;

import java.util.ArrayList;
import java.util.List;

public class QPlot {

	public static final Integer QCHART_STD_UNDEFINED = Integer.valueOf(0);
	public static final Integer QCHART_STD_GROWTH = Integer.valueOf(1);
	public static final Integer QCHART_SIM_TARGET = Integer.valueOf(2);
	
	private Integer type = QCHART_STD_UNDEFINED;
	private List<QPlotSeries> seriesList = new ArrayList<>();
	
	
	public QPlot() {
	}
	
	public QPlot(Integer type) {
		this.type = type;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public List<QPlotSeries> getSeriesList() {
		return seriesList;
	}

	public void setSeriesList(List<QPlotSeries> seriesList) {
		this.seriesList = seriesList;
	}

	public void addSeries(QPlotSeries series) {
		if (series == null) {
			return;
		}
		seriesList.add(series);
	}
	
	@Override
	public QPlot clone() {
		QPlot result = new QPlot(this.type);
		for (QPlotSeries s : this.seriesList) {
			QPlotSeries sClone = s.clone();
			result.addSeries(sClone);
		}
		return result;
	}
}
