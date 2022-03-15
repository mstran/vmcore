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
 * The main event class for clocks makeing change requests
 *
 * @author Mike Stran
 *
 */
public class WindowChangeEvent extends ApplicationEvent
{
	
    public static final String NEW_VIEWER_EVENT = "newViewerEvent";
    public static final String PREVIOUS_VIEWER_EVENT = "previousClockEvent";
    public static final String NEXT_VIEWER_EVENT = "nextClockEvent";
    public static final String F10_EVENT = "F10Event";
    public static final String F12_EVENT = "F12Event";
    public static final String VIEWER_CLOSE_EVENT = "viewerCloseEvent";
    public static final String VIEWER_RESIZE_EVENT = "resizeEvent";
    public static final String DIALOG_SELECT_CLOCK = "dialogClockSelection";
    public static final String SHOW_VIEWER_EVENT = "showViewerEvent";
    public static final String HIDE_VIEWER_EVENT = "hideViewerEvent";
    public static final String CHANGE_VIEWER_EVENT = "changeViewerEvent";
	public static final String SLIDE_VIEWER_EVENT = "slideViewerEvent";
    public static final String CHANGE_TIMEZONE_EVENT = "changeTimeZoneEvent";
	public static final String CREATE_VIEWER_ROW_EVENT = "createViewerRowEvent";
    public static final String CYCLE_CLOCK_EVENT = "cycleClockEvent";
	public static final String CHANGE_SCREENSAVER_EVENT = "changeScreenSaverEvent";
	public static final String UPDATE_VIEWER_EVENT = "updateViewerEvent";
	
	/**
	 * Default constructor.
	 */
	public WindowChangeEvent( )
	{
	}
	
	/**
	 * 
	 * @param eventData for the event
	 */
	public WindowChangeEvent( String type, Object eventData )
	{
		this.eventData = eventData;
		this.eventType = type;
	}
		
	/**
	 * 
	 * @return The event type.
	 */
	public String getApplicationEventType( )
	{
		return(eventType);
	}
	
	
}
