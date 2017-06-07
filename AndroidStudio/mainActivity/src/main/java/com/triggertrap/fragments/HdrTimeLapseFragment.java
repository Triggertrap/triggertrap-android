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
import com.triggertrap.widget.OngoingButton;
import com.triggertrap.widget.TimerView;

public class HdrTimeLapseFragment extends PulseSequenceFragment {

    private static final String TAG = HdrTimeLapseFragment.class.getSimpleName();
    private DialpadManager.InputSelectionListener mInputListener = null;

    private int[] mShutterSpeedValues;
    private float[] mEvValues = {0.33f, 0.5f, 1.0f, 2.0f};
    private boolean mScrolling = false;
    private long mCurrentMiddleSpeed = 60000;
    private float mCurrentEvValue = mEvValues[2];
    private int mCurrentNumExposures = 3;
    private int mCurrentExposureCount;

    private View mRootView;
    private OngoingButton mButton;
    private View mIntervalView;
    private TimerView mIntervalTimeInput;
    private View mButtonContainer;

    private View mCountDownLayout;
    private CircleTimerView mCircleTimerView;
    private CountingTimerView mTimerText;
    private SimpleTimerView mExposureTimerText;
    private SimpleTimerView mGapTimerText;
    private TextView mSequenceCountText;


    private Animation mSlideInFromTop;
    private Animation mSlideOutToTop;

    private boolean mSyncCircle = false;

    private long mInterval = 0;

    public HdrTimeLapseFragment() {
        mRunningAction = TTApp.OnGoingAction.HDR_TIMELAPSE;
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

        mRootView = inflater.inflate(R.layout.le_hdr_timelapse, container, false);


        mShutterSpeedValues = getResources().getIntArray(
                R.array.shutter_speed_values);
        final String[] shutterSpeeds = getResources().getStringArray(
                R.array.shutter_speeds);

        ArrayWheelAdapter<String> middleAdapter = new ArrayWheelAdapter<String>(
                getActivity(), shutterSpeeds);

        View middleExposure = mRootView.findViewById(R.id.hdrMiddleExposure);

        final AbstractWheel hdrMiddle = (AbstractWheel) middleExposure
                .findViewById(R.id.wheelHorizontalView);
        middleAdapter.setItemResource(R.layout.wheel_text_centered);
        middleAdapter.setItemTextResource(R.id.text);
        hdrMiddle.setViewAdapter(middleAdapter);

        ArrayWheelAdapter<String> evAdapter = new ArrayWheelAdapter<String>(
                getActivity(), new String[]{"1/3", "1/2", "1", "2"});
        evAdapter.setItemResource(R.layout.wheel_text_centered);
        evAdapter.setItemTextResource(R.id.text);
        View evSteps = mRootView.findViewById(R.id.hdrEvStep);
        final AbstractWheel hdrEv = (AbstractWheel) evSteps
                .findViewById(R.id.wheelHorizontalView);
        hdrEv.setViewAdapter(evAdapter);

        setUpMiddleExposure(hdrMiddle);
        setUpEvValues(hdrEv);
        setUpInterval();
        setKeyBoardSize();
        setUpButton();
        setUpCircleTimer();
        setUpAnimations();
        resetVolumeWarning();
        return mRootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        //Persist the state of the star trail mode
        TTApp.getInstance(getActivity()).setHDRTimeLapseMiddleExposure(mCurrentMiddleSpeed);
        TTApp.getInstance(getActivity()).setHDRTimeLapseInterval(mIntervalTimeInput.getTime());
        TTApp.getInstance(getActivity()).setHDRTimeLapseEvStep(mCurrentEvValue);
    }

    private void setUpMiddleExposure(final AbstractWheel hdrMiddle) {
        hdrMiddle.addScrollingListener(new OnWheelScrollListener() {
            public void onScrollingStarted(AbstractWheel wheel) {
                mScrolling = true;
            }

            public void onScrollingFinished(AbstractWheel wheel) {
                mScrolling = false;
                Log.d(TAG, "New shutter speed: " + mShutterSpeedValues[hdrMiddle.getCurrentItem()]);
                mCurrentMiddleSpeed = mShutterSpeedValues[hdrMiddle.getCurrentItem()];
                checkInterval();
            }
        });

        mCurrentMiddleSpeed = TTApp.getInstance(getActivity()).getHDRTimeLapseMiddleExposure();
        Log.d(TAG, "Getting Middle exposure of:" + mCurrentMiddleSpeed);
        Log.d(TAG, "Current middle exposure index:" + hdrMiddle.getCurrentItem());
        int i = 0;
        for (int speed : mShutterSpeedValues) {
            if (speed == mCurrentMiddleSpeed) {
                break;
            }
            i++;
        }
        hdrMiddle.setCurrentItem(i);
    }


