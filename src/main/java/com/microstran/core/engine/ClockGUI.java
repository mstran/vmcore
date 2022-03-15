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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JProgressBar;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.resources.ResourceManager;

import com.microstran.core.clock.Clock;
import com.microstran.core.clock.ClockDisplayElement;
import com.microstran.core.engine.util.ExecEnvironment;
import com.microstran.core.engine.util.MessageLocalizer;
import com.microstran.core.engine.util.MsgBox;
import com.microstran.core.event.ApplicationEvent;
import com.microstran.core.event.ApplicationEventServer;
import com.microstran.core.event.clock.WindowChangeEvent;
import com.microstran.core.viewer.AboutDialog;
import com.microstran.core.viewer.SVG_SWTViewerFrame;

/**
 * Main application class for our clock apps
 * 
 * @author Mike Stran
 *
 */
public class ClockGUI implements com.microstran.core.event.ApplicationEventListener
{
	
	/**
	 * where to find the root for SVG resources
	 */
	public static final String SVG_RESOURCES = "resources";
	
	/**
	 * Icon to use for a frame window
	 */
	public static ImageIcon frameIcon = null;
	
	
	public static final int BOTTOM_MESSAGE_BAR_SIZE = 25;
	
    /**
     * The resource bundle
     */
    public static ResourceBundle bundle;

	public static String resourcesPath;
	public static String workingDirectory;
    static {
    	Path path = FileSystems.getDefault().getPath("").toAbsolutePath();
		workingDirectory = path.toString();
    	resourcesPath = workingDirectory +'/'+"resources";
    }
    
    /**
     * The resource manager - load the properties
     */
    public static ResourceManager resources;
    static 
    {
    	try {
    		File fl = new File(resourcesPath);
    		URL[] urls  = {fl.toURI().toURL()};
    		ClassLoader loader = new URLClassLoader(urls);
    		bundle = ResourceBundle.getBundle(MessageLocalizer.MESSAGES, Locale.getDefault(), loader);
    		resources = new ResourceManager(bundle);
    	}catch(Throwable t) {
    		System.out.println("Startup failure obtaining resources: "+t.getMessage() + " From: "+resourcesPath);    	
    	}
    }
    
	/**
     * cache the alarms
     */	
    public static Vector<String> alarmSounds = new Vector<String>();
    
    /**
     * load up all wav files from the jar 
     */
    static
	{
	    try
	    {
	    	File f = new File(resourcesPath + "/alarms");
	    	ArrayList<String> names = new ArrayList<String>(Arrays.asList(f.list()));
	    	Iterator<String>It = names.iterator();
	    	while (It.hasNext()){
	    		String name = It.next();
	            if (name.indexOf(".wav") != -1 ){
	                alarmSounds.add(name.substring((name.lastIndexOf('/') + 1)));
	            }
	        }
	    }
	    catch (Exception e)
	    {
	        String errStr = resources.getString("ClockError.ExceptionListingResources");
            System.out.println(errStr + " " + e.getMessage());
	    }
	}
    
    /**
     * Quick boolean test for if this is windows platform
     */
    public static boolean isWindowsPlatform = true;
	
    static 
    	{
            if ((ExecEnvironment.OSName.indexOf("Windows") == -1))
	            isWindowsPlatform = false;

            System.out.println("Starting up with Java Version: " + ExecEnvironment.javaVersion);
    		System.out.println("Java Vendor " + ExecEnvironment.javaVendor);
        }
        
    /**
     * Linked list of all clock viewers
     */
    protected static List<SVG_SWTViewerFrame> clockViewers = new ArrayList<SVG_SWTViewerFrame>();
	
    /**
     * startup splash screen
     */
    public static AboutDialog splashScreen = null;
    
    /**
     * Progress bar for startup
     */
    public static JProgressBar progressBar = null;

    /**
     * progress bar tracker value
     */
    public static int pbTracker = 0;
    
    /**
     * Tracking some startup conditions
     */
    protected static boolean startup = true;
    
    /**
     * Number of screens detected
     */
    public static int numberofScreens = 0;
    /**
     * list of resolutions per screen 
     * (rectangles that include size and positional information)
     */
    protected static ArrayList<Rectangle> screenRes;

