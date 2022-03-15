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

import java.awt.geom.AffineTransform;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.RunnableQueue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.microstran.core.engine.ClockGUI;
import com.microstran.core.viewer.SVG_SWTViewerFrame;

/**
 * An  base class renderers for all clock types
 *
 * @author Mike Stran
 *
 */
public class Renderer 
{
	protected Clock clock;
	protected Document document;
	protected SVG_SWTViewerFrame frame;
	protected JSVGCanvas canvas;
	protected RunnableQueue queue;

	protected boolean enabled = true;
	
	protected int previousMinutes;
	protected int previousHours = -1;
	protected int strengthHours;
	protected int previousDay = -1;
	
	protected Element minuteHand;
	protected Element hourHand;
	protected Element secondHand;

	protected Element timerSecondHand;
	protected Element timerMinuteHand;

	protected Element strengthIndicator;
	protected Element moonPhaseDial;
	protected Element complication1;
	
	protected GraphicsNode gNodeSecondHand;
	protected GraphicsNode gNodeMinuteHand;
	protected GraphicsNode gNodeHourHand;
	
	protected GraphicsNode gNodeTimerSecondHand;
	protected GraphicsNode gNodeTimerMinuteHand;
	
	protected GraphicsNode gNodeStrengthHand;
	protected GraphicsNode gNodeMoonPhaseDial;
	protected GraphicsNode gNodeComplication1;
	
	/**
	 * Default constructor.
	 */
	public Renderer( )
	{
	}

	/**
	 * Abstract class method, subclasses should override and set elements as well as document
	 * @param document
	 */
	public void setDocument(Document document) {
	    this.document = document;
		this.minuteHand = document.getElementById("MinuteHand");
		this.hourHand = document.getElementById("HourHand");
		this.secondHand = document.getElementById("SecondHand");
		this.timerSecondHand = document.getElementById("timerSecondHand");
		this.timerMinuteHand = document.getElementById("timerMinuteHand");
		this.strengthIndicator = document.getElementById("StrengthIndicator");
		this.moonPhaseDial = document.getElementById("MoonPhaseDial");
		if (moonPhaseDial != null) {
			clock.setMoonCalc(new MoonPhaseCalculator());
		}
	}
	
	/**
	 * Recieve notification and advance time
	 * derived classes MUST implement this
	 * 
	 * @param applicationEvent The application event to process
	 * 
	 */
	public void render()
	{
	    if (!enabled)
	        return;
		try 
		{
		    if (queue == null)
	        {
		        frame = this.clock.getFrame(); 
		        if (frame == null)
		            return;
		        canvas = frame.getJSVGCanvas();
		        queue = canvas.getUpdateManager().getUpdateRunnableQueue(); 
		        BridgeContext context = frame.getJSVGCanvas().getUpdateManager().getBridgeContext();
		        if (secondHand != null) {
		        	gNodeSecondHand = context.getGraphicsNode(secondHand);
		        }
		        if (minuteHand != null) {
		        	gNodeMinuteHand = context.getGraphicsNode(minuteHand);
		        }
		        if (hourHand != null) {
		        	gNodeHourHand = context.getGraphicsNode(hourHand);
		        }
		        if (timerSecondHand != null) {
		        	gNodeTimerSecondHand = context.getGraphicsNode(timerSecondHand);
		        }
		        if (timerMinuteHand != null) {
		        	gNodeTimerMinuteHand = context.getGraphicsNode(timerMinuteHand);
		        }
		        if (strengthIndicator != null) {
		        	gNodeStrengthHand = context.getGraphicsNode(strengthIndicator);
		        }
		        if (moonPhaseDial != null) {
		        	gNodeMoonPhaseDial = context.getGraphicsNode(moonPhaseDial);
		        }
		        //a general purpose field for components you want to animate
		        if (complication1 != null) {
		        	gNodeComplication1 = context.getGraphicsNode(complication1);
		        }	        
	        }
		    //System.out.println("ID of QueueInvoker: " + Thread.currentThread().getId());
		    //This Runnable recycles the SAME THREAD, it's not creating new threads, validate on a different thread by un-commenting the code above and below
	        queue.preemptAndWait(new Runnable() 
			{
				public void run() 
				{
				    //System.out.println("ID of hands Rotater: " + Thread.currentThread().getId());
					rotateHands();
		        }
		    });
	        
	        /**
	         * THIS IS THE CRITICAL CODE PIECE BELOW, IT FORCES IMMEDIATE REPAINTING AND AVOIDS SITTING IN THE SWING REPAINT QUEUE 
	         */ 
	     // VVVVVVVVVVVVVVVVVVVVVVVVV
	        canvas.immediateRepaint();
	     // ^^^^^^^^^^^^^^^^^^^^^^^^^  
		}
		catch (Exception e) 
		{
		    String errStr = ClockGUI.resources.getString("ClockError.ExceptionInRendering");
			System.out.println(errStr + " " + e.getMessage());		
		}
	}
	
