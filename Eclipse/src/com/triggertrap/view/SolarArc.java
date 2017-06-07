package com.triggertrap.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Region;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.triggertrap.R;

/**
 * Custom View showing the Solar events of a particular day along with progress
 * towards the next event.
 * 
 * @author Scott Mellors
 * @version 13052014
 * @since 2.1
 */
public class SolarArc extends View {

	private static final int DENSITYOFFSET50 = 50;
	private static final int DENSTIYOFFSET10 = 10;
	private static final int DENSITYOFFSET5 = 5;
	private static final int DENSITYOFFSET35 = 35;
	private static final int DENSITYOFFSET15 = 15;
	private static final int DENSITYOFFSET25 = 25;
	private static final int CANVASDRAWPOS180 = 180;
	private static final int CANVASDRAWPOS200 = 200;
	private static final int CANVASDRAWPOS220 = 220;
	private static final int SHORT_ANIMATIONDURATION = 500;
	private static final int ANIMATIONDURATION = 1500;
	private static final int RADIUS_MOD = 3;
	private static final int DEGREE_MOD = 180;
	private static final int MIDNIGHT_HOUR = 23;
	private static final int MIDNIGHT_MIN = 59;
	private static final int MIDNIGHT_SEC = 59;
	private static final int MIDNIGHT_MS = 999;

	private int mSunXPos;
	private int mSunYPos;

	private int mSunAngle;

	// Attributes
	private int mArcRadius = 0;

	// Internal variables
	private RectF mArcRect = new RectF();
	private Paint mArcPaint;
	private Paint mFillerPaint;
	private Paint mGrayFillPaint;

	private boolean mIsAfterSunset;

	private float mDensity;

	private String mSunsetTime;
	private String mSunriseTime;
	private String mSunsetTwilightTime;
	private String mSunriseTwilightTime;

	private String mTimeLayoutValue;

	private Bitmap mGraySunBitmap;
	private Bitmap mRedSunBitmap;

	private ObjectAnimator mSunAnimator;

	private SimpleDateFormat mTimeFormat;

	public SolarArc(Context context, AttributeSet attrs) {
		super(context, attrs);


			boolean is24 = DateFormat.is24HourFormat(context);
			if (!is24) {
				mTimeFormat = new SimpleDateFormat("hh:mm", Locale.getDefault());
			} else {
				mTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
			}



		mSunsetTime = context.getString(R.string.undefined_time);
		mSunriseTime = context.getString(R.string.undefined_time);
		mSunsetTwilightTime = context.getString(R.string.undefined_time);
		mSunriseTwilightTime = context.getString(R.string.undefined_time);

		mDensity = context.getResources().getDisplayMetrics().density;

		mArcPaint = new Paint();
		mArcPaint.setColor(getResources().getColor(R.color.tt_dark_grey));
		mArcPaint.setPathEffect(new DashPathEffect(new float[] {
				DENSTIYOFFSET10 * mDensity, DENSITYOFFSET5 * mDensity }, 0));
		mArcPaint.setStyle(Paint.Style.STROKE);
		mArcPaint.setStrokeWidth(1 * mDensity);
		mArcPaint.setAntiAlias(true);

		mFillerPaint = new Paint();
		mFillerPaint.setColor(getResources().getColor(R.color.tt_red));
		mFillerPaint.setAntiAlias(true);

		mGrayFillPaint = new Paint();
		mGrayFillPaint.setColor(getResources().getColor(R.color.tt_dark_grey));
		mGrayFillPaint.setTextSize(DENSITYOFFSET15 * mDensity);
		mGrayFillPaint.setStyle(Paint.Style.FILL);
		mGrayFillPaint.setAntiAlias(true);
		mGrayFillPaint.setStrokeWidth(1 * mDensity);

		mGraySunBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.sun_icon);

		mRedSunBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.sun_icon_red);

		this.postInvalidate();
	}

	public void setIsAfterSunset(boolean isAfter) {
		mIsAfterSunset = isAfter;

		invalidate();
	}

	public int getSunAngle() {

		return mSunAngle;
	}

	public void setSunAngle(int angle) {

		this.mSunAngle = angle;

		mSunXPos = (int) (mArcRadius * Math.cos(Math.toRadians(angle)));
		mSunYPos = (int) (mArcRadius * Math.sin(Math.toRadians(angle)));

		invalidate();
	}

	public void toggleDayNightLayout() {

		if (mIsAfterSunset) {
			mIsAfterSunset = false;
		} else {
			mIsAfterSunset = true;
		}

		mSunAnimator = ObjectAnimator.ofInt(this, "sunAngle", 0);
		mSunAnimator.setDuration(0);
		mSunAnimator.start();

		invalidate();

	}

	public void progressSun(int percent) {

		// animate to new percentage
		mSunAnimator = ObjectAnimator.ofInt(this, "sunAngle", getSunAngle()
				+ percent);
		mSunAnimator.setDuration(SHORT_ANIMATIONDURATION);
		mSunAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
		mSunAnimator.start();
	}

	public void updateTime(Calendar twilightSunriseTime, Calendar sunriseTime,
			Calendar sunsetTime, Calendar twilightSunsetTime) {

		mSunriseTime = mTimeFormat.format(sunriseTime.getTime()).toString();
		mSunriseTwilightTime = mTimeFormat
				.format(twilightSunriseTime.getTime()).toString();
		mSunsetTime = mTimeFormat.format(sunsetTime.getTime()).toString();
		mSunsetTwilightTime = mTimeFormat.format(twilightSunsetTime.getTime())
				.toString();

		if (sunriseTime.before(sunsetTime)) {
			mIsAfterSunset = false;
		} else {
			mIsAfterSunset = true;
		}

		Calendar midnightTonight = (Calendar) sunsetTime.clone();
		midnightTonight.set(Calendar.HOUR_OF_DAY, MIDNIGHT_HOUR);
		midnightTonight.set(Calendar.MINUTE, MIDNIGHT_MIN);
		midnightTonight.set(Calendar.SECOND, MIDNIGHT_SEC);
		midnightTonight.set(Calendar.MILLISECOND, MIDNIGHT_MS);

		mSunAngle = 0;

		if (sunsetTime.before(sunriseTime)) {
			long timeDifference = sunriseTime.getTime().getTime()
					- sunsetTime.getTime().getTime();

			long differenceToNow = Calendar.getInstance().getTime().getTime()
					- sunsetTime.getTime().getTime();

			float fraction = (float) (((double) differenceToNow / (double) timeDifference));

			int percentageAsDegrees = (int) (fraction * DEGREE_MOD);

			mSunAnimator = ObjectAnimator.ofInt(this, "sunAngle",
					percentageAsDegrees);
		} else {
			long timeDifference = sunsetTime.getTime().getTime()
					- sunriseTime.getTime().getTime();

			long differenceToNow = Calendar.getInstance().getTime().getTime()
					- sunriseTime.getTime().getTime();

			float fraction = (float) (((double) differenceToNow / (double) timeDifference));

			int percentageAsDegrees = (int) (fraction * DEGREE_MOD);

			mSunAnimator = ObjectAnimator.ofInt(this, "sunAngle",
					percentageAsDegrees);
		}

		mSunAnimator.setDuration(ANIMATIONDURATION);
		mSunAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
		mSunAnimator.start();
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
		int arcDiameter = 0;

		if (mArcRadius != 0) {
			arcDiameter = mArcRadius * 2;
			top = height / 2 - (arcDiameter / 2);
			left = width / 2 - (arcDiameter / 2);
			mArcRect.set(left, top, left + arcDiameter, top + arcDiameter);
		} else {
			arcDiameter = min - getPaddingLeft();
			mArcRadius = arcDiameter / RADIUS_MOD;
			top = height / 2 - (arcDiameter / 2);
			left = width / 2 - (arcDiameter / 2);
			mArcRect.set(left, top, left + arcDiameter, top + arcDiameter);
		}

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if (mIsAfterSunset) {

			canvas.translate(0, mDensity * -DENSITYOFFSET50);
			canvas.drawArc(mArcRect, CANVASDRAWPOS180, -CANVASDRAWPOS180,
					false, mArcPaint);
			canvas.drawLine(mArcRect.left - (DENSITYOFFSET50 * mDensity),
					canvas.getHeight() / 2, mArcRect.right
							+ (DENSITYOFFSET50 * mDensity),
					canvas.getHeight() / 2, mArcPaint);

			canvas.save();
			canvas.clipRect(mArcRect.left, (canvas.getHeight() / 2) + 1,
					mArcRect.right, mArcRect.right, Region.Op.INTERSECT);
			canvas.drawArc(mArcRect, -CANVASDRAWPOS200, CANVASDRAWPOS220,
					false, mFillerPaint);
			canvas.restore();

			canvas.drawCircle(mArcRect.left, canvas.getHeight() / 2, mDensity
					* DENSITYOFFSET5, mGrayFillPaint);

			canvas.drawCircle(mArcRect.right, canvas.getHeight() / 2, mDensity
					* DENSITYOFFSET5, mGrayFillPaint);

			canvas.drawText(mSunriseTime, mArcRect.left
					- (mDensity * (DENSITYOFFSET50 - 2)),
					(canvas.getHeight() / 2) - (mDensity * DENSITYOFFSET5),
					mGrayFillPaint);
			canvas.drawText(mSunriseTwilightTime, mArcRect.left
					- (mDensity * (DENSITYOFFSET50 - 2)),
					(canvas.getHeight() / 2) + (mDensity * DENSITYOFFSET35),
					mGrayFillPaint);

			canvas.drawText(mSunsetTime, mArcRect.right
					+ (mDensity * DENSTIYOFFSET10), (canvas.getHeight() / 2)
					- (mDensity * DENSITYOFFSET5), mGrayFillPaint);
			canvas.drawText(mSunsetTwilightTime, mArcRect.right
					+ (mDensity * DENSTIYOFFSET10), (canvas.getHeight() / 2)
					+ (mDensity * DENSITYOFFSET35), mGrayFillPaint);

			canvas.translate(mArcRect.centerX()
					- (mGraySunBitmap.getWidth() / 2), mArcRect.centerY()
					- (mGraySunBitmap.getHeight() / 2));

			canvas.drawBitmap(mGraySunBitmap, mSunXPos, mSunYPos,
					mGrayFillPaint);

		} else {

			canvas.translate(0, mDensity * DENSITYOFFSET25);

			canvas.drawArc(mArcRect, -CANVASDRAWPOS180, CANVASDRAWPOS180,
					false, mArcPaint);
			canvas.drawLine(mArcRect.left - (DENSITYOFFSET50 * mDensity),
					canvas.getHeight() / 2, mArcRect.right
							+ (DENSITYOFFSET50 * mDensity),
					canvas.getHeight() / 2, mArcPaint);
			canvas.save();
			canvas.clipRect(mArcRect.left, (canvas.getHeight() / 2) + 1,
					mArcRect.right, mArcRect.right, Region.Op.INTERSECT);
			canvas.drawArc(mArcRect, -CANVASDRAWPOS200, CANVASDRAWPOS220,
					false, mFillerPaint);
			canvas.restore();

			canvas.drawCircle(mArcRect.left, canvas.getHeight() / 2, mDensity
					* DENSITYOFFSET5, mGrayFillPaint);

			canvas.drawCircle(mArcRect.right, canvas.getHeight() / 2, mDensity
					* DENSITYOFFSET5, mGrayFillPaint);

			canvas.drawText(mSunriseTime, mArcRect.left
					- (mDensity * (DENSITYOFFSET50 - 2)),
					(canvas.getHeight() / 2) - (mDensity * DENSITYOFFSET5),
					mGrayFillPaint);
			canvas.drawText(mSunriseTwilightTime, mArcRect.left
					- (mDensity * (DENSITYOFFSET50 - 2)),
					(canvas.getHeight() / 2) + (mDensity * DENSITYOFFSET35),
					mGrayFillPaint);

			canvas.drawText(mSunsetTime, mArcRect.right
					+ (mDensity * DENSTIYOFFSET10), (canvas.getHeight() / 2)
					- (mDensity * DENSITYOFFSET5), mGrayFillPaint);
			canvas.drawText(mSunsetTwilightTime, mArcRect.right
					+ (mDensity * DENSTIYOFFSET10), (canvas.getHeight() / 2)
					+ (mDensity * DENSITYOFFSET35), mGrayFillPaint);

			canvas.translate(mArcRect.centerX()
					- (mGraySunBitmap.getWidth() / 2), mArcRect.centerY()
					- (mGraySunBitmap.getHeight() / 2));

			canvas.drawBitmap(mRedSunBitmap, -mSunXPos, -mSunYPos,
					mGrayFillPaint);
		}
	}

}
