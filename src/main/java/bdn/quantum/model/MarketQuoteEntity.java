package bdn.quantum.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import bdn.quantum.QuantumConstants;

@Entity
@Table(name=QuantumConstants.TABLE_MKTQUOTE)
public class MarketQuoteEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = QuantumConstants.MKTQUOTE_SYMBOL)
	private String symbol;

	@Column(name = QuantumConstants.MKTQUOTE_MKT_DATE)
	private String mktDate;

	@Column(name = QuantumConstants.MKTQUOTE_UNADJUSTED_CLOSE)
	private BigDecimal uClose;

	@Column(name = QuantumConstants.MKTQUOTE_UNADJUSTED_OPEN)
	private BigDecimal uOpen;

	@Column(name = QuantumConstants.MKTQUOTE_UNADJUSTED_HIGH)
	private BigDecimal uHigh;

	@Column(name = QuantumConstants.MKTQUOTE_UNADJUSTED_LOW)
	private BigDecimal uLow;

	@Column(name = QuantumConstants.MKTQUOTE_UNADJUSTED_VOLUME)
	private BigDecimal uVolume;

	@Column(name = QuantumConstants.MKTQUOTE_CLOSE)
	private BigDecimal close;
	// calculated adjusted close based on possible recent stock splits
	private BigDecimal adjustedClose = null;

	@Column(name = QuantumConstants.MKTQUOTE_OPEN)
	private BigDecimal open;

	@Column(name = QuantumConstants.MKTQUOTE_HIGH)
	private BigDecimal high;

	@Column(name = QuantumConstants.MKTQUOTE_LOW)
	private BigDecimal low;

	@Column(name = QuantumConstants.MKTQUOTE_VOLUME)
	private BigDecimal volume;

	
	public MarketQuoteEntity() {	
	}
	
	public MarketQuoteEntity(Long id, String symbol, String mktDate, BigDecimal uClose, BigDecimal uOpen, BigDecimal uHigh,
			BigDecimal uLow, BigDecimal uVolume, BigDecimal close, BigDecimal open, BigDecimal high, BigDecimal low,
			BigDecimal volume) {
		this.id = id;
		this.symbol = symbol;
		this.mktDate = mktDate;
		this.uClose = uClose;
		this.uOpen = uOpen;
		this.uHigh = uHigh;
		this.uLow = uLow;
		this.uVolume = uVolume;
		this.close = close;
		this.open = open;
		this.high = high;
		this.low = low;
		this.volume = volume;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getMktDate() {
		return mktDate;
	}

	public void setMktDate(String mktDate) {
		this.mktDate = mktDate;
	}

	public BigDecimal getUClose() {
		return uClose;
	}

	public void setUClose(BigDecimal uClose) {
		this.uClose = uClose;
	}

	public BigDecimal getUOpen() {
		return uOpen;
	}

	public void setUOpen(BigDecimal uOpen) {
		this.uOpen = uOpen;
	}

	public BigDecimal getUHigh() {
		return uHigh;
	}

	public void setUHigh(BigDecimal uHigh) {
		this.uHigh = uHigh;
	}

	public BigDecimal getULow() {
		return uLow;
	}

	public void setULow(BigDecimal uLow) {
		this.uLow = uLow;
	}

	public BigDecimal getUVolume() {
		return uVolume;
	}

	public void setUVolume(BigDecimal uVolume) {
		this.uVolume = uVolume;
	}

	public BigDecimal getClose() {
		return close;
	}
	
	public BigDecimal getAdjustedClose() {
		if (adjustedClose == null) {
			return getClose();
		}
		return adjustedClose;
	}

	public void setClose(BigDecimal close) {
		this.close = close;
	}
	
	public void setAdjustedClose(BigDecimal adjustedClose) {
		this.adjustedClose = adjustedClose;
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
