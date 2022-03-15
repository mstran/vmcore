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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.microstran.core.alarm.ClockAlarm;
import com.microstran.core.clock.Clock;
import com.microstran.core.clock.ClockDate;
import com.microstran.core.engine.AlarmEventListener;
import com.microstran.core.engine.ClockGUI;
import com.microstran.core.engine.EngineHelper;
import com.microstran.core.graphics.DrawingPanel;
import com.toedter.calendar.JDateChooser;
import com.toedter.components.JSpinField;



/**
 * Main configuration dialog class
 * @author Mstran
 */
public class ConfigDLG extends JDialog 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1599687595935333902L;


	//The clocks to configure + data settings
	private Clock clock;
	private String clockID = null;
	private Map<String, Clock> allClocks = EngineHelper.getAllDefinedClocks();
	private boolean startingUp = true;
	private boolean okButtonState = false;
	
	private ClockAlarm selectedAlarm = null;
	
	
	
	//Tabbed display and 2 Main Panels
	private JTabbedPane tabbedPane;
	
	/********************************************************************************
	**
	**	Config Clock Tab members
	**
	********************************************************************************/
	private JPanel configPanel;
	//Image panel
	private JPanel imagePanel;
	private DrawingPanel canvas;
	private JLabel clocksLabel;
	private JTextField captionText;
	//panel for scrolling buttons
	private JPanel ScrollLeftRight;
	private JButton scrollRight;
	private JButton scrollLeft;
	//Panel for time and all other setting
	//we put the date time and scale panels in here
	private JPanel 	timeScaleSettingsPanel;
	private JComboBox<String> timeZoneComboBox;
	private JLabel timeZoneLabel;
	private JCheckBox showDate;
	private JCheckBox showTime;
	private JCheckBox twelveHourDisplay;
	private JCheckBox canScale;
	private JCheckBox preserveAspectRatio;
	private JCheckBox alwaysOnTop;

	private JSpinField minuteClockCycleField;
	private JLabel hourClockCycleLabel;
	private JCheckBox cycleClock;
	private JCheckBox limitToGroup;
	private JComboBox clockGroupsComboBox;
	
	
	//OK Cancel Buttons Panel
	private JPanel oKCancelPanel;
	private JButton okButton;
	private JButton cancelButton;

		
	/********************************************************************************
	**
	**	Alarms Tab members
	**
	********************************************************************************/
	private JPanel alarmsPanel;
	//Configured Alarms Panel
	private JPanel configuredAlarmsPanel;
	private JList  configuredAlarmsList;
	private DefaultListModel listModel;
	private JButton addAlarmButton;
	private JButton removeAlarmButton;
	private JButton updateAlarmButton;
	private ButtonGroup enabledDiabledAlarmGroup;
	private JRadioButton alarmEnabledRadioButton;
	private JRadioButton alarmDisabledRadioButton;
	private JPanel addRemovePanel;
	//panel that contains the sound and date settings
	private JPanel dateSoundPanel;
	private JComboBox soundComboBox;
	private JLabel soundLabel;
	private JDateChooser dateComboBox;
	private JLabel dateLabel;
	private JSpinField minuteField;
	private JLabel minuteLabel;
	private JSpinField hourField;
	private JLabel hourLabel;
	private ButtonGroup amPmButtonGroup;
	private JRadioButton amRadioButton;
	private JRadioButton pmRadioButton;
	private JButton testSoundButton;
	//panel that contains the repeate flags
	private JPanel repeatSettingsPanel;
	private JLabel repeatLabel;
	private ButtonGroup howOftenGroup;
	private JRadioButton onceRadioButton;
	private JRadioButton weeklyRadioButton;
	
	
	private static final int MAX_WIDTH=400;
	private static final int UPPER_PANEL_HEIGHT = 150;
	private static final int LOWER_PANEL_HEIGHT = 200;
	private static final int CANVAS_HEIGHT = 120;
	private static final int SCROLL_HEIGHT = 34;
	
	/**
	 *  Main Constructor
	 */
	public ConfigDLG() 
	{
	    super();
	    setSize(new Dimension(550, 500));
		
	    startingUp = true;
		okButtonState = false;
		
		setResizable(false);
		setModal(true);

		//set the content pane to use gridbaglayout
		getContentPane().setLayout(new GridBagLayout());

		//Setup OK Cancel Panel
		oKCancelPanel = new JPanel();
		oKCancelPanel.setPreferredSize(new Dimension(MAX_WIDTH, 25));
		final FlowLayout okCancelflowLayout;
		okCancelflowLayout = new FlowLayout();
		okCancelflowLayout.setAlignment(FlowLayout.RIGHT);
		oKCancelPanel.setLayout(okCancelflowLayout);
		final GridBagConstraints okCancelPanelGridBagConstraints;
		okCancelPanelGridBagConstraints = new GridBagConstraints();
		okCancelPanelGridBagConstraints.anchor = GridBagConstraints.SOUTH;
		okCancelPanelGridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		okCancelPanelGridBagConstraints.insets = new Insets(0, 0, 0, 0);
		okCancelPanelGridBagConstraints.gridy = 1;
		okCancelPanelGridBagConstraints.weighty = 0;
		okCancelPanelGridBagConstraints.weightx = 0;
		getContentPane().add(oKCancelPanel, okCancelPanelGridBagConstraints);
		okButton = new JButton();
		okButton.addActionListener(new ActionListener() 
		    {
			public void actionPerformed(ActionEvent e) 
			{
				do_okButton_actionPerformed();
			}
		});
		okButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		oKCancelPanel.add(okButton);
		okButton.setText(ClockGUI.resources.getString("ConfigDLG.save"));
		cancelButton = new JButton();
		cancelButton.setVerifyInputWhenFocusTarget(false);
		cancelButton.addActionListener(new ActionListener() 
		    {
			public void actionPerformed(ActionEvent e) 
			{
				do_cancelButton_actionPerformed();
			}
		});
		cancelButton.setHorizontalAlignment(SwingConstants.RIGHT);
		oKCancelPanel.add(cancelButton);
		cancelButton.setText(ClockGUI.resources.getString("ConfigDLG.cancel"));

		//Setup the tabbed pane
		tabbedPane = new JTabbedPane();
		final GridBagConstraints tabPanegridBagConstraints;
		tabPanegridBagConstraints = new GridBagConstraints();
		tabPanegridBagConstraints.ipady = 42;
		tabPanegridBagConstraints.fill = GridBagConstraints.BOTH;
		tabPanegridBagConstraints.weighty = 1;
		tabPanegridBagConstraints.weightx = 1;
		getContentPane().add(tabbedPane, tabPanegridBagConstraints);

		//add all the controls for the clock configuration tab
		setupClockTab();
		
		//add all the controls for the alarms tab
		setupAlarmsTab();
		
		setTitle(ClockGUI.resources.getString("ConfigDLG.config"));
	}

	
	/**
	 *  Method to setup the main clock tab
	 */
	private void setupClockTab()
	{
	    configPanel = new JPanel();
	    configPanel.setBounds(0, 0, 450, 420);
	    configPanel.setPreferredSize(new Dimension(450,420));
		
	    configPanel.setLayout(new BorderLayout());
		
		tabbedPane.addTab(ClockGUI.resources.getString("ConfigDLG.settings"), null, configPanel, ClockGUI.resources.getString("ConfigDLG.configClock"));

		imagePanel = new JPanel();
		imagePanel.setMinimumSize(new Dimension(450, 125));
		imagePanel.setMaximumSize(new Dimension(450, 125));
		imagePanel.setPreferredSize(new Dimension(450, 125));
		imagePanel.setLayout(new BorderLayout());
		canvas = new DrawingPanel();
		canvas.setBorder(new BevelBorder(BevelBorder.LOWERED));
		canvas.setPreferredSize(new Dimension(MAX_WIDTH, CANVAS_HEIGHT));
		imagePanel.add(canvas);
		
		//Scroll LeftRight Panel
		ScrollLeftRight = new JPanel();
		ScrollLeftRight.setBorder(new BevelBorder(BevelBorder.RAISED));
		ScrollLeftRight.setPreferredSize(new Dimension(MAX_WIDTH, SCROLL_HEIGHT));
		
		final FlowLayout leftRightPanelflowLayout;
		leftRightPanelflowLayout = new FlowLayout();
		leftRightPanelflowLayout.setHgap(7);
		leftRightPanelflowLayout.setAlignment(FlowLayout.RIGHT);
		ScrollLeftRight.setLayout(leftRightPanelflowLayout);
		captionText = new JTextField();
		captionText.setToolTipText(ClockGUI.resources.getString("ConfigDLG.addCaption"));
		captionText.setPreferredSize(new Dimension(175, 20));
		ScrollLeftRight.add(captionText);
		clocksLabel = new JLabel();
		ScrollLeftRight.add(clocksLabel);
		clocksLabel.setText(ClockGUI.resources.getString("ConfigDLG.clocks"));
		scrollLeft = new JButton();
		scrollLeft.addActionListener(new ActionListener() 
		    {
			public void actionPerformed(ActionEvent e) 
			{
				do_scrollLeft_actionPerformed();
			}
		});
		ScrollLeftRight.add(scrollLeft);
		scrollLeft.setText("<");
		scrollRight = new JButton();
		scrollRight.addActionListener(new ActionListener() 
		    {
			public void actionPerformed(ActionEvent e) 
			{
				do_scrollRight_actionPerformed();
			}
		});
		ScrollLeftRight.add(scrollRight);
		scrollRight.setText(">");
		imagePanel.add(ScrollLeftRight, BorderLayout.SOUTH);
		configPanel.add(imagePanel, BorderLayout.NORTH);
		
		
		//Setup panel that will include the time panel and scale panels
	    timeScaleSettingsPanel = new JPanel();
	    timeScaleSettingsPanel.setMaximumSize(new Dimension(450, 190));
	    timeScaleSettingsPanel.setMinimumSize(new Dimension(450, 190));
	    timeScaleSettingsPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
	    timeScaleSettingsPanel.setLayout(new GridBagLayout());
	    timeScaleSettingsPanel.setPreferredSize(new Dimension(450, 190));
	    timeZoneComboBox = new JComboBox();
	    timeZoneComboBox.setPreferredSize(new Dimension(120, 25));
	    timeZoneComboBox.addActionListener(new ActionListener() 
	    {
			public void actionPerformed(ActionEvent e) 
			{
				do_timeZoneComboBox_actionPerformed();
			}
		});
	    final GridBagConstraints gridBagConstraints_36 = new GridBagConstraints();
	    gridBagConstraints_36.insets = new Insets(10, 0, 0, 0);
	    gridBagConstraints_36.anchor = GridBagConstraints.WEST;
	    gridBagConstraints_36.gridx = 0;
	    gridBagConstraints_36.gridy = 0;
	    gridBagConstraints_36.fill = GridBagConstraints.BOTH;
		timeScaleSettingsPanel.add(timeZoneComboBox, gridBagConstraints_36);
	    
		timeZoneLabel = new JLabel();
	    final GridBagConstraints gridBagConstraints_26 = new GridBagConstraints();
	    gridBagConstraints_26.insets = new Insets(10, 5, 0, 0);
	    gridBagConstraints_26.anchor = GridBagConstraints.WEST;
	    gridBagConstraints_26.gridx = 1;
	    gridBagConstraints_26.gridy = 0;
	    timeScaleSettingsPanel.add(timeZoneLabel, gridBagConstraints_26);
	    timeZoneLabel.setText(ClockGUI.resources.getString("ConfigDLG.timeZone"));
	    
		
	    showDate = new JCheckBox();
	    final GridBagConstraints gridBagConstraints_23 = new GridBagConstraints();
	    gridBagConstraints_23.insets = new Insets(5, 0, 0, 0);
	    gridBagConstraints_23.anchor = GridBagConstraints.WEST;
	    gridBagConstraints_23.gridx = 0;
	    gridBagConstraints_23.gridy = 1;
	    showDate.addActionListener(new ActionListener() 
			    {
				public void actionPerformed(ActionEvent e) 
				{
				    do_showDate_actionPerformed();
				}
			});
	    timeScaleSettingsPanel.add(showDate, gridBagConstraints_23);
	    showDate.setText(ClockGUI.resources.getString("ConfigDLG.showDate"));
	    twelveHourDisplay = new JCheckBox();
	    final GridBagConstraints gridBagConstraints_24 = new GridBagConstraints();
	    gridBagConstraints_24.anchor = GridBagConstraints.WEST;
	    gridBagConstraints_24.gridx = 1;
	    gridBagConstraints_24.gridy = 1;
		twelveHourDisplay.addActionListener(new ActionListener() 
			    {
				public void actionPerformed(ActionEvent e) 
				{
				    do_twelveHourDisplay_actionPerformed();
				}
			});
	    timeScaleSettingsPanel.add(twelveHourDisplay, gridBagConstraints_24);
	    twelveHourDisplay.setText(ClockGUI.resources.getString("ConfigDLG.useFormat"));
	    preserveAspectRatio = new JCheckBox();
	    final GridBagConstraints gridBagConstraints_25 = new GridBagConstraints();
	    gridBagConstraints_25.anchor = GridBagConstraints.WEST;
	    gridBagConstraints_25.gridx = 0;
	    gridBagConstraints_25.gridy = 2;
		preserveAspectRatio.addActionListener(new ActionListener() 
			    {
				public void actionPerformed(ActionEvent e) 
				{
				    do_perserveAspectRatio_actionPerformed();
				}
			});
	    timeScaleSettingsPanel.add(preserveAspectRatio, gridBagConstraints_25);
	    preserveAspectRatio.setText(ClockGUI.resources.getString("ConfigDLG.preserveAspect"));
	    showTime = new JCheckBox();
	    
	    final GridBagConstraints gridBagConstraints_28 = new GridBagConstraints();
	    gridBagConstraints_28.anchor = GridBagConstraints.WEST;
	    gridBagConstraints_28.gridx = 1;
		gridBagConstraints_28.gridy = 2;
	    showTime.addActionListener(new ActionListener() 
			    {
				public void actionPerformed(ActionEvent e) 
				{
				    do_showTime_actionPerformed();
				}
			});	    
		timeScaleSettingsPanel.add(showTime, gridBagConstraints_28);
		showTime.setText(ClockGUI.resources.getString("ConfigDLG.showTime"));
	    canScale = new JCheckBox();
	    final GridBagConstraints gridBagConstraints_29 = new GridBagConstraints();
	    gridBagConstraints_29.anchor = GridBagConstraints.WEST;
	    gridBagConstraints_29.gridx = 0;
	    gridBagConstraints_29.gridy = 3;
		canScale.addActionListener(new ActionListener() 
			    {
				public void actionPerformed(ActionEvent e) 
				{
				    do_canScale_actionPerformed();
				}
			});
	    timeScaleSettingsPanel.add(canScale, gridBagConstraints_29);
	    canScale.setText(ClockGUI.resources.getString("ConfigDLG.allowScale"));
	    alwaysOnTop = new JCheckBox();
	    final GridBagConstraints gridBagConstraints_30 = new GridBagConstraints();
	    gridBagConstraints_30.anchor = GridBagConstraints.WEST;
	    gridBagConstraints_30.gridx = 1;
	    gridBagConstraints_30.gridy = 3;
		alwaysOnTop.addActionListener(new ActionListener() 
			    {
				public void actionPerformed(ActionEvent e) 
				{
				    do_alwaysOnTop_actionPerformed();
				}
			});
	    timeScaleSettingsPanel.add(alwaysOnTop, gridBagConstraints_30);
	    alwaysOnTop.setText(ClockGUI.resources.getString("ConfigDLG.alwaysOnTop"));
	
	    cycleClock = new JCheckBox();
	    final GridBagConstraints gridBagConstraints_33 = new GridBagConstraints();
	    gridBagConstraints_33.anchor = GridBagConstraints.WEST;
	    gridBagConstraints_33.gridx = 0;
	    gridBagConstraints_33.gridy = 4;
		
	    cycleClock.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
			    do_cycleClock_actionPerformed();
			}
		});	
		    
		timeScaleSettingsPanel.add(cycleClock, gridBagConstraints_33);

		hourClockCycleLabel = new JLabel();
		hourClockCycleLabel.setPreferredSize(new Dimension(2, 20));
		final GridBagConstraints gridBagConstraints_32 = new GridBagConstraints();
		gridBagConstraints_32.fill = GridBagConstraints.BOTH;
		gridBagConstraints_32.insets = new Insets(0, 2, 0, 0);
		gridBagConstraints_32.anchor = GridBagConstraints.WEST;
		gridBagConstraints_32.gridx = 0;
		gridBagConstraints_32.gridy = 5;
		timeScaleSettingsPanel.add(hourClockCycleLabel, gridBagConstraints_32);
		hourClockCycleLabel.setText(ClockGUI.resources.getString("ConfigDLG.cycleHours"));
		cycleClock.setText(ClockGUI.resources.getString("ConfigDLG.cycleClock"));

		minuteClockCycleField = new JSpinField();
		minuteClockCycleField.setPreferredSize(new Dimension(8, 20));
		minuteClockCycleField.setMaximum(240);
		minuteClockCycleField.setMinimum(1);
		minuteClockCycleField.setValue(10);
		final GridBagConstraints gridBagConstraints_31 = new GridBagConstraints();
		gridBagConstraints_31.insets = new Insets(2, 0, 2, 0);
		gridBagConstraints_31.anchor = GridBagConstraints.WEST;
		gridBagConstraints_31.fill = GridBagConstraints.BOTH;
		gridBagConstraints_31.gridx = 1;
		gridBagConstraints_31.gridy = 5;
		timeScaleSettingsPanel.add(minuteClockCycleField, gridBagConstraints_31);
		
		clockGroupsComboBox = new JComboBox();
		clockGroupsComboBox.setPreferredSize(new Dimension(120, 25));
		
		final GridBagConstraints gridBagConstraints_35 = new GridBagConstraints();
	    gridBagConstraints_35.anchor = GridBagConstraints.WEST;
	    gridBagConstraints_35.fill = GridBagConstraints.BOTH;
		gridBagConstraints_35.gridx = 0;
		gridBagConstraints_35.gridy = 6;
	    timeScaleSettingsPanel.add(clockGroupsComboBox, gridBagConstraints_35);
	    clockGroupsComboBox.addActionListener(new ActionListener() 
			    {
				public void actionPerformed(ActionEvent e) 
				{
					do_groupLimitComboBox_actionPerformed();
				}
			});	
	    limitToGroup = new JCheckBox();
	    final GridBagConstraints gridBagConstraints_34 = new GridBagConstraints();
	    gridBagConstraints_34.anchor = GridBagConstraints.WEST;
	    gridBagConstraints_34.gridx = 1;
	    gridBagConstraints_34.gridy = 6;
		
	    limitToGroup.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
			    do_cycleClockLimitToGroup_actionPerformed();
			}
		});	
		timeScaleSettingsPanel.add(limitToGroup, gridBagConstraints_34);
		limitToGroup.setText(ClockGUI.resources.getString("ConfigDLG.cycleGroupLimit"));
		
		configPanel.add(timeScaleSettingsPanel, BorderLayout.SOUTH);
	    
	}
	
	/**
	 *  Method to setup the main alarms tab
	 */
	private void setupAlarmsTab()
	{
	    //main alarms panel
		alarmsPanel = new JPanel();
		alarmsPanel.setBounds(0, 0, 450, 420);
		alarmsPanel.setPreferredSize(new Dimension(450,420));
		alarmsPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
	    alarmsPanel.setLayout(new BorderLayout());
		tabbedPane.addTab(ClockGUI.resources.getString("ConfigDLG.alarms"), null, alarmsPanel, ClockGUI.resources.getString("ConfigDLG.configAlarms"));
		//configured alarms panel
		configuredAlarmsPanel = new JPanel();
		configuredAlarmsPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		configuredAlarmsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		configuredAlarmsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		configuredAlarmsPanel.setLayout(new GridBagLayout());
		configuredAlarmsPanel.setPreferredSize(new Dimension(MAX_WIDTH, UPPER_PANEL_HEIGHT));
		alarmsPanel.add(configuredAlarmsPanel, BorderLayout.NORTH);
		listModel = new DefaultListModel();
		configuredAlarmsList = new JList(listModel);
		configuredAlarmsList.setPreferredSize(new Dimension(145, 140));
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.anchor = GridBagConstraints.WEST;
		gridBagConstraints_1.insets = new Insets(2, 0, 2, 0);
		gridBagConstraints_1.gridwidth = 1;
		gridBagConstraints_1.gridheight = 4;
		gridBagConstraints_1.gridx = 0;
		gridBagConstraints_1.gridy = 0;
		configuredAlarmsList.addListSelectionListener(new ListSelectionListener() {
		    public void valueChanged(ListSelectionEvent e) {
		        do_configuredAlarmsList_valueChanged();
		    }
		});
		configuredAlarmsList.addHierarchyListener(new HierarchyListener() {
		    public void hierarchyChanged(HierarchyEvent e) {
		        do_configuredAlarmsList_hierarchyChanged();
		    }
		});
		configuredAlarmsPanel.add(configuredAlarmsList, gridBagConstraints_1);
		alarmEnabledRadioButton = new JRadioButton();
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.anchor = GridBagConstraints.WEST;
		gridBagConstraints_2.gridx = 1;
		alarmEnabledRadioButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        do_alarmEnabledRadioButton_actionPerformed();
		    }
		});
		configuredAlarmsPanel.add(alarmEnabledRadioButton, gridBagConstraints_2);
		alarmEnabledRadioButton.setText(ClockGUI.resources.getString("ConfigDLG.enabled"));

		alarmDisabledRadioButton = new JRadioButton();
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.anchor = GridBagConstraints.WEST;
		gridBagConstraints_3.gridx = 1;
		gridBagConstraints_3.gridy = 1;
		alarmDisabledRadioButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        do_alarmDisabledRadioButton_actionPerformed();
		    }
		});
		configuredAlarmsPanel.add(alarmDisabledRadioButton, gridBagConstraints_3);
		alarmDisabledRadioButton.setText(ClockGUI.resources.getString("ConfigDLG.disabled"));
		
		
		
		addRemovePanel = new JPanel();
		addRemovePanel.setPreferredSize(new Dimension(145, 60));
		addRemovePanel.setLayout(new FlowLayout());

		final GridBagConstraints gridBagConstraints_13 = new GridBagConstraints();
		gridBagConstraints_13.gridx = 1;
		gridBagConstraints_13.gridy = 2;
		configuredAlarmsPanel.add(addRemovePanel, gridBagConstraints_13);
		addAlarmButton = new JButton();
		addAlarmButton.setMargin(new Insets(2, 2, 2, 2));
		addAlarmButton.setIconTextGap(2);
		addAlarmButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        do_addAlarmButton_actionPerformed();
		    }
		});
		addRemovePanel.add(addAlarmButton);
		addAlarmButton.setText(ClockGUI.resources.getString("ConfigDLG.add"));
		removeAlarmButton = new JButton();
		removeAlarmButton.setMargin(new Insets(2, 2, 2, 2));
		removeAlarmButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        do_removeAlarmButton_actionPerformed();
		    }
		});
		addRemovePanel.add(removeAlarmButton);
		removeAlarmButton.setText(ClockGUI.resources.getString("ConfigDLG.remove"));
		
		updateAlarmButton = new JButton();
		updateAlarmButton.setMargin(new Insets(2, 2, 2, 2));
		updateAlarmButton.setHorizontalAlignment(SwingConstants.LEFT);
		updateAlarmButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        do_updateAlarmButton_actionPerformed();
		    }
		});
		addRemovePanel.add(updateAlarmButton);
		updateAlarmButton.setText(ClockGUI.resources.getString("ConfigDLG.update"));
		
		dateSoundPanel = new JPanel();
		dateSoundPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		dateSoundPanel.setLayout(new GridBagLayout());
		dateSoundPanel.setPreferredSize(new Dimension(335, LOWER_PANEL_HEIGHT));
		alarmsPanel.add(dateSoundPanel, BorderLayout.EAST);
		soundComboBox = new JComboBox();
		soundComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		soundComboBox.setPreferredSize(new Dimension(180, 20));
		final GridBagConstraints gridBagConstraints_5 = new GridBagConstraints();
		gridBagConstraints_5.insets = new Insets(4, 0, 2, 0);
		gridBagConstraints_5.fill = GridBagConstraints.BOTH;
		gridBagConstraints_5.anchor = GridBagConstraints.WEST;
		gridBagConstraints_5.gridwidth = 2;
		soundComboBox.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        do_soundComboBox_actionPerformed();
		    }
		});
		dateSoundPanel.add(soundComboBox, gridBagConstraints_5);
		soundLabel = new JLabel();
		soundLabel.setHorizontalAlignment(SwingConstants.LEFT);
		final GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
		gridBagConstraints_4.anchor = GridBagConstraints.WEST;
		gridBagConstraints_4.fill = GridBagConstraints.BOTH;
		gridBagConstraints_4.insets = new Insets(0, 2, 0, 0);
		gridBagConstraints_4.gridx = 3;
		dateSoundPanel.add(soundLabel, gridBagConstraints_4);
		soundLabel.setText(ClockGUI.resources.getString("ConfigDLG.sound"));
		dateComboBox = new JDateChooser();
		dateComboBox.addInputMethodListener(new InputMethodListener() {
		    public void inputMethodTextChanged(InputMethodEvent e) {
		        do_dateComboBox_inputMethodTextChanged();
		    }
		    public void getInputMethodRequests(InputMethodEvent e){
		        do_inputMethodRequestsChanged(e);
		    }
		    public void caretPositionChanged(InputMethodEvent e) {
		        do_dateComboBox_caretPositionChanged();
		    }
		});
		dateComboBox.setPreferredSize(new Dimension(180, 25));
		final GridBagConstraints gridBagConstraints_6 = new GridBagConstraints();
		gridBagConstraints_6.insets = new Insets(2, 0, 2, 0);
		gridBagConstraints_6.anchor = GridBagConstraints.WEST;
		gridBagConstraints_6.gridwidth = 2;
		gridBagConstraints_6.fill = GridBagConstraints.BOTH;
		gridBagConstraints_6.gridy = 1;
		dateComboBox.addInputMethodListener(new InputMethodListener() {
		    public void inputMethodTextChanged(InputMethodEvent e) {
		        do_dateComboBox_inputMethodTextChanged();
		    }
		    public void caretPositionChanged(InputMethodEvent e) {
		        do_dateComboBox_caretPositionChanged();
		    }
		});
		dateSoundPanel.add(dateComboBox, gridBagConstraints_6);
		dateLabel = new JLabel();
		final GridBagConstraints gridBagConstraints_7 = new GridBagConstraints();
		gridBagConstraints_7.insets = new Insets(0, 2, 0, 0);
		gridBagConstraints_7.anchor = GridBagConstraints.WEST;
		gridBagConstraints_7.gridy = 1;
		gridBagConstraints_7.gridx = 3;
		dateSoundPanel.add(dateLabel, gridBagConstraints_7);
		dateLabel.setText(ClockGUI.resources.getString("ConfigDLG.date"));
		hourField = new JSpinField();
		hourField.addInputMethodListener(new InputMethodListener() {
		    public void inputMethodTextChanged(InputMethodEvent e) {
		        do_hourField_inputMethodTextChanged();
		    }
		    public void caretPositionChanged(InputMethodEvent e) {
		        do_hourField_caretPositionChanged();
		    }
		});
		hourField.setPreferredSize(new Dimension(75, 20));
		hourField.setMaximum(12);
		hourField.setMinimum(1);
		hourField.setValue(1);
		final GridBagConstraints gridBagConstraints_8 = new GridBagConstraints();
		gridBagConstraints_8.insets = new Insets(2, 0, 2, 0);
		gridBagConstraints_8.anchor = GridBagConstraints.WEST;
		gridBagConstraints_8.fill = GridBagConstraints.BOTH;
		gridBagConstraints_8.gridy = 3;
		hourField.addInputMethodListener(new InputMethodListener() {
		    public void inputMethodTextChanged(InputMethodEvent e) {
		        do_hourField_inputMethodTextChanged();
		    }
		    public void caretPositionChanged(InputMethodEvent e) {
		        do_hourField_caretPositionChanged();
		    }
		});
		dateSoundPanel.add(hourField, gridBagConstraints_8);
		hourLabel = new JLabel();
		hourLabel.setPreferredSize(new Dimension(50, 20));
		final GridBagConstraints gridBagConstraints_9 = new GridBagConstraints();
		gridBagConstraints_9.fill = GridBagConstraints.BOTH;
		gridBagConstraints_9.insets = new Insets(0, 2, 0, 0);
		gridBagConstraints_9.anchor = GridBagConstraints.WEST;
		gridBagConstraints_9.gridy = 3;
		gridBagConstraints_9.gridx = 1;
		dateSoundPanel.add(hourLabel, gridBagConstraints_9);
		hourLabel.setText(ClockGUI.resources.getString("ConfigDLG.hour"));
		minuteField = new JSpinField();
		minuteField.setMaximum(60);
		minuteField.setMinimum(1);
		minuteField.setValue(1);
		minuteField.setPreferredSize(new Dimension(75, 20));
		final GridBagConstraints gridBagConstraints_10 = new GridBagConstraints();
		gridBagConstraints_10.insets = new Insets(2, 0, 2, 0);
		gridBagConstraints_10.fill = GridBagConstraints.BOTH;
		gridBagConstraints_10.anchor = GridBagConstraints.WEST;
		gridBagConstraints_10.gridy = 4;
		minuteField.addInputMethodListener(new InputMethodListener() {
		    public void inputMethodTextChanged(InputMethodEvent e) {
		        do_minuteField_inputMethodTextChanged();
		    }
		    public void caretPositionChanged(InputMethodEvent e) {
		        do_minuteField_caretPositionChanged();
		    }
		});
		dateSoundPanel.add(minuteField, gridBagConstraints_10);
		minuteLabel = new JLabel();
		final GridBagConstraints gridBagConstraints_11 = new GridBagConstraints();
		gridBagConstraints_11.insets = new Insets(0, 2, 0, 7);
		gridBagConstraints_11.fill = GridBagConstraints.BOTH;
		gridBagConstraints_11.anchor = GridBagConstraints.WEST;
		gridBagConstraints_11.gridy = 4;
		gridBagConstraints_11.gridx = 1;
		dateSoundPanel.add(minuteLabel, gridBagConstraints_11);
		minuteLabel.setText(ClockGUI.resources.getString("ConfigDLG.minute"));
		amRadioButton = new JRadioButton();
		final GridBagConstraints gridBagConstraints_31 = new GridBagConstraints();
		gridBagConstraints_31.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints_31.gridy = 5;
		gridBagConstraints_31.gridx = 0;
		amRadioButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        do_amRadioButton_actionPerformed();
		    }
		});		dateSoundPanel.add(amRadioButton, gridBagConstraints_31);
		amRadioButton.setText(ClockGUI.resources.getString("ConfigDLG.am"));

		pmRadioButton = new JRadioButton();
		final GridBagConstraints gridBagConstraints_32 = new GridBagConstraints();
		gridBagConstraints_32.insets = new Insets(0, 0, 0, 0);
		gridBagConstraints_32.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints_32.gridy = 5;
		gridBagConstraints_32.gridx = 1;
		pmRadioButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        do_pmRadioButton_actionPerformed();
		    }
		});			
		dateSoundPanel.add(pmRadioButton, gridBagConstraints_32);
		pmRadioButton.setText(ClockGUI.resources.getString("ConfigDLG.pm"));
		testSoundButton = new JButton();
		testSoundButton.setMargin(new Insets(2, 2, 2, 2));
		final GridBagConstraints gridBagConstraints_14 = new GridBagConstraints();
		gridBagConstraints_14.insets = new Insets(4, 2, 0, 0);
		gridBagConstraints_14.anchor = GridBagConstraints.WEST;
		gridBagConstraints_14.gridx = 4;
		testSoundButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        do_testSoundButton_actionPerformed();
		    }
		});
		dateSoundPanel.add(testSoundButton, gridBagConstraints_14);
		testSoundButton.setText(ClockGUI.resources.getString("ConfigDLG.test"));
		repeatLabel = new JLabel();
		final GridBagConstraints gridBagConstraints_15 = new GridBagConstraints();
		gridBagConstraints_15.gridwidth = 2;
		gridBagConstraints_15.gridy = 3;
		gridBagConstraints_15.gridx = 3;
		dateSoundPanel.add(repeatLabel, gridBagConstraints_15);
		repeatLabel.setText(ClockGUI.resources.getString("ConfigDLG.repeat"));
		onceRadioButton = new JRadioButton();
		onceRadioButton.setIconTextGap(2);
		final GridBagConstraints gridBagConstraints_16 = new GridBagConstraints();
		gridBagConstraints_16.insets = new Insets(0, 4, 0, 0);
		gridBagConstraints_16.gridy = 4;
		gridBagConstraints_16.gridx = 3;
		onceRadioButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        do_onceRadioButton_actionPerformed();
		    }
		});		
		dateSoundPanel.add(onceRadioButton, gridBagConstraints_16);
		onceRadioButton.setText(ClockGUI.resources.getString("ConfigDLG.once"));
		weeklyRadioButton = new JRadioButton();
		final GridBagConstraints gridBagConstraints_17 = new GridBagConstraints();
		weeklyRadioButton.setIconTextGap(2);
		weeklyRadioButton.setMargin(new Insets(2, 0, 2, 2));
		gridBagConstraints_17.gridy = 4;
		gridBagConstraints_17.gridx = 4;
		weeklyRadioButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        do_weeklyRadioButton_actionPerformed();
		    }
		});	
		dateSoundPanel.add(weeklyRadioButton, gridBagConstraints_17);
		weeklyRadioButton.setText(ClockGUI.resources.getString("ConfigDLG.weekly"));
		repeatSettingsPanel = new JPanel();
		repeatSettingsPanel.setOpaque(false);
		repeatSettingsPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		final GridBagConstraints gridBagConstraints_20 = new GridBagConstraints();
		gridBagConstraints_20.insets = new Insets(0, 2, 4, 0);
		gridBagConstraints_20.fill = GridBagConstraints.BOTH;
		gridBagConstraints_20.gridheight = 4;
		gridBagConstraints_20.gridwidth = 3;
		gridBagConstraints_20.gridy = 2;
		gridBagConstraints_20.gridx = 2;
		dateSoundPanel.add(repeatSettingsPanel, gridBagConstraints_20);
		enabledDiabledAlarmGroup = new ButtonGroup();
		enabledDiabledAlarmGroup.add(alarmEnabledRadioButton);
		enabledDiabledAlarmGroup.add(alarmDisabledRadioButton);
	
		amPmButtonGroup = new ButtonGroup();
		amPmButtonGroup.add(amRadioButton);
		amPmButtonGroup.add(pmRadioButton);
		howOftenGroup = new ButtonGroup();
		howOftenGroup.add(onceRadioButton);
		howOftenGroup.add(weeklyRadioButton);
	}
	
	/**
	 * Function to configure the dialog before we make it visible
	 * 
	 * @param aClock
	 * @param frame
	 */
	public void configureDialog(Clock clock)
	{
	    this.clock  = clock;
	    if (clockID == null)
			clockID = getClockIDInMap(clock, allClocks);
		
	    configureClockSettingsTab(clock);
	    configureAlarmSettingsTab();
	}

	/**
	 * Configure the settings for the clock tab only
	 */
	private void configureClockSettingsTab(Clock aClock)
	{
	    this.clock  = aClock;
		if (clockID == null)
			clockID = getClockIDInMap(clock, allClocks);
		
		if (clockID.equals(EngineHelper.getFirstClockIDForCollection(allClocks))) {
			scrollLeft.setEnabled(false);
		}
		else {
			scrollLeft.setEnabled(true);
		}
		
		if (clockID.equals(EngineHelper.getLastClockIDForCollection(allClocks))) {
				scrollRight.setEnabled(false);
		}
		else {
			scrollRight.setEnabled(true);
		}
		
	    canScale.setSelected(clock.isAllowedToScale());
	    preserveAspectRatio.setSelected(clock.isFixedAspectRatio());
	    
	    //if is windows show always on top
        alwaysOnTop.setEnabled(true);
	    
        if (clock.isOnTopInFullWindowMode())
            alwaysOnTop.setSelected(true);
	    else
	        alwaysOnTop.setSelected(false);
		
        
        showDate.setSelected(clock.isAllowedToShowDate());
		showTime.setSelected(clock.isAllowedToShowTime());
		twelveHourDisplay.setSelected(clock.isUsingTwelveHrFormat());
	    
		captionText.setText(clock.getCaption());
		
		String [] timeZones = SimpleTimeZone.getAvailableIDs();
		//probably makes sense to sort this alphabetically
		sortStrings(timeZones);
		
		int index = 0;
		for (int i = 0; i < timeZones.length; i++)
		{
			String timeZone = timeZones[i];
			timeZoneComboBox.addItem(timeZone);
			if (timeZone.equals(clock.getDate().getTimeZone()))
				index = i;
		}
		timeZoneComboBox.setSelectedIndex(index);

		//Setup the clock cycle part
		cycleClock.setSelected(clock.isCycleClocks());
		limitToGroup.setSelected(clock.isCycleGroupLimit());
		minuteClockCycleField.setValue(clock.getCyclePeriod());
	    if (clock.isCycleClocks() == false)
		{
		    minuteClockCycleField.setEnabled(false);
		    limitToGroup.setEnabled(false);
		    clockGroupsComboBox.setEnabled(false);
		}
		else
		{
		    minuteClockCycleField.setEnabled(true);
	        limitToGroup.setEnabled(true);
		    clockGroupsComboBox.setEnabled(clock.isCycleGroupLimit());
		}
	    index = 0;
	    for (int i = 0; i < ClockGUI.clockCategories.size(); i++)
	    {
	        String category = (String)ClockGUI.clockCategories.get(i);
	        clockGroupsComboBox.addItem(category);
			if (category.equals(clock.getCycleGroup()))
				index = i;
		}
	    clockGroupsComboBox.setSelectedIndex(index);
	
		//THIS WILL RETURN NULL without the leading "/" !!! 
		//NOTE ALSO: IF you run this In the Debugger, this Jar file MUST be 
		//in the class path (add in the classpath tab) as well or you will get null for URL
		//String clockStr = ClockGUI.RESOURCE_PATH + clock.getPreviewPath();
	    URL url = null;
	    try {
	    	url = new File(ClockGUI.resourcesPath + '/' + clock.getPreviewPath()).toURI().toURL();
	    }catch(Exception e) {
	    	System.out.println("Error: "+ e.getMessage());
	    }
		
		Image img = Toolkit.getDefaultToolkit().getImage(url);
		canvas.setImage(img);
		canvas.paintAll(canvas.getGraphics());
		startingUp = false;

	}
	
	/**
	 * configure the settings for the alarms tab only
	 */
	private void configureAlarmSettingsTab()
	{
	    selectedAlarm = null;
		Vector<ClockAlarm> alarms = clock.getAlarms();
		for (int i = 0; i < alarms.size(); i++)
		{
			ClockAlarm alarm =(ClockAlarm)alarms.get(i); 
		    String alarmTime = alarm.getDate().getDialogBoxDisplayStringFromDate(alarm.isWeekly());
		    listModel.insertElementAt(alarmTime, i);
		}
	    for (int i = 0; i < ClockGUI.alarmSounds.size(); i++)
		    soundComboBox.addItem((String)ClockGUI.alarmSounds.get(i));
		updateAlarmButton.setEnabled(false);
	    addAlarmButton.setEnabled(false);
        removeAlarmButton.setEnabled(false);
	}
	

	/**
	 * called to update the settings from a selection change in the text list
	 */
	private void updateAlarmDisplay()
	{
	    int ID = configuredAlarmsList.getSelectedIndex();
	    if (ID == -1)
	    {
	        //null out values
	        updateAlarmButton.setEnabled(false);
		    addAlarmButton.setEnabled(false);
		    removeAlarmButton.setEnabled(false); 
		    alarmEnabledRadioButton.setSelected(false);
		    alarmDisabledRadioButton.setSelected(false);
		    amRadioButton.setSelected(false);
		    pmRadioButton.setSelected(false);
		    weeklyRadioButton.setSelected(false);
		    onceRadioButton.setSelected(false);
		    hourField.setValue(1);
		    minuteField.setValue(1);
		    dateComboBox.setDate(new Date());
		    soundComboBox.setSelectedIndex(0);
	        return;
	    }
	    
	    //inserted into the test list in the order they
	    //appear in the vector so the index will be correct
	    Vector<ClockAlarm> alarms = clock.getAlarms();
	    if ((alarms.size() > 0) && (alarms.size() > ID))
	    {
	        selectedAlarm = (ClockAlarm)clock.getAlarms().get(ID);
	        ID = selectedAlarm.getID();
	        updateAlarmButton.setEnabled(true);
		    addAlarmButton.setEnabled(false);
		    removeAlarmButton.setEnabled(true); 
		    boolean isEnabled = selectedAlarm.isEnabled();
		    alarmEnabledRadioButton.setSelected(isEnabled);
		    alarmDisabledRadioButton.setSelected(!isEnabled);
		    int amPm = selectedAlarm.getDate().getAmPm();
		    boolean isAM = (amPm == GregorianCalendar.AM); 
		    amRadioButton.setSelected(isAM);
		    pmRadioButton.setSelected(!isAM);
		    boolean isWeekly = selectedAlarm.isWeekly();
		    weeklyRadioButton.setSelected(isWeekly);
		    onceRadioButton.setSelected(!isWeekly);
		    hourField.setValue(selectedAlarm.getDate().getHour());
		    minuteField.setValue(selectedAlarm.getDate().getMinute());
		    dateComboBox.setDate(selectedAlarm.getDate().getDialogBoxDateFromInternalDate());
		    int index = getSoundIndexInComboBox(selectedAlarm.getAlarmResource());
		    soundComboBox.setSelectedIndex(index);
	    }
	}
	
	
	/**
	 * Create a new alarm
	 */
	private void addAlarm()
	{
	    selectedAlarm = new ClockAlarm();
	    ClockDate date = new ClockDate();
	    date.setTimeZone(clock.getDate().getTimeZone());
	    
	    selectedAlarm.setDate(date);
	    //default value to keep it from blowing up, not used currently
	    selectedAlarm.setVolume("low");
	    //need to set ID 
	    Vector<ClockAlarm> alarms = clock.getAlarms();
	    int maxAlarmID = -1;
	    int numAlarms = alarms.size();
	    if (numAlarms == 0)
	    	maxAlarmID = 0;
	    else
	    {
	        for (int i = 0; i < alarms.size(); i++)
	        {
	            int ID = ((ClockAlarm)alarms.get(i)).getID();
	            maxAlarmID = (ID >= maxAlarmID ? (ID + 1) : maxAlarmID);
	        }
	    }
	    selectedAlarm.setID(maxAlarmID);
	    alarms.add(selectedAlarm);
	    updateSelectedAlarmConfig(false);
	    String alarmTime = selectedAlarm.getDate().getDialogBoxDisplayStringFromDate(selectedAlarm.isWeekly());
	    int index = listModel.size();
	    listModel.insertElementAt(alarmTime, index);
	    configuredAlarmsList.setSelectedIndex(index);
	    configuredAlarmsList.ensureIndexIsVisible(index);
	}
	
	/**
	 * Update a selected alarm with new properties
	 */
	private void updateSelectedAlarmConfig(boolean evaluateString)
	{
	    //save the settings
	    selectedAlarm.setEnabled(alarmEnabledRadioButton.isSelected());
	    selectedAlarm.setWeekly(weeklyRadioButton.isSelected());
	    selectedAlarm.setAlarmResource((String)soundComboBox.getSelectedItem());
	    ClockDate date = selectedAlarm.getDate();
	    date.setHour(hourField.getValue());
	    date.setMinute(minuteField.getValue());
	    boolean isAM = amRadioButton.isSelected();
	    date.setAmPm(isAM == true ? GregorianCalendar.AM : GregorianCalendar.PM);
	    Date calDate = dateComboBox.getDate();
	    
	    //compare what is in the list box with what might be there and if not ==
	    //then change
	    int index = configuredAlarmsList.getSelectedIndex();
	    if (index != -1 && evaluateString)
	    {
	        String curList = (String)listModel.get(index);
	        String alarmTime = selectedAlarm.getDate().getDialogBoxDisplayStringFromDate(selectedAlarm.isWeekly());
		    if (curList.equals(alarmTime) == false)
		    {
		        listModel.set(index,alarmTime);
		    }
	    }
	    
	    //need to set date year, day month
	    SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-F-yyyy");
	    String strDate = formatter.format(calDate);
	    StringTokenizer st = new StringTokenizer(strDate, "-");
	    date.setMonth(Integer.parseInt(st.nextToken()));
	    date.setDayOfMonth(Integer.parseInt(st.nextToken()));
	    date.setDayOfWeek(Integer.parseInt(st.nextToken()));
	    date.setYear(Integer.parseInt(st.nextToken()));
	    updateAlarmButton.setEnabled(true);
	}

	/**
	 * Removes a previously configured alarm
	 */
	private void removeSelectedAlarm()
	{
	    int index = configuredAlarmsList.getSelectedIndex();
	    Vector alarms = clock.getAlarms();
	    ClockAlarm alarm = (ClockAlarm)alarms.get(index);
	    alarms.remove(index);
	    for(int i = 0; i < alarms.size(); i++)
	    {
	        //resequence ID's
	        alarm = (ClockAlarm)alarms.get(i);
	        alarm.setID(i);
	    }
	    listModel.remove(index);
	    configuredAlarmsList.setSelectedIndex(-1);
	}
	
	/**
	 * Adjust add, remove, update button states
	 */
	private void setAddRemoveUpdateButtonStates()
	{
	    int index = configuredAlarmsList.getSelectedIndex();
	    if (index == -1)
	        {
	        updateAlarmButton.setEnabled(false);
	        removeAlarmButton.setEnabled(false);
	        }
	    else
	    {
	        updateAlarmButton.setEnabled(true);
	        removeAlarmButton.setEnabled(true);
	    }
	    
	    //now figure out if the add button should be enabled
	    if (amRadioButton.isSelected() == true || pmRadioButton.isSelected() == true)
	    {
        	if(onceRadioButton.isSelected() == true || weeklyRadioButton.isSelected() == true)
        	{
        	    if (alarmEnabledRadioButton.isSelected() == true || alarmDisabledRadioButton.isSelected() == true)
        	    {
        	        addAlarmButton.setEnabled(true);
        	    }
        	}
        }   
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

	
	/**************************************************************
	 *  EVENT Handlers for configuration panel
	 **************************************************************/
	protected void do_canScale_actionPerformed() 
	{
	    clock.setAllowedToScale(this.canScale.isSelected());
	}
	
	protected void do_perserveAspectRatio_actionPerformed() 
	{
		clock.setFixedAspectRatio(preserveAspectRatio.isSelected());
	}
	
	protected void do_showDate_actionPerformed() 
	{
		clock.setAllowedToShowDate(showDate.isSelected());
	}
	
	protected void do_showTime_actionPerformed() 
	{
		clock.setAllowedToShowTime(showTime.isSelected());
	}
	
	protected void do_twelveHourDisplay_actionPerformed() 
	{
		clock.setUsingTwelveHrFormat(twelveHourDisplay.isSelected());
	}
	
	protected void do_alwaysOnTop_actionPerformed() 
	{
		clock.setOnTopInFullWindowMode(alwaysOnTop.isSelected());
	}
		
	protected void do_timeZoneComboBox_actionPerformed() 
	{
		if (startingUp == false)
		{
			//reset all the times and possibly the date based on the new time zone
			Long currentTime = new Date().getTime();
			clock.configureTime(currentTime, (String)timeZoneComboBox.getSelectedItem());
			clock.getDate().setTimeZone((String)timeZoneComboBox.getSelectedItem());
			
			//try and fashion a new caption from this
			String caption = ClockGUI.generateCaption((String)timeZoneComboBox.getSelectedItem());
		    clock.setCaption(caption);
			configureClockSettingsTab(clock);
		}
		
	}
	
	protected void do_groupLimitComboBox_actionPerformed()
	{
	    if (startingUp == false)
	        clock.setCycleGroup((String)clockGroupsComboBox.getSelectedItem());
	}
	
	protected void do_cycleClockLimitToGroup_actionPerformed()
	{
	    boolean isSelected = limitToGroup.isSelected();
	    clock.setCycleGroupLimit(isSelected);
	    if (isSelected == false)
	    {
	        clockGroupsComboBox.setEnabled(false);
	    }
	    else
	    {
	        clockGroupsComboBox.setEnabled(true);
	    }
	}
	
	protected void do_cycleClock_actionPerformed()
	{
	    boolean isSelected = cycleClock.isSelected();
	    clock.setCycleClocks(isSelected);
		if (isSelected == false)
		{
		    minuteClockCycleField.setEnabled(false);
		    limitToGroup.setEnabled(false);
		    clockGroupsComboBox.setEnabled(false);
		}
		else
		{
		    minuteClockCycleField.setEnabled(true);
		    limitToGroup.setEnabled(true);
		    clockGroupsComboBox.setEnabled(true);
		}
	}
	
	protected void do_scrollLeft_actionPerformed() 
	{
		clockID = EngineHelper.getPreviousClockIDForCollection(clockID, allClocks);
		Clock selectedClock =  allClocks.get(clockID);
		Clock newClock = Clock.createClock(selectedClock);
		Clock.copyRuntimeSettings(newClock, clock, selectedClock.getID());
		clock = newClock;
		configureClockSettingsTab(clock);
	}
	
	protected void do_scrollRight_actionPerformed() 
	{
		clockID = EngineHelper.getNextClockIDForCollection(clockID, allClocks);
		Clock selectedClock =  allClocks.get(clockID);
		Clock newClock = Clock.createClock(selectedClock);
		Clock.copyRuntimeSettings(newClock, clock, selectedClock.getID());
		clock = newClock;
		configureClockSettingsTab(clock);
	}
	
	protected void do_okButton_actionPerformed() 
	{
	    clock.setCaption(captionText.getText());
		clock.setCyclePeriod(minuteClockCycleField.getValue());
	    okButtonState = true;
		this.setVisible(false);
	}
	
	protected void do_cancelButton_actionPerformed() 
	{
		okButtonState = false;
		this.setVisible(false);
	}

	/**************************************************************
	 *  EVENT Handlers for alarms panel
	 **************************************************************/

	protected void do_configuredAlarmsList_hierarchyChanged() 
	{
    }
    protected void do_configuredAlarmsList_valueChanged() 
    {
        updateAlarmDisplay();
	    setAddRemoveUpdateButtonStates();
    }
	protected void do_alarmEnabledRadioButton_actionPerformed() 
	{
	    setAddRemoveUpdateButtonStates();
	}
	protected void do_alarmDisabledRadioButton_actionPerformed() 
	{
	    setAddRemoveUpdateButtonStates();
	}
	protected void do_removeAlarmButton_actionPerformed() 
	{
	    removeSelectedAlarm();
	}
	protected void do_updateAlarmButton_actionPerformed() 
	{
	    updateSelectedAlarmConfig(true);
	}
	protected void do_addAlarmButton_actionPerformed()
	{
	    addAlarm();
	}
	protected void do_soundComboBox_actionPerformed() 
	{
	    setAddRemoveUpdateButtonStates();
	}
	protected void  do_inputMethodRequestsChanged(InputMethodEvent e)
	{
	}
	protected void do_dateComboBox_inputMethodTextChanged() 
	{
    }
    protected void do_dateComboBox_caretPositionChanged() 
    {
    }
    protected void do_hourField_inputMethodTextChanged() 
    {	    
	    setAddRemoveUpdateButtonStates();
    }
    protected void do_hourField_caretPositionChanged() 
    {
    }
    protected void do_minuteField_inputMethodTextChanged() 
    {
	    setAddRemoveUpdateButtonStates();
    }
    protected void do_minuteField_caretPositionChanged() 
    {
    }	
	protected void do_amRadioButton_actionPerformed() 
	{
	    setAddRemoveUpdateButtonStates();
	}
	protected void do_pmRadioButton_actionPerformed() 
	{
	    setAddRemoveUpdateButtonStates();
	}
	protected void do_lowRadioButton_actionPerformed() 
	{
	    setAddRemoveUpdateButtonStates();
	}
	protected void do_mediumRadioButton_actionPerformed() 
	{
	    setAddRemoveUpdateButtonStates();
	}
	protected void do_highRadioButton_actionPerformed() 
	{
	    setAddRemoveUpdateButtonStates();
	}
	protected void do_testSoundButton_actionPerformed() 
	{
	    try
	    {
	        String alarmSelected = (String)soundComboBox.getSelectedItem();
	        String alarmStr = "/resources/alarms/" + alarmSelected;
	        URL url = this.getClass().getResource(alarmStr);
	        AlarmEventListener.playSoundClip(url);
	    }
	    catch(Exception e)
	    {
	        
	        System.out.println(e.getMessage());
	    }
	}
	protected void do_onceRadioButton_actionPerformed() 
	{
	    setAddRemoveUpdateButtonStates();
	}
	protected void do_weeklyRadioButton_actionPerformed() 
	{
	    setAddRemoveUpdateButtonStates();
	}
	protected void do_includeWeekendCheckBox_actionPerformed()
	{
	    setAddRemoveUpdateButtonStates();
	}
	
	/**************************************************************
	 * Private Functions
	 * 
	 **************************************************************/	
	
	/**
	 * Simple string sort function for the time zones
	 * @param sorted
	 */
	private void sortStrings(String [] sorted)
	{
		int j; 
		boolean atLeastOneSwap=true; 
		String temp;
		
		while(atLeastOneSwap) 
		{
			atLeastOneSwap = false;
		    for(j=0; j < sorted.length-1; ++j) 
		    {
		    	if(sorted[j].compareToIgnoreCase(sorted[j+1]) >  0 ) 
		    	{   
		    		temp = sorted[j];  
		    		sorted[j] = sorted[j+1];
		            sorted[j+1] = temp; 
		            atLeastOneSwap = true;
		        } 
	        }   
	    }  
	}
	
	/**
	 * This function will determine where in the vector of all available clocks this particular
	 * clock lies
	 * @param clock
	 * @param allClocks
	 * @return
	 */
	private String getClockIDInMap(Clock clock, Map<String, Clock>allClocks)
	{
		String ID = null;
		
		//The parent ID is where it comes from
		String clkID = clock.getParentID();
		Iterator<String> It = allClocks.keySet().iterator();
		
		while (It.hasNext()){
			ID = It.next();
			if (clkID.equals(ID))
				return(ID);
		}
		return(ID);
	}
	
	/**
	 * Finds the sound in the combo box
	 * @param sound
	 * @return
	 */
	private int getSoundIndexInComboBox(String sound)
	{
	    for(int i = 0; i < soundComboBox.getItemCount(); i++)
	    {
	        if (sound.equals(soundComboBox.getItemAt(i)))
	            return(i);
	    }
	    return(-1);
	}
	
	/**
	 * @return Returns the okButton.
	 */
	public boolean getOkButtonState() 
	{
		return okButtonState;
	}
	
	/**
	 * @return Returns the clock.
	 */
	public Clock getClock() 
	{
		return clock;
	}
}
