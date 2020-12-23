package bdn.quantum.model.util;

import java.util.Comparator;

import org.springframework.stereotype.Service;

import bdn.quantum.model.Security;

@Service("securityComparator")
public class SecurityComparator implements Comparator<Security> {

	@Override
	public int compare(Security s1, Security s2) {
		if (s1 != null && s2 != null) {
			return s1.getSymbol().compareToIgnoreCase(s2.getSymbol());
		}

		return 0;
	}

}
