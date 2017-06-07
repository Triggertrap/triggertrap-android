package com.triggertrap.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.triggertrap.TTApp;
import com.triggertrap.util.WarningMessageManager;

public class TriggertrapFragment extends Fragment {

	public static interface BundleKey {
		public static final String FRAGMENT_TAG = "fragment_tag";
		public static final String IS_ACTION_ACTIVE = "is_action_active";
		public static final String PULSE_INTERVAL= "pulse_interval";
		public static final String MIDDLE_EXPOSURE = "middle_exposure";
		public static final String NUMBER_EXPOSURES = "number_exposures";
		public static final String EV_VALUE = "ev_value";
		public static final String EASING = "easing";	
		public static final String WIFI_SLAVE_INFO = "wifi_slave_info";
	}

	protected Typeface SAN_SERIF_LIGHT = null;
	protected Typeface SAN_SERIF_THIN = null;

	public TriggertrapFragment() {


	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		SAN_SERIF_LIGHT = Typeface.createFromAsset(activity.getAssets(),
	             "fonts/Roboto-Light.ttf");
		SAN_SERIF_THIN = Typeface.createFromAsset(activity.getAssets(),
	             "fonts/Roboto-Thin.ttf");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		resetVolumeWarning();
	}
	
	protected int mRunningAction = TTApp.OnGoingAction.NONE;
	protected Bundle mStateBundle;
		
	protected int mState = State.STOPPED;
	protected interface State {
		public int STARTED = 0;
		public int STOPPED = 1;
	}
	
	public int getRunningAction() {
		return mRunningAction;
	}
	public Bundle getStateBundle() {
		mStateBundle = new Bundle();
		mStateBundle.putString(TriggertrapFragment.BundleKey.FRAGMENT_TAG, getTag());
		return mStateBundle;
	}
	
	/*After the volume warning has been shown we need to reset to show again*/
	public void resetVolumeWarning() {
		  Intent intent = new Intent(WarningMessageManager.ACTION);
          intent.putExtra(WarningMessageManager.ACTION_TYPE, WarningMessageManager.Action.RESET);       
          LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
	}
	
	public void checkVolume() {
		  Intent intent = new Intent(WarningMessageManager.ACTION);
          intent.putExtra(WarningMessageManager.ACTION_TYPE, WarningMessageManager.Action.SHOW);       
          LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
	}
	
	
	public void setActionState(boolean actionState) {

	}
	public void dismissError() {	
	}
}
