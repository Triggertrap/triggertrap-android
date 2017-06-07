package com.triggertrap.fragments;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import uk.me.jstott.coordconv.LatitudeLongitude;
import uk.me.jstott.sun.Sun;
import uk.me.jstott.sun.Time;

import android.content.Context;
import android.content.res.Resources;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.triggertrap.R;

/**
 * Fragment Activity allowing the user to see the numerous solar events, with
 * visual and textual content.
 *
 * @author Scott Mellors
 * @version 1
 * @since 2.1
 */
public class SunriseSunsetFragment extends TriggertrapFragment implements
        LocationListener {

    private static final int PERCENTAGE_PROGRESS = 1;
    private static final int TIME_LIMIT = 30000;
    private static final int ANIMATION_DURATION = 1000;
    private static final int LOCATIONREQUESTDISTANCE = 10000;
    private static final int LOCATIONREQUESTDURATION = 500;

    private static final int MIDNIGHT_HOUR = 23;
    private static final int MIDNIGHT_MIN = 59;
    private static final int MIDNIGHT_SEC = 59;
    private static final int MIDNIGHT_MS = 999;

    private static final int HOURS_IN_A_DAY = 24;

    private static final int PERCENTAGE_MODIFIER = 100;

    private static final int MS_IN_A_DAY = 86400000;
    private static final int MS_IN_A_HOUR = 3600000;
    private static final int MS_IN_A_MINUTE = 60000;
    private static final int MS_IN_A_SECOND = 1000;

    private static final int MS_GAP = 100;

    /**
     * Enum for use in determining which solar event calculation is requested.
     *
     * @author scottmellors
     */
    private enum SolarEvent {
        SUNRISETWILIGHT, SUNRISE, SUNSET, SUNSETTWILIGHT
    }

    private com.triggertrap.view.SolarArc mSolarArcView;

    private GeolocateTask mGeolocateTask;

    private boolean mWasRunning;

    private LocationManager mLocationManager;

    private Calendar mSunriseCal = null, mSunriseTwilightCal = null,
            mSunsetCal = null, mSunsetTwilightCal = null;

    private boolean mGoneMidnight = false;

    private TextView mTitle1TextView, mTitle2TextView, mSolarTime1TextView,
            mSolarTime2TextView, mRelativeTime1TextView,
            mRelativeTime2TextView, mTwilightTime1TextView,
            mTwilightTime2TextView, mLocationTextView;

    private Handler mTimeUpdater;

    private Calendar mNextSunrise, mNextSunset, mLastSunrise, mLastSunset;
    private Calendar mLastSunsetTwilight, mLastSunriseTwilight,
            mNextSunsetTwilight, mNextSunriseTwilight;

    /**
     * Handler Extension allowing the handler to receive two events in the
     * constructor for processing the time between them.
     *
     * @author scottmellors
     * @since 2.1
     */
    private class EventUpdateHandler extends Handler {

        private Calendar mEvent1;
        private Calendar mEvent2;

        private long mNextUpdate;

        public EventUpdateHandler(Calendar cal1, Calendar cal2) {
            mEvent1 = cal1;
            mEvent2 = cal2;

        }

        public void handleMessage(Message msg) {

            Calendar now = Calendar.getInstance();

            long percentChange = (mEvent2.getTimeInMillis() - mEvent1
                    .getTimeInMillis()) / PERCENTAGE_MODIFIER;

            mNextUpdate = now.getTimeInMillis() % percentChange;

            if (isAdded()) {

                long event1Difference;

                event1Difference = getRelativeTimeToEvent(mEvent1);

                mRelativeTime1TextView.setText(getStringOfDifference(mEvent1));

                mRelativeTime2TextView.setText(getStringOfDifference(mEvent2));

                Calendar lastEvent;

                if (mLastSunset == null) {
                    lastEvent = (Calendar) mLastSunrise.clone();
                } else {
                    lastEvent = (Calendar) mLastSunset.clone();
                }

                Calendar midnightTonight = lastEvent;
                midnightTonight.set(Calendar.HOUR_OF_DAY, MIDNIGHT_HOUR);
                midnightTonight.set(Calendar.MINUTE, MIDNIGHT_MIN);
                midnightTonight.set(Calendar.SECOND, MIDNIGHT_SEC);
                midnightTonight.set(Calendar.MILLISECOND, MIDNIGHT_MS);

                if (Calendar.getInstance().after(midnightTonight)
                        && !mGoneMidnight) {
                    // redraw populate
                    populateLayout(getString(R.string.sunrise), mNextSunrise,
                            mNextSunriseTwilight, getString(R.string.sunset),
                            mNextSunset, mNextSunsetTwilight);

                    mGoneMidnight = true;
                }

                if (event1Difference < MS_GAP) {

                    loadUi();

                } else {

                    if (mNextUpdate == 0) {

                        mSolarArcView.progressSun(PERCENTAGE_PROGRESS);
                    }

                    this.sendEmptyMessageDelayed(1, ANIMATION_DURATION);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.sunrise_sunset_mode,
                container, false);

        mTitle1TextView = (TextView) rootView.findViewById(R.id.title1);
        mTitle1TextView.setTypeface(SAN_SERIF_LIGHT);
        mTitle2TextView = (TextView) rootView.findViewById(R.id.title2);
        mTitle2TextView.setTypeface(SAN_SERIF_LIGHT);
        mSolarTime1TextView = (TextView) rootView.findViewById(R.id.timeLabel1);
        mSolarTime2TextView = (TextView) rootView.findViewById(R.id.timeLabel2);
        mRelativeTime1TextView = (TextView) rootView
                .findViewById(R.id.relativeTimeLabel1);
        mRelativeTime1TextView.setTypeface(SAN_SERIF_LIGHT);
        mRelativeTime2TextView = (TextView) rootView
                .findViewById(R.id.relativeTimeLabel2);
        mRelativeTime2TextView.setTypeface(SAN_SERIF_LIGHT);
        mTwilightTime1TextView = (TextView) rootView
                .findViewById(R.id.twilightTime1);
        mTwilightTime1TextView.setTypeface(SAN_SERIF_LIGHT);
        mTwilightTime2TextView = (TextView) rootView
                .findViewById(R.id.twilightTime2);
        mTwilightTime2TextView.setTypeface(SAN_SERIF_LIGHT);

        mLocationTextView = (TextView) rootView
                .findViewById(R.id.locationTextView);
        mLocationTextView.setTypeface(SAN_SERIF_LIGHT);

        mSolarArcView = (com.triggertrap.view.SolarArc) rootView
                .findViewById(R.id.solarArcView);

        loadUi();

        rootView.invalidate();

        return rootView;
    }

    @Override
    public void onPause() {

        if (mTimeUpdater != null) {
            mTimeUpdater.removeCallbacksAndMessages(null);
            mWasRunning = true;
        }

        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
            mLocationManager = null;
        }

        if (mGeolocateTask != null) {

            if (!mGeolocateTask.isCancelled()) {
                mGeolocateTask.cancel(true);
            }
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mWasRunning) {
            mWasRunning = false;

            mTimeUpdater.sendEmptyMessageDelayed(1, ANIMATION_DURATION);
        }

    }

    private void loadUi() {
        mLocationManager = (LocationManager) getActivity().getSystemService(
                Context.LOCATION_SERVICE);

        Location location = null;

        Location bestResult = null;
        // getting GPS status
        boolean isGPSEnabled = mLocationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        boolean isNetworkEnabled = mLocationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            Criteria criteria = new Criteria();
            String bestProvider = mLocationManager.getBestProvider(criteria,
                    true);

            if (bestProvider != null) {
                location = mLocationManager.getLastKnownLocation(bestProvider);
            }

            if (location != null) {
                float accuracy = location.getAccuracy();
                long time = location.getTime();

                float bestAccuracy = Float.MAX_VALUE;
                long bestTime = Long.MIN_VALUE;

                if ((time > TIME_LIMIT && accuracy < bestAccuracy)) {
                    bestResult = location;
                } else if (time < TIME_LIMIT && bestAccuracy == Float.MAX_VALUE
                        && time > bestTime) {
                    bestResult = location;
                }
            }
        } else {
            if (isGPSEnabled) {
                if (location == null) {
                    mLocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            LOCATIONREQUESTDURATION, LOCATIONREQUESTDISTANCE,
                            this);
                    if (mLocationManager != null) {

                        bestResult = mLocationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }
            }

            if (isNetworkEnabled) {
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        LOCATIONREQUESTDURATION, LOCATIONREQUESTDISTANCE, this);
                if (mLocationManager != null) {
                    bestResult = mLocationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
        }

        if (bestResult != null) {

            mGeolocateTask = new GeolocateTask();
            mGeolocateTask.execute(bestResult);

            LatitudeLongitude longLat = new LatitudeLongitude(
                    bestResult.getLatitude(), bestResult.getLongitude());

            mSunriseCal = Calendar.getInstance();
            mSunriseTwilightCal = Calendar.getInstance();

            mSunsetCal = Calendar.getInstance();
            mSunsetTwilightCal = Calendar.getInstance();

            findTimesForSolarEvents(longLat);

        } else {
            mLocationTextView.setText(getString(R.string.loc_not_found));
        }

    }

    private String getStringOfDifference(Calendar event) {

        Date currentTime = Calendar.getInstance().getTime();

        int days, hours, minutes, seconds;

        long difference = event.getTime().getTime() - currentTime.getTime();

        days = (int) (difference / MS_IN_A_DAY);
        difference -= days * MS_IN_A_DAY;
        hours = (int) (difference / MS_IN_A_HOUR);
        difference -= hours * MS_IN_A_HOUR;
        minutes = (int) (difference / MS_IN_A_MINUTE);
        difference -= minutes * MS_IN_A_MINUTE;
        seconds = (int) (difference / MS_IN_A_SECOND);

        if (days == 1) {
            hours += HOURS_IN_A_DAY;
        }

        String relativeTimeString = getString(R.string.in) + " ";
        Resources res = getResources();

        relativeTimeString += res.getQuantityString(R.plurals.numberOfHours,
                hours, hours);

        relativeTimeString += " ";

        relativeTimeString += res.getQuantityString(R.plurals.numberOfMinutes,
                minutes, minutes);
        relativeTimeString += " ";

        relativeTimeString += res.getQuantityString(R.plurals.numberOfSeconds,
                seconds, seconds);

        return relativeTimeString;
    }

    /**
     * Creates a Calendar object of the requested event.
     *
     * @param c         Calendar to start with.
     * @param longLat   position of device.
     * @param timeEvent which event was requested.
     * @return new Calender object with the time and date of the requested
     * event.
     */

    private Calendar getCalendarForTime(Calendar c, LatitudeLongitude longLat,
                                        SolarEvent timeEvent) {

        Time tempTime = null;
        Calendar returnedCal = Calendar.getInstance();

        switch (timeEvent) {
            case SUNRISETWILIGHT:
                tempTime = Sun.morningCivilTwilightTime(c, longLat, TimeZone
                                .getDefault(),
                        TimeZone.getDefault().inDaylightTime(new Date()));
                break;
            case SUNRISE:
                tempTime = Sun.sunriseTime(c, longLat, TimeZone.getDefault(),
                        TimeZone.getDefault().inDaylightTime(new Date()));
                break;
            case SUNSET:
                tempTime = Sun.sunsetTime(c, longLat, TimeZone.getDefault(),
                        TimeZone.getDefault().inDaylightTime(new Date()));
                break;
            case SUNSETTWILIGHT:
                tempTime = Sun.eveningCivilTwilightTime(c, longLat, TimeZone
                                .getDefault(),
                        TimeZone.getDefault().inDaylightTime(new Date()));
                break;
            default:
                break;
        }

        returnedCal.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DATE), tempTime.getHours(),
                tempTime.getMinutes(), (int) tempTime.getSeconds());

        return returnedCal;
    }

    private void findTimesForSolarEvents(LatitudeLongitude longLat) {

        Calendar now = Calendar.getInstance();

        mSunriseTwilightCal = getCalendarForTime(now, longLat,
                SolarEvent.SUNRISETWILIGHT);
        mSunriseCal = getCalendarForTime(now, longLat, SolarEvent.SUNRISE);
        mSunsetCal = getCalendarForTime(now, longLat, SolarEvent.SUNSET);
        mSunsetTwilightCal = getCalendarForTime(now, longLat,
                SolarEvent.SUNSETTWILIGHT);

        // presunrise state
        if (now.before(mSunsetCal) && now.before(mSunriseCal)) {

            mNextSunrise = (Calendar) mSunriseCal.clone();
            mNextSunriseTwilight = (Calendar) mSunriseTwilightCal.clone();
            mNextSunset = (Calendar) mSunsetCal.clone();
            mNextSunsetTwilight = (Calendar) mSunsetTwilightCal.clone();

            now.roll(Calendar.DAY_OF_YEAR, false);

            mLastSunset = getCalendarForTime(now, longLat, SolarEvent.SUNSET);
            mLastSunsetTwilight = getCalendarForTime(now, longLat,
                    SolarEvent.SUNSETTWILIGHT);

            mSolarArcView.setIsAfterSunset(true);
            mSolarArcView.updateTime(mNextSunriseTwilight, mNextSunrise,
                    mLastSunset, mLastSunsetTwilight);

            populateLayout(getString(R.string.sunrise), mNextSunrise,
                    mNextSunriseTwilight, getString(R.string.sunset),
                    mNextSunset, mNextSunsetTwilight);

            mRelativeTime1TextView.setText(getStringOfDifference(mNextSunrise));

            mRelativeTime2TextView.setText(getStringOfDifference(mNextSunset));

            // post sunset state
        } else if (mSunsetCal.getTimeInMillis() <= Calendar.getInstance()
                .getTimeInMillis()) {

            mLastSunrise = (Calendar) mSunriseCal.clone();
            mLastSunriseTwilight = (Calendar) mSunriseTwilightCal.clone();
            mLastSunset = (Calendar) mSunsetCal.clone();
            mLastSunsetTwilight = (Calendar) mSunsetTwilightCal.clone();

            mSunriseCal.roll(Calendar.DAY_OF_YEAR, true);

            mNextSunrise = getCalendarForTime(mSunriseCal, longLat,
                    SolarEvent.SUNRISE);
            mNextSunriseTwilight = getCalendarForTime(mSunriseCal, longLat,
                    SolarEvent.SUNRISETWILIGHT);

            mSunsetCal.roll(Calendar.DAY_OF_YEAR, true);

            mNextSunset = getCalendarForTime(mSunsetCal, longLat,
                    SolarEvent.SUNSET);
            mNextSunsetTwilight = getCalendarForTime(mSunriseCal, longLat,
                    SolarEvent.SUNSETTWILIGHT);

            mSolarArcView.setIsAfterSunset(true);
            mSolarArcView.updateTime(mNextSunriseTwilight, mNextSunrise,
                    mLastSunset, mLastSunsetTwilight);

            Calendar midnightTonight = (Calendar) mLastSunset.clone();
            midnightTonight.set(Calendar.HOUR_OF_DAY, MIDNIGHT_HOUR);
            midnightTonight.set(Calendar.MINUTE, MIDNIGHT_MIN);
            midnightTonight.set(Calendar.SECOND, MIDNIGHT_SEC);
            midnightTonight.set(Calendar.MILLISECOND, MIDNIGHT_MS);

            if (now.before(midnightTonight)) {
                populateLayout(getString(R.string.next_sunrise), mNextSunrise,
                        mNextSunriseTwilight, getString(R.string.next_sunset),
                        mNextSunset, mNextSunsetTwilight);
            } else {
                populateLayout(getString(R.string.sunrise), mNextSunrise,
                        mNextSunriseTwilight, getString(R.string.sunset),
                        mNextSunset, mNextSunsetTwilight);
            }

            // postsunrise state
        } else {
            mLastSunrise = (Calendar) mSunriseCal.clone();
            mLastSunriseTwilight = (Calendar) mSunriseTwilightCal.clone();
            mNextSunset = (Calendar) mSunsetCal.clone();
            mNextSunsetTwilight = (Calendar) mSunsetTwilightCal.clone();

            mSunriseCal.roll(Calendar.DAY_OF_YEAR, true);

            mNextSunrise = getCalendarForTime(mSunriseCal, longLat,
                    SolarEvent.SUNRISE);
            mNextSunriseTwilight = getCalendarForTime(mSunriseCal, longLat,
                    SolarEvent.SUNRISETWILIGHT);

            mSolarArcView.setIsAfterSunset(false);
            mSolarArcView.updateTime(mLastSunriseTwilight, mLastSunrise,
                    mNextSunset, mNextSunsetTwilight);

            populateLayout(getString(R.string.sunset), mNextSunset,
                    mNextSunsetTwilight, getString(R.string.next_sunrise),
                    mNextSunrise, mNextSunriseTwilight);

            mRelativeTime1TextView.setText(getStringOfDifference(mNextSunset));

            mRelativeTime2TextView.setText(getStringOfDifference(mNextSunrise));

        }
    }

    /**
     * An extension of AsyncTask which takes a location and determines the
     * geolocation string. Updating the UI where necessary. This prevents the UI
     * locking up should the user be without a network connection.
     *
     * @author scottmellors
     * @since 2.1
     */
    private class GeolocateTask extends AsyncTask<Location, Void, Void> {

        private String mLocationString;

        @Override
        protected void onPreExecute() {
            mLocationTextView.setText(getString(R.string.finding_loc));
        }

        @Override
        protected Void doInBackground(Location... loc) {
            mLocationString = getLocationStringFrom(loc[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mLocationTextView.setText(mLocationString);

        }

    }

    /**
     * Converts a location with longitude and latitude information to a readable
     * location name.
     *
     * @param l Location object to be used in determining a named place
     * @return String object either containing a found named location or a
     * string derived from the Longitude and Latitude of Location l
     */

    private String getLocationStringFrom(Location l) {

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getActivity().getBaseContext(),
                Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(l.getLatitude(),
                    l.getLongitude(), 1);

            if (addresses != null && addresses.size() > 0) {
                // String address = addresses.get(0).getAddressLine(0);
                // String country = addresses.get(0).getAddressLine(2);
                return addresses.get(0).getAddressLine(1);
            } else {
                return String.valueOf(l.getLatitude()) + " "
                        + String.valueOf(l.getLongitude());

            }

        } catch (NullPointerException e) {

            return "Location not Found";

        } catch (Exception e) {
            e.printStackTrace();

            return String.valueOf(l.getLatitude()) + " "
                    + String.valueOf(l.getLongitude());
        }
    }

    /**
     * Function to calculate the relative time to a solar event. Used with
     * handler to update the UI.
     */
    private long getRelativeTimeToEvent(Calendar event) {

        Calendar cal = Calendar.getInstance();
        return event.getTimeInMillis() - cal.getTimeInMillis();
    }

    /**
     * Function to populate the numerous widgets with the calculated
     * Sunrise/Sunset information. This also calculates the relative time to the
     * main two events.
     *
     * @param firstTitle  The title to be displayed in the first title TextView.
     * @param time1       Calendar object containing the solar event time to be
     *                    displayed first.
     * @param twilight1   Calendar object containing the twilight time of the event
     *                    passed with time1.
     * @param secondTitle The title to be displayed in the second title TextView.
     * @param time2       Calendar object containing the solar event time to be
     *                    displayed second.
     * @param twilight2   Calendar object containing the twilight time of the event
     *                    passed with time2.
     */
    private void populateLayout(String firstTitle, Calendar time1,
                                Calendar twilight1, String secondTitle, Calendar time2,
                                Calendar twilight2) {

        java.text.DateFormat df = DateFormat.getTimeFormat(getActivity());

        mTitle1TextView.setText(firstTitle);
        mTitle2TextView.setText(secondTitle);

        mSolarTime1TextView.setText(df.format(time1.getTime()));
        mSolarTime2TextView.setText(df.format(time2.getTime()));

        if (firstTitle.equals(getString(R.string.sunset))) {
            mTwilightTime1TextView.setText(getString(R.string.last_light_at)
                    + " " + df.format(twilight1.getTime()));
            mTwilightTime2TextView.setText(getString(R.string.first_light_at)
                    + " " + df.format(twilight2.getTime()));
        } else {
            mTwilightTime1TextView.setText(getString(R.string.first_light_at)
                    + " " + df.format(twilight1.getTime()));
            mTwilightTime2TextView.setText(getString(R.string.last_light_at)
                    + " " + df.format(twilight2.getTime()));
        }

        mTimeUpdater = new EventUpdateHandler(time1, time2);
        mTimeUpdater.sendEmptyMessageDelayed(1, ANIMATION_DURATION);

    }

    /**
     * Location Listener methods.
     */
    @Override
    public void onLocationChanged(Location arg0) {
        if (arg0 != null && isAdded()) {

            mGeolocateTask = new GeolocateTask();
            mGeolocateTask.execute(arg0);

            if (mLocationManager != null) {
                mLocationManager.removeUpdates(this);
                mLocationManager = null;
            }
        }
    }

    @Override
    public void onProviderDisabled(String arg0) {

    }

    @Override
    public void onProviderEnabled(String arg0) {

    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

    }
}
