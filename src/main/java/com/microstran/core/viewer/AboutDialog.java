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
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import com.microstran.core.engine.ClockGUI;

/**
 * A dialog to show version information as well as BATIK contributions
 * a Click or escape key press will remove this dialog
 *
 * @author mstran
 */
public class AboutDialog extends JWindow {
	private static final long serialVersionUID = 3125259141803824781L;
    public static final String IMAGE_PATH = "/images/SplashScreen.jpg";
    
    
    /**
     * Default constructor
     */
    public AboutDialog()
    {
        super();
        buildSplashScreen();
    }

    public AboutDialog(Frame owner)
    {
        super(owner);
        buildAboutBox();
        
        addKeyListener(new KeyAdapter()
                {
                public void keyPressed(KeyEvent e)
                {
                    if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    {
                        setVisible(false);
                        dispose();
                    }
                }
            });
        addMouseListener(new MouseAdapter()
                {
                public void mousePressed(MouseEvent e)
                {
                    setVisible(false);
                    dispose();
                }
            });
    }

    /**
     * 	center this on the screen
     */
    public Dimension setAbsoluteLocation()
    {
        Dimension ss = getToolkit().getScreenSize();
        Dimension ds = getPreferredSize();
        setLocation((ss.width  - ds.width) / 2,(ss.height - ds.height) / 2);
        return(ds);
    }
    
    /**
     * Set this relative to a frame we create the dialog box for
     * @param frame to create relative to
     */
    public void setRelativeLocation(Frame frame) 
    {
        Dimension invokerSize = frame.getSize();
        Point loc = frame.getLocation();
        Point invokerScreenLocation = new Point(loc.x, loc.y);

        Rectangle bounds = getBounds();
        int  dx = invokerScreenLocation.x+((invokerSize.width-bounds.width)/2);
        int  dy = invokerScreenLocation.y+((invokerSize.height - bounds.height)/2);
        Dimension screenSize = getToolkit().getScreenSize();

        if (dy+bounds.height>screenSize.height) 
        {
            dy = screenSize.height-bounds.height;
            dx = invokerScreenLocation.x<(screenSize.width>>1) ? invokerScreenLocation.x+invokerSize.width :
                invokerScreenLocation.x-bounds.width;
        }
        if (dx+bounds.width>screenSize.width) 
        {
            dx = screenSize.width-bounds.width;
        }

        if (dx<0) dx = 0;
        if (dy<0) dy = 0;
        setLocation(dx, dy);
    }

    /**
     * Adds the controls and graphics to the about dialog
     */
    protected void buildSplashScreen()
    {
        setBackground(Color.white);
        getContentPane().setBackground(Color.white);

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBackground(Color.white);

        //this.getClass().getClassLoader();

        //Add splash screen image
        try {
        mainPanel.add(BorderLayout.CENTER, new JLabel(new ImageIcon(new File(ClockGUI.resourcesPath+IMAGE_PATH).toURI().toURL())));
        }catch(Throwable t) {
        	System.out.println("Failed to load image Icon, exiting");
        	System.exit(0);
        }
        //Add version information
        String nameVersion = "2022 (c) Microstran Inc."+ 
        ClockGUI.resources.getString("BuildInfo.VMCC");
        JLabel VMCC_contribution = new JLabel(nameVersion);
        VMCC_contribution.setHorizontalTextPosition(SwingConstants.CENTER);
        VMCC_contribution.setHorizontalAlignment(SwingConstants.CENTER);
        
        mainPanel.add(BorderLayout.SOUTH, VMCC_contribution);
        
        ((JComponent)getContentPane()).setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.gray, Color.black),
              BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder
              (BorderFactory.createEmptyBorder(3, 3, 3, 3),BorderFactory.createLineBorder(Color.black)),
              BorderFactory.createEmptyBorder(10, 10, 10, 10))));
        getContentPane().add(mainPanel);
        pack();
    }

    
    /**
     * Adds the controls and graphics to the about dialog
     */
    protected void buildAboutBox()
    {
    	
    	setBackground(Color.white);
        getContentPane().setBackground(Color.white);

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBackground(Color.white);

        //this.getClass().getClassLoader();

        //Add splash screen image
        try {
        mainPanel.add(BorderLayout.CENTER, new JLabel(new ImageIcon(new File(ClockGUI.resourcesPath+IMAGE_PATH).toURI().toURL())));
        }catch(Throwable t) {
        	System.out.println("Failed to load image Icon, exiting");
        	System.exit(0);
        }
        //Add version information
        String nameVersion = "2022 (c) Microstran Inc."+ 
        ClockGUI.resources.getString("BuildInfo.VMCC");
                
        JLabel VMCC_contribution = new JLabel(nameVersion);
        VMCC_contribution.setHorizontalTextPosition(SwingConstants.CENTER);
        VMCC_contribution.setHorizontalAlignment(SwingConstants.CENTER);
        
        mainPanel.add(BorderLayout.SOUTH, VMCC_contribution);

        JPanel contributionPanel = new JPanel(new BorderLayout());
        contributionPanel.setBackground(Color.white);
        contributionPanel.add(mainPanel, BorderLayout.NORTH);
        //Add text areaa for the contribution lines 
        
        //build up string
        StringBuffer openSourceContrib = new StringBuffer();
        openSourceContrib.append(ClockGUI.resources.getString("Copyrights.Batik") + "\n");
        openSourceContrib.append(ClockGUI.resources.getString("Copyrights.JCalendar") + "\n");
        openSourceContrib.append(ClockGUI.resources.getString("Copyrights.PiecesOfTime"));
         JTextArea openSourceContribution = new JTextArea(openSourceContrib.toString())
            { 
              {
                  setLineWrap(true); 
                  setWrapStyleWord(true); 
                  setEnabled(false); 
                  setRows(6); 
              }
            };

        openSourceContribution.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        contributionPanel.add(openSourceContribution,BorderLayout.CENTER);
        
        JButton okButton = new JButton();
		okButton.addActionListener(new ActionListener() 
		    {
			public void actionPerformed(ActionEvent e) 
			{
				do_okButton_actionPerformed();
			}
		});
		okButton.setText(ClockGUI.resources.getString("AboutBox.ok"));
		okButton.setPreferredSize(new Dimension(50,25));
		contributionPanel.add(okButton,BorderLayout.SOUTH);
        
        
        ((JComponent)getContentPane()).setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.gray, Color.black),
              BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder
              (BorderFactory.createEmptyBorder(3, 3, 3, 3),BorderFactory.createLineBorder(Color.black)),
              BorderFactory.createEmptyBorder(10, 10, 10, 10))));
        
        getContentPane().add(contributionPanel);
        pack();
    }
    
	protected void do_okButton_actionPerformed() 
	{
	    this.setVisible(false);
        dispose();
	}
}
