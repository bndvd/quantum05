package bdn.quantum.model.util;

import java.util.Comparator;

import org.springframework.stereotype.Service;

import bdn.quantum.model.CapitalGainFragment;
import bdn.quantum.model.IncomeFragment;

@Service("incomeFragmentComparator")
public class IncomeFragmentComparator implements Comparator<IncomeFragment> {

	@Override
	public int compare(IncomeFragment if1, IncomeFragment if2) {
		if (if1 != null && if2 != null) {
			if (if1 instanceof CapitalGainFragment && if2 instanceof CapitalGainFragment) {
				// both have disposal date
				if (if1.getIncomeDate() != null && if2.getIncomeDate() != null) {
					if (if1.getIncomeDate().before(if2.getIncomeDate())) {
						return -1;
					}
					if (if1.getIncomeDate().after(if2.getIncomeDate())) {
						return 1;
					}
				}
				// only left one has disposal date
				else if (if1.getIncomeDate() != null) {
					return -1;
				}
				// only right one has disposal date
				else if (if2.getIncomeDate() != null) {
					return 1;
				}
				// neither has a disposal date (use acquisition dates)
				else {
					CapitalGainFragment cgf1 = (CapitalGainFragment) if1;
					CapitalGainFragment cgf2 = (CapitalGainFragment) if2;
					if (cgf1.getAcqDate() != null && cgf2.getAcqDate() != null) {
						return cgf1.getAcqDate().compareTo(cgf2.getAcqDate());
					}
				}
			}
			else {
				if (if1.getIncomeDate() != null && if2.getIncomeDate() != null) {
					if (if1.getIncomeDate().before(if2.getIncomeDate())) {
						return -1;
					}
					if (if1.getIncomeDate().after(if2.getIncomeDate())) {
						return 1;
					}
				}
				else if (if1.getIncomeDate() != null) {
					return -1;
				}
				else if (if2.getIncomeDate() != null) {
					return 1;
				}
			}
		}

		return 0;
	}

}
