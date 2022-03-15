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

import java.time.Instant;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.microstran.core.alarm.ClockAlarm;
import com.microstran.core.engine.ClockGUI;
import com.microstran.core.event.ApplicationEvent;
import com.microstran.core.event.ApplicationEventServer;
import com.microstran.core.event.clock.WindowChangeEvent;
import com.microstran.core.util.helper.ObjectHelper;
import com.microstran.core.viewer.SVG_SWTViewerFrame;

/**
 * The base class for all clock types
 *
 * @author Mike Stran
 *
 */
public class Clock extends Thread
{
	protected String ID;
	protected String ParentID;
	protected String clockName;
	protected String svgPath;
	protected String previewPath;
	protected String category;
	
	//runtime data
	protected String caption;
	protected int srcWidth, srcHeight;
	protected int currentWidth, currentHeight;
	protected int posX, posY;
	
	protected int hourHandX, hourHandY;
	protected int minHandX, minHandY;
	protected int secHandX, secHandY;
	protected int strengthIndX, strengthIndY;
	protected int moonPhaseDialX, moonPhaseDialY;
	
	protected float timerMinuteHandX, timerMinuteHandY;
	protected float timerSecondHandX, timerSecondHandY;

	protected float degreePerDayOfMonth, degreePerMonthOfYear, degreePerDayOfWeek;
	protected float degreePerHour, degreePerMinute, degreePerMinutePerHour, degreePerSecondPerHour; 
	protected float degreePerSecond, degreePerFractionalSecond; 
	protected float timerDegreePerSecond, timerDegreePerFractionalSecond;
	protected float strengthBaseDegrees;	
	protected float degreesPerMoonPhase;
	
	protected Renderer renderer;
	protected String implementationType;
	protected String rendererType;
	protected String mainClockEventType;
	
	protected Vector<ClockAlarm> alarms = new Vector<ClockAlarm>();
	
	//some flags from the runtime profile
	protected boolean allowedToScale = true;
	protected boolean fixedAspectRatio = true;
	protected boolean displayDate = true;
	protected boolean allowedToShowTime = true;
	protected boolean usingTwelveHrFormat = true;
	protected boolean allowedToShowDate = true;
	protected boolean onTopInFullWindowMode = true;
	protected boolean isFullWindow = false;
	protected boolean cycleClocks = false;
	protected int cyclePeriod = 0;
	protected boolean CycleGroupLimit=false;
	protected String CycleGroup="";
	
	protected SVG_SWTViewerFrame frame;
	protected ClockDate date;
	
	protected boolean running = true;
	protected boolean renderNow = false;
	protected boolean canRender = true;
	
	public MoonPhaseCalculator moonCalc;
	
	/**
	 * internal timer for clock ticks
	 */
	protected Timer clockTickTimer;
    
	 /**
     * timer for changing clock events
     */
    protected Timer changeClockEventTimer;

	
	/**
	 * rendering hint
	 */
	public static final int FAVORS_WIDTH = 1;
	public static final int FAVORS_HEIGHT = 2;
	public static final int SQUARE = 3;	
	
	public long desiredInterval;
	
	//the overall gui application, needed to provide the renderer with
	//actual access to the window and hence the update thread
	private ClockGUI application;
	
	public Clock()
	{
	}
	
