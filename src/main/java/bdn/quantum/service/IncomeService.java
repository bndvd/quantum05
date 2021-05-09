package bdn.quantum.service;

import java.util.List;
import java.util.Map;

import bdn.quantum.model.CapitalGainFragment;
import bdn.quantum.model.DividendFragment;
import bdn.quantum.model.IncomeFragment;
import bdn.quantum.model.Transaction;

public interface IncomeService {

	public List<IncomeFragment> getIncomeFragments(List<Transaction> tList);
	public Map<Integer, List<IncomeFragment>> getIncomeFragmentsByTransaction(List<Transaction> tList);
	public List<CapitalGainFragment> getCapitalGainFragments(List<Transaction> tList);
	public List<DividendFragment> getDividendFragments(List<Transaction> tList);
	
}
