package bdn.quantum.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bdn.quantum.QuantumConstants;
import bdn.quantum.QuantumProperties;
import bdn.quantum.model.Asset;
import bdn.quantum.model.BasketEntity;
import bdn.quantum.model.CapitalGainFragment;
import bdn.quantum.model.IncomeFragment;
import bdn.quantum.model.Position;
import bdn.quantum.model.Security;
import bdn.quantum.model.SecurityEntity;
import bdn.quantum.model.Transaction;
import bdn.quantum.model.util.AssetComparator;
import bdn.quantum.model.util.PositionComparator;
import bdn.quantum.model.util.SecurityComparator;
import bdn.quantum.repository.BasketRepository;
import bdn.quantum.repository.SecurityRepository;
import bdn.quantum.util.QuantumDateUtils;

@Service("assetService")
public class AssetServiceImpl implements AssetService {

	@Autowired
	private BasketRepository basketRepository;
	@Autowired
	private SecurityRepository securityRepository;
	@Autowired
	private TransactionService transactionService;
	@Autowired
	private IncomeService incomeService;
	@Autowired
	private KeyvalService keyvalService;
	@Autowired
	private MarketDataService marketDataService;

	@Autowired
	private PositionComparator positionComparator;
	@Autowired
	private AssetComparator assetComparator;
	@Autowired
	private SecurityComparator securityComparator;

	@Override
	public Iterable<BasketEntity> getBaskets() {
		return basketRepository.findAll();
	}

	@Override
	public BasketEntity getBasket(Integer id) {
		Optional<BasketEntity> b = basketRepository.findById(id);

		BasketEntity result = null;
		if (b.isPresent()) {
			result = b.get();
		}
		return result;
	}

	@Override
	public BasketEntity createBasket(BasketEntity basket) {
		return basketRepository.save(basket);
	}

	@Override
	public Security getSecurity(Integer id) {
		Optional<SecurityEntity> s = securityRepository.findById(id);
		
		Security result = null;
		if (s.isPresent()) {
			SecurityEntity se = s.get();
			result = new Security(se);
		}
		return result;
	}

	@Override
	public Security getSecurityForSymbol(String symbol) {
		Iterable<SecurityEntity> seIter = securityRepository.findBySymbol(symbol);

		Security result = null;
		if (seIter != null) {
			Iterator<SecurityEntity> seIterator = seIter.iterator();
			if (seIterator != null && seIterator.hasNext()) {
				// there should only be one security with a given symbol
				SecurityEntity se = seIterator.next();
				if (se != null) {
					result = new Security(se);
				}
			}
		}

		return result;
	}
	
	@Override
	public Iterable<Security> getSecurities() {
		Iterable<SecurityEntity> seIter = securityRepository.findAll();
		return convertSecurityEntityIterableToSortedSecurityIterable(seIter);
	}

	@Override
	public Iterable<Security> getSecuritiesInBasket(Integer basketId) {
		Iterable<SecurityEntity> seIter = securityRepository.findByBasketId(basketId);
		return convertSecurityEntityIterableToSortedSecurityIterable(seIter);
	}

	@Override
	public Security createSecurity(Security security) {
		if (security == null) {
			return null;
		}
		SecurityEntity se = new SecurityEntity(security.getId(), security.getBasketId(), security.getSymbol());
		se = securityRepository.save(se);

		Security result = null;
		if (se != null) {
			result = new Security(se);
		}
		return result;
	}

	private Iterable<Security> convertSecurityEntityIterableToSortedSecurityIterable(Iterable<SecurityEntity> seIter) {
		List<Security> result = new ArrayList<>();
		for (SecurityEntity se : seIter) {
			Security s = new Security(se);
			result.add(s);
		}
		result.sort(securityComparator);
		
		return result;
	}

