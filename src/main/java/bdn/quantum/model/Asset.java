package bdn.quantum.model;

import java.math.BigDecimal;

//
// Asset is a rough representation of a Basket, and is a grouping of one or more Positions that 
// represent the same underlying asset (e.g., an index fund and an ETF of the same asset)
//
public class Asset {

	private Integer basketId;
	private String basketName;
	private BigDecimal principal = BigDecimal.ZERO;
	private BigDecimal totalPrincipal = BigDecimal.ZERO;
	private BigDecimal lastValue = BigDecimal.ZERO;
	private BigDecimal unrealizedGain = BigDecimal.ZERO;
	private BigDecimal realizedGain = BigDecimal.ZERO;
	private BigDecimal realizedGainYtd = BigDecimal.ZERO;
	private BigDecimal ytdShortTermTax = BigDecimal.ZERO;
	private BigDecimal ytdLongTermTax = BigDecimal.ZERO;
	private BigDecimal ytdShortTermTaxAdj = BigDecimal.ZERO;
	private BigDecimal ytdLongTermTaxAdj = BigDecimal.ZERO;


	// optional elements - present only when all Assets are returned for portfolio and these
	// statistics can be calculated relative to the portfolio
	private BigDecimal targetRatio = BigDecimal.ZERO;
	private BigDecimal currentRatio = BigDecimal.ZERO;
	private BigDecimal ratioDeltaValue = BigDecimal.ZERO;
	private BigDecimal contribution = BigDecimal.ZERO;

	
	public Asset() {
	}

	public Asset(Integer basketId, String basketName, BigDecimal principal, BigDecimal totalPrincipal, BigDecimal lastValue,
			BigDecimal unrealizedGain, BigDecimal realizedGain, BigDecimal realizedGainYtd, BigDecimal ytdShortTermTax,
			BigDecimal ytdLongTermTax, BigDecimal ytdShortTermTaxAdj, BigDecimal ytdLongTermTaxAdj) {
		this.basketId = basketId;
		this.basketName = basketName;
		this.principal = principal;
		this.totalPrincipal = totalPrincipal;
		this.lastValue = lastValue;
		this.unrealizedGain = unrealizedGain;
		this.realizedGain = realizedGain;
		this.realizedGainYtd = realizedGainYtd;
		this.ytdShortTermTax = ytdShortTermTax;
		this.ytdLongTermTax = ytdLongTermTax;
		this.ytdShortTermTaxAdj = ytdShortTermTaxAdj;
		this.ytdLongTermTaxAdj = ytdLongTermTaxAdj;
	}
	
	public Asset(BasketEntity be) {
		this.basketId = be.getId();
		this.basketName = be.getName();
	}

	public Integer getBasketId() {
		return basketId;
	}

	public void setBasketId(Integer basketId) {
		this.basketId = basketId;
	}

	public String getBasketName() {
		return basketName;
	}

	public void setBasketName(String basketName) {
		this.basketName = basketName;
	}

	public BigDecimal getPrincipal() {
		return principal;
	}

	public void setPrincipal(BigDecimal principal) {
		this.principal = principal;
	}

	public BigDecimal getLastValue() {
		return lastValue;
	}

	public void setLastValue(BigDecimal lastValue) {
		this.lastValue = lastValue;
	}

	public BigDecimal getRealizedGain() {
		return realizedGain;
	}

	public void setRealizedGain(BigDecimal realizedGain) {
		this.realizedGain = realizedGain;
	}

	public BigDecimal getTotalPrincipal() {
		return totalPrincipal;
	}

	public void setTotalPrincipal(BigDecimal totalPrincipal) {
		this.totalPrincipal = totalPrincipal;
	}

	public BigDecimal getTargetRatio() {
		return targetRatio;
	}

	public void setTargetRatio(BigDecimal targetRatio) {
		this.targetRatio = targetRatio;
	}

	public BigDecimal getCurrentRatio() {
		return currentRatio;
	}

	public void setCurrentRatio(BigDecimal currentRatio) {
		this.currentRatio = currentRatio;
	}

	public BigDecimal getRatioDeltaValue() {
		return ratioDeltaValue;
	}

	public void setRatioDeltaValue(BigDecimal ratioDeltaValue) {
		this.ratioDeltaValue = ratioDeltaValue;
	}

	public BigDecimal getContribution() {
		return contribution;
	}

	public void setContribution(BigDecimal contribution) {
		this.contribution = contribution;
	}

	public BigDecimal getUnrealizedGain() {
		return unrealizedGain;
	}

	public void setUnrealizedGain(BigDecimal unrealizedGain) {
		this.unrealizedGain = unrealizedGain;
	}

	public BigDecimal getRealizedGainYtd() {
		return realizedGainYtd;
	}

	public void setRealizedGainYtd(BigDecimal realizedGainYtd) {
		this.realizedGainYtd = realizedGainYtd;
	}

	public BigDecimal getYtdShortTermTax() {
		return ytdShortTermTax;
	}

	public void setYtdShortTermTax(BigDecimal ytdShortTermTax) {
		this.ytdShortTermTax = ytdShortTermTax;
	}

	public BigDecimal getYtdLongTermTax() {
		return ytdLongTermTax;
	}

	public void setYtdLongTermTax(BigDecimal ytdLongTermTax) {
		this.ytdLongTermTax = ytdLongTermTax;
	}

	public BigDecimal getYtdShortTermTaxAdj() {
		return ytdShortTermTaxAdj;
	}

	public void setYtdShortTermTaxAdj(BigDecimal ytdShortTermTaxAdj) {
		this.ytdShortTermTaxAdj = ytdShortTermTaxAdj;
	}

	public BigDecimal getYtdLongTermTaxAdj() {
		return ytdLongTermTaxAdj;
	}

	public void setYtdLongTermTaxAdj(BigDecimal ytdLongTermTaxAdj) {
		this.ytdLongTermTaxAdj = ytdLongTermTaxAdj;
	}

	@Override
	public String toString() {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("BasketId:");
		strBuf.append(basketId);
		strBuf.append(", BasketName:");
		strBuf.append(basketName);
		return strBuf.toString();
	}

}
