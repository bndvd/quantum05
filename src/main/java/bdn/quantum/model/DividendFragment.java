package bdn.quantum.model;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import bdn.quantum.util.QuantumDateUtils;

public class DividendFragment extends IncomeFragment {

	public DividendFragment(Integer secId, String symbol, Integer divTranId, Date divDate, BigDecimal divAmnt,
			String costBasisTerm, BigDecimal divShares) {
		super(secId, symbol);
		this.incomeTranId = divTranId;
		this.income = divAmnt;
		this.incomeDate = divDate;
		this.costBasisTerm = costBasisTerm;
		this.acqShares = divShares;
		
		if (divDate != null) {
			this.taxYear = QuantumDateUtils.getDateField(divDate, Calendar.YEAR);
		}
	}

	
}