	@Override
	public Asset getAsset(Integer basketId) {
		Asset result = null;

		BasketEntity b = getBasket(basketId);
		if (b != null) {
			String basketName = b.getName();
			BigDecimal principal = BigDecimal.ZERO;
			BigDecimal totalPrincipal = BigDecimal.ZERO;
			BigDecimal lastValue = BigDecimal.ZERO;
			BigDecimal realizedGain = BigDecimal.ZERO;
			BigDecimal realizedGainYtd = BigDecimal.ZERO;
			BigDecimal ytdShortTermTax = BigDecimal.ZERO;
			BigDecimal ytdLongTermTax = BigDecimal.ZERO;
			BigDecimal ytdShortTermTaxAdj = BigDecimal.ZERO;
			BigDecimal ytdLongTermTaxAdj = BigDecimal.ZERO;
			BigDecimal unrealizedGain = BigDecimal.ZERO;

			Iterable<Position> positionIter = getPositions(basketId);
			List<Position> positions = new ArrayList<>();
			positionIter.forEach(positions::add);
			positions.sort(positionComparator);

			for (Position p : positions) {
				principal = principal.add(p.getPrincipal());
				totalPrincipal = totalPrincipal.add(p.getTotalPrincipal());
				BigDecimal positionValue = p.getLastValue();
				if (positionValue.doubleValue() >= QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
					lastValue = lastValue.add(positionValue);
				}
				realizedGain = realizedGain.add(p.getRealizedGain());
				realizedGainYtd = realizedGainYtd.add(p.getRealizedGainYtd());
				ytdShortTermTax = ytdShortTermTax.add(p.getYtdShortTermTax());
				ytdLongTermTax = ytdLongTermTax.add(p.getYtdLongTermTax());
				ytdShortTermTaxAdj = ytdShortTermTaxAdj.add(p.getYtdShortTermTaxAdj());
				ytdLongTermTaxAdj = ytdLongTermTaxAdj.add(p.getYtdLongTermTaxAdj());
				unrealizedGain = unrealizedGain.add(p.getUnrealizedGain());
			}

			result = new Asset(basketId, basketName, principal, totalPrincipal, lastValue, unrealizedGain, realizedGain,
					realizedGainYtd, ytdShortTermTax, ytdLongTermTax, ytdShortTermTaxAdj, ytdLongTermTaxAdj);
		}

		return result;
	}

	@Override
	public Iterable<Asset> getAssets() {
		List<Asset> result = new ArrayList<>();

		Iterable<BasketEntity> baskets = basketRepository.findAll();
		for (BasketEntity b : baskets) {
			Integer basketId = b.getId();
			Asset a = getAsset(basketId);
			if (a != null) {
				result.add(a);
			}
		}
		result.sort(assetComparator);
		
		computeAssetStatistics(result);

		return result;
	}
	
