package bdn.quantum.service.iex;

import java.util.Comparator;

import org.springframework.stereotype.Service;

import bdn.quantum.model.iex.IEXChart;

@Service("iexChartComparator")
public class IEXChartComparator implements Comparator<IEXChart> {

	@Override
	public int compare(IEXChart c1, IEXChart c2) {
		if (c1 != null && c2!= null) {
			return c1.getDate().compareTo(c2.getDate());
		}

		return 0;
	}
}
