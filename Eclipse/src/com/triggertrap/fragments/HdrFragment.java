package com.triggertrap.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.OnWheelScrollListener;
import antistatic.spinnerwheel.adapters.ArrayWheelAdapter;

import com.triggertrap.R;
import com.triggertrap.TTApp;
import com.triggertrap.fragments.dialog.HDRErrorDialog;
import com.triggertrap.util.PulseGenerator;
import com.triggertrap.view.CircleTimerView;
import com.triggertrap.view.CountingTimerView;
import com.triggertrap.view.SimpleTimerView;
import com.triggertrap.widget.OngoingButton;

public class HdrFragment extends PulseSequenceFragment {

	private static final String TAG = HdrFragment.class.getSimpleName();
	

	private int[] mShutterSpeedValues;
	private float[] mEvValues = {0.33f,0.5f,1.0f,2.0f};
	private int[] mNumExposures = {3, 5, 7, 9, 11, 13, 15, 17, 19};
	private long mCurrentMiddleSpeed = 60000;
	private float mCurrentEvValue = mEvValues[2];
	private int mCurrentNumExposures = 3;
	
	// Scrolling flag
    private boolean mScrolling = false;
	
	private View mRootView;
	private OngoingButton mButton;
	
	private View mCountDownLayout;
	private CircleTimerView mCircleTimerView;
	private CountingTimerView  mTimerText;
	private SimpleTimerView mExposureTimerText;
	private SimpleTimerView mGapTimerText;
	private TextView mSequenceCountText;
	
	
	private Animation mSlideInFromTop;
	private Animation mSlideOutToTop;
	
	private boolean mSyncCircle = false;
	
	public HdrFragment() {
		mRunningAction = TTApp.OnGoingAction.HDR;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.le_hdr, container, false);
		
		
		mShutterSpeedValues = getResources().getIntArray(R.array.shutter_speed_values);
		final String[] shutterSpeeds = getResources().getStringArray(R.array.shutter_speeds);
		
		 ArrayWheelAdapter<String> middleAdapter =
		            new ArrayWheelAdapter<String>(getActivity(), shutterSpeeds);
		
		 View middleExposure =  mRootView.findViewById(R.id.hdrMiddleExposure);
		 
		 final AbstractWheel hdrMiddle = (AbstractWheel) middleExposure.findViewById(R.id.wheelHorizontalView);
		 middleAdapter.setItemResource(R.layout.wheel_text_centered);
		 middleAdapter.setItemTextResource(R.id.text);
	     hdrMiddle.setViewAdapter(middleAdapter);
   
	     
	     ArrayWheelAdapter<String> numExpAdapter =
		            new ArrayWheelAdapter<String>(getActivity(), new String[]{ "3", "5", "7", "9", "11", "13", "15", "17", "19" });
	     View numExposure =  mRootView.findViewById(R.id.hdrNumberExposures);
		 final AbstractWheel numHdrExposures = (AbstractWheel) numExposure.findViewById(R.id.wheelHorizontalView);
	     //NumericWheelAdapter numExpAdapter = new NumericWheelAdapter(getActivity(), 3, 19, "%1d");
	     numExpAdapter.setItemResource(R.layout.wheel_text_centered);
	     numExpAdapter.setItemTextResource(R.id.text);
	     numHdrExposures.setViewAdapter(numExpAdapter);
	     
	    
         
