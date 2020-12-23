package bdn.quantum.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import bdn.quantum.QuantumConstants;

@Entity
@Table(name=QuantumConstants.TABLE_MKTSTATUS)
public class MarketStatusEntity {

	@Id
	@Column(name = QuantumConstants.MKTSTATUS_MKT_DATE)
	private String mktDate;

	@Column(name = QuantumConstants.MKTSTATUS_OPEN_STATUS)
	private Boolean openStatus;

	
	public MarketStatusEntity() {
	}
	
	public MarketStatusEntity(String mktDate, Boolean openStatus) {
		this.mktDate = mktDate;
		this.openStatus = openStatus;
	}
	
	public MarketStatusEntity(MarketStatus ms) {
		this.mktDate = ms.getMktDate();
		this.openStatus = ms.getOpenStatus();
	}

	public String getMktDate() {
		return mktDate;
	}

	public void setMktDate(String mktDate) {
		this.mktDate = mktDate;
	}

	public Boolean getOpenStatus() {
		return openStatus;
	}

	public void setOpenStatus(Boolean openStatus) {
		this.openStatus = openStatus;
	}
	
}
