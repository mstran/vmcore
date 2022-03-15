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

import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import com.microstran.core.alarm.ClockAlarm;
import com.microstran.core.event.ApplicationEvent;


/**
 * The base class for all clock types
 *
 * @author Mike Stran
 *
 */
public class AlarmEventListener implements com.microstran.core.event.ApplicationEventListener
{

	public AlarmEventListener()
	{}
	
	/**
	 * Recieve notification and Cause Alarm to happen.
	 * 
	 * @param applicationEvent The application event to process
	 * 
	 */
	public void listen( ApplicationEvent applicationEvent )
	{
		try
		{
		    ClockAlarm alarm = (ClockAlarm)applicationEvent.getEventData();
		    String alarmStr = "/resources/alarms/"+alarm.getAlarmResource();
		    URL url = this.getClass().getResource(alarmStr);
		    AlarmEventListener.playSoundClip(url);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Play the clip
	 * @param clip
	 */
	public static void playSoundClip(URL url)
	{
	    try
	    {
		    Clip clip = AudioSystem.getClip();
		    AudioInputStream ais = AudioSystem.getAudioInputStream(url);
		    clip.open(ais);
		    clip.loop(1);
	    }
	    catch(Exception e)
	    {
	        System.out.println(e.getMessage());
	    }
	}

}