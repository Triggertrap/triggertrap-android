package com.triggertrap;

import com.triggertrap.fragments.preference.SettingsFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.preference.PreferenceManager;

public class TTApp {

	// Typefaces
	public Typeface SAN_SERIF_LIGHT = null;
	public Typeface SAN_SERIF_THIN = null;

	// Shared preference keys
	private static final String TT_PREFS = "triggertrap_prefs";
	private static final String TT_MODE = "tt_mode";
	private static final String TT_IS_FIRST_LAUNCH = "tt_is_first_launch";
	private static final String TT_LAST_FRAGMENT = "tt_last_fragment";
	private static final String TT_LAST_ACTION_BAR_LABEL = "tt_last_action_bar_label";
	private static final String TT_LAST_LIST_ITEM_CHECKED = "tt_last_list_item_checked";
	private static final String TT_TIMELASPE_INTERVAL = "tt_timelaspe_interval";
	private static final String TT_TIME_MODE_TIME = "tt_time_mode_time";
	private static final String TT_SELF_TIME_MODE_TIME = "tt_self_time_mode_time";
	private static final String TT_SOUND_SENSOR_THRESHOLD = "tt_sound_sensor_threshold";
	private static final String TT_SOUND_SENSOR_SENSITIVTY = "tt_sound_sensor_sensitivty";
	private static final String TT_STAR_TRAIL_ITERATIONS = "tt_star_trail_interations";
	private static final String TT_STAR_TRAIL_EXPOSURE = "tt_star_trail_exposure";
	private static final String TT_STAR_TRAIL_GAP = "tt_star_trail_gap";
	private static final String TT_HDR_MIDDLE_EXP = "tt_hdr_middle_exp";
	private static final String TT_HDR_NUM_EXP = "tt_hdr_num_exp";
	private static final String TT_HDR_EV_STEP = "tt_hdr_ev_step";
	private static final String TT_HDR_TIMELPASE_MIDDLE_EXP = "tt_hdr_timelapse_middle_exp";
	private static final String TT_HDR_TIMELPASE_INTERVAL = "tt_hdr_timelapse_interval";
	private static final String TT_HDR_TIMELPASE_EV_STEP = "tt_hdr_timelapse_ev_step";
	private static final String TT_BRAMPING_ITERATIONS = "tt_bramping_iterations";
	private static final String TT_BRAMPING_INTERVAL = "tt_bramping_interval";
	private static final String TT_BRAMPING_START_EXP = "tt_bramping_start_exp";
	private static final String TT_BRAMPING_END_EXP = "tt_bramping_end_exp";
	private static final String TT_DISTANCELAPSE_DISTANCE = "tt_distancelapse_distance";
	private static final String TT_TIMEWARP_ITERATIONS = "tt_timewarp_iterations";
	private static final String TT_TIMEWARP_DURATION = "tt_timewarp_duration";
	private static final String TT_TIMEWARP_CONTROL1_X = "tt_timewarp_control1_x";
	private static final String TT_TIMEWARP_CONTROL1_Y = "tt_timewarp_control1_y";
	private static final String TT_TIMEWARP_CONTROL2_X = "tt_timewarp_control2_x";
	private static final String TT_TIMEWARP_CONTROL2_Y = "tt_timewarp_control2_y";
	private static final String TT_WIFI_SLAVE_LAST_MASTER = "tt_wifi_slave_last_master";
	private static final String TT_WIFI_MASTER_IS_ON = "tt_wifi_master_is_on";
	private static final String TT_SHOW_DIALOG_AGAIN = "tt_show_dialog_again";
	private static final String TT_LAUNCH_COUNT = "tt_launch_count";
	private static final String TT_FIRST_LAUNCH_DATE = "tt_first_launch_date";

	private static final String TT_DEFAULT_SHUTTER_SPEED = "tt_default_shutter_speed";

