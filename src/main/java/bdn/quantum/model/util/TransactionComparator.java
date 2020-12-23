package bdn.quantum.model.util;

import java.util.Comparator;

import org.springframework.stereotype.Service;

import bdn.quantum.model.AbstractTransaction;

@Service("transactionComparator")
public class TransactionComparator implements Comparator<AbstractTransaction> {

	@Override
	public int compare(AbstractTransaction t1, AbstractTransaction t2) {
		if (t1 != null && t2!= null) {
			if (t1.getTranDate().before(t2.getTranDate())) {
				return -1;
			}
			if (t1.getTranDate().after(t2.getTranDate())) {
				return 1;
			}
		}

		return 0;
	}

}
