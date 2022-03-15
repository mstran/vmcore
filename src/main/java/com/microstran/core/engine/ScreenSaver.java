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

import java.awt.Color;


/**
 * Generic interface for a screen savers
 * 
 * @author Mike Stran
 *
 */
public interface ScreenSaver 
{
	public void layoutClocks();
    public void instantiateClocks();
	public void registerForEvents();
	public void unregisterForEvents();
	public Color getBackgroundColor();
	public void startScreenSaver();
	public void stopScreenSaver();
}
