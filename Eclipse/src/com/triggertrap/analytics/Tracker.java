package com.triggertrap.analytics;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.flurry.android.FlurryAgent;

public class Tracker {

	private static final String TAG = Tracker.class.getSimpleName();
	
	public static interface Categories {
		public static final String UI_INTERACTION = "ui_interaction";
		public static final String INTERNAL_ERROR = "internal_error";
	}
	
	public static interface Actions {
		public static final String PAGE_VIEW = "page_view";
		public static final String BUTTON_PRESS = "button_press";
		public static final String INPUT_SELCTION = "input_selection";
		public static final String LINK_SELECTION = "link_selection";
	}
	
	private static final String CATEGORY_KEY = "category";
	private static final String ACTION_KEY = "action";
	private static final String LABEL_KEY = "label";
	private static final String VALUE_KEY = "value";
	
	private Context mAppContext;
	private static Tracker mInstance = null;
	private static final String FLURRY_KEY = "JHXWBNTVXB6DNBBWZVF3";
	
	
	private Tracker(Context appContext) {
		mAppContext = appContext;
	
	}	
	public static Tracker getInstance(Context ctx) {
		final Context mAppContext = ctx.getApplicationContext();
		if (mInstance == null) {
			mInstance = new Tracker(mAppContext);
		}
		return mInstance;
	}
	
	public void onStartTracking() {
		 FlurryAgent.onStartSession(mAppContext, FLURRY_KEY);
	}
	
	
	public void onEvent(String category, String action, String label, String value) {
		
		Log.d(TAG,"Event: Category: " + category + " Action: " +action + " Label: " + label + " Value:" +value);
		if(action.equals(Actions.PAGE_VIEW)) {
			FlurryAgent.onPageView();
		}
		Map<String, String> eventParams = new HashMap<String, String>();
		eventParams.put(CATEGORY_KEY, category); 
		eventParams.put(ACTION_KEY, action); 
		eventParams.put(LABEL_KEY, label); 
		eventParams.put(VALUE_KEY, value); 
        
		FlurryAgent.onEvent(action, eventParams);
	}
	
	public void onStopTracking() {
		FlurryAgent.onEndSession(mAppContext);
	}
}
