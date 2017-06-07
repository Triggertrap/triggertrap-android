package com.triggertrap.widget;


import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.triggertrap.R;


public class BezierWidget extends View {

    private static final String TAG = BezierWidget.class.getSimpleName();

    private int mX1 = 0, mY1 = 0;
    private int mX2 = 0, mY2 = 0;


    private Path curvePath = new Path();
    private RectF frame = new RectF();
    private int mFrameStrokeWidth = 1;
    private int mCurveStrokeWidth = 3;
    private int mTouchCircleRadius = 10;

    private final Paint mCurvePaint = new Paint();
    private final Paint mLinePaint = new Paint();
    private final Paint mCirclePaint = new Paint();
    private final Paint mCircleOutlinePaint = new Paint();
    private final Paint mFramePaint = new Paint();

    private int mFrameWidth, mFrameHeight;
    private int mFrameXpos, mFrameYpos;

    private int mTranslateXOffset, mTranslateYOffset;
    private int mInnerFramePadding = 13;
    private int mInnerFrameWidth, mInnerFrameHeight;

    ArrayList<Float> mOverlapPoints = null;

    private OnControlChangeListener mListener;


    public interface OnControlChangeListener {
        public void onControlChanged(float x1, float y1, float x2, float y2);
    }

    public BezierWidget(Context context) {
        super(context);
        init(context, null, 0);
    }

    public BezierWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public BezierWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        final Resources res = getResources();

        float density = context.getResources().getDisplayMetrics().density;

        //Adjust for density
        mFrameStrokeWidth = (int) (mFrameStrokeWidth * density);
        mCurveStrokeWidth = (int) (mCurveStrokeWidth * density);
        mTouchCircleRadius = (int) (mTouchCircleRadius * density);
        mInnerFramePadding = (int) (mInnerFramePadding * density);

        int curveColor = res.getColor(R.color.tt_red);
        int lineColor = res.getColor(R.color.tt_medium_grey);
        int frameColor = res.getColor(R.color.tt_dark_grey);
        int circleColor = res.getColor(R.color.tt_medium_grey);

        mCurvePaint.setColor(curveColor);
        mCurvePaint.setAntiAlias(true);
        mCurvePaint.setStyle(Paint.Style.STROKE);
        mCurvePaint.setStrokeCap(Paint.Cap.ROUND);
        mCurvePaint.setStrokeWidth(mCurveStrokeWidth);


        mFramePaint.setColor(frameColor);
        mFramePaint.setAntiAlias(true);
        mFramePaint.setStyle(Paint.Style.STROKE);

        mFramePaint.setStrokeWidth(mFrameStrokeWidth);

        mLinePaint.setColor(lineColor);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(mFrameStrokeWidth);

        mCirclePaint.setColor(circleColor);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStrokeWidth(mFrameStrokeWidth);

