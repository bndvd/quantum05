package bdn.quantum.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketQuoteJson {

	@JsonProperty
	private String mktDate;
	@JsonProperty
	private String close;
	
	public MarketQuoteJson() {
	}

	public String getMktDate() {
		return mktDate;
	}

	public void setMktDate(String mktDate) {
		this.mktDate = mktDate;
	}

	public String getClose() {
		return close;
	}

	public void setClose(String close) {
		this.close = close;
	}
	
}
