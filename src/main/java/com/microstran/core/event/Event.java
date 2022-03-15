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
 * Generic interface for an event
 * 
 * @author Mike Stran
 *
 */
public interface Event
{
		
	/**
	 * interface to identify the type of event  
	 * 
	 * @return A String that identifies the event type.
	 */
	public String getApplicationEventType( );
		
	/**
	 * Set any data for the Event.
	 * 
	 * @param eventData the events data if any
	 */
	public void setEventData( Object eventData );

	/**
	 * Getter for event data.
	 * 
	 * @return events data if present, null if not.
	 */
	public Object getEventData( );
}
