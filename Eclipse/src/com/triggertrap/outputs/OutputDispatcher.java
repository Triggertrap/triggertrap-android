package com.triggertrap.outputs;

import android.content.Context;

import com.triggertrap.outputs.PulseHandler.PulseListener;

public class OutputDispatcher implements PulseListener {
	
	private static final String TAG = OutputDispatcher.class.getSimpleName();
	PulseHandler mPulseHandler; 
	
	
	private OutputListener mListener;
	
	public interface OutputListener {
		public void onOutputStart();
		public void onOutputStop();
		public void onOutputPauseDone();
	}
	
	public OutputDispatcher(OutputListener listener, Context context) {
		mListener = listener;
		mPulseHandler = new PulseHandler(this, context);
	}
	
	
	
	public void trigger(long length) {
		mPulseHandler.playPulse(length);
	}
	
	public void trigger(long length, long pauseLength) {
		mPulseHandler.playPulse(length, pauseLength);
	}
		
	public void start() {
		mPulseHandler.playPulse(PulseHandler.FOREVER);
	}
	
	public void stop() {
		mPulseHandler.stop();
	}
	
	public void close() {
		mPulseHandler.close();
	}

	public void setOutputListener(OutputListener listener) {
		mListener = listener;
	}
	
	//Listener methods for the Pulse Handler
	@Override
	public void onPulseStart() {
		if(mListener != null) {
			mListener.onOutputStart();
		}
		
	}

	@Override
	public void onPulseStop() {
		if(mListener != null) {
			mListener.onOutputStop();
		}
		
	}

	@Override
	public void onPulsePauseDone() {
		if(mListener != null) {
			mListener.onOutputPauseDone();
		}
		
	}

}
