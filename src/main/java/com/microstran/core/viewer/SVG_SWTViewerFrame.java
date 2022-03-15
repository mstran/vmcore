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

package com.microstran.core.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import javax.crypto.spec.SecretKeySpec;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGOMGElement;
import org.apache.batik.bridge.ExternalResourceSecurity;
import org.apache.batik.bridge.RelaxedExternalResourceSecurity;
import org.apache.batik.bridge.RelaxedScriptSecurity;
import org.apache.batik.bridge.ScriptSecurity;
import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.bridge.UpdateManagerEvent;
import org.apache.batik.bridge.UpdateManagerListener;
import org.apache.batik.bridge.Window;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.gvt.GVTTreeRendererListener;
import org.apache.batik.swing.svg.SVGUserAgent;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.batik.util.gui.JErrorPane;
import org.apache.batik.util.gui.MemoryMonitor;
import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.MissingListenerException;
import org.apache.batik.util.gui.resource.ToolBarFactory;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;

import com.microstran.core.clock.Clock;
import com.microstran.core.clock.ClockDate;
import com.microstran.core.engine.ClockGUI;
import com.microstran.core.engine.encryption.EncryptionServer;
import com.microstran.core.engine.util.FileInputHandler;
import com.microstran.core.engine.util.MessageLocalizer;
import com.microstran.core.engine.util.SVGInputHandler;
import com.microstran.core.event.ApplicationEventServer;
import com.microstran.core.event.clock.WindowChangeEvent;

/**
 * 
 * @author Mstran
 *
 * Main SVG Viewer Frame Class
 * These are created by instances of ClockGUI class implementations
 * There are a lot of interfaces implemented here, mostly for events having 
 * to do with the rendering process but also for actions
 */
public class SVG_SWTViewerFrame extends JFrame 
	implements ActionMap, GVTTreeRendererListener, UpdateManagerListener
{
	private static final long serialVersionUID = -8752691509155312426L;
    
//  Actions to be performed
public static final String ABOUT_VMCC_ACTION 	 = "AboutVMCCAction";
public static final String NEW_CLOCK_ACTION      = "NewClockAction";
public static final String PREVIOUS_CLOCK_ACTION = "PreviousClockAction";
public static final String NEXT_CLOCK_ACTION     = "NextClockAction";
public static final String FULL_WINDOW_ACTION    = "FullWindowAction";
public static final String CONFIG_DIALOG_ACTION  = "ConfigDialogAction";
public static final String CLOSE_CLOCK_ACTION    = "CloseClockAction";
public static final String EXIT_ACTION           = "ExitAction";
public static final String HOT_EXIT_ACTION       = "HotExitAction";
public static final String START_CLOCK_ACTION    = "StartClockAction";
public static final String PAUSE_CLOCK_ACTION    = "PauseClockAction";
public static final String STOP_CLOCK_ACTION     = "StopClockAction";
public static final String RESTORE_WINDOW_ACTION = "RestoreWindowAction";
public static final String MEMORY_DIALOG_ACTION  = "MemoryDialogAction";
public static final String NO_OP_ACTION          = "NoOpAction";

public static final int TOOLBAR_ID_PREVIOUS = 4;
public static final int TOOLBAR_ID_NEXT = 5;
public static final int TOOLBAR_ID_START = 11;
public static final int TOOLBAR_ID_PAUSE = 12;


/**
 * Shared wait cursor
 */
public static final Cursor WAIT_CURSOR    = new Cursor(Cursor.WAIT_CURSOR);
/**
 * Shared normal cursor
 */
public static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);

/**
 * Our map of action actionListeners
 */
protected Map actionListeners = null; 

/**
 * Default File input handler for use across all instances of viewer
 */
protected static SVGInputHandler fileHandler = new SVGInputHandler();


/**
 * A JFrame for memory monitoring
 */
protected static JFrame memoryDialogFrame;

/**
 * A back reference to an instance of the main application class
 */
protected static ClockGUI clockGUI;  

/**
 * The internal <code>JSVGCanvas</code>
 */
protected JSVGCanvas svgCanvas;

/**
 * The SVG clock document associated with this view
 */
protected SVGDocument svgDocument;

/**
 * The clock associated with this frame
 */
protected Clock clock;

/**
 * Batik Scripting Window see <code>window</code>
 */
protected Window scriptingWindow; 

/**
 * The <code>JPanel</code> where the svgCanvas is displayed
 */
protected JPanel svgCanvasPanel;

/**
 * Exit action
 */
static Method setExitAction = null;

/**
 * Starts the Clock
 */
protected StartClockAction startClockAction = new StartClockAction();

/**
 * Pauses the Clock
 */
protected PauseClockAction pauseClockAction = new PauseClockAction();

/**
 * Stops the Clock
 */
protected StopClockAction stopClockAction  = new StopClockAction();

/**
 * Stop system
 */
protected HotExitAction hotExitAction = new HotExitAction();

/**
 * noOp Action
 */
protected NoOpAction noOpAction = new NoOpAction();

/**
 * for memory monitor
 */
protected MemoryDialogAction memoryDialogAction = new MemoryDialogAction();

/**
 * Go to the previous clock
 */
protected PreviousClockAction previousClockAction = new PreviousClockAction();

/**
 * Goes to the next clock
 */
protected NextClockAction nextClockAction = new NextClockAction();

/**
 * toggle for full window
 */
protected FullWindowAction fullWindowAction = new FullWindowAction();

/**
 * Action to take when we press escape key while window is zoomed
 */
protected RestoreWindowAction restoreWindowAction = new RestoreWindowAction();

/**
 * A flag to control whether we dynamically adjust the controls to fit the window
 */
protected boolean adjustForControls = true;

/**
 * A flag to indicate if the previous button is enabled
 */
protected boolean hasPrevious = false;

/**
 * A flag to indicate if the next button is enabled
 */
protected boolean hasNext = false;

/**
 * A concrete instance of an SVG user agent for use with alerting the user
 * to conditions within our application
 */
protected SVGUserAgent clockUserAgent = new ClockUserAgent();

/**
 * A reference to the Message bar at the bottom of the screen where we will post updates
 */
protected MessageBar messageBar = null;

/**
 * Flag to tell if we should start the engine up, this is a simple sync issue with the update manager
 */
protected boolean isInitialized = false;

/**
 * synchronization objects
 */
protected boolean managerRunning = false;
protected boolean renderedOnce = false;
protected boolean resizingNow = false;
protected boolean isTransparent = false;
protected boolean isAM = true;

/**
 * the main toolbar
 */
protected JToolBar toolBar = null;


/**
 * Creates a new SVG viewer frame 
 * and initializes it for the document at hand
 * @param gui
 * @param ID
 */
