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

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.microstran.core.alarm.ClockAlarm;
import com.microstran.core.clock.Clock;
import com.microstran.core.clock.ClockDate;
import com.microstran.core.clock.Renderer;
import com.microstran.core.util.helper.ObjectHelper;
import com.microstran.core.viewer.SVG_SWTViewerFrame;
import com.microstran.core.xml.DOMXMLParser;
import com.microstran.core.xml.DOMXMLParserHelper;


/**
 * The engine helper class, reads and writes configuration files 
 * All static helper class functions; it's not intended that you will create these
 *
 * @author Mike Stran
 *
 */
public class EngineHelper
{
	
	private static DOMXMLParserHelper helper  = null;
	private static DOMXMLParser parser = null;
	
	private static DOMXMLParserHelper ssHelper  = null;
	private static DOMXMLParser ssParser = null;
	
	private static Map<String,Clock> allDefinedClocks = null;
	private static int width  = 0;
	private static int height = 0;
	
	/**
	 * manifest constants for the 3 configuration files
	 */
	public static String DEFINITION_FILE = "/definitions.xml";
	public static String CONFIGURATION_FILE = "/configuration.xml";
	public static String SS_CONFIGURATION_FILE = "/ss_configuration.xml";
	
	
	/**
	 * This function will attempt to parse out and load up the screen saver settings
	 * IF THIS FAILS for any reason, like, bad config or missing file then we 
	 * quietly (i.e. don't throw exceptions) go with the default values.
	 * 
	 * @param clockAPP
	 */
	public static List<String> getScreenSaverConfiguration(AbstractScreenSaver saver)
	{
	    List<String> saverList = new ArrayList<String>();
		try
	    {
		    ssParser = new DOMXMLParser();
			//THIS WILL RETURN NULL without the leading "/" !!! 
			//NOTE ALSO: IF you run this In the Debugger, this Jar file MUST be 
			//in the class path (add in the classpath tab) as well or you will get null for URL
			String defsFilePath = ClockGUI.workingDirectory + SS_CONFIGURATION_FILE;
			ssParser.setXMLSource(defsFilePath);
			ssHelper = ssParser.parseIntoDOMXMLParserHelper();
			
			Element settings = ssHelper.getRootElement();
			Element activeScreenSavers = ssHelper.getElementByTagName(settings,"activeScreenSavers");
			NodeList savers = ssHelper.getElementsByTagName(activeScreenSavers, "screenSaver");
			for (int i = 0; i < savers.getLength(); i++)
	        {
				Element ssName = (Element)savers.item(i);
				saverList.add(ssHelper.getElementText(ssName));
	        }
			Element timeSettings = ssHelper.getElementByTagName(settings,"timeSettings");
			saver.setClockRotationPeriod(Integer.parseInt(timeSettings.getAttribute("rotationPeriodInScreenSaver")));
			saver.setScreenSaverRotationPeriod(Integer.parseInt(timeSettings.getAttribute("rotationPeriodBetweenScreenSavers")));
			Element clockMaxNum = ssHelper.getElementByTagName(settings, "clockMaxNumberPerWindow");
			saver.setMaxClocksPerScreen(Integer.parseInt(clockMaxNum.getAttribute("maxnum")));
			Element groupSettings = ssHelper.getElementByTagName(settings, "groupSettings");
			saver.setLimitScreenToGroup(groupSettings.getAttribute("keepWithinGroup").equals("true"));
			AbstractScreenSaver.setCurrentClockGroupName(groupSettings.getAttribute("group"));
			Element clockSettings = ssHelper.getElementByTagName(settings,"clockSettings");
			saver.setFrameAroundClocks(clockSettings.getAttribute("frameAroundEach").equals("true"));
			saver.setBoxAroundCaption(clockSettings.getAttribute("displayBoxAroundTimeDate").equals("true"));
	    }
	    catch(Exception ex)
		{
		    ssParser = null;
		    ssHelper = null;
		    saverList.clear();
		}
	    return(saverList);
	}
	
