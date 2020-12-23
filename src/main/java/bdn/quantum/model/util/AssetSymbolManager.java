package bdn.quantum.model.util;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bdn.quantum.model.Asset;
import bdn.quantum.model.Security;
import bdn.quantum.service.AssetService;

@Service("assetSymbolManager")
public class AssetSymbolManager {

	private static HashMap<String, String> fundToStockSymbolMap = new HashMap<>();
	static {
		fundToStockSymbolMap.put("VTSAX", "VTI");
		fundToStockSymbolMap.put("VTSMX", "VTI");
		fundToStockSymbolMap.put("VSIAX", "VBR");
		fundToStockSymbolMap.put("VISVX", "VBR");
	}
	
	public static String getStockSymbol(String fundSymbol) {
		String result = fundSymbol;
		if (fundSymbol != null) {
			String stockSymbol = fundToStockSymbolMap.get(fundSymbol);
			if (stockSymbol != null) {
				result = stockSymbol;
			}
		}
		return result;
	}
	
	
	@Autowired
	private AssetService assetService;
	
	private HashMap<Integer, String> basketIdToSymbolMap = null;
	
	
	public AssetSymbolManager() {
	}
	
	
	public String getSymbolForAsset(Integer basketId) {
		if (basketIdToSymbolMap == null) {
			initBasketIdToSymbolMap();
		}
		
		String result = null;
		if (basketIdToSymbolMap != null) {
			result = basketIdToSymbolMap.get(basketId);
		}
		return result;
	}
	
	
	private void initBasketIdToSymbolMap() {
		basketIdToSymbolMap = new HashMap<>();
		
		Iterable<Asset> assets = assetService.getAssets();
		for (Asset a : assets) {
			Integer basketId = a.getBasketId();
			Iterable<Security> securities = assetService.getSecuritiesInBasket(basketId);
			if (securities != null) {
				for (Security s : securities) {
					if (s != null) {
						String stockSymbol = getStockSymbol(s.getSymbol());
						if (stockSymbol != null) {
							basketIdToSymbolMap.put(basketId, stockSymbol);
							break;
						}
					}
				}
			}
		}
	}
	
}
