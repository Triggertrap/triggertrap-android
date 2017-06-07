package com.triggertrap.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.triggertrap.R;

public class OngoingButton extends View {

    private static final String TAG = OngoingButton.class.getSimpleName();

    private static final int DEFAULT_OUTER_WIDTH = 12;
    private static final int CIRCLE_BOARDER_WIDTH = 2;
    private static final int ANIM_FINAL_RADIUS = 85;
    private static final int ANIMATION_DURATION = 1000;

    private float mOuterCircleWidth = DEFAULT_OUTER_WIDTH;
    private boolean mToggle = false;
    private boolean mOneShot = false;
    private int mAnimationTime = ANIMATION_DURATION;
    private Paint mCirclePaint;
    private Paint mCircleBoarderPaint;
    private Paint mOuterCirclePaint;
    private Paint mFirstCirclePaint;
    private Paint mSecondCirclePaint;
    private Paint mThirdCirclePaint;
    private RectF mRectCircle = new RectF();
    private RectF mRectOuterCircle = new RectF();
    private int mInnerGap = 13;
    private int mRadius = 65;
    private int mCircleBoarderWidth;
    private float mCx;
    private float mCy;

    //Attributes for the animated circles
    private float mStartCircleRadius;
    private float mFinalCircleRadius;
    private float mFirstCircleRadius;
    private float mSecondCircleRadius;
    private float mThirdCircleRadius;
    private float mFirstCircleAlpha = 125;
    private float mThirdCircleStartRadius;
    private float mSecondCircleAlpha = 125;
    private float mThirdCircleAlpha = 125;

    private float mFirstCircleStroke = DEFAULT_OUTER_WIDTH;
    private float mSecondCircleStroke = DEFAULT_OUTER_WIDTH;

    private ObjectAnimator mFirstCircleAnimator = null;
    private ObjectAnimator mSecondCircleAnimator = null;
    private ObjectAnimator mThirdCircleAnimator;
    private AnimatorSet animSet = new AnimatorSet();

    private int mState = State.STOPPED;

    private OnToggleListener mToggleListener = null;
    private OnTouchListener mTouchListener = null;
    private boolean mIgnoreTouch = true;

    private interface State {
        public static final int ONGOING = 0;
        public static final int STOPPED = 1;
    }

    public interface OnToggleListener {
        public void onToggleOn();

        public void onToggleOff();
    }

    public interface OnTouchListener {
        public void onTouchDown();

        public void onTouchUp();
    }

    public OngoingButton(Context context) {
        super(context);
        init(context, null, 0);
    }

    public OngoingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public OngoingButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        final Resources res = getResources();
        float density = context.getResources().getDisplayMetrics().density;
        Log.d(TAG, "Denisty of the screen is: " + density);

        //Convert progress width to pixels for current density
        mOuterCircleWidth = (int) (mOuterCircleWidth * density);
        mInnerGap = (int) (mInnerGap * density);
        mRadius = (int) (mRadius * density);
        mCircleBoarderWidth = (int) (CIRCLE_BOARDER_WIDTH * density);
        mFinalCircleRadius = (int) (ANIM_FINAL_RADIUS * density);

        int circleColor = res.getColor(R.color.tt_red);
        int circleBoarderColor = res.getColor(R.color.tt_button_circle_border_color);
        int outerColor = res.getColor(R.color.tt_button_outer_circle_color);