	/**
	 * Read XML configuration in, instantiating each clock which will register to recieve 
	 * event notifications
	 * 
	 * @param dataFilePath
	 * @return
	 */
	public static void getAllClockDefinitions(ClockGUI clockAPP)
	{
		parser = new DOMXMLParser();
		
		//THIS WILL RETURN NULL without the leading "/" !!! 
		//NOTE ALSO: IF you run this In the Debugger, this Jar file MUST be 
		//in the class path (add in the classpath tab) as well or you will get null for URL
		String defsFilePath = ClockGUI.resourcesPath + '/' +  DEFINITION_FILE ;
		try {		
			parser.setXMLSourceURL(new File(defsFilePath).toURI().toURL());
			helper = parser.parseIntoDOMXMLParserHelper();
			
			Element root = helper.getRootElement();
			Element clockDefinitions = helper.getElementByTagName(root,"clockDefinitions");
			NodeList clockDefinition = helper.getElementsByTagName(clockDefinitions,"clockDefinition");
			allDefinedClocks = new LinkedHashMap<String,Clock>();
		
			for (int i = 0; i < clockDefinition.getLength(); i++) {
				//create clock
				Clock clock = new Clock();
				clock.setApplication(clockAPP);
				Element clockDef = (Element)clockDefinition.item(i);
				//get ID
				Element ID = helper.getElementByTagName(clockDef,"ID");
				String idString = helper.getElementText(ID);
				clock.setID(idString);
				//getCategory
				Element Category = helper.getElementByTagName(clockDef,"category");
				clock.setCategory(helper.getElementText(Category));
				//get Name
				Element name = helper.getElementByTagName(clockDef,"name");
				String tmpString = helper.getElementText(name);
				clock.setClockName(tmpString);
				//size and scale information
				Element size = helper.getElementByTagName(clockDef,"size");
				clock.setSrcWidth(Integer.parseInt(size.getAttribute("width")));
				clock.setSrcHeight(Integer.parseInt(size.getAttribute("height")));
				Element scale = helper.getElementByTagName(clockDef,"scale");
				String allowScale = scale.getAttribute("allowScale");
				String presAspect = scale.getAttribute("preserveAspect");
				boolean allowedToScale =  allowScale.equals("true") ? true : false;
				clock.setAllowedToScale(allowedToScale);
				boolean preservesAspect = presAspect.equals("true") ? true : false;
				clock.setFixedAspectRatio(preservesAspect);
				//event type
				Element eventType = helper.getElementByTagName(clockDef, "eventType");
				tmpString = helper.getElementText(eventType);
				clock.setMainClockEventType(tmpString);
				
				Element timeDimension = helper.getElementByTagName(clockDef, "hourHandX");
				tmpString = helper.getElementText(timeDimension);
				clock.setHourHandX(Integer.parseInt(tmpString));
				
				timeDimension = helper.getElementByTagName(clockDef, "hourHandY");
				tmpString = helper.getElementText(timeDimension);
				clock.setHourHandY(Integer.parseInt(tmpString));
				
				timeDimension = helper.getElementByTagName(clockDef, "minHandX");
				tmpString = helper.getElementText(timeDimension);
				clock.setMinHandX(Integer.parseInt(tmpString));
				
				timeDimension = helper.getElementByTagName(clockDef, "minHandY");
				tmpString = helper.getElementText(timeDimension);
				clock.setMinHandY(Integer.parseInt(tmpString));
				
				timeDimension = helper.getElementByTagName(clockDef, "secHandX");
				tmpString = helper.getElementText(timeDimension);
				clock.setSecHandX(Integer.parseInt(tmpString));
							
				timeDimension = helper.getElementByTagName(clockDef, "secHandY");
				tmpString = helper.getElementText(timeDimension);
				clock.setSecHandY(Integer.parseInt(tmpString));
				
				timeDimension = helper.getElementByTagName(clockDef, "timerMinuteHandX");
				tmpString = helper.getElementText(timeDimension);
				clock.setTimerMinuteHandX(Float.parseFloat(tmpString));
				
				timeDimension = helper.getElementByTagName(clockDef, "timerMinuteHandY");
				tmpString = helper.getElementText(timeDimension);
				clock.setTimerMinuteHandY(Float.parseFloat(tmpString));
				
				timeDimension = helper.getElementByTagName(clockDef, "timerSecondHandX");
				tmpString = helper.getElementText(timeDimension);
				clock.setTimerSecondHandX(Float.parseFloat(tmpString));
				
				timeDimension = helper.getElementByTagName(clockDef, "timerSecondHandY");
				tmpString = helper.getElementText(timeDimension);
				clock.setTimerSecondHandY(Float.parseFloat(tmpString));
				
				timeDimension = helper.getElementByTagName(clockDef, "timerSecondHandY");
				tmpString = helper.getElementText(timeDimension);
				clock.setTimerSecondHandY(Float.parseFloat(tmpString));

				timeDimension = helper.getElementByTagName(clockDef, "strengthIndX");
				tmpString = helper.getElementText(timeDimension);
				clock.setStrengthIndX(Integer.parseInt(tmpString));
				
				timeDimension = helper.getElementByTagName(clockDef, "strengthIndY");
				tmpString = helper.getElementText(timeDimension);
				clock.setStrengthIndY(Integer.parseInt(tmpString));

				timeDimension = helper.getElementByTagName(clockDef, "strengthBaseDegrees");
				tmpString = helper.getElementText(timeDimension);
				clock.setStrengthBaseDegrees(Float.parseFloat(tmpString));
				
				timeDimension = helper.getElementByTagName(clockDef, "degreePerHour");
				tmpString = helper.getElementText(timeDimension);
				clock.setDegreePerHour(Float.parseFloat(tmpString));

				timeDimension = helper.getElementByTagName(clockDef, "degreePerMinute");
				tmpString = helper.getElementText(timeDimension);
				clock.setDegreePerMinute(Float.parseFloat(tmpString));
				
				timeDimension = helper.getElementByTagName(clockDef, "degreePerMinutePerHour");
				tmpString = helper.getElementText(timeDimension);
				clock.setDegreePerMinutePerHour(Float.parseFloat(tmpString));

				timeDimension = helper.getElementByTagName(clockDef, "degreePerSecondPerHour");
				tmpString = helper.getElementText(timeDimension);
				clock.setDegreePerSecondPerHour(Float.parseFloat(tmpString));
				
				timeDimension = helper.getElementByTagName(clockDef, "degreePerFractionalSecond");
				tmpString = helper.getElementText(timeDimension);
				clock.setDegreePerFractionalSecond(Float.parseFloat(tmpString));
				
				timeDimension = helper.getElementByTagName(clockDef, "degreePerSecond");
				tmpString = helper.getElementText(timeDimension);
				clock.setDegreePerSecond(Float.parseFloat(tmpString));
				
				timeDimension = helper.getElementByTagName(clockDef, "timerDegreePerSecond");
				tmpString = helper.getElementText(timeDimension);
				clock.setTimerDegreePerSecond(Float.parseFloat(tmpString));
				
				timeDimension = helper.getElementByTagName(clockDef, "timerDegreePerFractionalSecond");
				tmpString = helper.getElementText(timeDimension);
				clock.setTimerDegreePerFractionalSecond(Float.parseFloat(tmpString));
				
				timeDimension = helper.getElementByTagName(clockDef, "moonPhaseDialX");
				tmpString = helper.getElementText(timeDimension);
				clock.setMoonPhaseDialX(Integer.parseInt(tmpString));

				timeDimension = helper.getElementByTagName(clockDef, "moonPhaseDialY");
				tmpString = helper.getElementText(timeDimension);
				clock.setMoonPhaseDialY(Integer.parseInt(tmpString));
				
				timeDimension = helper.getElementByTagName(clockDef, "degreesPerMoonPhase");
				tmpString = helper.getElementText(timeDimension);
				clock.setDegreesPerMoonPhase(Float.parseFloat(tmpString));

				timeDimension = helper.getElementByTagName(clockDef, "degreePerDayOfMonth");
				tmpString = helper.getElementText(timeDimension);
				clock.setDegreePerDayOfMonth(Float.parseFloat(tmpString));

				timeDimension = helper.getElementByTagName(clockDef, "degreePerMonthOfYear");
				tmpString = helper.getElementText(timeDimension);
				clock.setDegreePerMonthOfYear(Float.parseFloat(tmpString));
				
				timeDimension = helper.getElementByTagName(clockDef, "degreePerDayOfWeek");
				tmpString = helper.getElementText(timeDimension);
				clock.setDegreePerDayOfWeek(Float.parseFloat(tmpString));
				
				//get implementation
				Element implementation = helper.getElementByTagName(clockDef, "implementation");
				tmpString = helper.getElementText(implementation);
				clock.setImplementationType(tmpString);
				//get renderer type
				Element renderer = helper.getElementByTagName(clockDef, "renderer");
				tmpString = helper.getElementText(renderer);
				clock.setRendererType(tmpString);
				//get SVG Path
				Element SVG = helper.getElementByTagName(clockDef,"svg");
				tmpString = helper.getElementText(SVG);
				clock.setSvgPath(tmpString);
				//get preview file Path
				Element previewFile = helper.getElementByTagName(clockDef,"previewFile");
				tmpString = helper.getElementText(previewFile);
				clock.setPreviewPath(tmpString);
				//add to the array
				allDefinedClocks.put(idString, clock);
	        }
		}
		catch(Exception e)
		{
	        String errStr = ClockGUI.resources.getString("ClockError.ExceptionGettingClockDefs");
	        System.out.println(errStr + " " + e.getMessage());
		}
	}
	
