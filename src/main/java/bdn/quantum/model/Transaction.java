package bdn.quantum.model;

import java.math.BigDecimal;
import java.util.Date;

//
//Security is a rough representation of TranEntity
//
public class Transaction implements AbstractTransaction {

	private static final Date CURRENT_DATE = new Date();
	
	private Integer id;
	private Integer secId;
	private Integer userId;
	private Date tranDate;
	private String type;
	private BigDecimal shares;
	private BigDecimal price;
	private BigDecimal tranValue = BigDecimal.ZERO;
	private BigDecimal totalShares = BigDecimal.ZERO;
	private BigDecimal principal = BigDecimal.ZERO;
	private BigDecimal principalDelta = BigDecimal.ZERO;
	private BigDecimal value = BigDecimal.ZERO;
	private BigDecimal realizedGain = BigDecimal.ZERO;
	private BigDecimal unrealizedGain = BigDecimal.ZERO;
	
	public Transaction() {
	}
	
	public Transaction(Integer id, Integer secId, Integer userId, Date tranDate, String type, BigDecimal shares,
			BigDecimal price) {
		this.id = id;
		this.secId = secId;
		this.userId = userId;
		this.tranDate = tranDate;
		this.type = type;
		this.shares = shares;
		this.price = price;
	}
	
	public Transaction(TranEntity te) {
		this(te.getId(), te.getSecId(), te.getUserId(), te.getTranDate(), te.getType(), te.getShares(), te.getPrice());
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getSecId() {
		return secId;
	}

	public void setSecId(Integer secId) {
		this.secId = secId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	@Override
	public Date getTranDate() {
		return tranDate;
	}

	public void setTranDate(Date tranDate) {
		this.tranDate = tranDate;
	}
	
	public boolean isInCurrentYear() {
		boolean result = false;
		if (tranDate != null) {
			result = (tranDate.getYear() == CURRENT_DATE.getYear());
		}
		return result;
	}

	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public BigDecimal getShares() {
		return shares;
	}

	public void setShares(BigDecimal shares) {
		this.shares = shares;
	}

	@Override
	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public BigDecimal getTranValue() {
		return tranValue;
	}

	public void setTranValue(BigDecimal tranValue) {
		this.tranValue = tranValue;
	}

	public BigDecimal getTotalShares() {
		return totalShares;
	}

	public void setTotalShares(BigDecimal totalShares) {
		this.totalShares = totalShares;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public BigDecimal getRealizedGain() {
		return realizedGain;
	}

	public void setRealizedGain(BigDecimal realizedGain) {
		this.realizedGain = realizedGain;
	}

	public BigDecimal getUnrealizedGain() {
		return unrealizedGain;
	}

	public void setUnrealizedGain(BigDecimal unrealizedGain) {
		this.unrealizedGain = unrealizedGain;
	}

	public void setPrincipal(BigDecimal principal) {
		this.principal = principal;
	}
	
	public BigDecimal getPrincipal() {
		return principal;
	}

	public void setPrincipalDelta(BigDecimal principalDelta) {
		this.principalDelta = principalDelta;
	}
	
	@Override
	public BigDecimal getPrincipalDelta() {
		return principalDelta;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("(").append(tranDate).append("::").append(type).append("::").append(shares).append("::").append(price).append(")");
		return sb.toString();
	}

}