	// compute asset statistics in context of portfolio
	private void computeAssetStatistics(List<Asset> assets) {
		if (assets == null || assets.size() < 1) {
			return;
		}
		
		BigDecimal valueSum = BigDecimal.ZERO;
		BigDecimal[] currentRatios = new BigDecimal[assets.size()];
		BigDecimal targetRatioSum = BigDecimal.ZERO;
		BigDecimal[] targetRatios = new BigDecimal[assets.size()];
		for (int i = 0; i < targetRatios.length; i++) {
			targetRatios[i] = BigDecimal.ZERO;
		}
		try {
			for (int i = 0; i < assets.size(); i++) {
				valueSum = valueSum.add(assets.get(i).getLastValue());
				currentRatios[i] = BigDecimal.ZERO;
				
				Integer basketId = assets.get(i).getBasketId();
				StringBuffer key = new StringBuffer();
				key.append(QuantumProperties.PROP_PREFIX).append(QuantumProperties.TARGET_RATIO);
				key.append(basketId);
				
				String ratioStr = keyvalService.getKeyvalStr(key.toString());
				if (ratioStr != null) {
					targetRatios[i] = new BigDecimal(ratioStr);
					targetRatioSum = targetRatioSum.add(targetRatios[i]);
				}
			}
			
			// if we have non-zero total value, we can calculate the current ratios
			if (valueSum.abs().doubleValue() >= QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
				for (int i = 0; i < assets.size(); i++) {
					currentRatios[i] = assets.get(i).getLastValue().divide(valueSum,
							QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);
					assets.get(i).setCurrentRatio(currentRatios[i]);
				}
			}
			
			// if we have non-zero target ratios
			if (targetRatioSum.abs().doubleValue() >= QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
				for (int i = 0; i < assets.size(); i++) {
					BigDecimal tr = targetRatios[i].divide(targetRatioSum,
							QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);
					assets.get(i).setTargetRatio(tr);
					BigDecimal deltaValue = currentRatios[i].subtract(tr).multiply(valueSum);
					assets.get(i).setRatioDeltaValue(deltaValue);
				}
				
				// figure out contribution amounts for each asset
				BigDecimal contributionAmount = null;
				StringBuffer key = new StringBuffer();
				key.append(QuantumProperties.PROP_PREFIX).append(QuantumProperties.CONTRIBUTION);				
				String contributionStr = keyvalService.getKeyvalStr(key.toString());
				if (contributionStr != null) {
					contributionAmount = new BigDecimal(contributionStr);
				}
				if (contributionAmount != null && contributionAmount.doubleValue() >= QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
					BigDecimal totalValueWithContribution = valueSum.add(contributionAmount);
					BigDecimal[] targetVsCurrentDelta = new BigDecimal[assets.size()];
					BigDecimal sumPositiveDeltas = BigDecimal.ZERO;
					for (int i = 0; i < assets.size(); i++) {
						targetVsCurrentDelta[i] = targetRatios[i].divide(targetRatioSum, QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP)
								.multiply(totalValueWithContribution).subtract(assets.get(i).getLastValue());
						// contribution is only buying assets, so we're allocating the contribution amount across positive-delta assets
						// zero out the negative deltas (since we're not selling assets)
						if (targetVsCurrentDelta[i].doubleValue() < QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
							targetVsCurrentDelta[i] = BigDecimal.ZERO;
						}
						else {
							sumPositiveDeltas = sumPositiveDeltas.add(targetVsCurrentDelta[i]);
						}
					}
					// calculate the contribution amount for each asset
					for (int i = 0; i < assets.size(); i++) {
						BigDecimal assetContribution = contributionAmount.multiply(targetVsCurrentDelta[i]).divide(sumPositiveDeltas,
								QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);
						if (assetContribution.doubleValue() >= QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
							assets.get(i).setContribution(assetContribution);
						}
					}
				}
			}
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	@Override
	public Asset createAsset(Asset asset) {
		if (asset == null) {
			return null;
		}
		BasketEntity be = new BasketEntity();
		be.setName(asset.getBasketName());
		
		be = basketRepository.save(be);
		Asset result = null;
		if (be != null) {
			result = new Asset(be);
		}
		return result;
	}

	@Override
	public Position getPosition(Integer secId) {
		return getPosition(secId, true);
	}
	
	private Position getPosition(Integer secId, boolean includeTransactions) {
		Security s = getSecurity(secId);
		Position result = Position.EMPTY_POSITION;
	
		if (s != null) {
			// default long-term tax rate is 30% to account for assumed federal 25% and state 5% tax rates
			BigDecimal shortTermTaxRate = new BigDecimal(0.3);
			String taxRateStr = keyvalService.getKeyvalStr(QuantumProperties.PROP_PREFIX + QuantumProperties.SHORT_TERM_TAX_RATE);
			if (taxRateStr != null) {
				try {
					shortTermTaxRate = new BigDecimal(taxRateStr);
				}
				catch(Exception exc) {
					exc.printStackTrace();
				}
			}
			// default long-term tax rate is 20% to account for assumed federal 15% and state 5% tax rates
			BigDecimal longTermTaxRate = new BigDecimal(0.2);
			taxRateStr = keyvalService.getKeyvalStr(QuantumProperties.PROP_PREFIX + QuantumProperties.LONG_TERM_TAX_RATE);
			if (taxRateStr != null) {
				try {
					longTermTaxRate = new BigDecimal(taxRateStr);
				}
				catch(Exception exc) {
					exc.printStackTrace();
				}
			}
			
			String symbol = s.getSymbol();
			BigDecimal tPrice = BigDecimal.ZERO;
			BigDecimal principal = BigDecimal.ZERO;
			BigDecimal totalPrincipal = BigDecimal.ZERO;
			BigDecimal shares = BigDecimal.ZERO;
			BigDecimal sharesLongTerm = BigDecimal.ZERO;
			
			BigDecimal realizedGain = BigDecimal.ZERO;		// total gain up to a given transaction
			BigDecimal realizedGainYtd = BigDecimal.ZERO;
			
			BigDecimal ytdShortTermIncome = BigDecimal.ZERO;
			BigDecimal ytdShortTermTaxAdj = BigDecimal.ZERO;
			BigDecimal ytdLongTermIncome = BigDecimal.ZERO;
			BigDecimal ytdLongTermTaxAdj = BigDecimal.ZERO;

			Date todaysDate = new Date();
			Date oneYearAgo = QuantumDateUtils.addYears(todaysDate, -1);
			int currentYear = QuantumDateUtils.getDateField(todaysDate, Calendar.YEAR);
	
			
			List<Transaction> transactions = transactionService.getTransactionsForSecurity(secId);
			Map<Integer, List<IncomeFragment>> incomeMap = incomeService.getIncomeFragmentsByTransaction(transactions);
			
			for (Transaction t : transactions) {
				Integer tId = t.getId();
				Date tDate = t.getTranDate();
				boolean tranInCurrentYear = (currentYear == QuantumDateUtils.getDateField(tDate, Calendar.YEAR));
				BigDecimal tPrincipalDelta = BigDecimal.ZERO;
				BigDecimal tValue = BigDecimal.ZERO;
				BigDecimal tShares = t.getShares();
				
				if (t.getPrice() == null || tShares == null) {
					System.err.println("AssetServiceImpl::getPosition - ERROR: encountered transaction with null price or shares. Skipping calculations...");
					continue;
				}
				else if (t.getType().equals(QuantumConstants.TRAN_TYPE_BUY)) {
					tPrincipalDelta = t.getPrice().multiply(tShares);
					principal = principal.add(tPrincipalDelta);
					totalPrincipal = totalPrincipal.add(tPrincipalDelta);
					shares = shares.add(tShares);
					if (QuantumDateUtils.beforeDay(tDate, oneYearAgo)) {
						sharesLongTerm = sharesLongTerm.add(tShares);
					}
					
					tValue = t.getPrice().multiply(tShares);
					// tPrice will be used for future dividend transactions as well as this transaction
					tPrice = t.getPrice();
				}
				else if (t.getType().equals(QuantumConstants.TRAN_TYPE_SELL)) {
					tPrincipalDelta = BigDecimal.ZERO;
					BigDecimal transactionProfit = BigDecimal.ZERO;
					
					// calculate principal delta and transaction income (cap gains + wash sale adjustments) from IncomeFragments
					if (incomeMap != null && incomeMap.get(tId) != null) {
						List<IncomeFragment> tranIFList = incomeMap.get(tId);
						for (IncomeFragment incF : tranIFList) {
							transactionProfit = transactionProfit.add(incF.getIncome());
							if (incF instanceof CapitalGainFragment) {
								CapitalGainFragment cgf = (CapitalGainFragment) incF;
								tPrincipalDelta = tPrincipalDelta.subtract(cgf.getCostBasis());
								if (tranInCurrentYear) {
									String term = cgf.getCostBasisTerm();
									if (term != null && term.equals(QuantumConstants.COSTBASIS_LONGTERM)) {
										ytdLongTermIncome = ytdLongTermIncome.add(cgf.getIncome());
										if (cgf.getWashSaleAdj() != null) {
											ytdLongTermTaxAdj = ytdLongTermTaxAdj.add(cgf.getWashSaleAdj());
										}
									}
									else {
										ytdShortTermIncome = ytdShortTermIncome.add(cgf.getIncome());
										if (cgf.getWashSaleAdj() != null) {
											ytdShortTermTaxAdj = ytdShortTermTaxAdj.add(cgf.getWashSaleAdj());
										}
									}
								}
							}
							else {
								System.err.println("AssetServiceImpl::getPosition - ERROR: encountered non-capgain IncomeFragment data for tranId:"+
										tId+" secId:"+secId+". Principal and Income calculations will be incorrect");
							}
						}
					}
					else {
						System.err.println("AssetServiceImpl::getPosition - ERROR: encountered missing IncomeFragment data for tranId:"+
									tId+" secId:"+secId+". Principal and Income calculations will be incorrect");
					}
					
					realizedGain = realizedGain.add(transactionProfit);
					// if transaction is in this year, add to realized gain YTD
					if (tranInCurrentYear) {
						realizedGainYtd = realizedGainYtd.add(transactionProfit);
					}
	
					principal = principal.add(tPrincipalDelta);
					shares = shares.subtract(tShares);
					sharesLongTerm = sharesLongTerm.subtract(tShares);
					if (sharesLongTerm.compareTo(BigDecimal.ZERO) < 0) {
						sharesLongTerm = BigDecimal.ZERO;
					}
					
					tValue = t.getPrice().multiply(tShares);
					tPrice = t.getPrice();
				}
				else if (t.getType().equals(QuantumConstants.TRAN_TYPE_DIVIDEND)) {
					// do not update tPrice - since we want to use tPrice from previous transaction in the DIV case
					BigDecimal dividend = t.getPrice().multiply(tShares);
					realizedGain = realizedGain.add(dividend);
					
					if (tranInCurrentYear) {
						realizedGainYtd = realizedGainYtd.add(dividend);
						
						if (incomeMap != null && incomeMap.get(tId) != null) {
							List<IncomeFragment> tranIFList = incomeMap.get(tId);
							for (IncomeFragment incF : tranIFList) {
								String term = incF.getCostBasisTerm();
								if (term != null && term.equals(QuantumConstants.COSTBASIS_LONGTERM)) {
									ytdLongTermIncome = ytdLongTermIncome.add(incF.getIncome());
								}
								else {
									ytdShortTermIncome = ytdShortTermIncome.add(incF.getIncome());
								}
							}
						}
						else {
							System.err.println("AssetServiceImpl::getPosition - ERROR: encountered missing IncomeFragment data for dividend tranId:"+
										tId+" secId:"+secId+". Income calculations will be incorrect");
						}
					}
					
					tValue = dividend;
				}
				else if (t.getType().equals(QuantumConstants.TRAN_TYPE_SPLIT)) {
					shares = shares.multiply(tShares);
					sharesLongTerm = sharesLongTerm.multiply(tShares);
					tPrice = t.getPrice();
				}
				else if (t.getType().equals(QuantumConstants.TRAN_TYPE_CONVERSION)) {
					// if total shares and long-term shares are equal at this point, replace the long-term shares
					// with new amount; otherwise, use the conversion factor
					if (sharesLongTerm.subtract(shares).abs().doubleValue() < QuantumConstants.THRESHOLD_DECIMAL_EQUALING_ZERO) {
						sharesLongTerm = tShares;
					}
					else {
						sharesLongTerm = sharesLongTerm.multiply(tShares).
								divide(shares, QuantumConstants.NUM_DECIMAL_PLACES_PRECISION, RoundingMode.HALF_UP);
					}
					shares = tShares;
					tPrice = t.getPrice();
				}
				// update total shares/value/realizedGain as of this transaction in Transaction
				t.setTranValue(tValue);
				BigDecimal value = tPrice.multiply(shares);
				t.setTotalShares(shares);
				t.setPrincipal(principal);
				t.setPrincipalDelta(tPrincipalDelta);
				t.setValue(value);
				t.setRealizedGain(realizedGain);
				t.setUnrealizedGain(value.subtract(principal));
			}
	
			BigDecimal lastStockPrice = BigDecimal.ZERO;
			try {
				lastStockPrice = marketDataService.getLastPrice(symbol);
			}
			catch (RuntimeException re) {
				System.err.println("Exception in IEXTrading packet: " + re.getMessage());
			}
			catch (Exception exc) {
				System.err.println("Exception in IEXTrading packet: " + exc.getMessage());
			}
			catch (Error err) {
				//System.err.println("Error in IEXTrading packet: " + err.getMessage());
			}
			
			BigDecimal lastValue = lastStockPrice.multiply(shares);
			BigDecimal unrealizedGain = lastValue.subtract(principal);

			BigDecimal ytdShortTermTax = BigDecimal.ZERO;
			BigDecimal ytdLongTermTax = BigDecimal.ZERO;			
			// if short-term has been defined by the user, calculate the tax/adjustment statistics
			if (shortTermTaxRate != null && longTermTaxRate != null) {
				ytdShortTermTax = ytdShortTermIncome.multiply(shortTermTaxRate);
				ytdLongTermTax = ytdLongTermIncome.multiply(longTermTaxRate);
			}	
			
			
			List<Transaction> transactionList = null;
			if (includeTransactions) {
				transactionList = transactions;
			}
			
			result = new Position(secId, symbol, principal, totalPrincipal, shares, sharesLongTerm, unrealizedGain,
					realizedGain, realizedGainYtd, ytdShortTermTax, ytdLongTermTax, ytdShortTermTaxAdj,
					ytdLongTermTaxAdj, lastStockPrice, lastValue, transactionList);
		
		}
	
		return result;
	}

	@Override
	public List<Position> getPositions() {
		return getPositions(false);
	}
	
	@Override
	public List<Position> getPositions(boolean includeTransactions) {
		List<Position> result = new ArrayList<>();

		Iterable<SecurityEntity> securities = securityRepository.findAll();
		for (SecurityEntity s : securities) {
			Integer secId = s.getId();
			Position p = getPosition(secId, includeTransactions);
			if (p != Position.EMPTY_POSITION) {
				result.add(p);
			}
		}

		result.sort(positionComparator);
		return result;
	}

	@Override
	public List<Position> getPositions(Integer basketId) {
		List<Position> result = new ArrayList<>();

		Iterable<SecurityEntity> securities = securityRepository.findByBasketId(basketId);
		for (SecurityEntity s : securities) {
			Integer secId = s.getId();
			Position p = getPosition(secId, false);
			if (p != Position.EMPTY_POSITION) {
				result.add(p);
			}
		}

		result.sort(positionComparator);
		return result;
	}

}