        mCircleOutlinePaint.setColor(frameColor);
        mCircleOutlinePaint.setAntiAlias(true);
        mCircleOutlinePaint.setStyle(Paint.Style.STROKE);
        mCircleOutlinePaint.setStrokeWidth(mFrameStrokeWidth);

//		mX2 = 200;
//		mY2 = 0;
	}


	@Override
	protected void onDraw(Canvas canvas) {

		frame.set(mFrameXpos + mFrameStrokeWidth/2,mFrameYpos + mFrameStrokeWidth/2, mFrameXpos + mFrameWidth - mFrameStrokeWidth/2, mFrameYpos + mFrameHeight- mFrameStrokeWidth/2);
		canvas.drawRoundRect(frame, 30,30, mFramePaint);
		
		canvas.translate(mTranslateXOffset + mInnerFramePadding, mTranslateYOffset +mInnerFramePadding);

        canvas.drawLine(0, mInnerFrameHeight, mX1, mY1, mLinePaint);
        canvas.drawLine(mInnerFrameWidth, 0, mX2, mY2, mLinePaint);

		curvePath.reset();
		curvePath.moveTo(0, mInnerFrameHeight);
		curvePath.cubicTo(mX1, mY1,mX2, mY2, mInnerFrameWidth, 0);
		canvas.drawPath(curvePath, mCurvePaint);
		
		drawOverlaps(canvas);


        //Control 1
        canvas.drawCircle(mX1, mY1, mTouchCircleRadius, mCirclePaint);
        canvas.drawCircle(mX1, mY1, mTouchCircleRadius, mCircleOutlinePaint);

        //Control 2
        canvas.drawCircle(mX2, mY2, mTouchCircleRadius, mCirclePaint);
        canvas.drawCircle(mX2, mY2, mTouchCircleRadius, mCircleOutlinePaint);
	}
	
	
	private void drawOverlaps(Canvas canvas) {
		//mInterpolator.getTimeIntervals(sequenceDuration, count, exposure, gap)
		if (mOverlapPoints != null && mOverlapPoints.size() != 0) {
			int count = mOverlapPoints.size()/2;
			
			for (int i = 0; i < count; i++) {
				try {
					float xNormCoord = mOverlapPoints.get(i * 2);
					float yNormCoord = mOverlapPoints.get((i * 2) + 1);
					float xCoord = xNormCoord * mInnerFrameWidth;
					float yCoord = (1 - yNormCoord) * mInnerFrameHeight;

					canvas.drawCircle(xCoord, yCoord, 20, mCurvePaint);
				} catch (IndexOutOfBoundsException exp) {
					// Don't so anything if we haven't got overlap points.
				} catch (NullPointerException exp) {
					// Don't so anything if we haven't got overlap points.
				}

			}
			
		}
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int maxHeight = getDefaultSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		final int maxWidth = getDefaultSize(getSuggestedMinimumWidth(),
				widthMeasureSpec);
		int paddingTop = getPaddingTop();
		int paddingBottom = getPaddingBottom();
		int paddingLeft = getPaddingLeft();
		int paddingRight = getPaddingRight();
		
		final int width = maxWidth - paddingLeft - paddingRight;
		final int height = maxHeight - paddingTop - paddingBottom;

        final int min = Math.min(width, height);

        mFrameWidth = mFrameHeight = min;

        mInnerFrameWidth = mFrameWidth - mInnerFramePadding * 2 - mFrameStrokeWidth;
        mInnerFrameHeight = mFrameHeight - mInnerFramePadding * 2 - mFrameStrokeWidth;

        //Log.d(TAG, "Frame width and height: " +mFrameWidth);

        int frameXCenter = (width / 2) + paddingLeft;
        int frameYCenter = (height / 2) + paddingTop;
        //Log.d(TAG, " Frame center: x:" + frameXCenter + " y:" +frameYCenter);

        mFrameXpos = frameXCenter - (mFrameWidth / 2);

        mFrameYpos = frameYCenter - (mFrameHeight / 2);


        //Log.d(TAG, "Frame pos x:" + mFrameXpos + " y:" + mFrameYpos);

        mTranslateXOffset = mFrameXpos;
        mTranslateYOffset = mFrameYpos;

//		mX1 = mInnerFrameWidth/2;
//		mY1 = mInnerFrameHeight;
//		mX2 = mInnerFrameWidth/2;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setControlPoints(float x1Norm, float y1Norm, float x2Norm, float y2Norm) {
        //Log.d(TAG,"X1Norm: " + x1Norm + " Y1Norm:" + y1Norm + " X2Norm:" + x2Norm + " Y2Norm" + y2Norm);
        mX1 = (int) (mInnerFrameWidth * x1Norm);
        mY1 = mInnerFrameHeight - (int) (mInnerFrameHeight * y1Norm);
        mX2 = (int) (mInnerFrameWidth * x2Norm);
        mY2 = mInnerFrameHeight - (int) (mInnerFrameHeight * y2Norm);


        Log.d(TAG, "X1: " + mX1 + " Y1:" + mY1 + " X2:" + mX2 + " Y2" + mY2);
        if (mListener != null) {
            mListener.onControlChanged(x1Norm, y1Norm, x2Norm, y2Norm);
        }
        this.invalidate();
    }

    public float[] getControlPoints() {

        // Normalise coords to 0-1 values
        float x1Norm = (float) mX1 / mInnerFrameWidth;
        float y1Norm = (float) mY1 / mInnerFrameHeight;
        float x2Norm = (float) mX2 / mInnerFrameWidth;
        float y2Norm = (float) mY2 / mInnerFrameHeight;

        // Switch origin to bottom left corner
        y1Norm = 1 - y1Norm;
        y2Norm = 1 - y2Norm;
        float[] controlPoints = {x1Norm, y1Norm, x2Norm, y2Norm};
        return controlPoints;
    }

    public void setOverlapPoints(ArrayList<Float> overlapPoints) {
        mOverlapPoints = overlapPoints;
        invalidate();
    }

    private void updateOnTouch(MotionEvent event) {

        float[] pts = new float[2];
        pts[0] = (int) event.getX();
        pts[1] = (int) event.getY();

        Matrix inverse = new Matrix();
        this.getMatrix().invert(inverse);

        inverse.mapPoints(pts);
        int xPos = (int) pts[0] - mTranslateXOffset - mInnerFramePadding;
        int yPos = (int) pts[1] - mTranslateYOffset - mInnerFramePadding;

        //Are we touching the first control point?
        if (Math.pow((xPos - mX1), 2) + Math.pow((yPos - mY1), 2) < Math.pow(mTouchCircleRadius + 60, 2)) {
            if ((xPos > 0) && (xPos <= mInnerFrameWidth)) {
                mX1 = (int) xPos;
                invalidate();
            }
            if (yPos > 0 && yPos <= mInnerFrameHeight) {
                mY1 = (int) yPos;
                invalidate();
            }


            return;
        }

        //Are we touch the second control point?
        if (Math.pow((xPos - mX2), 2) + Math.pow((yPos - mY2), 2) < Math.pow(mTouchCircleRadius + 60, 2)) {
            if ((xPos > 0) && (xPos <= mInnerFrameWidth)) {
                mX2 = xPos;
                invalidate();
//				if(mListener != null) {
//					float[]normPts =  getControlPoints();
//					mListener.onControlChanged(normPts[0], normPts[1], normPts[2], normPts[3]);
//				}
            }

            if (yPos > 0 && yPos <= mInnerFrameHeight) {
                mY2 = yPos;
                invalidate();
//				if(mListener != null) {
//					float[]normPts =  getControlPoints();
//					mListener.onControlChanged(normPts[0], normPts[1], normPts[2], normPts[3]);
//				}
            }
            return;
        }


    }

    private void onTouchUp() {
        //Normalise cords to 0-1 values and update listener
        float x1Norm = (float) mX1 / mInnerFrameWidth;
        float y1Norm = (float) mY1 / mInnerFrameHeight;
        float x2Norm = (float) mX2 / mInnerFrameWidth;
        float y2Norm = (float) mY2 / mInnerFrameHeight;

        // Switch origin to bottom left corner
        y1Norm = 1 - y1Norm;
        y2Norm = 1 - y2Norm;

        Log.d(TAG, "X1: " + x1Norm + " Y1:" + y1Norm + " X2:" + x2Norm + " Y2:" + y2Norm);
        if (mListener != null) {
            mListener.onControlChanged(x1Norm, y1Norm, x2Norm, y2Norm);
        }
    }

    private void onTouchDown(MotionEvent event) {
        if (mOverlapPoints != null) {
            mOverlapPoints.clear();
        }
        updateOnTouch(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                updateOnTouch(event);
                break;
            case MotionEvent.ACTION_UP:
                onTouchUp();
                break;
            case MotionEvent.ACTION_CANCEL:


                break;
        }

        return true;
    }

    public void setListener(OnControlChangeListener listener) {
        mListener = listener;
    }


}