        if (attrs != null) {
            // Attribute initialization
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.OnGoingButton, defStyle, 0);
            mToggle = a.getBoolean(R.styleable.OnGoingButton_toggle, mToggle);
            mOneShot = a.getBoolean(R.styleable.OnGoingButton_oneShot, mOneShot);
            mAnimationTime = a.getInt(R.styleable.OnGoingButton_animTime, mAnimationTime);

        }
        mCirclePaint = new Paint();
        mCirclePaint.setColor(circleColor);
        mCirclePaint.setAntiAlias(true);


        mCircleBoarderPaint = new Paint();
        mCircleBoarderPaint.setColor(circleBoarderColor);
        mCircleBoarderPaint.setAntiAlias(true);
        mCircleBoarderPaint.setStyle(Paint.Style.STROKE);
        mCircleBoarderPaint.setStrokeWidth(mCircleBoarderWidth);


        mOuterCirclePaint = new Paint();
        mOuterCirclePaint.setColor(outerColor);
        mOuterCirclePaint.setAntiAlias(true);
        mOuterCirclePaint.setStyle(Paint.Style.STROKE);
        mOuterCirclePaint.setStrokeWidth(mOuterCircleWidth);

        mFirstCirclePaint = new Paint();
        mFirstCirclePaint.setColor(circleColor);
        mFirstCirclePaint.setAntiAlias(true);
        mFirstCirclePaint.setAlpha((int) mFirstCircleAlpha);
        mFirstCirclePaint.setStyle(Paint.Style.STROKE);
        mFirstCirclePaint.setStrokeWidth(mOuterCircleWidth);

        mSecondCirclePaint = new Paint();
        mSecondCirclePaint.setColor(circleColor);
        mSecondCirclePaint.setAntiAlias(true);
        mSecondCirclePaint.setAlpha((int) mSecondCircleAlpha);
        mSecondCirclePaint.setStyle(Paint.Style.STROKE);
        mSecondCirclePaint.setStrokeWidth(mOuterCircleWidth);

        mThirdCirclePaint = new Paint();
        mThirdCirclePaint.setColor(circleBoarderColor);
        mThirdCirclePaint.setAntiAlias(true);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawArc(mRectOuterCircle, 0, 360, false, mOuterCirclePaint);

        //Animated circles (outer)
        canvas.drawCircle(mCx, mCy, mFirstCircleRadius, mFirstCirclePaint);
        canvas.drawCircle(mCx, mCy, mSecondCircleRadius, mSecondCirclePaint);


        canvas.drawArc(mRectCircle, 0, 360, true, mCirclePaint);
        canvas.drawArc(mRectCircle, 0, 360, true, mCircleBoarderPaint);

        //Animated circle in center
        canvas.drawCircle(mCx, mCy, mThirdCircleRadius, mThirdCirclePaint);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int arcDiameter = mRadius * 2;
        float top = (height / 2) - mRadius;
        float left = (width / 2) - mRadius;

        mRectOuterCircle.set(left, top, left + arcDiameter, top + arcDiameter);
        mRectCircle.set(left + mInnerGap, top + mInnerGap, left + arcDiameter - mInnerGap, top + arcDiameter - mInnerGap);

        mCx = width / 2;
        mCy = height / 2;
        mStartCircleRadius = mRadius - mInnerGap;

        mThirdCircleStartRadius = (float) mRadius / 4;

        setUpAnimations();

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        animSet.end();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

    }

    private void setUpAnimations() {
        if (mFirstCircleAnimator == null) {
            //Check for null since we only want to do this once!.
            //Set up the animations
            PropertyValuesHolder pvhFirstCircleRadius = PropertyValuesHolder.ofFloat(
                    "firstCircleRadius", mStartCircleRadius, mFinalCircleRadius);
            PropertyValuesHolder pvhFirstCircleAlpha = PropertyValuesHolder.ofFloat(
                    "firstCircleAlpha", 125, 0);
            PropertyValuesHolder pvhFirstCircleStroke = PropertyValuesHolder.ofFloat(
                    "firstCircleStroke", DEFAULT_OUTER_WIDTH, (DEFAULT_OUTER_WIDTH * 2));
            mFirstCircleAnimator = ObjectAnimator.ofPropertyValuesHolder(this, pvhFirstCircleAlpha, pvhFirstCircleStroke, pvhFirstCircleRadius).setDuration(mAnimationTime);
            mFirstCircleAnimator.setInterpolator(new DecelerateInterpolator(1.1f));
            if (!mOneShot) {
                mFirstCircleAnimator.setRepeatCount(ValueAnimator.INFINITE);
                mFirstCircleAnimator.setRepeatMode(ValueAnimator.RESTART);
            }

            PropertyValuesHolder pvhSecondCircleRadius = PropertyValuesHolder.ofFloat(
                    "secondCircleRadius", mStartCircleRadius, mFinalCircleRadius);
            PropertyValuesHolder pvhSecondCircleAlpha = PropertyValuesHolder.ofFloat(
                    "secondCircleAlpha", 125, 0);
            PropertyValuesHolder pvhSecondCircleStroke = PropertyValuesHolder.ofFloat(
                    "secondCircleStroke", DEFAULT_OUTER_WIDTH, (DEFAULT_OUTER_WIDTH * 2));
            mSecondCircleAnimator = ObjectAnimator.ofPropertyValuesHolder(this, pvhSecondCircleAlpha, pvhSecondCircleStroke, pvhSecondCircleRadius).setDuration(mAnimationTime);
            mSecondCircleAnimator.setInterpolator(new DecelerateInterpolator(1.3f));
            if (!mOneShot) {
                mSecondCircleAnimator.setRepeatCount(ValueAnimator.INFINITE);
                mSecondCircleAnimator.setRepeatMode(ValueAnimator.RESTART);
            }
            mSecondCircleAnimator.setStartDelay(mAnimationTime / 3);
            animSet.play(mFirstCircleAnimator).with(mSecondCircleAnimator);


            PropertyValuesHolder pvhThirdCircleRadius = PropertyValuesHolder.ofFloat(
                    "thirdCircleRadius", mThirdCircleStartRadius, mStartCircleRadius);
            PropertyValuesHolder pvhthirdCircleAlpha = PropertyValuesHolder.ofFloat(
                    "thirdCircleAlpha", 0, 255);
            mThirdCircleAnimator = ObjectAnimator.ofPropertyValuesHolder(this, pvhthirdCircleAlpha, pvhThirdCircleRadius).setDuration(300);

            if (mState == State.ONGOING) {
                animSet.start();
            }

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onStartAnimation(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                onStopAnimation(event.getX(), event.getY());
                setPressed(false);
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return true;
    }

    public void setToggleListener(OnToggleListener toggleListener) {
        mToggleListener = toggleListener;
    }

    public void setTouchListener(OnTouchListener touchListener) {
        mTouchListener = touchListener;
    }

    private void onStartAnimation(float xPos, float yPos) {
        if (!ignoreTouch(xPos, yPos)) {
            mIgnoreTouch = false;
            if (mOneShot) {
                animSet.start();
                mThirdCircleAnimator.start();
                if (mTouchListener != null) {
                    mTouchListener.onTouchDown();
                }
                return;
            }

            if (!mToggle) {
                animSet.start();
                if (mTouchListener != null) {
                    mTouchListener.onTouchDown();
                }

            } else {
                if (mState == State.ONGOING) {
                    animSet.end();
                    mState = State.STOPPED;
                    if (mToggleListener != null) {
                        mToggleListener.onToggleOff();
                    }
                } else {
                    animSet.start();
                    mState = State.ONGOING;
                    if (mToggleListener != null) {
                        mToggleListener.onToggleOn();
                    }
                }
            }
            mThirdCircleAnimator.start();

        } else {
            mIgnoreTouch = true;
        }
    }


    private void onStopAnimation(float xPos, float yPos) {

        if (!mToggle) {
            animSet.end();
            if (mTouchListener != null) {
                mTouchListener.onTouchUp();
            }

        }
        if (mIgnoreTouch == false) {
            mThirdCircleAnimator.reverse();
        }

    }

    public void startAnimation() {
       mState = State.ONGOING;
       animSet.start();
    }

    public void stopAnimation() {
        animSet.end();
        mState = State.STOPPED;
    }

    private boolean ignoreTouch(float xPos, float yPos) {
        boolean ignore = true;
        if (mRectOuterCircle.contains(xPos, yPos)) {
            ignore = false;
        }
        return ignore;
    }


    public void setFirstCircleRadius(float firstCircleRadius) {
        mFirstCircleRadius = firstCircleRadius;
        invalidate();
    }

    public float getFirstCirclRadius() {
        return mFirstCircleRadius;
    }


    public void setFirstCircleAlpha(float firstCircleAlpha) {
        mFirstCircleAlpha = firstCircleAlpha;
        mFirstCirclePaint.setAlpha((int) mFirstCircleAlpha);

    }

    public float getFirstCircleAlpha() {
        return mFirstCircleAlpha;
    }

    public void setFirstCircleStroke(float firstCircleStorke) {
        mFirstCircleStroke = firstCircleStorke;
        mFirstCirclePaint.setStrokeWidth(mFirstCircleStroke);

    }

    public float getFirstCircleStroke() {
        return mFirstCircleStroke;
    }

    public void setSecondCircleRadius(float secondCircleRadius) {
        mSecondCircleRadius = secondCircleRadius;
        invalidate();
    }

    public float getSecondCircleRadius() {
        return mSecondCircleRadius;
    }


    public void setSecondCircleAlpha(float secondCircleAlpha) {
        mSecondCircleAlpha = secondCircleAlpha;
        mSecondCirclePaint.setAlpha((int) mSecondCircleAlpha);

    }

    public float getSecondCircleAlpha() {
        return mSecondCircleAlpha;
    }

    public void setSecondCircleStroke(float secondCircleStorke) {
        mSecondCircleStroke = secondCircleStorke;
        mSecondCirclePaint.setStrokeWidth(mSecondCircleStroke);

    }

    public float getSecondCircleStroke() {
        return mSecondCircleStroke;
    }


    public void setThirdCircleRadius(float thirdCircleRadius) {
        mThirdCircleRadius = thirdCircleRadius;
        invalidate();
    }

    public float getThirdCircleRadius() {
        return mThirdCircleRadius;
    }


    public void setThirdCircleAlpha(float thirdCircleAlpha) {
        mThirdCircleAlpha = thirdCircleAlpha;
        mThirdCirclePaint.setAlpha((int) mThirdCircleAlpha);

    }

    public float getThirdCircleAlpha() {
        return mThirdCircleAlpha;
    }


}
