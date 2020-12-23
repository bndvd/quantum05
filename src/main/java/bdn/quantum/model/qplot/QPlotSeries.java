package bdn.quantum.model.qplot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import bdn.quantum.QuantumConstants;

public class QPlotSeries {

	public static final Integer QCHART_SERIES_UNDEFINED = Integer.valueOf(0);
	public static final Integer QCHART_SERIES_CASH = Integer.valueOf(1);
	public static final Integer QCHART_SERIES_BENCHMARK = Integer.valueOf(2);
	public static final Integer QCHART_SERIES_USER_PORTFOLIO = Integer.valueOf(3);
	public static final Integer QCHART_SERIES_SIM_TARGET_PORTFOLIO = Integer.valueOf(4);
	
	private Integer type = QCHART_SERIES_UNDEFINED;
	private List<QPlotPoint> points = new ArrayList<>();
	
	
	public QPlotSeries() {
	}
	
	public QPlotSeries(Integer type) {
		this.type = type;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public List<QPlotPoint> getPoints() {
		return points;
	}

	public void setPoints(List<QPlotPoint> points) {
		this.points = points;
	}
	
	public void addPoint(QPlotPoint point) {
		points.add(point);
	}
	
	public void applyProgressiveScale(BigDecimal scalar) {
		if (scalar == null || points == null) {
			return;
		}
		
		int numPoints = points.size();
		BigDecimal pointSpaces = new BigDecimal(numPoints -1 );
		BigDecimal scalarAdjustmentValue = scalar.subtract(BigDecimal.ONE);
		
		for (int i = 1; i < numPoints; i++) {
			BigDecimal pointPositionRatio = new BigDecimal(i).divide(pointSpaces, 
									QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);
			BigDecimal progressiveScalar = scalarAdjustmentValue.multiply(pointPositionRatio).add(BigDecimal.ONE);
			QPlotPoint p = points.get(i);
			p.scale(progressiveScalar);
		}
	}
	
	@Override
	public QPlotSeries clone() {
		QPlotSeries result = new QPlotSeries(this.type);
		for (QPlotPoint p : this.points) {
			QPlotPoint pClone = p.clone();
			result.addPoint(pClone);
		}
		return result;
	}
}
