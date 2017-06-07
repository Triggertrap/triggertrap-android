package com.triggertrap.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.OnWheelScrollListener;
import antistatic.spinnerwheel.adapters.ArrayWheelAdapter;

import com.triggertrap.R;
import com.triggertrap.TTApp;
import com.triggertrap.util.DialpadManager;
import com.triggertrap.util.PulseGenerator;
import com.triggertrap.view.CircleTimerView;
import com.triggertrap.view.CountingTimerView;
import com.triggertrap.view.SimpleTimerView;
import com.triggertrap.widget.NumericView;
import com.triggertrap.widget.OngoingButton;
import com.triggertrap.widget.TimerView;

public class BrampingFragment extends PulseSequenceFragment implements DialpadManager.InputUpdatedListener {

    private static final String TAG = BrampingFragment.class.getSimpleName();

    private TimerView mExposureTimeInput;
    private View mExposureTime;
    private View mButtonContainer;
    private NumericView mNumericInput;

    private View mRootView;
    private View mCountDownLayout;
    private CircleTimerView mCircleTimerView;
    private CountingTimerView mTimerText;
    private SimpleTimerView mExposureTimerText;
    private SimpleTimerView mGapTimerText;
    private TextView mSequenceCountText;
    private TextView mDurationText;
    private OngoingButton mButton;
    private AbstractWheel mBrampStart;
    private AbstractWheel mBrampEnd;

    private int mCurrentExposureCount;

    private DialpadManager.InputSelectionListener mInputListener = null;

    private int[] mShutterSpeedValues;
    private long mStartExposure;
    private long mEndExposure;
    private int mIterations = 0;
    private long mDuration;
    private long mInterval;
    private boolean mSyncCircle = false;
    private boolean mScrolling = false;
    private boolean mAutomaticAdjustStart = false;
    private boolean mAutomaticAdjustEnd = false;
    private String[] mShutterSpeeds;
    private String[] mValidShutterSpeeds;

    private Animation mSlideInFromTop;
    private Animation mSlideOutToTop;

