/*
 * Copyright(c) 2022 Microstran Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microstran.core.util.helper;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * 
 * Static helper class to expose time handling routines
 * 
 * @author stranm
 * @version $Id: DateHelper.java,v 1.1 2016/11/14 18:24:25 stranm Exp $
 *
 */
public class DateHelper
{
	public final static long	SECOND_MILLIS	= 1000;
	public final static long	MINUTE_MILLIS	= SECOND_MILLIS * 60;
	public final static long	HOUR_MILLIS		= MINUTE_MILLIS * 60;
	public final static long	DAY_MILLIS		= HOUR_MILLIS * 24;
	public final static long	YEAR_MILLIS		= DAY_MILLIS * 365;

	/** Suppress constructor */
	private DateHelper()
	{
		return;
	}

	/**
	 * @return
	 */
	public static int getWeekOfYear()
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		return cal.get(Calendar.WEEK_OF_YEAR);
	}

	/**
	 * Is it...monday?
	 * 
	 * @return
	 */
	public static boolean isMonday()
	{
		return (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY);
	}

	/**
	 * Per Holly the week of the year needs to begin on Monday
	 * 
	 * @return
	 */
	public static int getMondayStartWeekOfYear()
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == Calendar.SUNDAY)
			weekOfYear--;

		return weekOfYear;
	}

	/**
	 * Per Holly the week of the year needs to begin on Monday
	 * 
	 * @return
	 */
	public static int getMondayStartWeekOfYear(Date weekDate)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(weekDate);
		int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == Calendar.SUNDAY)
			weekOfYear--;

		return weekOfYear;
	}

	public static String getMondayStartOfWeekDateStr(Date weekDate)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(weekDate);
		cal.set(Calendar.DAY_OF_WEEK, 1);

		String year = String.valueOf(cal.get(Calendar.YEAR));
		String month = String.valueOf((cal.get(Calendar.MONTH) + 1));
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH) + 1);

		return (year + "-" + month + "-" + day);
	}

	public static String getSundayEndOfWeekDateStr(Date weekDate)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(weekDate);
		cal.set(Calendar.DAY_OF_WEEK, 7);

		String year = String.valueOf(cal.get(Calendar.YEAR));
		String month = String.valueOf((cal.get(Calendar.MONTH) + 1));
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH) + 1);

		return (year + "-" + month + "-" + day);
	}

	/**
	 * @return
	 */
	public static int getYear()
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		return cal.get(Calendar.YEAR);
	}

	/**
	 * @param date
	 * @return
	 */
	public static int getYear(Date date)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.YEAR);
	}

	/**
	 * @param testDate
	 * @param compareAgainstDate
	 * @return
	 */
	public static boolean isDateEarlier(Date testDate, Date compareAgainstDate)
	{
		int diff = (int) ((testDate.getTime() / DAY_MILLIS) - (compareAgainstDate.getTime() / DAY_MILLIS));
		return diff < 0;
	}

	/**
	 * @param testDate
	 * @param compareAgainstDate
	 * @return
	 */
	public static boolean isDateLater(Date testDate, Date compareAgainstDate)
	{
		int diff = (int) ((testDate.getTime() / DAY_MILLIS) - (compareAgainstDate.getTime() / DAY_MILLIS));
		return diff > 0;
	}

	/**
	 * @param date
	 * @return
	 */
	public static boolean isToday(Date date)
	{
		return isSameDay(date, Calendar.getInstance().getTime());
	}

	/**
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static boolean isSameDay(Date date1, Date date2)
	{
		if (date1 == null || date2 == null)
		{
			throw new IllegalArgumentException("The dates must not be null");
		}
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		return isSameDay(cal1, cal2);
	}

	/**
	 * <p>
	 * Checks if two calendars represent the same day ignoring time.
	 * </p>
	 * 
	 * @param cal1
	 *            the first calendar, not altered, not null
	 * @param cal2
	 *            the second calendar, not altered, not null
	 * @return true if they represent the same day
	 * @throws IllegalArgumentException
	 *             if either calendar is <code>null</code>
	 */
	public static boolean isSameDay(Calendar cal1, Calendar cal2)
	{
		if (cal1 == null || cal2 == null)
		{
			throw new IllegalArgumentException("The dates cannot be null");
		}
		return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
	}

	/**
	 * @param inputDate
	 * @param numDaysToAddSubtract
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Date addSubtractDays(Date inputDate, int numDaysToAddSubtract) throws Exception
	{
		GregorianCalendar cal = getGregorianCalendarFromDate(inputDate);
		cal.add(Calendar.DAY_OF_MONTH, numDaysToAddSubtract); // negative
																// numbers
																// subtract here
		return getDateFromGregorianCalendar(cal);
	}

	/**
	 * @param earlierDate
	 * @param laterDate
	 * @return
	 */
	public static int secondsDiff(Date earlierDate, Date laterDate)
	{
		if (earlierDate == null || laterDate == null)
			return 0;

		return (int) ((laterDate.getTime() / SECOND_MILLIS) - (earlierDate.getTime() / SECOND_MILLIS));
	}

	/**
	 * @param earlierDate
	 * @param laterDate
	 * @return
	 */
	public static int minutesDiff(Date earlierDate, Date laterDate)
	{
		if (earlierDate == null || laterDate == null)
			return 0;

		return (int) ((laterDate.getTime() / MINUTE_MILLIS) - (earlierDate.getTime() / MINUTE_MILLIS));
	}

	/**
	 * @param earlierDate
	 * @param laterDate
	 * @return
	 */
	public static int hoursDiff(Date earlierDate, Date laterDate)
	{
		if (earlierDate == null || laterDate == null)
			return 0;

		return (int) ((laterDate.getTime() / HOUR_MILLIS) - (earlierDate.getTime() / HOUR_MILLIS));
	}

	/**
	 * @param earlierDate
	 * @param laterDate
	 * @return
	 */
	public static int daysDiff(Date earlierDate, Date laterDate)
	{
		if (earlierDate == null || laterDate == null)
			return 0;

		return (int) ((laterDate.getTime() / DAY_MILLIS) - (earlierDate.getTime() / DAY_MILLIS));
	}

	public static java.sql.Timestamp getNow()
	{
		return new java.sql.Timestamp(new java.util.Date().getTime());
	}

	public static java.sql.Timestamp getNow(java.sql.Timestamp Date)
	{
		return getNow(Date, 0);
	}

	public static java.sql.Date getNow(java.sql.Date Date)
	{
		return getNow(Date, 0);
	}

	public static java.sql.Timestamp getNow(java.sql.Timestamp Date, long milliSecToAdd)
	{
		// lineOut(milliSecToAdd);
		return new java.sql.Timestamp(new java.util.Date().getTime() + milliSecToAdd);
	}

	/**
	 * @param Date
	 * @param milliSecToAdd
	 * @return
	 */
	public static java.sql.Date getNow(java.sql.Date Date, long milliSecToAdd)
	{
		return new java.sql.Date(new java.util.Date().getTime() + milliSecToAdd);
	}

	/**
	 * @return
	 */
	public static java.sql.Date getDay()
	{
		return new java.sql.Date(new java.util.Date().getTime());
	}

	/**
	 * @param Date
	 * @return
	 */
	public static java.sql.Timestamp getDate(java.sql.Timestamp Date)
	{
		return new java.sql.Timestamp(new java.util.Date().getTime());
	}

	/**
	 * @param Date
	 * @return
	 */
	public static java.sql.Date getDate(java.sql.Date Date)
	{
		return new java.sql.Date(new java.util.Date().getTime());
	}

	/**
	 * Method GetGregorianCalendarFromDate Convert Date to Gregorian Calendar.
	 * 
	 * @param date
	 * @return GregorianCalendar
	 */
	//
	public static GregorianCalendar getGregorianCalendarFromDate(Date date) throws Exception
	{
		if (date == null)
			return (null);

		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
		String strDate = formatter.format(date);
		GregorianCalendar cal = getGregorianCalendarFromString(strDate);
		return (cal);
	}

	/**
	 * Method GetDateFromGregorianCalendar. Convert Gregorian Calendar to Date
	 * 
	 * @param cal
	 * @return Date
	 */
	public static Date getDateFromGregorianCalendar(GregorianCalendar cal) throws Exception
	{
		if (cal == null)
			return (null);

		// !!!!NOTE Add 1 here, gregorian calendar is zero based
		// but the DATE IS NOT!
		String strDate = (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH) + "-" + cal.get(Calendar.YEAR);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		ParsePosition pos = new ParsePosition(0);
		Date date = dateFormat.parse(strDate, pos);
		return (date);
	}

	/**
	 * @return
	 */
	public static Date getLastDayOfMonth()
	{
		Calendar calendar = Calendar.getInstance();
		int lastDate = calendar.getActualMaximum(Calendar.DATE);
		calendar.set(Calendar.DATE, lastDate);
		return calendar.getTime();
	}

	/**
	 * @return
	 */
	public static Date getFirstDayOfMonth()
	{
		Calendar calendar = Calendar.getInstance();
		int firstDate = calendar.getActualMinimum(Calendar.DATE);
		calendar.set(Calendar.DATE, firstDate);
		return calendar.getTime();
	}

	/**
	 * start date can be null in which case it will use today as benchmark
	 * NOTE!!!! this will return MONDAY as first day of week not the java
	 * standard of sunday...
	 * 
	 * @param startDate
	 * @return
	 */
	public static Date getFirstDayOfWeek(Date startDate)
	{
		if (startDate == null)
			startDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		int currentDOW = cal.get(Calendar.DAY_OF_WEEK);
		cal.add(Calendar.DAY_OF_YEAR, (currentDOW * -1) + 2);
		return cal.getTime();
	}

	/**
	 * @return
	 */
	public static Date getFirstDayOfCurrentQuarter()
	{
		Calendar calendar = Calendar.getInstance();
		int currentMonth = calendar.get(Calendar.MONTH);
		String currentYear = String.valueOf(calendar.get(Calendar.YEAR));
		int quarter = 1;
		if ((currentMonth > 2) && (currentMonth < 6))
			quarter = 2;
		else if ((currentMonth > 5) && (currentMonth < 9))
			quarter = 3;
		else if ((currentMonth > 8) && (currentMonth <= 11))
			quarter = 4;

		switch (quarter)
		{
			case 1:
			{
				return getDateFromString(currentYear + "-01-01");
			}
			case 2:
			{
				return getDateFromString(currentYear + "-04-01");
			}
			case 3:
			{
				return getDateFromString(currentYear + "-07-01");
			}
			case 4:
			{
				return getDateFromString(currentYear + "-10-01");
			}
		}
		return null;
	}

	/**
	 * @return
	 */
	public static Date getlastDayOfCurrentQuarter()
	{
		Calendar calendar = Calendar.getInstance();
		int currentMonth = calendar.get(Calendar.MONTH);
		String currentYear = String.valueOf(calendar.get(Calendar.YEAR));
		int quarter = 1;
		if ((currentMonth > 2) && (currentMonth < 6))
			quarter = 2;
		else if ((currentMonth > 5) && (currentMonth < 9))
			quarter = 3;
		else if ((currentMonth > 8) && (currentMonth <= 11))
			quarter = 4;

		switch (quarter)
		{
			case 1:
			{
				return getDateFromString(currentYear + "-03-31");
			}
			case 2:
			{
				return getDateFromString(currentYear + "-06-30");
			}
			case 3:
			{
				return getDateFromString(currentYear + "-09-30");
			}
			case 4:
			{
				return getDateFromString(currentYear + "-12-31");
			}
		}
		return null;
	}

	/**
	 * Method getShortFormatMonthYearFromDate.
	 * 
	 * @param date
	 * @return String
	 */
	public static String getShortFormatMonthYearFromDate(Date date)
	{
		if (date == null)
			return (null);
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd");
		String strDate = formatter.format(date);
		return (strDate);
	}

	/**
	 * Method GetStringFromDate. convert String to Date
	 * 
	 * @param date
	 * @return String
	 */
	public static String getStringFromDate(Date date)
	{
		if (date == null)
			return (null);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = formatter.format(date);
		return (strDate);
	}

	/**
	 * Method GetDateFromString. convert Date to String
	 * 
	 * @param strDate
	 * @return Date
	 */
	public static Date getDateFromString(String strDate)
	{
		if (strDate == null)
			return (null);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition pos = new ParsePosition(0);
		Date date = dateFormat.parse(strDate, pos);
		return (date);
	}

	/**
	 * Method FormatHTMLUseableDate. Convert Gregorian Calendar to HTML Usable
	 * string
	 * 
	 * @param cal
	 * @return String
	 */
	public static String formatHTMLUseableDate(GregorianCalendar cal)
	{
		if (cal == null)
			return (null);

		String year = String.valueOf(cal.get(Calendar.YEAR));

		// The calendar class is an array and zero based,
		// when we want to display we need to add 1
		String month = String.valueOf((cal.get(Calendar.MONTH) + 1));
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));

		String strHTML = month + "/" + day + "/" + year;

		return (strHTML);
	}

	/**
	 * Method formatHTMLUseableDate.
	 * 
	 * @param date
	 * @return String
	 */
	public static String formatHTMLUseableDate(Date date) throws Exception
	{
		GregorianCalendar cal = getGregorianCalendarFromDate(date);
		if (cal == null)
			return (null);

		String year = String.valueOf(cal.get(Calendar.YEAR));
		// The calendar class is an array and zero based,
		// when we want to display we need to add 1
		String month = String.valueOf((cal.get(Calendar.MONTH) + 1));
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));

		String strHTML = month + "/" + day + "/" + year;

		return (strHTML);
	}

	/**
	 * Method getTodaysDateLocalized.
	 * 
	 * @return Date
	 * @throws Exception
	 */
	public static Date getTodaysDateLocalized() throws Exception
	{
		Date curDate = new Date();
		TimeZone zone = (TimeZone) SimpleTimeZone.getDefault();
		GregorianCalendar cal = new GregorianCalendar(zone);
		cal.setTime(curDate);
		return (DateHelper.getDateFromGregorianCalendar(cal));
	}

	/**
	 * Method getMonthOffsetDateLocalized -get a day offset from today.
	 * 
	 * @return Date
	 * @throws Exception
	 */
	public static Date getMonthOffsetDateLocalized(int monthOffset, boolean beginningOfMonth) throws Exception
	{
		Date curDate = new Date();
		TimeZone zone = (TimeZone) SimpleTimeZone.getDefault();
		GregorianCalendar cal = new GregorianCalendar(zone);
		cal.setTime(curDate);

		// add (or subtract) the month, allow to cross year boundaries
		cal.add(Calendar.MONTH, monthOffset);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		if (beginningOfMonth == true)
			cal.set(year, month, 1);
		else cal.set(year, month, cal.getActualMaximum(Calendar.DAY_OF_MONTH));

		return (DateHelper.getDateFromGregorianCalendar(cal));
	}

	/**
	 * Method setEndDateMonth.
	 * 
	 * @param newStartDate
	 * @return Date
	 */
	public static Date setEndDateMonth(Date startDate) throws Exception
	{
		GregorianCalendar cal = getGregorianCalendarFromDate(startDate);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		cal.set(year, month, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		return (DateHelper.getDateFromGregorianCalendar(cal));
	}

	/**
	 * Method getNumberOfMonths.
	 * 
	 * @param startDate
	 * @param endDate
	 * @return int
	 * @throws Exception
	 */
	public static int getNumberOfMonths(Date startDate, Date endDate) throws Exception
	{
		int numMonths = 1;
		GregorianCalendar startCal = getGregorianCalendarFromDate(startDate);
		GregorianCalendar endCal = getGregorianCalendarFromDate(endDate);

		int year1 = startCal.get(Calendar.YEAR);
		int month1 = startCal.get(Calendar.MONTH);
		int year2 = endCal.get(Calendar.YEAR);
		int month2 = endCal.get(Calendar.MONTH);

		// inclusive so always add 1;
		if (month2 < month1) // cross year boundaries?
		{
			if (year1 < year2)
				numMonths = (((12 - month1) + month2) + 1);
			else throw new Exception("Time formatting error, start is after end");
		}
		else numMonths = ((month2 - month1) + 1);

		return (numMonths);
	}

	/**
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws Exception
	 */
	public static int getNumberOfDays(Date startDate, Date endDate) throws Exception
	{
		int numDays = 1;
		GregorianCalendar startCal = getGregorianCalendarFromDate(startDate);
		GregorianCalendar endCal = getGregorianCalendarFromDate(endDate);

		int year1 = startCal.get(Calendar.YEAR);
		int day1 = startCal.get(Calendar.DAY_OF_YEAR);
		int year2 = endCal.get(Calendar.YEAR);
		int day2 = endCal.get(Calendar.DAY_OF_YEAR);

		if (day2 < day1) // cross year boundaries?
		{
			if (year1 < year2)
				numDays = (((365 - day1) + day2));
			else throw new Exception("Time formatting error, start is after end");
		}
		else numDays = ((day2 - day1) + 1);

		return (numDays);
	}

	/**
	 * Method addMonth.
	 * 
	 * @param startDate
	 * @return Date
	 * @throws Exception
	 */
	public static Date addMonth(Date startDate) throws Exception
	{
		GregorianCalendar cal = getGregorianCalendarFromDate(startDate);
		cal.add(Calendar.MONTH, 1);
		return (DateHelper.getDateFromGregorianCalendar(cal));
	}

	/**
	 * Method getDateFromCalendarString. Strings come from the calendar control
	 * as "Date(1999,11,25)
	 * 
	 * @param calString
	 * @return Date
	 * @throws Exception
	 */
	public static Date getDateFromCalendarString(String calString) throws Exception
	{
		if (calString == null)
			return (null);

		Date newDate = null;
		int dateStart = calString.indexOf("(");
		int indexOfYear = calString.indexOf("/");
		if (dateStart != -1)
		{
			int dateEnd = calString.indexOf(")");
			String tokenString = calString.substring(dateStart + 1, dateEnd);
			StringTokenizer st = new StringTokenizer(tokenString, ",");
			String year = st.nextToken();
			String month = st.nextToken();
			String day = st.nextToken();
			newDate = getDateFromString(year + "-" + month + "-" + day);
		}
		else if ((indexOfYear < 3) && (indexOfYear != -1)) // month-day-year
															// format, convert
		{
			StringTokenizer st = new StringTokenizer(calString, "/");
			String month = st.nextToken();
			String day = st.nextToken();
			String year = st.nextToken();
			newDate = getDateFromString(year + "-" + month + "-" + day);
		}
		else
		{
			newDate = getDateFromString(calString);
		}
		return (newDate);

	}

	/**
	 * Method GetGregorianCalendarFromString.
	 * 
	 * @param strDate
	 * @return GregorianCalendar
	 */
	private static GregorianCalendar getGregorianCalendarFromString(String strDate) throws Exception
	{
		GregorianCalendar cal = new GregorianCalendar();
		TimeZone zone = (TimeZone) SimpleTimeZone.getDefault();
		cal.setTimeZone(zone);

		StringTokenizer st = new StringTokenizer(strDate, "-");

		// NOTE!!! Gregorian Calendar is Zero Based,
		// must subtract one on the months
		int nMonth = (Integer.parseInt(st.nextToken()) - 1);
		int nDay = Integer.parseInt(st.nextToken());
		int nYear = Integer.parseInt(st.nextToken());

		// if has
		if (st.hasMoreTokens())
		{
			int nHour = Integer.parseInt(st.nextToken());
			int nMinute = Integer.parseInt(st.nextToken());
			int nSecond = Integer.parseInt(st.nextToken());
			cal.set(nYear, nMonth, nDay, nHour, nMinute, nSecond);
		}
		else
		{
			cal.set(nYear, nMonth, nDay);
		}

		return (cal);
	}

	/**
	 * @param startDate
	 * @param period
	 * @param amount
	 * @return
	 */
	public static java.sql.Time rollTime(java.util.Date startDate, int period, int amount)
	{
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(startDate);
		gc.add(period, amount);
		return new java.sql.Time(gc.getTime().getTime());
	}

	/**
	 * @param startDate
	 * @param period
	 * @param amount
	 * @return
	 */
	public static java.util.Date rollDateTime(java.util.Date startDate, int period, int amount)
	{
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(startDate);
		gc.add(period, amount);
		return new java.util.Date(gc.getTime().getTime());
	}

	/**
	 * @param startDate
	 * @param period
	 * @param amount
	 * @return
	 */
	public static java.sql.Date rollDate(java.util.Date startDate, int period, int amount)
	{
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(startDate);
		gc.add(period, amount);
		return new java.sql.Date(gc.getTime().getTime());
	}

}