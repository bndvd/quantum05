package bdn.quantum.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketStatus {

	@JsonProperty
	private String mktDate;
	@JsonProperty
	private Boolean openStatus;

	public MarketStatus() {
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
