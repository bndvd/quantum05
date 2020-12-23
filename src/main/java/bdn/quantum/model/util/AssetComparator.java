package bdn.quantum.model.util;

import java.util.Comparator;

import org.springframework.stereotype.Service;

import bdn.quantum.model.Asset;

@Service("assetComparator")
public class AssetComparator implements Comparator<Asset> {

	@Override
	public int compare(Asset a1, Asset a2) {
		if (a1 != null && a2 != null) {
			return a1.getBasketName().compareToIgnoreCase(a2.getBasketName());
		}

		return 0;
	}

}
