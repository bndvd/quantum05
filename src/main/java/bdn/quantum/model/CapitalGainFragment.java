package bdn.quantum.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import bdn.quantum.QuantumConstants;
import bdn.quantum.util.QuantumDateUtils;

//
// Fragment of an asset disposal with a specific cost basis (e.g., from an earlier acquisition event) and for which
// a single wash sale adjustment is applied. Note: whenever the cost basis is no longer the same for all shares being 
// disposed that are represented in a fragment, the fragment is broken up in to smaller fragments to ensure each
// fragment has uniform properties.
//
public class CapitalGainFragment extends IncomeFragment {

	private static BigDecimal EQUALITY_MARGIN = new BigDecimal(QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO);
	
	// Acquisition
	private Integer acqTranId = null;
	private Date acqDate = null;
	//private BigDecimal acqShares --> defined in IncomeFragment original # shares (not accounting for future splits/conversions)
	private BigDecimal acqShareConvFactor = null;
	private BigDecimal acqSharePrice = null;		// original share price (not accounting for future splits/conversions)
	
	// Disposal
	private Integer dispTranId = null;
	private BigDecimal dispShares = null;
	private BigDecimal dispShareConvFactor = null;
	private BigDecimal dispSharePrice = null;
	
	// Wash Sale Adjustments - if this disposal is a wash sale
	private Integer washSaleAcqTranId = null;		// the acquisition (e.g., repurchase) that triggered a cap loss desposal to be a wash sale
	private BigDecimal washSaleCapGainPerDispShareAdj = null;		// adjustment in dollars/share of cap gains, which nullifies the cap loss
	
	// Wash Sale Adjustments - if this acquisition (e.g., a repurchase within 30 days) triggered a wash sale in another disposal fragment
	private Integer washSaleDispTranId = null;		// the disposal bearing the wash sale, which this fragment's acquisition caused
	private BigDecimal washSaleCostBasisPerAcqShareAdj = null;		// adjustment to in dollars/share of cost basis to the acqusition from wash sale
	private Integer washSaleHoldingPeriodAdj = null;		// # days of holding period adjustment from wash sale to the acquisition date
	
	// Computed Cost Basis Figures (computed after each update)
	private BigDecimal unadjCostBasis = null;				// unadjusted for wash sales
	private BigDecimal unadjCapGainPerDispShare = null;		// unadjusted for wash sales
	private BigDecimal unadjCapGain = null;					// unadjusted for wash sales
	private String unadjTerm = null;						// unadjusted for wash sales
	private BigDecimal costBasis = null;
	private BigDecimal saleProceeds = null;
	private BigDecimal washSaleAdj = null;		// wash sale adjustment to capital gains in a cap loss disposal
	private BigDecimal incomePerDispShare = null;
	
	
	// Initialize with acquisition data only
	public CapitalGainFragment(Integer secId, String symbol, Integer acqTranId, Date acqDate, BigDecimal acqShares,
			BigDecimal acqShareConvFactor, BigDecimal acqSharePrice) {
		super(secId, symbol);
		this.acqTranId = acqTranId;
		this.acqDate = acqDate;
		this.acqShares = acqShares;
		this.acqShareConvFactor = acqShareConvFactor;
		this.acqSharePrice = acqSharePrice;

		refreshCostBasisFigures();
	}

	// Initialize with acquisition and disposal data only
	public CapitalGainFragment(Integer secId, String symbol, Integer acqTranId, Date acqDate, BigDecimal acqShares,
			BigDecimal acqShareConvFactor, BigDecimal acqSharePrice, Integer dispTranId, Date dispDate, BigDecimal dispShares,
			BigDecimal dispShareConvFactor, BigDecimal dispSharePrice) {
		this(secId, symbol, acqTranId, acqDate, acqShares, acqShareConvFactor, acqSharePrice);
		this.dispTranId = dispTranId;
		this.incomeDate = dispDate;
		this.dispShares = dispShares;
		this.dispShareConvFactor = dispShareConvFactor;
		this.dispSharePrice = dispSharePrice;

		refreshCostBasisFigures();
	}

