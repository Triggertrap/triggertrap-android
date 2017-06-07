package com.triggertrap.fragments;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.triggertrap.R;
import com.triggertrap.TTApp;
import com.triggertrap.util.CubicBezierInterpolator;
import com.triggertrap.util.DialpadManager;
import com.triggertrap.util.PulseGenerator;
import com.triggertrap.view.AnalogClockPreview;
import com.triggertrap.view.CircleTimerView;
import com.triggertrap.view.CountingTimerView;
import com.triggertrap.view.SimpleTimerView;
import com.triggertrap.widget.BezierWidget;
import com.triggertrap.widget.NumericView;
import com.triggertrap.widget.OngoingButton;
import com.triggertrap.widget.TimerView;

public class TimeWarpFragment extends TimeFragment implements DialpadManager.InputUpdatedListener, BezierWidget.OnControlChangeListener {

    private static final String TAG = TimeWarpFragment.class.getSimpleName();
    public static final int MINIMUM_TIMEWARP_GAP = 10;
    private DialpadManager.InputSelectionListener mInputListener = null;
    private int MAXIMUM_OVERLAPS = 150;


    private View mRootView;
    private OngoingButton mButton;
    private NumericView mTimewarpIterationsView;
    private TimerView mTimewarpDurationView;
    private FrameLayout mShowSettings;
    private FrameLayout mHideSettings;
    private View mButtonContainer;
    private View mTimeWarpSettings;
    private View mExposureDuration;
    private TextView mExposureValue1;
    private TextView mExposureValue2;
    private SimpleTimerView mDuration1;
    private SimpleTimerView mDuration2;
    private BezierWidget mBezierView;
    private CountingTimerView mTimerText;
    private SimpleTimerView mExposureTimerText;
    private SimpleTimerView mGapTimerText;
    private TextView mSequenceCountText;
    private Button mPreviewTimeWarp;
    private AnalogClockPreview mAnalogPreview;

    private CubicBezierInterpolator mInterpolator;

    private int mCurrentExposureCount;

    private Animation mSlideInFromTop;
    private Animation mSlideOutToTop;
    private Animation mSlideDownFade;
    private Animation mSlideUpShow;
    private ObjectAnimator mObjectAnimator;


    private int mInitialIterations;
    private long mDuration;
    private float mControl1X, mControl1Y, mControl2X, mControl2Y;
    private boolean mSyncCircle = false;

    private int mPreviewState = PreviewState.PREVIEW_STOPPED;

    public interface PreviewState {
        final int PREVIEW_RUNNING = 0;
        final int PREVIEW_STOPPED = 1;
    }