	     ArrayWheelAdapter<String> evAdapter =
		            new ArrayWheelAdapter<String>(getActivity(), new String[]{ "1/3", "1/2", "1", "2"});
	     evAdapter.setItemResource(R.layout.wheel_text_centered);
	     evAdapter.setItemTextResource(R.id.text);
	     View evSteps =  mRootView.findViewById(R.id.hdrEvStep);
	     final AbstractWheel hdrEv = (AbstractWheel) evSteps.findViewById(R.id.wheelHorizontalView);     
	     hdrEv.setViewAdapter(evAdapter);
	     
	    
	    setUpMiddleExposure(hdrMiddle);
	    setUpNumberExposures(numHdrExposures);
	    setUpEvValues(hdrEv);
	    setUpButton();
		setUpAnimations();
		setUpCircleTimer();
		resetVolumeWarning();
		return mRootView;
	}
	
	@Override
	public void onStop() {
		super.onStop();
		//Persist the state of the star trail mode  
		TTApp.getInstance(getActivity()).setHDRMiddleExposure(mCurrentMiddleSpeed);
		TTApp.getInstance(getActivity()).setHDRNumExposures(mCurrentNumExposures);
		TTApp.getInstance(getActivity()).setHDREvStep(mCurrentEvValue);
	}
	
	private void setUpMiddleExposure(final AbstractWheel hdrMiddle) {
	     hdrMiddle.addScrollingListener( new OnWheelScrollListener() {
	            public void onScrollingStarted(AbstractWheel wheel) {
	                mScrolling = true;
	            }
	            public void onScrollingFinished(AbstractWheel wheel) {
	            	mScrolling = false;
	            	Log.d(TAG,"New shutter speed: "  + mShutterSpeedValues[hdrMiddle.getCurrentItem()]);
	            	mCurrentMiddleSpeed = mShutterSpeedValues[hdrMiddle.getCurrentItem()];
	            }
	     });
	     
	     mCurrentMiddleSpeed = TTApp.getInstance(getActivity()).getHDRMiddleExposure();
	     Log.d(TAG,"Getting Middle exposure of:" + mCurrentMiddleSpeed);
	     Log.d(TAG,"Current middle exposure index:" + hdrMiddle.getCurrentItem());
	     int i = 0;
	     for(int speed: mShutterSpeedValues) {
	    	 if (speed == mCurrentMiddleSpeed) {
	    		 break;
	    	 }
	    	 i++;
	     }
	     hdrMiddle.setCurrentItem(i);
	}
	
	private void setUpNumberExposures(final AbstractWheel numExposures) {
		numExposures.addScrollingListener( new OnWheelScrollListener() {
	            public void onScrollingStarted(AbstractWheel wheel) {
	                mScrolling = true;
	            }
	            public void onScrollingFinished(AbstractWheel wheel) {
	            	mScrolling = false;
	            	int index = numExposures.getCurrentItem();
	            	mCurrentNumExposures = mNumExposures[index];
	            	Log.d(TAG, "CurrentExposure Number: " + mCurrentNumExposures);
	            }
	     });
		
		mCurrentNumExposures = TTApp.getInstance(getActivity()).getHDRNumExposures();
		int i = 0;
		for(int numberExp: mNumExposures) {
			if (mCurrentNumExposures ==  numberExp) {
				numExposures.setCurrentItem(i);
				break;
			}
			i++;
		}
			
	}
	
	
	private void setUpEvValues(final AbstractWheel hdrEv) {
		hdrEv.addScrollingListener( new OnWheelScrollListener() {
	            public void onScrollingStarted(AbstractWheel wheel) {
	                mScrolling = true;
	            }
	            public void onScrollingFinished(AbstractWheel wheel) {
	            	mScrolling = false;

	            	mCurrentEvValue = mEvValues[hdrEv.getCurrentItem()];
	            }
	     });
		
		 mCurrentEvValue = TTApp.getInstance(getActivity()).getHDREvStep();
	     int i = 0;
	     for(float evValue: mEvValues) {
	    	 if (evValue == mCurrentEvValue) {
	    		 break;
	    	 }
	    	 i++;
	     }
	     hdrEv.setCurrentItem(i);
		
	}
	
	
	private void setUpButton() {
		mButton = (OngoingButton) mRootView
				.findViewById(R.id.hdrButton);	
		mButton.setToggleListener(new OngoingButton.OnToggleListener() {

			@Override
			public void onToggleOn() {
				Log.d(TAG, "onToggleON");
				onStartTimer();
				checkVolume();
			}

			@Override
			public void onToggleOff() {
				Log.d(TAG, "onToggleOff");
				onStopTimer();
			}
		});
	}
	
	private void setUpCircleTimer() {
		Log.d(TAG,"Setting up circle timer");
		mCountDownLayout = mRootView.findViewById(R.id.circularTimer);
		mCircleTimerView = (CircleTimerView) mRootView.findViewById(R.id.circleTimer);
		mTimerText= (CountingTimerView) mRootView.findViewById(R.id.countingTimeText);
		mCircleTimerView.setTimerMode(true);	
		mExposureTimerText = (SimpleTimerView) mRootView.findViewById(R.id.expsoureTimeText);
		mGapTimerText = (SimpleTimerView) mRootView.findViewById(R.id.gapTimeText);
		mSequenceCountText = (TextView) mRootView.findViewById(R.id.sequenceCount);
		
		mCountDownLayout.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				//Consume the touch event when the count down is visible
				return true;
			}
		});
		
	}
	
	
	private void setUpAnimations() {
		mSlideInFromTop = AnimationUtils.loadAnimation(getActivity(),
				R.anim.slide_in_from_top);
		mSlideOutToTop = AnimationUtils.loadAnimation(getActivity(),
				R.anim.slide_out_to_top);

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
			mSyncCircle = true;
			showCountDown();
			mButton.startAnimation();
		} else {
			hideCountDown();
			mButton.stopAnimation();
		}
	}
	
	private void showCountDown() {
		mCountDownLayout.setVisibility(View.VISIBLE);
		
	}
	
	private void hideCountDown() {
		mCountDownLayout.setVisibility(View.GONE);	
	}
	
	public void onPulseStarted(int currentCount, int totalCount, long timeToNext, long totalTimeMaining) {		
		String sequenceProgress = currentCount + "/" + totalCount;
		Log.d(TAG ,"Pulse started Sequence count: " + sequenceProgress);
		mSequenceCountText.setText(sequenceProgress);		
	}
	
	
	public void onPulseUpdate(long [] sequence, int exposures, long timeToNext,
			long remainingPulseTime, long remainingSequenceTime) {
		//Log.d(TAG,"Pulse update exposures: " +exposures);
		//Log.d(TAG,"Remaining sequence time: " + remainingSequenceTime);
		int currentSeq = exposures -1;
		long currentExposure = sequence[currentSeq *  2];
		long currentGap =  sequence[(currentSeq * 2) + 1];
		long remainingExposure = 0;
		long remainingGap = 0;
		if(remainingPulseTime > currentGap) {
			//We need to count down the exposure
			remainingExposure = currentExposure - (timeToNext - remainingPulseTime);
			mExposureTimerText.setTime(remainingExposure, true, true);
			if ((remainingGap == 0) && ((currentSeq * 2) + 1) <=  sequence.length) {
				long nextGap = sequence[(currentSeq * 2) + 1];
				mGapTimerText.setTime(nextGap, true, true);
			} else  if (((currentSeq * 2) + 1) >  sequence.length) {
				mGapTimerText.setTime(0, true, true);
			}
		} else {
			//We need to count down the gap
			remainingGap = remainingPulseTime;
			mGapTimerText.setTime(remainingGap, true, true);		
			if ((remainingExposure == 0) && ((currentSeq * 2) + 2) <  sequence.length) {
				long nextExposure = sequence[(currentSeq * 2) + 2];
				mExposureTimerText.setTime(nextExposure, true, true);
			} else  if (((currentSeq * 2) + 2) >=  sequence.length) {
				mExposureTimerText.setTime(0, true, true);
			}
			
		}
		
		mTimerText.setTime(remainingSequenceTime, true, true);
		if(remainingPulseTime != 0) {
			if (mSyncCircle) {
				String sequenceProgress = exposures + "/" + (sequence.length/2);
				mSequenceCountText.setText(sequenceProgress);		
				synchroniseCircle(remainingSequenceTime,  sequence);
			}
		}
	}
	
	private void synchroniseCircle(long remainingTime, long[] sequence) {
		 long totaltime = PulseGenerator.getSequenceTime(sequence);
		 Log.d(TAG,"Sequence time: " + totaltime);
		 Log.d(TAG,"Remaining time: " + remainingTime);
		 mCircleTimerView.setIntervalTime(totaltime);
		 mCircleTimerView.setPassedTime((totaltime - remainingTime), true);
		 mCircleTimerView.startIntervalAnimation();
		 mSyncCircle = false;
	}
	
	public void onPulseStop() {
		mTimerText.setTime(0, true, true);
		onStopTimer();
	}
	
	
	//Debug function
	private void logSequence(long [] sequence) {
		String squencestring = "[ ";
		for(int i = 0; i < sequence.length; i ++) {
			squencestring += sequence[i];
			squencestring += ",";
		}
		squencestring += "]";	
		Log.d(TAG ,"HDR sequence is: " + squencestring);
	}
	
	private boolean validateSequence(long[] sequence) {
		boolean valid = true; 
		for(int i = 0; i < sequence.length; i ++) {
			if (sequence[i] == 0) {
				valid = false;
				break;
			}
		}
		return valid;
	}
	
	private void onStartTimer() {
		
		if (mState == State.STOPPED) {
						
			
			mPulseSequence = mPulseGenerator.getHdrSequence(mCurrentMiddleSpeed,mCurrentNumExposures, mCurrentEvValue, 0);
			if(!validateSequence(mPulseSequence)) {
				mButton.stopAnimation();
				HDRErrorDialog hdrDialog = new HDRErrorDialog();
				hdrDialog.show(getActivity());
				return;
			}
			
			mState = State.STARTED;			
			mCountDownLayout.setVisibility(View.VISIBLE);
			mCountDownLayout.startAnimation(mSlideInFromTop);
			mCircleTimerView.setPassedTime(0, false);
		
			
			mPulseSeqListener.onPulseSequenceCreated(TTApp.OnGoingAction.HDR, mPulseSequence, false);
			logSequence(mPulseSequence);
			
			long totaltime = PulseGenerator.getSequenceTime(mPulseSequence);
			Log.d(TAG, "Total time Circle: " +  totaltime);
			mCircleTimerView.setIntervalTime(totaltime);
			
			mExposureTimerText.setTime(mPulseSequence[0], true, true);
			mGapTimerText.setTime(mPulseSequence[1], true, true);
			mCircleTimerView.startIntervalAnimation();
			mTimerText.setTime(totaltime, true, false);			
		} 
	}
	
	private void onStopTimer() {
		if (mState == State.STARTED) {
			mCircleTimerView.abortIntervalAnimation();
			mCountDownLayout.startAnimation(mSlideOutToTop);
			mCountDownLayout.setVisibility(View.GONE);
			mButton.stopAnimation();
			mState = State.STOPPED;
			mPulseSeqListener.onPulseSequenceCancelled();
			//mProgressCountText.setText(String.valueOf(0));
		}
	}
	
}
