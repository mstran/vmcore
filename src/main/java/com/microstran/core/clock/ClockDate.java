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

package com.microstran.core.clock;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import java.util.TimeZone;

import com.microstran.core.event.ApplicationEventServer;
import com.microstran.core.event.clock.WindowChangeEvent;
import com.microstran.core.viewer.SVG_SWTViewerFrame;

/**
 * The class for all clock dates
 *
 * @author Mike Stran
 *
 */
public class ClockDate
{
   
    private int year;
    private int month;
    private int dayOfWeek;
    private int dayOfMonth;
    private int hour;
    private int minute;
    private int second;
    private int fractionalSeconds;
    private int amPm;
    private String timeZone;
    private long time;
    private SVG_SWTViewerFrame frame;
    

	/**
     * basic constructor
     */
    public ClockDate()
    {}
    
    
    /**
     * copy constructor
     * @param date
     */
    public ClockDate(ClockDate date)
    {
        this.year       = date.year;
        this.month		= date.month;
        this.dayOfMonth = date.dayOfMonth;
        this.dayOfWeek  = date.dayOfWeek;
        this.hour		= date.hour;
        this.minute		= date.minute;
        this.second		= date.second;
        this.fractionalSeconds = date.fractionalSeconds;
    	this.amPm		= date.amPm;
        this.timeZone   = date.timeZone;
        this.time	    = date.time;
        this.amPm       = date.amPm;
    }
 
    
	/**
	 * Add a whole Second
	 */

	public void addTime(int timeAmt) {
		switch(timeAmt) {
			case 1000:
			{
				addSecond();
				break;
			}
			case 500:
			{
				addHalfSecond();
				break;
			}
			case 250:
			{
				addQuarterSecond();
				break;
			}
			case 100:
			{
				addTenthSecond();
				break;
			}
		}
	}
	