    public TimeWarpFragment() {
        mRunningAction = TTApp.OnGoingAction.TIMEWARP;
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
        mRootView = inflater.inflate(R.layout.time_warp, container, false);
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


        mExposureDuration = mRootView.findViewById(R.id.exposureDuration);
        mExposureValue1 = (TextView) mExposureDuration.findViewById(R.id.exposureValue);
        mDuration1 = (SimpleTimerView) mExposureDuration.findViewById(R.id.durationValue);
        mShowSettings = (FrameLayout) mExposureDuration.findViewById(R.id.showHideSettings);

        mTimeWarpSettings = mRootView.findViewById(R.id.timeWarpSettings);
        View exposureDuration = mTimeWarpSettings.findViewById(R.id.exposureDuration);
        mExposureValue2 = (TextView) mTimeWarpSettings.findViewById(R.id.exposureValue);
        mDuration2 = (SimpleTimerView) mTimeWarpSettings.findViewById(R.id.durationValue);
        mHideSettings = (FrameLayout) mTimeWarpSettings.findViewById(R.id.showHideSettings);
        ImageView showHide = (ImageView) mTimeWarpSettings.findViewById(R.id.showHideArrow);
        showHide.setImageDrawable(getResources().getDrawable(R.drawable.tickup));


        TextView exposures = (TextView) mRootView.findViewById(R.id.exposuresText);
        TextView gap = (TextView) mRootView.findViewById(R.id.gapText);
        TextView exposureLabel = (TextView) mRootView.findViewById(R.id.exposureLabel);
        TextView pauseLabel = (TextView) mRootView.findViewById(R.id.PauseLabel);

        mCurrentExposureCount = 1;
		
		mExposureDuration.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mTimeWarpSettings.setVisibility(View.VISIBLE);
				mTimeWarpSettings.startAnimation(mSlideInFromTop);
				mExposureDuration.startAnimation(mSlideDownFade);
				mExposureDuration.setVisibility(View.GONE);
				
			}
		});
		
		exposureDuration.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mTimeWarpSettings.startAnimation(mSlideOutToTop);
				mTimeWarpSettings.setVisibility(View.GONE);
				mExposureDuration.setVisibility(View.VISIBLE);
				mExposureDuration.startAnimation(mSlideUpShow);
				mInputListener.onInputDeSelected();
				
			}
		});

		setUpIterations();
		setUpDurationInput();
		
		setKeyBoardSize();
		setUpButton();
		setUpAnimations();
		setUpCircleTimer();
		
		setUpControlPoints();
		setUpInterpolator();
		resetVolumeWarning();
		setUpPreview();		
		
		return mRootView;
	}
	
	
	@Override
	public void onStop() {
		super.onStop();
	
		TTApp.getInstance(getActivity()).setTimeWarpIterations(mTimewarpIterationsView.getValue());
		TTApp.getInstance(getActivity()).setTimewarpDuration(mTimewarpDurationView.getTime());
		
		float [] pts = mBezierView.getControlPoints();
		TTApp.getInstance(getActivity()).setTimewarpControl1X(pts[0]);
		TTApp.getInstance(getActivity()).setTimewarpControl1Y(pts[1]);
		TTApp.getInstance(getActivity()).setTimewarpControl2X(pts[2]);
		TTApp.getInstance(getActivity()).setTimewarpControl2Y(pts[3]);
	}

	private void setUpButton() {
		mButton = (OngoingButton) mRootView
				.findViewById(R.id.timeWarpButton);	
		mButton.setToggleListener(new OngoingButton.OnToggleListener() {

			@Override
			public void onToggleOn() {
				Log.d(TAG, "onToggleON");
				onStartTimer();
				checkVolume();
				//startPulseSequence();
			}

			@Override
			public void onToggleOff() {
				Log.d(TAG, "onToggleOff");
				onStopTimer();
				//mOutputDispatcher.stop();
			}
		});
	}

	private void setUpAnimations() {
		mSlideInFromTop = AnimationUtils.loadAnimation(getActivity(),
				R.anim.slide_in_from_top);
		
		mSlideOutToTop = AnimationUtils.loadAnimation(getActivity(),
				R.anim.slide_out_to_top);

		mSlideDownFade = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down_fade);
		mSlideUpShow = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up_show);
		
		mSlideUpShow.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				onUpdateBezierWidget();
			}
		});
	}
	
	
	private void setUpIterations() {
		mTimewarpIterationsView = (NumericView) mRootView.findViewById(R.id.timewarpInterations);	
		mTimewarpIterationsView.setUpdateListener(this);
		mTimewarpIterationsView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if(mTimewarpIterationsView.getState() == TimerView.State.UN_SELECTED) {
						mInputListener.onInputSelected(mTimewarpIterationsView);
					} else {
						mInputListener.onInputDeSelected();
					}
					break;
				}
				return true;
			}
		});
	
		
		Bundle fragmentState = getArguments();
		if(fragmentState != null) {
			//TODO restore state for rotation
			
		} else {
			//Restore state of timewarp from persistent storage		
			mInitialIterations = TTApp.getInstance(getActivity()).getTimeWarpIterations();
			Log.d(TAG,"Initial Interations: " + mInitialIterations);
			mDuration = TTApp.getInstance(getActivity()).getTimewarpDuration();
		}
		mTimewarpIterationsView.initValue(mInitialIterations);
		mExposureValue1.setText(String.valueOf(mTimewarpIterationsView.getValue()));
		mExposureValue2.setText(String.valueOf(mTimewarpIterationsView.getValue()));
		
	}
	
	
	private void setUpDurationInput() {
		
		
		mTimewarpDurationView = (TimerView) mRootView.findViewById(R.id.timerTimeText);
		mTimewarpDurationView.setUpdateListener(this);
		mTimewarpDurationView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if(mTimewarpDurationView.getState() == TimerView.State.UN_SELECTED) {
						mInputListener.onInputSelected(mTimewarpDurationView);
					} else {
						mInputListener.onInputDeSelected();
					}
					break;
				}
				return true;
			}
		});
		
		mTimewarpDurationView.setTextInputTime(mDuration);
		mTimewarpDurationView.initInputs(mDuration);
		mDuration1.setTime(mTimewarpDurationView.getTime(), true, true);
		mDuration2.setTime(mTimewarpDurationView.getTime(), true, true);
	}
	
	private void setUpControlPoints() {
		mBezierView = (BezierWidget) mRootView.findViewById(R.id.bezierWidget);
		
		Bundle fragmentState = getArguments();
		if(fragmentState != null) {
			//TODO restore state for rotation
			
		} else {
			//Restore state of timewarp from persistent storage		
			mControl1X = TTApp.getInstance(getActivity()).getTimewarpControl1X();
			mControl1Y = TTApp.getInstance(getActivity()).getTimewarpControl1Y();
			mControl2X = TTApp.getInstance(getActivity()).getTimewarpControl2X();
			mControl2Y = TTApp.getInstance(getActivity()).getTimewarpControl2Y();
		}
		
		final ViewTreeObserver vto = mRootView.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
			
				mBezierView.setControlPoints(mControl1X, mControl1Y, mControl2X, mControl2Y);
				ViewTreeObserver obs = mRootView.getViewTreeObserver();
				obs.removeGlobalOnLayoutListener(this);
				
			}
		});
		
		mBezierView.setListener(this);
		
		
	}
	
	public void setUpInterpolator() {
		Bundle fragmentState = getArguments();
		if(fragmentState != null) {
			//TODO restore state for rotation
			
		} else {
			//Restore state of timewarp from persistent storage		
			mControl1X = TTApp.getInstance(getActivity()).getTimewarpControl1X();
			mControl1Y = TTApp.getInstance(getActivity()).getTimewarpControl1Y();
			mControl2X = TTApp.getInstance(getActivity()).getTimewarpControl2X();
			mControl2Y = TTApp.getInstance(getActivity()).getTimewarpControl2Y();
		}
		
		mInterpolator = new CubicBezierInterpolator(mControl1X, mControl1Y, mControl2X, mControl2Y);
		
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
	
	private void setKeyBoardSize() {
		mButtonContainer =  mRootView.findViewById(R.id.buttonContainer);
		final ViewTreeObserver vto = mRootView.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				int buttonContainerHeight = mButtonContainer.getHeight();		
				int buttonContainerWidth =  mButtonContainer.getWidth();
				
				Log.d(TAG, "Button container height is: "
						+ buttonContainerHeight);
				mInputListener.inputSetSize(buttonContainerHeight, buttonContainerWidth);
				ViewTreeObserver obs = mRootView.getViewTreeObserver();
				obs.removeGlobalOnLayoutListener(this);
				
			}
		});
	}
	
	
	private void setUpPreview()  {
		mAnalogPreview = (AnalogClockPreview) mRootView.findViewById(R.id.analogClock);
		mAnalogPreview.setRotationY(90);
		mButton.setRotationY(0);
		
		mObjectAnimator = ObjectAnimator.ofFloat(mAnalogPreview, "handAngle", 0f, 360f);	
		
		mObjectAnimator.setDuration(2000);
		mObjectAnimator.setInterpolator(mInterpolator);
		
		
		mObjectAnimator.setStartDelay(300);
		
		mObjectAnimator.addListener(new AnimatorListener() {
			
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
				mButton.animate().cancel();
				mAnalogPreview.animate().cancel();
				mAnalogPreview.animate().setInterpolator(new AccelerateInterpolator()).setDuration(150).rotationYBy(90).setStartDelay(0);
				mButton.animate().setInterpolator(new OvershootInterpolator(9f)).setDuration(150).rotationYBy(90).setStartDelay(150);
				mPreviewState = PreviewState.PREVIEW_STOPPED;	
				mPreviewTimeWarp.animate().rotationXBy(-90).setDuration(150);
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		mPreviewTimeWarp =  (Button) mRootView.findViewById(R.id.previewTimeWarp);
		
		mPreviewTimeWarp.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				if( mPreviewState == PreviewState.PREVIEW_STOPPED) {
					mPreviewState = PreviewState.PREVIEW_RUNNING;
					mAnalogPreview.setHandAngle(0f);
					mButton.animate().cancel();
					mAnalogPreview.animate().cancel();
					mObjectAnimator.cancel();
					mButton.setRotationY(0);
					mAnalogPreview.setRotationY(90);				
					mButton.animate().setInterpolator(new AccelerateInterpolator()).setDuration(150).rotationYBy(-90).setStartDelay(0);
					mAnalogPreview.animate().setInterpolator(new OvershootInterpolator(9f)).setDuration(150).rotationYBy(-90).setStartDelay(150);
					mObjectAnimator.start();
					mPreviewTimeWarp.animate().rotationXBy(90).setDuration(150);
				} else if (mPreviewState == PreviewState.PREVIEW_RUNNING) {
                    return;
                } else {
					mButton.animate().cancel();
					mAnalogPreview.animate().cancel();
					mObjectAnimator.cancel();
					mAnalogPreview.animate().setInterpolator(new AccelerateInterpolator()).setDuration(150).rotationYBy(90).setStartDelay(0);
					mButton.animate().setInterpolator(new OvershootInterpolator(9f)).setDuration(150).rotationYBy(90).setStartDelay(150);
					mPreviewState = PreviewState.PREVIEW_STOPPED;		
					
					
				}
			}
		});
		
	}
	private void onStartTimer() {
		
		if (mState == State.STOPPED) {
			
			if (mTimewarpIterationsView.getValue() == 0 ||  mTimewarpDurationView.getTime() == 0 ) {
				mButton.stopAnimation();
				return;
			}
			
			mState = State.STARTED;			
			mCountDownLayout.setVisibility(View.VISIBLE);
			mCountDownLayout.startAnimation(mSlideInFromTop);
			mCircleTimerView.setPassedTime(0, false);
			
			//mInterpolator.setControlPoints(0.98f, 0.01f, 0.01f, 0.98f);
			mPulseSequence = mPulseGenerator.getTimeWarpSequence(mTimewarpIterationsView.getValue(), mTimewarpDurationView.getTime(), mInterpolator);
			mPulseSeqListener.onPulseSequenceCreated(mRunningAction, mPulseSequence, false);
			
			long totaltime = PulseGenerator.getSequenceTime(mPulseSequence);
			Log.d(TAG, "Total time Circle: " +  totaltime);
			mCircleTimerView.setIntervalTime(totaltime);
			mTimerText.setTime(totaltime, true, false);
			mExposureTimerText.setTime(mPulseSequence[0], true, true);
			mGapTimerText.setTime(mPulseSequence[1], true, true);
			mCircleTimerView.startIntervalAnimation();
			mTimerText.setTime(totaltime, true, false);
			String sequenceProgress = 1 + "/" + (mPulseSequence.length/2);
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
			if(mButton != null) {
				mButton.startAnimation();
			}
		} else {
            if(mButton != null) {
			    hideCountDown();
				mButton.stopAnimation();
			}
		}
	}

	public void onPulseStarted(int currentCount, int totalCount, long timeToNext, long totalTimeMaining) {
        mCurrentExposureCount = currentCount;
        String sequenceProgress = currentCount + "/" + totalCount;
        mSequenceCountText.setText(sequenceProgress);
    }

    public void onPulseUpdate(long[] sequence, int exposures, long timeToNext,
                              long remainingPulseTime, long remainingSequenceTime) {
//		Log.d(TAG,"Pulse update exposures: " +exposures);
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
        mExposureValue1.setText(String.valueOf(mTimewarpIterationsView.getValue()));
        mExposureValue2.setText(String.valueOf(mTimewarpIterationsView.getValue()));
        mDuration1.setTime(mTimewarpDurationView.getTime(), true, true);
        mDuration2.setTime(mTimewarpDurationView.getTime(), true, true);
    }

    ArrayList<Float> overlapCoords = new ArrayList<Float>();
    CalculateOverLaps calculateOverLaps = new CalculateOverLaps();

    private void onUpdateBezierWidget() {

        calculateOverLaps.cancel(true);
        calculateOverLaps = new CalculateOverLaps();
        calculateOverLaps.execute();

    }

    @Override
    public void onControlChanged(float x1, float y1, float x2, float y2) {
        //Log.d(TAG,"Control changed");
        mInterpolator.setControlPoints(x1, y1, x2, y2);
        onUpdateBezierWidget();
    }

    private class CalculateOverLaps extends AsyncTask<Void, Void, ArrayList<Float>> {


        private void addOverlaps(int startIndex, int endIndex, long[] pauses) {
            for (int i = startIndex; i < endIndex; i++) {
                if (pauses[i] < 0) {
                    float fraction = (float) i / (pauses.length - 1);
                    overlapCoords.add(fraction);
                    overlapCoords.add(mInterpolator.getInterpolation(fraction));
                }
            }
        }

        protected ArrayList<Float> doInBackground(Void... arg0) {
            long[] pauses = mInterpolator.getOriginalPauses(
                    mTimewarpDurationView.getTime(),
                    mTimewarpIterationsView.getValue(), TTApp.getInstance(getActivity()).getBeepLength(), MINIMUM_TIMEWARP_GAP);

            overlapCoords.clear();
            if (pauses.length > MAXIMUM_OVERLAPS) {
                int startIndex = 0;
                int endIndex = MAXIMUM_OVERLAPS / 3;
                addOverlaps(startIndex, endIndex, pauses);

                startIndex = (pauses.length / 2) - MAXIMUM_OVERLAPS / 3 / 2;
                endIndex = (pauses.length / 2) + MAXIMUM_OVERLAPS / 3 / 2;
                addOverlaps(startIndex, endIndex, pauses);

                startIndex = (pauses.length) - MAXIMUM_OVERLAPS / 3;
                endIndex = pauses.length;
                addOverlaps(startIndex, endIndex, pauses);


            } else {
                int startIndex = 0;
                int endIndex = pauses.length;
                addOverlaps(startIndex, endIndex, pauses);

            }
            return overlapCoords;
        }

        protected void onPostExecute(ArrayList<Float> test) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mBezierView.setOverlapPoints(overlapCoords);
                }
            });

        }
    }

    public int getCurrentExposureCount() {
        return mCurrentExposureCount;
    }


}