    public static Clock findClockWithOrientation(Map<String, Clock> clockGroup, int currentClockHint, String searchStartClockID)
    {
        Clock clock = null;
        Iterator<String> It = clockGroup.keySet().iterator();
        boolean foundStart = false;
        while (It.hasNext()) {
        	String key = It.next();
        	if (key.equals(searchStartClockID)){
        		foundStart = true;
        		break;
        	}
        }
        if (foundStart) {
        	while (It.hasNext()) {
	        	String key = It.next();
	        	clock = clockGroup.get(key);
	        	int localHint = clock.getRenderingHint();
	            if (localHint == currentClockHint)
	            	return clock;
	        }
        }
		//start over
		It = clockGroup.keySet().iterator();
		while (It.hasNext()) {
        	String key = It.next();
        	clock = clockGroup.get(key);
        	int localHint = clock.getRenderingHint();
            if (localHint == currentClockHint)
            	return clock;
		}
        return(null);
    }
     	
	
	public static String getRandomClockFromKeys(Set<String>keys) {
        int size = keys.size();
        int item = new Random().nextInt(size); 
        int i = 0;
        String clockID = null;
        for(String key : keys)
        {
            if (i == item) {
            	clockID = key;
            	return clockID;
            }
            i++;
        }
        return null;
    }
    
