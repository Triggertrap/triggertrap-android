package com.triggertrap.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class TTLinearLayout extends LinearLayout {

    public TTLinearLayout(Context context) {
        super(context);
    }

    public TTLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public TTLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public float getXFraction() {
        if (getWidth() > 0) {
            return getX() / getWidth(); // TODO: guard divide-by-zero
        } else {
            return 0;
        }
    }

    public void setXFraction(float xFraction) {
        // TODO: cache width
        final int width = getWidth();
        setX((width > 0) ? (xFraction * width) : -9999);
    }
}
