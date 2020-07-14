package com.gametac.utils;

import java.util.Calendar;
import java.util.TimeZone;

public class DateUtils {
	
	public static void main(String[] args) {
		getLastWeekLimitInMillis();
	}
	
	public static long ONE_MINUTE_IN_MILLIS = 1000 * 60;
	public static long ONE_HOUR_IN_MILLIS = ONE_MINUTE_IN_MILLIS * 60;
	public static long ONE_DAY_IN_MILLIS = ONE_HOUR_IN_MILLIS * 24;
	
	public static long getLastWeekLimitInMillis() {
		Calendar cal = getCalendar();
		int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		cal.add(Calendar.DAY_OF_WEEK, -(currentDayOfWeek - 1));
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
	
	public static long getLastMonthLimitInMillis() {
		Calendar cal = getCalendar();
		cal.set(Calendar.DAY_OF_MONTH, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
	
	public static long getLastMonths(int months) {
		long lastMonthLimitInMillis = getLastMonthLimitInMillis();
		Calendar cal = getCalendar();
		cal.setTimeInMillis(lastMonthLimitInMillis);
		cal.add(Calendar.MONTH, -months);
		return cal.getTimeInMillis();
	}
	
	public static long getLastSemesterInTimeMillis() {
		return getLastMonths(6);
	}
	
	public static long getLastYearInTimeMillis() {
		return getLastMonths(12);
	}
	
	public static TimeZone getTimeZone() {
		return TimeZone.getTimeZone("EST");
	}
	
	public static Calendar getCalendar() {
		TimeZone timeZone = getTimeZone();
		Calendar cal = Calendar.getInstance(timeZone);
		return cal;		
	}
}