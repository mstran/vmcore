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

package com.microstran.core.clock;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The class to do moon phase calculations
 *
 * @author Mike Stran
 *
 */
public class MoonPhaseCalculator 
{

	static final long secondsInMinute = 60L;
	static final long secondsInHour = secondsInMinute * 60L;
	static final long secondsInDay = secondsInHour * 24L;
	static final double secondsInLunarPeriod = 2551442.9;
	static final double daysInLunarPeriod = secondsInLunarPeriod / Long.valueOf(secondsInDay).doubleValue();
	static final long knownEpochDate = 811976100L;	// 1995 Sep 24 16:54 UTC
	static final int knownEpochLunationNumber = 900;	// Lunation number of "knownEpochDate"
	static final int lunationOffset = 953;	// Lunation commencing 2000 Jan 6
	
	Date nextLunationDate = new Date(0L);
	Date [] lunationDates = new Date[5];

	public MoonPhaseCalculator()
	{}
	
	/**
	 * The following variables are used by the function calcLunationNumber.
	 * They are initialized here to known values and are changed later.
	 */
	long calculatedLunationDate = knownEpochDate;
	int calculatedLunationNumber = knownEpochLunationNumber;
	double calculatedSecondsInCalculatedLunation = secondsInLunarPeriod;

	public int calcPhaseOfMoon(int year, int month, int dayOfMonth, int hour, int minute, int second)
	{
			double currentEpoch, daysIntoPeriod, amountIlluminated, percentBy2;
			int i;

			//fabricate up the date 
			String strDate = year+"-"+month+"-"+dayOfMonth+"-"+hour+"-"+minute+"-"+second;
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-K-m-s");
	        ParsePosition pos = new ParsePosition(0);
	        Date currentDate = dateFormat.parse(strDate, pos);
			
			//if (lunationDates[0].getTime() == 0)
			initializeLunationData(currentDate);

			/*
			 * The "currentEpoch" variable receives the current percentage of time into the current lunation.
			 */
			currentEpoch = Long.valueOf(currentDate.getTime() / 1000L - knownEpochDate).doubleValue() / secondsInLunarPeriod;

			/*
			 * The "daysIntoPeriod" variable receives the current number of days into the current lunation.
			 */
			daysIntoPeriod = daysInLunarPeriod * (currentEpoch - Long.valueOf((long)currentEpoch).doubleValue());

			/*
			 * The following lines of code calculate the percentage of the Moon's surface which is currently
			 * illuminated. This calculation is based on a similar formula written by Chris Osburne of Lunar
			 * Outreach, Inc. 
			 */
			for (i = 3; i > 0; i--)
				if (currentDate.getTime() >= lunationDates[i].getTime())
					break;

			amountIlluminated = (1.0 - Math.cos(2.0 * Math.PI * (((float)currentDate.getTime() - (float)lunationDates[i].getTime()) / (((float)lunationDates[i + 1].getTime() - (float)lunationDates[i].getTime())) / 4.0 + (float)i / 4.0))) / 2.0;

			/*
			 * The following assignment calculates an integral percentage of the amount illuminated (i.e., a number
			 * from 0.0 to 100.0). It is inversed by taking 100.0 and subtracting this percentage from it.
			 */
			percentBy2 = Long.valueOf((long)((amountIlluminated + 0.005) * 100.0)).doubleValue();

			int AmountIlluminated = 0;//(int)((amountIlluminated + 0.005) * 100.0);
			
			if (currentDate.getTime() >= nextLunationDate.getTime())
			{
				initializeLunationData(currentDate);
				nextLunationDate.setTime(lunationDates[4].getTime());

			int amountofLunation =  calcLunationNumber(currentDate);
			}
			
				if (amountIlluminated < 0.05)
				{
						//phaseText = "New";
						AmountIlluminated = 0;
				}
				else if (amountIlluminated < 0.495)
				{
						if (daysIntoPeriod / daysInLunarPeriod < 0.5)
						{
						//    phaseText = "Waxing Crescent";
							AmountIlluminated = 1;
						}
						else
						{
						//    phaseText = "Waning Crescent";
							AmountIlluminated = 6;
						}
				}
				else if (amountIlluminated < 0.505)
				{
						if (daysIntoPeriod / daysInLunarPeriod < 0.5)
						{
						//    phaseText = "First Quarter";
							AmountIlluminated = 2;
						}
						else
						{
						//    phaseText = "Last Quarter";
							AmountIlluminated = 7;
						}
				}
				else if (amountIlluminated < 0.995)
				{
						if (daysIntoPeriod / daysInLunarPeriod < 0.5)
						{
						  //  phaseText = "Waxing Gibbous";
							AmountIlluminated = 3;
						}
						else
						{
						    //phaseText = "Waning Gibbous";
							AmountIlluminated = 5;
						}
				}
				else
				{
				//    phaseText = "Full";
					AmountIlluminated = 4;
				}

	return(AmountIlluminated);		
	}

	
	/**
	 * Helper function that calculates lunation data variables.
	 */
	private final void initializeLunationData(Date currentDate)
	{
		int i;

		for (i = 0; i <= 4; i++)
			lunationDates[i] = calcPhase(currentDate, i);

		calculatedLunationNumber = calcLunationNumber(lunationDates[2]);
		calculatedLunationDate = lunationDates[0].getTime() / 1000L;
		calculatedSecondsInCalculatedLunation = (lunationDates[4].getTime() - lunationDates[0].getTime()) / 1000L;
	}