    /**
     * Set of manifest constants to control max number of clocks
     * based on system memory constraints 
     */
    public static final int MAX_NUM_CLOCKS_36MB  = 8;
    public static final int MAX_NUM_CLOCKS_64MB  = 10;
    public static final int MAX_NUM_CLOCKS_128MB = 20;
    public static final int MAX_NUM_CLOCKS_256MB = 50;
    
    /**
     * groups as defined by the definitions.xml file
     * Miscellaneous through pocketwatches
     */
    public static final String POCKETWATCH_GROUP = "Antique Pocket Watch";
    public static final String AIRCRAFT_GROUP = "Aviation";
    public static final String MARINE_CHRONOMETER_GROUP = "Ship Chronometer";
    public static final String WRISTWATCH_GROUP = "Wrist Watch";
    public static final String DESKTOP_GROUP = "Desktop Clock";
    public static final String VMCC_GROUP = "VMCC Clock";
    public static final String MISCELLANEOUS_GROUP = "Miscellaneous";
    
    public static List<String> clockCategories = new ArrayList<String>();
    static
    {
    	clockCategories.add(VMCC_GROUP);
    	clockCategories.add(DESKTOP_GROUP);
        clockCategories.add(MISCELLANEOUS_GROUP);
    	clockCategories.add(POCKETWATCH_GROUP);
        clockCategories.add(AIRCRAFT_GROUP);
        clockCategories.add(MARINE_CHRONOMETER_GROUP);
        clockCategories.add(WRISTWATCH_GROUP);
    }
    
    /**
     * vector of available clocks
     */
    protected List<Clock> activeClocks = null;
	
    private StringBuffer lockObj = new StringBuffer();
	
    ////////////////////////////////////////////////////////////////////////////////////////////
    //
    //		Application Methods
    //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public ClockGUI()
    {
        boolean acceptable = true;
        acceptable =  true; //checkOperatingEnvironment();
        if (!acceptable)
        {
            System.out.println("Unacceptable system configuration, exiting");
            System.exit(0);
        }
        ApplicationEventServer.instance().register(this, WindowChangeEvent.NEW_VIEWER_EVENT);
        ApplicationEventServer.instance().register(this, WindowChangeEvent.NEXT_VIEWER_EVENT);
        ApplicationEventServer.instance().register(this, WindowChangeEvent.PREVIOUS_VIEWER_EVENT);
        ApplicationEventServer.instance().register(this, WindowChangeEvent.F10_EVENT);
        ApplicationEventServer.instance().register(this, WindowChangeEvent.VIEWER_RESIZE_EVENT);
        ApplicationEventServer.instance().register(this, WindowChangeEvent.F12_EVENT);
        ApplicationEventServer.instance().register(this, WindowChangeEvent.VIEWER_CLOSE_EVENT);
        ApplicationEventServer.instance().register(this, WindowChangeEvent.DIALOG_SELECT_CLOCK);
        ApplicationEventServer.instance().register(this, WindowChangeEvent.CYCLE_CLOCK_EVENT);
        ApplicationEventServer.instance().register(this, WindowChangeEvent.UPDATE_VIEWER_EVENT);
        SVG_SWTViewerFrame.setClockGUI(this);
    }
	
    /**
     * Unregister this instance
     */
    public void unRegisterClock()
    {
        ApplicationEventServer.instance().unregister(this, WindowChangeEvent.NEW_VIEWER_EVENT);
        ApplicationEventServer.instance().unregister(this, WindowChangeEvent.NEXT_VIEWER_EVENT);
        ApplicationEventServer.instance().unregister(this, WindowChangeEvent.PREVIOUS_VIEWER_EVENT);
        ApplicationEventServer.instance().unregister(this, WindowChangeEvent.F10_EVENT);
        ApplicationEventServer.instance().unregister(this, WindowChangeEvent.VIEWER_RESIZE_EVENT);
        ApplicationEventServer.instance().unregister(this, WindowChangeEvent.F12_EVENT);
        ApplicationEventServer.instance().unregister(this, WindowChangeEvent.VIEWER_CLOSE_EVENT);
        ApplicationEventServer.instance().unregister(this, WindowChangeEvent.DIALOG_SELECT_CLOCK);
        ApplicationEventServer.instance().unregister(this, WindowChangeEvent.CYCLE_CLOCK_EVENT);
        ApplicationEventServer.instance().unregister(this, WindowChangeEvent.UPDATE_VIEWER_EVENT);
        SVG_SWTViewerFrame.setClockGUI(null);
    }
    
