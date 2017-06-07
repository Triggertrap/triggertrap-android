package com.triggertrap.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.triggertrap.R;
import com.triggertrap.view.CircleTimerView;
import com.triggertrap.view.CountingTimerView;
import com.triggertrap.widget.OngoingButton;

/**
 * Created by scottmellors on 20/08/2014.
 */
public class QuickReleaseFragment extends TriggertrapFragment {

    private static final int TIME_INTERVAL = 1000;
    private View mRootView;
    private View mCountDownLayout;
    private OngoingButton mButton;
    private CountingTimerView mTimerText;
    private CircleTimerView mCircleTimerView;
    private Animation mSlideInFromTop;
    private Animation mSlideOutToTop;

    private QuickReleaseListener mListener = null;

    public interface QuickReleaseListener {
        public void onQuickPressStarted();

        public void onQuickPressStopped();
    }

    public QuickReleaseFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (QuickReleaseListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement QuickReleaseListener");
        }
    }

    @Override
    public void onDetach() {

        super.onDetach();
        if (mState == State.STARTED) {
            mListener.onQuickPressStopped();
            mState = State.STOPPED;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.quick_release_mode, container, false);
        mButton = (OngoingButton) mRootView.findViewById(R.id.quickReleaseButton);
        TextView title = (TextView) mRootView.findViewById(R.id.quickReleaseText);
        title.setTypeface(SAN_SERIF_LIGHT);

        mRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if (mListener != null) {
                        if (mState == State.STARTED) {
                            mListener.onQuickPressStopped();
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        mSlideInFromTop = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_from_top);
        mSlideOutToTop = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_to_top);

        mButton.setTouchListener(new OngoingButton.OnTouchListener() {
            @Override
            public void onTouchUp() {
                if (mListener != null) {
                    if (mState == State.STARTED) {
                        mListener.onQuickPressStopped();
                    }
                }
            }

            @Override
            public void onTouchDown() {
                if (mListener != null) {
                    mListener.onQuickPressStarted();
                    checkVolume();
                }
            }
        });
        setUpCircleTimer();
        resetVolumeWarning();
        return mRootView;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void setUpCircleTimer() {

        mCountDownLayout = mRootView.findViewById(R.id.circularTimer);
        mCircleTimerView = (CircleTimerView) mRootView.findViewById(R.id.circleTimer);
        mTimerText = (CountingTimerView) mRootView.findViewById(R.id.countingTimeText);
        mCircleTimerView.setTimerMode(false);
    }

    protected void hideCountDown() {
        mCountDownLayout.setVisibility(View.GONE);

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
    }

    public void stopStopwatch() {
        mCircleTimerView.abortIntervalAnimation();
        mCountDownLayout.startAnimation(mSlideOutToTop);
        mCountDownLayout.setVisibility(View.GONE);
        mButton.stopAnimation();
        mState = State.STOPPED;
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
        if (mState != State.STARTED) {
            hideCountDown();
            mButton.stopAnimation();
        }
    }
}