	public static Clock createClock(Clock refClock)
	{
		try
		{
		    //Instantiate a real clock from the abstract definitions
			String implType = refClock.getImplementationType();
			Clock implClock = (Clock)ObjectHelper.build(implType); 
			implClock.createTimer();
			//instantiate the clocks renderer
			String renderType = refClock.getRendererType();
			Object obj = ObjectHelper.build(renderType); 
			Renderer renderer = (Renderer)obj;
			renderer.setClock(implClock);
			implClock.setRenderer(renderer);	
			copyDesignTimeSettings(implClock, refClock);
			return(implClock);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		return(null);
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run( )
	{
		long x = desiredInterval;
		long diff = 0;
        long startTime = 0;
        long endTime = 0;
        Instant instantStart = null;
        Instant instantEnd = null;
        
	    while(running)
	    {
	        try
	        {
	        	Thread.sleep(x);
	            instantStart = Instant.now();
	            startTime = instantStart.toEpochMilli();
	            renderNow = true;
	            listen(null);
	            renderNow = false;
	            instantEnd = Instant.now();
	            endTime = instantEnd.toEpochMilli();
	            diff = endTime - startTime;
	            x = desiredInterval - diff;
	            if (x < 0) {
	            	x = 0;
	            }
	        }
	        catch(InterruptedException e)
	        {
	        System.out.println("interrupted");    
	        }
	    }
	}
	
	/**
	 * create the timer
	 */
	public void createTimer()
	{
	    clockTickTimer = new Timer();
	}
	
	/**
	 * Function to start the clock thread running
	 */
	public void startClock()
	{
	    this.start();
	    
	    
	    if (mainClockEventType.equals("OneSecondEvent"))
	        desiredInterval = 1000;
	    else if (mainClockEventType.equals("HalfSecondEvent"))
	    	desiredInterval = 500;
	    else if (mainClockEventType.equals("QuarterSecondEvent"))
	    	desiredInterval = 250;
	    else if (mainClockEventType.equals("TenthSecondEvent"))
	    	desiredInterval = 100;
	    
	    canRender = true;
	    renderNow = false;
	    
	    if (cycleClocks == true)
	    {
	        changeClockEventTimer = new Timer();
	        //cycle period expressed in minutes. 60,000 per minute
	        long valueInMils = (cyclePeriod * 60000);
	        //start out sometime in the near future then after that
	        changeClockEventTimer.schedule(clockChangeEventTime, valueInMils);
	    }
	    
	}
	
	/**
	 * Function to stop the clock ticking
	 */
	public void pauseClock()
	{
	    canRender = false;
	    
	}
	
	/**
	 * function to resume a paused clock
	 */
	public void resumeClock()
	{
	    canRender = true;
	}
	
	/**
	 * API to terminate the clock and associated thread
	 * NOTE!!!! once you call this you MUST NOT try and resume the
	 * clock, this is END of this clock and it cannot be resumed,
	 * for pause and resume behavior use pauseClock/resumeClock
	 */
	public void endClock()
	{
		//check, this will be null when first setting this value, the old clock does NOT have a timer setup yet!
	    if (cycleClocks) {
	    	if (this.changeClockEventTimer != null) {
	    		this.changeClockEventTimer.cancel();
	    	}
	    }
	    this.canRender = false;
	    //condition to end while loop and thread.
	    this.running = false;
	    //cancel out rendering if on is pending
	    this.renderer.endRenderer();
	    //null out some things to help ensure they don't stay around
	    this.frame = null;
	    this.alarms.clear();
	    this.renderer = null;
	    this.clockName = null;
	    this.svgPath = null;
	    this.previewPath = null;
	    this.category = null;
	    this.implementationType = null;
	    this.rendererType = null;
	    this.mainClockEventType = null;
	    this.caption = null;
	    this.CycleGroup = null;
	}

    /**
     * event to fire
     * Comment for <code>timerTickTime</code>
     */
    private TimerTask clockChangeEventTime = new TimerTask() 
    {
        public void run() 
        {
            //fire off a change event for this clock
            ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.CYCLE_CLOCK_EVENT,frame));
        }
    };
    
	/**
	 * Default constructor.
	 */
	public Clock(ClockGUI application, Renderer renderer)
	{
		this.renderer = renderer;
		this.application = application;
		renderer.setClock(this);
	}
	