	public static Map<String, Clock> getClocksForGroup(String group)
	{
	    Map<String,Clock> clockCategory = new LinkedHashMap<String,Clock>();
	    Iterator<String>It=allDefinedClocks.keySet().iterator();
	    while (It.hasNext())
	    {
	        String ID =  It.next();
	        Clock clock = allDefinedClocks.get(ID);
	        if (clock.getCategory().equals(group))
	            clockCategory.put(ID,clock);
	    }
	    return(clockCategory);
	}
	
	public static String getFirstClockIDForCollection(Map<String, Clock> allClocks) {
		return(allClocks.keySet().iterator().next());
	}
	
	public static String getNextClockIDForCollection(String currentID, Map<String, Clock> allClocks) {
		String ID = null;
		Iterator<String>It = allClocks.keySet().iterator();
		while (It.hasNext()) {
			ID = It.next();
			if (currentID.equals(ID)) {
				if (It.hasNext())
					return (It.next());
				else {
					return getFirstClockIDForCollection(allClocks);
				}
			}
		}
		return(null);
	}
	
	public static String getPreviousClockIDForCollection(String currentID, Map<String, Clock> allClocks) {
		String ID = null;
		String lastID = null;
		Iterator<String>It = allClocks.keySet().iterator();
		while (It.hasNext()) {
			ID = It.next();
			if (currentID.equals(ID)) {
				if (lastID != null)
					return (lastID);
				else {
					return getLastClockIDForCollection(allClocks);
				}
			}
			lastID = ID;	
		}
		return(null);
	}
	
