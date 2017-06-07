package com.triggertrap.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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
import com.triggertrap.fragments.dialog.EnableGPSDialog;
import com.triggertrap.fragments.preference.SettingsFragment;
import com.triggertrap.util.DialpadManager;
import com.triggertrap.view.ArcProgress;
import com.triggertrap.widget.NumericView;
import com.triggertrap.widget.OngoingButton;
import com.triggertrap.widget.TimerView;

public class DistanceLapseFragment extends TriggertrapFragment {

    private static final String TAG = DistanceLapseFragment.class.getSimpleName();
    private DialpadManager.InputSelectionListener mInputListener = null;

    private View mRootView;
    private OngoingButton mButton;
    private View mDistanceProgressLayout;
    private View mButtonContainer;
    private NumericView mNumericInput;
    private ArcProgress mProgressArc;
    private TextView mDistanceTraveledText;
    private TextView mDistanceRemainingText;
    private TextView mSpeed;
    private TextView mDistanceUnits;
    private TextView mSpeedUnits;

    private int mDistanceTrigger;
    private float mDistanceTraveled;
    private boolean mIngnoreGPS = false;

    private Animation mSlideInFromTop;
    private Animation mSlideOutToTop;

    private int mExposureCount = 0;

    private int mGooglePlayServiceState = GooglePlayServiceState.SERVICE_NOT_AVAILABLE;

    private DistanceLapseListener mDistanceListener;

    public interface DistanceLapseListener {
        public void onStartDistanceLapse(int distance);

        public void onStopDistanceLapse();

    }

    public interface GooglePlayServiceState {
        public static int SERVICE_AVAILABLE = 0;
        public static int SERVICE_NOT_AVAILABLE = 1;
    }

