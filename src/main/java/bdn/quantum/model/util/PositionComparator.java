package bdn.quantum.model.util;

import java.util.Comparator;

import org.springframework.stereotype.Service;

import bdn.quantum.model.Position;

@Service("positionComparator")
public class PositionComparator implements Comparator<Position> {

	@Override
	public int compare(Position p1, Position p2) {
		if (p1 != null && p2 != null) {
			return p1.getSymbol().compareToIgnoreCase(p2.getSymbol());
		}

		return 0;
	}

}