	/**
	 * Copy over the design time settings
	 * Copy the key settings over
	 * @param targetClock
	 * @param refClock
	 */
	public static void copyDesignTimeSettings(Clock targetClock, Clock refClock)
	{
		targetClock.setApplication(refClock.getApplication());
		targetClock.setImplementationType(refClock.getImplementationType());
        targetClock.setRendererType(refClock.getRendererType());
		targetClock.setParentID(refClock.getParentID());
		targetClock.setID(refClock.getID());
		targetClock.setCategory(refClock.getCategory());
		targetClock.setClockName(refClock.getClockName());
		targetClock.setPreviewPath(refClock.getPreviewPath());
		targetClock.setSvgPath(refClock.getSvgPath());
		targetClock.setCaption(refClock.getCaption());
		targetClock.setSrcWidth(refClock.getSrcWidth());
		targetClock.setSrcHeight(refClock.getSrcHeight());
		targetClock.setMainClockEventType(refClock.getMainClockEventType());
	    targetClock.setHourHandX(refClock.getHourHandX());
	    targetClock.setHourHandY(refClock.getHourHandY());
	    targetClock.setMinHandX(refClock.getMinHandX());
	    targetClock.setMinHandY(refClock.getMinHandY());
	    targetClock.setSecHandX(refClock.getSecHandX());
	    targetClock.setSecHandY(refClock.getSecHandY());
	    targetClock.setTimerMinuteHandX(refClock.getTimerMinuteHandX());
	    targetClock.setTimerMinuteHandY(refClock.getTimerMinuteHandY());
	    targetClock.setTimerSecondHandX(refClock.getTimerSecondHandX());
	    targetClock.setTimerSecondHandY(refClock.getTimerSecondHandY());
	    targetClock.setStrengthIndX(refClock.getStrengthIndX());
	    targetClock.setStrengthIndY(refClock.getStrengthIndY());
	    targetClock.setStrengthBaseDegrees(refClock.getStrengthBaseDegrees());
	    targetClock.setMoonPhaseDialX(refClock.getMoonPhaseDialX());
	    targetClock.setMoonPhaseDialY(refClock.getMoonPhaseDialY());
	    targetClock.setDegreesPerMoonPhase(refClock.getDegreesPerMoonPhase());
	    targetClock.setDegreePerDayOfMonth(refClock.getDegreePerDayOfMonth());
	    targetClock.setDegreePerMonthOfYear(refClock.getDegreePerMonthOfYear());
	    targetClock.setDegreePerDayOfWeek(refClock.getDegreePerDayOfWeek());

	    targetClock.setDegreePerHour(refClock.getDegreePerHour());
	    targetClock.setDegreePerMinute(refClock.getDegreePerMinute());
	    targetClock.setDegreePerMinutePerHour(refClock.getDegreePerMinutePerHour()); 
	    targetClock.setDegreePerSecondPerHour(refClock.getDegreePerSecondPerHour()); 
	    targetClock.setDegreePerSecond(refClock.getDegreePerSecond());
	    targetClock.setDegreePerFractionalSecond(refClock.getDegreePerFractionalSecond());
	    targetClock.setTimerDegreePerSecond(refClock.getTimerDegreePerSecond());
	    targetClock.setTimerDegreePerFractionalSecond(refClock.getTimerDegreePerFractionalSecond());
    }
	
	/**
	 * Clocks from the next/prev settings are cloned off the stack 
	 * and are hot replacements to running clocks that have runtime data associated so
	 * you call this to set that data into the new clock
	 * @param targetClock
	 * @param refClock
	 * @param parentID
	 */
	public static void copyRuntimeSettings(Clock targetClock, Clock refClock, String parentID)
	{
	    targetClock.setCurrentWidth(refClock.getCurrentWidth());
		targetClock.setCurrentHeight(refClock.getCurrentHeight());
		targetClock.setPosX(refClock.getPosX());
		targetClock.setPosY(refClock.getPosY());
		targetClock.setCaption(refClock.getCaption());
	    targetClock.setDisplayDate(refClock.isDisplayDate());
	    targetClock.setAllowedToShowDate(refClock.isAllowedToShowDate());
	    targetClock.setUsingTwelveHrFormat(refClock.isUsingTwelveHrFormat());
	    targetClock.setAllowedToShowTime(refClock.isAllowedToShowTime());
	    targetClock.setAllowedToScale(refClock.isAllowedToScale());
	    targetClock.setFixedAspectRatio(refClock.isFixedAspectRatio());
	    targetClock.setOnTopInFullWindowMode(refClock.isOnTopInFullWindowMode());
	    targetClock.setFullWindow(refClock.isFullWindow());
	    targetClock.date = new ClockDate(refClock.getDate());
	    targetClock.getAlarms().clear();
	    
	    targetClock.setCycleClocks(refClock.isCycleClocks());
		targetClock.setCyclePeriod(refClock.getCyclePeriod());
		targetClock.setCycleGroupLimit(refClock.isCycleGroupLimit());
		targetClock.setCycleGroup(refClock.getCycleGroup());
	    
	    copyAlarms(targetClock, refClock);
	    //we don't want to copy the parent ID though, since that in fact does need to come from our selection!
	    targetClock.setParentID(parentID);
	}
	

