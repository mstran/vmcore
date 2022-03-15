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

package com.microstran.core.alarm;

import com.microstran.core.clock.ClockDate;
import com.microstran.core.event.ApplicationEventServer;
import com.microstran.core.event.clock.AlarmEvent;

/**
 * Generic interface for an alarm
 * 
 * @author Mike Stran
 *
 */
public class ClockAlarm implements com.microstran.core.alarm.Alarm
{

	private boolean enabled;
    private boolean weekly;
    
    private int ID;
    private ClockDate date;
    
    private String alarmResource;
    private String volume;
    
    
    /**
     * constructor
     */
    public ClockAlarm()
    {}
    
    /**
     * Copy constructor
     * @param alarm
     */
    public ClockAlarm(ClockAlarm alarm)
    {
        this.date = alarm.getDate();
        this.enabled = alarm.enabled;
        this.weekly  = alarm.weekly;
        this.alarmResource = alarm.alarmResource;
        this.volume  = alarm.volume;
    }
    
    /* (non-Javadoc)
     * @see com.microstran.core.alarm.Alarm#isEnabled()
     */
    public boolean isEnabled()
    {
        return(enabled);
    }

    /**
     * Set enabled flag
     * @param enabled
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
    
    /* (non-Javadoc)
     * @see com.microstran.core.alarm.Alarm#isDaily()
     */
    public boolean isWeekly()
    {
        return(weekly);
    }
    
    /**
     * Set the daily flag
     * @param daily
     */
    public void setWeekly(boolean weekly)
    {
        this.weekly = weekly;
    }
    
	/**
	 * try and fire an event based on the date, compare against us
	 * @param weeklyAlarm
	 * @param date
	 */
	public void sendAlarm(ClockDate date)
	{
	    if (!enabled)
	        return;
	        
        //is the am/pm set right
	    if (this.date.getAmPm() == date.getAmPm())
	    {   //ampm is right, is the time correct? 
	        if ((this.date.getHour() == date.getHour()) && (this.date.getMinute() == date.getMinute()))
	        {
	            //is year right
	            if (this.date.getYear() == date.getYear())
                {
	                if (this.weekly == true)
	                {
	    	            if(this.date.getDayOfWeek() == date.getDayOfWeek())
	    	            {
	    	                ApplicationEventServer.instance().publishEvent(new AlarmEvent(this));
    	                }
	                }
	                else
	                {	//this is a one time only event
	    	            if(this.date.getDayOfMonth() == date.getDayOfMonth())
	    	            {
	    	                ApplicationEventServer.instance().publishEvent(new AlarmEvent(this));
	    	                enabled=false;
    	                }
	                }
                }    
	        }
	    }
	}
	
	/* (non-Javadoc)
	 * @see com.microstran.core.alarm.Alarm#getAlarmResource()
	 */
	public String getAlarmResource()
	{
	    return(alarmResource);
	}
    /**
     * @param alarmResource The alarmResource to set.
     */
    public void setAlarmResource(String alarmResource)
    {
        this.alarmResource = alarmResource;
    }
    /**
     * @return Returns the volume.
     */
    public String getVolume()
    {
        return volume;
    }
    /**
     * @param volume The volume to set.
     */
    public void setVolume(String volume)
    {
        this.volume = volume;
    }
    /**
     * @return Returns the iD.
     */
    public int getID()
    {
        return ID;
    }
    /**
     * @param id The iD to set.
     */
    public void setID(int id)
    {
        ID = id;
    }

    /**
     * @return Returns the date.
     */
    public ClockDate getDate()
    {
        return date;
    }
    /**
     * @param date The date to set.
     */
    public void setDate(ClockDate date)
    {
        this.date = date;
    }
}