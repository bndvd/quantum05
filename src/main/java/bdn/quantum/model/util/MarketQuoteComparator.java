package bdn.quantum.model.util;

import java.util.Comparator;

import org.springframework.stereotype.Service;

import bdn.quantum.model.MarketQuote;

@Service("marketQuoteComparator")
public class MarketQuoteComparator implements Comparator<MarketQuote> {

	@Override
	public int compare(MarketQuote q1, MarketQuote q2) {
		if (q1 != null && q2!= null) {
			return q1.getMktDate().compareTo(q2.getMktDate());
		}

		return 0;
	}

}
