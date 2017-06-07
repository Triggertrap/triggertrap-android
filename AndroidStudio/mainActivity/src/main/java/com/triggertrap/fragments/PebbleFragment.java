package com.triggertrap.fragments;

import java.util.UUID;

import android.animation.Animator;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;
import com.triggertrap.R;
import com.triggertrap.TTApp;
import com.triggertrap.outputs.OutputDispatcher;
import com.triggertrap.widget.OngoingButton;

public class PebbleFragment extends TriggertrapFragment {

    private static final String TAG = PebbleFragment.class.getSimpleName();
    private PebbleListener mlistener;
    private OngoingButton mButton;
    private View mRootView;
    private TextView mTriggerText;
    private LinearLayout mSlaveInfoView;
    OutputDispatcher mOutputDispatcher;

    private Animation mSlideInFromTop;
    private Animation mSlideOutToTop;

    // Pebble stuff
    private PebbleKit.PebbleDataReceiver dataReceiver;
    private String appURLStr = "http://tri.gg/tt-pebble.pbw";
    private Uri appURI = Uri.parse(appURLStr);

    private final static UUID PEBBLE_APP_UUID = UUID
            .fromString("96439271-2DE4-4209-A68C-5571CF2C418E");
    private final static int CMD_KEY = 0x01;
    private final static int CMD_DOWN = 0x01;

    private Handler mHandler = new Handler();

    public interface PebbleListener {
        public void onStartPebbleApp();

        public void onStopPebbleApp();
    }

    public PebbleFragment() {
        mRunningAction = TTApp.OnGoingAction.PEBBLE;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mlistener = (PebbleListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PebbleListener");
        }

    }

    ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOutputDispatcher = new OutputDispatcher(null, getActivity());
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");
        mRootView = inflater.inflate(R.layout.pebble, container, false);
        // mMasterListLayout = (LinearLayout) mRootView
        // .findViewById(R.id.wifi_slave_list);
        //
        mSlaveInfoView = (LinearLayout) mRootView
                .findViewById(R.id.slaceInfoView);
        mTriggerText = (TextView) mRootView.findViewById(R.id.triggerText);

        Bundle fragmentState = getArguments();
        if (fragmentState != null) {
            String tag = fragmentState
                    .getString(TriggertrapFragment.BundleKey.FRAGMENT_TAG);
            // Is this bundle for this Fragment?
            if (tag.equals(getTag())) {
                boolean isActive = fragmentState.getBoolean(
                        TriggertrapFragment.BundleKey.IS_ACTION_ACTIVE, false);
                if (isActive) {
                    mState = State.STARTED;
                } else {
                    mState = State.STOPPED;
                }

            }

        } else {

        }

        setUpAnimations();
        setUpButton();

        return mRootView;
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.pebble_menu, menu);
    }

    private void setUpAnimations() {
        mSlideInFromTop = AnimationUtils.loadAnimation(getActivity(),
                R.anim.slide_in_from_top);
        mSlideOutToTop = AnimationUtils.loadAnimation(getActivity(),
                R.anim.slide_out_to_top);

    }

    private void setUpButton() {
        mButton = (OngoingButton) mRootView.findViewById(R.id.pebbleButton);
        mButton.setToggleListener(new OngoingButton.OnToggleListener() {

            @Override
            public void onToggleOn() {
                Log.d(TAG, "onToggleON");
                startPebble();
            }

            @Override
            public void onToggleOff() {
                Log.d(TAG, "onToggleOff");
                stopPebble();

            }
        });
    }

    private void startPebble() {
        mSlaveInfoView.startAnimation(mSlideOutToTop);
        mSlaveInfoView.setVisibility(View.GONE);
        mlistener.onStartPebbleApp();
    }

    private void stopPebble() {
        mSlaveInfoView.setVisibility(View.VISIBLE);
        mSlaveInfoView.startAnimation(mSlideInFromTop);
        mlistener.onStopPebbleApp();

    }

    public void onPebbleTrigger() {
        Log.d(TAG, "Trigger received from pebble");
        mTriggerText.setVisibility(View.VISIBLE);
        mTriggerText.setAlpha(0);
        mTriggerText.animate().alphaBy(1).setDuration(75).setListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                mTriggerText.animate().setListener(null).alpha(0).setDuration(75);

            }

            @Override
            public void onAnimationCancel(Animator arg0) {
                // TODO Auto-generated method stub

            }
        });
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
            mSlaveInfoView.setVisibility(View.GONE);
            mButton.startAnimation();
        } else {
            if (mButton != null) {
                mSlaveInfoView.setVisibility(View.VISIBLE);
                mButton.stopAnimation();
            }
        }
    }

}
