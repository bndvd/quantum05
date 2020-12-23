package bdn.quantum.model.qplot;

import java.math.BigDecimal;
import java.time.LocalDate;

public class QPlotPoint {

	private Integer id;
	private LocalDate date;
	private BigDecimal value;
	
	
	public QPlotPoint(Integer id, LocalDate date, BigDecimal value) {
		this.id = id;
		this.date = date;
		this.value = value;
	}

	public Integer getId() {
		return id;
	}

	public LocalDate getDate() {
		return date;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}
	
	public void scale(BigDecimal scalar) {
		BigDecimal v = getValue();
		if (scalar == null || v == null) {
			return;
		}
		setValue(v.multiply(scalar));
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("(").append(date).append(",").append(value).append(")");
		return sb.toString();
	}
	
	@Override
	public QPlotPoint clone() {
		QPlotPoint result = new QPlotPoint(this.id, this.date, this.value);
		return result;
	}
	
}