    /* (non-Javadoc)
     * @see com.microstran.core.event.ApplicationEventListener#listen(com.microstran.core.event.ApplicationEvent)
     */
    public void listen( ApplicationEvent applicationEvent )
	{
        SVG_SWTViewerFrame frame = (SVG_SWTViewerFrame)applicationEvent.getEventData();
        
        if (applicationEvent.getApplicationEventType().equals(WindowChangeEvent.NEW_VIEWER_EVENT))
        {
            synchronized(lockObj)
            {
                createNewViewerWindow(frame.getClock(), true, true, true, true, Color.LIGHT_GRAY, Color.WHITE);
            }
        }
        else if (applicationEvent.getApplicationEventType().equals(WindowChangeEvent.PREVIOUS_VIEWER_EVENT))
        {
            synchronized(lockObj)
            {
                showPreviousClock(frame);
            }
        }
        else if (applicationEvent.getApplicationEventType().equals(WindowChangeEvent.NEXT_VIEWER_EVENT))
        {
            synchronized(lockObj)
            {
                showNextClock(frame, false);
            }
        }
        else if (applicationEvent.getApplicationEventType().equals(WindowChangeEvent.F10_EVENT))
        {
            synchronized(lockObj)
            {
                switchFullWindowMode(frame);
            }
        }
        else if (applicationEvent.getApplicationEventType().equals(WindowChangeEvent.VIEWER_CLOSE_EVENT))
        {
            closeSVG_SWTViewerFrame(frame, true);
        }
        else if (applicationEvent.getApplicationEventType().equals(WindowChangeEvent.F12_EVENT))
        {
            closeSVG_SWTViewerFrame(null, true);
        }
        else if (applicationEvent.getApplicationEventType().equals(WindowChangeEvent.DIALOG_SELECT_CLOCK))
        {
            showClock(frame, frame.getClock().getParentID(), true, true, true, true, Color.LIGHT_GRAY, Color.WHITE);
        }
        else  if (applicationEvent.getApplicationEventType().equals(WindowChangeEvent.CYCLE_CLOCK_EVENT))
        {
            boolean stayInGroup = frame.getClock().isCycleGroupLimit();
            showNextClock(frame, stayInGroup);
        }
        else if (applicationEvent.getApplicationEventType().equals(WindowChangeEvent.UPDATE_VIEWER_EVENT))
        {
            synchronized(lockObj)
            {
            	resetWindow(frame);
            }
        }
 	}
    
    /**  
     * Callback from fame when user presses the new clock window button
     * @param clock
     */
    public void createNewViewerWindow(Clock clock, boolean makeVisible, boolean showToolbar, 
            boolean showMessageBar, boolean drawBorder, Color frameColor, Color messageBarColor)
    {
        try
        {
            //you can use the old clock here as the source for the new clock since the
            //clock and render types are the same
	        Clock newClock = Clock.createClock(clock);
	        Clock.copyRuntimeSettings(newClock,clock,newClock.getID());
			//do not overlay new clocks 
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	        int posX = newClock.getPosX() + 25;
			if (posX >= screenSize.width)
			    posX -= 50;
			int posY = newClock.getPosY() + 25;
			if (posY >= screenSize.height)
			    posY -= 50;
			
			newClock.setPosX(posX);
			newClock.setPosY(posY);
		    SVG_SWTViewerFrame newFrame = CreateClockFrameSetClock(newClock,showToolbar,showMessageBar,drawBorder,frameColor, messageBarColor);
            if (makeVisible)
            {
                newFrame.setVisible(true);
                newFrame.getSvgCanvas().requestFocusInWindow();
            }
	    } 
        catch (Exception e) 
        {
	        String errStr = resources.getString("ClockError.ExceptionCreatingNewViewer");
            System.out.println(errStr + " " + e.getMessage());
        }
    }

