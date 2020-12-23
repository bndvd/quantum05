package bdn.quantum.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import bdn.quantum.QuantumConstants;

@Entity
@Table(name=QuantumConstants.TABLE_SECURITY)
public class SecurityEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@Column(name = QuantumConstants.SECURITY_BASKET_ID)
	private Integer basketId;
	
	@Column(name = QuantumConstants.SECURITY_SYMBOL)
	private String symbol;
	
	
	public SecurityEntity() {
	}
	
	public SecurityEntity(Integer id, Integer basketId, String symbol) {
		this.id = id;
		this.basketId = basketId;
		this.symbol = symbol;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getBasketId() {
		return basketId;
	}

	public void setBasketId(Integer basketId) {
		this.basketId = basketId;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("Id:").append(id);
		strBuf.append(" BasketId:").append(basketId);
		strBuf.append(" Symbol:").append(symbol);
		return strBuf.toString();
	}
}
