package com.triggertrap.inputs;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class HeadsetWatcher  {

	public static final int HEADSET_PLUGGED = 1;
	public static final int HEADSET_UNPLUGGED = 0;
	
	private static final String TAG = HeadsetWatcher.class.getSimpleName();
	
	private HeadsetBroadcastReceiver headsetReceiver;
	
	//Support two listeners Primary is always set secondary is transient
	private ArrayList<HeadsetListener> mListeners = null;
	
	public interface HeadsetListener {
		public void onHeadsetChanged(int state);
	}
	
	public HeadsetWatcher(Context ctx, HeadsetListener listener) {
		headsetReceiver = new HeadsetBroadcastReceiver(this);
		ctx.registerReceiver(headsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		mListeners = new ArrayList<HeadsetWatcher.HeadsetListener>();
		mListeners.add(listener);
	}
	
	public void changed(int state) {
		Log.d(TAG, "State changed " + state);
		if(mListeners != null) {
			for(HeadsetListener listener : mListeners) {
				if(listener != null) {
					listener.onHeadsetChanged(state);
				}
			}
		}
	}
	
	public void addSecondryListener(HeadsetListener listener) {
		mListeners.add(1, listener);
	}
	
	public void unregister(Context ctx) {
		ctx.unregisterReceiver(headsetReceiver);
	}
	
	public class HeadsetBroadcastReceiver extends BroadcastReceiver
    {     
        protected HeadsetWatcher watcher; 
        
        public HeadsetBroadcastReceiver(HeadsetWatcher watcher) {
        	super();
        	this.watcher = watcher; 
        }
        
        @Override        
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("Broadcast Receiver", action);
            if( (action.compareTo(Intent.ACTION_HEADSET_PLUG))  == 0) {
                int headsetState = intent.getIntExtra("state", 0); 
                watcher.changed(headsetState);
            }           
 
        }

    }

	
}