    /**
	 * Frame will use this callback to remove the existing clock and replace it with 
	 * the next one
	 * @param frame
	 * @param stayInCurrentGroup, should we limit the next clock to the current group?
	 */
	public void showNextClock(SVG_SWTViewerFrame frame, boolean stayInCurrentGroup)
	{
        try
        {
		    Clock oldClock = frame.getClock();
		    if (stayInCurrentGroup)
		    {
		        Map<String, Clock> clockGroup = EngineHelper.getClocksForGroup(oldClock.getCycleGroup());
		        //find the ID in the group, if at end start over
			    String oldID = frame.getClock().getID();
		    	String newID = EngineHelper.getNextClockIDForCollection(oldID, clockGroup);
				showClock(frame, newID, true, true, true, true, Color.LIGHT_GRAY, Color.WHITE);
		        return;
		    }
		    else
		    {
			    String oldID = frame.getClock().getID();
		    	Map<String,Clock>allClocks = EngineHelper.getAllDefinedClocks();
			    String newID = EngineHelper.getNextClockIDForCollection(oldID, allClocks);
				showClock(frame, newID, true, true, true, true, Color.LIGHT_GRAY, Color.WHITE);
		    }
	    } 
        catch (Exception e) 
        {
	        String errStr = resources.getString("ClockError.ExceptionShowingNextClock");
            System.out.println(errStr + " " + e.getMessage());
        }
	}
	
	/**
	 * Frame will use this callback to remove the existing clock and replace it with 
	 * the previous one
	 * @param frame
	 */
	public void showPreviousClock(SVG_SWTViewerFrame frame)
	{
        try
        {
		    String oldID = frame.getClock().getID();
	    	Map<String,Clock>allClocks = EngineHelper.getAllDefinedClocks();
		    String ID = EngineHelper.getPreviousClockIDForCollection(oldID, allClocks);
			showClock(frame, ID, true, true, true, true, Color.LIGHT_GRAY, Color.WHITE);
        } 
        catch (Exception e) 
        {
	        String errStr = resources.getString("ClockError.ExceptionShowingPreviousClock");
            System.out.println(errStr + " " + e.getMessage());
        }
	}

    /**
     * Switch to and from full window viewing mode
     * @param frame
     */
    public void switchFullWindowMode(SVG_SWTViewerFrame frame)
    {
        try
        {
	        Clock clock = frame.getClock();
	        boolean isFullWindow = clock.isFullWindow() == true ? false : true;
	        clock.setFullWindow(isFullWindow);

	        Clock newClock = Clock.createClock(clock);
	        Clock.copyRuntimeSettings(newClock,clock,newClock.getID());
			
	        SVG_SWTViewerFrame newFrame = CreateClockFrameSetClock(newClock,true,true,true,Color.LIGHT_GRAY, Color.WHITE);
	        newFrame.setVisible(true);
	        //take the original frame out of service
	        deactivateFrame(frame, true);
	        newFrame.getSvgCanvas().requestFocusInWindow();
	    }
        catch(Exception ex)
        {
	        String errStr = resources.getString("ClockError.ExceptionSwitchingViewerMode");
            System.out.println(errStr + " " + ex.getMessage());
        }
    }
	
    /**
     * Use this to reset the frame for certain operations that would require it
     * such as adding/removing the bottom time panel or switching between AM/PM
     * @param frame
     */
    public void resetWindow(SVG_SWTViewerFrame frame)
    {
        try
        {
	        Clock clock = frame.getClock();
	        Clock newClock = Clock.createClock(clock);
	        Clock.copyRuntimeSettings(newClock,clock,newClock.getID());
			
	        SVG_SWTViewerFrame newFrame = CreateClockFrameSetClock(newClock,true,true,true,Color.LIGHT_GRAY, Color.WHITE);
	        newFrame.setVisible(true);
	        //take the original frame out of service
	        deactivateFrame(frame, true);
	        newFrame.getSvgCanvas().requestFocusInWindow();
	    }
        catch(Exception ex)
        {
	        String errStr = resources.getString("ClockError.ExceptionSwitchingViewerMode");
            System.out.println(errStr + " " + ex.getMessage());
        }
    }
    
    
	/**
	 * Load up only the clock definitions
	 * @param clockGUI
	 */
	public void LoadDefinitionsOnly(ClockGUI clockGUI)
	{
		//Read configuration and build required clocks
		EngineHelper.getAllClockDefinitions(clockGUI);
		ApplicationEventServer.instance().register(new AlarmEventListener(),"AlarmEvent");
	}

