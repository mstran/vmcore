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
package com.microstran.core.engine;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.util.List;
import java.util.Random;
import java.util.SimpleTimeZone;
import java.util.Timer;


/**
 * @author Mstran
 *	Screen Save base class. This class is intended to provide base class functionality for all screen savers
 * 
 * 	It will use a fixed number of clocks.
 *  
 */
public class AbstractScreenSaver extends ClockGUI implements ScreenSaver
{
    
    /////////////////TIMERS//////////////////////////
    /**
     * timer for starting events
     */
    protected static Timer showClockEventTimer;

    /**
     * timer for changing events
     */
    protected static Timer changeClockEventTimer;

    /**
     * timer for changing events
     */
    protected static Timer moveClockEventTimer;
    
    
    
    
    /**
     * Random generator for showing clocks
     */
    protected static Random timeZoneGenerator = new Random();
    
    /**
     * Random generator for showing clocks
     */
    protected static Random showGenerator = new Random();

        
    
    /////////////////////////////////////////Collection Classes/////////////////////////////////////
	/**
     * Array of all candidate screen rectangles ->NEEDED WHERE user has more than 1 monitor hooked up!
     */
    public static List<Rectangle[][]> allScreenRectangles;

    /**
     * Array of all clock display elements for the screen in here are all the elements
     */
    protected static List allScreenClocks;

    /**
     * Total number of created clocks
     */
    protected int totalClocks;

    /**
     * array of timezones to use
     */
    public static String [] timeZones;

    static
    {
        timeZones = SimpleTimeZone.getAvailableIDs();
    }
    
    
    //flag so that we don't try and create or swap a clock when we are changing the screen saver
    protected boolean changingScreenSaver = false;
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    

    /**********************************************************
     * 					CONFIG FILE PARAMETERS
     * The following parameters all have default values so that
     * if config file is missing or damaged these will be used
     * as the reasonable default values in it's place   
     **********************************************************/
    
    /**
     * How frequently to rotate screen savers default to 15 minutes
     */
    protected static long screenSaverRotationPeriod = 900000;
    
    /**
     * How frequently to rotate clocks withing a screen saver defaults to 5 minutes
     */
    protected static long clockRotationPeriod = 30000;
    
    /**
     * How many to allow per window
     */
    protected static int maxClocksPerScreen = 16;

    /**
     * various control flags
     */
    protected static boolean limitScreenToGroup = false;
    protected static boolean frameAroundClocks  = false;
    protected static boolean boxAroundCaption   = false;

    /**
     * the clock name for groups
     */
    protected static String currentClockGroupName = "Antique Pocket Watch";
    
    
    
    
    /**
     * syncrhonization object
     */
    protected StringBuffer lockObj = new StringBuffer();
    
    /**
     * constant for initial show rate of 3 seconds
     */
    protected static final long SHOW_CLOCK_TIME_EVENT_VALUE = 3000;
    
        
    protected Cursor invisibleCursor;

	/**
	 * Random generator for changing clocks
	 */
	protected static Random changeGenerator = new Random();
    
   
	/**
	 * default constructor
	 */
	public AbstractScreenSaver()
	{
	    super();
	}
    
	public void layoutClocks()
	{    
	}
    public void instantiateClocks()
    {
    }
	public void registerForEvents()
	{
	}
	public void unregisterForEvents()
	{
	}
	public Color getBackgroundColor()
	{
        return(Color.LIGHT_GRAY);
	}
	public void startScreenSaver()
	{
	}
	public void stopScreenSaver()
	{
	    unRegisterClock();
	}
	
    /**
     * @return Returns the allScreenRectangles.
     */
    public static List<Rectangle[][]> getAllScreenRectangles()
    {
        return allScreenRectangles;
    }
    /**
     * @param allScreenRectangles The allScreenRectangles to set.
     */
    public static void setAllScreenRectangles(List<Rectangle[][]> allScreenRectangles)
    {
        AbstractScreenSaver.allScreenRectangles = allScreenRectangles;
    }
    /**
     * @return Returns the boxAroundCaption.
     */
    public boolean isBoxAroundCaption()
    {
        return boxAroundCaption;
    }
    /**
     * @param boxAroundCaption The boxAroundCaption to set.
     */
    public void setBoxAroundCaption(boolean boxCaption)
    {
        boxAroundCaption = boxCaption;
    }
    /**
     * @return Returns the clockRotationPeriod.
     */
    public long getClockRotationPeriod()
    {
        return clockRotationPeriod;
    }
    /**
     * @param clockRotationPeriod The clockRotationPeriod to set.
     */
    public void setClockRotationPeriod(long rotationPeriod)
    {
        clockRotationPeriod = rotationPeriod;
    }
    /**
     * @return Returns the frameAroundClocks.
     */
    public boolean isFrameAroundClocks()
    {
        return frameAroundClocks;
    }
    /**
     * @param frameAroundClocks The frameAroundClocks to set.
     */
    public void setFrameAroundClocks(boolean frameClocks)
    {
        frameAroundClocks = frameClocks;
    }
    /**
     * @return Returns the limitScreenToGroup.
     */
    public boolean isLimitScreenToGroup()
    {
        return limitScreenToGroup;
    }
    /**
     * @param limitScreenToGroup The limitScreenToGroup to set.
     */
    public void setLimitScreenToGroup(boolean limitGroup)
    {
        limitScreenToGroup = limitGroup;
    }
    /**
     * @return Returns the maxClocksPerScreen.
     */
    public int getMaxClocksPerScreen()
    {
        return maxClocksPerScreen;
    }
    /**
     * @param maxClocksPerScreen The maxClocksPerScreen to set.
     */
    public void setMaxClocksPerScreen(int maxClocks)
    {
        maxClocksPerScreen = maxClocks;
    }
    /**
     * @return Returns the screenSaverRotationPeriod.
     */
    public long getScreenSaverRotationPeriod()
    {
        return screenSaverRotationPeriod;
    }
    /**
     * @param screenSaverRotationPeriod The screenSaverRotationPeriod to set.
     */
    public void setScreenSaverRotationPeriod(long rotationPeriod)
    {
        screenSaverRotationPeriod = rotationPeriod;
    }
    /**
     * @return Returns the totalClocks.
     */
    public int getTotalClocks()
    {
        return totalClocks;
    }
    /**
     * @param totalClocks The totalClocks to set.
     */
    public void setTotalClocks(int totalClocks)
    {
        this.totalClocks = totalClocks;
    }
    /**
     * @param invisibleCursor The invisibleCursor to set.
     */
    public void setInvisibleCursor(Cursor invisibleCursor)
    {
        this.invisibleCursor = invisibleCursor;
    }
    /**
     * @return Returns the currentClockGroupName.
     */
    public static String getCurrentClockGroupName()
    {
        return currentClockGroupName;
    }
    /**
     * @param currentClockGroupName The currentClockGroupName to set.
     */
    public static void setCurrentClockGroupName(String currentClockGroupName)
    {
        AbstractScreenSaver.currentClockGroupName = currentClockGroupName;
    }
}