public SVG_SWTViewerFrame(Clock clock, boolean hasToolbar, 
        boolean hasMessageBar,  boolean hasBorder, boolean terminateOnUserInteraction, 
        Color backgroundColor, Color messageBarColor)
{
    try{
	    setVisible(false);
	    this.clock = clock;
	    
	    ClockDate date = clock.getDate();
	    //for publishing events as the hours cross between AM/PM
	    date.setFrame(this);
	    
	    isAM = (date.getAmPm() == GregorianCalendar.AM);  //PM == 1 AM == 0
	    
	    if (clock.isFullWindow() == true){
	        //if fullwindow no toolbar no matter what the params say
	        hasToolbar = false;
	        if (clock.isOnTopInFullWindowMode() == true) {
	        	this.setAlwaysOnTop(true);
	        	isTransparent = true;
	        }
	    }
	    addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) 
	        {
	            ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.VIEWER_CLOSE_EVENT,SVG_SWTViewerFrame.this));
	        }
	    });

	    // Create a new canvas and set the frame's maximum size to the screen size
	    svgCanvas = new JSVGCanvas(clockUserAgent, true, true)
	    {
	    	private static final long serialVersionUID = -1439447288103242211L;
	    	Dimension screenSize;
	        {	
	            screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	            setMaximumSize(screenSize);
	        }
	        public Dimension getPreferredSize()
	        {
	            Dimension dim = super.getPreferredSize();
	            if (dim.width > screenSize.width) 
	                dim.width =screenSize.width;
	            if (dim.height > screenSize.height) 
	                dim.height = screenSize.height;
	            return(dim);
	        }
	        /**
	         * Called when a component knows the desired size of the window 
	         * (based on width/height of outermost SVG element). 
	         * We override it to pack the frame.
	         */
	        public void setMySize(Dimension d) 
	        {
	            setPreferredSize(d);
	            invalidate();
	            if (SVG_SWTViewerFrame.this.adjustForControls) 
	            {
	                SVG_SWTViewerFrame.this.pack();
	            }
	        }
	    };
    
	    if (isTransparent) {
	    	setUndecorated(true);
	      	if (isAM == true) { 
	      		svgCanvas.setBackground(new Color(1.0f,1.0f,1.0f,0.0f));
	      	} else {
	      		svgCanvas.setBackground(backgroundColor);
	      	}
	    }else{
	      	if (isAM == true) { 
	    		svgCanvas.setBackground(new Color(1.0f,1.0f,1.0f,0.0f));
	      	} else {
	    		svgCanvas.setBackground(backgroundColor);
	      	}
	    }

	    //set for double buffering
	    svgCanvas.setDoubleBufferedRendering(true);
	    //configure canvas as dynamic
	    svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
	
	    javax.swing.ActionMap map = svgCanvas.getActionMap();
	    map.put(FULL_WINDOW_ACTION, fullWindowAction);
	    map.put(PREVIOUS_CLOCK_ACTION, previousClockAction);
	    map.put(NEXT_CLOCK_ACTION, nextClockAction);
	    map.put(MEMORY_DIALOG_ACTION, memoryDialogAction);
	    map.put(RESTORE_WINDOW_ACTION, new RestoreWindowAction());
	    
	    map.put(HOT_EXIT_ACTION, hotExitAction);
	    javax.swing.InputMap imap = svgCanvas.getInputMap(JComponent.WHEN_FOCUSED);
	    // SET F10 Key = full window
	    KeyStroke fullWindowKey = KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
	    imap.put(fullWindowKey, FULL_WINDOW_ACTION);
	
	    // SET F12 Key = exit application
	    KeyStroke exitApplicationKey = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
	    imap.put(exitApplicationKey, HOT_EXIT_ACTION);
	
	    // SET Left arrow key to previous clock
	    KeyStroke previousClockKey = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
	    imap.put(previousClockKey, PREVIOUS_CLOCK_ACTION);
	
	    //SET UP arrow key to NoOp
	    KeyStroke upKey = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
	    imap.put(upKey, NO_OP_ACTION);
	
	    //SET DOWN arrow key to NoOp
	    KeyStroke dwnKey = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
	    imap.put(dwnKey, NO_OP_ACTION);
	    
	    // SET a key stroke combination for the memory dialog
	    KeyStroke memoryDialogKey = KeyStroke.getKeyStroke(KeyEvent.VK_F2, java.awt.Event.CTRL_MASK);
	    imap.put(memoryDialogKey, MEMORY_DIALOG_ACTION);
	    
	    // SET Left arrow key to previous clock
	    KeyStroke nextClockKey = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
	    imap.put(nextClockKey, NEXT_CLOCK_ACTION);
	    
	    // SET Escape Key to restore a zoomed window
	    KeyStroke restoreZoomedWindowKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	    imap.put(restoreZoomedWindowKey, RESTORE_WINDOW_ACTION);
	    
	    //put a copy of all of the actionListeners that this screen will have 
	    //these coorespond to the toolbar actions that will be associated
	    //with the toolbar when it's built
	    actionListeners  = new HashMap();
	    
	    actionListeners.put(ABOUT_VMCC_ACTION, new AboutVMCCAction());
	    actionListeners.put(NEW_CLOCK_ACTION, new NewClockAction());
	    actionListeners.put(PREVIOUS_CLOCK_ACTION, previousClockAction);
	    actionListeners.put(NEXT_CLOCK_ACTION, nextClockAction);
	    actionListeners.put(CONFIG_DIALOG_ACTION, new ConfigDialogAction());
	    actionListeners.put(CLOSE_CLOCK_ACTION, new CloseClockAction());
	    actionListeners.put(EXIT_ACTION, hotExitAction);
	    actionListeners.put(START_CLOCK_ACTION, startClockAction);
	    actionListeners.put(PAUSE_CLOCK_ACTION, pauseClockAction);
	    actionListeners.put(STOP_CLOCK_ACTION, stopClockAction);
	    actionListeners.put(FULL_WINDOW_ACTION, fullWindowAction);
	    
	    JPanel p = null;
	    try 
		{
	        if (this.clock.isFullWindow() == false){
	            p = new JPanel(new BorderLayout());
	            
	    	    if(hasBorder)
	    	        p.setBorder(BorderFactory.createEtchedBorder());
	        
		        /**
		         *  Creates the toolbar using the Batik util toolbar factory which
		         *  takes the resource bundle and looks for entries in the form of 
		         *  ToolBar=Item1 Item2 - Item3  -> We pass our resource bundle in
		         * and the toolbar will be created. Each reference in the space 
		         * delimited (or use '-' to create a "separator") must later include
		         * in the resource bundle the following 3 entries:  
		         *		name.icon=iconpath to resource image 
		         *		name.action=action to associate see the above actions 
		         *		name.tooltip=tooltip to display 
		         */
	    	    if(hasToolbar){
		    	    ToolBarFactory tbf = new ToolBarFactory(ClockGUI.bundle, this);
			        toolBar = tbf.createJToolBar("ToolBar");
			        toolBar.setFloatable(false);//we want to keep it docked
			        getContentPane().add(p, BorderLayout.NORTH);
			        p.add(toolBar, BorderLayout.NORTH);
	    	    }
	        } else {
	        	setUndecorated(true);
	        }
	    } catch (MissingResourceException e){
	        System.out.println("Missing Resource = " + e.getMessage());
	        System.exit(0);
	    }

    svgCanvasPanel = new JPanel(new BorderLayout());
    if(hasBorder) {
	    if (this.clock.isFullWindow() == false){
	        svgCanvasPanel.setBorder(BorderFactory.createEtchedBorder());
	    } else {
	    	if (isTransparent) {
	    		svgCanvasPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
	    	}
	    }
    }    
    svgCanvasPanel.add(svgCanvas, BorderLayout.CENTER);
    p = new JPanel(new BorderLayout());
    p.add(svgCanvasPanel, BorderLayout.CENTER);
    
    
    if (clock.isAllowedToShowTime()){
        p.add(messageBar = new MessageBar(messageBarColor, hasBorder), BorderLayout.SOUTH);
    }
    
    if (this.clock.isFullWindow() == false)
    {
        getContentPane().add(p, BorderLayout.CENTER);
    }
    else
    {
	        JPanel pInset = new JPanel(new BorderLayout());
	        if ((hasBorder == true) && (isTransparent == false)) {
	            pInset.setBorder(BorderFactory.createEtchedBorder());
	        }
	        pInset.add(p, BorderLayout.CENTER);
	        getContentPane().add(pInset, BorderLayout.CENTER);
    }
        
    svgCanvas.addUpdateManagerListener(this);
    svgCanvas.addGVTTreeRendererListener(this);
    
    //add a movement listener so that we can store position information dynamically
    addComponentListener(new ComponentAdapter()
    {
        public void componentMoved(ComponentEvent e)
        {
            if (SVG_SWTViewerFrame.this.isInitialized)
    	    {
                Point topLeft = SVG_SWTViewerFrame.this.getLocation();
                SVG_SWTViewerFrame.this.clock.setPosX(topLeft.x);
                SVG_SWTViewerFrame.this.clock.setPosY(topLeft.y);
    	    }
        }
    });
    //Resize Listener so that we can store positional info and dynamically resize the clocks
    svgCanvas.addComponentListener(new ComponentAdapter() 
    {
    	public void componentResized(ComponentEvent e) 
    	{
    	    //if we've finished initializing then compare the size of the current canvas with our target size
    	    //if they're different AND auto scale is enabled we want to zoom the image up or down.
    	    if (SVG_SWTViewerFrame.this.isInitialized)
    	    {
    	        if (SVG_SWTViewerFrame.this.clock.isAllowedToScale())
    	        {
    	            //stop processing  THIS IS A SYNCHRONIZED ACTIVITY
    	            synchronized(this){SVG_SWTViewerFrame.this.resizingNow = true;}
    	            Dimension canvasDimensions = SVG_SWTViewerFrame.this.svgCanvas.getSize();
    	    	    SVG_SWTViewerFrame.this.clock.setCurrentWidth(canvasDimensions.width);
    	    	    SVG_SWTViewerFrame.this.clock.setCurrentHeight(canvasDimensions.height);
   	    		    SVG_SWTViewerFrame.this.AdjustImageTransform();
    	        }
    	    }
        }
    });

	    if (terminateOnUserInteraction)
	    {
	        svgCanvas.addMouseMotionListener(new MouseMotionAdapter() 
	        {
	            MouseEvent initialEvent = null;
	            
	            //mouse moved seems to be rediculously hyper-sensitive so we want to ensure that
	            //in fact the mouse was really moved...
                public void mouseMoved(MouseEvent e) 
                {
                    if (SVG_SWTViewerFrame.this.isInitialized)
                        {
                        	if (initialEvent != null)
                        	{
                        	    if ((initialEvent.getX() != e.getX()) && (initialEvent.getY() != e.getY()))
                        	        System.exit(0);
                        	}
                        	else
                        	    initialEvent = e;
                        }
                }
	        });
	        svgCanvas.addMouseListener(new MouseAdapter() 
  	        {
                public void mouseClicked(MouseEvent e) 
                {
                    if (SVG_SWTViewerFrame.this.isInitialized)
                        clockGUI.closeSVG_SWTViewerFrame(null, false);
   	            }
   	        });
	        svgCanvas.addKeyListener(new KeyAdapter()
	        {
	            public void keyPressed(KeyEvent k)
                {
                    if (SVG_SWTViewerFrame.this.isInitialized)
                        System.exit(0);
  	            }
  	        });
	            
	    }
    }
    catch(Exception e)
    {
        System.out.println("Error constructing viewer frame, error = " + e.getMessage());
        System.out.println("Stack = " + e.getStackTrace());
    }
}


