package com.triggertrap.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.triggertrap.R;
import com.triggertrap.util.DialpadManager;
import com.triggertrap.util.DialpadManager.DialPadInput;
import com.triggertrap.view.ZeroTopPaddingTextView;

public class TimerView extends LinearLayout implements DialPadInput {

    private final int START_POINTER_DECIMAL = -1;
    private final int START_POINTER_SECONDS = 1;
    
	private ZeroTopPaddingTextView mHoursOnes, mMinutesOnes;
    private ZeroTopPaddingTextView mHoursTens, mMinutesTens;
    private TextView mSeconds, mMilliSeconds;
    private Typeface mAndroidClockMonoThin, mAndroidClockMonoBold;
    private Typeface mOriginalHoursTypeface;
    private final int mWhiteColor, mGrayColor;

    private int mInputSize = 8;
	private int mInputPointer = START_POINTER_DECIMAL;
	private int mInputPointerStart = START_POINTER_DECIMAL;
	private int mInput[] = new int[mInputSize];
	    
	private DialpadManager.InputUpdatedListener  mUpdateListener = null;
	private int mState = State.UN_SELECTED;
	
	public interface State {
		public static final int SELECTED = 0;
		public static final int UN_SELECTED = 1;
	}
    public TimerView(Context context) {
        this(context, null);
    }

