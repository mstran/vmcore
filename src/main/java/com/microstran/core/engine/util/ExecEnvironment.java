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

package com.microstran.core.engine.util;

import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

/*
 * 
 * 
 * @author Mstran
 *
 * Simple static methods class to determine where we are operating, to see 
 * if we can do the windows "always on top" style if the user requests it.
 * 
 * 
 */
public class ExecEnvironment 
{	
    public static final String CR_LF = System.getProperty("line.separator");	
    
    public static final String OSName = System.getProperty("os.name");
    public static final String OSArchitecture = System.getProperty("os.arch");
    public static final String OSVersion = System.getProperty("os.version");
    public static final String javaVersion = System.getProperty("java.version");
    public static final String javaVendor = System.getProperty("java.vendor");
    public static final String userTimeZone = System.getProperty("user.timezone");
    
    private static final long KBMB = 1048576;
    
	public static GraphicsEnvironment ge;
	static
    {
        ge = GraphicsEnvironment.getLocalGraphicsEnvironment(); 
    }
    
    
    public static long getFreeMemory() 
    {		
    	Runtime runtime = Runtime.getRuntime(); 
    	return(runtime.freeMemory()); 
    }	
	
    public static long getTotalMemory() 
    {		
    	Runtime runtime = Runtime.getRuntime(); 
    	return(runtime.totalMemory()); 
    }	
    	
    public static long getMaxMemory() 
    {		
    	Runtime runtime = Runtime.getRuntime(); 
    	return(runtime.maxMemory()); 
    }	
    
    public static int getTotalMemoryInMB()
    {
        long totalMemory = getTotalMemory();
        return((int)((totalMemory/1024)/1024));
    }

    public static int getFreeMemoryInMB()
    {
        long freeMemory = getFreeMemory();
        return((int)((freeMemory/1024)/1024));
    }
    
    public static int getMaxMemoryInMB()
    {
        long maxMemory = getFreeMemory();
        return((int)((maxMemory/1024)/1024));
    }

    /**
     * Get the number of monitors
     * @return
     */
    public static int getNumberScreens()
    {
        GraphicsDevice[] gs = ge.getScreenDevices();
        return(gs.length);
    }

    
    /**
     * Get the resolution for a given screen
     * @return
     */    public static Rectangle getResolutionForScreen(int which)
    {
        Rectangle bounds = new Rectangle();

        GraphicsDevice[] gs = ge.getScreenDevices();
        GraphicsDevice gd = gs[which];
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        bounds = gc.getBounds();
        return(bounds);
    }
    
    /**
     * Get the total resolution, which may span multiple monitors!
     * @return
     */
    public static Rectangle getResolutionForTotalScreen()
    {
        Rectangle virtualBounds = new Rectangle();
        GraphicsDevice[] gs = ge.getScreenDevices();
        for (int j = 0; j < gs.length; j++) 
        { 
            GraphicsDevice gd = gs[j];
            GraphicsConfiguration[] gc = gd.getConfigurations();
            for (int i=0; i < gc.length; i++) 
            {
                virtualBounds = virtualBounds.union(gc[i].getBounds());
            }
        }
        return(virtualBounds);
    }

    /**
     * gets color depth Big note here, doesn't work on XWindows/Linux
     * for reasons I don't know...
     * @param whichScreen
     * @return
     */
    public static int getColorDepthForScreen(int whichScreen)
    {
        GraphicsDevice[] gs = ge.getScreenDevices();
        GraphicsDevice gd = gs[whichScreen];
        DisplayMode dm = gd.getDisplayMode();
        int colorDepth = dm.getBitDepth();
        return(colorDepth);
    }

    
    /**
     * get whether or not full screen mode is supported
     * @return
     */
    public static boolean isFullScreenModeSupported()
    {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gs = ge.getDefaultScreenDevice();
        return(gs.isFullScreenSupported());   
    }
    
}