/**
 * 	Called when image size is modified so that we can resize if dynamic
 */
private void AdjustImageTransform()
{
    try
    {
	    int srcHeight 	 = clock.getSrcHeight();
	    int srcWidth 	 = clock.getSrcWidth();
	    int targetHeight = clock.getCurrentHeight();
	    int targetWidth  = clock.getCurrentWidth();
	     
	    if ((srcHeight == targetHeight) && (srcWidth == targetWidth))
	        return;
	    
	    //scale but preserve original dimensional ratios
	    double targetScaleX = 0;
	    double targetScaleY = 0; 
	    
	    
	    AffineTransform scaledAT =  svgCanvas.getRenderingTransform();
	    //if the aspect ratio is fixed we need to preserve it
	    if (clock.isFixedAspectRatio())
	    {
	        int newTargetHeight = targetHeight;
	        int newTargetWidth  = targetWidth;
	        
	        double targetRatio = (double)targetWidth / (double)targetHeight;
	        double imageRatio = (double)srcWidth / (double)srcHeight;
	        if (targetRatio < imageRatio) 
	        {
	          newTargetHeight = (int)(targetWidth / imageRatio);
	        } 
	        else 
	        {
	          newTargetWidth = (int)(targetHeight * imageRatio);
	        }
	        targetScaleX = (double)newTargetWidth / (double)srcWidth;
	        targetScaleY = (double)newTargetHeight / (double)srcHeight;
	        
	        //first scale the matrix this call in Batik will remove any translation!
	        scaledAT.setToScale(1,1);
	        //scale the matrix to the target factors
	        scaledAT.setToScale(targetScaleX,targetScaleY);
	        //find the scaled width and height
	        scaledAT.transform(new Point2D.Double(srcWidth, srcHeight),null);
	        double tX,tY;
	        
	        //create a new origin point factoring out scale since the matrix will put it in for us 
	        if (targetScaleX != 0)
	            tX = ((targetWidth/2) - (newTargetWidth/2))/targetScaleX;
	        else
	            tX = (targetWidth/2) - (newTargetWidth/2);
	        if (targetScaleY != 0)
	            tY = ((targetHeight/2) - (newTargetHeight/2))/targetScaleY;
	        else
	            tY = (targetHeight/2) - (newTargetHeight/2);
	        scaledAT.translate(tX,tY);
	    }
	    else
	    {
		    //scale without regard to original proportions
		    targetScaleX = (double)targetWidth / (double)srcWidth;
		    targetScaleY = (double)targetHeight / (double)srcHeight;
		    //scaling the matrix in Batik will reset the origin to 0,0 for us!
		    scaledAT.setToScale(targetScaleX,targetScaleY);
	    }
	    //now set the new matrix into the canvas, boolean true second argument or it won't get picked up immediately
	    svgCanvas.setRenderingTransform(scaledAT,true);
	    synchronized(this){SVG_SWTViewerFrame.this.resizingNow = false;}
    }
    catch(Exception e)
    {
        clockUserAgent.displayError(e);
    }
    
}