	/**
	 * Load up the clock definitions and the active configured clocks
	 * @param clockGUI
	 */
	public void LoadDefinitionsAndClocks(ClockGUI clockGUI)
	{
		//Read configuration and build required clocks
		EngineHelper.getAllClockDefinitions(clockGUI);
		activeClocks = EngineHelper.getAllConfiguredClocks(clockGUI);
		ApplicationEventServer.instance().register(new AlarmEventListener(),"AlarmEvent");
	}
	
	/**
     * 	Method to imcrement progress bar as the individual clocks get created
     */
    protected void frameLoaded()
    {
        try
        {
            if (startup == false)
	            return;
	        
            if (splashScreen != null)
	        {
		        progressBar.setValue(++pbTracker);
			    if (progressBar.getMaximum() == pbTracker)
		        {
			        splashScreen.dispose();
		            startup = false;
		            splashScreen = null;
		        }
	        }
	    } 
	    catch (Exception e) 
		{
	        String errStr = resources.getString("ClockError.ExceptionFrameLoaded");
            System.out.println(errStr + " " + e.getMessage());

            if (splashScreen != null)
	            splashScreen.dispose();
            startup = false;
		}
    }
    
	/**
	 * Will close a given frame, when last frame is closed application exits
	 * @param frame
	 */
	public  void closeSVG_SWTViewerFrame(SVG_SWTViewerFrame frame, boolean saveSettings) 
	{
	    try
	    {
		    //if the exit action is calling us or this is the last
		    //viewer then we will write out the settings
		    if ((frame == null) || clockViewers.size() == 1)
		    {
		        if (saveSettings)
		            saveApplicationSettings();
	            for (int i = 0; i < clockViewers.size(); i++)
	            {
	                SVG_SWTViewerFrame vFrame = (SVG_SWTViewerFrame)clockViewers.get(i);
	                deactivateFrame(vFrame, true);
	            }
	            System.exit(0);
		    }
		    else
		    {
		        deactivateFrame(frame, true);
		    }
	    } 
	    catch (Exception e) 
		{
	        String errStr = resources.getString("ClockError.ExceptionClosingViewer");
            System.out.println(errStr + " " + e.getMessage());
		}
    }
	
	/**
	 * show a clock in a frame
	 * @param frame
	 * @param ID
	 */
	protected void showClock(SVG_SWTViewerFrame frame, String IDToUseForNewClock, boolean makeVisible, boolean showToolbar, 
            boolean showMessageBar, boolean drawBorder, Color frameColor, Color messageBarColor)
	{
	    try
	    {
		    Map<String, Clock> allDefinedClocks = EngineHelper.getAllDefinedClocks();
		    if (IDToUseForNewClock!=null) 
		    {
			    Clock oldClock = frame.getClock();
			    Clock newClock = Clock.createClock(allDefinedClocks.get(IDToUseForNewClock));
		        
			    //NOTE!!!! new clock will NOT have run time data so we will
			    //need to populate it from the old clock, things like time zone, position, etc...
		        Clock.copyRuntimeSettings(newClock, oldClock, newClock.getID());
		        
		        SVG_SWTViewerFrame newFrame = CreateClockFrameSetClock(newClock,showToolbar, showMessageBar,drawBorder,frameColor, messageBarColor);
	            if (makeVisible)
	            {
	                newFrame.setVisible(true);
	                newFrame.getSvgCanvas().requestFocusInWindow();
	            }

		        //take the original frame out of service
		        deactivateFrame(frame, true);
		    }
	    } 
	    catch (Exception e) 
    	{
	        String errStr = resources.getString("ClockError.ExceptionShowingClock");
            System.out.println(errStr + " " + e.getMessage());
    	}
	}

