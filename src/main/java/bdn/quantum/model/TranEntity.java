package bdn.quantum.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import bdn.quantum.QuantumConstants;

@Entity
@Table(name = QuantumConstants.TABLE_TRANSACTION)
public class TranEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(name = QuantumConstants.TRANSACTION_SEC_ID)
	private Integer secId;

	@Column(name = QuantumConstants.TRANSACTION_USER_ID)
	private Integer userId;

	@Column(name = QuantumConstants.TRANSACTION_TRAN_DATE)
	@Temporal(TemporalType.TIMESTAMP)
	private Date tranDate;

	@Column(name = QuantumConstants.TRANSACTION_TRAN_TYPE)
	private String type;

	// BUY/SELL: # shares transacted; DIVIDEND: # shares that earned dividend;
	// SPLIT: # new shares for each old share
	@Column(name = QuantumConstants.TRANSACTION_TRAN_SHARES, precision = 20, scale = 10)
	private BigDecimal shares;

	// BUY/SELL: price per share sold/bought; DIVIDEND: dividend per share; SPLIT:
	// undefined
	@Column(name = QuantumConstants.TRANSACTION_TRAN_PRICE, precision = 20, scale = 10)
	private BigDecimal price;

	public TranEntity() {
	}

	public TranEntity(Integer id, Integer secId, Integer userId, Date tranDate, String type, BigDecimal shares,
			BigDecimal price) {
		this.id = id;
		this.secId = secId;
		this.userId = userId;
		this.tranDate = tranDate;
		this.type = type;
		this.shares = shares;
		this.price = price;
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

	public Date getTranDate() {
		return tranDate;
	}

	public void setTranDate(Date tranDate) {
		this.tranDate = tranDate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public BigDecimal getShares() {
		return shares;
	}

	public void setShares(BigDecimal shares) {
		this.shares = shares;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	@Override
	public String toString() {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("Id:");
		strBuf.append(id);
		strBuf.append(", SecId:");
		strBuf.append(secId);
		strBuf.append(", Type:");
		strBuf.append(type);
		strBuf.append(", Date:");
		strBuf.append(tranDate);
		strBuf.append(", Shares:");
		strBuf.append(shares);
		strBuf.append(", Price:");
		strBuf.append(price);
		return strBuf.toString();
	}

}