	public void rotateHands()
	{
	    try
	    {
	        if (clock==null)
	            return;
		    //in absolute terms NOT relative
		    ClockDate date 	= clock.getDate();
		    int minutes 	= date.getMinute();
			int hours   	= date.getHour();
			int seconds 	= date.getSecond();
			int fractsec    = date.getFractionalSeconds();
			
			int monthOfYear = date.getMonth();
    		int dayOfMonth  = date.getDayOfMonth();
    		int year        = date.getYear();
    		
			if ((strengthIndicator != null) && (previousHours == -1)){
			    previousHours = hours;
			    if (previousHours > 8){
			        previousHours = (12 - hours);
			        strengthHours = previousHours - 1;
			    }
			    else{
			        strengthHours = previousHours;
			    }
			}
			//nudge minute hand every other second
			if ((minuteHand != null) && (seconds%2 == 0)){
				updateMinuteHandTransform(minutes, seconds);
	    	}   
			
			if ((hourHand != null) && (minutes != previousMinutes))
			{
				previousMinutes = minutes;
	    		clock.getFrame().resetMessageBarMessages();
	    		updateHourHandTransform(hours, minutes);
			}
			
			if (strengthIndicator != null) {
				if (hours != previousHours)
	    		{
	    		    strengthHours++;
		    		previousHours = hours;
		    		if (strengthHours > 8) //if 9 go back to zero
		    		{
		    		    strengthHours = 0;
		    		}
	    		}		    
				float iRotation = (clock.getStrengthBaseDegrees() - (strengthHours*30))-(minutes * clock.getDegreePerMinutePerHour());
		        double iRot = degreeToRadians(iRotation);
			    AffineTransform it = AffineTransform.getRotateInstance(iRot,clock.getStrengthIndX(),clock.getStrengthIndY());
			    gNodeStrengthHand.setTransform(it);
			}
			if ((moonPhaseDial != null) && (dayOfMonth != previousDay)) {
				previousDay = dayOfMonth;
	            int phaseValue = clock.getMoonCalc().calcPhaseOfMoon(year, monthOfYear,dayOfMonth, hours, minutes, seconds);
	            float moonPhaseRotation = (phaseValue * clock.getDegreesPerMoonPhase()) - 90;
	            double mpRot = degreeToRadians(moonPhaseRotation);
			    AffineTransform mpt = AffineTransform.getRotateInstance(mpRot,clock.getMoonPhaseDialX(),clock.getMoonPhaseDialY());
			    gNodeMoonPhaseDial.setTransform(mpt);
			}
			
			if (secondHand != null) {
				updateSecondHandTransform(seconds, fractsec);
			}	    
		}
		catch (Exception e) 
		{
		    String errStr = ClockGUI.resources.getString("ClockError.ExceptionInRendering");
			System.out.println(errStr + " " + e.getMessage());		
		}
	}
	
