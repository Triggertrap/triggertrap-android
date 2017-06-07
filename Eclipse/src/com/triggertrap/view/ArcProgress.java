package com.triggertrap.view;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.triggertrap.R;

public class ArcProgress  extends View {

	private static final String TAG = ArcProgress.class.getSimpleName();
	
	private static final int DEFAULT_PROGRESS_WIDTH = 4;
	private static final int DEFAULT_START_ANGLE = 45;
	private static final int DEFAULT_END_ANGLE = 270;
	
    private float mProgressWidth = DEFAULT_PROGRESS_WIDTH;
    private int mAngleOffset = -90;
    private int mProgress = 0;
    private int mStartAngle = DEFAULT_START_ANGLE;
    private int mEndAngle = DEFAULT_END_ANGLE;
    private int mEndProgressAngle= 0;
    private int mStartProgressAngle = 0;
    private int mRotation = 0;
    private boolean mClockwise = true;
     
    private RectF mTempRect = new RectF();
	private Paint mCirclePaint;
	private Paint mProgressPaint;
	 
	private ObjectAnimator mProgressAnimation;
	
	private OnProgressListener mListener;
	
	public interface OnProgressListener {
		public void onProgressChange(int progress);
	}
	
	public ArcProgress(Context context) {
		super(context);
		init(context, null, 0);
	}

	public ArcProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public ArcProgress(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}
	
	private void init(Context context, AttributeSet attrs, int defStyle) {
		final Resources res = getResources();
		
		int circleColor = res.getColor(android.R.color.darker_gray);
		int progressColor = res.getColor(android.R.color.holo_blue_bright);

		if (attrs != null) {
			// Attribute initialization
			final TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.ArcProgress, defStyle, 0);
			mProgress = a.getInteger(R.styleable.ArcProgress_progress,
					mProgress);
			mStartAngle = 
					a.getInteger(R.styleable.ArcProgress_startAngle,
							mStartAngle);
			mEndAngle = 
					a.getInteger(R.styleable.ArcProgress_endAngle,
							mEndAngle);
			mProgressWidth = 
					a.getDimension(R.styleable.ArcProgress_progressWidth,
							mProgressWidth);
            mRotation = a.getInt(R.styleable.ArcProgress_rotation, mRotation);
            mClockwise = a.getBoolean(R.styleable.ArcProgress_clockwise, mClockwise);
            
			circleColor = a.getColor(R.styleable.ArcProgress_circleColor,circleColor);
			progressColor = a.getColor(R.styleable.ArcProgress_progressColor, progressColor);			
			a.recycle();			
		}
		
				
		mCirclePaint = new Paint();
		mCirclePaint.setColor(circleColor);
		mCirclePaint.setAntiAlias(true);
		mCirclePaint.setStyle(Paint.Style.STROKE);
		mCirclePaint.setStrokeCap(Paint.Cap.ROUND);
		mCirclePaint.setStrokeWidth(mProgressWidth);

		mProgressPaint = new Paint();
		mProgressPaint.setColor(progressColor);
		mProgressPaint.setAntiAlias(true);
		mProgressPaint.setStyle(Paint.Style.STROKE);
		mProgressPaint.setStrokeWidth(mProgressWidth + 2);
		mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
		
		if(mClockwise) {
			mStartProgressAngle = mStartAngle + mAngleOffset + mRotation;
		} else {
			mStartProgressAngle = mStartAngle + mEndAngle;
		}
				
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawArc(mTempRect, mStartAngle + mAngleOffset + mRotation, mEndAngle  , false, mCirclePaint);
		canvas.drawArc(mTempRect, mStartProgressAngle, mEndProgressAngle , false, mProgressPaint);
	}
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		final int height = getDefaultSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		final int width = getDefaultSize(getSuggestedMinimumWidth(),
				widthMeasureSpec);
		final int min = Math.min(width, height);
		float top = 0;
		float left = 0;
		int arcDiameter = min - getPaddingLeft();

		top = height / 2 - (arcDiameter / 2);
		left = width / 2 - (arcDiameter / 2);
		mTempRect.set(left, top, left + arcDiameter, top + arcDiameter);
	
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	public int getProgress() {
		return mProgress;
	}
	
	public void setStartProgress(int startAngle) {
		if(startAngle <= mStartAngle) {
			startAngle = mStartAngle;
		}
		
		mStartProgressAngle = startAngle;
		
	}
	
	public int getStartProgess() {
		return mStartProgressAngle;
	}
	
	public int getEndProgress(int endProgress) {
		return mEndProgressAngle;
	}
	
	public void setEndProgress(int endProgressAngle) {
		if(endProgressAngle >= mEndAngle) {
			endProgressAngle = mEndAngle;
		}
		mEndProgressAngle = endProgressAngle;
		invalidate();
	}
 
	public void setProgress(int progress) {
		int newStartProgress = 0;
		final int newEndProgress = (mEndAngle) * (int) ((float) progress) /100;
		
				
		newStartProgress = (mStartAngle + mEndAngle) - newEndProgress;
		

		
		if(mClockwise) {
			
			PropertyValuesHolder drawArc = PropertyValuesHolder.ofInt(
	                "endProgress", mEndProgressAngle, newEndProgress);
			mProgressAnimation =  ObjectAnimator.ofPropertyValuesHolder(this, drawArc).setDuration(30);
		} else  {
			//Drawing arc anti-clockwise so have to animate start angle and Drawing arc
			PropertyValuesHolder drawArc = PropertyValuesHolder.ofInt(
	                "endProgress", mEndProgressAngle, newEndProgress);
			PropertyValuesHolder startAngle = PropertyValuesHolder.ofInt(
	                "startProgress", mStartProgressAngle , newStartProgress);
			mProgressAnimation =  ObjectAnimator.ofPropertyValuesHolder(this,  startAngle, drawArc).setDuration(30);
				
		}
			
				
		((Activity) this.getContext()).runOnUiThread(new Runnable() {                        
            @Override
            public void run() {
                    mProgressAnimation.start();
                    setEndProgress(newEndProgress);
                    
            }
		});
		
		
		mProgressAnimation.addUpdateListener(new ObjectAnimator.AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float progress = (float) mEndProgressAngle / ( mEndAngle) * 100;
				
				
				if (mProgress != (int)progress) {
					mProgress = (int) progress + 1;					
					//Log.d(TAG, "Percentage progress" + (int)mProgress);
					if (mListener != null) {
						mListener.onProgressChange((int)mProgress);
					}
				}
				
			}
		});
				
	}
	
	public void setProgressListener(OnProgressListener progressListener) {
		mListener = progressListener;
	}
	

}
