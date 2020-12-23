package bdn.quantum.model;

import java.math.BigDecimal;
import java.util.List;

import bdn.quantum.QuantumConstants;

//
// Position is a rough representation of Security and underlying Transactions
//
public class Position {

	public static final Position EMPTY_POSITION = new Position(0, "", BigDecimal.ZERO, BigDecimal.ZERO,
			BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
			BigDecimal.ZERO, null);

	private Integer secId;
	private String symbol;
	private BigDecimal principal;
	private BigDecimal totalPrincipal; // all money invested historically whether realized or unrealized gain
	private BigDecimal shares;
	private BigDecimal realizedGain; // profit/loss realized from sales of security or dividends
	private BigDecimal realizedGainYtd;
	private BigDecimal realizedGainYtdTax;
	private BigDecimal unrealizedGain;
	private BigDecimal lastPrice;
	private BigDecimal lastValue;
	private List<Transaction> transactions;  // sorted from oldest to newest

	public Position() {
	}

	public Position(Integer secId, String symbol, BigDecimal principal, BigDecimal totalPrincipal, BigDecimal shares,
			BigDecimal realizedGain, BigDecimal realizedGainYtd, BigDecimal realizedGainYtdTax, BigDecimal unrealizedGain,
			BigDecimal lastPrice, BigDecimal lastValue, List<Transaction> transactions) {
		setSecId(secId);
		setSymbol(symbol);
		setPrincipal(principal);
		setTotalPrincipal(totalPrincipal);
		setShares(shares);
		setRealizedGain(realizedGain);
		setRealizedGainYtd(realizedGainYtd);
		setRealizedGainYtdTax(realizedGainYtdTax);
		setUnrealizedGain(unrealizedGain);
		setLastPrice(lastPrice);
		setLastValue(lastValue);
		setTransactions(transactions);
	}

	public Integer getSecId() {
		return secId;
	}

	public void setSecId(Integer secId) {
		this.secId = secId;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public BigDecimal getPrincipal() {
		return principal;
	}

	public void setPrincipal(BigDecimal principal) {
		BigDecimal p = principal;
		if (principal.abs().doubleValue() < QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
			p = BigDecimal.ZERO;
		}

		this.principal = p;
	}

	public BigDecimal getShares() {
		return shares;
	}

	public void setShares(BigDecimal shares) {
		BigDecimal s = shares;
		if (shares.abs().doubleValue() < QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
			s = BigDecimal.ZERO;
		}

		this.shares = s;
	}

	public BigDecimal getRealizedGain() {
		return realizedGain;
	}

	public void setRealizedGain(BigDecimal realizedGain) {
		this.realizedGain = realizedGain;
	}

	public BigDecimal getLastPrice() {
		return lastPrice;
	}

	public void setLastPrice(BigDecimal price) {
		this.lastPrice = price;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	// Must be sorted from oldest to newest
	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}

	public BigDecimal getTotalPrincipal() {
		return totalPrincipal;
	}

	public void setTotalPrincipal(BigDecimal totalPrincipal) {
		this.totalPrincipal = totalPrincipal;
	}

	public BigDecimal getUnrealizedGain() {
		return unrealizedGain;
	}

	public void setUnrealizedGain(BigDecimal unrealizedGain) {
		this.unrealizedGain = unrealizedGain;
	}

	public BigDecimal getLastValue() {
		return lastValue;
	}

	public void setLastValue(BigDecimal lastValue) {
		this.lastValue = lastValue;
	}

	public BigDecimal getRealizedGainYtd() {
		return realizedGainYtd;
	}

	public void setRealizedGainYtd(BigDecimal realizedGainYtd) {
		this.realizedGainYtd = realizedGainYtd;
	}

	public BigDecimal getRealizedGainYtdTax() {
		return realizedGainYtdTax;
	}

	public void setRealizedGainYtdTax(BigDecimal realizedGainYtdTax) {
		this.realizedGainYtdTax = realizedGainYtdTax;
	}

	@Override
	public String toString() {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("SecId:");
		strBuf.append(secId);
		strBuf.append(", Symbol:");
		strBuf.append(symbol);
		return strBuf.toString();
	}

}