	public static String getLastClockIDForCollection(Map<String, Clock> allClocks) {
		String ID = null;
		Iterator<String>It = allClocks.keySet().iterator();
		while (It.hasNext()) {
			ID = It.next();
		}
		return(ID);
	}
	
	
	public static List<Clock> getAllConfiguredClocks(ClockGUI clockAPP)
	{
	    List<Clock> clocks = new ArrayList<Clock>();
		
	    try
	    {
	        //any errors at all will result in the file being dumped and a new single clock created
	        parser = new DOMXMLParser();
	        parser.setXMLSource(ClockGUI.workingDirectory + CONFIGURATION_FILE);
	        helper = parser.parseIntoDOMXMLParserHelper();
	    	
			Element root = helper.getRootElement();
			if (root == null) //as in no file
			{
				String ID = EngineHelper.getFirstClockIDForCollection(allDefinedClocks);
				Clock clock= allDefinedClocks.get(ID);
				Object implObj  = ObjectHelper.build(clock.getImplementationType());
				Clock implClock = (Clock)implObj; 
				Clock.copyDesignTimeSettings(implClock, clock);
				implClock.setApplication(clockAPP);
				//instantiate the clocks renderer
				Object obj = ObjectHelper.build(clock.getRendererType() );
				Renderer renderer = (Renderer)obj;
				implClock.setRenderer(renderer);	
				renderer.setClock(implClock);
				
				//new date object
				ClockDate clockDate = new ClockDate();
				TimeZone zone = (TimeZone)SimpleTimeZone.getDefault();
		        String timeZoneID = zone.getDisplayName();
				clockDate.setTimeZone(timeZoneID);
				implClock.setDate(clockDate);
				String caption = ClockGUI.generateCaption(timeZoneID);
    		    implClock.setCaption(caption);
    		    implClock.setCurrentWidth(clock.getSrcWidth());
				implClock.setCurrentHeight(clock.getSrcHeight());
				implClock.setOnTopInFullWindowMode(true);
				implClock.setFullWindow(false);
				implClock.setParentID(ID);
				//get scaling attributes
				implClock.setAllowedToScale(true);
				implClock.setFixedAspectRatio(true);
				implClock.setPosX(0);
				implClock.setPosY(0);
				//Get time/date attributes
				implClock.setAllowedToShowTime(true);
				implClock.setUsingTwelveHrFormat(true);
				implClock.setAllowedToShowDate(true);
				
				clocks.add(implClock);
				
    		    return (clocks);
			}
			//Acquire all runtime settings
			Element runtimeSettings = helper.getElementByTagName(root,"runtimeSettings");
			Element activeClocks = helper.getElementByTagName(runtimeSettings,"activeClocks");
			//get screen dimensions here
			NodeList activeClock = helper.getElementsByTagName(activeClocks,"activeClock");
			
			for (int i = 0; i < activeClock.getLength(); i++)
	        {
				//match up clocks previously created and add runtime detail
				Element clockDef = (Element)activeClock.item(i);
				String parentID = clockDef.getAttribute("ParentID");
				Clock clock = null;
				
				Iterator<String> It = allDefinedClocks.keySet().iterator(); 
				while (It.hasNext())
				{
					String ID = It.next();
					clock = allDefinedClocks.get(ID);
					if (parentID.equals(clock.getID()))
						break;
				}
				//instantiate a real clock from the abstract definitions
				Object implObj  = ObjectHelper.build(clock.getImplementationType());
				Clock implClock = (Clock)implObj; 
				
				Clock.copyDesignTimeSettings(implClock, clock);
				
				implClock.setApplication(clockAPP);
				implClock.setParentID(parentID);
				//instantiate the clocks renderer
				Object obj = ObjectHelper.build(clock.getRendererType() );
				Renderer renderer = (Renderer)obj;
				implClock.setRenderer(renderer);	
				renderer.setClock(implClock);
				
				//new date object
				ClockDate clockDate = new ClockDate();
				//Timezone for CLock
				Element timeZone = helper.getElementByTagName(clockDef,"timeZone");
				String timeZoneString = helper.getElementText(timeZone);
				clockDate.setTimeZone(timeZoneString);
				implClock.setDate(clockDate);
				//caption for clock
				Element caption = helper.getElementByTagName(clockDef,"caption");
				String captionString = helper.getElementText(caption);
				implClock.setCaption(captionString);
				//get location and position information
				Element size = helper.getElementByTagName(clockDef,"size");
				implClock.setCurrentWidth(Integer.parseInt(size.getAttribute("width")));
				implClock.setCurrentHeight(Integer.parseInt(size.getAttribute("height")));
				String allowFullWindowMode = size.getAttribute("allowOnTopInFullWindowMode");
				boolean fullWindowMode =  allowFullWindowMode.equals("true") ? true : false;
				implClock.setOnTopInFullWindowMode(fullWindowMode);
				
				String isFullWindowMode = size.getAttribute("inFullWindowMode");
				boolean inFullWindowMode =  isFullWindowMode.equals("true") ? true : false;
				implClock.setFullWindow(inFullWindowMode);

				//get scaling attributes
				Element scale = helper.getElementByTagName(clockDef,"scale");
				String allowScale = scale.getAttribute("allowScale");
				String presAspect = scale.getAttribute("preserveAspect");
				boolean allowedToScale =  allowScale.equals("true") ? true : false;
				implClock.setAllowedToScale(allowedToScale);
				boolean preservesAspect = presAspect.equals("true") ? true : false;
				implClock.setFixedAspectRatio(preservesAspect);
				Element position = helper.getElementByTagName(clockDef,"position");
				implClock.setPosX(Integer.parseInt(position.getAttribute("x")));
				implClock.setPosY(Integer.parseInt(position.getAttribute("y")));
				//Get time/date attributes
				Element timeDate = helper.getElementByTagName(clockDef,"timeDate");
				String showTime = timeDate.getAttribute("showTime");
				String twelveHrDisplay = timeDate.getAttribute("twelveHourDisplay");
				String showDate = timeDate.getAttribute("showDate");
				boolean displayTime =  showTime.equals("true") ? true : false;
				implClock.setAllowedToShowTime(displayTime);
				boolean usingTwelveHrFormat = twelveHrDisplay.equals("true") ? true : false;
				implClock.setUsingTwelveHrFormat(usingTwelveHrFormat);
				boolean displayDate = showDate.equals("true") ? true : false;
				implClock.setAllowedToShowDate(displayDate);
				
				//get the clock cycling parameters
				Element cycleClock = helper.getElementByTagName(clockDef,"cycleClock");
				String cycleAllowed = cycleClock.getAttribute("cycleClocks");
				boolean cycleAllow =  cycleAllowed.equals("true") ? true : false;
				if (cycleAllow)
				{
					implClock.setCycleClocks(cycleAllow);
					implClock.setCyclePeriod(Integer.parseInt(cycleClock.getAttribute("minutes")));
					String limitToGroup = cycleClock.getAttribute("limitToGroup");
					boolean limitGroup =  limitToGroup.equals("true") ? true : false;
					implClock.setCycleGroupLimit(limitGroup);
					if (limitGroup == true)
					    implClock.setCycleGroup(cycleClock.getAttribute("group"));
					else
					    implClock.setCycleGroup("");
				}				
				else
				{
				    implClock.setCycleClocks(false);
				    implClock.setCyclePeriod(0);
				    implClock.setCycleGroupLimit(false);
				    implClock.setCycleGroup("");
				}				
			    
				//retrieve any alarms here
				Element alarms = helper.getElementByTagName(runtimeSettings,"alarms");
				NodeList clockAlarms = helper.getElementsByTagName(alarms,"alarm");
				for (int j = 0; j < clockAlarms.getLength(); j++)
			    {
				    ClockAlarm clockAlarm =  new ClockAlarm();
					ClockDate clkDate = new ClockDate();
				    //match up clocks previously created and add runtime detail
					Element alarm = (Element)clockAlarms.item(j);
					clockAlarm.setID(j);
					clockAlarm.setEnabled(alarm.getAttribute("enabled").equals("true") ? true : false);
					Element sound = helper.getElementByTagName(alarm,"sound");
					clockAlarm.setAlarmResource(sound.getAttribute("path"));
					clockAlarm.setVolume(sound.getAttribute("volume"));
					Element date = helper.getElementByTagName(alarm, "date");
				    clkDate.setYear(Integer.parseInt(date.getAttribute("year")));
				    clkDate.setMonth(Integer.parseInt(date.getAttribute("month")));
				    clkDate.setDayOfMonth(Integer.parseInt(date.getAttribute("dayOfMonth")));
					Element time = helper.getElementByTagName(alarm,"time");
					clkDate.setAmPm(time.getAttribute("amPm").equals("am") ? GregorianCalendar.AM : GregorianCalendar.PM);
					clkDate.setHour(Integer.parseInt(time.getAttribute("hour")));
					clkDate.setMinute(Integer.parseInt(time.getAttribute("minute")));
					Element repeat = helper.getElementByTagName(alarm,"repeat");
					clockAlarm.setWeekly(repeat.getAttribute("weekly").equals("true") ? true : false);
					clkDate.setTimeZone(implClock.getDate().getTimeZone());
					clockAlarm.setDate(clkDate);
					implClock.addAlarm(clockAlarm);
			    }			
				clocks.add(implClock);
	        }
		}
		catch(Exception ex)
		{
	        String errStr = ClockGUI.resources.getString("ClockError.ExceptionReadingStoredConfig");
	        System.out.println(errStr);
	        //null them out then return 0 length array
	        parser = null;
	        helper = null;
	        clocks.clear();
		}
		return(clocks);
	}
	
