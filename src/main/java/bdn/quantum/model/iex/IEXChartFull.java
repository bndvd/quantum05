package bdn.quantum.model.iex;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IEXChartFull extends IEXChart {

	private BigDecimal uClose;
	private BigDecimal uOpen;
	private BigDecimal uHigh;
	private BigDecimal uLow;
	private BigDecimal uVolume;
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal volume;
	
	public IEXChartFull() {
		super();
	}

	public BigDecimal getuClose() {
		return uClose;
	}

	public void setuClose(BigDecimal uClose) {
		this.uClose = uClose;
	}

	public BigDecimal getuOpen() {
		return uOpen;
	}

	public void setuOpen(BigDecimal uOpen) {
		this.uOpen = uOpen;
	}

	public BigDecimal getuHigh() {
		return uHigh;
	}

	public void setuHigh(BigDecimal uHigh) {
		this.uHigh = uHigh;
	}

	public BigDecimal getuLow() {
		return uLow;
	}

	public void setuLow(BigDecimal uLow) {
		this.uLow = uLow;
	}

	public BigDecimal getuVolume() {
		return uVolume;
	}

	public void setuVolume(BigDecimal uVolume) {
		this.uVolume = uVolume;
	}

	public BigDecimal getOpen() {
		return open;
	}

	public void setOpen(BigDecimal open) {
		this.open = open;
	}

	public BigDecimal getHigh() {
		return high;
	}

	public void setHigh(BigDecimal high) {
		this.high = high;
	}

	public BigDecimal getLow() {
		return low;
	}

	public void setLow(BigDecimal low) {
		this.low = low;
	}

	public BigDecimal getVolume() {
		return volume;
	}

	public void setVolume(BigDecimal volume) {
		this.volume = volume;
	}
	
	
}