    public BrampingFragment() {
        mRunningAction = TTApp.OnGoingAction.BRAMPING;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mInputListener = (DialpadManager.InputSelectionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialpadManager.InputSelectionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mShutterSpeedValues = getResources().getIntArray(R.array.shutter_speed_values);
        mShutterSpeeds = getResources().getStringArray(R.array.shutter_speeds);

        mRootView = inflater.inflate(R.layout.bramping, container, false);
        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInputListener != null) {
                    mInputListener.onInputDeSelected();
                }
            }
        });

        View startExposure = mRootView.findViewById(R.id.brampStartExposure);
        View endExposure = mRootView.findViewById(R.id.brampEndExposure);
        mDurationText = (TextView) mRootView.findViewById(R.id.duratationText);

        mBrampStart = (AbstractWheel) startExposure.findViewById(R.id.wheelHorizontalView);
        mBrampEnd = (AbstractWheel) endExposure.findViewById(R.id.wheelHorizontalView);


        //String [] validShutterSpeeds  = getValidShutterSpeeds();
        ArrayWheelAdapter<String> exposureAdapter = new ArrayWheelAdapter<String>(
                getActivity(), mShutterSpeeds);

        exposureAdapter.setItemResource(R.layout.wheel_text_centered);
        exposureAdapter.setItemTextResource(R.id.text);
        mBrampStart.setViewAdapter(exposureAdapter);
        mBrampEnd.setViewAdapter(exposureAdapter);

        setUpButton();
        setUpIterations();
        setUpStartExposureSetting(mBrampStart);
        setUpEndExposureSetting(mBrampEnd);
        setUpTimeInputs();
        setKeyBoardSize();
        setUpAnimations();
        setUpCircleTimer();
        setUpDuration();
        setValidShutterSpeeds();
        resetVolumeWarning();
        return mRootView;
    }


    @Override
    public void onStop() {
        super.onStop();
        //Persist the state of the bramping mode
        TTApp.getInstance(getActivity()).setBrampingInterval(mExposureTimeInput.getTime());
        TTApp.getInstance(getActivity()).setBrampingIterations(mNumericInput.getValue());
        TTApp.getInstance(getActivity()).setBrampingStartExposure(mStartExposure);
        TTApp.getInstance(getActivity()).setBrampingEndExposure(mEndExposure);
    }


    private void onStartTimer() {

        if (mState == State.STOPPED) {

            if (mNumericInput.getValue() == 0 || mExposureTimeInput.getTime() == 0) {
                mButton.stopAnimation();
                return;
            }

            mState = State.STARTED;
            mCountDownLayout.setVisibility(View.VISIBLE);
            mCountDownLayout.startAnimation(mSlideInFromTop);
            mCircleTimerView.setPassedTime(0, false);

            mPulseSequence = mPulseGenerator.getBrampingSequence(mNumericInput.getValue(), mExposureTimeInput.getTime(), mStartExposure, mEndExposure);
            mPulseSeqListener.onPulseSequenceCreated(mRunningAction, mPulseSequence, false);

            long totaltime = PulseGenerator.getSequenceTime(mPulseSequence);
            Log.d(TAG, "Total time Circle: " + totaltime);
            mCircleTimerView.setIntervalTime(totaltime);
            mTimerText.setTime(mExposureTimeInput.getTime(), true, false);
            mExposureTimerText.setTime(mPulseSequence[0], true, true);
            mGapTimerText.setTime(mPulseSequence[1], true, true);
            mCircleTimerView.startIntervalAnimation();
            mTimerText.setTime(totaltime, true, false);
            String sequenceProgress = 1 + "/" + (mPulseSequence.length / 2);
            mSequenceCountText.setText(sequenceProgress);

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

    private void setUpDuration() {
        mDuration = mExposureTimeInput.getTime() * mNumericInput.getValue();
        mDurationText.setText(formatTime(mDuration));
    }

    private void setUpButton() {
        mButton = (OngoingButton) mRootView
                .findViewById(R.id.brampingButton);
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

    private void setUpIterations() {
        mNumericInput = (NumericView) mRootView.findViewById(R.id.brampIterations);
        mNumericInput.setUpdateListener(this);
        mNumericInput.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mNumericInput.getState() == TimerView.State.UN_SELECTED) {
                            mInputListener.onInputSelected(mNumericInput);
                        } else {
                            mInputListener.onInputDeSelected();
                        }
                        break;
                }
                return true;
            }
        });


        Bundle fragmentState = getArguments();
        if (fragmentState != null) {
            //TODO restore state for rotation

        } else {
            //Restore state of Bramping from persistent storage
            mInterval = TTApp.getInstance(getActivity()).getBrampingInterval();
            mIterations = TTApp.getInstance(getActivity()).getBrampingIterations();
            mStartExposure = TTApp.getInstance(getActivity()).getBrampingStartExposure();
            mEndExposure = TTApp.getInstance(getActivity()).getBrampingStartExposure();

        }
        mNumericInput.initValue(mIterations);

    }

    private void setUpStartExposureSetting(final AbstractWheel exposure) {
        exposure.addScrollingListener(new OnWheelScrollListener() {
            public void onScrollingStarted(AbstractWheel wheel) {
                mScrolling = true;
            }

            public void onScrollingFinished(AbstractWheel wheel) {
                mScrolling = false;
                Log.d(TAG, "New shutter speed: " + mShutterSpeedValues[exposure.getCurrentItem()]);
                mStartExposure = mShutterSpeedValues[exposure.getCurrentItem()];
                if (mAutomaticAdjustStart) {
                    mAutomaticAdjustStart = false;
                    String[] validShutterSpeeds = getValidShutterSpeeds();
//	            		   Log.d(TAG, "Valid Shutter speeds are: ");
//	            		     
//	            		     for( String speed: validShutterSpeeds) {
//	            		    	 Log.d(TAG, speed);
//	            		     }
                    ArrayWheelAdapter<String> exposureAdapter = new ArrayWheelAdapter<String>(
                            getActivity(), validShutterSpeeds);

                    exposureAdapter.setItemResource(R.layout.wheel_text_centered);
                    exposureAdapter.setItemTextResource(R.id.text);
                    mBrampStart.setViewAdapter(exposureAdapter);
                }
            }
        });

        exposure.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mInputListener != null) {
                    mInputListener.onInputDeSelected();
                }
                return false;
            }
        });

        mStartExposure = TTApp.getInstance(getActivity()).getBrampingStartExposure();
        Log.d(TAG, "Getting start exposure of:" + mStartExposure);

        int i = 0;
        for (int speed : mShutterSpeedValues) {
            if (speed == mStartExposure) {
                break;
            }
            i++;
        }
        exposure.setCurrentItem(i);
    }

    private void setUpEndExposureSetting(final AbstractWheel exposure) {
        exposure.addScrollingListener(new OnWheelScrollListener() {
            public void onScrollingStarted(AbstractWheel wheel) {
                mScrolling = true;
            }

            public void onScrollingFinished(AbstractWheel wheel) {
                mScrolling = false;
                Log.d(TAG, "New shutter speed: " + mShutterSpeedValues[exposure.getCurrentItem()]);
                mEndExposure = mShutterSpeedValues[exposure.getCurrentItem()];
                if (mAutomaticAdjustEnd) {
                    mAutomaticAdjustEnd = false;
                    String[] validShutterSpeeds = getValidShutterSpeeds();
                    ArrayWheelAdapter<String> exposureAdapter = new ArrayWheelAdapter<String>(
                            getActivity(), validShutterSpeeds);

                    exposureAdapter.setItemResource(R.layout.wheel_text_centered);
                    exposureAdapter.setItemTextResource(R.id.text);
                    mBrampEnd.setViewAdapter(exposureAdapter);
                }
            }
        });

        exposure.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mInputListener != null) {
                    mInputListener.onInputDeSelected();
                }
                return false;
            }
        });
        mEndExposure = TTApp.getInstance(getActivity()).getBrampingEndExposure();
        Log.d(TAG, "Getting end exposure of:" + mEndExposure);

        int i = 0;
        for (int speed : mShutterSpeedValues) {
            if (speed == mEndExposure) {
                break;
            }
            i++;
        }
        exposure.setCurrentItem(i);
    }


    private void setMaxExposure(final AbstractWheel exposure, long interval) {
        int i = 0;
        for (int speed : mShutterSpeedValues) {
            if (speed >= interval) {
                Log.d(TAG, "Max speed:" + speed);
                break;
            }
            i++;
        }
        exposure.setCurrentItem((i - 1), true);
        Log.d(TAG, "Current Max value is: " + mShutterSpeedValues[exposure.getCurrentItem()]);
    }

    private String[] getValidShutterSpeeds() {
        String[] validShutterSpeeds = null;
        long interval = mExposureTimeInput.getTime();
        int i = 0;
        for (int speed : mShutterSpeedValues) {
            if (speed >= interval) {
                break;
            }
            i++;
        }
        validShutterSpeeds = new String[i];
        i = 0;
        for (String validSpeed : validShutterSpeeds) {
            validShutterSpeeds[i] = mShutterSpeeds[i];
            i++;
        }


        return validShutterSpeeds;
    }

    private void setValidShutterSpeeds() {
        String[] validShutterSpeeds = getValidShutterSpeeds();
        ArrayWheelAdapter<String> exposureAdapter = new ArrayWheelAdapter<String>(
                getActivity(), validShutterSpeeds);

        exposureAdapter.setItemResource(R.layout.wheel_text_centered);
        exposureAdapter.setItemTextResource(R.id.text);
        mBrampStart.setViewAdapter(exposureAdapter);
        mBrampEnd.setViewAdapter(exposureAdapter);
    }

    private void setUpTimeInputs() {
        mExposureTime = (View) mRootView.findViewById(R.id.brampExposure);

        mExposureTimeInput = (TimerView) mExposureTime.findViewById(R.id.timerTimeText);
        mExposureTimeInput.setUpdateListener(this);
        mExposureTime.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mExposureTimeInput.getState() == TimerView.State.UN_SELECTED) {
                            mInputListener.onInputSelected(mExposureTimeInput);
                        } else {
                            mInputListener.onInputDeSelected();
                        }
                        break;
                }
                return true;
            }
        });


        mExposureTimeInput.setTextInputTime(mInterval);
        mExposureTimeInput.initInputs(mInterval);
    }

    private void setKeyBoardSize() {
        mButtonContainer = mRootView.findViewById(R.id.buttonContainer);
        final ViewTreeObserver vto = mRootView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int buttonContainerHeight = mButtonContainer.getHeight();
                int buttonContainerWidth = mButtonContainer.getWidth();

                Log.d(TAG, "Button container height is: "
                        + buttonContainerHeight);
                mInputListener.inputSetSize(buttonContainerHeight, buttonContainerWidth);
                ViewTreeObserver obs = mRootView.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);

            }
        });
    }

    private void setUpCircleTimer() {
        Log.d(TAG, "Setting up circle timer");
        mCountDownLayout = mRootView.findViewById(R.id.circularTimer);
        mCircleTimerView = (CircleTimerView) mRootView.findViewById(R.id.circleTimer);
        mTimerText = (CountingTimerView) mRootView.findViewById(R.id.countingTimeText);
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

    private void showCountDown() {
        mCountDownLayout.setVisibility(View.VISIBLE);
    }

    private void hideCountDown() {
        mCountDownLayout.setVisibility(View.GONE);
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
            if (mButton != null) {
                hideCountDown();
                mButton.stopAnimation();
            }
        }
    }

    public void onPulseStarted(int currentCount, int totalCount, long timeToNext, long totalTimeMaining) {
        String sequenceProgress = currentCount + "/" + totalCount;
        mSequenceCountText.setText(sequenceProgress);
    }

    public void onPulseUpdate(long[] sequence, int exposures, long timeToNext,
                              long remainingPulseTime, long remainingSequenceTime) {

        mCurrentExposureCount = exposures;

        //Log.d(TAG,"Pulse update exposures: " +exposures);
        int currentSeq = exposures - 1;
        long currentExposure = sequence[currentSeq * 2];
        long currentGap = sequence[(currentSeq * 2) + 1];
        long remainingExposure = 0;
        long remainingGap = 0;
        if (remainingPulseTime > currentGap) {
            //We need to count down the exposure
            remainingExposure = currentExposure - (timeToNext - remainingPulseTime);
            mExposureTimerText.setTime(remainingExposure, true, true);
            if ((remainingGap == 0) && ((currentSeq * 2) + 1) <= sequence.length) {
                long nextGap = sequence[(currentSeq * 2) + 1];
                mGapTimerText.setTime(nextGap, true, true);
            } else if (((currentSeq * 2) + 1) > sequence.length) {
                mGapTimerText.setTime(0, true, true);
            }
        } else {
            //We need to count down the gap
            remainingGap = remainingPulseTime;
            mGapTimerText.setTime(remainingGap, true, true);
            if ((remainingExposure == 0) && ((currentSeq * 2) + 2) < sequence.length) {
                long nextExposure = sequence[(currentSeq * 2) + 2];
                mExposureTimerText.setTime(nextExposure, true, true);
            } else if (((currentSeq * 2) + 2) >= sequence.length) {
                mExposureTimerText.setTime(0, true, true);
            }

        }

        mTimerText.setTime(remainingSequenceTime, true, true);
        if (remainingPulseTime != 0) {
            if (mSyncCircle) {
                String sequenceProgress = exposures + "/" + (sequence.length / 2);
                mSequenceCountText.setText(sequenceProgress);
                synchroniseCircle(remainingSequenceTime, sequence);
            }
        }
    }

    private void synchroniseCircle(long remainingTime, long[] sequence) {
        long totaltime = PulseGenerator.getSequenceTime(sequence);
        Log.d(TAG, "Sequence time: " + totaltime);
        Log.d(TAG, "Remaining time: " + remainingTime);
        mCircleTimerView.setIntervalTime(totaltime);
        mCircleTimerView.setPassedTime((totaltime - remainingTime), true);
        mCircleTimerView.startIntervalAnimation();
        mSyncCircle = false;
    }

    public void onPulseStop() {
        mTimerText.setTime(0, true, true);
        onStopTimer();
    }

    @Override
    public void onInputUpdated() {
        Log.d(TAG, "Input has been updated");
        long interval = mExposureTimeInput.getTime();
        mDuration = mNumericInput.getValue() * interval;
        Log.d(TAG, "Duration is now: " + mDuration + " interval is:" + interval);

        mDurationText.setText(formatTime(mDuration));

        if (mStartExposure >= interval) {
            mAutomaticAdjustStart = true;
            setMaxExposure(mBrampStart, interval);
        } else {
            String[] validShutterSpeeds = getValidShutterSpeeds();
            ArrayWheelAdapter<String> exposureAdapter = new ArrayWheelAdapter<String>(
                    getActivity(), validShutterSpeeds);

            exposureAdapter.setItemResource(R.layout.wheel_text_centered);
            exposureAdapter.setItemTextResource(R.id.text);
            mBrampStart.setViewAdapter(exposureAdapter);
        }

        if (mEndExposure >= interval) {
            mAutomaticAdjustEnd = true;
            setMaxExposure(mBrampEnd, interval);
        } else {
            String[] validShutterSpeeds = getValidShutterSpeeds();
            ArrayWheelAdapter<String> exposureAdapter = new ArrayWheelAdapter<String>(
                    getActivity(), validShutterSpeeds);

            exposureAdapter.setItemResource(R.layout.wheel_text_centered);
            exposureAdapter.setItemTextResource(R.id.text);
            mBrampEnd.setViewAdapter(exposureAdapter);
        }

    }

    private String formatTime(long time) {
        int hundreds, seconds, minutes, hours;
        seconds = (int) time / 1000;
//		hundreds = (int) (time - seconds * 1000) / 10;
        minutes = (int) seconds / 60;
        seconds = (int) seconds - minutes * 60;
        hours = (int) minutes / 60;
        minutes = (int) minutes - hours * 60;
        StringBuilder formattedTime = new StringBuilder().append(hours)
                .append("h ")
                .append(String.format("%02d", minutes))
                .append("m ")
                .append(String.format("%02d", seconds))
                .append("s")
//				.append(String.format("%02d", hundreds))
                ;

        return formattedTime.toString();

    }

    public int getCurrentExposureCount() {
        return mCurrentExposureCount;
    }

}