/**
 * 		Reset the message bar messages
 */
public void resetMessageBarMessages()
{
    if (messageBar == null)
        return;
    try
    {  
	    StringBuffer formatString = new StringBuffer();
	    if (clock.isAllowedToShowTime())
	    {
	        if (clock.isUsingTwelveHrFormat())
	            formatString.append("h:mm ");
	        else
	            formatString.append("k:mm ");
	        
	        if (clock.isUsingTwelveHrFormat())
	            formatString.append("a");
	    }
	    if (clock.isAllowedToShowDate())
	        formatString.append(" EEE, MMM d, yyyy");
	    
	    String displayString;
	    if (formatString.length() == 0)
	        displayString="";
	    else
	        displayString = ClockDate.getDisplayStringForLong(clock.getDate().updateAndGetCurrentTimeForDisplay(), formatString.toString(), clock.getDate().getTimeZone());
	
	    messageBar.setAMPMText(" " + displayString + " ");
	    messageBar.resetMessage();
	    messageBar.setClockMessageText(" " + clock.getCaption() + " ");
    }
    catch(Exception e)
    {
        clockUserAgent.displayError(e);
    }
}


/* (non-Javadoc)
 * @see java.awt.Window#dispose()
 */
public void dispose() 
{
    svgCanvas.dispose();
    super.dispose();
    
    
    //null out things we want to be done with
    if (this.messageBar != null)
    {
        this.messageBar.endMessageBar();
        this.messageBar = null;
    }
    this.toolBar = null;
    this.actionListeners.clear();
    this.clock = null;
    this.nextClockAction = null;
    this.previousClockAction = null;
    this.pauseClockAction = null;
    this.scriptingWindow = null;
    this.startClockAction = null;
    this.stopClockAction = null;
    this.hotExitAction = null;
    
    this.svgCanvas = null;
    this.svgCanvasPanel = null;
    this.svgDocument = null;
    this.accessibleContext = null;
}

/**
 *  This is the first of 3 key methods to fire:
 * 	It the SVG Doc URI into the canvas, this causes the clock SVG document to load, 
 *  the document loaded callback executes a method: setSVGDocumentInitializeClock()
 *  Finally the render methods callback fires which in turn registers the key 
 *  components and sets the event listener for rendering the hands
 * 
 */
public void showSVGDocument(String fileName)
{
	try 
	{
        byte[] sKey = new byte[] { 0x1A, -0x3C, 0x5B, 0x0B, 0x75, 0x64, 0x70, -0x4D };
        final SecretKeySpec key = new SecretKeySpec(sKey,"DES");
        EncryptionServer server = new EncryptionServer(key);
        byte [] input = server.runSVGDecrypt(server, fileName);
        //now parse into SVG document from the input stream
        //byte [] input = Files.readAllBytes(Paths.get(fileName));
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        SVGDocument doc = f.createSVGDocument(null,inputStream);
        this.svgDocument = doc;
        inputStream.close();
        previousClockAction.update(this.hasPrevious);
        nextClockAction.update(this.hasNext);
        startClockAction.update(false);
        svgCanvas.setCursor(DEFAULT_CURSOR);
        svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
        /*
         * Calling this will load up the document, triggering gvt rendering completed which
         * calls the clockGUI frameLoaded function This function only incriments the splash screen.
         * the loop driving the loading (by calling showSVGDocument) will block and wait for
         * the update manager to start and a first rendering to happen
        */
        svgCanvas.setSVGDocument(doc);
    } 
    catch (Exception e) 
	{
        if (clockUserAgent != null) 
        {
            clockUserAgent.displayError(e);
        }
        else
        {
            System.out.println("Exception showing Document: " + fileName + "  Exception = " + e.getMessage());
        }
	}
}

/**
 * Returns the action associated with the given string or null on error
 * @param key the key mapped with the action to get
 * @throws MissingListenerException if the action is not found
 */
public Action getAction(String key) 
	throws MissingListenerException 
{
    Action result = (Action)actionListeners.get(key);
    if (result == null) 
    {
        throw new MissingListenerException("Requested Action For Key Not Listed", MessageLocalizer.MESSAGES, key);
    }
    return result;
}

/**
 * Returns the input handler for the given URI
 */
