package bdn.quantum.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

public class QuantumDateUtils {

	public static Date asDate(LocalDate localDate) {
		return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}

	public static LocalDate asLocalDate(Date date) {
		return LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}
	
	public static Date addDays(Date date, int days) {
		return DateUtils.addDays(date, days);
	}
	
	public static Date addYears(Date date, int years) {
		return DateUtils.addYears(date, years);
	}
	
	public static boolean beforeDay(Date lhs, Date rhs) {
		if (lhs == null || rhs == null) {
			return false;
		}
		Date lhsDay = DateUtils.truncate(lhs, Calendar.DATE);
		Date rhsDay = DateUtils.truncate(rhs, Calendar.DATE);
		return lhsDay.before(rhsDay);
	}
	
	public static boolean afterDay(Date lhs, Date rhs) {
		if (lhs == null || rhs == null) {
			return false;
		}
		Date lhsDay = DateUtils.truncate(lhs, Calendar.DATE);
		Date rhsDay = DateUtils.truncate(rhs, Calendar.DATE);
		return lhsDay.after(rhsDay);
	}
	
	public static boolean equalsDay(Date lhs, Date rhs) {
		if (lhs == null || rhs == null) {
			return false;
		}
		Date lhsDay = DateUtils.truncate(lhs, Calendar.DATE);
		Date rhsDay = DateUtils.truncate(rhs, Calendar.DATE);
		return lhsDay.equals(rhsDay);
	}
	
	public static int dayDifference(Date lhs, Date rhs) {
		int result = 0;
		if (lhs != null && rhs != null) {
			LocalDate lhsDay = asLocalDate(DateUtils.truncate(lhs, Calendar.DATE));
			LocalDate rhsDay = asLocalDate(DateUtils.truncate(rhs, Calendar.DATE));
			result = (int) ChronoUnit.DAYS.between(lhsDay, rhsDay);
		}
		return result;
	}
	
	public static int getDateField(Date date, int calendarField) {
		if (date == null) {
			return -1;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(calendarField);
	}
}