		/**
	 * The following functions were written by Chris Osburne at Lunar Outreach Services
	 * (http://www.lunaroutreach.org). I have made changes where necessary to make them
	 * Java-compatible and/or more efficient.
	 */

	/**
	 * Helper function that calculates the date/time of the specified phase,
	 * relative to the start of the lunation number corresponding to the
	 * "currentDate" parameter.
	 */
	private final Date calcPhase(Date currentDate, int phase)
	{
		double k;		/* current lunation number with phase percentage tacked on */
		double T;		/* time parameter, Julian Centuries since J2000 */
		double JDE;		/* Julian Ephemeris Day of phase event */
		double E;		/* Eccentricity anomaly */
		double M;		/* Sun's mean anomaly */
		double M1;		/* Moon's mean anomaly */
		double F;		/* Moon's argument of latitude */
		double O;		/* Moon's longitude of ascenfing node */
		double [] A;	/* planetary arguments */
		double W;		/* added correction for quarter phases */
		int i;

		A = new double[15];
		k = Long.valueOf(calcLunationNumber(currentDate)).doubleValue() - lunationOffset + Integer.valueOf(phase).doubleValue() / 4.0;
		T = k / 1236.85;

		/* first approximation */
		JDE = 2451550.09765 + (29.530588853 * k) + T * T * (0.0001337 + T * (-0.000000150 + 0.00000000073 * T));

		/* correction parameters */
		E = 1.0 + T * (-0.002516 + -0.0000074 * T);
		M = 2.5534 + 29.10535669 * k + T * T * (-0.0000218 + -0.00000011 * T);
		M1 = 201.5643 + 385.81693528 * k + T * T * (0.0107438 + T * (0.00001239 + -0.000000058 * T));
		F = 160.7108 + 390.67050274 * k + T * T * (-0.0016341 * T * (-0.00000227 + 0.000000011 * T));
		O = 124.7746 - 1.56375580 * k + T * T * (0.0020691 + 0.00000215 * T);

		/* planetary arguments */
		A[0]  = 0;
		A[1]  = 299.77 +  0.107408 * k - 0.009173 * T * T;
		A[2]  = 251.88 +  0.016321 * k;
		A[3]  = 251.83 + 26.651886 * k;
		A[4]  = 349.42 + 36.412478 * k;
		A[5]  =  84.66 + 18.206239 * k;
		A[6]  = 141.74 + 53.303771 * k;
		A[7]  = 207.14 +  2.453732 * k;
		A[8]  = 154.84 +  7.306860 * k;
		A[9]  =  34.52 + 27.261239 * k;
		A[10] = 207.19 +  0.121824 * k;
		A[11] = 291.34 +  1.844379 * k;
		A[12] = 161.72 + 24.198154 * k;
		A[13] = 239.56 + 25.513099 * k;
		A[14] = 331.55 +  3.592518 * k;

		M = toRad(M);
		M1 = toRad(M1);
		F = toRad(F);
		O = toRad(O);

		for (i = 1; i <= 14; i++)
			A[i] = toRad(A[i]);

		switch(phase)
		{
			case 0: /* Last New Moon */
			case 4: /* Next New Moon */
				JDE = JDE
					- 0.40720         * Math.sin(M1)
					+ 0.17241 * E     * Math.sin(M)
					+ 0.01608         * Math.sin(2.0 * M1)
					+ 0.01039         * Math.sin(2.0 * F)
					+ 0.00739 * E     * Math.sin(M1 - M)
					- 0.00514 * E     * Math.sin(M1 + M)
					+ 0.00208 * E * E * Math.sin(2.0 * M)
					- 0.00111         * Math.sin(M1 - 2.0 * F)
					- 0.00057         * Math.sin(M1 + 2.0 * F)
					+ 0.00056 * E     * Math.sin(2.0 * M1 + M)
					- 0.00042         * Math.sin(3.0 * M1)
					+ 0.00042 * E     * Math.sin(M + 2.0 * F)
					+ 0.00038 * E     * Math.sin(M - 2.0 * F)
					- 0.00024 * E     * Math.sin(2.0 * M1 - M)
					- 0.00017         * Math.sin(O)
					- 0.00007         * Math.sin(M1 + 2.0 * M)
					+ 0.00004         * Math.sin(2.0 * M1 - 2.0 * F)
					+ 0.00004         * Math.sin(3.0 * M)
					+ 0.00003         * Math.sin(M1 + M - 2.0 * F)
					+ 0.00003         * Math.sin(2.0 * M1 + 2.0 * F)
					- 0.00003         * Math.sin(M1 + M + 2.0 * F)
					+ 0.00003         * Math.sin(M1 - M + 2.0 * F)
					- 0.00002         * Math.sin(M1 - M - 2.0 * F)
					- 0.00002         * Math.sin(3.0 * M1 + M)
					+ 0.00002         * Math.sin(4.0 * M1);
			break;

			case 2: /* Full Moon */
				JDE = JDE
					- 0.40614         * Math.sin(M1)
					+ 0.17302 * E     * Math.sin(M)
					+ 0.01614         * Math.sin(2.0 * M1)
					+ 0.01043         * Math.sin(2.0 * F)
					+ 0.00734 * E     * Math.sin(M1 - M)
					- 0.00515 * E     * Math.sin(M1 + M)
					+ 0.00209 * E * E * Math.sin(2.0 * M)
					- 0.00111         * Math.sin(M1 - 2.0 * F)
					- 0.00057         * Math.sin(M1 + 2.0 * F)
					+ 0.00056 * E     * Math.sin(2.0 * M1 + M)
					- 0.00042         * Math.sin(3.0 * M1)
					+ 0.00042 * E     * Math.sin(M + 2.0 * F)
					+ 0.00038 * E     * Math.sin(M - 2.0 * F)
					- 0.00024 * E     * Math.sin(2.0 * M1 - M)
					- 0.00017         * Math.sin(O)
					- 0.00007         * Math.sin(M1 + 2.0 * M)
					+ 0.00004         * Math.sin(2.0 * M1 - 2.0 * F)
					+ 0.00004         * Math.sin(3.0 * M)
					+ 0.00003         * Math.sin(M1 + M - 2.0 * F)
					+ 0.00003         * Math.sin(2.0 * M1 + 2.0 * F)
					- 0.00003         * Math.sin(M1 + M + 2.0 * F)
					+ 0.00003         * Math.sin(M1 - M + 2.0 * F)
					- 0.00002         * Math.sin(M1 - M - 2.0 * F)
					- 0.00002         * Math.sin(3.0 * M1 + M)
					+ 0.00002         * Math.sin(4.0 * M1);
			break;

			case 1: /* First Quarter */
			case 3: /* Last Quarter */
				JDE = JDE
					- 0.62801         * Math.sin(M1)
					+ 0.17172 * E     * Math.sin(M)
					- 0.01183 * E     * Math.sin(M1 + M)
					+ 0.00862         * Math.sin(2.0 * M1)
					+ 0.00804         * Math.sin(2.0 * F)
					+ 0.00454 * E     * Math.sin(M1 - M)
					+ 0.00204 * E * E * Math.sin(2.0 * M)
					- 0.00180         * Math.sin(M1 - 2.0 * F)
					- 0.00070         * Math.sin(M1 + 2.0 * F)
					- 0.00040         * Math.sin(3.0 * M1)
					- 0.00034 * E     * Math.sin(2.0 * M1 - M)
					+ 0.00032 * E     * Math.sin(M + 2.0 * F)
					+ 0.00032 * E     * Math.sin(M - 2.0 * F)
					- 0.00028 * E * E * Math.sin(M1 + 2.0 * M)
					+ 0.00027 * E     * Math.sin(2.0 * M1 + M)
					- 0.00017         * Math.sin(O)
					- 0.00005         * Math.sin(M1 - M - 2.0 * F)
					+ 0.00004         * Math.sin(2.0 * M1 + 2.0 * F)
					- 0.00004         * Math.sin(M1 + M + 2.0 * F)
					+ 0.00004         * Math.sin(M1 - 2.0 * M)
					+ 0.00003         * Math.sin(M1 + M - 2.0 * F)
					+ 0.00003         * Math.sin(3.0 * M)
					+ 0.00002         * Math.sin(2.0 * M1 - 2.0 * F)
					+ 0.00002         * Math.sin(M1 - M + 2.0 * F)
					- 0.00002         * Math.sin(3.0 * M1 + M);

				W = 0.00306
				  - 0.00038 * E * Math.cos(M)
				  + 0.00026 * Math.cos(M1)
				  - 0.00002 * Math.cos(M1 - M)
				  + 0.00002 * Math.cos(M1 + M)
				  + 0.00002 * Math.cos(2.0 * F);
				if (phase == 3)
					W = -W;
				JDE += W;
			break;
		}
		/* final corrections */
		JDE = JDE
			+ 0.000325 * Math.sin(A[1])
			+ 0.000165 * Math.sin(A[2])
			+ 0.000164 * Math.sin(A[3])
			+ 0.000126 * Math.sin(A[4])
			+ 0.000110 * Math.sin(A[5])
			+ 0.000062 * Math.sin(A[6])
			+ 0.000060 * Math.sin(A[7])
			+ 0.000056 * Math.sin(A[8])
			+ 0.000047 * Math.sin(A[9])
			+ 0.000042 * Math.sin(A[10])
			+ 0.000040 * Math.sin(A[11])
			+ 0.000037 * Math.sin(A[12])
			+ 0.000035 * Math.sin(A[13])
			+ 0.000023 * Math.sin(A[14]);

		return(julianToDate(JDE));
	}