    private void setUpEvValues(final AbstractWheel hdrEv) {
        hdrEv.addScrollingListener(new OnWheelScrollListener() {
            public void onScrollingStarted(AbstractWheel wheel) {
                mScrolling = true;
            }

            public void onScrollingFinished(AbstractWheel wheel) {
                mScrolling = false;
                mCurrentEvValue = mEvValues[hdrEv.getCurrentItem()];
                checkInterval();
            }
        });

        mCurrentEvValue = TTApp.getInstance(getActivity()).getHDRTimeLapseEvStep();
        int i = 0;
        for (float evValue : mEvValues) {
            if (evValue == mCurrentEvValue) {
                break;
            }
            i++;
        }
        hdrEv.setCurrentItem(i);

    }

    public void checkInterval() {
        long[] sequence = mPulseGenerator.getHdrSequence(mCurrentMiddleSpeed, mCurrentNumExposures, mCurrentEvValue, 0);
        long totaltime = PulseGenerator.getSequenceTime(sequence);
        Log.d(TAG, "Total time for sequence is: " + totaltime);
        if(mIntervalTimeInput != null) {
            Log.d(TAG, "Interval time is: " + mIntervalTimeInput.getTime());
            if (mIntervalTimeInput.getTime() < totaltime) {
                mIntervalTimeInput.setTextInputTime(totaltime);
                mIntervalTimeInput.initInputs(totaltime);
            }
        }

    }

    private void setUpInterval() {
        mIntervalView = (View) mRootView.findViewById(R.id.intervalInput);

        mIntervalTimeInput = (TimerView) mIntervalView.findViewById(R.id.timerTimeText);

        mIntervalView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mIntervalTimeInput.getState() == TimerView.State.UN_SELECTED) {
                            mInputListener.onInputSelected(mIntervalTimeInput);
                        } else {
                            mInputListener.onInputDeSelected();
                            checkInterval();
                        }
                        break;
                }
                return true;
            }
        });

        mInterval = TTApp.getInstance(getActivity()).getHDRTimeLapseInterval();

        mIntervalTimeInput.setTextInputTime(mInterval);
        mIntervalTimeInput.initInputs(mInterval);
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

    private void showCountDown() {
        mCountDownLayout.setVisibility(View.VISIBLE);

    }

    private void hideCountDown() {
        if (mCountDownLayout != null) {
            mCountDownLayout.setVisibility(View.GONE);
        }
    }

    public void onPulseStarted(int sequenceInteration, int totalCount, long timeToNext, long totalTimeMaining) {
        String sequenceProgress = "" + sequenceInteration;
        //Log.d(TAG ,"Pulse started Sequence count: " + sequenceProgress);
        mSequenceCountText.setText(sequenceProgress);
    }


    public void onPulseUpdate(long[] sequence, int exposures, long timeToNext,
                              long remainingPulseTime, long remainingSequenceTime) {
//		Log.d(TAG,"Pulse update exposures: " +exposures);
//		Log.d(TAG,"Remaining sequence time: " + remainingSequenceTime);
//		
        mCurrentExposureCount = exposures;

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
                //String sequenceProgress = exposures + "/" + (sequence.length/2);
                //mSequenceCountText.setText(sequenceProgress);
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

    public void onPulseSequenceIterated(long[] sequence) {
        long totaltime = PulseGenerator.getSequenceTime(sequence);
        Log.d(TAG, "Iterration ...Total time Circle: " + totaltime);
        mCircleTimerView.setIntervalTime(totaltime);

        //mExposureTimerText.setTime(sequence[0], true, true);
        //mGapTimerText.setTime(sequence[1], true, true);
        mCircleTimerView.setPassedTime(0, true);
        mCircleTimerView.startIntervalAnimation();
        mTimerText.setTime(totaltime, true, false);
    }

    private void onStartTimer() {

        if (mState == State.STOPPED) {

            mState = State.STARTED;
            mCountDownLayout.setVisibility(View.VISIBLE);
            mCountDownLayout.startAnimation(mSlideInFromTop);
            mCircleTimerView.setPassedTime(0, false);

            mPulseSequence = mPulseGenerator.getHdrSequence(mCurrentMiddleSpeed, mCurrentNumExposures, mCurrentEvValue, mIntervalTimeInput.getTime());
            mPulseSeqListener.onPulseSequenceCreated(TTApp.OnGoingAction.HDR_TIMELAPSE, mPulseSequence, true);
            //logSequence(mPulseSequence);

            long totaltime = PulseGenerator.getSequenceTime(mPulseSequence);
            Log.d(TAG, "Total time Circle: " + totaltime);
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

    public int getCompletedExposures() {
        return mCurrentExposureCount;
    }


}