	public CapitalGainFragment(Integer secId, String symbol, Integer acqTranId, Date acqDate, BigDecimal acqShares,
			BigDecimal acqShareConvFactor, BigDecimal acqSharePrice, Integer dispTranId, Date dispDate, BigDecimal dispShares,
			BigDecimal dispShareConvFactor, BigDecimal dispSharePrice, Integer washSaleAcqTranId,
			BigDecimal washSaleCapGainPerDispShareAdj, Integer washSaleDispTranId,
			BigDecimal washSaleCostBasisPerAcqShareAdj, Integer washSaleHoldingPeriodAdj) {
		this(secId, symbol, acqTranId, acqDate, acqShares, acqShareConvFactor, acqSharePrice, dispTranId, dispDate, dispShares,
				dispShareConvFactor, dispSharePrice);
		this.washSaleAcqTranId = washSaleAcqTranId;
		this.washSaleCapGainPerDispShareAdj = washSaleCapGainPerDispShareAdj;
		this.washSaleDispTranId = washSaleDispTranId;
		this.washSaleCostBasisPerAcqShareAdj = washSaleCostBasisPerAcqShareAdj;
		this.washSaleHoldingPeriodAdj = washSaleHoldingPeriodAdj;

		refreshCostBasisFigures();
	}

	
	// Set disposal data; if disposal is for less shares than acquired shares in current fragment, split the acquired+disposed into a
	// new fragment and return it, updating the current fragment to carry the remainder of acquired shares that are not disposed
	public CapitalGainFragment addDisposal(Integer dispTranId, Date dispDate, BigDecimal dispShares, BigDecimal dispShareConvFactor,
			BigDecimal dispSharePrice) {
		if (this.acqTranId == null || this.acqDate == null || this.acqShares == null || this.acqShareConvFactor == null ||
				this.acqSharePrice == null) {
			System.err.println("CostBasisFragment::addDisposal - Cannot set disp data, since acq data is null");
			return null;
		}
		if (dispTranId == null || dispDate == null || dispShares == null || dispShareConvFactor == null || dispSharePrice == null) {
			System.err.println("CostBasisFragment::addDisposal - Cannot set disp data, since disp data is null");
			return null;
		}
		if (this.dispTranId != null || this.incomeDate != null || this.dispShares != null || this.dispShareConvFactor != null ||
				this.dispSharePrice != null) {
			System.err.println("CostBasisFragment::addDisposal - Cannot set disp data, since disp data is already set in this fragment");
			return null;
		}
		
		BigDecimal convAcqShares = this.acqShares;
		if (this.acqShareConvFactor.compareTo(dispShareConvFactor) != 0) {
			convAcqShares = this.acqShares.multiply(dispShareConvFactor)
					.divide(this.acqShareConvFactor, QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);
		}
		if (dispShares.compareTo(convAcqShares.add(EQUALITY_MARGIN)) > 0) {
			System.err.println("CostBasisFragment::addDisposal - Cannot set disp data, since disp shares > acq shares");
			return null;
		}
		
		CapitalGainFragment dispFragment = null;
		
		// if disposed shares equals the acquired equivalent shares, then take care of full disposal in this fragment
		if (convAcqShares.subtract(dispShares).abs().doubleValue() < QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
			this.dispTranId = dispTranId;
			this.incomeDate = dispDate;
			this.dispShares = dispShares;
			this.dispShareConvFactor = dispShareConvFactor;
			this.dispSharePrice = dispSharePrice;
		}
		else {
			BigDecimal dispFragmentAcqShares = dispShares.multiply(this.acqShareConvFactor)
					.divide(dispShareConvFactor, QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);
			dispFragment = new CapitalGainFragment(this.secId, this.symbol, this.acqTranId, this.acqDate, dispFragmentAcqShares,
					this.acqShareConvFactor, this.acqSharePrice, dispTranId, dispDate, dispShares, dispShareConvFactor, dispSharePrice);
			this.acqShares = this.acqShares.subtract(dispFragmentAcqShares);
		}
		
		refreshCostBasisFigures();
		
		return dispFragment;
	}

	
	public CapitalGainFragment addWashSaleDisposalAdjustment(Integer washSaleAcqTranId, BigDecimal washSaleCapGainPerDispShareAdj,
			BigDecimal affectedDispShares) {
		if (washSaleAcqTranId == null || washSaleCapGainPerDispShareAdj == null || affectedDispShares == null) {
			System.err.println("CostBasisFragment::addWashSaleDisposalAdjustment - Cannot add wash sale disposal adjustment data, since  data is null");
			return null;
		}
		if (this.washSaleAcqTranId != null || this.washSaleCapGainPerDispShareAdj != null) {
			System.err.println("CostBasisFragment::addWashSaleDisposalAdjustment - Cannot add wash sale disposal adjustment data, since  data is already set in this fragment");
			return null;
		}
		if (affectedDispShares.compareTo(this.dispShares.add(EQUALITY_MARGIN)) > 0) {
			System.err.println("CostBasisFragment::addWashSaleDisposalAdjustment - Cannot add wash sale disposal adjustment data, since affected disp shares > disp shares");
			return null;
		}
		
		CapitalGainFragment washSaleDispFragment = null;
		// if disposed shares affected by wash sale equals the disposed shares, then take care of wash sale update in this fragment
		if (affectedDispShares.subtract(this.dispShares).abs().doubleValue() < QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
			this.washSaleAcqTranId = washSaleAcqTranId;
			this.washSaleCapGainPerDispShareAdj = washSaleCapGainPerDispShareAdj;
		}
		// otherwise split into 2 fragments
		else {
			this.dispShares = this.dispShares.subtract(affectedDispShares);
			BigDecimal affectedAcqShares = affectedDispShares;
			if (this.acqShareConvFactor.compareTo(this.dispShareConvFactor) != 0) {
				affectedAcqShares = affectedDispShares.multiply(this.acqShareConvFactor)
						.divide(this.dispShareConvFactor, QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);
			}
			this.acqShares = this.acqShares.subtract(affectedAcqShares);
			if (this.acqShares.abs().doubleValue() >= QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO &&
					this.acqShares.compareTo(BigDecimal.ZERO) < 0) {
				System.err.println("CostBasisFragment::addWashSaleDisposalAdjustment - Cannot add wash sale disposal adjustment data, since affected acq shares > acq shares");
				return null;
			}
			
			washSaleDispFragment = new CapitalGainFragment(this.secId, this.symbol, this.acqTranId, this.acqDate, affectedAcqShares,
					this.acqShareConvFactor, this.acqSharePrice, this.dispTranId, this.incomeDate, affectedDispShares,
					this.dispShareConvFactor, this.dispSharePrice);
			washSaleDispFragment.addWashSaleDisposalAdjustment(washSaleAcqTranId, washSaleCapGainPerDispShareAdj, affectedDispShares);
		}
		
		refreshCostBasisFigures();
		
		return washSaleDispFragment;
	}
	
	
	public CapitalGainFragment addWashSaleAcquisitionAdjustment(Integer washSaleDispTranId, BigDecimal washSaleCostBasisPerAcqShareAdj,
			Integer washSaleHoldingPeriodAdj, BigDecimal affectedAcqShares) {
		if (washSaleDispTranId == null || washSaleCostBasisPerAcqShareAdj == null || washSaleHoldingPeriodAdj == null ||
				affectedAcqShares == null) {
			System.err.println("CostBasisFragment::addWashSaleAcquisitionAdjustment - Cannot add wash sale acquisition adjustment data, since  data is null");
			return null;
		}
		if (this.washSaleDispTranId != null || this.washSaleCostBasisPerAcqShareAdj != null || this.washSaleHoldingPeriodAdj != null) {
			System.err.println("CostBasisFragment::addWashSaleAcquisitionAdjustment - Cannot add wash sale acquisition adjustment data, since  data is already set in this fragment");
			return null;
		}
		if (affectedAcqShares.compareTo(this.acqShares.add(EQUALITY_MARGIN)) > 0) {
			System.err.println("CostBasisFragment::addWashSaleAcquisitionAdjustment - Cannot add wash sale acquisition adjustment data, since affected acq shares > acq shares");
			return null;
		}
		
		CapitalGainFragment washSaleAcqFragment = null;
		// if acquired shares affected by wash sale equals the acquired shares, then take care of wash sale update in this fragment
		if (affectedAcqShares.subtract(this.acqShares).abs().doubleValue() < QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
			this.washSaleDispTranId = washSaleDispTranId;
			this.washSaleCostBasisPerAcqShareAdj = washSaleCostBasisPerAcqShareAdj;
			this.washSaleHoldingPeriodAdj = washSaleHoldingPeriodAdj;
		}
		// otherwise split into 2 fragments
		else {
			this.acqShares = this.acqShares.subtract(affectedAcqShares);
			// if there are disposal shares in this fragment, take care of splitting them as well
			BigDecimal affectedDispShares = null;
			if (this.dispShares != null) {
				affectedDispShares = affectedAcqShares;
				if (this.acqShareConvFactor.compareTo(this.dispShareConvFactor) != 0) {
					affectedDispShares = affectedAcqShares.multiply(this.dispShareConvFactor)
							.divide(this.acqShareConvFactor, QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);
				}
				this.dispShares = this.dispShares.subtract(affectedDispShares);
				if (this.dispShares.abs().doubleValue() >= QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO &&
						this.dispShares.compareTo(BigDecimal.ZERO) < 0) {
					System.err.println("CostBasisFragment::addWashSaleAcquisitionAdjustment - Cannot add wash sale acquisition adjustment data, since affected disp shares > disp shares");
					return null;
				}
			}
			
			washSaleAcqFragment = new CapitalGainFragment(this.secId, this.symbol, this.acqTranId, this.acqDate, affectedAcqShares,
					this.acqShareConvFactor, this.acqSharePrice, this.dispTranId, this.incomeDate, affectedDispShares,
					this.dispShareConvFactor, this.dispSharePrice);
			washSaleAcqFragment.addWashSaleAcquisitionAdjustment(washSaleDispTranId, washSaleCostBasisPerAcqShareAdj,
					washSaleHoldingPeriodAdj, affectedAcqShares);
		}
		
		refreshCostBasisFigures();
		
		return washSaleAcqFragment;
	}
	
	
	private void refreshCostBasisFigures() {
		unadjCostBasis = null;
		costBasis = null;
		saleProceeds = null;
		washSaleAdj = null;
		unadjCapGain = null;
		unadjCapGainPerDispShare = null;
		incomeTranId = dispTranId;
		income = null;
		incomePerDispShare = null;
		unadjTerm = null;
		costBasisTerm = null;
		taxYear = null;
		
		if (acqShares != null && acqSharePrice != null) {
			unadjCostBasis = acqShares.multiply(acqSharePrice);
			costBasis = unadjCostBasis;
			if (washSaleCostBasisPerAcqShareAdj != null) {
				costBasis = costBasis.add(acqShares.multiply(washSaleCostBasisPerAcqShareAdj));
			}
		}
		if (dispShares != null && dispSharePrice != null) {
			saleProceeds = dispShares.multiply(dispSharePrice);
		}
		if (dispShares != null && washSaleCapGainPerDispShareAdj != null) {
			washSaleAdj = dispShares.multiply(washSaleCapGainPerDispShareAdj);
		}
		if (costBasis != null && saleProceeds != null && dispShares != null) {
			unadjCapGain = saleProceeds.subtract(costBasis);
			unadjCapGainPerDispShare = unadjCapGain.divide(dispShares, QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);

			income = unadjCapGain;
			if (washSaleAdj != null) {
				income = income.add(washSaleAdj);
			}
			incomePerDispShare = income.divide(dispShares, QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);
		}
		if (acqDate != null && incomeDate != null) {
			Date oneYearPreDisp = QuantumDateUtils.addYears(incomeDate, -1);
			unadjTerm = QuantumConstants.COSTBASIS_SHORTTERM;
			if (QuantumDateUtils.beforeDay(acqDate, oneYearPreDisp)) {
				unadjTerm = QuantumConstants.COSTBASIS_LONGTERM;
			}
			
			Date adjustedAcqDate = acqDate;
			if (washSaleHoldingPeriodAdj != null) {
				adjustedAcqDate = QuantumDateUtils.addDays(adjustedAcqDate, -1 * washSaleHoldingPeriodAdj);
			}
			costBasisTerm = QuantumConstants.COSTBASIS_SHORTTERM;
			if (QuantumDateUtils.beforeDay(adjustedAcqDate, oneYearPreDisp)) {
				costBasisTerm = QuantumConstants.COSTBASIS_LONGTERM;
			}
		}
		if (incomeDate != null) {
			taxYear = QuantumDateUtils.getDateField(incomeDate, Calendar.YEAR);
		}
	}
	
	
	public BigDecimal getUndisposedShares(BigDecimal shareConfFactor) {
		if (acqShares == null || acqShareConvFactor == null || shareConfFactor == null) {
			// should not happen
			return null;
		}
		
		BigDecimal undisposedSharesInFragment = BigDecimal.ZERO;
		
		// if dispShares are defined, there are no undisposed shares (fragments do not contain partially disposed shares, instead
		// they are split up when that happens)
		if (dispShares == null) {
			undisposedSharesInFragment = acqShares;
			if (acqShareConvFactor.compareTo(shareConfFactor) != 0) {
				undisposedSharesInFragment = undisposedSharesInFragment.multiply(shareConfFactor)
						.divide(acqShareConvFactor, QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);
			}	
		}
		
		return undisposedSharesInFragment;
	}
	
	
	public Integer getHoldingPeriod() {
		int result = 0;
		if (acqDate != null && incomeDate != null && acqDate.before(incomeDate)) {
			result = QuantumDateUtils.dayDifference(acqDate, incomeDate);
		}
		return result;
	}
	
	
	public Integer getAcqTranId() {
		return acqTranId;
	}



