package com.triggertrap.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class ZeroTopPaddingTextView extends TextView {
	
	private static final float NORMAL_FONT_PADDING_RATIO = 0.328f;
    // the bold fontface has less empty space on the top
    private static final float BOLD_FONT_PADDING_RATIO = 0.208f;

    private static final float NORMAL_FONT_BOTTOM_PADDING_RATIO = 0.25f;
    // the bold fontface has less empty space on the top
    private static final float BOLD_FONT_BOTTOM_PADDING_RATIO = 0.208f;

    private static final Typeface SAN_SERIF_BOLD = Typeface.create("san-serif", Typeface.BOLD);
    private static final Typeface SAN_SERIF__CONDENSED_BOLD =
            Typeface.create("sans-serif-condensed", Typeface.BOLD);

    private int mPaddingRight = 0;

    public ZeroTopPaddingTextView(Context context) {
        this(context, null);
    }

    public ZeroTopPaddingTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZeroTopPaddingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setIncludeFontPadding(false);
        updatePadding();
       
    }

    public void updatePadding() {
        float paddingRatio = NORMAL_FONT_PADDING_RATIO;
        float bottomPaddingRatio = NORMAL_FONT_BOTTOM_PADDING_RATIO;
      
        //Handling missing thin font for 4.0, 4.1
        Typeface androidClockMonoThin = null;
        if(getTypeface() == null) {
        	androidClockMonoThin = Typeface.createFromAsset(getContext().getAssets(),
                     "fonts/AndroidClockMono-Thin.ttf");
        	 setTypeface(androidClockMonoThin);
        } 
        
        
//        if (getTypeface().equals(SAN_SERIF_BOLD) ||
//                    getTypeface().equals(SAN_SERIF__CONDENSED_BOLD)) {
        	
       //if (getTypeface().getStyle() == Typeface.BOLD) {	
            paddingRatio = BOLD_FONT_PADDING_RATIO;
            bottomPaddingRatio = BOLD_FONT_BOTTOM_PADDING_RATIO;
        //}
        // no need to scale by display density because getTextSize() already returns the font
        // height in px
        setPadding(0, (int) (-paddingRatio * getTextSize()), mPaddingRight,
                (int) (-bottomPaddingRatio * getTextSize()));
    }

    public void setPaddingRight(int padding) {
        mPaddingRight = padding;
        updatePadding();
    }
}
