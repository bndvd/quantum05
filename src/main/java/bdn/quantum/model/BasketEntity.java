package bdn.quantum.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import bdn.quantum.QuantumConstants;

@Entity
@Table(name=QuantumConstants.TABLE_BASKET)
public class BasketEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@Column(name = QuantumConstants.BASKET_NAME)
	private String name;

	public BasketEntity() {
	}
	
	public BasketEntity(Integer id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("Id:").append(id);
		strBuf.append(" Name:").append(name);
		return strBuf.toString();
	}

}
