package com.triggertrap.widget;

import android.widget.PopupWindow;
import android.widget.TextView;

import com.triggertrap.R;

public class ErrorPopup extends PopupWindow {
	
	private boolean mAbove = false;
	private TextView mView;
	private int mPopupInlineErrorBackgroundId = 0;
    private int mPopupInlineErrorAboveBackgroundId = 0;
	
	public ErrorPopup(TextView tv, int width , int height) {
		super(tv, width,height);
		mView = tv;
		mPopupInlineErrorAboveBackgroundId = R.drawable.popup_inline_error_above_holo_dark_am;
		mPopupInlineErrorBackgroundId = R.drawable.popup_inline_error_holo_dark_am;
		
        mView.setBackgroundResource(mPopupInlineErrorBackgroundId);
	}
	
	public void updateDirection(boolean above) {
		mAbove = above;
		
		if(above) {
			//Use above background
			mView.setBackgroundResource(mPopupInlineErrorAboveBackgroundId);
		} else {
			//Use below background
			mView.setBackgroundResource(mPopupInlineErrorBackgroundId);
		}
	}
	
	@Override
	public void update(int x, int y, int width, int height, boolean force) {
		// TODO Auto-generated method stub
		super.update(x, y, width, height, force);
		boolean above = isAboveAnchor();
		if(above != mAbove) {
			updateDirection(above);
		}
	}
}
