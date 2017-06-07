package com.triggertrap.util;

import java.util.ArrayList;
import java.util.Iterator;

import android.view.animation.Interpolator;

public class CubicBezierInterpolator implements Interpolator {
	
	private static final String TAG = CubicBezierInterpolator.class.getSimpleName();
	
	//Algorithm loses accuracy above these levels
	private final float MAX_VALUE = 0.98f;
	private final float MIN_VALUE = 0.02f;
	
	private float mX1, mY1, mX2, mY2;
	
	public CubicBezierInterpolator(float x1, float y1, float x2, float y2) {
		setControlsInRange(x1, y1, x2,  y2);
	
	}
	
	private float A(float aA1, float aA2) { 
		return 1.0f - 3.0f * aA2 + 3.0f * aA1; 
	}
	private float B(float aA1, float aA2) { 
		return 3.0f * aA2 - 6.0f * aA1; 
	}
	private float C(float aA1) { 
		return 3.0f * aA1; 
	}
	
	private float calcBezier(float aT, float aA1, float aA2) {
	    return ((A(aA1, aA2)*aT + B(aA1, aA2))*aT + C(aA1))*aT;
	}
	   
	private float getSlope(float aT, float aA1, float aA2) {
	    return 3.0f * A(aA1, aA2)*aT*aT + 2.0f * B(aA1, aA2) * aT + C(aA1);
	}
	
	private float getTForX(float aX) {
	    // Newton raphson iteration
		float aGuessT = aX;
	    for (int i = 0; i < 4; ++i) {
	      float currentSlope = getSlope(aGuessT, mX1, mX2);
	      if (currentSlope == 0.0) return aGuessT;
	      float currentX = calcBezier(aGuessT, mX1, mX2) - aX;
	      aGuessT -= currentX / currentSlope;
	    }
	    return aGuessT;
	  }
	
	
	private void setControlsInRange(float x1, float y1, float x2, float y2) {
		if (x1 > MAX_VALUE) {
			mX1 = MAX_VALUE;
		} else if (x1 < MIN_VALUE) {
			mX1 = MIN_VALUE;
		} else {
			mX1 = x1;
		}
		
		if (y1 > MAX_VALUE) {
			mY1 = MAX_VALUE;
		} else if (y1 < MIN_VALUE) {
			mY1 = MIN_VALUE;
		} else {
			mY1 = y1;
		}
		
		if (x2 > MAX_VALUE) {
			mX2 = MAX_VALUE;
		} else if (x2 < MIN_VALUE) {
			mX2 = MIN_VALUE;
		} else {
			mX2 = x2;
		}
		
		if (y2 > MAX_VALUE) {
			mY2 = MAX_VALUE;
		} else if (y2 < MIN_VALUE) {
			mY2 = MIN_VALUE;
		} else {
			mY2 = y2;
		}
		
	
	}
	public void setControlPoints(float x1, float y1, float x2, float y2) {
		setControlsInRange(x1, y1, x2,  y2);	
	}
	
	@Override
	public float getInterpolation(float input) {
		if (mX1 == mY1 && mX2 == mY2) return input; // linear
		return calcBezier(getTForX(input), mY1, mY2);	
	}
	
	

	public long[] getOriginalPauses(long sequenceDuration, int count, long exposure, long gap) {
		
		long[] timeIntervals = calculateTimeIntervals(sequenceDuration, count,  exposure,  gap);	
		long pauses[] = new long[count];
		//String orginalPauses = "";
		for (int i = 0; i < count; i++) {
			pauses[i] = timeIntervals[i+ 1] - timeIntervals[i] -  exposure ;	
			//orginalPauses = orginalPauses + pauses[i] + " ";
		}
		//Log.d(TAG , "Original Pauses: " + orginalPauses);
		return pauses;
	}
	
	private long[] calculateTimeIntervals(long sequenceDuration, int count, long exposure, long gap) {
		long minInterval = exposure + gap;
		long [] timeIntervals = new long[count +1]; 
		timeIntervals[count] = sequenceDuration + minInterval;
		//String timeLapseString = "";
		for (int i = 0; i < count; i++) {
			float fraction =  (float) i / (count - 1);			
			timeIntervals[i] = (long) (getInterpolation(fraction) * sequenceDuration);	
			//timeLapseString = timeLapseString + timeIntervals[i] + " ";
		}
		
		//Log.d(TAG, "TIME INTERVALS: " + timeLapseString);
		return timeIntervals;
	}
	public long[] getPauses(long sequenceDuration, int count, long exposure, long gap) {
		ArrayList<Integer> overlapIndicies = new ArrayList<Integer>();
		ArrayList<Long> adjustedPauses = new ArrayList<Long>();
		//long [] timeIntervals = new long[count +1]; 
		long pauses[] = new long[count];
		//long minInterval = exposure + gap;
		//timeIntervals[count] = sequenceDuration + minInterval;
		
		long[] timeIntervals = calculateTimeIntervals(sequenceDuration, count,  exposure,  gap);	
		
//		for (int i = 0; i < count; i++) {
//			float fraction =  (float) i / (count - 1)  ;
//			timeIntervals[i] = (long) (getInterpolation(fraction) * sequenceDuration);	
//		
//		}
		
		//String pausesString = "";
		boolean inNegativeSequnce = false;
		for (int i = 0; i < count; i++) {
			pauses[i] = timeIntervals[i+ 1] - timeIntervals[i] -  exposure ;	
			//pausesString = pausesString + pauses[i] + " ";
			//Track where we have overlaps so we can correct by dropping shots
			if(pauses[i] <= 0 && !inNegativeSequnce) {
				overlapIndicies.add(i);
				inNegativeSequnce = true;
			} else if (pauses[i] > 0 && inNegativeSequnce) {
				overlapIndicies.add(i);
				inNegativeSequnce = false;
			}
		}
		//Log.d(TAG , "Pauses: " + pausesString);
		//Correct for overlapping pauses
		// Calculate how many exposure can fit in time 
		// add as many as possible and drop the rest.
		int overlapCount = 0;
		for (int i = 0; i < count; i++) {
			if(pauses[i] > 0)  {
				adjustedPauses.add(pauses[i]);
			} else {
				if(overlapIndicies.size() > 0) {
					int startIndex = overlapIndicies.get(overlapCount);
					int endIndex = overlapIndicies.get(overlapCount + 1);
					long overlaptimePeriod = timeIntervals[endIndex]
							- timeIntervals[startIndex];
					int numberOfShots = (int) (overlaptimePeriod / (exposure + gap));
					if (numberOfShots != 0) {
						long newGap = (overlaptimePeriod - (exposure * numberOfShots))
								/ numberOfShots;
						for (int j = 0; j < numberOfShots; j++) {
							adjustedPauses.add(newGap);
						}
					}
					i = endIndex;
					overlapCount += 2;
				}
			}
		}
		
		long[] ret = new long[adjustedPauses.size()];
		Iterator<Long> iterator = adjustedPauses.iterator();
		for (int i = 0; i < ret.length; i++) {
			ret[i] = iterator.next().longValue();
		}
			
		return ret;
	}

}