	private static final String LAST_FRAGMENT_DEFAULT = FragmentTags.GETTING_STARTED;
	private static final int LAST_LIST_ITEM_CHECKED_DEFAULT = 1;
	private final String LAST_ACTION_BAR_LABEL_DEFAULT;
	private static final long CAMERA_BEEP_LENGTH_DEFAULT = 150;
	public static final int TIMELASPE_INTERVAL_DEFAULT = 1000;
	public static final int TIME_MODE_TIME_DEFAULT = 30000;
    public static final int SELF_TIME_MODE_TIME_DEFAULT = 30000;
	public static final int SOUND_SENSOR_THRESHOLD_DEFAULT = 50;
	public static final int SOUND_SENSOR_SENSITIVTY_DEFAULT = 50;
	public static final int STAR_TRAIL_ITERATIONS_DEFAULT = 10;
	public static final long STAR_TRAIL_EXPOSURE_DEFAULT = 90000;
	public static final long STAR_TRAIL_GAP_DEFAULT = 5000;
	public static final long LE_HDR_MIDDLE_EXPOSURE_DEFAULT = 2000;
	public static final int LE_HDR_NUM_EXPOSURES_DEFAULT = 3;
	public static final float LE_HDR_EV_STEP_DEFAULT = 0.5f;
	public static final long LE_HDR_TIMELPASE_MIDDLE_EXPOSURE_DEFAULT = 2000;
	public static final long LE_HDR_TIMELPASE_INTERVAL_DEFAULT = 10000;
	public static final float LE_HDR_TIMELPASE_EV_STEP_DEFAULT = 0.5f;
	public static final int BRAMPING_INTERATION_DEFAULT = 360;
	public static final long BRAMPING_INTERVAL_DEFAULT = 10000;
	public static final long BRAMPING_START_EXPOSURE_DEFAULT = 2000;
	public static final long BRAMPING_END_EXPOSURE_DEFAULT = 8000;
	public static final int DISTANCELAPSE_DISTANCE_DEFAULT = 25;
	public static final int TIMEWARP_INTERATIONS_DEAFAULT = 100;
	public static final long TIMEWARP_DURATION_DEFAULT = 3600000;
	public static final float TIMEWARP_CONTROL1_X_DEFAULT = 0.5f;
	public static final float TIMEWARP_CONTROL1_Y_DEFAULT = 0.0f;
	public static final float TIMEWARP_CONTROL2_X_DEFAULT = 0.5f;
	public static final float TIMEWARP_CONTROL2_Y_DEFAULT = 1.0f;
	public static final String WIFI_SLAVE_LAST_MASTER_DEFAULT = "";
	public static final Boolean WIFI_MASTER_IS_ON_DEFAULT = false;

	public static final Boolean SHOW_DIALOG_AGAIN_DEFAULT = true;
	public static final int LAUNCH_COUNT_DEFAULT = 0;
	public static final long FIRST_LAUNCH_DEFAULT = 0;
	public static final int ND_FILTER_SHUTTER_SPEED_DEFAULT = 0;

	private static final int UNINTIALISED = -1;
	private static final String UNINTIALISED_STRING = null;
	private static final Boolean UNINTIALISED_BOOL = null;

	private Context mAppContext;

	private String mLastFragmentTag = UNINTIALISED_STRING;
	private String mLastActionBarLabel = UNINTIALISED_STRING;
	private int mLastListItemChecked = UNINTIALISED;

	// Gap between beeps in millisconds
	private static final long BEEP_GAP = 750;
	private static final long HDR_GAP = 1000;

	private long mBeepLength = UNINTIALISED;
	// Time lapse interval in milliseconds.
	private long mTimeLapseInterval = UNINTIALISED;
	// Timed mode time in milliseconds
	private long mTimedModeTime = UNINTIALISED;
    //Self Timer time in milliseconds
    private long mSelfTimedModeTime = UNINTIALISED;
	// Sound sensor values
	private int mSensorResetDelay = UNINTIALISED;
	private int mSensorDelay = UNINTIALISED;
	private int mSoundSensorThreshold = UNINTIALISED;
	private int mSoundSensorSensitivity = UNINTIALISED;

	// Star trial values
	private int mStarTrailInterations = UNINTIALISED;
	private long mStarTrailExposure = UNINTIALISED;
	private long mStarTrailGap = UNINTIALISED;

	// HDR values
	private long mHdrMiddleExposure = UNINTIALISED;
	private int mHdrNumberExposures = UNINTIALISED;
	private float mHdrEvStep = UNINTIALISED;

	// HDR timelapse values
	private long mHdrTimeLapseMiddleExposure = UNINTIALISED;
	private long mHdrTimeLapseInterval = UNINTIALISED;
	private float mHdrTimeLapseEvStep = UNINTIALISED;

	// Bramping values
	private int mBrampingIterations = UNINTIALISED;
	private long mBrampingInterval = UNINTIALISED;
	private long mBrampingStartExposure = UNINTIALISED;
	private long mBrampingEndExposure = UNINTIALISED;

