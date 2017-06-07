package com.triggertrap.fragments;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Layout;
import android.text.StaticLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.triggertrap.R;
import com.triggertrap.TTApp;
import com.triggertrap.fragments.preference.SettingsFragment;
import com.triggertrap.util.DialpadManager;
import com.triggertrap.widget.ErrorPopup;
import com.triggertrap.widget.OngoingButton;

public class TimeLapseFragment extends TimeFragment implements
        DialpadManager.InputUpdatedListener {

    private static String TAG = TimeLapseFragment.class.getSimpleName();

	private View mRootView;
	private OngoingButton mButton;
	private TextView mTimeLapseCount;
	private long mInterval;
	private int mLastCount = 0;
	private View mButtonContainer;
	private ImageView mErrorIcon;
	private ErrorPopup mErrorPopup;
	private String mError;

    private int mCurrentExposureCount;

	private int mValidState = ValidState.VALID;

	private interface ValidState {
		public static final int VALID = 0;
		public static final int IN_VALID = 1;
	}

	// Handler for received Events from Settings Fragment
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Extract data included in the Intent
			Log.d(TAG, "Received Broadcast.....");
			int type = intent.getIntExtra(
					SettingsFragment.SettingsEvent.EVENT_TYPE, 0);
			int value = (int) intent.getLongExtra(
					SettingsFragment.SettingsEvent.EVENT_VALUE, 2);
			if (type == SettingsFragment.SettingsType.PULSE_LENGTH) {
				if (value > mTimeView.getTime()) {
					Log.d(TAG, "Setting Error ICON to VISIBLE");
					mErrorIcon.setRotationY(0);
					mErrorIcon.setVisibility(View.VISIBLE);
					mValidState = ValidState.IN_VALID;
				} else {
					Log.d(TAG, "Setting Error ICON to INVISIBLE");
					mErrorIcon.setVisibility(View.INVISIBLE);
					mValidState = ValidState.VALID;

				}
			}

		}
	};

	public TimeLapseFragment() {
		mRunningAction = TTApp.OnGoingAction.TIMELAPSE;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		Bundle fragmentState = getArguments();
		if (fragmentState != null) {
			String tag = fragmentState
					.getString(TriggertrapFragment.BundleKey.FRAGMENT_TAG);
			// Is this bundle for this Fragment?
			if (tag.equals(getTag())) {
				mInterval = fragmentState.getLong(
						TriggertrapFragment.BundleKey.PULSE_INTERVAL, 1000);
				boolean isActive = fragmentState.getBoolean(
						TriggertrapFragment.BundleKey.IS_ACTION_ACTIVE, false);
				if (isActive) {
					mState = State.STARTED;
				} else {
					mState = State.STOPPED;
				}
			}

		} else {
			// Restore state of time lapse from persistent storage
			mInterval = TTApp.getInstance(getActivity()).getTimeLapseInterval();
		}
		// Register mMessageReceiver to receive messages.
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				mMessageReceiver,
				new IntentFilter(SettingsFragment.SETTINGS_UPDATE_EVENT));

		mError = getResources().getString(R.string.time_lapse_error);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Unregister mMessageReceiver to receive messages.
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
				mMessageReceiver);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		Log.d(TAG, "Interval is :" + mInterval);
		mRootView = super.onCreateView(inflater, container, savedInstanceState);
		TextView title = (TextView) mRootView.findViewById(R.id.timeLapseText);
		title.setTypeface(SAN_SERIF_LIGHT);
		mTimeLapseCount = (TextView) mRootView
				.findViewById(R.id.timeLapseCount);
		mTimeView.setTextInputTime(mInterval);
		mTimeView.initInputs(mInterval);
		mTimeView.setUpdateListener(this);
		mErrorIcon = (ImageView) mRootView.findViewById(R.id.errorIcon);

		if (mTimeView.getTime() < TTApp.getInstance(getActivity())
				.getBeepLength()) {
			mErrorIcon.setRotationY(0);
			mErrorIcon.setVisibility(View.VISIBLE);
			mValidState = ValidState.IN_VALID;
		} else {
			mErrorIcon.setVisibility(View.INVISIBLE);
			mValidState = ValidState.VALID;
		}

		mTimedInputView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onInputDeSelected();
					// Set the text on the input just in case the user entered
					// something weird like 88 mins.
					mTimeView.setTextInputTime(mTimeView.getTime());
					dismissError();
				}

			}
		});

		mErrorIcon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mErrorPopup != null && mErrorPopup.isShowing()) {
					mErrorPopup.dismiss();
				} else {
					showError();
				}

			}
		});

		mTimeView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mListener.onInputSelected(mTimeView);
					dismissError();
					break;
				}
				return true;
			}
		});
		setUpButton();
		setKeyBoardSize();
		resetVolumeWarning();
		return mRootView;
	}

	private void setUpButton() {
		mButton = (OngoingButton) mRootView.findViewById(R.id.timelapseButton);
		mButton.setToggleListener(new OngoingButton.OnToggleListener() {

			@Override
			public void onToggleOn() {
				Log.d(TAG, "onToggleON");
                mCurrentExposureCount = 0;
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
                mListener.inputSetSize(buttonContainerHeight,
                        buttonContainerWidth);
                ViewTreeObserver obs = mRootView.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);

            }
        });
    }

    @Override
    public void onStop() {
        // Persist the state of the timelapse interval
        Log.d(TAG, "Stopping timelapse: " + mTimeView.getTime());
        mInterval = mTimeView.getTime();
        TTApp.getInstance(getActivity()).setTimeLapseInterval(mInterval);
        super.onStop();

    }

    @Override
    public Bundle getStateBundle() {
        super.getStateBundle();
        mStateBundle.putLong(TriggertrapFragment.BundleKey.PULSE_INTERVAL,
                mInterval);
        if (mState == State.STARTED) {
            mStateBundle.putBoolean(
                    TriggertrapFragment.BundleKey.IS_ACTION_ACTIVE, true);
        } else {
            mStateBundle.putBoolean(
                    TriggertrapFragment.BundleKey.IS_ACTION_ACTIVE, false);
        }
        return mStateBundle;
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

    public void onPulseStarted(int count, long timeToNext) {
        mCircleTimerView.setPassedTime(0, false);
        mCircleTimerView.setIntervalTime(timeToNext);
        mCircleTimerView.startIntervalAnimation();
        mTimerText.setTime(timeToNext, true, false);
        mTimeLapseCount.setText(String.valueOf(count));
        mLastCount = count;
    }

    public void onPulseUpdate(int exposures, long timeToNext,
                              long remainingPulseTime) {

        mCurrentExposureCount = exposures;

        mTimerText.setTime(remainingPulseTime, true, true);
        if (remainingPulseTime != 0) {
            if (syncCircle) {
                mTimeLapseCount.setText(String.valueOf(exposures));
                synchroniseCircle(remainingPulseTime);
            }
        }
    }

    private void setInitialUiState() {
        if (mState == State.STARTED) {
            syncCircle = true;
            showCountDown();
            if (mTimeLapseCount !=  null) {
                mTimeLapseCount.setText(String.valueOf(mLastCount));
                mButton.startAnimation();
            }
        } else {
            if (mButton != null) {
                hideCountDown();
                mButton.stopAnimation();
            }
        }
    }

    private void onStartTimer() {

        if (mState == State.STOPPED) {
            if (mValidState == ValidState.IN_VALID) {
                showError();
                mButton.stopAnimation();
                return;
            }
            mState = State.STARTED;
            mLastCount = 0;
            long timeMilliSeconds = mTimeView.getTime();
            mCountDownLayout.setVisibility(View.VISIBLE);
            mCountDownLayout.startAnimation(mSlideInFromTop);
            mCircleTimerView.setPassedTime(0, false);
            mCircleTimerView.setIntervalTime(timeMilliSeconds);
            mTimerText.setTime(timeMilliSeconds, true, false);
            mPulseSequence = mPulseGenerator
                    .getTimeLapseSequence(timeMilliSeconds);
            mPulseSeqListener.onPulseSequenceCreated(
                    TTApp.OnGoingAction.TIMELAPSE, mPulseSequence, true);

        }
    }

    private void onStopTimer() {
        if (mState == State.STARTED) {
            mCircleTimerView.abortIntervalAnimation();
            mCountDownLayout.startAnimation(mSlideOutToTop);
            mCountDownLayout.setVisibility(View.INVISIBLE);
            mButton.stopAnimation();
            mState = State.STOPPED;
            mPulseSeqListener.onPulseSequenceCancelled();
            // mProgressCountText.setText(String.valueOf(0));
        }
    }

    private void chooseSize(PopupWindow pop, CharSequence text, TextView tv) {
        int wid = tv.getPaddingLeft() + tv.getPaddingRight();
        int ht = tv.getPaddingTop() + tv.getPaddingBottom();

        int defaultWidthInPixels = tv.getResources().getDimensionPixelSize(
                R.dimen.text_error_width);
        Layout l = new StaticLayout(text, tv.getPaint(), defaultWidthInPixels,
                Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
        float max = 0;
        for (int i = 0; i < l.getLineCount(); i++) {
            max = Math.max(max, l.getLineWidth(i));
        }

		/*
         * Now set the popup size to be big enough for the text plus the border
		 * capped to DEFAULT_MAX_POPUP_WIDTH
		 */
        pop.setWidth(wid + (int) Math.ceil(max));
        pop.setHeight(ht + l.getHeight());
    }

    private int getErrorX() {
		/*
		 * The "25" is the distance between the point and the right edge of the
		 * background
		 */
        final float scale = mErrorIcon.getResources().getDisplayMetrics().density;

        int errorX;
        int offset;

		offset = (int) (1 * scale + 0.5f);
		errorX = mErrorIcon.getWidth() - mErrorPopup.getWidth()
				- offset;
        return errorX;
    }

    private int getErrorY() {

		final int compoundPaddingTop = mErrorIcon.getPaddingTop();
		int vspace = mErrorIcon.getBottom() - mErrorIcon.getTop()
				- mErrorIcon.getPaddingBottom() - compoundPaddingTop;

		int icontop = (int) (compoundPaddingTop + vspace / 1.5f);

        icontop += 10;

		final float scale = mErrorIcon.getResources().getDisplayMetrics().density;
		return icontop - mErrorIcon.getHeight()
				- (int) (2 * scale + 0.5f);
	}

	private void showError() {

		if (mErrorPopup == null) {
			LayoutInflater inflater = LayoutInflater.from(mErrorIcon
					.getContext());
			final TextView err = (TextView) inflater.inflate(
					R.layout.textview_hint, null);

			final float scale = mErrorIcon.getResources().getDisplayMetrics().density;
			mErrorPopup = new ErrorPopup(err, (int) (200 * scale + 0.5f),
					(int) (50 * scale + 0.5f));
			mErrorPopup.setFocusable(false);
			// The user is entering text, so the input method is needed. We
			// don't want the popup to be displayed on top of it.
			mErrorPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
		}

		TextView tv = (TextView) mErrorPopup.getContentView();
		chooseSize(mErrorPopup, mError, tv);
		tv.setText(mError);

		mErrorPopup.showAsDropDown(mErrorIcon, getErrorX(), getErrorY());
		mErrorPopup.updateDirection(mErrorPopup.isAboveAnchor());
	}

	@Override
	public void dismissError() {
		if (mErrorPopup != null && mErrorPopup.isShowing()) {
			mErrorPopup.dismiss();
		}
	}

	@Override
	public void onInputUpdated() {
		if (mTimeView.getTime() < TTApp.getInstance(getActivity())
				.getBeepLength()) {
			if (mValidState != ValidState.IN_VALID) {
				mErrorIcon.setVisibility(View.VISIBLE);
				mErrorIcon.setRotationY(90);
				mErrorIcon.animate().rotationYBy(-90).setDuration(150);
				mErrorIcon.animate().setListener(
						new Animator.AnimatorListener() {

							@Override
							public void onAnimationStart(Animator animation) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationRepeat(Animator animation) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationEnd(Animator animation) {
								showError();

							}

							@Override
							public void onAnimationCancel(Animator animation) {
								// TODO Auto-generated method stub

							}
						});
				mValidState = ValidState.IN_VALID;

			}
		} else {
			if (mValidState != ValidState.VALID) {
				mErrorIcon.animate().setListener(null);
				mErrorIcon.animate().rotationYBy(90).setDuration(150);
				mValidState = ValidState.VALID;
				dismissError();
			}
		}
	}

    public int getCurrentExposureCount() {
        return mCurrentExposureCount;
    }

}