	public Date getAcqDate() {
		return acqDate;
	}



	public BigDecimal getAcqShares() {
		return acqShares;
	}



	public BigDecimal getAcqShareConvFactor() {
		return acqShareConvFactor;
	}



	public BigDecimal getAcqSharePrice() {
		return acqSharePrice;
	}



	public Integer getDispTranId() {
		return dispTranId;
	}



	public BigDecimal getDispShares() {
		return dispShares;
	}



	public BigDecimal getDispShareConvFactor() {
		return dispShareConvFactor;
	}



	public BigDecimal getDispSharePrice() {
		return dispSharePrice;
	}



	public Integer getWashSaleAcqTranId() {
		return washSaleAcqTranId;
	}



	public BigDecimal getWashSaleCapGainPerDispShareAdj() {
		return washSaleCapGainPerDispShareAdj;
	}



	public Integer getWashSaleDispTranId() {
		return washSaleDispTranId;
	}



	public BigDecimal getWashSaleCostBasisPerAcqShareAdj() {
		return washSaleCostBasisPerAcqShareAdj;
	}



	public Integer getWashSaleHoldingPeriodAdj() {
		return washSaleHoldingPeriodAdj;
	}



	public BigDecimal getUnadjCostBasis() {
		return unadjCostBasis;
	}


	
	public BigDecimal getUnadjCapGainPerDispShare() {
		return unadjCapGainPerDispShare;
	}
	
	

	public BigDecimal getUnadjCapGain() {
		return unadjCapGain;
	}
	

	
	public String getUnadjTerm() {
		return unadjTerm;
	}
	
	

	public BigDecimal getCostBasis() {
		return costBasis;
	}



	public BigDecimal getSaleProceeds() {
		return saleProceeds;
	}
	
	
	
	public BigDecimal getWashSaleAdj() {
		return washSaleAdj;
	}



	public BigDecimal getIncomePerDispShare() {
		return incomePerDispShare;
	}

	
	public Integer getTaxYear() {
		return taxYear;
	}
	
	
	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return "ACQ:"+sdf.format(acqDate)+":"+acqShares+":"+acqSharePrice+" DISP:"+(incomeDate != null ? sdf.format(incomeDate): null)+":"+
				dispShares+":"+dispSharePrice+" WASH-D:"+
				washSaleCapGainPerDispShareAdj+" WASH-A:"+washSaleCostBasisPerAcqShareAdj+" CG:"+costBasisTerm+":"+costBasis+":"+saleProceeds+
				":"+washSaleAdj+":"+income;
	}
}