	/**
	 * @param file
	 * @return a correctly formatted string for a file in a jar
	 */
	protected URL getJarURL(String file)
	{
		try
		{
			return(this.getClass().getResource(file));
		}
		catch(Exception e)
		{
	        String errStr = resources.getString("ClockError.ExceptionGeneratingJarURL");
            System.out.println(errStr + " " + e.getMessage());
		}
		return(null);
	}
	

    /**
     * Create a new clock frame and add the clock
     * @param newClock
     * @param hasToolbar
     * @param hasMessageBar
     * @param hasBorder
     * @param backgroundColor
     * @return
     */
    protected SVG_SWTViewerFrame CreateClockFrameSetClock(Clock newClock,boolean hasToolbar, 
            boolean hasMessageBar,  boolean hasBorder, Color backgroundColor, Color messageBarColor)
    {
        try
        {
		    SVG_SWTViewerFrame newFrame = new SVG_SWTViewerFrame(newClock, hasToolbar, hasMessageBar, hasBorder, false, backgroundColor, messageBarColor);
		    newClock.setFrame(newFrame);
		    newFrame.setIconImage(frameIcon.getImage());
		    String title = newClock.getCaption();
		    if (title.length() == 0)
		        title = resources.getString("Frame.title");
		    newFrame.setTitle(title);
		    clockViewers.add(newFrame);
		  
		    String clockURL = newClock.getSvgPath();
			String ID = newClock.getID();
			Map<String, Clock>allClocks = EngineHelper.getAllDefinedClocks();
			Iterator<String>It = allClocks.keySet().iterator();
			String firstKey =  It.next();
			if (firstKey.equals(ID))
		        newFrame.setHasPrevious(false);
		    else
		        newFrame.setHasPrevious(true);
		
			String lastKey = null;
			while (It.hasNext()) {
				lastKey=It.next();
			}
			if (lastKey.equals(ID)) {
				newFrame.setHasNext(false);
			}
		    else {
		        newFrame.setHasNext(true);
		    }
		    // set us to saved location
	        newFrame.setLocation(newClock.getPosX(), newClock.getPosY());

	        //actually load the document up 
		    newFrame.showSVGDocument(resourcesPath + clockURL);
		    
	        //We need to do this on the correct thread of control, so 
		    //call show SVGDocument which causes the document to load 
		    //and become visible (off screen) but we must block and wait on this
		    //thread untill the update manager loads otherwise get intermittent 
	        //update manager startup failures which stops rendering
		    newFrame.isManagerRunning();
		    
		    //get dimensions first, show document second, size frame third
	        Dimension curDimCanvas = newFrame.getSvgCanvas().getSize();
	        int widthDif = newClock.getCurrentWidth() - curDimCanvas.width;
	        int heightDif = newClock.getCurrentHeight() - curDimCanvas.height;
	        Dimension curDimFrame = newFrame.getSize();
			frameLoaded();
	        newFrame.setInitialized(true);
	        newFrame.setSize((curDimFrame.width + widthDif), (curDimFrame.height + heightDif));
	        syncClockToFrame(newClock,newFrame);
	        
	        //check if resizing, then make sure you render
	        newFrame.isResizingNow();
	        for(int i = 0; i < 3; i++)
	        {
	            newFrame.setRenderedOnce(false);
	            newFrame.isRenderedOnce();
	        }
	        return(newFrame);
    	}
        catch (Exception e) 
    	{
	        String errStr = resources.getString("ClockError.ExceptionMainCreateMethod");
            System.out.println(errStr + " " + e.getMessage());
    	}
        return(null);
    }    
 