public FileInputHandler getInputHandler(ParsedURL purl) throws IOException 
{
    return(fileHandler);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
//
//		Getters/Setters
//
///////////////////////////////////////////////////////////////////////////////////////////////////////


/**
 * Whether to auto adjust the canvas to the size of the document 
 * as the window changes shape.
 */
public void setAutoAdjust(boolean adjustForControls) 
{
    this.adjustForControls = adjustForControls;
}

/**
 * Gets the autoadjust flag
 * @return
 */
public boolean getAutoAdjust()
{ 
    return(this.adjustForControls);
}

/**
 * Returns the main JSVGCanvas of this frame.
 */
public JSVGCanvas getJSVGCanvas() 
{
    return svgCanvas;
}

/**
 * @return Returns the svgDocument.
 */
public SVGDocument getSVGDocument()
{
    return svgDocument;
}

/**
 * @return Returns the clockGUI.
 */
public static ClockGUI getClockGUI()
{
    return clockGUI;
}

/**
 * @param clockGUI The clockGUI to set.
 */
public static void setClockGUI(ClockGUI clockGUI)
{
    SVG_SWTViewerFrame.clockGUI = clockGUI;
}
/**
 * @return Returns the scriptingWindow.
 */
public Window getScriptingWindow()
{
    return scriptingWindow;
}

/**
 * @return Returns the clock.
 */
public Clock getClock()
{
    return clock;
}
/**
 * @param clock The clock to set.
 */
public void setClock(Clock clock)
{
    this.clock = clock;
    this.clock.setFrame(this);
}

/**
 * @param hasNext The hasNext to set.
 */
public void setHasNext(boolean hasNext)
{
    this.hasNext = hasNext;
}
/**
 * @param hasPrevious The hasPrevious to set.
 */
public void setHasPrevious(boolean hasPrevious)
{
    this.hasPrevious = hasPrevious;
}

/**
 * @return Returns the isInitialized.
 */
public boolean isInitialized()
{
    return isInitialized;
}
/**
 * @param isInitialized The isInitialized to set.
 */
public void setInitialized(boolean isInitialized)
{
    this.isInitialized = isInitialized;
}

/**
 * @return Returns the svgCanvas.
 */
public JSVGCanvas getSvgCanvas()
{
    return svgCanvas;
}


/**
 * @return height of the message bar
 */
public int getMessageBarHeight()
{
    if (messageBar != null)
        return(messageBar.getSize().height);
    else
        return(0);
}

/**
 * @return widht of the message bar
 */
public int getMessageBarWidth()
{
    if (messageBar != null)
        return(messageBar.getSize().width);
    else
        return(0);
}

/**
 * @return Returns the managerRunning.
 */
public boolean isManagerRunning()
{
    synchronized(this)
    {
	    if (managerRunning == false)
	    { 
	        try
	        { 
	            wait(); 
	        } 
	        catch(InterruptedException e)
	        { 
	            return(managerRunning); 
	        } 
	        catch(Exception e)
	        { 
	            return(managerRunning); 
	        } 
	    }
	    return(managerRunning);
    }
}

/**
 * @return Returns the managerRunning.
 */
public boolean isManagerStopped()
{
    synchronized(this)
    {
	    if (managerRunning == true)
	    { 
	        try
	        { 
	            wait(); 
	        } 
	        catch(InterruptedException e)
	        { 
	            return(managerRunning); 
	        } 
	        catch(Exception e)
	        { 
	            return(managerRunning); 
	        } 
	    }
	    return(managerRunning);
    }
}


/**
 * @param renderedOnce The renderedOnce to set.
 */
public void setRenderedOnce(boolean renderedOnce)
{
    synchronized(this)
    {
        this.renderedOnce = renderedOnce;
    }
}

/**
 * blocks us untill we've rendered the image once,
 * this prevents us from seeing the image and having the
 * hands jump into place, the image is rendered one time
 * off screen then moved on screen
* @return Returns the renderedOnce.
*/
public boolean isRenderedOnce()
{
    synchronized(this)
    {
	    if (renderedOnce == false)
	    { 
	        try
	        { 
	            wait(); 
	        } 
	        catch(InterruptedException e)
	        { 
	            return(renderedOnce); 
	        } 
	        catch(Exception e)
	        { 
	            return(renderedOnce); 
	        } 
	    }
	    return(renderedOnce); 
    }
}

/**
 * @return Returns the resizingNow.
 */
public boolean isResizingNow()
{
    synchronized(this)
    {
	    if (resizingNow == true)
	    { 
	        try
	        { 
	            wait(); 
	        } 
	        catch(InterruptedException e)
	        { 
	            return(resizingNow); 
	        } 
	        catch(Exception e)
	        { 
	            return(resizingNow); 
	        } 
	    }
	    return(resizingNow);
    }
}

/**
 * @param resizingNow The resizingNow to set.
 */
public void setResizingNow(boolean resizingNow)
{
    synchronized(this)
    {
        this.resizingNow = resizingNow;
        notify();
    }
}


///////////////////////////////////////////////////////////////////////////////////////////////////////
//
//			ALL ACTION CLASSES BELOW HERE
//
///////////////////////////////////////////////////////////////////////////////////////////////////////


/**
 * Class to show the about dialog
 */
public class AboutVMCCAction extends AbstractAction 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 493919305583664470L;

	public AboutVMCCAction()
    {}

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        AboutDialog aboutDlg = new AboutDialog(SVG_SWTViewerFrame.this);
        Dimension ds = aboutDlg.setAbsoluteLocation();
        aboutDlg.setSize(ds); 
        aboutDlg.setVisible(true);
        aboutDlg.toFront();
        aboutDlg.requestFocusInWindow();
    }
//about action class
}

/**
 * Class To open a new window.
 */
public class NewClockAction extends AbstractAction 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -1534940415891189144L;

	public NewClockAction() 
    {}
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) 
    {
        //call back into the ClockGUI class and duplicate this viewer basically
        ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.NEW_VIEWER_EVENT,SVG_SWTViewerFrame.this));
    }
//new window action class    
}

/**
 * Class To show/set the preferences via dialog.
 */
