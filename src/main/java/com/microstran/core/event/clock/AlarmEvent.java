/*
Copyright(c) 2022 Microstran Inc..  All rights reserved.
 
This file contains data proprietary to Microstran Inc.
It may contain data proprietary to others, with use granted to Microstran
 under a non-disclosure agreement. Do not release this 
information to any party unless that party has signed all appropriate 
non-disclosure agreements. 
The source code for this software is not published and 
remains protected by trade secret laws, notwithstanding any deposits 
with the U.S. Copyright Office.
*/

package com.microstran.core.event.clock;

import com.microstran.core.event.ApplicationEvent;

/**
 * The main event class for clocks
 *
 * @author Mike Stran
 *
 */
public class AlarmEvent extends ApplicationEvent
{
		
	/**
	 * Default constructor.
	 */
	public AlarmEvent( )
	{
	}
	
	/**
	 * 
	 * @param eventData for the event
	 */
	public AlarmEvent( Object eventData )
	{
		this.eventData = eventData;
	}
		
	/**
	 * 
	 * @return The event type.
	 */
	public String getApplicationEventType( )
	{
		return("AlarmEvent");
	}
	
}
