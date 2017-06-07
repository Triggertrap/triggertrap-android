package com.triggertrap.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.triggertrap.R;
import com.triggertrap.util.DialpadManager;
import com.triggertrap.view.CircleTimerView;
import com.triggertrap.view.CountingTimerView;
import com.triggertrap.widget.TimerView;

public class TimeFragment extends PulseSequenceFragment {

    private static final String TAG = TimeFragment.class.getSimpleName();

    private View mRootView;
    protected TimerView mTimeView;
    protected ImageView mDelete;
    protected View mDialPad;
    protected View mCountDownLayout;
    protected View mTimedInputView;
    protected long mInitialTime = 0;
    protected boolean syncCircle = false;

    protected CircleTimerView mCircleTimerView;
    protected CountingTimerView mTimerText;

    protected Animation mSlideInFromTop;
    protected Animation mSlideOutToTop;


    protected DialpadManager.InputSelectionListener mListener = null;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (DialpadManager.InputSelectionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialpadManager.InputSelectionListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        mRootView = inflater.inflate(R.layout.timelapse, container, false);

        mTimedInputView = mRootView.findViewById(R.id.timeLapseInputView);
        mTimedInputView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onInputDeSelected();
                    //Set the text on the input just in case the user entered something weird like 88 mins.
                    mTimeView.setTextInputTime(mTimeView.getTime());

                }

            }
        });

        setUpTimePicker();
        setUpAnimations();
        setUpCircleTimer();

        return mRootView;

    }


    protected void showCountDown() {
        if (mCountDownLayout != null) {
            mCountDownLayout.setVisibility(View.VISIBLE);
        }
    }

    protected void hideCountDown() {
        if (mCountDownLayout != null) {
            mCountDownLayout.setVisibility(View.GONE);
        }
    }


    private void setUpTimePicker() {
        mTimeView = (TimerView) mRootView.findViewById(R.id.timerTimeText);
        mDelete = (ImageButton) mRootView.findViewById(R.id.delete);
//		updateDeleteButton();
//		mDelete.setOnClickListener(this);
//		mDelete.setOnLongClickListener(this);
        //Hide the delete button
        mDelete.setVisibility(View.GONE);

        mTimeView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mListener.onInputSelected(mTimeView);
                        break;
                }
                return true;
            }
        });


        mTimeView.setTextInputTime(mInitialTime);
        mTimeView.initInputs(mInitialTime);
//		updateDeleteButton();
    }

    private void setUpAnimations() {
        mSlideInFromTop = AnimationUtils.loadAnimation(getActivity(),
                R.anim.slide_in_from_top);
        mSlideOutToTop = AnimationUtils.loadAnimation(getActivity(),
                R.anim.slide_out_to_top);

    }

    private void setUpCircleTimer() {
        Log.d(TAG, "Setting up circle timer");
        mCountDownLayout = mRootView.findViewById(R.id.circularTimer);
        mCircleTimerView = (CircleTimerView) mRootView.findViewById(R.id.circleTimer);
        mTimerText = (CountingTimerView) mRootView.findViewById(R.id.countingTimeText);
        mCircleTimerView.setTimerMode(true);
    }


    protected void synchroniseCircle(long remainingTime) {
        Log.d(TAG, "Schronising circle with time: " + mTimeView.getTime() + " Remaining time: " + remainingTime);
        mCircleTimerView.setIntervalTime(mTimeView.getTime());
        mCircleTimerView.setPassedTime((mTimeView.getTime() - remainingTime), true);
        mCircleTimerView.startIntervalAnimation();
        syncCircle = false;
    }
}