	public void addSecond()
	{
	    {
	        this.second++;
			if (this.second == 60) {
				this.second = 0;
				this.minute++;
			}
			if (this.minute == 60){
			    this.minute = 0;
				this.hour++;
				if (this.hour == 12){
					this.hour = 0;
					amPm = (amPm == 1) ? 0 : 1;
					if (frame != null) {
						ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.UPDATE_VIEWER_EVENT,frame));
					}
				}
			}
	    }
	}

	public void addHalfSecond()
	{
	    {
	        this.fractionalSeconds++;
	    
			if (this.fractionalSeconds >= 2)
			{
				this.fractionalSeconds = 0;
				this.second++;
				if ((this.second % 30) == 0){
					if (this.second == 60) {
						this.second = 0;
						this.minute++;
					}
					if (this.minute == 60){
						this.minute = 0;
						this.hour++;
						if (this.hour >= 12){
							this.hour = 0;
							amPm = (amPm == 1) ? 0 : 1;
							if (frame != null) {
								ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.UPDATE_VIEWER_EVENT,frame));
							}
						}
					}
				}
			}
	    }
	}
	
	public void addQuarterSecond()
	{
	    {
	        this.fractionalSeconds++;
		    if (this.fractionalSeconds >= 4)
			{
				this.fractionalSeconds = 0;
				this.second++;
				if ((this.second % 30) == 0){
					if (this.second == 60) {
						this.second = 0;
						this.minute++;
					}
					if (this.minute == 60){
						this.minute = 0;
						this.hour++;
						if (this.hour >= 12) {
							this.hour = 0;
							amPm = (amPm == 1) ? 0 : 1;
							if (frame != null) {
								ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.UPDATE_VIEWER_EVENT,frame));
							}
						}
					}
				}
			}
	    }
	}
	
	public void addTenthSecond()
	{
	    {
	        this.fractionalSeconds++;
		    if (this.fractionalSeconds >= 10)
			{
				this.fractionalSeconds = 0;
				this.second++;
				if ((this.second % 30) == 0){
					if (this.second == 60) {
						this.second = 0;
						this.minute++;
					}
					if (this.minute == 60){
						this.minute = 0;
						this.hour++;
						if (this.hour >= 12) {
							this.hour = 0;
							amPm = (amPm == 1) ? 0 : 1;
							if (frame != null) {
								ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.UPDATE_VIEWER_EVENT,frame));
							}
						}
					}
				}
			}
	    }
	}
	
	/**
	 * Configure the time index values to reflect the timestamp
	 */
	public void configureTime(Long time, String zone)
	{
	    this.time = time.longValue();
	    this.timeZone = zone;
	    Date curDate = new Date(time.longValue());
	    getTime(curDate, zone);
	}
	
	/**
	 * Configure the time index values to reflect the timestamp
	 */
	public void configureTime()
	{
	    Date curDate = new Date();
		getTime(curDate,timeZone);
		this.time = curDate.getTime();
	}	
		
	/**
	 * Configure the time index values to reflect the timestamp
	 */
	private void getTime(Date curDate, String zone)
	{
		try
		{
		    SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy-K-m-s-a");
		    TimeZone tmZone = TimeZone.getTimeZone(zone);
		    formatter.setTimeZone(tmZone);
			String strDate = formatter.format(curDate);
	
			StringTokenizer st = new StringTokenizer(strDate, "-");
		    this.month = (Integer.parseInt(st.nextToken()));
		    this.dayOfMonth = Integer.parseInt(st.nextToken());
		    Calendar now = Calendar.getInstance(tmZone);
		    this.dayOfWeek = (now.get(Calendar.DAY_OF_WEEK) - 1);
		    this.year = Integer.parseInt(st.nextToken());
		    this.hour = Integer.parseInt(st.nextToken());
		    this.minute = Integer.parseInt(st.nextToken());
		    this.second = Integer.parseInt(st.nextToken());
		    String sAmPm = st.nextToken();
		    setAmPm(sAmPm.equals("PM") ? GregorianCalendar.PM : GregorianCalendar.AM);
		    }
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

    public void getNonLocalizedDayOfWeekFromDate(Date date)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("MM-F");
		String strDate = formatter.format(date);

		StringTokenizer st = new StringTokenizer(strDate, "-");
	    st.nextToken();
	    this.dayOfWeek = Integer.parseInt(st.nextToken());
    }
    
    /**
     * Gets a date object for use in the dialog box
     * @return
     */
    public Date getDialogBoxDateFromInternalDate()
    {
        String strDate = (year + "-" + month + "-" + dayOfMonth);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        return(dateFormat.parse(strDate, pos));   
    }
    
    /**
     * Method Get a display String From Date.
     * convert String to Date
     * @param date
     * @return String
     */
    public String getDialogBoxDisplayStringFromDate(boolean weekly)
    {
        try
        {
            StringBuffer strDate = new StringBuffer();
	        
            String amPM = (amPm == GregorianCalendar.AM ? "AM" : "PM");
            String strDateFormatter = (year + "-" + month + "-" + dayOfMonth + "-" + hour + "-" + minute + "-" + amPM);
	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-K-m-a");
	        ParsePosition pos = new ParsePosition(0);
	        Date date = dateFormat.parse(strDateFormatter, pos);   
	        if (!weekly)
	        {
		        SimpleDateFormat format = new SimpleDateFormat("EEE. MMM dd, yy @ K:mm a");
		        strDate.append(format.format(date));
	        }
	        else
	        {
		        SimpleDateFormat format = new SimpleDateFormat("EEE. @ K:mm a");
		        strDate.append("Every " + format.format(date));
	        }
	        return(strDate.toString());
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        return(null);
    }
    
    
    
    /**
     * @return Returns the amPm.
     */
    public int getAmPm()
    {
        return amPm;
    }
    /**
     * @param amPm The amPm to set.
     */
    public void setAmPm(int amPm)
    {
        this.amPm = amPm;
    }
    /**
     * @return Returns the dayOfMonth.
     */
    public int getDayOfMonth()
    {
        return dayOfMonth;
    }
    /**
     * @param dayOfMonth The dayOfMonth to set.
     */
    public void setDayOfMonth(int dayOfMonth)
    {
        this.dayOfMonth = dayOfMonth;
    }
    /**
     * @return Returns the dayOfWeek.
     */
    public int getDayOfWeek()
    {
        return dayOfWeek;
    }
    /**
     * @param dayOfWeek The dayOfWeek to set.
     */
    public void setDayOfWeek(int dayOfWeek)
    {
        this.dayOfWeek = dayOfWeek;
    }
    
    /**
     * @return Returns the hour.
     */
    public int getHour()
    {
        return hour;
    }
    /**
     * @param hour The hour to set.
     */
    public void setHour(int hour)
    {
        this.hour = hour;
    }
    /**
     * @return Returns the minute.
     */
    public int getMinute()
    {
        return minute;
    }
    /**
     * @param minute The minute to set.
     */
    public void setMinute(int minute)
    {
        this.minute = minute;
    }
    /**
     * @return Returns the month.
     */
    public int getMonth()
    {
        return month;
    }
    /**
     * @param month The month to set.
     */
    public void setMonth(int month)
    {
        this.month = month;
    }
    /**
     * @return Returns the second.
     */
    public int getSecond()
    {
        return second;
    }
    /**
     * @param second The second to set.
     */
    public void setSecond(int second)
    {
        this.second = second;
    }
    /**
     * @return Returns the time.
     */
    public long getTime()
    {
        return time;
    }
    
    public long updateAndGetCurrentTimeForDisplay() {
    	configureTime();
    	return time;
    }
    
    /**
     * @param time The time to set.
     */
    public void setTime(long time)
    {
        this.time = time;
    }
    /**
     * @return Returns the year.
     */
    public int getYear()
    {
        return year;
    }
    /**
     * @param year The year to set.
     */
    public void setYear(int year)
    {
        this.year = year;
    }
    /**
     * @return Returns the timeZone.
     */
    public String getTimeZone()
    {
        return timeZone;
    }
    /**
     * @param timeZone The timeZone to set.
     */
    public void setTimeZone(String timeZone)
    {
        this.timeZone = timeZone;
    }
    /**
     * @return Returns the fractionalSeconds.
     */
    public int getFractionalSeconds()
    {
        return fractionalSeconds;
    }
    /**
     * @param fractionalSeconds The fractionalSeconds to set.
     */
    public void setFractionalSeconds(int fractionalSeconds)
    {
        this.fractionalSeconds = fractionalSeconds;
    }
   
    /*************************************************************************************
     * 
     * Static Helpers
     *
     ************************************************************************************/
    
    /**
     * Method isDateEarlier .
     * @param testDate
     * @param compareAgainstDate
     * @return boolean true if testDate is earlier
     */
    public static boolean isDateEarlier(Date testDate, Date compareAgainstDate)
    {
        long timeTestDate = testDate.getTime();
        long timeCompareDate = compareAgainstDate.getTime();
        return(timeTestDate < timeCompareDate);    
    }

    /**
     * Method GetGregorianCalendarFromDate
     * Convert Date to Gregorian Calendar.
     * @param date
     * @return GregorianCalendar
     */
    //
    public static GregorianCalendar getGregorianCalendarFromDate(Date date)
        throws Exception
    {   
    try
        { 
        if (date == null)
            return(null);
            
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
        String strDate = formatter.format(date);
        GregorianCalendar cal = getGregorianCalendarFromString(strDate);
        return(cal);
        }
    catch(Exception ex)
        {
        throw new Exception("Generated a general exception, function=GetGregorianCalendarFromDate. Error = "+ex.getMessage());    
        }
    }

    /**
     * Method GetDateFromGregorianCalendar.
     * Convert Gregorian Calendar to Date
     * @param cal
     * @return Date
     */
    public static Date getDateFromGregorianCalendar(GregorianCalendar cal)
        throws Exception
    {   
    try
        {  
        if (cal == null)
            return(null);

        //!!!!NOTE Add 1 here, gregorian calendar is zero based
        //but the DATE IS NOT!
        String strDate = (cal.get(Calendar.MONTH)+1)+"-"+cal.get(Calendar.DAY_OF_MONTH)+"-"+cal.get(Calendar.YEAR);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        ParsePosition pos = new ParsePosition(0);
        Date date = dateFormat.parse(strDate, pos);
        return(date);
        }
    catch(Exception ex)
        {
        throw new Exception("Generated a general exception, function=GetDateFromGregorianCalendar. Error = "+ex.getMessage());    
        }
    }

    /**
     * Method getShortFormatMonthYearFromDate.
     * @param date
     * @return String
     */
    public static String getShortFormatMonthYearFromDate(Date date)
    {
        if (date == null)
            return(null);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd");
        String strDate = formatter.format(date);
        return(strDate);
    }

    /**
     * Method GetStringFromDate.
     * convert String to Date
     * @param date
     * @return String
     */
    public static String getStringFromDate(Date date)
    {
        if (date == null)
            return(null);
            
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = formatter.format(date);
        return(strDate);
    }

    /**
     * Method GetDateFromString.
     * convert Date to String
     * @param strDate
     * @return Date
     */
    public static Date getDateFromString(String strDate)
    {
        if (strDate == null)
            return(null);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        Date date = dateFormat.parse(strDate, pos);
        return(date);    
    }

    /**
     * Method FormatHTMLUseableDate.
     * Convert Gregorian Calendar to HTML Usable string
     * @param cal
     * @return String
     */
    public static String formatHTMLUseableDate(GregorianCalendar cal)
    {
        if (cal == null)
            return(null);

        String year  = String.valueOf(cal.get(GregorianCalendar.YEAR));
        
        //The calendar class is an array and zero based, 
        //when we want to display we need to add 1
        String month = String.valueOf((cal.get(GregorianCalendar.MONTH) + 1));
        String day   = String.valueOf(cal.get(GregorianCalendar.DAY_OF_MONTH));
        
        String strHTML =  month + "/" + day + "/" + year;
        
        return(strHTML);
    }

    /**
     * Method formatHTMLUseableDate.
     * @param date
     * @return String
     */
    public static String formatHTMLUseableDate(Date date)
        throws Exception
    {
        GregorianCalendar cal = getGregorianCalendarFromDate(date);
        if (cal == null)
            return(null);

        String year  = String.valueOf(cal.get(GregorianCalendar.YEAR));
        //The calendar class is an array and zero based, 
        //when we want to display we need to add 1
        String month = String.valueOf((cal.get(GregorianCalendar.MONTH) + 1));
        String day   = String.valueOf(cal.get(GregorianCalendar.DAY_OF_MONTH));
        
        String strHTML =  month + "/" + day + "/" + year;
        
        return(strHTML);
    }

    /**
     * Method getTodaysDateLocalized.
     * @return Date
     * @throws Exception
     */
    public static Date getTodaysDateLocalized()
        throws Exception
    {
        Date curDate = new Date();
        TimeZone zone = (TimeZone)SimpleTimeZone.getDefault();
        GregorianCalendar cal = new GregorianCalendar(zone);    
        cal.setTime(curDate);
        return(ClockDate.getDateFromGregorianCalendar(cal));                    
    }


    /**
     * Method getMonthOffsetDateLocalized -get a day offset from today.
     * @return Date
     * @throws Exception
     */
    public static Date getMonthOffsetDateLocalized(int monthOffset, boolean beginningOfMonth)
        throws Exception
    {   
    try
        { 
        Date curDate = new Date();
        TimeZone zone = (TimeZone)SimpleTimeZone.getDefault();
        GregorianCalendar cal = new GregorianCalendar(zone);    
        cal.setTime(curDate);
        
        //add (or subtract) the month, allow to cross year boundaries
        cal.add(Calendar.MONTH, monthOffset);
        int year  = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        if (beginningOfMonth == true)
            cal.set(year,month,1);
        else
            cal.set(year,month,cal.getActualMaximum(Calendar.DAY_OF_MONTH));

        return(ClockDate.getDateFromGregorianCalendar(cal));                    
        }    
    catch(Exception ex)
        {
        throw new Exception("Generated a general exception, function=getMonthOffsetDateLocalized. Error = "+ex.getMessage());    
        }
    }


    /**
     * Method setEndDateMonth.
     * @param newStartDate
     * @return Date
     */
    public static synchronized Date setEndDateMonth(Date startDate)
        throws Exception
    {   
    try
        { 
        GregorianCalendar cal = getGregorianCalendarFromDate(startDate);
        int year  = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        cal.set(year,month,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return(ClockDate.getDateFromGregorianCalendar(cal));                    
        }    
    catch(Exception ex)
        {
        throw new Exception("Generated a general exception, function=setEndDateMonth. Error = "+ex.getMessage());    
        }
    }

    /**
     * Method getNumberOfMonths.
     * @param startDate
     * @param endDate
     * @return int
     * @throws Exception
     */
    public static synchronized int getNumberOfMonths(Date startDate, Date endDate)
        throws Exception
    {   
    try
        { 
        int numMonths = 1;
        GregorianCalendar startCal = getGregorianCalendarFromDate(startDate);
        GregorianCalendar endCal = getGregorianCalendarFromDate(endDate);

        int year1=startCal.get(Calendar.YEAR);
        int month1=startCal.get(Calendar.MONTH);
        int year2=endCal.get(Calendar.YEAR);
        int month2=endCal.get(Calendar.MONTH);
        
        //inclusive so always add 1;
        
        if (month2 < month1) //cross year boundaries?
            {
            if (year1 < year2)
                {           //total months in year 0-11
                numMonths = ( ( ( 12 - month1 ) + month2 ) + 1 );
                }    
            else
                {
                throw new Exception("Time formatting error, start is after end");    
                }        
            }    
        else
            numMonths = ( ( month2 - month1 ) + 1 );
        
        return(numMonths);
        }    
    catch(Exception ex)
        {
        throw new Exception("Generated a general exception, function=getNumberOfMonths. Error = "+ex.getMessage());    
        }
    }

    /**
     * Method addMonth.
     * @param startDate
     * @return Date
     * @throws Exception
     */
    public static synchronized Date addMonth(Date startDate)
        throws Exception
    {   
    try
        { 
           GregorianCalendar cal = getGregorianCalendarFromDate(startDate);
           cal.add(Calendar.MONTH, 1); 
        return(ClockDate.getDateFromGregorianCalendar(cal));                    
        }    
    catch(Exception ex)
        {
        throw new Exception("Generated a general exception, function=addMonth. Error = "+ex.getMessage());    
        }
    }


    /**
     * Method getDateFromCalendarString.
     * Strings come from the calendar control as "Date(1999,11,25)
     * @param calString
     * @return Date
     * @throws Exception
     */
    public static synchronized Date getDateFromCalendarString(String calString)
        throws Exception
    {   
    try
        { 
        if (calString == null)
            return(null);
                
        Date newDate = null;    
        int dateStart = calString.indexOf("(");
        int indexOfYear = calString.indexOf("/");
        if ( dateStart != -1 )
            {
            int dateEnd = calString.indexOf(")");
            String tokenString = calString.substring(dateStart+1,dateEnd);
            StringTokenizer st = new StringTokenizer(tokenString, ",");
            String year   = st.nextToken();
            String month  = st.nextToken();
            String day    = st.nextToken();
            newDate = getDateFromString(year+"-"+month+"-"+day);
            }
        else if ((indexOfYear < 3) && (indexOfYear != -1)) //month-day-year format, convert    
            {
            StringTokenizer st = new StringTokenizer(calString, "/");
            String month  = st.nextToken();
            String day    = st.nextToken();
            String year   = st.nextToken();
            newDate = getDateFromString(year+"-"+month+"-"+day);
            }    
        else
            {
            newDate =  getDateFromString(calString);   
            }    
        return(newDate);
        }    
    catch(Exception ex)
        {
        throw new Exception("Generated a general exception, function=GetGregorianCalendarFromString. Error = "+ex.getMessage());    
        }
    }


    /**
     * Take inputs for a reference date and format and locale and create a vieweable string
     * @param refDate
     * @param formatString
     * @param locale
     * @return
     */
    public static String getDisplayStringForLong(long refDate, String formatString, String timeZone)
    {
        Date curDate = new Date(refDate);
        SimpleDateFormat formatter = new SimpleDateFormat(formatString);
        formatter.setTimeZone(TimeZone.getTimeZone(timeZone));
        String displayString = formatter.format(curDate);
        return(displayString);
    }

    /**
     * Method GetGregorianCalendarFromString.
     * @param strDate
     * @return GregorianCalendar
     */
    public static synchronized int getCalendarElementFromLong(long refDate, int whichElement)
        throws Exception
    {   
    try
        {
    	Date curDate = new Date(refDate);
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy-K-m-s-a");
        String strDate = formatter.format(curDate);

    	StringTokenizer st = new StringTokenizer(strDate, "-");
        
        //NOTE!!! Gregorian Calendar is Zero Based, 
        //must subtract one on the months
        int nMonth  = (Integer.parseInt(st.nextToken()) - 1);
        int nDay    = Integer.parseInt(st.nextToken());
        int nYear   = Integer.parseInt(st.nextToken());
        
        int nHour   = 0;
        int nMinute = 0;
        int nSecond = 0;
        int nAmPm   = 0;	
        nHour   = Integer.parseInt(st.nextToken());
        nMinute = Integer.parseInt(st.nextToken());
        nSecond = Integer.parseInt(st.nextToken());
        String AmPm = st.nextToken();
        if (AmPm.equals("PM"))
        	nAmPm = GregorianCalendar.PM;
        else
        	nAmPm = GregorianCalendar.AM;

        switch(whichElement)
    	{
        	case GregorianCalendar.YEAR:
        		return(nYear);
        	case GregorianCalendar.MONTH:
        		return(nMonth);
        	case GregorianCalendar.DAY_OF_MONTH:
        		return(nDay);
        	case GregorianCalendar.HOUR:
        		return(nHour);
        	case GregorianCalendar.MINUTE:
        		return(nMinute);
        	case GregorianCalendar.SECOND:
        		return(nSecond);
        	case GregorianCalendar.AM_PM:
        		return(nAmPm);
    	}
        return(0);
    	}    
    	catch(Exception ex)
    	{
    		throw new Exception("Generated a general exception, function=getCalendarElementFromLong. Error = "+ex.getMessage());    
    	}
    }


    /**
     * Method GetGregorianCalendarFromString.
     * @param strDate
     * @return GregorianCalendar
     */
    private static synchronized GregorianCalendar getGregorianCalendarFromString(String strDate)
        throws Exception
    {   
    try
        { 
        GregorianCalendar cal = new GregorianCalendar();
        TimeZone zone = (TimeZone)SimpleTimeZone.getDefault();
        cal.setTimeZone(zone);

        StringTokenizer st = new StringTokenizer(strDate, "-");
        
        //NOTE!!! Gregorian Calendar is Zero Based, 
        //must subtract one on the months
        int nMonth  = (Integer.parseInt(st.nextToken()) - 1);
        int nDay    = Integer.parseInt(st.nextToken());
        int nYear   = Integer.parseInt(st.nextToken());
        
        //if has
        if (st.hasMoreTokens())
            {
            int nHour   = Integer.parseInt(st.nextToken());
            int nMinute = Integer.parseInt(st.nextToken());
            int nSecond = Integer.parseInt(st.nextToken());
            cal.set(nYear, nMonth, nDay, nHour, nMinute, nSecond);
            }
        else
            {
            cal.set(nYear, nMonth, nDay);
            }
        
        return(cal);
        }    
    catch(Exception ex)
        {
        throw new Exception("Generated a general exception, function=GetGregorianCalendarFromString. Error = "+ex.getMessage());    
        }
    }

    
    public void setFrame(SVG_SWTViewerFrame frame) {
		this.frame = frame;
	}

 
  }