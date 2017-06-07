package com.triggertrap.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.triggertrap.R;
import com.triggertrap.util.DialpadManager;
import com.triggertrap.util.DialpadManager.DialPadInput;

public class NumericView extends LinearLayout implements DialPadInput {

	private static final String TAG = NumericView.class.getSimpleName();
	private static final int DEFAULT_MAX_CHARS = 5;
	private Typeface mAndroidClockMonoThin;
	private Typeface mAndroidClockMonoBold;
	private TextView mNumericValue;
	private int mMaxChars = DEFAULT_MAX_CHARS;
	private final int  mGrayColor;

	private DialpadManager.InputUpdatedListener  mUpdateListener = null;
	private int mState = State.UN_SELECTED;
	
	public interface State {
		public static final int SELECTED = 0;
		public static final int UN_SELECTED = 1;
	}
	public NumericView(Context context) {
		 this(context, null);
	}
	
	public NumericView(Context context, AttributeSet attrs) {
		super(context, attrs);	
		if(!isInEditMode()) {
			mAndroidClockMonoThin =
	                Typeface.createFromAsset(context.getAssets(), "fonts/AndroidClockMono-Thin.ttf");
			mAndroidClockMonoBold = 
					Typeface.createFromAsset(context.getAssets(), "fonts/AndroidClockMono-Bold.ttf");
		}
		 mGrayColor = context.getResources().getColor(R.color.tt_dark_grey);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mNumericValue = (TextView)findViewById(R.id.numericValue);
		mNumericValue.setTypeface(mAndroidClockMonoBold);
		mNumericValue.setTextColor(mGrayColor);
	}
	
	public void initValue(int value) {
		mNumericValue.setText(String.valueOf(value));
	}
	
	public void setUpdateListener(DialpadManager.InputUpdatedListener listener) {
		mUpdateListener = listener;
	}
	public int getState() {
		return mState;
	}
	public int getValue() {
		String text = mNumericValue.getText().toString();
		return Integer.parseInt(text);
	}
	
	@Override
	public void upDateValue(int val) {
		String text = mNumericValue.getText().toString();
		int numChars = text.length();
		int currentVal = Integer.parseInt(text);
		int newVal = 0;
		int multiplier = 1;
		if(numChars == mMaxChars) {
			//Ignore input
			return;
		}

		if(currentVal != 0) {	
			multiplier = 10;
		} 
		newVal = (currentVal * multiplier) + val;
		mNumericValue.setText(String.valueOf(newVal));
		if(mUpdateListener != null) {
			mUpdateListener.onInputUpdated();
		}

	}

	@Override
	public void deleteValue() {
		String text =  mNumericValue.getText().toString();
		int currentVal = Integer.parseInt(text);
		int numChars = text.length();
		int newVal = 0;
		if(numChars != 1) {
			newVal = currentVal/10;
		}
		mNumericValue.setText(String.valueOf(newVal));
		if(mUpdateListener != null) {
			mUpdateListener.onInputUpdated();
		}
	}

	@Override
	public void clearValue() {
		mNumericValue.setText(String.valueOf(0));
		if(mUpdateListener != null) {
			mUpdateListener.onInputUpdated();
		}
	}

	@Override
	public void switchState() {
		// TODO Auto-generated method stub

		int paddingLeft = getPaddingLeft();
		int paddingRight = getPaddingRight();
		int paddingTop = getPaddingTop();
		int paddingBottom = getPaddingBottom();
		
		if (mState == State.SELECTED) {
			mState = State.UN_SELECTED;
			setBackgroundDrawable(getResources().getDrawable(R.drawable.grey_text_field));
		} else {
			mState = State.SELECTED;
			setBackgroundDrawable(getResources().getDrawable(R.drawable.red_text_field));
		}
		setPadding(paddingLeft,paddingTop,paddingRight, paddingBottom );
	}
	
	public void setMaxChars(int numchars) {
		mMaxChars = numchars;
	}

}
