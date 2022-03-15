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

/**
 * The main event class
 *
 * @author Mike Stran
 *
 */
public class ApplicationEvent implements Event
{
	protected Object eventData = null;
	protected String eventType = null;
		
	/**
	 * Default constructor.
	 */
	public ApplicationEvent( )
	{
	}
	
	/**
	 * 
	 * @param eventData for the event
	 */
	public ApplicationEvent( Object eventData )
	{
		this.eventData = eventData;
	}
		
	/**
	 * 
	 * @return The event type.
	 */
	public String getApplicationEventType( )
	{
		return("applicationEvent");
	}

	/**
	 * Set the data of the Event.
	 * 
	 * @param eventData data for the event.
	 */
    public void setEventData( Object eventData )
    {
		this.eventData = eventData;
    }

	/**
	 * getter for event data.
	 * 
	 * @return the events data if valid, null if empty.
	 */
    public Object getEventData( )
    {
        return(this.eventData);
    }

	/**
	 * Equality Test.  
	 * 
	 * @param obj The object to compare with
	 * 
	 * @return true if the provided object is an Event with the same 
	 *         type as this Event.  false if it is/does not.
	 */
	public boolean equals( Object obj )
	{
		if ( obj.getClass( ).getName( ).equals( this.getClass( ).getName( ) ) )
		{
			ApplicationEvent event = (ApplicationEvent)obj;
			//Check against the to prevent collisions
			if (event.getApplicationEventType().equals(this.getApplicationEventType()))
			{
				return(true);
			}
		}
		return(false);
	}
	
	
	/**
	 * Return a String of the event, in our case it will just contain the type data 
	 * NOT the internal data representation
	 * 
	 * @return event type data
	 */
	public String toString( )
	{
		return(this.getApplicationEventType());
	}

}