	/**
	 * Remove the current runtime settings
	 * 
	 * @return true on success, false on failure
	 */
	public static boolean removeRuntimeSettings()
	{
	    if (helper == null)
	        return(true);
	    
	    Element root = helper.getRootElement();
	    if (root == null)
	    	return true;
	    Element runtimeSettings = helper.getElementByTagName(root,"runtimeSettings");
	    Element activeClocks = helper.getElementByTagName(runtimeSettings,"activeClocks");
		NodeList activeClockList = helper.getElementsByTagName(activeClocks,"activeClock");
		try
		{
		    int numActiveClocks = activeClockList.getLength();
			for (int i = 0; i < numActiveClocks; i++)
	        {
			    //the activeClock NodeList is dynamic so always delete item 0
			    Element clockDef = (Element)activeClockList.item(0);
			    
			    Element alarms = helper.getElementByTagName(clockDef,"alarms");
			    NodeList alarmList = helper.getElementsByTagName(alarms,"alarm");
			    int numAlarms = alarmList.getLength();
			    for(int j = 0; j < numAlarms; j++)
			    {
			        Element alarmDef = (Element)alarmList.item(0);
			        helper.removeChildFromParent(alarms, alarmDef);
			    }
			    helper.removeChildFromParent(activeClocks,clockDef);
	        }
		}
		catch(Exception e)
		{
	        String errStr = ClockGUI.resources.getString("ClockError.ExceptionRemovingRuntimeSettings");
	        System.out.println(errStr + " " + e.getMessage());
		}
	    return(true);
	}
	