    public TimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!isInEditMode()) {
        	mAndroidClockMonoThin =
	                Typeface.createFromAsset(context.getAssets(), "fonts/AndroidClockMono-Thin.ttf");
	        mAndroidClockMonoBold = Typeface.createFromAsset(
	                context.getAssets(), "fonts/AndroidClockMono-Bold.ttf");
        }
	   
        mWhiteColor = context.getResources().getColor(R.color.tt_white);
        mGrayColor = context.getResources().getColor(R.color.tt_dark_grey);
  
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mHoursTens = (ZeroTopPaddingTextView)findViewById(R.id.hours_tens);
        mMinutesTens = (ZeroTopPaddingTextView)findViewById(R.id.minutes_tens);
        mHoursOnes = (ZeroTopPaddingTextView)findViewById(R.id.hours_ones);
        mMinutesOnes = (ZeroTopPaddingTextView)findViewById(R.id.minutes_ones);
        mSeconds = (TextView)findViewById(R.id.seconds);
        mMilliSeconds  = (TextView)findViewById(R.id.milliseconds);
        
        if (mHoursOnes != null || mMinutesOnes != null) {
            //mOriginalHoursTypeface = mMinutesOnes.getTypeface();
            mOriginalHoursTypeface =  mAndroidClockMonoBold;
        }
        // Set the lowest time unit with thin font (excluding hundredths)
        if (mSeconds != null) {
				mSeconds.setTypeface(mAndroidClockMonoThin);
			
        } else  {
            if (mMinutesTens != null) {
                mMinutesTens.setTypeface(mAndroidClockMonoThin);
                mMinutesTens.updatePadding();
            }
            if (mMinutesOnes != null) {
                mMinutesOnes.setTypeface(mAndroidClockMonoThin);
                mMinutesOnes.updatePadding();
            }
        }
        
        if (mMilliSeconds != null) {
				mMilliSeconds.setTypeface(mAndroidClockMonoThin);    	
        } else {
        	 mInputPointer = START_POINTER_SECONDS;
        	 mInputPointerStart = START_POINTER_SECONDS;
        }
        
        //If we don't have hours reduce the input size accordingly
        if (mHoursTens == null && mHoursOnes == null) {
        	mInputSize = 6;
        }
    }
    
    public void switchState() {
    	if (mState == State.SELECTED) {
    		mState =  State.UN_SELECTED;
    		setBackgroundDrawable(getResources().getDrawable(R.drawable.grey_text_field));
			setPadding(0, (int)getResources().getDimension(R.dimen.medium_font_padding), 0, (int)getResources().getDimension(R.dimen.medium_font_padding));
		} else {
			mState =  State.SELECTED;
			setBackgroundDrawable(getResources().getDrawable(R.drawable.red_text_field));
			setPadding(0, (int)getResources().getDimension(R.dimen.medium_font_padding), 0, (int)getResources().getDimension(R.dimen.medium_font_padding));
		}
    }
    
	public void initInputs(long time) {
		long hundreds, seconds, minutes, hours;
		seconds = time / 1000;
		hundreds = (time - seconds * 1000) / 10;
		minutes = seconds / 60;
		seconds = seconds - minutes * 60;
		hours = minutes / 60;
		minutes = minutes - hours * 60;
		if (hours > 999) {
			hours = 0;
		}
		// Log.d(TAG, "Setting time to, hours:" + hours + " minutes: " + minutes
		// + " seconds:" + seconds + " hundreths:" + hundreds);

		final int hoursTens = (int) hours / 10 % 10;
		final int hoursUnits = (int) hours - (hoursTens * 10);
		final int minTens = (int) minutes / 10 % 10;
		final int minUnits = (int) minutes - (minTens * 10);
		final int secTens = (int) seconds / 10 % 10;
		final int secsUnits = (int) seconds - (secTens * 10);
		final int hundredsTens = (int) hundreds / 10 % 10;
		final int hundredsUnits = (int) hundreds - (hundredsTens * 10);
		
		if (hoursTens > 0) {
			mInputPointer = 7;			
		} else if (hoursUnits > 0) {
			mInputPointer = 6;			
		} else if (minTens > 0) {
			mInputPointer = 5;			
		} else if (minUnits > 0) {
			mInputPointer = 4;			
		} else if (secTens > 0) {
			mInputPointer = 3;			
		} else if (secsUnits > 0) {
			mInputPointer = 2;			
		} else if (hundredsTens > 0) {
			mInputPointer = 1;			
		} else if (hundredsUnits > 0) {
			mInputPointer = 0;			
		}	
		
		mInput[7] = hoursTens;
		mInput[6] = hoursUnits;
		mInput[5] = minTens;
		mInput[4] = minUnits;
		mInput[3] = secTens;
		mInput[2] = secsUnits;
		mInput[1] = hundredsTens;
		mInput[0] = hundredsUnits;
	}
	
	public int getState() {
		return mState;
	}
	public long getTime() {
		long secs = mInput[7] * 36000 + mInput[6] * 3600 + mInput[5] * 600
				+ mInput[4] * 60 + mInput[3] * 10 + mInput[2];
		// Return the time in milliseconds
		return (secs * 1000) + (mInput[1] * 100) + (mInput[0] * 10);
	}

    public void setTime(int hoursTensDigit, int hoursOnesDigit, int minutesTensDigit,
            int minutesOnesDigit, int seconds, int milliseconds) {
        if (mHoursTens != null) {
            // Hide digit
            if (hoursTensDigit == -2) {
                mHoursTens.setVisibility(View.INVISIBLE);
            } else if (hoursTensDigit == -1) {
                mHoursTens.setText("-");
                //mHoursTens.setTypeface(mAndroidClockMonoThin);
                //mHoursTens.setTextColor(mGrayColor);
                mHoursTens.updatePadding();
                mHoursTens.setVisibility(View.VISIBLE);
            } else {
                mHoursTens.setText(String.format("%d",hoursTensDigit));
                mHoursTens.setTypeface(mOriginalHoursTypeface);
                //mHoursTens.setTextColor(mGrayColor);
                mHoursTens.updatePadding();
                mHoursTens.setVisibility(View.VISIBLE);
            }
        }
        if (mHoursOnes != null) {
            if (hoursOnesDigit == -1) {
                mHoursOnes.setText("-");
                //mHoursOnes.setTypeface(mAndroidClockMonoThin);
                //mHoursOnes.setTextColor(mGrayColor);
                mHoursOnes.updatePadding();
            } else {
                mHoursOnes.setText(String.format("%d",hoursOnesDigit));
                mHoursOnes.setTypeface(mOriginalHoursTypeface);
                //mHoursOnes.setTextColor(mGrayColor);
                mHoursOnes.updatePadding();
            }
        }
        if (mMinutesTens != null) {
            if (minutesTensDigit == -1) {
                mMinutesTens.setText("-");
                //mMinutesTens.setTextColor(mGrayColor);
            } else {
                //mMinutesTens.setTextColor(mGrayColor);
                mMinutesTens.setText(String.format("%d",minutesTensDigit));
                mMinutesTens.setTypeface(mOriginalHoursTypeface);
            }
        }
        if (mMinutesOnes != null) {
            if (minutesOnesDigit == -1) {
                mMinutesOnes.setText("-");
                //mMinutesOnes.setTextColor(mGrayColor);
            } else {
                mMinutesOnes.setText(String.format("%d",minutesOnesDigit));
                //mMinutesOnes.setTextColor(mGrayColor);
                mMinutesOnes.setTypeface(mOriginalHoursTypeface);
            }
        }

        if (mSeconds != null) {
            mSeconds.setText(String.format("%02d",seconds));
        }
        
        if (mMilliSeconds != null) {
        	mMilliSeconds.setText(String.format("%02d", milliseconds));
        }
       
    }
    
	public void setTextInputTime(long time) {
		long hundreds, seconds, minutes, hours;
		seconds = time / 1000;
		hundreds = (time - seconds * 1000) / 10;
		minutes = seconds / 60;
		seconds = seconds - minutes * 60;
		hours = minutes / 60;
		minutes = minutes - hours * 60;
		if (hours > 999) {
			hours = 0;
		}
		// Log.d(TAG, "Setting time to, hours:" + hours + " minutes: " + minutes
		// + " seconds:" + seconds + " hundreths:" + hundreds);

		final int hoursTens = (int) hours / 10 % 10;
		final int hoursUnits = (int) hours - (hoursTens * 10);
		final int minTens = (int) minutes / 10 % 10;
		final int minUnits = (int) minutes - (minTens * 10);

		// Log.d(TAG, "Hours tens: " + hoursTens + " Hours units:" +
		// hoursUnits);

		setTime(hoursTens, hoursUnits, minTens, minUnits,
				(int) seconds, (int) hundreds);
	}

	public void setUpdateListener(DialpadManager.InputUpdatedListener listener) {
		mUpdateListener = listener;
	}
	
	private void updateTime() {
		setTime(mInput[7], mInput[6], mInput[5], mInput[4], mInput[3]
				* 10 + mInput[2], mInput[1] * 10 + mInput[0]);
	}
	@Override
	public void upDateValue(int val) {

		// pressing "0" as the first digit does nothing
		if (mInputPointer == mInputPointerStart && val == 0) {
			return;
		}
		if (mInputPointer < mInputSize - 1) {
			for (int i = mInputPointer; i >= 0; i--) {
				mInput[i + 1] = mInput[i];
			}
			mInputPointer++;
			mInput[mInputPointerStart + 1] = val;
			updateTime();
		}
		if(mUpdateListener != null) {
			mUpdateListener.onInputUpdated();
		}
		return;
			
	}

	@Override
	public void deleteValue() {
		if (mInputPointer >= (mInputPointerStart + 1)) {
			for (int i = 0; i < mInputPointer; i++) {
				mInput[i] = mInput[i + 1];
			}
			mInput[mInputPointer] = 0;
			mInputPointer--;
			updateTime();
		}
		if(mUpdateListener != null) {
			mUpdateListener.onInputUpdated();
		}
		
	}

	@Override
	public void clearValue() {
		for (int i = 0; i < mInputSize; i++) {
			mInput[i] = 0;
		}
		mInputPointer = mInputPointerStart;
		updateTime();		
		if(mUpdateListener != null) {
			mUpdateListener.onInputUpdated();
		}
	}
    
	public void drawAllBold() {
		mSeconds.setTypeface(mAndroidClockMonoBold);
		mMilliSeconds.setTypeface(mAndroidClockMonoBold);
	}
	
    
   
}