    /**
     * use this for frame drawing for screen savers
     * @param targetRect
     * @param newClock
     * @param hasMessageBar
     * @param hasBorder
     * @param backgroundColor
     * @return
     */
    protected SVG_SWTViewerFrame CreateSSFrameSetClock(Rectangle targetRect, Clock newClock, boolean hasMessageBar,  boolean hasBorder, Color backgroundColor, Color messageBarColor)
    {
        try
        {
            SVG_SWTViewerFrame newFrame = new SVG_SWTViewerFrame(newClock, false, hasMessageBar, hasBorder, true, backgroundColor, messageBarColor);
		    newClock.setFrame(newFrame);
		    newFrame.setIconImage(frameIcon.getImage());
		    String title = newClock.getCaption();
		    if (title.length() == 0)
		        title = resources.getString("Frame.title");
		    newFrame.setTitle(title);
		    
		    String clockURL = newClock.getSvgPath();
			
		    // set us to saved location
	        newFrame.setLocation(newClock.getPosX(), newClock.getPosY());

	        //actually load the document up 
		    newFrame.showSVGDocument(resourcesPath + clockURL);
		    
	        //We need to do this on the correct thread of control, so 
		    //call show SVGDocument which causes the document to load 
		    //and become visible (off screen) but we must block and wait on this
		    //thread untill the update manager loads otherwise get intermittent 
	        //update manager startup failures which stops rendering
		    newFrame.isManagerRunning();
		    frameLoaded();
		    //set document into the renderer
		    newClock.getRenderer().setDocument(newFrame.getSVGDocument());
	        
		    //reset the time so it starts out correct
	        newClock.configureTime();
		    newClock.getRenderer().reset();
	
		    newFrame.setInitialized(true);
	        
	        newFrame.setLocation(targetRect.x, targetRect.y);
	        newFrame.setSize(targetRect.width, targetRect.height);
	        
	        return(newFrame);
    	}
        catch (Exception e) 
    	{
	        String errStr = resources.getString("ClockError.ExceptionMainCreateMethod");
            System.out.println(errStr + " " + e.getMessage());
    	}
        return(null);
    }    

    
    
    /**
     * Check if there are clocks to display
     * @param screenElements
     * @return
     */
    protected boolean hasClocksToDisplay(ClockDisplayElement[][] screenElements)
    {
        try
        {
	        for (int j = 0; j < screenElements.length; j++) 
	        {
	            for(int k = 0; k < screenElements[j].length; k++)
	            {
	                ClockDisplayElement element = screenElements[j][k];
	                if (element.isVisible() == false)
	                    return(true);
	            }
	        }
    	}
        catch(Exception e)
        {
            String errStr = resources.getString("ClockError.ExceptionCheckingAvailableClocksArray");
            System.out.println(errStr + " " + e.getMessage());
        }
 
        return(false);
    }
    
    /**
     * Generate a caption for the window based on the time zone
     * @param timeZone
     * @return
     */
    public static String generateCaption(String timeZone)
    {
        //take the time zone and replace instances of "_" and "/" with " "
        String message = timeZone.replace('_',' ');
        message = message.replace('/',' ');
        return(message + " Time");
    }
    

    /**
     * return a timezone string
     * @return
     */
    protected int findTimeZone(String search, String[] zones)
    {
        for (int i = 0; i < zones.length; i++)
        {
            if (zones[i].indexOf(search) != -1)
                return(i);
        }
        return(-1);
    }
    
    /**
     * common routine, sets document and tries to minimize "flash" when showing as 
     * the window changes
     * @param newClock
     * @param newFrame
     */
    protected void syncClockToFrame(Clock newClock, SVG_SWTViewerFrame newFrame)
    {
	    //set document into the renderer
	    newClock.getRenderer().setDocument(newFrame.getSVGDocument());
	    newClock.getRenderer().enableRenderer(true);
        newClock.configureTime();
	    newClock.getRenderer().reset();
	    newClock.getRenderer().render();
	    newClock.startClock();
    }
    