	/**
	 * add the settings for an individual clock to the XML document
	 * @param frame
	 * @return true on success, false on failure
	 */
	@SuppressWarnings("unused")
	public static boolean writeRuntimeSettings(SVG_SWTViewerFrame frame)
	{
	    try
	    {
	        //if helper is null create new file
	        if ((helper == null) || (helper.getDocument() == null))
	        {
	            helper = new DOMXMLParserHelper();
		        helper.createDocument("digitalClock", "", "");
  	            Element root = (Element)helper.getDocument().createElement("digitalClock");
  	            helper.setRootElement(root);
  	            //Element runtimeSettings = helper.addChildToRoot("runtimeSettings");
  	            //Element activeClocks = helper.addChildToParent(runtimeSettings,"activeClocks");
  	            parser = helper.getDOMXMLParser();
  	            parser.setXMLSource(ClockGUI.workingDirectory + CONFIGURATION_FILE);
	        }
	        
	        Clock clock = frame.getClock();
	        Element root = helper.getRootElement();
		    Element runtimeSettings = helper.getElementByTagName(root,"runtimeSettings");
            if (runtimeSettings == null) {
            	runtimeSettings = helper.addChildToRoot("runtimeSettings");
            }
            Element activeClocks = helper.getElementByTagName(runtimeSettings,"activeClocks");
            if (activeClocks == null) {
            	activeClocks = helper.addChildToParent(runtimeSettings,"activeClocks");
            }
            //First Add active clock to parent
		    Element activeClock = helper.addChildToParent(activeClocks,"activeClock");
		    activeClock.setAttribute("ParentID", clock.getParentID().toString());
		    activeClock.setAttribute("ID", clock.getID().toString());
		    //add size
		    Element size = helper.addChildToParent(activeClock, "size");
		    size.setAttribute("width", String.valueOf(clock.getCurrentWidth()));
		    size.setAttribute("height", String.valueOf(clock.getCurrentHeight()));
			size.setAttribute("allowOnTopInFullWindowMode",String.valueOf(clock.isOnTopInFullWindowMode()));
		    size.setAttribute("inFullWindowMode", String.valueOf(clock.isFullWindow()));
			
			//add scale
		    Element scale = helper.addChildToParent(activeClock, "scale");
		    scale.setAttribute("allowScale", String.valueOf(clock.isAllowedToScale()));
		    scale.setAttribute("preserveAspect", String.valueOf(clock.isFixedAspectRatio()));
		    //add position
		    Element position = helper.addChildToParent(activeClock, "position");
		    Point framePos = frame.getLocation();
		    position.setAttribute("x", String.valueOf(framePos.x));
		    position.setAttribute("y", String.valueOf(framePos.y));
		    ClockDate clockDate = clock.getDate();
		    //add Time Zone
		    Element timeZone = helper.addChildToParent(activeClock, "timeZone",clockDate.getTimeZone());
		    //add caption
		    Element caption = helper.addChildToParent(activeClock, "caption",clock.getCaption());
		    //add Time and date flags
		    Element timeDate = helper.addChildToParent(activeClock, "timeDate");
		    timeDate.setAttribute("showTime", String.valueOf(clock.isAllowedToShowTime()));
		    timeDate.setAttribute("twelveHourDisplay", String.valueOf(clock.isUsingTwelveHrFormat()));
		    timeDate.setAttribute("showDate", String.valueOf(clock.isAllowedToShowDate()));
	        
		    //write out clock cycle attributes here
		    Element cycleClock = helper.addChildToParent(activeClock, "cycleClock");
		    cycleClock.setAttribute("cycleClocks", String.valueOf(clock.isCycleClocks()));
		    cycleClock.setAttribute("minutes", String.valueOf(clock.getCyclePeriod()));
		    cycleClock.setAttribute("limitToGroup", String.valueOf(clock.isCycleGroupLimit()));
		    cycleClock.setAttribute("group", clock.getCycleGroup());
		    
		    Vector<ClockAlarm> clockAlarms = clock.getAlarms();
		    //add the alarms element here
			Element alarms= helper.addChildToParent(activeClock,"alarms");
		    for (int i = 0; i < clockAlarms.size(); i++)
		    {
			    ClockAlarm clockAlarm =  (ClockAlarm)clockAlarms.get(i);
			    ClockDate alarmDate = clockAlarm.getDate();
			    Element alarm = helper.addChildToParent(alarms,"alarm");
			    alarm.setAttribute("ID", String.valueOf(i));
			    alarm.setAttribute("enabled", String.valueOf(clockAlarm.isEnabled()));
			    Element sound = helper.addChildToParent(alarm, "sound");
			    sound.setAttribute("path", clockAlarm.getAlarmResource());
			    sound.setAttribute("volume", clockAlarm.getVolume());
			    Element date = helper.addChildToParent(alarm, "date");
			    date.setAttribute("year", String.valueOf(alarmDate.getYear()));
			    date.setAttribute("month", String.valueOf(alarmDate.getMonth()));
			    date.setAttribute("dayOfMonth", String.valueOf(alarmDate.getDayOfMonth()));
			    Element time = helper.addChildToParent(alarm, "time");
			    time.setAttribute("amPM", alarmDate.getAmPm() == GregorianCalendar.AM ? "am" : "pm");
			    time.setAttribute("hour", String.valueOf(alarmDate.getHour()));
			    time.setAttribute("minute", String.valueOf(alarmDate.getMinute()));
			    Element repeat = helper.addChildToParent(alarm, "repeat");
			    repeat.setAttribute("weekly", String.valueOf(clockAlarm.isWeekly()));
		    }			
	    }
	    catch(Exception e)
	    {
	        String errStr = ClockGUI.resources.getString("ClockError.ExceptionWritingSettings");
	        System.out.println(errStr + " " + e.getMessage());
	    }
	    
	    return(true);
	}
	