	// Distancelapse values
	private int mDistanceLapaseDistance = UNINTIALISED;
	private int mDistanceLapseUnit = UNINTIALISED;
	private int mDistanceLapseSpeedUnit = UNINTIALISED;

	// Timewarp values
	private int mTimeWarpInterations = UNINTIALISED;
	private long mTimeWarpDuration = UNINTIALISED;
	private float mTimewarpControl1X = UNINTIALISED;
	private float mTimewarpControl1Y = UNINTIALISED;
	private float mTimewarpControl2X = UNINTIALISED;
	private float mTimewarpControl2Y = UNINTIALISED;

	// App Rating Values
	private Boolean mShowDialogAgain = UNINTIALISED_BOOL;
	private int mLaunchCount = UNINTIALISED;
	private long mDateFirstLaunched = UNINTIALISED;

	// Wifi slave values
	private String mLastConnectedMaster = UNINTIALISED_STRING;

	// Wif Master values
	private Boolean mIsMasterON = UNINTIALISED_BOOL;

	// ND Filter Values
	private int mDefaultShutterSpeed = UNINTIALISED;

	public interface FragmentTags {
		public static String NONE = "none";
		public static String GETTING_STARTED = "getting_started";
		public static String BUY_DONGLE = "buy_dongle";
		public static String SIMPLE = "simple";
        public static String QUICK_RELEASE = "quick_release";
        public static String PRESS_AND_HOLD = "press_and_hold";
        public static String PRESS_TO_START = "press_to_start";
        public static String TIMED = "timed";
        public static String SELF_TIMER = "self_timer";
        public static String TIMELAPSE = "timelapse";
        public static String TIMEWARP = "timewarp";
        public static String STARTRAIL = "startrail";
        public static String BRAMPING = "bramping";
        public static String BANG = "bang";
        public static String DISTANCE_LAPSE = "distance_lapse";
        public static String HDR = "hdr";
        public static String HDR_LAPSE = "hdr_lapse";
        public static String WIFI_SLAVE = "wifi_slave";
        public static String WIFI_MASTER = "wifi_master";
        public static String PEBBLE = "pebble";
        public static String PLACEHOLDER = "placeholder";
        public static String SUNRISESUNSET = "sunrise_sunset";
        public static String ND_CALCULATOR = "nd_calculator";
    }

    public interface OnGoingAction {
        public static final int INVALID = -2;
        public static final int NONE = -1;
        public static final int PRESS_START_STOP = 0;
        public static final int TIMED = 1;
        public static final int TIMELAPSE = 2;
        public static final int TIMEWARP = 3;
        public static final int STAR_TRAIL = 4;
        public static final int BRAMPING = 5;
        public static final int BANG = 6;
        public static final int DISTANCE_LAPSE = 7;
        public static final int HDR = 8;
        public static final int HDR_TIMELAPSE = 9;
        public static final int WI_FI_SLAVE = 10;
        public static final int WI_FI_MASTER = 11;
        public static final int PRESS_AND_HOLD = 12;
        public static final int PEBBLE = 13;
        public static final int SELF_TIMER = 14;
        public static final int QUICK_RELEASE = 15;
	}

	private static TTApp mInstance;

	private TTApp(Context ctx) {
		mAppContext = ctx.getApplicationContext();
		LAST_ACTION_BAR_LABEL_DEFAULT = mAppContext.getResources().getString(
				R.string.getting_started);
	}