    /**
     * Retrieve key system settings
     */
    public void getConfigurationSettings()
    {
        screenRes = new ArrayList<Rectangle>();
        numberofScreens = ExecEnvironment.getNumberScreens(); //MJS comment out get screens and set for 1 screen if DEBUGING
        System.out.println("Number of screens = " + numberofScreens);
        
        for(int i = 0; i < numberofScreens; i++)
        {
        	//boolean fullScreen = ExecEnvironment.isFullScreenModeSupported();
        	Rectangle sr = ExecEnvironment.getResolutionForScreen(i); 
        	System.out.println("Raw screen resolution for screen "+i+"= "+ sr.width + "X" + sr.height);
         	screenRes.add(sr);
        }
    }
    
    
	/**
     * 	Shutdown function, write out all clock settings for open frames
     */
    private void saveApplicationSettings()
    {
        //walk the list of open frames and update the XML document with positional 
        //and other information
        try
        {
            EngineHelper.removeRuntimeSettings();
        
            for (int i = 0; i < clockViewers.size(); i++)
            {
                SVG_SWTViewerFrame vFrame = (SVG_SWTViewerFrame)clockViewers.get(i);
                EngineHelper.writeRuntimeSettings(vFrame);
            }
        }
        catch(Exception e)
        {
	        String errStr = resources.getString("ClockError.ExceptionRemovingRuntimeSettings");
            System.out.println(errStr + " " + e.getMessage());
        }
        try
        {
            EngineHelper.writeClockConfigurationFile();
        }
        catch(Exception ex)
        {
	        String errStr = resources.getString("ClockError.ExceptionWritingSettings");
            System.out.println(errStr + " " + ex.getMessage());
        }
    }
    
    
    /**
     * deactivate the frame
     * @param frame
     */
    protected synchronized void deactivateFrame(SVG_SWTViewerFrame frame, boolean removeFromList)
	{
        try
        {
	        Clock clock = frame.getClock();
		    //we do this because the nature of the event driven system does not ensure 
	        //that 1 extra event will not slip through, this results in null pointers
	        clock.getRenderer().enableRenderer(false);
		    
	        clock.endClock();
	        frame.setVisible(false);
	        if (removeFromList)
	           	clockViewers.remove(frame);
	        
	        JSVGCanvas canvas = frame.getJSVGCanvas();
	        canvas.setCursor(null);
	        canvas.stopProcessing();
	        frame.isManagerStopped(); 
            frame.setCursor(null);
	        frame.dispose();
            frame = null;
        }
        catch (Exception e) 
        {
	        String errStr = resources.getString("ClockError.ExceptionRemovingViewer");
            System.out.println(errStr + " " + e.getMessage() +"\n");
            e.printStackTrace();
        }
	}

    /**
     * Check for minimum operating conditions, enough memory, high enough resolution
     * reasonable bit depth
     * @return
     */
    public boolean checkOperatingEnvironment()
    {
        boolean acceptable = true;
        Rectangle r = ExecEnvironment.getResolutionForTotalScreen();

        if (r.width < 800)
            acceptable = false;
        if (r.height < 600)
            acceptable = false;

        //long bitDepth = ExecEnvironment.getColorDepthForScreen(0);
        //if (bitDepth < 16)
          //  acceptable = false;

        if (!acceptable)
        {
           String errStr = resources.getString("ClockInitialize.MinimumEnvironment");
           new MsgBox(errStr, true);
        }
        
        System.out.println("Screen virtual width x height = " + r.width + "X" + r.height);
        //System.out.println("Screen pixel depth = " + bitDepth);
        int numScreens = ExecEnvironment.getNumberScreens();
        System.out.println("Number of screens = " + numScreens);
        for(int i = 0; i < numScreens; i++)
        {
            Rectangle sr = ExecEnvironment.getResolutionForScreen(i);
            System.out.println("Screen resolution for screen "+i+"= "+ sr.width + "X" + sr.height);
        }
        int totalMemory = ExecEnvironment.getTotalMemoryInMB();
        System.out.println("Total Memory (in MB) for Program = " + totalMemory);
        int maxMemory = ExecEnvironment.getMaxMemoryInMB();
        System.out.println("Maximum Memory (int MB) for Program = " + maxMemory);
        int freeMemory = ExecEnvironment.getFreeMemoryInMB();
        System.out.println("Free Memory (int MB) for Program = " + freeMemory);
        
        return(acceptable);
    }
    
    /**
     * @return Returns the screenRes.
     */
    public static ArrayList<Rectangle> getScreenRes()
    {
        return screenRes;
    }
    

}
