package bdn.quantum.service.iex;

import java.util.Comparator;

import org.springframework.stereotype.Service;

import bdn.quantum.model.iex.IEXTradeDay;

@Service("iexTradDayComparator")
public class IEXTradeDayComparator implements Comparator<IEXTradeDay> {

	@Override
	public int compare(IEXTradeDay d1, IEXTradeDay d2) {
		if (d1 != null && d2!= null) {
			return d1.getDate().compareTo(d2.getDate());
		}

		return 0;
	}
}