	public static TTApp getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new TTApp(context);
			mInstance.init();
		}
		return mInstance;
	}

	private void init() {
		// Load values store in the shares prefs
		SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
				Context.MODE_PRIVATE);

		SAN_SERIF_LIGHT = Typeface.createFromAsset(mAppContext.getAssets(),
				"fonts/Roboto-Light.ttf");
		SAN_SERIF_THIN = Typeface.createFromAsset(mAppContext.getAssets(),
				"fonts/Roboto-Thin.ttf");
	}

	public boolean isFirstStarted() {
		SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
				Context.MODE_PRIVATE);
		boolean isFirstLaunch = prefs.getBoolean(TT_IS_FIRST_LAUNCH, true);
		// Subsequent calls should always return false;
		Editor editor = prefs.edit();
		editor.putBoolean(TT_IS_FIRST_LAUNCH, false);
		editor.commit();
		return isFirstLaunch;
	}

	public String getLastFragmentTag() {
		if (mLastFragmentTag == UNINTIALISED_STRING) {
			SharedPreferences prefs = mAppContext.getSharedPreferences(
					TT_PREFS, Context.MODE_PRIVATE);
			mLastFragmentTag = prefs.getString(TT_LAST_FRAGMENT,
					LAST_FRAGMENT_DEFAULT);
		}
		return mLastFragmentTag;
	}

	public void setLastFragmentTag(String fragmentTag) {
		SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(TT_LAST_FRAGMENT, fragmentTag);
		editor.commit();
		mLastFragmentTag = fragmentTag;
	}

	public String getLastActionBarLabel() {
		if (mLastActionBarLabel == UNINTIALISED_STRING) {
			SharedPreferences prefs = mAppContext.getSharedPreferences(
					TT_PREFS, Context.MODE_PRIVATE);
			mLastActionBarLabel = prefs.getString(TT_LAST_ACTION_BAR_LABEL,
					LAST_ACTION_BAR_LABEL_DEFAULT);
		}
		return mLastActionBarLabel;
	}

	public void setLastActionBarLabel(String label) {
		SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(TT_LAST_ACTION_BAR_LABEL, label);
		editor.commit();
		mLastActionBarLabel = label;
	}

	public int getLastListItemChecked() {
		if (mLastListItemChecked == UNINTIALISED) {
			SharedPreferences prefs = mAppContext.getSharedPreferences(
					TT_PREFS, Context.MODE_PRIVATE);
			mLastListItemChecked = prefs.getInt(TT_LAST_LIST_ITEM_CHECKED,
					LAST_LIST_ITEM_CHECKED_DEFAULT);
		}
		return mLastListItemChecked;
	}

	public void setLastListItemChecked(int listItemIndex) {
		SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putInt(TT_LAST_LIST_ITEM_CHECKED, listItemIndex);
		editor.commit();
		mLastListItemChecked = listItemIndex;
	}

	public Boolean getShowAgain() {
		if (mShowDialogAgain == UNINTIALISED_BOOL) {
			SharedPreferences sharedPref = mAppContext.getSharedPreferences(TT_PREFS,
                    Context.MODE_PRIVATE);
            mShowDialogAgain = sharedPref.getBoolean(TT_SHOW_DIALOG_AGAIN,
                    SHOW_DIALOG_AGAIN_DEFAULT);
        }
        return mShowDialogAgain;
    }

    public void setShowDialogAgain(boolean showAgain) {
        mShowDialogAgain = showAgain;

        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putBoolean(TT_SHOW_DIALOG_AGAIN, showAgain);
        editor.commit();
    }

    public int getLaunchCount() {
        if (mLaunchCount == UNINTIALISED) {
            SharedPreferences sharedPref = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mLaunchCount = sharedPref.getInt(TT_LAUNCH_COUNT,
                    LAUNCH_COUNT_DEFAULT);
        }
        return mLaunchCount;
    }

    public void setLaunchCount(int count) {
        mLaunchCount = count;
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(
                TT_PREFS, Context.MODE_PRIVATE);
        Editor editor = sharedPref.edit();
        editor.putInt(TT_LAUNCH_COUNT, count);
        editor.commit();
    }

    public long getFirstLaunchDate() {
        if (mDateFirstLaunched == UNINTIALISED) {
            SharedPreferences sharedPref = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mDateFirstLaunched = sharedPref.getLong(TT_FIRST_LAUNCH_DATE,
                    FIRST_LAUNCH_DEFAULT);
        }
        return mDateFirstLaunched;
    }

    public void setFirstLaunchDate(long date) {
        mDateFirstLaunched = date;
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(
                TT_PREFS, Context.MODE_PRIVATE);
        Editor editor = sharedPref.edit();
        editor.putLong(TT_FIRST_LAUNCH_DATE, date);
        editor.commit();
    }

    public long getBeepLength() {
        // Get the Beep Length from the DEFAULT share prefs made with the
        // preferences.xml
        if (mBeepLength == UNINTIALISED) {
            SharedPreferences sharedPref = PreferenceManager
                    .getDefaultSharedPreferences(mAppContext);
            String pulseLengthStr = sharedPref.getString(
                    SettingsFragment.PULSE_LENGTH_SETTING, "");
            mBeepLength = Long.parseLong(pulseLengthStr);
        }
        return mBeepLength;
    }

    public void setBeepLength(long beepLength) {
        mBeepLength = beepLength;
    }

    public long getHDRGapLength() {
        return HDR_GAP;
    }

    public long getTimeLapseInterval() {
        if (mTimeLapseInterval == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mTimeLapseInterval = prefs.getLong(TT_TIMELASPE_INTERVAL,
                    TIMELASPE_INTERVAL_DEFAULT);
        }
        return mTimeLapseInterval;
    }

    public void setTimeLapseInterval(long timeLapseInterval) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putLong(TT_TIMELASPE_INTERVAL, timeLapseInterval);
        editor.commit();
        mTimeLapseInterval = timeLapseInterval;
    }

    public long getSelfTimedModeTime() {
        if (mSelfTimedModeTime == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mSelfTimedModeTime = prefs.getLong(TT_SELF_TIME_MODE_TIME,
                    SELF_TIME_MODE_TIME_DEFAULT);
        }
        return mSelfTimedModeTime;
    }

    public void setSelfTimedModeTime(long time) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putLong(TT_SELF_TIME_MODE_TIME, time);
        editor.commit();
        mSelfTimedModeTime = time;
    }

    public long getTimedModeTime() {
        if (mTimedModeTime == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mTimedModeTime = prefs.getLong(TT_TIME_MODE_TIME,
                    TIME_MODE_TIME_DEFAULT);
        }
        return mTimedModeTime;
    }

    public void setTimedModeTime(long time) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putLong(TT_TIME_MODE_TIME, time);
        editor.commit();
        mTimedModeTime = time;
    }

    public int getSensorResetDelay() {
        // Get the Sensor Reset from the DEFAULT share prefs made with the
        // preferences.xml
        if (mSensorResetDelay == UNINTIALISED) {
            SharedPreferences sharedPref = PreferenceManager
                    .getDefaultSharedPreferences(mAppContext);
            String resetDelay = sharedPref.getString(
                    SettingsFragment.SENSOR_RESET_DELAY_SETTING, "");
            mSensorResetDelay = Integer.parseInt(resetDelay);
        }
        return mSensorResetDelay;
    }

    public void setSensorResetDelay(int sensorResetDelay) {
        mSensorResetDelay = sensorResetDelay;
    }

    public int getSensorDelay() {
        // Get the Sensor Reset from the DEFAULT share prefs made with the
        // preferences.xml
        if (mSensorDelay == UNINTIALISED) {
            SharedPreferences sharedPref = PreferenceManager
                    .getDefaultSharedPreferences(mAppContext);
            String resetDelay = sharedPref.getString(
                    SettingsFragment.SENSOR_DELAY_SETTING, "");
            mSensorDelay = Integer.parseInt(resetDelay);
        }
        return mSensorDelay;
    }

    public void setSensorDelay(int sensorDelay) {
        mSensorDelay = sensorDelay;
    }

    public int getSoundSensorThreshold() {
        if (mSoundSensorThreshold == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mSoundSensorThreshold = prefs.getInt(TT_SOUND_SENSOR_THRESHOLD,
                    SOUND_SENSOR_THRESHOLD_DEFAULT);
        }
        return mSoundSensorThreshold;
    }

    public void setSoundSensorThreshold(int amplitude) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putInt(TT_SOUND_SENSOR_THRESHOLD, amplitude);
        editor.commit();
        mSoundSensorThreshold = amplitude;
    }

    public int getSoundSensorSensitivity() {
        if (mSoundSensorSensitivity == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mSoundSensorSensitivity = prefs.getInt(TT_SOUND_SENSOR_SENSITIVTY,
                    SOUND_SENSOR_SENSITIVTY_DEFAULT);
        }
        return mSoundSensorSensitivity;
    }

    public void setSoundSensorSensitivity(int amplitude) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putInt(TT_SOUND_SENSOR_SENSITIVTY, amplitude);
        editor.commit();
        mSoundSensorSensitivity = amplitude;
    }

    public int getStarTrailIterations() {
        if (mStarTrailInterations == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mStarTrailInterations = prefs.getInt(TT_STAR_TRAIL_ITERATIONS,
                    STAR_TRAIL_ITERATIONS_DEFAULT);
        }
        return mStarTrailInterations;
    }

    public void setStarTrailIterations(int iterations) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putInt(TT_STAR_TRAIL_ITERATIONS, iterations);
        editor.commit();
        mStarTrailInterations = iterations;
    }

    public long getStarTrailExposure() {
        if (mStarTrailExposure == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mStarTrailExposure = prefs.getLong(TT_STAR_TRAIL_EXPOSURE,
                    STAR_TRAIL_EXPOSURE_DEFAULT);
        }
        return mStarTrailExposure;
    }

    public void setStarTrailExposure(long exposure) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putLong(TT_STAR_TRAIL_EXPOSURE, exposure);
        editor.commit();
        mStarTrailExposure = exposure;
    }

    public long getStarTrailGap() {
        if (mStarTrailGap == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mStarTrailGap = prefs.getLong(TT_STAR_TRAIL_GAP,
                    STAR_TRAIL_GAP_DEFAULT);
        }
        return mStarTrailGap;
    }

    public void setStarTrailGap(long gap) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putLong(TT_STAR_TRAIL_GAP, gap);
        editor.commit();
        mStarTrailGap = gap;
    }

    public long getHDRMiddleExposure() {
        if (mHdrMiddleExposure == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mHdrMiddleExposure = prefs.getLong(TT_HDR_MIDDLE_EXP,
                    LE_HDR_MIDDLE_EXPOSURE_DEFAULT);
        }
        return mHdrMiddleExposure;
    }

    public void setHDRMiddleExposure(long hdrMiddleExposure) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putLong(TT_HDR_MIDDLE_EXP, hdrMiddleExposure);
        editor.commit();
        mHdrMiddleExposure = hdrMiddleExposure;
    }

    public int getHDRNumExposures() {
        if (mHdrNumberExposures == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mHdrNumberExposures = prefs.getInt(TT_HDR_NUM_EXP,
                    LE_HDR_NUM_EXPOSURES_DEFAULT);
        }
        return mHdrNumberExposures;
    }

    public void setHDRNumExposures(int hdrNumExposures) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putInt(TT_HDR_NUM_EXP, hdrNumExposures);
        editor.commit();
        mHdrNumberExposures = hdrNumExposures;
    }

    public float getHDREvStep() {
        if (mHdrEvStep == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mHdrEvStep = prefs.getFloat(TT_HDR_EV_STEP, LE_HDR_EV_STEP_DEFAULT);
        }
        return mHdrEvStep;
    }

    public void setHDREvStep(float evStep) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putFloat(TT_HDR_EV_STEP, evStep);
        editor.commit();
        mHdrEvStep = evStep;
    }

    public long getHDRTimeLapseMiddleExposure() {
        if (mHdrTimeLapseMiddleExposure == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mHdrTimeLapseMiddleExposure = prefs.getLong(
                    TT_HDR_TIMELPASE_MIDDLE_EXP,
                    LE_HDR_TIMELPASE_MIDDLE_EXPOSURE_DEFAULT);
        }
        return mHdrTimeLapseMiddleExposure;
    }

    public void setHDRTimeLapseMiddleExposure(long hdrTimeLapseMiddleExposure) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putLong(TT_HDR_TIMELPASE_MIDDLE_EXP, hdrTimeLapseMiddleExposure);
        editor.commit();
        mHdrTimeLapseMiddleExposure = hdrTimeLapseMiddleExposure;
    }

    public long getHDRTimeLapseInterval() {
        if (mHdrTimeLapseInterval == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mHdrTimeLapseInterval = prefs.getLong(TT_HDR_TIMELPASE_INTERVAL,
                    LE_HDR_TIMELPASE_INTERVAL_DEFAULT);
        }
        return mHdrTimeLapseInterval;
    }

    public void setHDRTimeLapseInterval(long duration) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putLong(TT_HDR_TIMELPASE_INTERVAL, duration);
        editor.commit();
        mHdrTimeLapseInterval = duration;
    }

    public float getHDRTimeLapseEvStep() {
        if (mHdrTimeLapseEvStep == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mHdrTimeLapseEvStep = prefs.getFloat(TT_HDR_TIMELPASE_EV_STEP,
                    LE_HDR_TIMELPASE_EV_STEP_DEFAULT);
        }
        return mHdrTimeLapseEvStep;
    }

    public void setHDRTimeLapseEvStep(float evStep) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putFloat(TT_HDR_TIMELPASE_EV_STEP, evStep);
        editor.commit();
        mHdrTimeLapseEvStep = evStep;
    }

    public int getBrampingIterations() {
        if (mBrampingIterations == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mBrampingIterations = prefs.getInt(TT_BRAMPING_ITERATIONS,
                    BRAMPING_INTERATION_DEFAULT);
        }
        return mBrampingIterations;
    }

    public void setBrampingIterations(int iterations) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putInt(TT_BRAMPING_ITERATIONS, iterations);
        editor.commit();
        mBrampingIterations = iterations;
    }

    public long getBrampingInterval() {
        if (mBrampingInterval == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mBrampingInterval = prefs.getLong(TT_BRAMPING_INTERVAL,
                    BRAMPING_INTERVAL_DEFAULT);
        }
        return mBrampingInterval;
    }

    public void setBrampingInterval(long duration) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putLong(TT_BRAMPING_INTERVAL, duration);
        editor.commit();
        mBrampingInterval = duration;
    }

    public long getBrampingStartExposure() {
        if (mBrampingStartExposure == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mBrampingStartExposure = prefs.getLong(TT_BRAMPING_START_EXP,
                    BRAMPING_START_EXPOSURE_DEFAULT);
        }
        return mBrampingStartExposure;
    }

    public void setBrampingStartExposure(long brampingStartExposure) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putLong(TT_BRAMPING_START_EXP, brampingStartExposure);
        editor.commit();
        mBrampingStartExposure = brampingStartExposure;
    }

    public long getBrampingEndExposure() {
        if (mBrampingEndExposure == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mBrampingEndExposure = prefs.getLong(TT_BRAMPING_END_EXP,
                    BRAMPING_END_EXPOSURE_DEFAULT);
        }
        return mBrampingEndExposure;
    }

    public void setBrampingEndExposure(long brampingEndExposure) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putLong(TT_BRAMPING_END_EXP, brampingEndExposure);
        editor.commit();
        mBrampingEndExposure = brampingEndExposure;
    }

    public int getDistanceLapseDistance() {
        if (mDistanceLapaseDistance == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mDistanceLapaseDistance = prefs.getInt(TT_DISTANCELAPSE_DISTANCE,
                    DISTANCELAPSE_DISTANCE_DEFAULT);
        }
        return mDistanceLapaseDistance;
    }

    public void setDistanceLapseDistance(int distance) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putInt(TT_DISTANCELAPSE_DISTANCE, distance);
        editor.commit();
        mDistanceLapaseDistance = distance;
    }

    public int getDistlapseUnit() {
        if (mDistanceLapseUnit == UNINTIALISED) {
            SharedPreferences sharedPref = PreferenceManager
                    .getDefaultSharedPreferences(mAppContext);
            String resetDelay = sharedPref.getString(
                    SettingsFragment.DISTANCE_UNIT_SETTING, "");
            mDistanceLapseUnit = Integer.parseInt(resetDelay);
        }
        return mDistanceLapseUnit;
    }

    public void setDistancLapseUnit(int distanceUnit) {
        mDistanceLapseUnit = distanceUnit;
    }

    public int getDefaultShutterSpeedVal() {
        if (mDefaultShutterSpeed == UNINTIALISED) {
            SharedPreferences sharedPref = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mDefaultShutterSpeed = sharedPref.getInt(TT_DEFAULT_SHUTTER_SPEED,
                    ND_FILTER_SHUTTER_SPEED_DEFAULT);
        }

        return mDefaultShutterSpeed;
    }

    public void setDefaultShutterSpeedVal(int shutterSpeedLoc) {
        mDefaultShutterSpeed = shutterSpeedLoc;
        SharedPreferences sharedPref = mAppContext.getSharedPreferences(
                TT_PREFS, Context.MODE_PRIVATE);
        Editor editor = sharedPref.edit();
        editor.putInt(TT_DEFAULT_SHUTTER_SPEED, shutterSpeedLoc);
        editor.commit();
    }

    public int getDistlapseSpeedUnit() {
        if (mDistanceLapseSpeedUnit == UNINTIALISED) {
            SharedPreferences sharedPref = PreferenceManager
                    .getDefaultSharedPreferences(mAppContext);
            String resetDelay = sharedPref.getString(
                    SettingsFragment.DISTANCE_SPEED_SETTING, "");
            mDistanceLapseSpeedUnit = Integer.parseInt(resetDelay);
        }
        return mDistanceLapseSpeedUnit;
    }

    public void setDistancLapseSpeedUnit(int distanceSpeedUnit) {
        mDistanceLapseSpeedUnit = distanceSpeedUnit;
    }

    public int getTimeWarpIterations() {
        if (mTimeWarpInterations == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mTimeWarpInterations = prefs.getInt(TT_TIMEWARP_ITERATIONS,
                    TIMEWARP_INTERATIONS_DEAFAULT);
        }
        return mTimeWarpInterations;
    }

    public void setTimeWarpIterations(int iterations) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putInt(TT_TIMEWARP_ITERATIONS, iterations);
        editor.commit();
        mTimeWarpInterations = iterations;
    }

    public long getTimewarpDuration() {
        if (mTimeWarpDuration == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mTimeWarpDuration = prefs.getLong(TT_TIMEWARP_DURATION,
                    TIMEWARP_DURATION_DEFAULT);
        }
        return mTimeWarpDuration;
    }

    public void setTimewarpDuration(long exposure) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putLong(TT_TIMEWARP_DURATION, exposure);
        editor.commit();
        mTimeWarpDuration = exposure;
    }

    public float getTimewarpControl1X() {
        if (mTimewarpControl1X == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mTimewarpControl1X = prefs.getFloat(TT_TIMEWARP_CONTROL1_X,
                    TIMEWARP_CONTROL1_X_DEFAULT);
        }
        return mTimewarpControl1X;
    }

    public void setTimewarpControl1X(float controlCoord) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putFloat(TT_TIMEWARP_CONTROL1_X, controlCoord);
        editor.commit();
        mTimewarpControl1X = controlCoord;
    }

    public float getTimewarpControl1Y() {
        if (mTimewarpControl1Y == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mTimewarpControl1Y = prefs.getFloat(TT_TIMEWARP_CONTROL1_Y,
                    TIMEWARP_CONTROL1_Y_DEFAULT);
        }
        return mTimewarpControl1Y;
    }

    public void setTimewarpControl1Y(float controlCoord) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putFloat(TT_TIMEWARP_CONTROL1_Y, controlCoord);
        editor.commit();
        mTimewarpControl1Y = controlCoord;
    }

    public float getTimewarpControl2X() {
        if (mTimewarpControl2X == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mTimewarpControl2X = prefs.getFloat(TT_TIMEWARP_CONTROL2_X,
                    TIMEWARP_CONTROL2_X_DEFAULT);
        }
        return mTimewarpControl2X;
    }

    public void setTimewarpControl2X(float controlCoord) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putFloat(TT_TIMEWARP_CONTROL2_X, controlCoord);
        editor.commit();
        mTimewarpControl2X = controlCoord;
    }

    public float getTimewarpControl2Y() {
        if (mTimewarpControl2Y == UNINTIALISED) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mTimewarpControl2Y = prefs.getFloat(TT_TIMEWARP_CONTROL2_Y,
                    TIMEWARP_CONTROL2_Y_DEFAULT);
        }
        return mTimewarpControl2Y;
    }

    public void setTimewarpControl2Y(float controlCoord) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putFloat(TT_TIMEWARP_CONTROL2_Y, controlCoord);
        editor.commit();
        mTimewarpControl2Y = controlCoord;
    }

    public String getSlaveLastMaster() {
        if (mLastConnectedMaster == UNINTIALISED_STRING) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mLastConnectedMaster = prefs.getString(TT_WIFI_SLAVE_LAST_MASTER,
                    WIFI_SLAVE_LAST_MASTER_DEFAULT);
        }
        return mLastConnectedMaster;
    }

    public void setSlaveLastMaster(String lastMaster) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putString(TT_WIFI_SLAVE_LAST_MASTER, lastMaster);
        editor.commit();
        mLastConnectedMaster = lastMaster;
    }

    public boolean isMasterOn() {
        if (mIsMasterON == UNINTIALISED_BOOL) {
            SharedPreferences prefs = mAppContext.getSharedPreferences(
                    TT_PREFS, Context.MODE_PRIVATE);
            mIsMasterON = prefs.getBoolean(TT_WIFI_MASTER_IS_ON,
                    WIFI_MASTER_IS_ON_DEFAULT);
        }
        return mIsMasterON;
    }

    public void setMasterOn(boolean state) {
        SharedPreferences prefs = mAppContext.getSharedPreferences(TT_PREFS,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putBoolean(TT_WIFI_MASTER_IS_ON, state);
        editor.commit();
        mIsMasterON = state;
    }

}
