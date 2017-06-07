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

import com.triggertrap.R;
import com.triggertrap.TTApp;
import com.triggertrap.util.DialpadManager;
import com.triggertrap.view.CircleTimerView;
import com.triggertrap.view.CountingTimerView;
import com.triggertrap.widget.OngoingButton;
import com.triggertrap.widget.TimerView;

public class TimedFragment extends TriggertrapFragment {

    private static final String TAG = TimedFragment.class.getSimpleName();
    private View mRootView;
    private TimerView mTimeView;
    private OngoingButton mButton;
    private View mButtonContainer;

    private View mCountDownLayout;
    private View mTimedInputView;
    private CountingTimerView mTimerText;
    private CircleTimerView mCircleTimerView;

    private boolean syncCircle = false;
    private long mInitialTime = 0;

    private long mTimerDuration;


    private Animation mSlideInFromTop;
    private Animation mSlideOutToTop;


    private TimedListener mListener = null;
    private DialpadManager.InputSelectionListener mInputListener = null;


    public interface TimedListener {
        public void onTimedStarted(long time);

        public void onTimedStopped();
    }

    public TimedFragment() {
        mRunningAction = TTApp.OnGoingAction.TIMED;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (TimedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement TimedListener");
        }

        try {
            mInputListener = (DialpadManager.InputSelectionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialpadManager.InputSelectionListener");
        }


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        mRootView = inflater.inflate(R.layout.timed, container, false);
        TextView title = (TextView) mRootView.findViewById(R.id.timedText);
        title.setTypeface(SAN_SERIF_LIGHT);
        mTimedInputView = mRootView.findViewById(R.id.timedInputView);
        mTimedInputView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInputListener != null) {
                    mInputListener.onInputDeSelected();
                    //Set the text on the input just in case the user entered something weird like 88 mins.
                    mTimeView.setTextInputTime(mTimeView.getTime());

                }

            }
        });

        setUpTimePicker();
        setUpAnimations();
        setUpCircleTimer();
        setUpButton();
        setKeyBoardSize();
        resetVolumeWarning();
        return mRootView;
    }


    @Override
    public void onStop() {
        super.onStop();
        //Persist the state of the timed mode time setting
        TTApp.getInstance(getActivity()).setTimedModeTime(mTimeView.getTime());
    }


    private void setUpButton() {
        mButton = (OngoingButton) mRootView
                .findViewById(R.id.timedButton);
        mButton.setToggleListener(new OngoingButton.OnToggleListener() {

            @Override
            public void onToggleOn() {
                Log.d(TAG, "onToggleON");
                //showCountDown();
                if (mListener != null) {
                    if (mTimeView.getTime() != 0) {
                        mListener.onTimedStarted(mTimeView.getTime());
                        checkVolume();
                    } else {
                        mButton.stopAnimation();
                    }
                }

            }

            @Override
            public void onToggleOff() {
                Log.d(TAG, "onToggleOff");
                //hideCountDown();
                if (mListener != null) {
                    mListener.onTimedStopped();
                }

            }
        });
    }

    private void setUpCircleTimer() {
        Log.d(TAG, "Setting up circle timer");
        mCountDownLayout = mRootView.findViewById(R.id.circularTimer);
        mCircleTimerView = (CircleTimerView) mRootView.findViewById(R.id.circleTimer);
        mTimerText = (CountingTimerView) mRootView.findViewById(R.id.countingTimeText);
        mCircleTimerView.setTimerMode(true);
    }

    public void startTimer(long time) {
        mCountDownLayout.setVisibility(View.VISIBLE);
        mCountDownLayout.startAnimation(mSlideInFromTop);
        mCircleTimerView.setPassedTime(0, false);
        mCircleTimerView.setIntervalTime(time);
        mTimerText.setTime(time, true, false);
        mCircleTimerView.startIntervalAnimation();
        mState = State.STARTED;
        mTimerDuration = time;
    }

    public void stopTimer() {
        //mTimerText.setTime(0, true, true);
        mCircleTimerView.abortIntervalAnimation();
        mCountDownLayout.startAnimation(mSlideOutToTop);
        mCountDownLayout.setVisibility(View.GONE);
        mButton.stopAnimation();
        mState = State.STOPPED;
    }

    public void updateTimer(long time) {
        mTimerText.setTime(time, true, true);
        if (syncCircle) {
            synchroniseCircle(time);
        }

    }

    private void showCountDown() {
        if (mCountDownLayout != null) {
            mCountDownLayout.setVisibility(View.VISIBLE);
        }

    }

    private void hideCountDown() {
        if(mCountDownLayout != null) {
            mCountDownLayout.setVisibility(View.GONE);
        }
    }

    private void setUpTimePicker() {
        mTimeView = (TimerView) mRootView.findViewById(R.id.timerTimeText);
        mTimeView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mInputListener.onInputSelected(mTimeView);
                        break;
                }
                return true;
            }
        });

        Bundle fragmentState = getArguments();
        if (fragmentState != null) {
            //TODO restore state for rotation

        } else {
            //Restore state of time lapse from persistent storage
            mInitialTime = TTApp.getInstance(getActivity()).getTimedModeTime();
            Log.d(TAG, "Initial Time: " + mInitialTime);
        }
        mTimeView.setTextInputTime(mInitialTime);
        mTimeView.initInputs(mInitialTime);

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

    private void synchroniseCircle(long remainingTime) {
        Log.d(TAG, "Interval time: " + mTimeView.getTime());
        Log.d(TAG, "Remaining time: " + remainingTime);
        mCircleTimerView.setIntervalTime(mTimeView.getTime());
        mCircleTimerView.setPassedTime((mTimeView.getTime() - remainingTime), true);
        mCircleTimerView.startIntervalAnimation();
        syncCircle = false;
    }

    public long getTimerDuration() {
        return mTimerDuration;
    }


}
