package com.triggertrap.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.triggertrap.R;
import com.triggertrap.TTApp;
import com.triggertrap.view.CircleTimerView;
import com.triggertrap.view.CountingTimerView;
import com.triggertrap.widget.OngoingButton;

public class StartStopFragment extends TriggertrapFragment {

    private static final String TAG = StartStopFragment.class.getSimpleName();
    private static final int TIME_INTERVAL = 1000;
    private View mRootView;
    private View mCountDownLayout;
    private OngoingButton mButton;
    private CountingTimerView mTimerText;
    private CircleTimerView mCircleTimerView;
    private boolean syncCircle = false;
    private Animation mSlideInFromTop;
    private Animation mSlideOutToTop;

    private long mLastSetTime;

    private StartStopListener mListener = null;

    public interface StartStopListener {
        public void onStopwatchStarted();

        public void onStopwatchStopped();
    }

    public StartStopFragment() {
        mRunningAction = TTApp.OnGoingAction.PRESS_START_STOP;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (StartStopListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement StartStopListener");
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.start_stop_mode, container,
                false);
        TextView title = (TextView) mRootView.findViewById(R.id.startStopText);
        title.setTypeface(SAN_SERIF_LIGHT);
        mButton = (OngoingButton) mRootView
                .findViewById(R.id.pressStartButton);

        mSlideInFromTop = AnimationUtils.loadAnimation(getActivity(),
                R.anim.slide_in_from_top);
        mSlideOutToTop = AnimationUtils.loadAnimation(getActivity(),
                R.anim.slide_out_to_top);

        mButton.setToggleListener(new OngoingButton.OnToggleListener() {

            @Override
            public void onToggleOn() {
                Log.d(TAG, "onToggleON");
                //mCountText.setText(String.valueOf(0));
                //showCountDown()
                if (mListener != null) {
                    mListener.onStopwatchStarted();
                    checkVolume();
                }

            }

            @Override
            public void onToggleOff() {
                Log.d(TAG, "onToggleOff");
                //hideCountDown();
                if (mListener != null) {
                    mListener.onStopwatchStopped();
                }

            }
        });

        setUpCircleTimer();
        resetVolumeWarning();
        return mRootView;
    }

    private void setUpCircleTimer() {
        Log.d(TAG, "Setting up circle timer");
        mCountDownLayout = mRootView.findViewById(R.id.circularTimer);
        mCircleTimerView = (CircleTimerView) mRootView.findViewById(R.id.circleTimer);
        mTimerText = (CountingTimerView) mRootView.findViewById(R.id.countingTimeText);
        mCircleTimerView.setTimerMode(false);
    }


    public void startStopwatch() {
        mCountDownLayout.setVisibility(View.VISIBLE);
        mCountDownLayout.startAnimation(mSlideInFromTop);
        mCircleTimerView.setIntervalTime(TIME_INTERVAL);
        mCircleTimerView.setPassedTime(0, false);
        mTimerText.setTime(0, true, false);
        mCircleTimerView.startIntervalAnimation();
        mState = State.STARTED;

    }

    public void updateStopwatch(long time) {
        mTimerText.setTime(time, true, true);
        mLastSetTime = time;
        if (syncCircle) {
            synchroniseCircle(time);
        }
    }

    public void stopStopwatch() {
        if(mCircleTimerView != null) {
            mCircleTimerView.abortIntervalAnimation();
            mCountDownLayout.startAnimation(mSlideOutToTop);
            mCountDownLayout.setVisibility(View.GONE);
            mButton.stopAnimation();
            mState = State.STOPPED;
        }
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
            syncCircle = true;
            showCountDown();
            mButton.startAnimation();
        } else {
            if (mButton != null) {
                hideCountDown();
                mButton.stopAnimation();
            }
        }
    }

    private void synchroniseCircle(long passedTime) {
        mCircleTimerView.setIntervalTime(TIME_INTERVAL);
        mCircleTimerView.setPassedTime(passedTime, true);
        mCircleTimerView.startIntervalAnimation();
        syncCircle = false;
    }

    public long getLastTimeSet() {
        return mLastSetTime;
    }
}