	public void updateHourHandTransform(int hours, int minutes) {
		float hourRot = (hours * clock.getDegreePerHour())+(minutes * clock.getDegreePerMinutePerHour());
        double hRot = degreeToRadians(hourRot);
	    AffineTransform ht = AffineTransform.getRotateInstance(hRot,clock.getHourHandX(),clock.getHourHandY());
	    gNodeHourHand.setTransform(ht);
	}
	
	public void updateMinuteHandTransform(int minutes, int seconds) {
		double minuteRot = ((minutes * clock.getDegreePerMinute())+(seconds * clock.getDegreePerSecondPerHour()));
        double mRot = degreeToRadians(minuteRot);
		AffineTransform mt = AffineTransform.getRotateInstance(mRot,clock.getMinHandX(),clock.getMinHandY());
		gNodeMinuteHand.setTransform(mt);
		
		if (timerMinuteHand != null) {
			AffineTransform mht = AffineTransform.getRotateInstance(mRot,clock.getTimerMinuteHandX(),clock.getTimerMinuteHandY());
		    gNodeTimerMinuteHand.setTransform(mht);
		}
	}

	public void updateSecondHandTransform(int seconds, int fractsec) {
		float secondDegrees = 0F;
		float timerDegrees = 0F;
			secondDegrees = clock.getDegreePerFractionalSecond();
			timerDegrees = clock.getTimerDegreePerFractionalSecond(); //not supporting timer per 1/4
			
		double secondRot = (seconds * clock.getDegreePerSecond()) +  (fractsec *  secondDegrees);
        double sRot = degreeToRadians(secondRot);
	    AffineTransform st = AffineTransform.getRotateInstance(sRot,clock.getSecHandX(),clock.getSecHandY());
	    gNodeSecondHand.setTransform(st);
	    
	    if (timerSecondHand != null) {
	    	float tSecondRot = (seconds * clock.getTimerDegreePerSecond()) + (fractsec *  timerDegrees);
	        double tSRot = degreeToRadians(tSecondRot);
		    AffineTransform sht = AffineTransform.getRotateInstance(tSRot,clock.getTimerSecondHandX(),clock.getTimerSecondHandY());
		    gNodeTimerSecondHand.setTransform(sht);
	    }
	}
	
	/**
	 * @param application The application to set.
	 */
	public void setClock(Clock clock) 
	{
		this.clock = clock;
	}
	
	public Clock getClock()
	{
		return(this.clock);
	}
	
	public void reset()
	{
		this.previousMinutes = -1;
		queue = null;

	}
	
	/* (non-Javadoc)
	 * @see com.microstran.core.clock.Renderer#enableRenderer(boolean)
	 */
	public void enableRenderer(boolean enabled)
	{
	    this.enabled = enabled;
	}
	
	
	
	/**
	 * Call to end rendering
	 */
	public void endRenderer()
	{
	    this.enabled = false;
	    this.clock = null;
		this.document  = null;
		this.minuteHand = null;
		this.hourHand   = null;
		this.secondHand = null;
		this.gNodeSecondHand = null;
		this.gNodeMinuteHand = null;
		this.gNodeHourHand = null;
		this.queue = null;
	}
	
	/**
	 * Set text elements for the clock
	 * @param element
	 * @param text
	 */
	public void setTextElement(Element element, String text)
	{
		try
		{
		    Text textElement = document.createTextNode(text);
	        if ((element.getFirstChild() == null) || (element.getFirstChild().getNodeType() != Node.TEXT_NODE))
	            element.appendChild(textElement);
	        else
	            element.replaceChild(textElement, element.getFirstChild());
	       
	    }
	    catch (Exception e) 
		{
	        System.out.println("Error generating text = "+e.getMessage());
		}
	}
	
	public void recieveMouseAlert(String elementID)
	{}
	
	
	/**
	 * convert from degrees to radians for the rotation events
	 * @param degree
	 * @return
	 */
	protected double degreeToRadians(double degree)
	   {
	      return (degree/360.0) * 2 * Math.PI;
	   }
	
}
