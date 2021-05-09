package bdn.quantum.model;

import java.math.BigDecimal;
import java.util.Date;

public abstract class IncomeFragment {

	// Security Info
	protected Integer secId = null;
	protected String symbol = null;
	
	protected BigDecimal acqShares = null;
	protected Integer incomeTranId = null;
	protected String costBasisTerm = null;
	protected BigDecimal income = null;
	protected Date incomeDate = null;
	protected Integer taxYear = null;
	
	
	public IncomeFragment(Integer secId, String symbol) {
		this.secId = secId;
		this.symbol = symbol;
	}


	public Integer getSecId() {
		return secId;
	}


	public String getSymbol() {
		return symbol;
	}


	public BigDecimal getAcqShares() {
		return acqShares;
	}


	public Integer getIncomeTranId() {
		return incomeTranId;
	}


	public String getCostBasisTerm() {
		return costBasisTerm;
	}


	public BigDecimal getIncome() {
		return income;
	}


	public Date getIncomeDate() {
		return incomeDate;
	}


	public Integer getTaxYear() {
		return taxYear;
	}

	
}
