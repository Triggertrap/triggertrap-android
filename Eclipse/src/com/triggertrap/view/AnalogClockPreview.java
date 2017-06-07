/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.triggertrap.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.triggertrap.R;

/**
 * This widget display an analogic clock with two hands for hours and
 * minutes.
 */
public class AnalogClockPreview extends View {
   
    private final Drawable mSecondHand;
    private  Drawable mDial;

    private int mDialWidth = 140;
    private int mDialHeight = 140;
    private int mDialStrokeWidth = 6;
      
    private boolean mChanged;
    private final Context mContext;
    
   
    private float mHandAngle = 0f;
    
    private float mDotRadius = 7;
    private float mDotOffset = 0;
    private Paint mDotPaint;
    private Paint mDialPaint;

    public AnalogClockPreview(Context context) {
        this(context, null);
    }

    public AnalogClockPreview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnalogClockPreview(Context context, AttributeSet attrs,
                       int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        Resources r = mContext.getResources();
		final Resources res = getResources();
		
		float density = context.getResources().getDisplayMetrics().density;
		  
        mSecondHand = r.getDrawable(R.drawable.clock_analog_second_mipmap);

       
        mDotRadius = mDotRadius *density;
        mDotOffset = mDotOffset * density;
        mDialWidth = (int)(mDialWidth * density);
        mDialHeight =(int)( mDialHeight *density);
        mDialStrokeWidth =(int)( mDialStrokeWidth *density);
        
        final int dotColor = res.getColor(R.color.tt_red_30);
        if (dotColor != 0) {
            mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mDotPaint.setColor(dotColor);
        }
        
        mDialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDialPaint.setColor(dotColor);
        mDialPaint.setStyle(Paint.Style.STROKE);
        mDialPaint.setStrokeWidth(mDialStrokeWidth);


    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
  
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize =  MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize =  MeasureSpec.getSize(heightMeasureSpec);

        float hScale = 1.0f;
        float vScale = 1.0f;

        if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
            hScale = (float) widthSize / (float) mDialWidth;
        }

        if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight) {
            vScale = (float )heightSize / (float) mDialHeight;
        }

        float scale = Math.min(hScale, vScale);

        setMeasuredDimension(resolveSizeAndState((int) (mDialWidth * scale), widthMeasureSpec, 0),
                resolveSizeAndState((int) (mDialHeight * scale), heightMeasureSpec, 0));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mChanged = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean changed = mChanged;
        if (changed) {
            mChanged = false;
        }

        int availableWidth = getWidth();
        int availableHeight = getHeight();

        int x = availableWidth / 2;
        int y = availableHeight / 2;

        final Drawable dial = mDial;

        boolean scaled = false;

        canvas.drawCircle(x, y, mDialHeight/2- mDialStrokeWidth/2, mDialPaint);
    
        drawHand(canvas, mSecondHand, x, y, mHandAngle, changed);
       

        if (mDotRadius > 0f && mDotPaint != null) {
            canvas.drawCircle(x, y  + mDotOffset, mDotRadius, mDotPaint);
        }
        
        if (scaled) {
            canvas.restore();
        }
    }

    private void drawHand(Canvas canvas, Drawable hand, int x, int y, float angle,
          boolean changed) {
      canvas.save();
      canvas.rotate(angle, x, y);
      if (changed) {
          final int w = hand.getIntrinsicWidth();
          final int h = hand.getIntrinsicHeight();
          hand.setBounds(x - (w / 2), y - (3*h / 4), x + (w / 2), y + (h / 4));
      }
      hand.draw(canvas);
      canvas.restore();
    }

  
    public void setHandAngle(float angle) {
    	mHandAngle = angle;
    	invalidate();
    }
 

}