public class ConfigDialogAction extends AbstractAction 
{
    public ConfigDialogAction() 
    {}
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) 
    {
        ConfigDLG cfgDialog = null;
        try
        {
	        cfgDialog = new ConfigDLG();
			cfgDialog.setBounds(0, 0, 375, 400);
			
			//load dialog with details
			Clock tmpClock = Clock.createClock(SVG_SWTViewerFrame.this.clock);
			Clock.copyRuntimeSettings(tmpClock, 
			        						  SVG_SWTViewerFrame.this.clock,
			        						  SVG_SWTViewerFrame.this.clock.getID());
			
			cfgDialog.configureDialog(tmpClock);
			cfgDialog.setAbsoluteLocation();
			
	        //ApplicationEventServer.instance().unregister(SVG_SWTViewerFrame.this.clock, SVG_SWTViewerFrame.this.clock.getMainClockEventType());
			cfgDialog.setVisible(true);
			cfgDialog.toFront();
			
			if (cfgDialog.getOkButtonState() == true)
			{
			    tmpClock = cfgDialog.getClock();
			    //clock == tmpClock; in other words only the settings may have changed not the clock
			    if (SVG_SWTViewerFrame.this.clock.getID().equals(tmpClock.getID()))
			    {
			    	boolean originalCanShowTime = clock.isAllowedToShowTime();
			    	boolean canShowTime = tmpClock.isAllowedToShowTime();
			    	boolean originalCycleClocks = clock.isCycleClocks();
			    	boolean canCycleClocks = tmpClock.isCycleClocks();
			    	
			    	Clock.copyRuntimeSettings(SVG_SWTViewerFrame.this.clock, tmpClock, tmpClock.getID());
			        SVG_SWTViewerFrame.this.resetMessageBarMessages();
			        SVG_SWTViewerFrame.this.clock.configureTime();
			        SVG_SWTViewerFrame.this.clock.getRenderer().reset();
			        SVG_SWTViewerFrame.this.setTitle(clock.getCaption());
			        //these are a few conditions where we should swap out the old clock for a new one to get a proper frame refresh
					if ((canShowTime != originalCanShowTime) || (canCycleClocks != originalCycleClocks)) {
						//post an event here - you can't directly kill this window from the inner class, it will hang the thread
						ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.UPDATE_VIEWER_EVENT,SVG_SWTViewerFrame.this));
					}
			    }
			    else //clock itself has changed so reset frame
			    {
			        Clock.copyRuntimeSettings(SVG_SWTViewerFrame.this.clock, tmpClock, tmpClock.getID());
			        ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.DIALOG_SELECT_CLOCK,SVG_SWTViewerFrame.this));
			    }
			}
        }
		catch(Exception ex)
		{
            clockUserAgent.displayError(ex);
		}
		finally
		{
		    cfgDialog.dispose();
		}
    }
//config dialog action class    
}

/**
 * Class To close this clock.
 */
public class CloseClockAction extends AbstractAction 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -6705448559963214111L;

	public CloseClockAction() 
    {}
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) 
    {
        ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.VIEWER_CLOSE_EVENT,SVG_SWTViewerFrame.this));
    }
//clock close actionclass    
}

/**
 * Class To go to the previous clock
 */
public class PreviousClockAction extends AbstractAction
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 3935979903779925802L;

	public PreviousClockAction() 
    {}
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) 
    {
        //disable these to keep from having someone press twice too quickly...
        if (toolBar != null)
        {
            JComponent component = (JComponent)toolBar.getComponent(TOOLBAR_ID_NEXT);
            component.setEnabled(false);
            component = (JComponent)toolBar.getComponent(TOOLBAR_ID_PREVIOUS);
            component.setEnabled(false);
        }
        ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.PREVIOUS_VIEWER_EVENT,SVG_SWTViewerFrame.this));
     }
    
    /**
     * Allows us to enable or disable components in the toolbar based on events
     * @param enabled  
     */
    public void update(boolean enabled) 
    {
        if (toolBar != null)
        {
            JComponent component = (JComponent)toolBar.getComponent(TOOLBAR_ID_PREVIOUS);
            component.setEnabled(enabled);
        }
    }
//previous clock actionclass
}

/**
 * Class To go to Basically absorbe up/down keystrokes so the user can't move the clock
 * around vertically by accident
 */
public class NoOpAction extends AbstractAction 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 2151500630752640195L;

	public NoOpAction() 
    {}

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) 
    {
        //nothing!
    }
}

/**
 * Class To go to the previous clock
 */
public class NextClockAction extends AbstractAction 
{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -3473523979901775313L;

	public NextClockAction() 
    {}

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) 
    {
        //disable these to keep from having someone press twice too quickly...
        if (toolBar != null)
        {
            JComponent component = (JComponent)toolBar.getComponent(TOOLBAR_ID_NEXT);
	        component.setEnabled(false);
	        component = (JComponent)toolBar.getComponent(TOOLBAR_ID_PREVIOUS);
	        component.setEnabled(false);
        }
        ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.NEXT_VIEWER_EVENT,SVG_SWTViewerFrame.this));
    }

    /**
     * Allows us to enable or disable components in the toolbar based on events
     * @param enabled  
     */
    public void update(boolean enabled) 
    {
        if (toolBar != null)
        {
            JComponent component = (JComponent)toolBar.getComponent(TOOLBAR_ID_NEXT);
            component.setEnabled(enabled);
        }
    }
//next clock actionclass
}

/**
 * To restart a clock after a pause.
 */
public class StartClockAction extends AbstractAction 
{

    /**
	 * 
	 */
	private static final long serialVersionUID = -6594362618087652252L;

	public StartClockAction() 
    {}
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) 
    {
        SVG_SWTViewerFrame.this.clock.resumeClock();
        update(false);
        pauseClockAction.update(true);
        //since we are resuming we are most likely behind a bit
        SVG_SWTViewerFrame.this.clock.configureTime();
        //do this so we will immediately pick up time variations in the viewer
        SVG_SWTViewerFrame.this.clock.getRenderer().reset();
    }

    /**
     * Allows us to enable or disable components in the toolbar based on events
     * @param enabled  
     */
    public void update(boolean enabled) 
    {
        if (toolBar != null)
        {

            JComponent component = (JComponent)toolBar.getComponent(TOOLBAR_ID_START);
            component.setEnabled(enabled);
        }
    }
//restart clock actionclass    
}

/**
 * Class to pause clock 
 */
public class PauseClockAction extends AbstractAction 
{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 7910649211039892289L;

	public PauseClockAction() 
    {}
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) 
    {
        SVG_SWTViewerFrame.this.clock.pauseClock();
        update(false);
        startClockAction.update(true);
    }

     /**
     * @param enabled
     */
    public void update(boolean enabled) 
    {
        if (toolBar != null)
        {
            JComponent component = (JComponent)toolBar.getComponent(TOOLBAR_ID_PAUSE);
            component.setEnabled(enabled);
        }
    }
//pause clock actionclass    
}

/**
 * Class to stop a clock from processing and remove this window.
 */
public class StopClockAction extends  AbstractAction  
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -6070296927442144959L;

	public StopClockAction() 
    {}
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) 
    {
        ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.VIEWER_CLOSE_EVENT,SVG_SWTViewerFrame.this));
    }
}

/**
 * To switch view to full screen -note tied to F10 key press toggle!
 */
public class HotExitAction extends AbstractAction 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -770329832195446982L;

	public HotExitAction() {}

    public void actionPerformed(ActionEvent e) 
    {
        ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.F12_EVENT,SVG_SWTViewerFrame.this));
    }
//Exit action class
}


/**
 * To switch From Full view when user presses escape key
 */
public class RestoreWindowAction extends AbstractAction 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -2852587045781316622L;

	public RestoreWindowAction() {}

    public void actionPerformed(ActionEvent e) 
    {
        //only if we are in full window mode
        if (SVG_SWTViewerFrame.this.clock.isFullWindow() )
            ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.F10_EVENT,SVG_SWTViewerFrame.this));
    }