    //Handler for received Events from Settings Fragment
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            Log.d(TAG, "Received Broadcast.....");
            int type = intent.getIntExtra(SettingsFragment.SettingsEvent.EVENT_TYPE, 0);
            int value = intent.getIntExtra(SettingsFragment.SettingsEvent.EVENT_VALUE, 2);
            if (type == SettingsFragment.SettingsType.DISTANCE_UNIT) {
                if (value == SettingsFragment.DistanceUnits.METERS_KILOMETERS) {
                    mDistanceUnits.setText(getActivity().getResources().getString(R.string.meters));
                } else if (value == SettingsFragment.DistanceUnits.MILES_YARDS) {
                    mDistanceUnits.setText(getActivity().getResources().getString(R.string.yards));
                }
            } else if (type == SettingsFragment.SettingsType.DISTANCES_SPEED_UNT) {
                if (value == SettingsFragment.DistanceSpeedUnit.KILOMETERS_PER_HOUR) {
                    mSpeedUnits.setText(getActivity().getResources().getString(R.string.km_h));
                } else if (value == SettingsFragment.DistanceSpeedUnit.MILES_PER_HOUR) {
                    mSpeedUnits.setText(getActivity().getResources().getString(R.string.mph));
                } else if (value == SettingsFragment.DistanceSpeedUnit.METERS_PER_SECOND) {
                    mSpeedUnits.setText(getActivity().getResources().getString(R.string.m_s));
                }
            }

        }
    };

    public DistanceLapseFragment() {
        mRunningAction = TTApp.OnGoingAction.DISTANCE_LAPSE;
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

        try {
            mDistanceListener = (DistanceLapseListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DistanceLapseListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessageReceiver,
                new IntentFilter(SettingsFragment.SETTINGS_UPDATE_EVENT));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Creating View");
        mRootView = inflater.inflate(R.layout.distance_lapse, container, false);
        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInputListener != null) {
                    mInputListener.onInputDeSelected();
                    //Set the text on the input just in case the user entered something weird like 88 mins.
                    //mTimeView.setTextInputTime(mTimeView.getTime());

                }

            }
        });
        mDistanceProgressLayout = mRootView.findViewById(R.id.distanceProgress);
        mProgressArc = (ArcProgress) mRootView.findViewById(R.id.distanceProgressView);
        mDistanceTraveledText = (TextView) mRootView.findViewById(R.id.distanceCovered);
        mDistanceRemainingText = (TextView) mRootView.findViewById(R.id.remainingDistance);
        mSpeed = (TextView) mRootView.findViewById(R.id.speed);
        mDistanceUnits = (TextView) mRootView.findViewById(R.id.distanceText2);
        if (TTApp.getInstance(getActivity()).getDistlapseUnit() == SettingsFragment.DistanceUnits.MILES_YARDS) {
            mDistanceUnits.setText(getActivity().getResources().getString(R.string.yards));
        }
        mSpeedUnits = (TextView) mRootView.findViewById(R.id.speedUnits);
        if (TTApp.getInstance(getActivity()).getDistlapseSpeedUnit() == SettingsFragment.DistanceSpeedUnit.KILOMETERS_PER_HOUR) {
            mSpeedUnits.setText(getActivity().getResources().getString(R.string.km_h));
        } else if (TTApp.getInstance(getActivity()).getDistlapseSpeedUnit() == SettingsFragment.DistanceSpeedUnit.MILES_PER_HOUR) {
            mSpeedUnits.setText(getActivity().getResources().getString(R.string.mph));
        } else if (TTApp.getInstance(getActivity()).getDistlapseSpeedUnit() == SettingsFragment.DistanceSpeedUnit.METERS_PER_SECOND) {
            mSpeedUnits.setText(getActivity().getResources().getString(R.string.m_s));
        }

        setUpButton();
        setUpAnimations();
        setKeyBoardSize();
        setUpDistanceTrigger();
        resetVolumeWarning();
        return mRootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        //Persist the state of the distancelapse mode
        TTApp.getInstance(getActivity()).setDistanceLapseDistance(mNumericInput.getValue());
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setUpDistanceTrigger() {
        mNumericInput = (NumericView) mRootView.findViewById(R.id.distance);

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
            //Restore state of DistanceLapse from persistent storage
            mDistanceTrigger = TTApp.getInstance(getActivity()).getDistanceLapseDistance();

        }
        mNumericInput.initValue(mDistanceTrigger);
    }

    private void setUpButton() {
        mButton = (OngoingButton) mRootView
                .findViewById(R.id.distanceLapseButton);
        mButton.setToggleListener(new OngoingButton.OnToggleListener() {

            @Override
            public void onToggleOn() {
                Log.d(TAG, "onToggleON");
                onStartDistanceLapse();
                checkVolume();
            }

            @Override
            public void onToggleOff() {
                Log.d(TAG, "onToggleOff");
                onStopDistanceLapse();
            }
        });
    }

    private void setUpAnimations() {
        mSlideInFromTop = AnimationUtils.loadAnimation(getActivity(),
                R.anim.slide_in_from_top);
        mSlideOutToTop = AnimationUtils.loadAnimation(getActivity(),
                R.anim.slide_out_to_top);

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

    private void onStartDistanceLapse() {

        mExposureCount = 0;

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !mIngnoreGPS) {
            mButton.stopAnimation();
            Log.d(TAG, "GPS is disabled..");
            EnableGPSDialog gpsDialog = new EnableGPSDialog();
            gpsDialog.show(getActivity());
            return;
        }

        if (mState == State.STOPPED && mGooglePlayServiceState == GooglePlayServiceState.SERVICE_AVAILABLE) {
            mProgressArc.setProgress(0);
            mSpeed.setText("0.00");
            String distanceUnit = " ";
            if (TTApp.getInstance(getActivity()).getDistlapseUnit() == SettingsFragment.DistanceUnits.MILES_YARDS) {
                distanceUnit += getActivity().getResources().getString(R.string.yard_abrievation);
            } else if (TTApp.getInstance(getActivity()).getDistlapseUnit() == SettingsFragment.DistanceUnits.METERS_KILOMETERS) {
                distanceUnit += getActivity().getResources().getString(R.string.meter_abrievation);
            }
            mDistanceTraveledText.setText(String.valueOf(0) + distanceUnit);
            mDistanceTrigger = mNumericInput.getValue();
            mDistanceRemainingText.setText(String.valueOf(mDistanceTrigger + distanceUnit));
            mState = State.STARTED;
            mDistanceProgressLayout.setVisibility(View.VISIBLE);
            mDistanceProgressLayout.startAnimation(mSlideInFromTop);
            mDistanceListener.onStartDistanceLapse(mNumericInput.getValue());
        }
    }

    private void onStopDistanceLapse() {
        if (mState == State.STARTED) {
            mDistanceProgressLayout.startAnimation(mSlideOutToTop);
            mDistanceProgressLayout.setVisibility(View.GONE);
            mButton.stopAnimation();
            mState = State.STOPPED;
            mDistanceListener.onStopDistanceLapse();
        }
    }


    public void onDistanceLapseUpdate(float distanceTraveled, float speed) {
        Log.d(TAG, "onDistanceLapseUpdate" + distanceTraveled);

        //Calculate speed for kmh , mph and m/s
        float speedAdjusted = 0f;
        if (TTApp.getInstance(getActivity()).getDistlapseSpeedUnit() == SettingsFragment.DistanceSpeedUnit.KILOMETERS_PER_HOUR) {
            speedAdjusted = speed * 3.6f;
        } else if (TTApp.getInstance(getActivity()).getDistlapseSpeedUnit() == SettingsFragment.DistanceSpeedUnit.MILES_PER_HOUR) {
            speedAdjusted = speed * 2.2369f;
        } else {
            speedAdjusted = speed;
        }
        String result = String.format("%.2f", speedAdjusted);

        if (TTApp.getInstance(getActivity()).getDistlapseUnit() == SettingsFragment.DistanceUnits.MILES_YARDS) {
            //Convert distance traveled to yards
            distanceTraveled = distanceTraveled * 1.093f;
        }

        int progress = 0;
        if (distanceTraveled >= mDistanceTrigger) {
            float remainder = distanceTraveled % mDistanceTrigger;
            mDistanceTraveled = (int) remainder;
            progress = (int) (remainder / mDistanceTrigger * 100);
        } else {
            mDistanceTraveled = distanceTraveled;
            progress = (int) (mDistanceTraveled / mDistanceTrigger * 100);
        }

        Log.d(TAG, "onDistanceLapseUpdate progress " + progress);

        if (mDistanceTraveled >= mDistanceTrigger) {
            mDistanceTraveled = 0;
            mProgressArc.setProgress(0);
            mExposureCount += 1;
            mSpeed.setText(result);
        }
        mProgressArc.setProgress(progress);


        mSpeed.setText(result);
        String distanceUnit = " ";
        if (TTApp.getInstance(getActivity()).getDistlapseUnit() == SettingsFragment.DistanceUnits.MILES_YARDS) {
            distanceUnit += getActivity().getResources().getString(R.string.yard_abrievation);
        } else if (TTApp.getInstance(getActivity()).getDistlapseUnit() == SettingsFragment.DistanceUnits.METERS_KILOMETERS) {
            distanceUnit += getActivity().getResources().getString(R.string.meter_abrievation);
        }
        mDistanceTraveledText.setText(String.valueOf((int) mDistanceTraveled) + distanceUnit);
        int distanceRemaining = ((int) mDistanceTrigger - (int) mDistanceTraveled);
        mDistanceRemainingText.setText(String.valueOf(distanceRemaining + distanceUnit));
    }

    public void setDistanceLapseState(int state) {
        mGooglePlayServiceState = state;
    }

    public int getDistanceLapseState() {
        return mGooglePlayServiceState;
    }


    private void showDistanceProgress() {
        mDistanceProgressLayout.setVisibility(View.VISIBLE);
    }

    private void hideDistanceProgress() {
        mDistanceProgressLayout.setVisibility(View.GONE);
    }

    @Override
    public void setActionState(boolean actionState) {
        if (actionState == true) {
            mState = State.STARTED;
            setDistanceLapseState(GooglePlayServiceState.SERVICE_AVAILABLE);
        } else {
            mState = State.STOPPED;
        }
        setInitialUiState();
    }

    private void setInitialUiState() {
        if (mState == State.STARTED) {
            //mSyncCircle = true;
            showDistanceProgress();
            mButton.startAnimation();
        } else {
            if (mButton != null) {
                hideDistanceProgress();
                mButton.stopAnimation();
            }
        }
    }

    public void ignoreGPS(boolean ignore) {
        mIngnoreGPS = ignore;
    }

    public int getExposureCount() {
        return mExposureCount;
    }

}
