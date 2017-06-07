package com.triggertrap.util;

import android.content.Context;
import android.util.Log;

import com.triggertrap.TTApp;
import com.triggertrap.fragments.TimeWarpFragment;

public class PulseGenerator {

	private static final String TAG = PulseGenerator.class.getSimpleName();
	
	private Context mAppContext;
	
	//Easing options.
	public interface Easing {
		public static final int EASE_IN = 0;
		public static final int EASE_OUT = 1;
		public static final int EASE_IN_OUT = 2;
	}
	
	public PulseGenerator(Context appContext) {
		//Make sure we have an application context.
		mAppContext = appContext.getApplicationContext();
	}
	
	public static long getSequenceTime(long[] sequence) {
		int count = sequence.length/2;
		long totalTime = 0;
		for (int i = 0; i < count; i++) {      
			totalTime += sequence[i * 2]; 
			totalTime += sequence[(i * 2) + 1];     	    
		}
		return totalTime;
		
	}
	
	public static void logSequence(long [] sequence) {
		StringBuffer stringBuf = new StringBuffer();
		int count = sequence.length/2;
		for(int i = 0 ; i < count; i++) {
			stringBuf.append("[" +  sequence[i * 2]  + ","  + 	sequence[(i * 2) + 1] + "] ");
		}
		Log.d(TAG,"Sequence: " +  stringBuf.toString());
	}
	
	public long[] getTimeLapseSequence(long gap) {
		long[] sequence = new long[2];
		sequence[0] = TTApp.getInstance(mAppContext).getBeepLength();
		//sequence[1] = gap - TTApp.getInstance(mAppContext).getGapLenth();
		sequence[1] = gap - TTApp.getInstance(mAppContext).getBeepLength();
		return sequence;
	}
	
	/**
	 * Generate a pulse sequence for Star trail mode
	 * @param count Number of pulses
	 * @param pulseLength Length of pulse exposure
	 * @param gap Length of pulse gap
	 * @return
	 */
	public long[] getStarTrailSequence(int count, long pulseLength, long gap) {
//		long[] sequence = new long[2];
//		sequence[0] = pulseLength;
//		sequence[1] = gap - pulseLength;
		long[] sequence = new long[count*2];
		for (int i = 0; i < count; i++) {      
			sequence[i * 2] = pulseLength;
			sequence[(i * 2) + 1] = gap;       
	    
		}
		return sequence;
	}
	
	
	/**
	 * Generate a HDR or HDR timelapse sequence
	 * @param middle The middle shutter time in millseconds e.g 500
	 * @param count The Number of exposures
	 * @param ev The exposure value 0.33 , 0.5 ,1 or 2
	 * @param interval The interval betweeen HDR sequences set to 0 if we do not want timelapse
	 * @return The HDR pulse sequence
	 */
	public long[] getHdrSequence(long middle,int count, float ev, long interval)  {
		
		//Make sure count is always an odd number
		if(count % 2 != 1) {
	        count++;
	    }
		long[] sequence = new long[count*2];
		int j = 0;
		int step = (count - 1) / 2;
		for (int i = -step; i <= step; i++) {
			long exposure = Math.round(Math.pow(Math.pow(2, ev), i) * middle);
			//Log.d(TAG, "Exposure time: " + exposure);
			sequence[j] = exposure;
			sequence[j + 1] = TTApp.getInstance(mAppContext).getHDRGapLength();			
			j += 2;
		}
	    if(interval > 0)  {	
	    	long totaltime = PulseGenerator.getSequenceTime(sequence);
	    	sequence[sequence.length - 1] = interval - totaltime + TTApp.getInstance(mAppContext).getHDRGapLength();
	    }
		
		return sequence;
	}
	
	
	public long[] getBrampingSequence(int count, long interval, long firstExposure, long lastExposure)  {
		long[] sequence = new long[count*2];

		for (int i = 0; i < count; i++) {
			float fraction = (float) i / (count - 1) ;
			long exposure = (long) ((fraction) * (lastExposure - firstExposure) + firstExposure);
			sequence[i * 2] = exposure;
			sequence[(i * 2) + 1] = interval - sequence[i * 2];

		}		
		return sequence;
	}
	
	
	public long[] getTimeWarpSequence(int count, long sequenceDuration, CubicBezierInterpolator interpolator) {
		
		long[]  pauses = interpolator.getPauses(sequenceDuration, count, TTApp.getInstance(mAppContext).getBeepLength(), TimeWarpFragment.MINIMUM_TIMEWARP_GAP);
		long[] sequence = new long[(pauses.length)*2];
		
		for (int i = 0; i < pauses.length; i++) {
			sequence[i * 2] = TTApp.getInstance(mAppContext).getBeepLength();	
			if(pauses[i] >= 0) {
				sequence[(i * 2) + 1] = pauses[i];
			} else {
				sequence[(i * 2) + 1] = 1;
			}
		}		
		//PulseGenerator.logSequence(sequence);
		Log.d(TAG, "SEQUENCE TIME: " + getSequenceTime(sequence));
		return sequence;
	}
	
	public long[] getEasedTimeLapSequence(double exponent, int frames, int duration, int easingType) {
		//extra buffer in milliseconds
		final int buffer = 10;
		long min = TTApp.getInstance(mAppContext).getBeepLength(); 
		long pulseLength = TTApp.getInstance(mAppContext).getBeepLength();
		long[] sequence = new long[frames*2];
		long gap;
		min += buffer;
		double ratio;
		double eased;
		double previous = 0;
		for(int i = 0; i < frames; i++) {
			ratio = i/frames;
			switch (easingType) {
				case Easing.EASE_IN:
					eased = easeIn(ratio, exponent);
					break;
				case Easing.EASE_OUT:
					eased = easeOut(ratio, exponent);
					break;
				case Easing.EASE_IN_OUT:
					eased = easeInOut(ratio, exponent);
					break;
				default:
					eased = easeInOut(ratio, exponent);
			}
			gap = Math.round((eased - previous) * duration);
			gap = (gap < min) ? min : gap;
			previous = eased;
			sequence[i*2] = pulseLength;
			sequence[(i*2) + 1] = gap - (pulseLength);
			
		}
		
		return sequence;
	}
	
	private double easeIn(double frame, double exponent) {
		return Math.pow(frame, exponent);	
	}
	
	private double easeOut(double frame, double exponent) {
		return 1 - Math.pow((1.0 - frame), exponent);
	}
	
	private double easeInOut(double frame, double exponent) {
	    if(frame < 0.5) {
	        return easeIn(frame * 2.0, exponent) / 2.0;
	    } else {
	        return 0.5 + (easeOut((frame - 0.5) * 2.0, exponent) / 2.0);
	    }
	}
	
}
