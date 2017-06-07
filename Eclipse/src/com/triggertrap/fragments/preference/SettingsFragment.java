package com.triggertrap.fragments.preference;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.triggertrap.R;
import com.triggertrap.TTApp;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	private static final String TAG = SettingsFragment.class.getSimpleName();
	
	public static final String PULSE_LENGTH_SETTING = "pref_pulse_length_setting";
	public static final String SENSOR_DELAY_SETTING = "pref_sensor_delay_setting";
	public static final String SENSOR_RESET_DELAY_SETTING = "pref_sensor_reset_delay_setting";
	public static final String DISTANCE_SPEED_SETTING = "pref_distance_speed_setting";
	public static final String DISTANCE_UNIT_SETTING = "pref_distance_unit_setting";
	
	public static final String SETTINGS_UPDATE_EVENT = "distance_unit_update_event";
	
	public static interface SettingsType {
		public static final int DISTANCE_UNIT = 0;
		public static final int DISTANCES_SPEED_UNT = 1;
		public static final int PULSE_LENGTH = 2; 
	}
	
	
	public static interface DistanceUnits {
		public static final int METERS_KILOMETERS = 0;
		public static final int MILES_YARDS = 1;
	}
	
	public static interface DistanceSpeedUnit {
		public static final int METERS_PER_SECOND = 0;
		public static final int MILES_PER_HOUR = 1;
		public static final int KILOMETERS_PER_HOUR = 2;
		
	}
	
	public static interface SettingsEvent {
		public static final String EVENT_TYPE =  "event_type";
		public static final String EVENT_VALUE = "event_value";
	}
				
	 @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
		sp.registerOnSharedPreferenceChangeListener(this);
		Preference versionPref = findPreference("pref_key_version");

		PackageInfo pInfo = null;
		try {
			pInfo = getActivity().getPackageManager().getPackageInfo(
					getActivity().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		String version = getResources().getString(R.string.unknown);
		if (pInfo != null) {
			version = pInfo.versionName;
		}
		versionPref.setSummary(version);

		//Set up the initial summaries 
		ListPreference pulseLengthPref = (ListPreference) findPreference(PULSE_LENGTH_SETTING);
		pulseLengthPref.setSummary(pulseLengthPref.getEntry());	
		ListPreference sensorDelayPref = (ListPreference) findPreference(SENSOR_DELAY_SETTING);
		sensorDelayPref.setSummary(sensorDelayPref.getEntry());
		ListPreference sensorResetDelayPref = (ListPreference) findPreference(SENSOR_RESET_DELAY_SETTING);
		sensorResetDelayPref.setSummary(sensorResetDelayPref.getEntry());
		ListPreference distanceSpeedPref = (ListPreference) findPreference(DISTANCE_SPEED_SETTING);
		distanceSpeedPref.setSummary(distanceSpeedPref.getEntry());
		ListPreference distanceUnitPref = (ListPreference) findPreference(DISTANCE_UNIT_SETTING);
		distanceUnitPref.setSummary(distanceUnitPref.getEntry());
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
		if (key.equals(PULSE_LENGTH_SETTING)) {
			ListPreference connectionPref = (ListPreference) findPreference(key);	
            connectionPref.setSummary(connectionPref.getEntry());
            TTApp.getInstance(getActivity()).setBeepLength(Long.parseLong(sharedPrefs.getString(key,"")));
            //Create Distance update event and broadcast it
            Intent intent = new Intent(SETTINGS_UPDATE_EVENT);
            intent.putExtra(SettingsEvent.EVENT_TYPE, SettingsType.PULSE_LENGTH);
            intent.putExtra(SettingsEvent.EVENT_VALUE, Long.parseLong(sharedPrefs.getString(key,"")));            
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            
        } else if(key.equals(SENSOR_RESET_DELAY_SETTING )) {
        	ListPreference connectionPref = (ListPreference) findPreference(key);	
            connectionPref.setSummary(connectionPref.getEntry());
            TTApp.getInstance(getActivity()).setSensorResetDelay(Integer.parseInt(sharedPrefs.getString(key,"")));
        } else if(key.equals(SENSOR_DELAY_SETTING )) {
        	ListPreference connectionPref = (ListPreference) findPreference(key);	
            connectionPref.setSummary(connectionPref.getEntry());
            TTApp.getInstance(getActivity()).setSensorDelay(Integer.parseInt(sharedPrefs.getString(key,"")));
        } else if(key.equals(DISTANCE_SPEED_SETTING )) {
        	ListPreference connectionPref = (ListPreference) findPreference(key);	
            connectionPref.setSummary(connectionPref.getEntry());
            TTApp.getInstance(getActivity()).setDistancLapseSpeedUnit(Integer.parseInt(sharedPrefs.getString(key,"")));
            
            //Create Distance update event and broadcast it
            Intent intent = new Intent(SETTINGS_UPDATE_EVENT);
            intent.putExtra(SettingsEvent.EVENT_TYPE, SettingsType.DISTANCES_SPEED_UNT);
            intent.putExtra(SettingsEvent.EVENT_VALUE,Integer.parseInt(sharedPrefs.getString(key,"")));            
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            
        } else if(key.equals(DISTANCE_UNIT_SETTING )) {
        	ListPreference connectionPref = (ListPreference) findPreference(key);	
            connectionPref.setSummary(connectionPref.getEntry());
            TTApp.getInstance(getActivity()).setDistancLapseUnit(Integer.parseInt(sharedPrefs.getString(key,"")));
            
            Log.d(TAG,"Sending broadcast");
            //Create Distance update event and broadcast it
            Intent intent = new Intent(SETTINGS_UPDATE_EVENT);
            intent.putExtra(SettingsEvent.EVENT_TYPE, SettingsType.DISTANCE_UNIT);
            intent.putExtra(SettingsEvent.EVENT_VALUE,Integer.parseInt(sharedPrefs.getString(key,"")));            
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        }
		
	}
}
