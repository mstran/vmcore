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
package com.microstran.core.alarm;

import com.microstran.core.clock.ClockDate;

/**
 * Generic interface for an alarm
 * 
 * @author Mike Stran
 *
 */
public interface Alarm
{
	public boolean isEnabled();
	public boolean isWeekly();
	public void     sendAlarm(ClockDate date);
	public String   getAlarmResource();
}