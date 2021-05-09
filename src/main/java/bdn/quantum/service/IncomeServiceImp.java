package bdn.quantum.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bdn.quantum.QuantumConstants;
import bdn.quantum.model.CapitalGainFragment;
import bdn.quantum.model.DividendFragment;
import bdn.quantum.model.IncomeFragment;
import bdn.quantum.model.Security;
import bdn.quantum.model.Transaction;
import bdn.quantum.model.util.CapitalGainFragmentAcqComparator;
import bdn.quantum.model.util.IncomeFragmentComparator;
import bdn.quantum.model.util.TransactionComparator;
import bdn.quantum.util.QuantumDateUtils;


@Service("incomeService")
public class IncomeServiceImp implements IncomeService {

	@Autowired
	private AssetService assetService;
	@Autowired
	private IncomeFragmentComparator incomeFragmentComparator;
	@Autowired
	private CapitalGainFragmentAcqComparator capitalGainFragmentAcqComparator;
	@Autowired
	private TransactionComparator transactionComparator;
	

	@Override
	public List<IncomeFragment> getIncomeFragments(List<Transaction> tList) {
		List<CapitalGainFragment> cgfList = getCapitalGainFragments(tList);
		List<DividendFragment> dfList = getDividendFragments(tList);
		
		if (cgfList == null || dfList == null) {
			System.err.println("IncomeService::getIncomeFragments - ERROR: cap gains and/or div returned null. Canceling income calculations...");
			return null;
		}
		
		List<IncomeFragment> result = new ArrayList<>();
		result.addAll(cgfList);
		result.addAll(dfList);
		result.sort(incomeFragmentComparator);
		
		return result;
	}
	
	
	@Override
	public Map<Integer, List<IncomeFragment>> getIncomeFragmentsByTransaction(List<Transaction> tList) {
		List<IncomeFragment> ifList = getIncomeFragments(tList);
		if (ifList == null) {
			return null;
		}
		
		Map<Integer, List<IncomeFragment>> result = new HashMap<>();
		for (IncomeFragment incF : ifList) {
			// income tran ID is only present for fragments with income
			Integer tranId = incF.getIncomeTranId();
			if (tranId != null) {
				List<IncomeFragment> tranIFList = result.get(tranId);
				if (tranIFList == null) {
					tranIFList = new ArrayList<>();
					result.put(tranId, tranIFList);
				}
				tranIFList.add(incF);
			}
		}
		
		return result;
	}

	
	@Override
	public List<CapitalGainFragment> getCapitalGainFragments(List<Transaction> tList) {
		if (tList == null) {
			return null;
		}
		
		// separate transactions by security
		Map<Integer, List<Transaction>> secIdToTranListMap = getTransactionsBySecurity(tList);
		if (secIdToTranListMap == null) {
			return null;
		}
		
		List<CapitalGainFragment> cgfList = new ArrayList<>();
		Set<Integer> secIdSet = secIdToTranListMap.keySet();
		for (Integer secId : secIdSet) {
			List<Transaction> secTList = secIdToTranListMap.get(secId);
			List<CapitalGainFragment> cgfListForSec = getCapitalGainFragmentsForSecurity(secId, secTList);
			if (cgfListForSec != null) {
				cgfList.addAll(cgfListForSec);
			}
		}
		
		cgfList.sort(incomeFragmentComparator);

		return cgfList;
	}
	
	
	private Map<Integer, List<Transaction>> getTransactionsBySecurity(List<Transaction> tList) {
		if (tList == null) {
			return null;
		}
		
		Map<Integer, List<Transaction>> result = new HashMap<>();
		for (Transaction t : tList) {
			Integer secId = t.getSecId();
			List<Transaction> secTList = result.get(secId);
			if (secTList == null) {
				secTList = new ArrayList<>();
				result.put(secId, secTList);
			}
			secTList.add(t);
		}
		
		return result;
	}
	
	
	private List<CapitalGainFragment> getCapitalGainFragmentsForSecurity(Integer secId, List<Transaction> secTranList) {
		if (secId == null || secTranList == null) {
			return null;
		}
		
		Security security = assetService.getSecurity(secId);
		String symbol = security.getSymbol();
		secTranList.sort(transactionComparator);
		
		
		List<CapitalGainFragment> cgfList = new ArrayList<>();
		
		BigDecimal currShareConvFactor = BigDecimal.ONE;
		BigDecimal currShares = BigDecimal.ZERO;
		
		// FIRST PASS - create cost basis events
		for (Transaction t : secTranList) {
			Integer tId = t.getId();
			String tType = t.getType();
			Date tDate = t.getTranDate();
			BigDecimal tShares = t.getShares();
			BigDecimal tPrice = t.getPrice();
			
			if (tType == null || tDate == null || tShares == null || tPrice == null) {
				System.err.println("IncomeService::getCapitalGainFragmentsForSecurity - ERROR: encountered transaction with null data. Canceling capital gain calculations...");
				return null;
			}
			
			if (tType.equals(QuantumConstants.TRAN_TYPE_BUY)) {
				currShares = currShares.add(tShares);
				CapitalGainFragment cgf = new CapitalGainFragment(secId, symbol, tId, tDate, tShares, currShareConvFactor, tPrice);
				cgfList.add(cgf);				
			}
			else if (tType.equals(QuantumConstants.TRAN_TYPE_SELL)) {
				currShares = currShares.subtract(tShares);
				
				BigDecimal dispSharesToAllocateToCGF = tShares;
				int cgfListSize = cgfList.size();
				int cgfListIdx = 0;
				while (cgfListIdx < cgfListSize) {
					CapitalGainFragment cgf = cgfList.get(cgfListIdx);
					BigDecimal undisposedSharesInCGF = cgf.getUndisposedShares(currShareConvFactor);
					
					if (undisposedSharesInCGF.abs().doubleValue() >= QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
						BigDecimal dispAmountInCurrCGF = dispSharesToAllocateToCGF;
						if (dispSharesToAllocateToCGF.compareTo(undisposedSharesInCGF) > 0) {
							dispAmountInCurrCGF = undisposedSharesInCGF;
						}
							
						CapitalGainFragment newCGF = cgf.addDisposal(tId, tDate, dispAmountInCurrCGF, currShareConvFactor, tPrice);
						// if the disposal addition added a new CG fragment, insert it into the list
						if (newCGF != null) {
							cgfList.add(cgfListIdx, newCGF);
						}
						dispSharesToAllocateToCGF = dispSharesToAllocateToCGF.subtract(dispAmountInCurrCGF);
					}
					
					// exhausted the disposal shares
					if (dispSharesToAllocateToCGF.abs().doubleValue() < QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
						break;
					}
					cgfListIdx++;
					cgfListSize = cgfList.size();
				}
				
				if (dispSharesToAllocateToCGF.abs().doubleValue() >= QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
					System.err.println("IncomeService::getCapitalGainFragmentsForSecurity - ERROR: encountered higher disposal than acquisition for symbol:"
								+symbol+", tranId:"+tId+". Canceling capital gain calculations...");
					return null;					
				}
			}
			else if (tType.equals(QuantumConstants.TRAN_TYPE_DIVIDEND)) {
				// capital gains fragments do not include dividends, as they are not buy or sell transactions
			}
			else if (tType.equals(QuantumConstants.TRAN_TYPE_SPLIT)) {
				currShareConvFactor = currShareConvFactor.multiply(tShares);
				currShares = currShares.multiply(tShares);
			}
			else if (tType.equals(QuantumConstants.TRAN_TYPE_CONVERSION)) {
				currShareConvFactor = currShareConvFactor.multiply(
						tShares.divide(currShares, QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP));
				currShares = tShares;
			}
		}
		
		
		// SECOND PASS - compute Wash Sale adjustments

		// track all the CGFs not yet accounted for by wash sales in the Second Pass (to avoid double-counting)
		Set<CapitalGainFragment> washSaleUnaccountedCGFs = new HashSet<>();
		washSaleUnaccountedCGFs.addAll(cgfList);
		
		// incorporate adjustments into cost basis fragments, inserting fragments as necessary
		int cgfListSize = cgfList.size();
		int cgfListIdx = 0;
		while (cgfListIdx < cgfListSize) {
			CapitalGainFragment dispCGF = cgfList.get(cgfListIdx);
			
			BigDecimal perDispShareCapGain = dispCGF.getIncomePerDispShare();
			// if negative capital gain (capital loss)
			if (perDispShareCapGain != null && perDispShareCapGain.abs().doubleValue() >= QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO &&
					perDispShareCapGain.compareTo(BigDecimal.ZERO) < 0) {
				
				// make sure the original purchase of the capital loss is accounted for in wash sale accounting
				// (so in case it falls within the +/-30 day window, it's not mixed with repurchase events)
				washSaleUnaccountedCGFs.remove(dispCGF);
				
				
				// find all CGFs of buy transactions +/- 30 days of capital loss event
				// find all repurchase cases (unaccounted for by other wash sales or original purchases of capital loss shares)
				// apply cost basis and holding period adjustment(s) to the repurchase case(s) on a first come first served basis
				BigDecimal dispCGFShares = dispCGF.getDispShares();
				BigDecimal dispCGFShareConvFactor = dispCGF.getDispShareConvFactor();
				List<CapitalGainFragment> acqCGFsWithin30Days = getCGFsWithin30Days(dispCGF, washSaleUnaccountedCGFs);
				for (CapitalGainFragment acqCGF : acqCGFsWithin30Days) {
					BigDecimal acqCGFShares = acqCGF.getAcqShares();
					if (acqCGFShares != null &&
							acqCGFShares.abs().doubleValue() >= QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO &&
							acqCGFShares.compareTo(BigDecimal.ZERO) > 0) {
						
						BigDecimal acqCGFConvFactor = acqCGF.getAcqShareConvFactor();
						BigDecimal acqSharesDispShareEquivalent = acqCGFShares;
						if (dispCGFShareConvFactor.compareTo(acqCGFConvFactor) != 0) {
							acqSharesDispShareEquivalent = acqCGFShares.multiply(dispCGFShareConvFactor)
									.divide(acqCGFConvFactor, QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);
						}
						
						// Adjust the Disposal CGF
						BigDecimal dispSharesToAccount = dispCGFShares.min(acqSharesDispShareEquivalent);
						// make the adjustment positive
						BigDecimal perDispShareAdjustment = perDispShareCapGain.multiply(new BigDecimal(-1));
						CapitalGainFragment splitDispCGF = dispCGF.addWashSaleDisposalAdjustment(acqCGF.getAcqTranId(), perDispShareAdjustment,
								dispSharesToAccount);
						if (splitDispCGF != null) {
							cgfList.add(cgfListIdx, splitDispCGF);
							cgfListIdx++;
							cgfListSize = cgfList.size();
						}
						dispCGFShares = dispCGFShares.subtract(dispSharesToAccount);
						
						// Adjust the Acquiring CGF
						BigDecimal acqSharesToAccount = dispSharesToAccount;
						BigDecimal perAcqShareAdjustment = perDispShareAdjustment;
						if (dispCGFShareConvFactor.compareTo(acqCGFConvFactor) != 0) {
							// note share conversion (* target_share_conv_factor / src_share_conv_factor)
							acqSharesToAccount = dispSharesToAccount.multiply(acqCGFConvFactor)
									.divide(dispCGFShareConvFactor, QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);
							// note price conversion is "inverted" from share conversion (* src_share_conv_factor / target_share_conv_factor)
							perAcqShareAdjustment = perDispShareAdjustment.multiply(dispCGFShareConvFactor)
									.divide(acqCGFConvFactor, QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);
						}
						Integer dispCGFHoldingPeriod = dispCGF.getHoldingPeriod();
						CapitalGainFragment splitAcqCGF = acqCGF.addWashSaleAcquisitionAdjustment(dispCGF.getDispTranId(),
								perAcqShareAdjustment, dispCGFHoldingPeriod, acqSharesToAccount);
						if (splitAcqCGF != null) {
							cgfList.add(cgfListIdx, splitAcqCGF);
							cgfListIdx++;
							cgfListSize = cgfList.size();
						}
						else {
							washSaleUnaccountedCGFs.remove(acqCGF);
						}
					}
					// exhausted the CGF unadjusted wash sale shares (unaccounted disp CGF shares are <= 0)
					if (dispCGFShares.doubleValue() < QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
						break;
					}
				}
				
			}
			
			cgfListIdx++;
			cgfListSize = cgfList.size();
		}
		
		return cgfList;
	}
	
	
	private List<CapitalGainFragment> getCGFsWithin30Days(CapitalGainFragment dispCGF, Set<CapitalGainFragment> cgfSet) {
		if (dispCGF == null || cgfSet == null) {
			return null;
		}
		
		Date dispCGFDate = dispCGF.getIncomeDate();
		// window boundaries exclusive (the window dates are not included in the +/- 30 day window)
		Date windowBegin = QuantumDateUtils.addDays(dispCGFDate, -31);
		Date windowEnd = QuantumDateUtils.addDays(dispCGFDate, 31);
		
		List<CapitalGainFragment> result = new ArrayList<>();
		for (CapitalGainFragment cgf : cgfSet) {
			Date cgfDate = cgf.getAcqDate();
			if (cgfDate != null) {
				if (QuantumDateUtils.afterDay(cgfDate, windowBegin) && QuantumDateUtils.beforeDay(cgfDate, windowEnd)) {
					result.add(cgf);
				}
			}
		}
		result.sort(capitalGainFragmentAcqComparator);
		
		return result;
	}

	
	@Override
	public List<DividendFragment> getDividendFragments(List<Transaction> tList) {
		if (tList == null) {
			return null;
		}
		
		// separate transactions by security
		Map<Integer, List<Transaction>> secIdToTranListMap = getTransactionsBySecurity(tList);
		if (secIdToTranListMap == null) {
			return null;
		}
		
		List<DividendFragment> dfList = new ArrayList<>();
		Set<Integer> secIdSet = secIdToTranListMap.keySet();
		for (Integer secId : secIdSet) {
			List<Transaction> secTList = secIdToTranListMap.get(secId);
			List<DividendFragment> dfListForSec = getDividendFragmentsForSecurity(secId, secTList);
			if (dfListForSec != null) {
				dfList.addAll(dfListForSec);
			}
		}
		
		dfList.sort(incomeFragmentComparator);

		return dfList;
		
	}

	
	private List<DividendFragment> getDividendFragmentsForSecurity(Integer secId, List<Transaction> secTranList) {
		if (secId == null || secTranList == null) {
			return null;
		}
		
		Security security = assetService.getSecurity(secId);
		String symbol = security.getSymbol();
		secTranList.sort(transactionComparator);
		
		// capture all the dividend transactions so they can be checked against as we go through the history
		List<Transaction> unprocessedDivTransactions = new ArrayList<>();
		for (Transaction t : secTranList) {
			if (t.getType().equals(QuantumConstants.TRAN_TYPE_DIVIDEND)) {
				unprocessedDivTransactions.add(t);
			}
		}
		
		
		List<DividendFragment> dfList = new ArrayList<>();
		
		List<HoldingMemento> hmList = new ArrayList<>();
		
		for (Transaction t : secTranList) {
			Integer tId = t.getId();
			String tType = t.getType();
			Date tDate = t.getTranDate();
			BigDecimal tShares = t.getShares();
			BigDecimal tPrice = t.getPrice();
			
			if (tType == null || tDate == null || tShares == null || tPrice == null) {
				System.err.println("IncomeService::getDividendFragmentsForSecurity - ERROR: encountered transaction with null data. Canceling dividend income calculations...");
				return null;
			}
			
			// if we've processed all dividend transactions (or we had none to begin with), stop any further transaction processing
			if (unprocessedDivTransactions.isEmpty()) {
				break;
			}
			
			// before processing each transaction (incl. a div transaction), process for possible dividend fragment matches
			// (process current latest status BEFORE account for HM's of this transaction)
			// this allows for the proper check of # shares at the dttm point of this transaction (right before it)
			DividendFragment df = generateOrdDividend(secId, symbol, tDate, hmList, unprocessedDivTransactions);
			if (df != null) {
				dfList.add(df);
			}
			
			// now process all the transactions
			if (tType.equals(QuantumConstants.TRAN_TYPE_BUY)) {
				HoldingMemento hm = new HoldingMemento(tDate, tShares);
				hmList.add(hm);
			}
			else if (tType.equals(QuantumConstants.TRAN_TYPE_SELL)) {
				BigDecimal sharesToDispose = tShares;
				
				while (!hmList.isEmpty() && sharesToDispose.doubleValue() >= QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
					BigDecimal sharesInOldestAcq = hmList.get(0).shares;
					BigDecimal dispSharesInOldestAcq = sharesToDispose.min(sharesInOldestAcq);
					if (dispSharesInOldestAcq.compareTo(sharesInOldestAcq) == 0) {
						hmList.remove(0);
					}
					else {
						sharesInOldestAcq = sharesInOldestAcq.subtract(dispSharesInOldestAcq);
						hmList.get(0).shares = sharesInOldestAcq;
					}
					sharesToDispose = sharesToDispose.subtract(dispSharesInOldestAcq);
				}
				
				if (sharesToDispose.doubleValue() >= QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
					System.err.println("IncomeService::getDividendFragmentsForSecurity - ERROR: disposed of more shares than acqured at tranId:"+
							tId+", discrepancy:"+sharesToDispose+". Canceling dividend income calculations...");
					return null;
				}
			}
			else if (tType.equals(QuantumConstants.TRAN_TYPE_DIVIDEND)) {
				// do nothing - all dividend checks/processing is done at the end of each transaction
			}
			else if (tType.equals(QuantumConstants.TRAN_TYPE_SPLIT)) {
				BigDecimal convFactor = tShares;
				for (HoldingMemento hm : hmList) {
					hm.shares = hm.shares.multiply(convFactor);
				}
			}
			else if (tType.equals(QuantumConstants.TRAN_TYPE_CONVERSION)) {
				BigDecimal numSharesPriorToConv = BigDecimal.ZERO;
				for (HoldingMemento hm : hmList) {
					numSharesPriorToConv = numSharesPriorToConv.add(hm.shares);
				}
				
				BigDecimal convFactor = tShares.divide(numSharesPriorToConv, QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);
				for (HoldingMemento hm : hmList) {
					hm.shares = hm.shares.multiply(convFactor);
				}
			}
		}
		dfList.sort(incomeFragmentComparator);
		
		if (!unprocessedDivTransactions.isEmpty()) {
			for (Transaction t : unprocessedDivTransactions) {
				System.err.println("IncomeService::getDividendFragmentsForSecurity - ERROR: could not match div shares with acq shares at tranId:"+
					t.getId()+". Excluding dividend income calculation for this transaction...");
			}
		}
		
		return dfList;
	}
	
	
	// start looking for DividendFragment generation opportunity 10 days prior to Dividend Transaction date
	// if within this time period we find a matching number of shares, we go ahead and assume the dividend
	// applied to these shares and process the short-term / long-term calculations.
	// we cannot wait until the dividend date, since the dividend applies to the share snapshot a few days prior
	// (we assume within the last 10 days), and since that snapshot we could have sold shares, stock split/conversion
	// could have happened, etc., so we must anticipate beforehand.
	// we also assume that at any date, only one dividend can be applied (once we locate it, we don't search among
	// other dividends for this security)
	private DividendFragment generateOrdDividend(Integer secId, String symbol, Date date, List<HoldingMemento> hmList,
			List<Transaction> unprocessedDivTransactions) {
		if (date == null || hmList == null || unprocessedDivTransactions == null) {
			return null;
		}
		
		BigDecimal currentShareQty = BigDecimal.ZERO;
		for (HoldingMemento hm : hmList) {
			currentShareQty = currentShareQty.add(hm.shares);
		}
		
		DividendFragment result = null;
		for (Transaction divTran : unprocessedDivTransactions) {
			Date divTranDate = divTran.getTranDate();
			Date matchWindowStart = QuantumDateUtils.addDays(divTranDate, -11);
			Date matchWindowEnd = QuantumDateUtils.addDays(divTranDate, 1);
			BigDecimal divShares = divTran.getShares();
			
			// if date point in time is within last 10 days of div transaction and the number of shares matches
			if (QuantumDateUtils.afterDay(date, matchWindowStart) && QuantumDateUtils.beforeDay(date, matchWindowEnd) &&
					divShares.subtract(currentShareQty).abs().doubleValue() < QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
				
				// for simplicity we only compute the total ordinary dividends and not the qualified dividends
				// this implies the tax estimations will be done at the ordinary income rate (analogous to short-term cap gains rate)
				int hmIdx = 0;
				
				BigDecimal ordDivShares = BigDecimal.ZERO;
				while (hmIdx < hmList.size()) {
					ordDivShares = ordDivShares.add(hmList.get(hmIdx).shares);
					hmIdx++;
				}
				
				BigDecimal divAmnt = ordDivShares.multiply(divTran.getPrice());
				result = new DividendFragment(secId, symbol, divTran.getId(), divTranDate, divAmnt,
							QuantumConstants.COSTBASIS_SHORTTERM, ordDivShares);
				
				unprocessedDivTransactions.remove(divTran);
				break;
			}
		}
		
		return result;
	}
	
}


class HoldingMemento {
	protected Date acqDate = null;
	protected BigDecimal shares = BigDecimal.ZERO;
	
	public HoldingMemento(Date acqDate, BigDecimal shares) {
		this.acqDate = acqDate;
		this.shares = shares;
	}
	
	@Override
	public String toString() {
		return ""+acqDate+"::"+shares;
	}
}
