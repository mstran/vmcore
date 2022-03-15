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
package com.microstran.core.event;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.microstran.core.engine.ClockGUI;


/**
 * The ApplicationEventServer provides the mechanism for distribution of event messages to those 
 * objects registered to recieve a particular event type.  An object is notified by calling the
 * recieveEvent() interface method.  
 * <p>
 * The ApplicationEventServer is a singleton
 * 
 * Note also that having registered as an Event Listener  a class must be de-registered 
 * when done listening to events to prevent dangling references.
 *
 * @author Mike Stran
 *
 * 
 */
public class ApplicationEventServer
{

	//singleton
	private static ApplicationEventServer Instance = null;

	/**
	 * All the listeners
	 */
	private Hashtable eventListeners = new Hashtable( );
	
	private int count= 0;
	
	private static final int TRIM_AT = 125;
	private static final int POOL_START_COUNT = 1;
	
	private int clockEventListenerCount = 0;
	
	/**
	 * Register to listen to events of a specific type.
	 * 
	 * @param listener      An object that implements the ApplicationEventListener 
	 *                      interface.
	 * @param eventType		The type of event.
	 */
	public void register( ApplicationEventListener listener, String eventType )
	{	
		synchronized(this)
		{
		    try
		    {
				if (eventListeners.containsKey(eventType) == false)
				{
				    addNewCategory(listener, eventType);
				}
				else
				{
				    List listeners = (List)eventListeners.get(eventType);
					listeners.add(listener);
				}
				clockEventListenerCount++;
		    }
			catch(Exception e)
		    {
		        String errStr = ClockGUI.resources.getString("ClockError.ExceptionAddingListener");
	            System.out.println(errStr + " " + e.getMessage()+ "\n" );
	            e.printStackTrace();
		    }
		}
	}
	
	/**
	 * unregister an object from event listening.
	 * 
	 * @param listener      An object that implements the ApplicationEventListener 
	 *                      interface.
	 * @param eventType		The type of event.
	 */
	public void unregister( ApplicationEventListener listener, String eventType )
	{
		synchronized(this)
		{
		    try
		    {
			    if(listener == null)
			        return;
				
			    List list = (List)eventListeners.get(eventType);
				if (list == null)
				    return;
				
			    list.remove(listener);
			    clockEventListenerCount--; 
		    }
		    catch(Exception e)
		    {
		        String errStr = ClockGUI.resources.getString("ClockError.ExceptionRemovingListener");
	            System.out.println(errStr + " " + e.getMessage()+ "\n" );
	            e.printStackTrace();
		    }
		}
	}
	
	/**
	 * Remove all listeners.	 
	 */
	public void unregisterAll( )
	{
		eventListeners.clear();	
	}	
	
	/**
	 * Send an event.  It will be broadcast to all EventListeners that are 
	 * interested in its type.
	 * 
	 * @param event The event to send.
	 */
	public void publishEvent( ApplicationEvent event )
	{	
	    try
	    {
			List listeners = (List)eventListeners.get(event.getApplicationEventType());
			if (listeners == null)
				return;
			count++;
			Iterator listIterator  = listeners.iterator( );
		    while ( listIterator.hasNext( ) )
			{
	            ApplicationEventListener listener = (ApplicationEventListener)listIterator.next( );
	            try
	            {
	                ApplicationEventDispatcherThread eventThread = new ApplicationEventDispatcherThread( listener, event);
	                eventThread.start();
	            }
	            catch(Exception ex)
	            {/*catch these*/ }
			}
	    }
	    catch(Exception e)
	    {
	        String errStr = ClockGUI.resources.getString("ClockError.ExceptionPublishingEvent");
            System.out.println(errStr + " " + e.getMessage() + "\n" );
	    }
	}
	
	/**
	 * Singleton initializer
	 * 
	 * @return The shared instance.
	 */
	public static ApplicationEventServer instance( )
	{
		synchronized( ApplicationEventServer.class )
		{
			if ( Instance == null )
			{
				Instance = new ApplicationEventServer( );
			}
		}
		return(Instance);
	}


	/**
	 * Adds a category of listener
	 * @param listener
	 * @param eventType
	 */
	private void addNewCategory(ApplicationEventListener listener, String eventType)
	{
	    try
	    {
	        List listeners = new Vector();
	        listeners.add(listener);
	        eventListeners.put(eventType, listeners);
	    }
		catch(Exception e)
	    {
	        String errStr = ClockGUI.resources.getString("ClockError.ExceptionAddingListener");
            System.out.println(errStr + " " + e.getMessage()+ "\n" );
            e.printStackTrace();
	    }

	}
	
    /**
     * Private Constructor - Singleton.
     */
    private ApplicationEventServer( )
    {
    }
    
    
}
