package bdn.quantum.model.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class ModelUtils {

	public static final int CALC_EARLIEST_DATE = 1;
	public static final int CALC_LATEST_DATE = 2;
	
	private static final DateTimeFormatter CHART_LOCALDATE_DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateFormat CHART_DATE_DF = new SimpleDateFormat("yyyy-MM-dd");

	
	public static LocalDate stringToLocalDate(String dateStr) {
		LocalDate result = null;
		try {
			result = LocalDate.parse(dateStr, CHART_LOCALDATE_DTF);
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return result;
	}
	
	public static String localDateToString(LocalDate ld) {
		String result = null;
		if (ld != null) {
			result = ld.format(CHART_LOCALDATE_DTF);
		}
		return result;
	}

	public static Date stringToDate(String dateStr) {
		Date result = null;
		try {
			result = CHART_DATE_DF.parse(dateStr);
		}
		catch (Exception exc) {
			System.err.println(exc.getMessage());
		}
		return result;
	}
	
	public static String dateToString(Date d) {
		String result = null;
		if (d != null) {
			result = CHART_DATE_DF.format(d);
		}
		return result;
	}

	public static String getMostRecentDateStr(Iterable<String> dateStrIter) {
		String result = null;
		
		if (dateStrIter != null) {
			for (String d : dateStrIter) {
				if (result == null) {
					result = d;
				}
				else {
					result = (d.compareTo(result) > 0) ? d : result;
				}
			}
		}
		return result;
	}
	
	public static List<String> getDateStringsFromStartDateStr(String startDateStr){
		List<String> result = new ArrayList<>();
		LocalDate startDate = ModelUtils.stringToLocalDate(startDateStr);
		LocalDate todaysDate = LocalDate.now();
		int diffDays = Period.between(startDate, todaysDate).getDays();
		
		if (diffDays > 0) {
			Stream<LocalDate> ldStream = startDate.datesUntil(todaysDate);
			Iterator<LocalDate> ldStreamIter = ldStream.iterator();
			ldStreamIter.next();
			while (ldStreamIter.hasNext()) {
				result.add(localDateToString(ldStreamIter.next()));
			}
			result.add(localDateToString(todaysDate));
		}

		return result;
	}

	public static Date getCalculatedDate(List<Date> dateList, int calcType) {
		Date result = null;
		if (dateList != null && dateList.size() > 0) {
			result = dateList.get(0);
			for (int i = 1; i < dateList.size(); i++) {
				Date nextDate = dateList.get(i);
				switch (calcType) {
				case CALC_EARLIEST_DATE:
					if (nextDate.before(result)) {
						result = nextDate; 
					}
					break;
				case CALC_LATEST_DATE:
					if (nextDate.after(result)) {
						result = nextDate; 
					}
					break;
				}
			}
		}
		return result;
	}
}