	/**
	 * Remove the current runtime settings
	 * 
	 * @return true on success, false on failure
	 */
	public static boolean removeScreenSaverSettings()
	{
	    if (ssHelper == null)
	        return(true);
	    
		try
		{
		    Element settings = ssHelper.getRootElement();
			Element activeScreenSaversList = ssHelper.getElementByTagName(settings,"activeScreenSavers");
			NodeList savers = ssHelper.getElementsByTagName(activeScreenSaversList, "screenSaver");
			int numScreenSavers = savers.getLength();
			for (int i = 0; i < numScreenSavers; i++)
	        {
			   	Element activeScreenSaver = (Element)savers.item(0);
				ssHelper.removeChildFromParent(activeScreenSaversList,activeScreenSaver);
			}
			ssHelper.removeChildFromParent(settings,activeScreenSaversList);
			Element timeSettings = ssHelper.getElementByTagName(settings,"timeSettings");
			ssHelper.removeChildFromParent(settings,timeSettings);
			Element clockMaxNumberPerWindow = ssHelper.getElementByTagName(settings,"clockMaxNumberPerWindow");
			ssHelper.removeChildFromParent(settings,clockMaxNumberPerWindow);
			Element groupSettings = ssHelper.getElementByTagName(settings,"groupSettings");
			ssHelper.removeChildFromParent(settings,groupSettings);
			Element clockSettings = ssHelper.getElementByTagName(settings,"clockSettings");
			ssHelper.removeChildFromParent(settings,clockSettings);
		}
		catch(Exception e)
		{
	        String errStr = ClockGUI.resources.getString("ClockError.ExceptionRemovingRuntimeSettings");
	        System.out.println(errStr + " " + e.getMessage());
		}
	    return(true);
	}
	
	/**
	 * add the settings for an individual clock to the XML document
	 * @param frame
	 * @return true on success, false on failure
	 */
	@SuppressWarnings("unused")
	public static boolean writeScreenSaverSettings(AbstractScreenSaver saver, List<String> savers)
	{
	    try
	    {
	        //if helper is null create new file
	        if (ssHelper == null)
	        {
			    ssHelper = new DOMXMLParserHelper();
		        ssHelper.createDocument("screenSaverSettings", "", "");
  	            Element root = (Element)ssHelper.getDocument().createElement("screenSaverSettings");
  	            ssHelper.setRootElement(root);
  	            
  	            ssParser = ssHelper.getDOMXMLParser();
  	            ssParser.setXMLSource(ClockGUI.workingDirectory + SS_CONFIGURATION_FILE);
	        }
			Element root = ssHelper.getRootElement();
  	        Element activeScreenSavers = ssHelper.addChildToRoot("activeScreenSavers");
  	        for (int i = 0; i < savers.size(); i++)
  	            {
  	                ssHelper.addChildToParent(activeScreenSavers,"screenSaver", savers.get(i));
  	            }

  	        Element timeSettings = ssHelper.addChildToRoot("timeSettings");
  	        timeSettings.setAttribute("rotationPeriodInScreenSaver", String.valueOf(saver.getClockRotationPeriod()));
  	        timeSettings.setAttribute("rotationPeriodBetweenScreenSavers",String.valueOf(saver.getScreenSaverRotationPeriod()));
  	        Element clockMaxNumberPerWindow = ssHelper.addChildToRoot("clockMaxNumberPerWindow");
  	        clockMaxNumberPerWindow.setAttribute("maxnum",String.valueOf(saver.getMaxClocksPerScreen()));
  	        Element groupSettings = ssHelper.addChildToRoot("groupSettings");
  	        groupSettings.setAttribute("keepWithinGroup", String.valueOf(saver.isLimitScreenToGroup()));
	        groupSettings.setAttribute("group", AbstractScreenSaver.getCurrentClockGroupName());
  	        Element clockSettings = ssHelper.addChildToRoot("clockSettings");
  	        clockSettings.setAttribute("frameAroundEach", String.valueOf(saver.isFrameAroundClocks()));
  	        clockSettings.setAttribute("displayBoxAroundTimeDate", String.valueOf(saver.isBoxAroundCaption()));
  	        writeScreenSaverConfigurationFile();
	    }
	    catch(Exception e)
	    {
	        String errStr = ClockGUI.resources.getString("ClockError.ExceptionWritingSettings");
	        System.out.println(errStr + " " + e.getMessage());
	    }
	    
	    return(true);
	}
	
	/**
	 * Write the file out
	 * @return true on success, false on failure
	 */
	public static boolean writeClockConfigurationFile()
	{
	   return(helper.writeDocument());	    
	}

	/**
	 * Write the ScreenSaverfile out
	 * @return true on success, false on failure
	 */
	private static boolean writeScreenSaverConfigurationFile()
	{
	   return(ssHelper.writeDocument());	    
	}
	
	/**
	 * @return Returns the allClocks.
	 */
	public static Map<String, Clock> getAllDefinedClocks() 
	{
		return(allDefinedClocks);
	}
	
	/**
	 * @return Returns the height.
	 */
	public static int getHeight() 
	{
		return(height);
	}
	
	/**
	 * @return Returns the width.
	 */
	public static int getWidth() 
	{
		return(width);
	}
	
	
}