package com.triggertrap.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.triggertrap.R;
import com.triggertrap.TTApp;
import com.triggertrap.fragments.HdrTimeLapseFragment;

public class DialpadManager implements Button.OnClickListener, Button.OnLongClickListener{

	private static final String TAG = DialpadManager.class.getSimpleName();
	private Context mContext;
	private ImageButton mClear, mOK;
	private final Button mNumbers[] = new Button[10];
	private int mDialPadSate = DialPadState.HIDDEN;
	
	private View mDialPad = null;
	private DialPadInput mActiveInput = null;
	
	private Animation mSlideInFromBottom;
	private Animation mSlideOutToBottom;
	
	public interface DialPadState {
		public static final int HIDDEN = 0;
		public static final int SHOWING = 1;
	}
	
	/**
	 * Interface to be implement by the View that
	 * Receives updates from the DialPad
	 * @author neildavies
	 *
	 */
	public interface DialPadInput {
		public void upDateValue(int val);
		public void deleteValue();
		public void clearValue();
		public void switchState();
	}
	
	/**
	 * Implement by the owner of the DialPadManager so
	 * it can update the DialPadManager's active input
	 * @author neildavies
	 *
	 */
	public interface InputSelectionListener {
		public void onInputSelected(DialPadInput dialPadInput);
		public void onInputDeSelected();
		public void inputSetSize(int height, int width);
	}
	
	/**
	 * Implemented by owner of the View to listen for updates 
	 * to the view as a result of the dialpad input.
	 * @author neildavies
	 *
	 */
	public interface InputUpdatedListener {
		public void onInputUpdated();
	}
	
	public DialpadManager(Context ctx ,View v) {
		mContext = ctx;
		setUpDialPad(v);
		setUpAnimations();
	}
	
	public void setActiveInput(DialPadInput activeInput) {
		if (mActiveInput == activeInput) {
			deactiveInput();
			return;
		}
		if(mActiveInput != null && activeInput != mActiveInput) {
			mActiveInput.switchState();
			mActiveInput = activeInput;
			mActiveInput.switchState();
		} else {
			mActiveInput = activeInput;
			updateDialPad();
		}
	}
	
	public void deactiveInput() {		
		if(mDialPadSate == DialPadState.SHOWING) {
			updateDialPad();
		}

        //make sure we update values in the hdr timelapse fragment
        Activity activity = (Activity) mContext;
        HdrTimeLapseFragment hdrFragment = (HdrTimeLapseFragment) activity.getFragmentManager().findFragmentByTag(TTApp.FragmentTags.HDR_LAPSE);
		if(hdrFragment != null) {
            hdrFragment.checkInterval();
        }
        mActiveInput = null;
	}
	
	
	private void setUpAnimations() {
		mSlideInFromBottom = AnimationUtils.loadAnimation(mContext,
				R.anim.slide_in_from_bottom);
		mSlideOutToBottom = AnimationUtils.loadAnimation(mContext,
				R.anim.slide_out_to_bottom);

	}
	
	public void setKeyboardDimensions(int height, int width) {
		Log.d(TAG, "Setting keyboard dimensions: Height: "+ height);
	
		float density = mContext.getResources().getDisplayMetrics().density;
		final int textPadding = 15 * ((int) density);
		
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
		params.gravity = Gravity.BOTTOM | Gravity.RIGHT;

		mDialPad.setLayoutParams(params);
		
		final int numberRows = 4;
		final float defaultfontsize = mContext.getResources().getDimension(R.dimen.dialpad_font_size);
		
		
		if ((numberRows * (defaultfontsize + textPadding)) < height) {
			for (int i = 0; i < 10; i++) {
				mNumbers[i].setTextSize(defaultfontsize/density);
				LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
						0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                buttonParams.gravity = Gravity.CENTER;
				mNumbers[i].setLayoutParams(buttonParams);
			}		
		} else  {	
			for (int i = 0; i < 10; i++) {
				mNumbers[i].setTextSize((height /numberRows - textPadding)/density);
				LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
						0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
				mNumbers[i].setLayoutParams(buttonParams);
			}
		}
	}
	private void setUpDialPad(View dialPad) {

		mDialPad =  dialPad;
		View v1 = mDialPad.findViewById(R.id.first);
		View v2 = mDialPad.findViewById(R.id.second);
		View v3 = mDialPad.findViewById(R.id.third);
		View v4 = mDialPad.findViewById(R.id.fourth);

		mNumbers[1] = (Button) v1.findViewById(R.id.key_left);
		mNumbers[2] = (Button) v1.findViewById(R.id.key_middle);
		mNumbers[3] = (Button) v1.findViewById(R.id.key_right);

		mNumbers[4] = (Button) v2.findViewById(R.id.key_left);
		mNumbers[5] = (Button) v2.findViewById(R.id.key_middle);
		mNumbers[6] = (Button) v2.findViewById(R.id.key_right);

		mNumbers[7] = (Button) v3.findViewById(R.id.key_left);
		mNumbers[8] = (Button) v3.findViewById(R.id.key_middle);
		mNumbers[9] = (Button) v3.findViewById(R.id.key_right);

		mClear = (ImageButton) v4.findViewById(R.id.image_key_left);
		mClear.setOnClickListener(this);
		mClear.setOnLongClickListener(this);
        mClear.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		
		mNumbers[0] = (Button) v4.findViewById(R.id.key_middle);
		
		mOK = (ImageButton) v4.findViewById(R.id.image_key_right);
        mOK.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

		mOK.setOnClickListener(this);
		
		for (int i = 0; i < 10; i++) {
			mNumbers[i].setOnClickListener(this);
			mNumbers[i].setText(String.format("%d", i));
			mNumbers[i].setTag(R.id.numbers_key, new Integer(i));
		}

	}
	
	public int getDialPadState() {
		return mDialPadSate;
	}
	
	public void updateDialPad() {
		if (mDialPadSate == DialPadState.HIDDEN) {
			mDialPadSate = DialPadState.SHOWING;
			mDialPad.setVisibility(View.VISIBLE);
			mDialPad.startAnimation(mSlideInFromBottom);
		} else {
			mDialPadSate = DialPadState.HIDDEN;
			mDialPad.startAnimation(mSlideOutToBottom);
			mDialPad.setVisibility(View.GONE);
		}
		if(mActiveInput != null) {
			mActiveInput.switchState();
		}
	}

	public void updateDeleteButton() {
//		boolean enabled = mInputPointer != -1;
//		if (mDelete != null) {
//			mDelete.setEnabled(enabled);
//		}
    }

    @Override
    public boolean onLongClick(View v) {
        if (v == mClear) {
            if (mActiveInput != null) {
                mActiveInput.clearValue();
            }
            return true;
        }
        return false;
    }

    private void doOnClick(View v) {

        Integer val = (Integer) v.getTag(R.id.numbers_key);
        // A number was pressed
        if (val != null) {
            if (mActiveInput != null) {
                mActiveInput.upDateValue(val);
            }
            return;
        }

        // other keys
        // other keys
        if (v == mClear) {
            if (mActiveInput != null) {
                mActiveInput.deleteValue();
            }
        }

        if (v == mOK) {
            deactiveInput();
        }
    }


    @Override
    public void onClick(View v) {
        doOnClick(v);

    }
}
