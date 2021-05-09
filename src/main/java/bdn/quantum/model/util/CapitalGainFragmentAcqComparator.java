package bdn.quantum.model.util;

import java.util.Comparator;

import org.springframework.stereotype.Service;

import bdn.quantum.model.CapitalGainFragment;

@Service("capitalGainFragmentAcqComparator")
public class CapitalGainFragmentAcqComparator implements Comparator<CapitalGainFragment> {

	@Override
	public int compare(CapitalGainFragment cgf1, CapitalGainFragment cgf2) {
		if (cgf1 != null && cgf2 != null && cgf1.getAcqDate() != null && cgf2.getAcqDate() != null) {
			if (cgf1.getAcqDate().before(cgf2.getAcqDate())) {
				return -1;
			}
			if (cgf1.getAcqDate().after(cgf2.getAcqDate())) {
				return 1;
			}
		}

		return 0;
	}

}
