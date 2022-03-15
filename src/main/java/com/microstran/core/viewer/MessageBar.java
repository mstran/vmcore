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
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

/**
 * This class creates a Message Bar and give us the advantage of being able 
 * to change the text for AM or PM without having to redisplay the whole 
 * image and capture the callbacks for rendering
 *
 * @author mstran
 */
public class MessageBar extends JPanel 
{
	private static final long serialVersionUID = 2731598056430431577L;

    
    /**
     * The message label for the main clock message
     */
    private JLabel clockMessage;

    /**
     * The string backing the clock message label
     */
    private String clockMessageText;

    /**
     * The string backing the AMPM message label
     */
    private String AMPMText;

    /**
     * The current display thread.
     */
    protected Thread displayThread;

    /**
     * Creates a new Message bar
     */
    public MessageBar(Color messageBarColor, boolean hasBorder) 
    {
        super(new BorderLayout(5, 5));
        
        JPanel messagePanel = new JPanel(new BorderLayout(0, 0));
        messagePanel.setBackground(messageBarColor);
        add("West", messagePanel);

        clockMessage = new JLabel();
        clockMessage.setHorizontalTextPosition(SwingConstants.CENTER);
        clockMessage.setHorizontalAlignment(SwingConstants.CENTER);
        if (hasBorder)
        {
	        BevelBorder bb;
	        bb = new BevelBorder(BevelBorder.LOWERED, getBackground().brighter().brighter(),
	                             getBackground(),getBackground().darker().darker(),
	                             getBackground());
	        clockMessage.setBorder(bb);
        }
        messagePanel.add(clockMessage);
        add(messagePanel);
        
        setClockMessageText("Constructing");
    }

    /**
     * sets the result to AM or PM, valid values are "am" or "pm"
     * Case is ignored
     * 
     * @param AMPMTxt
     */
    public void setAMPMText(String AMPMTxt) 
    {
        this.AMPMText = AMPMTxt;
    }
    
    /**
     * end the messageBar
     */
    public void endMessageBar()
    {
        if (displayThread != null) 
        {
            displayThread.interrupt();
        }
        displayThread = null;
    }
    
    /**
     * reset the message bar
     */
    public void resetMessage() 
    {
        setPreferredSize(new Dimension(0, getPreferredSize().height));
        if (displayThread != null) 
        {
            displayThread.interrupt();
        }
        displayThread = new DisplayThread();
        displayThread.start();
    }
    /**
     * A thread message interrupt to display the clock message
     * @param messageText
     */
    public void setClockMessageText(String messageText) 
    {
        
        this.clockMessageText = messageText;
        clockMessage.setText(clockMessageText + "  " +  AMPMText);
        if (displayThread != null) 
        {
            displayThread.interrupt();
            displayThread = null;
        }
        setPreferredSize(new Dimension(0, getPreferredSize().height));
        
    }
 
    /**
     * To display the main message
     */
    protected class DisplayThread extends Thread 
    {
        public DisplayThread() 
        {
            setPriority(Thread.MIN_PRIORITY);
        }

        public void run() 
        {
            clockMessage.setText("");
            try 
            {
                Thread.sleep(5000);
            }
            catch(InterruptedException e) 
            {
            	//System.out.println("message box exception");
            }
            clockMessage.setText(clockMessageText + "  " +  AMPMText);
        }
    }
    
    
}
