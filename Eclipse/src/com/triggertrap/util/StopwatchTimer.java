package com.triggertrap.util;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

public abstract class StopwatchTimer {

	private static final int MSG = 1;
	private final long mCountdownInterval;
	private long mStartTime;
	
	public StopwatchTimer(long countDownInterval) {      
        mCountdownInterval = countDownInterval;
    }
	
	 /**
     * Start the countdown.
     */
    public synchronized final StopwatchTimer start() {
       
    	mStartTime = SystemClock.elapsedRealtime() ;
        mHandler.sendMessage(mHandler.obtainMessage(MSG));
        return this;
    }

    /**
     * Cancel the countdown.
     */
    public final void cancel() {
        mHandler.removeMessages(MSG);
    }

    
	public abstract void onTick(long millisUntilFinished);
	
    // handles counting down
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            synchronized (StopwatchTimer.this) {
                final long milliElapsed = SystemClock.elapsedRealtime() - mStartTime;

               if (milliElapsed < mCountdownInterval) {
                    // no tick, just delay until done
                    sendMessageDelayed(obtainMessage(MSG), milliElapsed);
                } else {
                    long lastTickStart = SystemClock.elapsedRealtime();
                    onTick(milliElapsed);

                    // take into account user's onTick taking time to execute
                    long delay = lastTickStart + mCountdownInterval - SystemClock.elapsedRealtime();

                    // special case: user's onTick took more than interval to
                    // complete, skip to next interval
                    while (delay < 0) delay += mCountdownInterval;
                    sendMessageDelayed(obtainMessage(MSG), delay);
                }
            }
        }
    };
}