//full window actionclass
}


/**
 * To switch view to full screen -note tied to F10 key press toggle!
 */
public class FullWindowAction extends AbstractAction 
{
    private static final long serialVersionUID = 1648848231194931025L;

	public FullWindowAction() {}

    public void actionPerformed(ActionEvent e) 
    {
        ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.F10_EVENT,SVG_SWTViewerFrame.this));
    }
}

/**
 * To display the memory monitor.
 */
public class MemoryDialogAction extends AbstractAction 
{
    private static final long serialVersionUID = 7730608226450517147L;
	public MemoryDialogAction() {}
    public void actionPerformed(ActionEvent e) 
    {
        if (memoryDialogFrame == null) 
        {
            memoryDialogFrame = new MemoryMonitor();
            Rectangle fr = getBounds();
            Dimension md = memoryDialogFrame.getSize();
            memoryDialogFrame.setLocation(fr.x + (fr.width  - md.width) / 2, fr.y + (fr.height - md.height) / 2);
        }
        memoryDialogFrame.setVisible(true); //.show();
    }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
//
//		ALL LISTENER METHODS BELOW HERE
//
///////////////////////////////////////////////////////////////////////////////////////////////////////


// GVTTreeRendererListener 

/* (non-Javadoc)
 * @see org.apache.batik.swing.gvt.GVTTreeRendererListener#gvtRenderingPrepare(org.apache.batik.swing.gvt.GVTTreeRendererEvent)
 */
public void gvtRenderingPrepare(GVTTreeRendererEvent e) 
{
    svgCanvas.setCursor(WAIT_CURSOR);
}

/* (non-Javadoc)
 * @see org.apache.batik.swing.gvt.GVTTreeRendererListener#gvtRenderingStarted(org.apache.batik.swing.gvt.GVTTreeRendererEvent)
 */
public void gvtRenderingStarted(GVTTreeRendererEvent e) 
{
}

/* (non-Javadoc)
 * @see org.apache.batik.swing.gvt.GVTTreeRendererListener#gvtRenderingCompleted(org.apache.batik.swing.gvt.GVTTreeRendererEvent)
 */
public void gvtRenderingCompleted(GVTTreeRendererEvent e) 
{
    try
    {
	    svgCanvas.setCursor(DEFAULT_CURSOR);
		
	    // if everything has initialized then we should be set
	    if (this.isInitialized == false)
	    {
	        UpdateManager manager = svgCanvas.getUpdateManager(); 
	        //this.scriptingWindow = manager.getScriptingEnvironment().createWindow();
	        this.scriptingWindow = manager.getScriptingEnvironment().getWindow();
	        if (SVG_SWTViewerFrame.this.clock.hasMouseAlerts())
	        {
	            List<String> mouseAlerts = SVG_SWTViewerFrame.this.clock.getMouseAlerts();
	            for (int i=0; i<mouseAlerts.size(); i++)
	            {
	                String svgid = (String)mouseAlerts.get(i);
	                EventTarget x = (EventTarget)(svgCanvas.getSVGDocument().getElementById(svgid));
	               
	            	EventListener el = new EventListener() 
	            	{
	            		public void handleEvent(Event evt) 
	            		{
	            		    SVGOMGElement element= (SVGOMGElement)evt.getCurrentTarget();
	            		    String mouseAlert = element.getId();
	            		    SVG_SWTViewerFrame.this.clock.recieveMouseAlert(mouseAlert);
	            		}
	            	};
	            	((EventTarget)x).addEventListener("click", el, false);
	            }
	        }
	    }
	    this.resetMessageBarMessages();
    }
    catch (Exception ex) 
	{
        if (clockUserAgent != null) 
        {
            clockUserAgent.displayError(ex);
        }
        else
        {
            System.out.println("Exception rendering, Exception = " + ex.getMessage());
        }
	}
}

/* (non-Javadoc)
 * @see org.apache.batik.swing.gvt.GVTTreeRendererListener#gvtRenderingCancelled(org.apache.batik.swing.gvt.GVTTreeRendererEvent)
 */
public void gvtRenderingCancelled(GVTTreeRendererEvent e) 
{
    svgCanvas.setCursor(DEFAULT_CURSOR);
}

/* (non-Javadoc)
 * @see org.apache.batik.swing.gvt.GVTTreeRendererListener#gvtRenderingFailed(org.apache.batik.swing.gvt.GVTTreeRendererEvent)
 */
public void gvtRenderingFailed(GVTTreeRendererEvent e) 
{
    svgCanvas.setCursor(DEFAULT_CURSOR);
}
//UpdateManagerListener 

/* (non-Javadoc)
 * @see org.apache.batik.bridge.UpdateManagerListener#managerStarted(org.apache.batik.bridge.UpdateManagerEvent)
 */
public void managerStarted(UpdateManagerEvent e) 
{
    synchronized(this)
    {
	    //System.out.println("manager Started");    
	    SVG_SWTViewerFrame.this.managerRunning = true;
	    notify();
    }
}

/* (non-Javadoc)
 * @see org.apache.batik.bridge.UpdateManagerListener#managerSuspended(org.apache.batik.bridge.UpdateManagerEvent)
 */
public void managerSuspended(UpdateManagerEvent e) 
{
    //System.out.println("manager suspended");
}

/* (non-Javadoc)
 * @see org.apache.batik.bridge.UpdateManagerListener#managerResumed(org.apache.batik.bridge.UpdateManagerEvent)
 */
public void managerResumed(UpdateManagerEvent e) 
{
    //System.out.println("manager resumed");
}

/* (non-Javadoc)
 * @see org.apache.batik.bridge.UpdateManagerListener#managerStopped(org.apache.batik.bridge.UpdateManagerEvent)
 */
public void managerStopped(UpdateManagerEvent e) 
{
    synchronized(this)
    {
        //System.out.println("manager stopped");
	    SVG_SWTViewerFrame.this.managerRunning = false;
	    notify();
    }
}

/* (non-Javadoc)
 * @see org.apache.batik.bridge.UpdateManagerListener#updateStarted(org.apache.batik.bridge.UpdateManagerEvent)
 */
public void updateStarted(final UpdateManagerEvent e) 
{
    //System.out.println("manager update started");
}

/* (non-Javadoc)
 * @see org.apache.batik.bridge.UpdateManagerListener#updateCompleted(org.apache.batik.bridge.UpdateManagerEvent)
 */
public void updateCompleted(final UpdateManagerEvent e) 
{
	if (renderedOnce == false)
    {
	    synchronized(this)
	    {
	        renderedOnce = true;
	        notify();
	    }
    }
}

/* (non-Javadoc)
 * @see org.apache.batik.bridge.UpdateManagerListener#updateFailed(org.apache.batik.bridge.UpdateManagerEvent)
 */
public void updateFailed(UpdateManagerEvent e) 
{
    System.out.println("manager update failed");
}



///////////////////////////////////////////////////////////////////////////////////////////////////////
//
//		User Agent class
//
///////////////////////////////////////////////////////////////////////////////////////////////////////


/**
 * This class implements an SVG User Agent and is handy for getting information and displaying it as well.
 */
protected class ClockUserAgent implements SVGUserAgent 
{
    /**
     * Creates a new SVGUserAgent.
     */
    protected ClockUserAgent() 
    {}

    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#getUserStyleSheetURI()
     */
    public String getUserStyleSheetURI() 
    {
        return(null);
    }
    
