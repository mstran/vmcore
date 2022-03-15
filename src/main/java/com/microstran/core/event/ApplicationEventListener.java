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
 * This is the interface that Clocks must implement in order to recieve events.
 *
 * @author Mike Stran
 *
 */
public interface ApplicationEventListener 
{
	/**
	 * specific to each listener
	 * 
	 * @param applicationEvent The application event to process
	 * 
	 */
	public void listen( ApplicationEvent applicationEvent );
}
