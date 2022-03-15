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

import java.awt.*;
import java.awt.event.*;

import javax.swing.JWindow;

public class MsgBox extends JWindow  implements ActionListener 
{
	 /**
	 * 
	 */
	private static final long serialVersionUID = -3064824540496237145L;
	boolean id = false;
	 Button ok,can;
	
	public MsgBox(String msg, boolean okcan)
	{
	    super();
	    setLayout(new BorderLayout());
		add("Center",new Label(msg));
		addOKCancelPanel(okcan);
		createFrame();
		pack();
		setVisible(true);
	}
	 
	public MsgBox(Frame owner, String msg, boolean okcan)
    {
      super(owner);
	  setLayout(new BorderLayout());
	  add("Center",new Label(msg));
	  addOKCancelPanel(okcan);
	  createFrame();
	  pack();
	  setVisible(true);
	}
	
	void addOKCancelPanel( boolean okcan ) 
	{
	  Panel p = new Panel();
	  p.setLayout(new FlowLayout());
	  createOKButton( p );
	  if (okcan == true)
	     createCancelButton( p );
	  add("South",p);
	}
	
	void createOKButton(Panel p) 
	{
	  p.add(ok = new Button("OK"));
	  ok.addActionListener(this); 
	}
	
	void createCancelButton(Panel p) 
	{
	  p.add(can = new Button("Cancel"));
	  can.addActionListener(this);
	}
	
	void createFrame() 
	{
	  Dimension d = getToolkit().getScreenSize();
	  setLocation(d.width/3,d.height/3);
	}
	
	public void actionPerformed(ActionEvent ae)
	{
	  if(ae.getSource() == ok) 
	  {
	    id = true;
	    setVisible(false);
	    dispose();
	  }
	  else if(ae.getSource() == can) 
	  {
	    setVisible(false);
	    dispose();
	  }
	}
}