	/**
	 * Helper function that calculates the lunation number for a given date.
	 */
	private final int calcLunationNumber(Date currentDate)
	{
		return(calculatedLunationNumber + (int)((Long.valueOf(currentDate.getTime() / 1000L).doubleValue() - calculatedLunationDate) / calculatedSecondsInCalculatedLunation));
	}

	/**
	 * Helper function that converts degrees to radians.
	 */
	private final double toRad(double deg)
	{
		return((deg % 360.0) * 0.01745329251994329576);
	}

	/**
	 * Helper function that converts a Julian date to a time_t date.
	 */
	private final Date julianToDate(double jd)
	{
		int a, a1, z, b, c, d, e;
		int mon, mday, year, hour, min;
		double f, day;

		jd += 0.5;
		z = (int)jd;
		f = jd - z;

		if (z < 2299161)
		  a = z;
		else
		{
		  a1 = (int)((z - 1867216.25) / 36524.25);
		  a = z + 1 + a1 - a1 / 4;
		}

		b = a + 1524;
		c = (int)((Integer.valueOf(b).doubleValue() - 122.1) / 365.25);
		d = (int)(365.25 * Integer.valueOf(c).doubleValue());
		e = (int)(Integer.valueOf(b - d).doubleValue() / 30.6001);
		day = b - d - (int)(30.6001 * Integer.valueOf(e).doubleValue()) + f;

		mon = (e < 14) ? e - 2 : e - 14;
		year = (mon > 1) ? c - 4716 - 1900 : c - 4715 - 1900;
		mday = (int)day;
		day = (day - Integer.valueOf(mday).doubleValue()) * 24.0;
		hour = (int)day;
		day = (day - Integer.valueOf(hour).doubleValue()) * 60.0;
		min = (int)day;

		return(new Date(year, mon, mday, hour, min, 0));
	}

		
}
