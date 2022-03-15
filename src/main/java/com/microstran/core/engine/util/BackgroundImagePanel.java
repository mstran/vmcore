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

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JPanel;


public class BackgroundImagePanel extends JPanel 
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -628680615674606856L;
	ImageIcon image = null;

	public BackgroundImagePanel()
	{
		super();

	}

	public BackgroundImagePanel(BorderLayout layout)
	{
	    super(layout);
	}
	
	public void paint(Graphics g) 
	{
		if( image != null )
		{
			// Draw the background image
		   	Rectangle d = this.getVisibleRect();
			for( int x = 0; x < d.width; x += image.getIconWidth() )
				for( int y = 0; y < d.height; y += image.getIconHeight() )
					g.drawImage( image.getImage(), x, y, null, null );
		}
		super.paint( g );
	}
	
	public void setBackgroundImage( ImageIcon image )
	{
		this.image = image;
	}
	
}
