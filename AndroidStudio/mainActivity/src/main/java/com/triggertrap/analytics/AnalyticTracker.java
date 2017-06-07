package com.triggertrap.analytics;

import android.content.Context;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/** Wrapper class to cover tracking of Analytics
 *
 * Created by scottmellors on 21/08/2014.
 * @since 2.3
 */
public class AnalyticTracker {

    private static final String PRODUCTION_KEY = "4122506383dcc763557d3aff6fc2e85e";

    public interface Event {
        public static final String SEQUENCE_COMPLETED = "Sequence Completed";
        public static final String SESSION_COMPLETED = "Session Completed";
    }

    public interface Property {
        public static final String MODE = "Mode";
        public static final String NO_EXPOSURES_TAKEN = "No. Exposures Taken";
        public static final String SESSION_DURATION = "Session Duration";
        public static final String EXPOSURE_DURATION = "Exposure Duration";
        public static final String SEQUENCE_DURATION = "Sequence Duration";
        public static final String LANGUAGE = "Language";
        public static final String TIMELAPSE_INTERVAL = "Timelapse Interval";
        public static final String SENSOR_DELAY = "Sensor Delay";
        public static final String SENSOR_RESET_DELAY = "Sensor Reset Delay";
        public static final String PULSE_LENGTH = "Pulse Length";
        public static final String SPEED_UNIT = "Speed Unit";
        public static final String DISTANCE_UNIT = "Distance Unit";
    }

    private Context mContext;
    private static AnalyticTracker mInstance;

    private Calendar mStartTime;

    private MixpanelAPI mMixpanel;
    private static JSONObject mProperties;

    public AnalyticTracker(Context context) {
        mContext = context;
    }

    public static AnalyticTracker getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AnalyticTracker(context);
        }

        mProperties = new JSONObject();

        return mInstance;
    }

    public void startSession() {

        mMixpanel = MixpanelAPI.getInstance(mContext, PRODUCTION_KEY);

        mStartTime = Calendar.getInstance();
    }

    public void endSession() {

        //Generate time in seconds between now and mStartTime
        Calendar timeNow = Calendar.getInstance();
        long diffInMs = 0;
        if(mStartTime != null) {
            diffInMs = timeNow.getTime().getTime() - mStartTime.getTime().getTime();
        }
        long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);

        JSONObject properties = new JSONObject();
        try {
            properties.put(Property.SESSION_DURATION, diffInSec);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //push to mixpanelmanger
        mMixpanel.track(Event.SESSION_COMPLETED, properties);

        //tidy up variables
        mStartTime = null;
    }

    public void trackEvent(String eventName) {
        mMixpanel.track(eventName, mProperties);
    }

    public void addProperty(String key, String value) {
        try {
            mProperties.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addProperty(String key, int value) {
        try {
            mProperties.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addProperty(String key, long value) {
        try {
            mProperties.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void flush() {
        mMixpanel.flush();
    }

}