	/**
	 * Return the rendering hint for favors width, height or square
	 * @return
	 */
	public int getRenderingHint()
	{
	    //looking at a roughly 3/4 or 4/3 ratio as targets
	    float width  = srcWidth;
	    float height = srcHeight;
	    float ratio = width/height;
	    if (ratio > 1.3)
	        return(FAVORS_WIDTH);
	    else if (ratio < .75)
	        return(FAVORS_HEIGHT);
	    else
	        return(SQUARE);
	}
	
	/**
	 * Does this clock support mouse clicks?
	 * @return
	 */
	public boolean hasMouseAlerts()
	{
	    return(false);
	}
	
	/**
	 * Return any mouse alerts
	 * @return
	 */
	public List<String> getMouseAlerts()
	{
	    return(null);
	}
	
	public void recieveMouseAlert(String elementID)
	{
	}
	
	/**
	 * Add an alarm
	 * @param alarm
	 */
	public void addAlarm(ClockAlarm alarm)
	{
	    this.alarms.add(alarm);
	}
	
	/**
	 * remove an alarm by ID
	 * @param alarm
	 */
	public void removeAlarm(int ID)
	{
	    for (int i = 0; i < alarms.size(); i++)
	    {
	        ClockAlarm refAlarm = (ClockAlarm)alarms.get(i);
	        if (refAlarm.getID() == ID)
	        {
	            alarms.remove(i);
	        }
	    }
	}
	
	/**
	 * clone any alarms that exist
	 * @param targetClock
	 * @param refClock
	 */
	public static void copyAlarms(Clock targetClock, Clock refClock)
	{
	    for (int i = 0; i < refClock.alarms.size(); i++)
	    {
	        targetClock.alarms.add(new ClockAlarm((ClockAlarm)refClock.alarms.get(i)));
	    }
	}
	
	/**
	 * Recieve notification and Cause rendering to happen.
	 * This is the key functionality tie into the internal clock
	 * Derived Classes MUST override this functionality for specific 
	 * processing, you will want to capture the types of events you 
	 * are interested in
	 * 
	 * @param applicationEvent The application event to process
	 * Default Half second listener
	 * 
	 */
	public void listen( ApplicationEvent applicationEvent ){
		if (!this.canRender) {
			return;
		}
		try{
			int oldMinute = date.getMinute();
			date.addTime((int)desiredInterval);
			this.renderer.render();
			if (oldMinute != date.getMinute())	{
			    checkAlarms();
			}
		}catch(Exception e){
		    String errStr = ClockGUI.resources.getString("ClockError.ExceptionClockListening");
			System.out.println(errStr + " " + e.getMessage());		
		}
	}

	
	/**
	 * walk the list of configured alarms and fire any that are due on a separate thread
	 */
	protected void checkAlarms()
	{
	    for(int i = 0; i < alarms.size(); i++)
	    {
	        ClockAlarm alarm = (ClockAlarm)alarms.get(i);
	        alarm.sendAlarm(date);
	    }
	}
	
