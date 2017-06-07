package com.triggertrap.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.triggertrap.R;
import com.triggertrap.widget.OngoingButton;

public class CableReleaseFragment extends TriggertrapFragment {

	private static final String TAG = CableReleaseFragment.class.getSimpleName();
	private OngoingButton mShutterButton;
	
	public CableReleaseFragment() {
		
	}
	
	private SimpleModeListener mListener = null;
	
	public interface SimpleModeListener {
		public void onPressSimple();
		
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
        	mListener = (SimpleModeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SimpleModeListener");
        }
        
        
	}
   
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.cable_mode, container, false);         
        mShutterButton = (OngoingButton) rootView.findViewById(R.id.shutterButton); 
        TextView title = (TextView) rootView.findViewById(R.id.releaseTitle);
        title.setTypeface(SAN_SERIF_LIGHT);
        mShutterButton.setTouchListener(new OngoingButton.OnTouchListener() {
			@Override
			public void onTouchUp() {
				//Do nothing
			}
			@Override
			public void onTouchDown() {
				//startStopwatch();		
				if(mListener != null) {
					mListener.onPressSimple();
					checkVolume();
				}
			}
		});
 
        resetVolumeWarning();
    	return rootView;
    }
    
	
	@Override
	public void onStop() {
		Log.d(TAG,"Stopping");
		super.onStop();
		
	}
    public void onPressShutter() {
    	//Quick fix for now need to make sure Utputdispatche in service is used.
    	//mOutputDispatcher.trigger(TTApp.getInstance(getActivity()).getBeepLength());
    }
    
	@Override
	public void setActionState(boolean actionState) {
		if (actionState == true) {
			mState = State.STARTED;
		} else {
			mState = State.STOPPED;
		}
		setInitialUiState();
	}

	private void setInitialUiState() {
		if (mState == State.STARTED) {
						
		} else {
			mShutterButton.stopAnimation();
		}
	}
 }