    public String getAlternateStyleSheet() 
    {
        return(null);
    }
    
    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#getLanguages()
     */
    public String getLanguages() 
    {
        return(Locale.getDefault().getLanguage());
    }
    
    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#getMedia()
     */
    public String getMedia() 
    {
        return("screen");
    }
    
     /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#displayError(java.lang.String)
     */
    public void displayError(String message) 
    {
    	JOptionPane pane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
        JDialog dialog = pane.createDialog(SVG_SWTViewerFrame.this, "Error: " + message);
        dialog.setModal(false);
        dialog.setVisible(true);
    }
    
    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#handleElement(org.w3c.dom.Element, java.lang.Object)
     */
    public void handleElement(Element elt, Object data)
    {
    }
    
    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#supportExtension(java.lang.String)
     */
    public boolean supportExtension(String s) 
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#openLink(java.lang.String, boolean)
     */
    public void openLink(String uri, boolean newc) 
    {
         showSVGDocument(uri);
    }
    
    public void checkLoadExternalResource(ParsedURL resourceURL, ParsedURL docURL) 
    	throws SecurityException 
    {
        ExternalResourceSecurity s = getExternalResourceSecurity(resourceURL, docURL);
        if (s != null) 
        {
            s.checkLoadExternalResource();
        }
    }
   
    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#checkLoadScript(java.lang.String, org.apache.batik.util.ParsedURL, org.apache.batik.util.ParsedURL)
     */
    public void checkLoadScript(String scriptType, ParsedURL scriptURL, ParsedURL docURL) 
    throws SecurityException 
    {
		ScriptSecurity s = getScriptSecurity(scriptType,scriptURL,docURL);
		if (s != null) 
		{
		    s.checkLoadScript();
		} 
    }
    
    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#getScriptSecurity(java.lang.String, org.apache.batik.util.ParsedURL, org.apache.batik.util.ParsedURL)
     */
    public ScriptSecurity getScriptSecurity(String scriptType, ParsedURL scriptURL, ParsedURL docURL)
    {
        return(new RelaxedScriptSecurity(scriptType,scriptURL,docURL));
    }

    /* (non-Javadoc)
	 * @see org.apache.batik.swing.svg.SVGUserAgent#getExternalResourceSecurity(org.apache.batik.util.ParsedURL, org.apache.batik.util.ParsedURL)
	 */
	public ExternalResourceSecurity getExternalResourceSecurity(ParsedURL resourceURL, ParsedURL docURL)
	{
	    return(new RelaxedExternalResourceSecurity(resourceURL,docURL));
	}

    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#displayError(java.lang.Exception)
     */
    public void displayError(Exception ex) 
    {
        JErrorPane pane = new JErrorPane(ex, JOptionPane.ERROR_MESSAGE);
        JDialog dialog = pane.createDialog(SVG_SWTViewerFrame.this, "Exception Error:" + ex.getMessage());
        dialog.setModal(false);
        dialog.setVisible(true);
    }

    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#displayMessage(java.lang.String)
     */
    public void displayMessage(String message) 
    {
        messageBar.resetMessage();
	    messageBar.setClockMessageText(message);
    }

    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#showAlert(java.lang.String)
     */
    public void showAlert(String message) 
    {
        svgCanvas.showAlert(message);
    }

    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#showPrompt(java.lang.String)
     */
    public String showPrompt(String message) 
    {
        return svgCanvas.showPrompt(message);
    }

    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#showPrompt(java.lang.String, java.lang.String)
     */
    public String showPrompt(String message, String defaultValue) 
    {
        return svgCanvas.showPrompt(message, defaultValue);
    }

    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#showConfirm(java.lang.String)
     */
    public boolean showConfirm(String message) 
    {
        return svgCanvas.showConfirm(message);
    }

    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#getPixelUnitToMillimeter()
     */
    public float getPixelUnitToMillimeter() 
    {
        return 0.26458333333333333333333333333333f; // 96dpi
    }

    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#getPixelToMM()
     */
    public float getPixelToMM() 
    {
        return getPixelUnitToMillimeter();
        
    }

    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#getDefaultFontFamily()
     */
    public String getDefaultFontFamily() 
    {
        return(ClockGUI.resources.getString("Font.defaultFamily"));  
    }

    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#getMediumFontSize()
     */
    public float getMediumFontSize() 
    {
        // 9pt (72pt == 1in)
        return 9f * 25.4f / (72f * getPixelUnitToMillimeter());
    }

    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#getLighterFontWeight(float)
     */
    public float getLighterFontWeight(float f) 
    {
        // Round f to nearest 100...
        int weight = ((int)((f+50)/100))*100;
        switch (weight) {
        case 100: return 100;
        case 200: return 100;
        case 300: return 200;
        case 400: return 300;
        case 500: return 400;
        case 600: return 400;
        case 700: return 400;
        case 800: return 400;
        case 900: return 400;
        default:
            throw new IllegalArgumentException("Bad Font Weight: " + f);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#getBolderFontWeight(float)
     */
    public float getBolderFontWeight(float fontWeight) 
    {
        // Round f to nearest 100...
        int weight = ((int)((fontWeight+50)/100))*100;
        switch (weight) {
        case 100: return 600;
        case 200: return 600;
        case 300: return 600;
        case 400: return 600;
        case 500: return 600;
        case 600: return 700;
        case 700: return 800;
        case 800: return 900;
        case 900: return 900;
        default:
            throw new IllegalArgumentException("Invalid Font Weight: " + fontWeight);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#getXMLParserClassName()
     */
    public String getXMLParserClassName() 
    {
        return(XMLResourceDescriptor.getXMLParserClassName());
    }

    /* (non-Javadoc)
     * @see org.apache.batik.swing.svg.SVGUserAgent#isXMLParserValidating()
     */
    public boolean isXMLParserValidating() 
    {
        return(false);
    }
  } //user agent class
}