	/**
	 * Configure the time index values to reflect the timestamp
	 */
	public void configureTime(Long time, String zone)
	{
	    date.configureTime(time,zone);
	}
	
	/**
	 * Configure the time index values to reflect the timestamp
	 */
	public void configureTime()
	{
	    date.configureTime();
	}	
		
	/**
	 * @return Returns the caption.
	 */
	public String getCaption() 
	{
		return caption;
	}
	
	/**
	 * @param caption The caption to set.
	 */
	public void setCaption(String caption) 
	{
		this.caption = caption;
	}
	
	/**
	 * @return Returns the iD.
	 */
	public String getID() 
	{
		return ID;
	}
	
	/**
	 * @param id The iD to set.
	 */
	public void setID(String id) 
	{
		ID = id;
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getClockName() 
	{
		return this.clockName;
	}
	
	/**
	 * @param name The name to set.
	 */
	public void setClockName(String clockName) 
	{
		this.clockName = clockName;
	}
	
	/**
	 * @return Returns the posX.
	 */
	public int getPosX() 
	{
		return posX;
	}
	
	/**
	 * @param posX The posX to set.
	 */
	public void setPosX(int posX) 
	{
		this.posX = posX;
	}
	
	/**
	 * @return Returns the posY.
	 */
	public int getPosY() 
	{
		return posY;
	}
	
	/**
	 * @param posY The posY to set.
	 */
	public void setPosY(int posY) 
	{
		this.posY = posY;
	}
	
	/**
	 * @return Returns the previewPath.
	 */
	public String getPreviewPath() 
	{
		return previewPath;
	}
	
	/**
	 * @param previewPath The previewPath to set.
	 */
	public void setPreviewPath(String previewPath) 
	{
		this.previewPath = previewPath;
	}
	
	/**
	 * @return Returns the renderer.
	 */
	public Renderer getRenderer() 
	{
		return renderer;
	}
	
	/**
	 * @param renderer The renderer to set.
	 */
	public void setRenderer(Renderer renderer) 
	{
		this.renderer = renderer;
	}
	
	/**
	 * @return Returns the svgPath.
	 */
	public String getSvgPath() 
	{
		return svgPath;
	}
	
	/**
	 * @param svgPath The svgPath to set.
	 */
	public void setSvgPath(String svgPath) 
	{
		this.svgPath = svgPath;
	}
	
	/**
	 * @return Returns the implementationType.
	 */
	public String getImplementationType() 
	{
		return implementationType;
	}
	
	/**
	 * @param implementationType The implementationType to set.
	 */
	public void setImplementationType(String implementationType) 
	{
		this.implementationType = implementationType;
	}
	
	/**
	 * @return Returns the rendererType.
	 */
	public String getRendererType() 
	{
		return rendererType;
	}
	
	/**
	 * @param rendererType The renderType to set.
	 */
	public void setRendererType(String rendererType) 
	{
		this.rendererType = rendererType;
	}

	/**
	 * @return Returns the application.
	 */
	public ClockGUI getApplication() 
	{
		return application;
	}
	/**
	 * @param application The application to set.
	 */
	public void setApplication(ClockGUI application) 
	{
		this.application = application;
	}

	/**
	 * @return Returns the parentID.
	 */
	public String getParentID()
	{
		return ParentID;
	}
	
	/**
	 * @param parentID The parentID to set.
	 */
	public void setParentID(String parentID) 
	{
		ParentID = parentID;
	}
	
    /**
     * @return Returns the frame.
     */
    public SVG_SWTViewerFrame getFrame()
    {
        return frame;
    }
    /**
     * @param frame The frame to set.
     */
    public void setFrame(SVG_SWTViewerFrame frame)
    {
        this.frame = frame;
    }
    
    /**
     * @return Returns the allowedToScale.
     */
    public boolean isAllowedToScale()
    {
        return allowedToScale;
    }
    
    /**
     * @param allowedToScale The allowedToScale to set.
     */
    public void setAllowedToScale(boolean allowedToScale)
    {
        this.allowedToScale = allowedToScale;
    }
    
    /**
     * @return Returns the fixedAspectRatio.
     */
    public boolean isFixedAspectRatio()
    {
        return fixedAspectRatio;
    }
    
    /**
     * @param fixedAspectRatio The fixedAspectRatio to set.
     */
    public void setFixedAspectRatio(boolean fixedAspectRatio)
    {
        this.fixedAspectRatio = fixedAspectRatio;
    }
    /**
     * @return Returns the displayDate.
     */
    public boolean isDisplayDate()
    {
        return displayDate;
    }
    /**
     * @param displayDate The displayDate to set.
     */
    public void setDisplayDate(boolean displayDate)
    {
        this.displayDate = displayDate;
    }
    /**
     * @return Returns the allowedToShowDate.
     */
    public boolean isAllowedToShowDate()
    {
        return allowedToShowDate;
    }
    
    /**
     * @param allowedToShowDate The allowedToShowDate to set.
     */
    public void setAllowedToShowDate(boolean allowedToShowDate)
    {
        this.allowedToShowDate = allowedToShowDate;
    }
    
    /**
     * @return Returns the allowedToShowTime.
     */
    public boolean isAllowedToShowTime()
    {
        return allowedToShowTime;
    }
    
    /**
     * @param allowedToShowTime The allowedToShowTime to set.
     */
    public void setAllowedToShowTime(boolean allowedToShowTime)
    {
        this.allowedToShowTime = allowedToShowTime;
    }
    
    /**
     * @return Returns the usingTwelveHrFormat.
     */
    public boolean isUsingTwelveHrFormat()
    {
        return usingTwelveHrFormat;
    }
    
    /**
     * @param usingTwelveHrFormat The usingTwelveHrFormat to set.
     */
    public void setUsingTwelveHrFormat(boolean usingTwelveHrFormat)
    {
        this.usingTwelveHrFormat = usingTwelveHrFormat;
    }
    /**
     * @return Returns the onTopInFullWindowMode.
     */
    public boolean isOnTopInFullWindowMode()
    {
        return onTopInFullWindowMode;
    }
    /**
     * @param onTopInFullWindowMode The onTopInFullWindowMode to set.
     */
    public void setOnTopInFullWindowMode(boolean onTopInFullWindowMode)
    {
        this.onTopInFullWindowMode = onTopInFullWindowMode;
    }
    /**
     * @return Returns the currentHeight.
     */
    public int getCurrentHeight()
    {
        return currentHeight;
    }
    /**
     * @param currentHeight The currentHeight to set.
     */
    public void setCurrentHeight(int currentHeight)
    {
        this.currentHeight = currentHeight;
    }
    /**
     * @return Returns the currentWidth.
     */
    public int getCurrentWidth()
    {
        return currentWidth;
    }
    /**
     * @param currentWidth The currentWidth to set.
     */
    public void setCurrentWidth(int currentWidth)
    {
        this.currentWidth = currentWidth;
    }
    /**
     * @return Returns the srcHeight.
     */
    public int getSrcHeight()
    {
        return srcHeight;
    }
    /**
     * @param srcHeight The srcHeight to set.
     */
    public void setSrcHeight(int srcHeight)
    {
        this.srcHeight = srcHeight;
    }
    /**
     * @return Returns the srcWidth.
     */
    public int getSrcWidth()
    {
        return srcWidth;
    }
    /**
     * @param srcWidth The srcWidth to set.
     */
    public void setSrcWidth(int srcWidth)
    {
        this.srcWidth = srcWidth;
    }

    /**
     * @return Returns the alarms.
     */
    public Vector<ClockAlarm> getAlarms()
    {
        return alarms;
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
    /**
     * @return Returns the isFullWindow.
     */
    public boolean isFullWindow()
    {
        return isFullWindow;
    }
    /**
     * @param isFullWindow The isFullWindow to set.
     */
    public void setFullWindow(boolean isFullWindow)
    {
        this.isFullWindow = isFullWindow;
    }
    /**
     * @return Returns the mainClockEventType.
     */
    public String getMainClockEventType()
    {
        return mainClockEventType;
    }
    /**
     * @param mainClockEventType The mainClockEventType to set.
     */
    public void setMainClockEventType(String mainClockEventType)
    {
        this.mainClockEventType = mainClockEventType;
    }
    /**
     * @return Returns the category.
     */
    public String getCategory()
    {
        return category;
    }
    /**
     * @param category The category to set.
     */
    public void setCategory(String category)
    {
        this.category = category;
    }
    /**
     * @return Returns the cycleClocks.
     */
    public boolean isCycleClocks()
    {
        return cycleClocks;
    }
    /**
     * @param cycleClocks The cycleClocks to set.
     */
    public void setCycleClocks(boolean cycleClocks)
    {
        this.cycleClocks = cycleClocks;
    }
    /**
     * @return Returns the cycleGroup.
     */
    public String getCycleGroup()
    {
        return CycleGroup;
    }
    /**
     * @param cycleGroup The cycleGroup to set.
     */
    public void setCycleGroup(String cycleGroup)
    {
        CycleGroup = cycleGroup;
    }
    /**
     * @return Returns the cycleGroupLimit.
     */
    public boolean isCycleGroupLimit()
    {
        return CycleGroupLimit;
    }
    /**
     * @param cycleGroupLimit The cycleGroupLimit to set.
     */
    public void setCycleGroupLimit(boolean cycleGroupLimit)
    {
        CycleGroupLimit = cycleGroupLimit;
    }
    /**
     * @return Returns the cyclePeriod.
     */
    public int getCyclePeriod()
    {
        return cyclePeriod;
    }
    /**
     * @param cyclePeriod The cyclePeriod to set.
     */
    public void setCyclePeriod(int cyclePeriod)
    {
        this.cyclePeriod = cyclePeriod;
    }
    
	public int getHourHandX() {
		return hourHandX;
	}

	public void setHourHandX(int hourHandX) {
		this.hourHandX = hourHandX;
	}

	public int getHourHandY() {
		return hourHandY;
	}

	public void setHourHandY(int hourHandY) {
		this.hourHandY = hourHandY;
	}

	public int getMinHandX() {
		return minHandX;
	}

	public void setMinHandX(int minHandX) {
		this.minHandX = minHandX;
	}

	public int getMinHandY() {
		return minHandY;
	}

	public void setMinHandY(int minHandY) {
		this.minHandY = minHandY;
	}

	public int getSecHandX() {
		return secHandX;
	}

	public void setSecHandX(int secHandX) {
		this.secHandX = secHandX;
	}

	public int getSecHandY() {
		return secHandY;
	}

	public void setSecHandY(int secHandY) {
		this.secHandY = secHandY;
	}

	public float getTimerMinuteHandX() {
		return timerMinuteHandX;
	}

	public void setTimerMinuteHandX(float timerMinuteHandX) {
		this.timerMinuteHandX = timerMinuteHandX;
	}

	public float getTimerMinuteHandY() {
		return timerMinuteHandY;
	}

	public void setTimerMinuteHandY(float timerMinuteHandY) {
		this.timerMinuteHandY = timerMinuteHandY;
	}

	public float getTimerSecondHandX() {
		return timerSecondHandX;
	}

	public void setTimerSecondHandX(float timerSecondHandX) {
		this.timerSecondHandX = timerSecondHandX;
	}

	public float getTimerSecondHandY() {
		return timerSecondHandY;
	}

	public void setTimerSecondHandY(float timerSecondHandY) {
		this.timerSecondHandY = timerSecondHandY;
	}

	public float getTimerDegreePerSecond() {
		return timerDegreePerSecond;
	}

	public void setTimerDegreePerSecond(float timerDegreePerSecond) {
		this.timerDegreePerSecond = timerDegreePerSecond;
	}

	public float getTimerDegreePerFractionalSecond() {
		return timerDegreePerFractionalSecond;
	}

	public void setTimerDegreePerFractionalSecond(float timerDegreePerFractionalSecond) {
		this.timerDegreePerFractionalSecond = timerDegreePerFractionalSecond;
	}

	public float getDegreePerHour() {
		return degreePerHour;
	}

	public void setDegreePerHour(float degreePerHour) {
		this.degreePerHour = degreePerHour;
	}

	public float getDegreePerMinute() {
		return degreePerMinute;
	}

	public void setDegreePerMinute(float degreePerMinute) {
		this.degreePerMinute = degreePerMinute;
	}

	public float getDegreePerMinutePerHour() {
		return degreePerMinutePerHour;
	}

	public void setDegreePerMinutePerHour(float degreePerMinutePerHour) {
		this.degreePerMinutePerHour = degreePerMinutePerHour;
	}

	public float getDegreePerSecond() {
		return degreePerSecond;
	}

	public void setDegreePerSecond(float degreePerSecond) {
		this.degreePerSecond = degreePerSecond;
	}

	public float getDegreePerFractionalSecond() {
		return degreePerFractionalSecond;
	}

	public void setDegreePerFractionalSecond(float degreePerFractionalSecond) {
		this.degreePerFractionalSecond = degreePerFractionalSecond;
	}

	public long getDesiredInterval() {
		return desiredInterval;
	}

	public void setDesiredInterval(long desiredInterval) {
		this.desiredInterval = desiredInterval;
	}

	public int getStrengthIndX() {
		return strengthIndX;
	}

	public void setStrengthIndX(int strengthIndX) {
		this.strengthIndX = strengthIndX;
	}

	public int getStrengthIndY() {
		return strengthIndY;
	}

	public void setStrengthIndY(int strengthIndY) {
		this.strengthIndY = strengthIndY;
	}

	public float getStrengthBaseDegrees() {
		return strengthBaseDegrees;
	}

	public void setStrengthBaseDegrees(float strengthBaseDegrees) {
		this.strengthBaseDegrees = strengthBaseDegrees;
	}

	public int getMoonPhaseDialX() {
		return moonPhaseDialX;
	}

	public void setMoonPhaseDialX(int moonPhaseDialX) {
		this.moonPhaseDialX = moonPhaseDialX;
	}

	public int getMoonPhaseDialY() {
		return moonPhaseDialY;
	}

	public void setMoonPhaseDialY(int moonPhaseDialY) {
		this.moonPhaseDialY = moonPhaseDialY;
	}

	public float getDegreesPerMoonPhase() {
		return degreesPerMoonPhase;
	}

	public void setDegreesPerMoonPhase(float degreesPerMoonPhase) {
		this.degreesPerMoonPhase = degreesPerMoonPhase;
	}

	public float getDegreePerDayOfMonth() {
		return degreePerDayOfMonth;
	}

	public void setDegreePerDayOfMonth(float degreePerDayOfMonth) {
		this.degreePerDayOfMonth = degreePerDayOfMonth;
	}

	public float getDegreePerMonthOfYear() {
		return degreePerMonthOfYear;
	}

	public void setDegreePerMonthOfYear(float degreePerMonthOfYear) {
		this.degreePerMonthOfYear = degreePerMonthOfYear;
	}

	public float getDegreePerDayOfWeek() {
		return degreePerDayOfWeek;
	}

	public void setDegreePerDayOfWeek(float degreePerDayOfWeek) {
		this.degreePerDayOfWeek = degreePerDayOfWeek;
	}

	public MoonPhaseCalculator getMoonCalc() {
		return moonCalc;
	}

	public void setMoonCalc(MoonPhaseCalculator moonCalc) {
		this.moonCalc = moonCalc;
	}
	
	public float getDegreePerSecondPerHour() {
		return degreePerSecondPerHour;
	}

	public void setDegreePerSecondPerHour(float degreePerSecondPerHour) {
		this.degreePerSecondPerHour = degreePerSecondPerHour;
	}

}
