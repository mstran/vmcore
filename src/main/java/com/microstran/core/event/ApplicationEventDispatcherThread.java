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
 * Actually dispatch an Event by calling the Listener's listen( )
 * method.
 *
 * @author Mike Stran
 *
 */
public class ApplicationEventDispatcherThread extends Thread
{
	private ApplicationEventListener listener = null;
	private ApplicationEvent event = null;

    /**
     * Construct a new EventDispatcherThread.
     * 
     * @param listener The EventListener to call.
     * @param event    The event to dispatch.
     * @param name     The name of the thread.
     */
    public ApplicationEventDispatcherThread( ApplicationEventListener listener, ApplicationEvent event)
    {
        //super( event.getApplicationEventType() );
        this.listener = listener;
        this.event    = event;
    }

	/**
	 * Call the ApplicationEventListener listen( ) method.
	 */
	public void run( )
	{
		try
		{
		    this.listener.listen( event );
		}
		catch ( Exception e )
		{
		}
	}
}
