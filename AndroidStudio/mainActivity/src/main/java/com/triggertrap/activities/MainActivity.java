package com.triggertrap.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.FirmwareVersionInfo;
import com.triggertrap.R;
import com.triggertrap.TTApp;
import com.triggertrap.analytics.AnalyticTracker;
import com.triggertrap.fragments.BrampingFragment;
import com.triggertrap.fragments.CableSelectorFragment;
import com.triggertrap.fragments.CableReleaseFragment;
import com.triggertrap.fragments.CableReleaseFragment.SimpleModeListener;
import com.triggertrap.fragments.DistanceLapseFragment;
import com.triggertrap.fragments.DistanceLapseFragment.DistanceLapseListener;
import com.triggertrap.fragments.GettingStartedFragment;
import com.triggertrap.fragments.HdrFragment;
import com.triggertrap.fragments.HdrTimeLapseFragment;
import com.triggertrap.fragments.NdCalculatorFragment;
import com.triggertrap.fragments.PebbleFragment;
import com.triggertrap.fragments.PebbleFragment.PebbleListener;
import com.triggertrap.fragments.PlaceHolderFragment;
import com.triggertrap.fragments.PressHoldFragment;
import com.triggertrap.fragments.PressHoldFragment.PressHoldListener;
import com.triggertrap.fragments.PulseSequenceFragment.PulseSequenceListener;
import com.triggertrap.fragments.QuickReleaseFragment;
import com.triggertrap.fragments.SelfTimerFragment;
import com.triggertrap.fragments.SoundSensorFragment;
import com.triggertrap.fragments.SoundSensorFragment.SoundSensorListener;
import com.triggertrap.fragments.StarTrailFragment;
import com.triggertrap.fragments.StartStopFragment;
import com.triggertrap.fragments.StartStopFragment.StartStopListener;
import com.triggertrap.fragments.SunriseSunsetFragment;
import com.triggertrap.fragments.TimeLapseFragment;
import com.triggertrap.fragments.TimeWarpFragment;
import com.triggertrap.fragments.TimedFragment;
import com.triggertrap.fragments.TimedFragment.TimedListener;
import com.triggertrap.fragments.TriggertrapFragment;
import com.triggertrap.fragments.WifiMasterFragment;
import com.triggertrap.fragments.WifiMasterFragment.WifiMasterListener;
import com.triggertrap.fragments.WifiSlaveFragment;
import com.triggertrap.fragments.WifiSlaveFragment.WifiSlaveListener;
import com.triggertrap.fragments.dialog.ErrorPlayServicesFragment;
import com.triggertrap.fragments.dialog.RunningActionDialog;
import com.triggertrap.fragments.handler.DrawerFragmentHandler;
import com.triggertrap.inputs.HeadsetWatcher;
import com.triggertrap.location.TTLocationService;
import com.triggertrap.service.TriggertrapService;
import com.triggertrap.service.TriggertrapService.TiggertrapServiceBinder;
import com.triggertrap.service.TriggertrapService.TriggertrapServiceListener;
import com.triggertrap.util.AppRater;
import com.triggertrap.util.DialpadManager;
import com.triggertrap.util.DialpadManager.DialPadInput;
import com.triggertrap.util.WarningMessageManager;
import com.triggertrap.wifi.TTServiceInfo;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends Activity implements PulseSequenceListener,
        WifiSlaveListener, WifiMasterListener, TriggertrapServiceListener,
        TimedListener, StartStopListener, SoundSensorListener, SelfTimerFragment.SelfTimerListener,
        PressHoldListener, SimpleModeListener, QuickReleaseFragment.QuickReleaseListener,
        DialpadManager.InputSelectionListener, DistanceLapseListener,
        PebbleListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private int mShotsTakenCount = 0;

    // Saved instance keys
    public static final String FRAGMENT_STATE = "fragment_state";
    public static final String FRAGMENT_TAG = "fragment_tag";

    private String mAppName;
    private String mSelectedItemName;
    private String[] mModesGroups;
    private ArrayList<String[]> mModes = new ArrayList<String[]>();
    private ArrayList<String[]> mModesSubText = new ArrayList<String[]>();
    private ArrayList<TypedArray> mModeIcons = new ArrayList<TypedArray>();
    private ExpandableListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private View mStatusBar;
    private TextView mStatusBarText;
    private HeadsetWatcher mWhatcher;
    private TTLocationService mLocationService = null;
    private DrawerFragmentHandler mDrawerFragHandler = null;
    private String mInitialFragmentTag = TTApp.FragmentTags.GETTING_STARTED;
    private Bundle mInitialFragmentState = null;

    // Service params
    private Dialog serviceErrorDialog = null;
    private TriggertrapService mService;
    private boolean mTriggertrapServiceBound = false;

    private DialpadManager mDialPadManager = null;
    private WarningMessageManager mWarningMessageManager;

    Animation mSlideUpFromBottom;
    Animation mSlideDownToBottom;

    private interface DrawerGroups {
        public static int WELCOME = 0;
        public static int CABLE_MODES = 1;
        public static int TIME_MODE = 2;
        public static int SOUND_MODES = 3;
        public static int HDR_MODES = 4;
        public static int REMOTE_MODES = 5;
        public static int SETTINGS = 7;
        public static int CALCULATORS = 6;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup default values of preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        boolean hasBeenRotated = false;
        // Check for a saved instance (Created when the device is rotated)
        if (savedInstanceState != null) {
//            Log.d(TAG, "onCreate Saved Instance state:" + savedInstanceState);
//            mInitialFragmentTag = savedInstanceState.getString(FRAGMENT_TAG);
//            mInitialFragmentState = savedInstanceState
//                    .getBundle(FRAGMENT_STATE);

            hasBeenRotated = true;
        }


        // For some reason launching from the history does not clear previous
        // intent
        // So we have to check this and ignore any intent extras.
        boolean isLauchedFromHistory = false;
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            Log.d(TAG, "FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY");
            isLauchedFromHistory = true;
        }

        // Check if this activity was passed an intent with extras
        // This would happen when we re-launch the App' from the notification
        // bar
        Bundle extras = getIntent().getExtras();
        if (extras != null && !isLauchedFromHistory && !hasBeenRotated) {

            String activeFragTag = extras.getString(FRAGMENT_TAG);
            Log.d(TAG, "Got extras from Intent setting initial Fragment: "
                    + activeFragTag);
            if (activeFragTag != null) {
                if (!activeFragTag.equals(TTApp.FragmentTags.NONE)) {
                    mInitialFragmentTag = activeFragTag;
                }
            }
        } else {
            mInitialFragmentTag = TTApp.getInstance(getApplicationContext()).getLastFragmentTag();
        }

        Log.d(TAG, "onCreate mInitialFragmentState: " +  mInitialFragmentState);
        Log.d(TAG, "onCreate Back stack count: " + getFragmentManager().getBackStackEntryCount());

        setContentView(R.layout.activity_main);

        setUpStatusBar();
        mAppName = getResources().getString(R.string.app_name);

        setUpFragmentHandler(hasBeenRotated);

        setUpModes();
        setUpNavigationDrawer();
        setUpDailPad();

        // Setup the warning message manager
        mWarningMessageManager = new WarningMessageManager(
                getApplicationContext(), findViewById(R.id.warningMessage));
        mWarningMessageManager.startListening();

        // Initialise App settings;
        TTApp.getInstance(this);

        // Start Headset whatcher
        mWhatcher = new HeadsetWatcher(this, null);

        // Initialise Location service
        mLocationService = new TTLocationService(this);

        if (!mTriggertrapServiceBound) {
            Intent intent = new Intent(this, TriggertrapService.class);
            bindService(intent, mTriggertrapServiceConnection,
                    Activity.BIND_AUTO_CREATE);
        }

        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        AudioManager audio = (AudioManager) getSystemService(this.AUDIO_SERVICE);
        // audio.setStreamVolume(AudioManager.STREAM_MUSIC,
        // audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

        mSlideUpFromBottom = AnimationUtils.loadAnimation(this,
                R.anim.slide_in_from_bottom);
        mSlideUpFromBottom.setInterpolator(new OvershootInterpolator(0.5f));
        mSlideUpFromBottom.setStartOffset(300);
        mSlideDownToBottom = AnimationUtils.loadAnimation(this,
                R.anim.slide_out_to_bottom);

        if (!hasBeenRotated) {
            AppRater.appLaunched(getApplicationContext(), this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the Triggertrap service when the actvity is shown.
        Log.d(TAG, "Service bound is: " + mTriggertrapServiceBound);

        Intent intent = new Intent(MainActivity.this, TriggertrapService.class);
        if (mService != null) {
            mService.goTobackground();
            stopService(intent);
            // Do we need to set the transientState of the Fragment
            setFragmentTransientState();
        }

        // Analytics stuff
        AnalyticTracker.getInstance(this).startSession();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

//        outState.putString(FRAGMENT_TAG,
//                mDrawerFragHandler.getCurrentFragmentTag());
//
//        // Get the current visible Fragment and save the transient state
//        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
//        Fragment fragment = getFragmentManager().findFragmentByTag(
//                currentFragTag);
//        Bundle fragmentState = null;
//        if (fragment != null && fragment.isVisible()) {
//            if (fragment instanceof TriggertrapFragment) {
//                TriggertrapFragment ttFragment = (TriggertrapFragment) fragment;
//                fragmentState = ttFragment.getStateBundle();
//            }
//        }
//        outState.putBundle(FRAGMENT_STATE, fragmentState);
    }

    @Override
    protected void onStop() {
        super.onStop();


        // Save the last shown Fragment Tag
        TTApp.getInstance(this).setLastFragmentTag(
                mDrawerFragHandler.getCurrentFragmentTag());
        TTApp.getInstance(this).setLastActionBarLabel(mSelectedItemName);
        TTApp.getInstance(this).setLastListItemChecked(
                mDrawerList.getCheckedItemPosition());

        if (isFinishing()) {
            Log.d(TAG, "MainActivity is Finishing");
        }
        Log.d(TAG,
                "Stopping Activity, isChangingConfigurations: "
                        + isChangingConfigurations() + " Service state: "
                        + mService.getState());
        // Make sure we reset the intent data
        setIntent(new Intent());

        // if (mService.getState() == TriggertrapService.State.IN_PROGRESS ) {
        // Keep the service alive we are just rotating.
        if (isChangingConfigurations()) {
            Log.d(TAG, "Starting in progress service to keep it alive");
            Intent intent = new Intent(this, TriggertrapService.class);
            startService(intent);
        }

        // If not changing configuration, Activity is not visible so run service
        // in foreground
        if (!isChangingConfigurations()
                && mService.getState() == TriggertrapService.State.IN_PROGRESS) {
            Intent intent = new Intent(this, TriggertrapService.class);
            startService(intent);
            mService.goToForeground();
        }

        setVolumeControlStream(AudioManager.STREAM_RING);


        // Analytics stuff
        AnalyticTracker.getInstance(this).endSession();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Flushing Mixpanel");
        AnalyticTracker.getInstance(this).flush();
        Log.d(TAG, "Destorying activity");
        // Only unbind from the service if the activity has been destroyed
        if (mTriggertrapServiceBound) {
            Log.d(TAG, "Unbinding service");
            // mService.setListener(null);
            unbindService(mTriggertrapServiceConnection);
            mTriggertrapServiceBound = false;
        }


        mWhatcher.unregister(this);
        mWarningMessageManager.stopListening();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mDialPadManager.getDialPadState() == DialpadManager.DialPadState.SHOWING) {
            mDialPadManager.deactiveInput();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Returning request code: " + requestCode);
        Log.d(TAG, "Returning result code: " + resultCode);

        switch (requestCode) {

            // Handle the result of checking the Location service.
            case TTLocationService.CONNECTION_FAILURE_RESOLUTION_REQUEST:
                if (serviceErrorDialog != null) {
                    serviceErrorDialog.dismiss();
                    serviceErrorDialog = null;
                }
            /*
             * If the result code is Activity.RESULT_OK, try to connect again
			 */
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // Call servicesConnected again to check that all is ok now.
                        DistanceLapseFragment distanceFragment = (DistanceLapseFragment) getFragmentManager()
                                .findFragmentByTag(TTApp.FragmentTags.DISTANCE_LAPSE);
                        if (mLocationService.servicesConnected(serviceErrorDialog)) {
                            if (mService != null) {
                                mService.setTTLocationService(mLocationService);
                            }
                            if (distanceFragment != null
                                    && distanceFragment.isVisible()) {
                                distanceFragment
                                        .setDistanceLapseState(DistanceLapseFragment.GooglePlayServiceState.SERVICE_AVAILABLE);
                            }
                        }

                    case Activity.RESULT_CANCELED:
                        // Display dialog: TT uses Google Play Location services to get
                        // location information, you cannot use distance lapse without a
                        // valid Google
                        // play account or Google Play installed.
                        ErrorPlayServicesFragment errorPlayServices = new ErrorPlayServicesFragment();
                        errorPlayServices.show(this);

                        break;
                }

        }

    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    public void stopRunningAction() {
        mService.stopCurrentAction();
        displaySatusBar();
    }

    public boolean checkInProgressState() {
        return mService.checkInProgressState();
    }

    private String getNotifcationText(int onGoingAction) {
        String[] notifcations = getResources().getStringArray(
                R.array.tt_notifications);
        String notifcationText = notifcations[onGoingAction];
        return notifcationText;
    }

    private void setUpStatusBar() {

        mStatusBar = findViewById(R.id.status);
        mStatusBarText = (TextView) findViewById(R.id.status_bar_text);

        View stopRunningAction = findViewById(R.id.stopRunningAction);
        stopRunningAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStatusBar.startAnimation(mSlideDownToBottom);
                stopRunningAction();
            }
        });
    }

    private void setUpFragmentHandler(boolean hasBeenRotated) {
        mDrawerFragHandler = new DrawerFragmentHandler(getFragmentManager(),
                R.id.content_frame);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.PLACEHOLDER,
                PlaceHolderFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.GETTING_STARTED,
                GettingStartedFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.BUY_DONGLE,
                CableSelectorFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.SIMPLE,
                CableReleaseFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.QUICK_RELEASE,
                QuickReleaseFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.PRESS_AND_HOLD,
                PressHoldFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.PRESS_TO_START,
                StartStopFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.TIMED,
                TimedFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.SELF_TIMER,
                SelfTimerFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.TIMELAPSE,
                TimeLapseFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.TIMEWARP,
                TimeWarpFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.STARTRAIL,
                StarTrailFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.BRAMPING,
                BrampingFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.BANG,
                SoundSensorFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.DISTANCE_LAPSE,
                DistanceLapseFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.HDR,
                HdrFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.HDR_LAPSE,
                HdrTimeLapseFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.WIFI_SLAVE,
                WifiSlaveFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.WIFI_MASTER,
                WifiMasterFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.PEBBLE,
                PebbleFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.SUNRISESUNSET,
                SunriseSunsetFragment.class, mInitialFragmentState);
        mDrawerFragHandler.addDrawerPane(TTApp.FragmentTags.ND_CALCULATOR, NdCalculatorFragment.class, mInitialFragmentState);
        mDrawerFragHandler.onDrawerSelected(this, mInitialFragmentTag, false, hasBeenRotated);

        // if(TTApp.getInstance(this).isFirstStarted()) {
        // mDrawerFragHandler.addBackstackFragment(this, WelcomeFragment.class);
        // }

    }

    private void setUpModes() {

        // Check if we want to show pebble
        FirmwareVersionInfo versionInfo = null;
        try {
            versionInfo = PebbleKit.getWatchFWVersion(getApplicationContext());
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        String[] tempRemoteTriggerModes = getResources().getStringArray(
                R.array.tt_remote_trigger_modes);
        String[] remoteTriggerModes;

        if (PebbleKit.isWatchConnected(getApplicationContext())
                && versionInfo != null && (versionInfo.getMajor() == 2)) {
            Log.d(TAG,
                    "Watch is connected and version is: "
                            + versionInfo.getTag());
            remoteTriggerModes = tempRemoteTriggerModes;
        } else {
            // Hide Pebble mode
            remoteTriggerModes = new String[1];
            remoteTriggerModes[0] = tempRemoteTriggerModes[0];
        }

        mModesGroups = getResources().getStringArray(R.array.tt_mode_groups);
        mModes.add(getResources().getStringArray(R.array.tt_welcome_modes));
        mModes.add(getResources().getStringArray(R.array.tt_cable_modes));
        mModes.add(getResources().getStringArray(R.array.tt_timer_modes));
        mModes.add(getResources().getStringArray(R.array.tt_sound_modes));
        mModes.add(getResources().getStringArray(R.array.tt_hdr_modes));
        mModes.add(remoteTriggerModes);
        mModes.add(getResources().getStringArray(R.array.tt_calculator_modes));
        mModes.add(getResources().getStringArray(R.array.tt_settings_modes));

        mModesSubText.add(getResources().getStringArray(
                R.array.tt_welcome_modes_sub_text));
        mModesSubText.add(getResources().getStringArray(
                R.array.tt_cable_modes_sub_text));
        mModesSubText.add(getResources().getStringArray(
                R.array.tt_timer_modes_sub_text));
        mModesSubText.add(getResources().getStringArray(
                R.array.tt_sound_modes_sub_text));
        mModesSubText.add(getResources().getStringArray(
                R.array.tt_hdr_modes_sub_text));
        mModesSubText.add(getResources().getStringArray(
                R.array.tt_remote_trigger_modes_sub_text));
        mModesSubText.add(getResources().getStringArray(
                R.array.tt_calculators_sub_text));
        mModesSubText.add(getResources().getStringArray(
                R.array.tt_settings_modes_sub_text));

        // Setup icons
        mModeIcons.add(getResources().obtainTypedArray(
                R.array.tt_welcome_mode_icons));
        mModeIcons.add(getResources().obtainTypedArray(
                R.array.tt_cable_mode_icons));
        mModeIcons.add(getResources().obtainTypedArray(
                R.array.tt_timer_mode_icons));
        mModeIcons.add(getResources().obtainTypedArray(
                R.array.tt_sound_mode_icons));
        mModeIcons.add(getResources().obtainTypedArray(R.array.tt_hdr_mode_icons));
        mModeIcons.add(getResources().obtainTypedArray(
                R.array.tt_remote_trigger_mode_icons));
        mModeIcons.add(getResources().obtainTypedArray(
                R.array.tt_calculators_mode_icons));
        mModeIcons.add(getResources().obtainTypedArray(
                R.array.tt_settings_mode_icons));
    }

    private void setUpNavigationDrawer() {
        DrawerExpandableListAdapter drawListAdapter = new DrawerExpandableListAdapter();
        mDrawerList = (ExpandableListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(drawListAdapter);
        mDrawerList.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                return true; // This way the expander cannot be collapsed
            }
        });

        // Make sure all the Expandable List Groups are open
        int count = drawListAdapter.getGroupCount();
        for (int position = 1; position <= count; position++) {
            mDrawerList.expandGroup(position - 1);
        }

        mDrawerList.setOnChildClickListener(new DrawerItemClickListener());
        int index = TTApp.getInstance(this).getLastListItemChecked();
        mDrawerList.setItemChecked(index, true);
        mSelectedItemName = TTApp.getInstance(getApplicationContext())
                .getLastActionBarLabel();
        setTitle(mSelectedItemName);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.nav_drawer_open,
                R.string.nav_drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mSelectedItemName);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
                // changeFragment();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                dismissFragmentError();
                getActionBar().setTitle(mAppName);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                dismissFragmentError();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

    }

    private void setUpDailPad() {
        View dialPad = findViewById(R.id.dialPad);
        mDialPadManager = new DialpadManager(this, dialPad);
        dialPad.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Do nothing just comsume the click.

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        dismissFragmentError();
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_pebble:
                // String url = "http://tri.gg/tt-pebble.pbw";
                String url = "pebble://appstore/52cb079345ffdd6857000014";
                Intent pebbleIntent = new Intent(Intent.ACTION_VIEW);
                pebbleIntent.setData(Uri.parse(url));
                pebbleIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(pebbleIntent);

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements
            ExpandableListView.OnChildClickListener {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v,
                                    int groupPosition, int childPosition, long id) {
            int index = parent.getFlatListPosition(ExpandableListView
                    .getPackedPositionForChild(groupPosition, childPosition));
            parent.setItemChecked(index, true);
            selectItem(groupPosition, childPosition, index);
            return true;
        }
    }

    private void selectItem(int group, int position, int listItemIndex) {
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(listItemIndex, true);
        mSelectedItemName = mModes.get(group)[position];
        setTitle(mSelectedItemName);
        if (mDialPadManager.getDialPadState() == DialpadManager.DialPadState.SHOWING) {
            mDialPadManager.deactiveInput();
        }
        changeFragment(group, position, listItemIndex);
        // setWifiState();
        setFragmentTransientState();
        displaySatusBar();
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void changeFragment(int group, int position, int listItemIndex) {
        // Only need this for getting rid of the welcome fragment at the moment.
        // FragmentManager fm = getFragmentManager();
        // fm.popBackStack();
        switch (group) {
            case DrawerGroups.WELCOME:
                if (position == 0) {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.GETTING_STARTED, true, false);
                } else {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.BUY_DONGLE, true, false);
                }
                break;
            case DrawerGroups.CABLE_MODES:
                if (position == 0) {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.SIMPLE, true, false);
                } else if (position == 1) {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.QUICK_RELEASE, true, false);
                } else if (position == 2) {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.PRESS_AND_HOLD, true, false);
                } else if (position == 3) {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.PRESS_TO_START, true, false);
                } else if (position == 4) {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.TIMED, true, false);
                } else if (position == 5) {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.SELF_TIMER, true, false);
                }
                break;
            case DrawerGroups.TIME_MODE:
                if (position == 0) {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.TIMELAPSE, true, false);
                } else if (position == 1) {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.TIMEWARP, true, false);
                } else if (position == 2) {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.DISTANCE_LAPSE, true, false);
                    if (mLocationService.servicesConnected(serviceErrorDialog)) {
                        if (mService != null) {
                            mService.setTTLocationService(mLocationService);
                        }
                        DistanceLapseFragment distanceFragment = (DistanceLapseFragment) getFragmentManager()
                                .findFragmentByTag(
                                        TTApp.FragmentTags.DISTANCE_LAPSE);
                        if (distanceFragment != null) {
                            distanceFragment
                                    .setDistanceLapseState(DistanceLapseFragment.GooglePlayServiceState.SERVICE_AVAILABLE);
                        }
                    }
                } else if (position == 3) {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.STARTRAIL, true, false);
                } else if (position == 4) {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.BRAMPING, true, false);
                }
                break;
            case DrawerGroups.SOUND_MODES:
                if (position == 0) {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.BANG, true, false);
                }
                break;

            case DrawerGroups.HDR_MODES:
                if (position == 0) {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.HDR, true, false);
                } else if (position == 1) {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.HDR_LAPSE, true, false);
                }
                break;
            case DrawerGroups.REMOTE_MODES:
                if (position == 0) {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.WIFI_SLAVE, true, false);
                } else if (position == 1) {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.PEBBLE, true, false);
                }
                break;
            case DrawerGroups.SETTINGS:
                if (position == 0) {

                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.WIFI_MASTER, true, false);
                }
                break;

            case DrawerGroups.CALCULATORS:
                if (position == 0) {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.SUNRISESUNSET, true, false);
                } else if (position == 1) {
                    mDrawerFragHandler.onDrawerSelected(this,
                            TTApp.FragmentTags.ND_CALCULATOR, true, false);
                }
                break;
            default:
                mDrawerFragHandler.onDrawerSelected(this,
                        TTApp.FragmentTags.PLACEHOLDER, true, false);
        }
    }

    private void dismissFragmentError() {
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        Fragment fragment = getFragmentManager().findFragmentByTag(
                currentFragTag);
        if (fragment != null) {
            TriggertrapFragment ttFragment = (TriggertrapFragment) fragment;
            ttFragment.dismissError();

        }
    }

    /**
     * The Fragment may be running an Action we need to check with Service
     */
    private void setFragmentTransientState() {
        Log.d(TAG, "Setting Transitent state");
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        Fragment fragment = getFragmentManager().findFragmentByTag(
                currentFragTag);
        if (fragment != null) {
            TriggertrapFragment ttFragment = (TriggertrapFragment) fragment;
            ttFragment
                    .setActionState(mService.isFragmentActive(currentFragTag));

            // If we are showing the sound sensor make sure we are running the
            // MicVolumeMonitor
            if (ttFragment instanceof SoundSensorFragment) {
                onStartSoundSensor();
            }

            // If we have an inactive distance lapse make sure the Location
            // service is register and running
            if (!(mService.isFragmentActive(currentFragTag) && ttFragment instanceof DistanceLapseFragment)) {
                if (mLocationService.servicesConnected(serviceErrorDialog)) {
                    if (mService != null) {
                        Log.d(TAG, "Setting Location Service");
                        mService.setTTLocationService(mLocationService);
                    }
                    DistanceLapseFragment distanceFragment = (DistanceLapseFragment) getFragmentManager()
                            .findFragmentByTag(
                                    TTApp.FragmentTags.DISTANCE_LAPSE);
                    if (distanceFragment != null) {
                        distanceFragment
                                .setDistanceLapseState(DistanceLapseFragment.GooglePlayServiceState.SERVICE_AVAILABLE);
                    }
                }

            }

            // If we have distance lapse makes sure we update its progress
            // appropriately
            if (mService.isFragmentActive(currentFragTag)
                    && ttFragment instanceof DistanceLapseFragment) {
                DistanceLapseFragment disrFrag = (DistanceLapseFragment) ttFragment;
                disrFrag.onDistanceLapseUpdate(
                        mService.getAccumulativeDistance(), mService.getSpeed());
            }

            if (mService.isFragmentActive(currentFragTag)
                    && ttFragment instanceof WifiSlaveFragment) {
                WifiSlaveFragment slaveFrag = (WifiSlaveFragment) ttFragment;
                slaveFrag.addAvaiableMasters(mService.getAvailableMasters());
            }

            if (ttFragment instanceof WifiMasterFragment) {
                WifiMasterFragment masterFrag = (WifiMasterFragment) ttFragment;
                ttFragment.setActionState(mService.isWifiMasterOn());
                masterFrag.addConnectedSlaves(mService.getConnectedSlaves());
            }

            displaySatusBar();
        }
    }

    public void setWifiState() {
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        if (currentFragTag.equals(TTApp.FragmentTags.WIFI_SLAVE)) {
            Log.d(TAG, "Watching wifi master....");
            mService.watchMasterWifi();
        } else {
            mService.unWatchMasterWifi();
        }
    }

    private void displaySatusBar() {
        if (mService.getState() == TriggertrapService.State.IN_PROGRESS) {
            Log.d(TAG, "Service State is IN_PROGRESS");
            String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
            Fragment fragment = getFragmentManager().findFragmentByTag(
                    currentFragTag);
            if (fragment != null) {
                if (fragment instanceof TriggertrapFragment) {
                    TriggertrapFragment triggertrapFrag = (TriggertrapFragment) fragment;
                    if (triggertrapFrag.getRunningAction() != mService
                            .getOnGoingAction()) {
                        mStatusBar.setVisibility(View.VISIBLE);
                        mStatusBarText.setText(getNotifcationText(mService
                                .getOnGoingAction()));
                        mStatusBar.startAnimation(mSlideUpFromBottom);
                        // //Animating the status bar in
                        // mStatusBar.setRotationX(90);
                        // mStatusBar.setPivotY(100);
                        // mStatusBar.setPivotX(100);
                        // mStatusBar.setAlpha(0.5f);
                        // mStatusBar.animate().rotationX(0).alpha(1.0f).setDuration(500).setInterpolator(new
                        // OvershootInterpolator(5.0f));
                    } else {
                        // Animating the status bar out
                        // mStatusBar.setPivotY(100);
                        // mStatusBar.setPivotX(100);
                        // mStatusBar.animate().rotationX(90).alpha(0.5f).setDuration(800);
                        mStatusBar.startAnimation(mSlideDownToBottom);
                        mStatusBar.setVisibility(View.INVISIBLE);
                    }
                }
            }
        } else {
            Log.d(TAG, "Service State is IDLE");
            // Animating the status bar out
            // mStatusBar.setPivotY(100);
            // mStatusBar.setPivotX(100);
            // mStatusBar.animate().rotationX(90).alpha(0.5f).setDuration(800);
            mStatusBar.setVisibility(View.INVISIBLE);
        }
    }

    private void removeStatusBar() {
        mStatusBar.startAnimation(mSlideDownToBottom);
        mStatusBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setTitle(CharSequence title) {
        getActionBar().setTitle(title);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and
     * onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public class DrawerExpandableListAdapter extends BaseExpandableListAdapter {

        LayoutInflater inflater = (LayoutInflater) MainActivity.this
                .getSystemService(MainActivity.LAYOUT_INFLATER_SERVICE);

        public Object getChild(int groupPosition, int childPosition) {
            return mModes.get(groupPosition)[childPosition];
        }

        public String getChildSubText(int groupPosition, int childPosition) {
            return mModesSubText.get(groupPosition)[childPosition];
        }

        public int getIcon(int groupPosition, int childPosition) {
            int icon;
            if (groupPosition > (mModeIcons.size() - 1)) {
                icon = R.drawable.icon_happy;
            } else {
                icon = mModeIcons.get(groupPosition).getResourceId(childPosition, -1);
            }
            return icon;
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            return mModes.get(groupPosition).length;
        }

        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View view, ViewGroup parent) {

            TextView textView = null;
            TextView subText = null;
            ImageView icon = null;
            if (view == null) {
                view = inflater.inflate(R.layout.drawer_list_item, null);
                textView = (TextView) view
                        .findViewById(R.id.drawListItemTextTitle);
                subText = (TextView) view
                        .findViewById(R.id.drawListItemTextSubtitle);
                textView.setTypeface(TTApp.getInstance(getApplicationContext()).SAN_SERIF_LIGHT);
                subText.setTypeface(TTApp.getInstance(getApplicationContext()).SAN_SERIF_LIGHT);

                icon = (ImageView) view.findViewById(R.id.drawlist_item_icon);
            }
            if (textView == null) {
                textView = (TextView) view
                        .findViewById(R.id.drawListItemTextTitle);
                subText = (TextView) view
                        .findViewById(R.id.drawListItemTextSubtitle);
                icon = (ImageView) view.findViewById(R.id.drawlist_item_icon);
            }

            textView.setText(getChild(groupPosition, childPosition).toString());
            subText.setText(getChildSubText(groupPosition, childPosition));
            icon.setImageResource(getIcon(groupPosition, childPosition));

            ImageView itemImage = (ImageView) view
                    .findViewById(R.id.drawlist_item_image);

            switch (childPosition) {
                case 0:
                    itemImage.setColorFilter(0x00000000);
                    break;
                case 1:
                    itemImage.setColorFilter(0x19000000);
                    break;
                case 2:
                    itemImage.setColorFilter(0x33000000);
                    break;
                case 3:
                    itemImage.setColorFilter(0x4c000000);
                    break;
                case 4:
                    itemImage.setColorFilter(0x64000000);
                    break;
                case 5:
                    itemImage.setColorFilter(0x7d000000);
                    break;

            }
            return view;
        }

        public Object getGroup(int groupPosition) {
            return mModesGroups[groupPosition];
        }

        public int getGroupCount() {
            return mModesGroups.length;
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View view, ViewGroup parent) {

            if (view == null) {
                view = inflater.inflate(R.layout.drawer_list_header_item, null);
            }
            TextView textView = (TextView) view
                    .findViewById(R.id.drawlist_header_item_text);

            textView.setText(getGroup(groupPosition).toString());
            return view;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }

    }

    /*
     * Listener for Simple mode
     */
    @Override
    public void onPressSimple() {
        mService.startSimple();
    }

    /*
     * Listener for PulseSequence Fragments
     */
    @Override
    public void onPulseSequenceCreated(int onGoingAction, long[] sequence,
                                       boolean repeat) {
        if (mTriggertrapServiceBound) {
            Log.d(TAG, "Starting Pulse sequence: Action:" + onGoingAction);
            mService.startPulseSequence(onGoingAction, sequence, repeat);
            mService.resetSequenceStartStopTime();

        }
    }

    @Override
    public void onPulseSequenceCancelled() {
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        Fragment fragment = getFragmentManager().findFragmentByTag(
                currentFragTag);

        switch (mService.getOnGoingAction()) {
            case TTApp.OnGoingAction.TIMEWARP:

                TimeWarpFragment timewarpFrag = (TimeWarpFragment) fragment;
                if (timewarpFrag != null) {
                    timewarpFrag.onPulseStop();

                    long sequenceMillis = Calendar.getInstance().getTimeInMillis() -
                            mService.getSequenceStartStopTime().getTimeInMillis();

                    AnalyticTracker tracker = AnalyticTracker.getInstance(this);
                    tracker.addProperty(AnalyticTracker.Property.MODE, "TimeWarp");
                    tracker.addProperty(AnalyticTracker.Property.NO_EXPOSURES_TAKEN,
                            timewarpFrag.getCurrentExposureCount());
                    tracker.addProperty(AnalyticTracker.Property.SEQUENCE_DURATION, sequenceMillis);
                    tracker.trackEvent(AnalyticTracker.Event.SEQUENCE_COMPLETED);
                }
                break;
            case TTApp.OnGoingAction.STAR_TRAIL:
                StarTrailFragment startFrag = (StarTrailFragment) fragment;
                if (startFrag != null) {

                    long sequenceMillis = Calendar.getInstance().getTimeInMillis() -
                            mService.getSequenceStartStopTime().getTimeInMillis();

                    AnalyticTracker tracker = AnalyticTracker.getInstance(this);
                    tracker.addProperty(AnalyticTracker.Property.MODE, "Star Trail");
                    tracker.addProperty(AnalyticTracker.Property.NO_EXPOSURES_TAKEN,
                            startFrag.getExposureCount());
                    tracker.addProperty(AnalyticTracker.Property.EXPOSURE_DURATION,
                            startFrag.getExposureTime());
                    tracker.addProperty(AnalyticTracker.Property.SEQUENCE_DURATION, sequenceMillis);
                    tracker.trackEvent(AnalyticTracker.Event.SEQUENCE_COMPLETED);
                }

                break;
            case TTApp.OnGoingAction.BRAMPING:
                BrampingFragment brampFrag = (BrampingFragment) fragment;
                if (brampFrag != null) {
                    brampFrag.onPulseStop();

                    long sequenceMillis = Calendar.getInstance().getTimeInMillis() -
                            mService.getSequenceStartStopTime().getTimeInMillis();

                    AnalyticTracker tracker = AnalyticTracker.getInstance(this);
                    tracker.addProperty(AnalyticTracker.Property.MODE, "Bramping");
                    tracker.addProperty(AnalyticTracker.Property.NO_EXPOSURES_TAKEN,
                            brampFrag.getCurrentExposureCount());
                    tracker.addProperty(AnalyticTracker.Property.SEQUENCE_DURATION, sequenceMillis);
                    tracker.trackEvent(AnalyticTracker.Event.SEQUENCE_COMPLETED);
                }

                break;
            case TTApp.OnGoingAction.HDR:
                HdrFragment hdrFragment = (HdrFragment) fragment;
                if (hdrFragment != null) {
                    long sequenceMillis = Calendar.getInstance().getTimeInMillis() -
                            mService.getSequenceStartStopTime().getTimeInMillis();

                    AnalyticTracker tracker = AnalyticTracker.getInstance(this);
                    tracker.addProperty(AnalyticTracker.Property.MODE, "LE HDR");
                    tracker.addProperty(AnalyticTracker.Property.NO_EXPOSURES_TAKEN,
                            hdrFragment.getCompletedExposures());
                    tracker.addProperty(AnalyticTracker.Property.SEQUENCE_DURATION, sequenceMillis);
                    tracker.trackEvent(AnalyticTracker.Event.SEQUENCE_COMPLETED);
                }

                break;
            case TTApp.OnGoingAction.HDR_TIMELAPSE:

                HdrTimeLapseFragment hdrTimeLapseFragment = (HdrTimeLapseFragment) fragment;
                if (hdrTimeLapseFragment != null) {
                    long sequenceMillis = Calendar.getInstance().getTimeInMillis() -
                            mService.getSequenceStartStopTime().getTimeInMillis();

                    AnalyticTracker tracker = AnalyticTracker.getInstance(this);
                    tracker.addProperty(AnalyticTracker.Property.MODE, "LE HDR Timelapse");
                    tracker.addProperty(AnalyticTracker.Property.NO_EXPOSURES_TAKEN,
                            hdrTimeLapseFragment.getCompletedExposures());
                    tracker.addProperty(AnalyticTracker.Property.SEQUENCE_DURATION, sequenceMillis);
                    tracker.trackEvent(AnalyticTracker.Event.SEQUENCE_COMPLETED);
                }
                break;

            case TTApp.OnGoingAction.TIMELAPSE:

                long sequenceMillis = Calendar.getInstance().getTimeInMillis() -
                        mService.getSequenceStartStopTime().getTimeInMillis();

                TimeLapseFragment timeLapseFragment = (TimeLapseFragment) fragment;

                AnalyticTracker tracker = AnalyticTracker.getInstance(this);
                tracker.addProperty(AnalyticTracker.Property.MODE, "Timelapse");
                tracker.addProperty(AnalyticTracker.Property.NO_EXPOSURES_TAKEN,
                        timeLapseFragment.getCurrentExposureCount());
                tracker.addProperty(AnalyticTracker.Property.EXPOSURE_DURATION,
                        TTApp.getInstance(this).getBeepLength());
                tracker.addProperty(AnalyticTracker.Property.SEQUENCE_DURATION, sequenceMillis);
                tracker.trackEvent(AnalyticTracker.Event.SEQUENCE_COMPLETED);

                break;
        }

        if (mTriggertrapServiceBound) {
            mService.stopSequence();
        }
    }

    /*
     * Listener for TimedFragment Fragments
     */
    @Override
    public void onTimedStarted(long time) {
        mService.startTimedMode(time);
    }

    @Override
    public void onTimedStopped() {
        mService.stopTimedMode();

    }

    /**
     * SelfTimer Listener functions
     */

    @Override
    public void onSelfTimerStarted(long time) {
        mService.startSelfTimer(time);
    }

    @Override
    public void onSelfTimerStopped() {
        //Cancel timer
        mService.stopSelfTimerMode();
    }

    /*
     * Listener for StartStop Fragment
     */
    @Override
    public void onStopwatchStarted() {
        mService.startStopwatch();

    }

    @Override
    public void onStopwatchStopped() {
        mService.stopStopWatch();

    }

    /*
     * Listener for press hold fragment Fragment
     */
    @Override
    public void onPressStarted() {
        mService.onStartPress();
    }

    @Override
    public void onPressStopped() {
        mService.onStopPress();
    }

    /*
     * Listener for Wifi slave Fragment
	 */
    @Override
    public void onWatchMaster() {
        mService.watchMasterWifi();

    }

    @Override
    public void onUnwatchMaster() {
        mService.unWatchMasterWifi();

    }

    /*
     * Listener for SoundSensor (Bang) Fragment
     */
    @Override
    public void onStartSoundSensor() {
        if (mService != null) {
            mService.startSoundSensor();
            mService.resetSequenceStartStopTime();
        }

    }

    @Override
    public void onStopSoundSensor() {
        if (mService != null) {
            mService.stopSoundSensor();
        }
    }

    @Override
    public void onEnableSoundThreshold() {
        mService.enableSoundThreshold();

    }

    @Override
    public void onDisableSoundThreshold() {
        mService.disableSoundThreshold();

        long sequenceMillis = Calendar.getInstance().getTimeInMillis() -
                mService.getSequenceStartStopTime().getTimeInMillis();

        AnalyticTracker tracker = AnalyticTracker.getInstance(this);
        tracker.addProperty(AnalyticTracker.Property.MODE, "Sound Sensor");
        tracker.addProperty(AnalyticTracker.Property.NO_EXPOSURES_TAKEN,
                mShotsTakenCount);
        tracker.addProperty(AnalyticTracker.Property.SENSOR_DELAY,
                TTApp.getInstance(this).getSensorDelay());
        tracker.addProperty(AnalyticTracker.Property.SENSOR_RESET_DELAY,
                TTApp.getInstance(this).getSensorResetDelay());
        tracker.addProperty(AnalyticTracker.Property.EXPOSURE_DURATION,
                TTApp.getInstance(this).getBeepLength());
        tracker.addProperty(AnalyticTracker.Property.SEQUENCE_DURATION, sequenceMillis);
        tracker.trackEvent(AnalyticTracker.Event.SEQUENCE_COMPLETED);

        mShotsTakenCount = 0;

    }

    @Override
    public void onSetMicSensitivity(int sensitivity) {
        if (mService != null) {
            mService.setMicSensitivity(sensitivity);
        }
    }

    @Override
    public void onSetSoundThreshold(int threshold) {
        if (mService != null) {
            mService.setSoundThreshold(threshold);
        }
    }

    /*
     * Listener for DistanceLapse Fragment
     */
    @Override
    public void onStartDistanceLapse(int distance) {
        if (mService != null) {
            mService.startLocationUpdates(distance);
            mService.resetSequenceStartStopTime();
        }
    }

    @Override
    public void onStopDistanceLapse() {
        if (mService != null) {
            mService.stopLocationUpdates();

            Fragment fragment = getFragmentManager().findFragmentByTag(
                    TTApp.FragmentTags.DISTANCE_LAPSE);

            DistanceLapseFragment distanceLapsefragment = (DistanceLapseFragment) fragment;

            long sequenceMillis = Calendar.getInstance().getTimeInMillis() -
                    mService.getSequenceStartStopTime().getTimeInMillis();

            AnalyticTracker tracker = AnalyticTracker.getInstance(this);
            tracker.addProperty(AnalyticTracker.Property.MODE, "DistanceLapse");
            tracker.addProperty(AnalyticTracker.Property.NO_EXPOSURES_TAKEN, distanceLapsefragment.getExposureCount());
            tracker.addProperty(AnalyticTracker.Property.EXPOSURE_DURATION, TTApp.getInstance(this).getBeepLength());
            tracker.addProperty(AnalyticTracker.Property.SEQUENCE_DURATION, sequenceMillis);
            tracker.addProperty(AnalyticTracker.Property.SPEED_UNIT, TTApp.getInstance(this).getDistlapseSpeedUnit());
            tracker.addProperty(AnalyticTracker.Property.DISTANCE_UNIT,
                    TTApp.getInstance(this).getDistlapseUnit());
            tracker.trackEvent(AnalyticTracker.Event.SEQUENCE_COMPLETED);
        }
    }

    /**
     * Listeners for the Triggertrap service
     */
    @Override
    public void onServiceActionRunning(String action) {
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        TriggertrapFragment ttFragment = (TriggertrapFragment) getFragmentManager()
                .findFragmentByTag(currentFragTag);
        if (ttFragment != null) {
            ttFragment.setActionState(false);
        }
        RunningActionDialog runningActionDialog = new RunningActionDialog();
        runningActionDialog.show(this, action);
    }

    @Override
    public void onServiceStartSimple() {
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        if (currentFragTag.equals(TTApp.FragmentTags.SIMPLE)) {
            AnalyticTracker tracker = AnalyticTracker.getInstance(this);
            tracker.addProperty(AnalyticTracker.Property.MODE, "Simple Cable Release");
            tracker.addProperty(AnalyticTracker.Property.NO_EXPOSURES_TAKEN, 1);
            tracker.addProperty(AnalyticTracker.Property.EXPOSURE_DURATION,
                    TTApp.getInstance(this).getBeepLength());
            tracker.trackEvent(AnalyticTracker.Event.SEQUENCE_COMPLETED);
        }

    }
    /*
    * Listener for quick release Fragment
     */

    public void onQuickPressStarted() {
        mService.onQuickStartPress();
    }

    public void onQuickPressStopped() {
        mService.onQuickStopPress();
    }


    @Override
    public void onServicePressStart() {
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        if (currentFragTag.equals(TTApp.FragmentTags.PRESS_AND_HOLD)
                && mService.getOnGoingAction() == TTApp.OnGoingAction.PRESS_AND_HOLD) {
            Fragment fragment = getFragmentManager().findFragmentByTag(
                    currentFragTag);
            PressHoldFragment pressHoldFragment = (PressHoldFragment) fragment;
            pressHoldFragment.startStopwatch();
        } else if (currentFragTag.equals(TTApp.FragmentTags.QUICK_RELEASE) && mService
                .getOnGoingAction() == TTApp.OnGoingAction.QUICK_RELEASE) {
            Fragment fragment = getFragmentManager().findFragmentByTag(
                    currentFragTag);
            QuickReleaseFragment quickReleaseFragment = (QuickReleaseFragment) fragment;
            quickReleaseFragment.startStopwatch();

        }
    }

    @Override
    public void onServicePressUpdate(long time) {
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();

        if (currentFragTag.equals(TTApp.FragmentTags.PRESS_AND_HOLD)
                && mService.getOnGoingAction() == TTApp.OnGoingAction.PRESS_AND_HOLD) {
            Fragment fragment = getFragmentManager().findFragmentByTag(
                    currentFragTag);
            if (fragment != null) {
                PressHoldFragment pressHoldFragment = (PressHoldFragment) fragment;
                pressHoldFragment.updateStopwatch(time);
            }
        } else if (currentFragTag.equals((TTApp.FragmentTags.QUICK_RELEASE))) {
            Fragment fragment = getFragmentManager().findFragmentByTag(
                    currentFragTag);
            if (fragment != null) {
                QuickReleaseFragment quickReleaseFragmentFragment = (QuickReleaseFragment) fragment;
                quickReleaseFragmentFragment.updateStopwatch(time);
            }

        }

    }

    @Override
    public void onServicePressStop() {
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();

        if (currentFragTag.equals(TTApp.FragmentTags.PRESS_AND_HOLD)) {
            Fragment fragment = getFragmentManager().findFragmentByTag(
                    currentFragTag);
            if (fragment != null) {
                PressHoldFragment pressHoldFragment = (PressHoldFragment) fragment;
                pressHoldFragment.stopStopwatch();

                AnalyticTracker tracker = AnalyticTracker.getInstance(this);
                tracker.addProperty(AnalyticTracker.Property.MODE, "Press and Hold");
                tracker.addProperty(AnalyticTracker.Property.NO_EXPOSURES_TAKEN, 1);
                tracker.addProperty(AnalyticTracker.Property.SEQUENCE_DURATION, pressHoldFragment.getLastTimeSet());
                tracker.addProperty(AnalyticTracker.Property.EXPOSURE_DURATION, pressHoldFragment.getLastTimeSet());
                tracker.trackEvent(AnalyticTracker.Event.SEQUENCE_COMPLETED);
            }
            // Check if we need to remove the status bar
            // displaySatusBar();
        } else if (currentFragTag.equals((TTApp.FragmentTags.QUICK_RELEASE))) {
            Fragment fragment = getFragmentManager().findFragmentByTag(
                    currentFragTag);
            if (fragment != null) {
                QuickReleaseFragment quickReleaseFragmentFragment = (QuickReleaseFragment) fragment;
                quickReleaseFragmentFragment.stopStopwatch();

                AnalyticTracker tracker = AnalyticTracker.getInstance(this);
                tracker.addProperty(AnalyticTracker.Property.MODE, "Quick Release");
                tracker.addProperty(AnalyticTracker.Property.NO_EXPOSURES_TAKEN, 1);
                tracker.addProperty(AnalyticTracker.Property.EXPOSURE_DURATION,
                        TTApp.getInstance(this).getBeepLength());
                tracker.trackEvent(AnalyticTracker.Event.SEQUENCE_COMPLETED);
            }
        }
    }

    @Override
    public void onServiceStopwatchStart() {
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        if (currentFragTag.equals(TTApp.FragmentTags.PRESS_TO_START)
                && mService.getOnGoingAction() == TTApp.OnGoingAction.PRESS_START_STOP) {
            Fragment fragment = getFragmentManager().findFragmentByTag(
                    currentFragTag);
            StartStopFragment startFragment = (StartStopFragment) fragment;
            startFragment.startStopwatch();
        }

    }

    @Override
    public void onServiceStopwatchUpdate(long time) {
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        if (currentFragTag.equals(TTApp.FragmentTags.PRESS_TO_START)
                && mService.getOnGoingAction() == TTApp.OnGoingAction.PRESS_START_STOP) {
            Fragment fragment = getFragmentManager().findFragmentByTag(
                    currentFragTag);
            if (fragment != null) {
                StartStopFragment startStopFrag = (StartStopFragment) fragment;
                startStopFrag.updateStopwatch(time);
            }
        }

    }

    @Override
    public void onServiceStopwatchStop() {
        String startStopFragTag = TTApp.FragmentTags.PRESS_TO_START;
        Fragment fragment = getFragmentManager().findFragmentByTag(
                startStopFragTag);
        if (fragment != null) {
            StartStopFragment startStopFrag = (StartStopFragment) fragment;
            startStopFrag.stopStopwatch();

            AnalyticTracker tracker = AnalyticTracker.getInstance(this);
            tracker.addProperty(AnalyticTracker.Property.MODE, "Press and lock");
            tracker.addProperty(AnalyticTracker.Property.NO_EXPOSURES_TAKEN, 1);
            tracker.addProperty(AnalyticTracker.Property.SEQUENCE_DURATION, startStopFrag.getLastTimeSet());
            tracker.addProperty(AnalyticTracker.Property.EXPOSURE_DURATION, startStopFrag.getLastTimeSet());
            tracker.trackEvent(AnalyticTracker.Event.SEQUENCE_COMPLETED);
        }
        // Check if we need to remove the status bar
        // displaySatusBar();

    }

    @Override
    public void onServiceTimedStart(long time) {
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        if (currentFragTag.equals(TTApp.FragmentTags.TIMED)
                && mService.getOnGoingAction() == TTApp.OnGoingAction.TIMED) {
            Fragment fragment = getFragmentManager().findFragmentByTag(
                    currentFragTag);
            TimedFragment timedFrag = (TimedFragment) fragment;
            timedFrag.startTimer(time);
        } else if (currentFragTag.equals(TTApp.FragmentTags.SELF_TIMER) && mService
                .getOnGoingAction() == TTApp.OnGoingAction.SELF_TIMER) {
            Fragment fragment = getFragmentManager().findFragmentByTag(
                    currentFragTag);
            SelfTimerFragment timedFrag = (SelfTimerFragment) fragment;
            timedFrag.startTimer(time);
        }

    }

    @Override
    public void onServiceTimedUpdate(long time) {
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        if (currentFragTag.equals(TTApp.FragmentTags.TIMED)
                && mService.getOnGoingAction() == TTApp.OnGoingAction.TIMED) {
            Fragment fragment = getFragmentManager().findFragmentByTag(
                    currentFragTag);
            if (fragment != null) {
                TimedFragment timedFrag = (TimedFragment) fragment;
                timedFrag.updateTimer(time);
            }
        } else if (currentFragTag.equals(TTApp.FragmentTags.SELF_TIMER) && mService
                .getOnGoingAction() == TTApp.OnGoingAction.SELF_TIMER) {
            Fragment fragment = getFragmentManager().findFragmentByTag(
                    currentFragTag);
            if (fragment != null) {
                SelfTimerFragment timerFrag = (SelfTimerFragment) fragment;
                timerFrag.updateTimer(time);
            }
        }

    }

    @Override
    public void onServiceTimedStop() {
        // Just make sure the TimedFragment has its state reset

        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        Fragment fragment = getFragmentManager()
                .findFragmentByTag(currentFragTag);

        if (currentFragTag.equals(TTApp.FragmentTags.TIMED)) {

            if (fragment != null) {
                TimedFragment timedFrag = (TimedFragment) fragment;
                timedFrag.stopTimer();

                AnalyticTracker tracker = AnalyticTracker.getInstance(this);
                tracker.addProperty(AnalyticTracker.Property.MODE, "Timed Release");
                tracker.addProperty(AnalyticTracker.Property.NO_EXPOSURES_TAKEN, 1);
                tracker.addProperty(AnalyticTracker.Property.EXPOSURE_DURATION, timedFrag.getTimerDuration());
                tracker.addProperty(AnalyticTracker.Property.SEQUENCE_DURATION, timedFrag.getTimerDuration());
                tracker.trackEvent(AnalyticTracker.Event.SEQUENCE_COMPLETED);
            }


        } else if (currentFragTag.equals(TTApp.FragmentTags.SELF_TIMER)) {


            String selfTimerFragTag = TTApp.FragmentTags.SELF_TIMER;
            fragment = getFragmentManager().findFragmentByTag(selfTimerFragTag);

            if (fragment != null) {
                SelfTimerFragment timerFrag = (SelfTimerFragment) fragment;
                timerFrag.stopTimer();

                AnalyticTracker tracker = AnalyticTracker.getInstance(this);
                tracker.addProperty(AnalyticTracker.Property.MODE, "Self Timer");
                tracker.addProperty(AnalyticTracker.Property.NO_EXPOSURES_TAKEN, 1);
                tracker.addProperty(AnalyticTracker.Property.EXPOSURE_DURATION,
                        TTApp.getInstance(this).getBeepLength());
                tracker.addProperty(AnalyticTracker.Property.SEQUENCE_DURATION, timerFrag.getTimerDuration());
                tracker.trackEvent(AnalyticTracker.Event.SEQUENCE_COMPLETED);
            }
        }

        if (!currentFragTag.equals(TTApp.FragmentTags.TIMED) && !currentFragTag.equals(TTApp.FragmentTags.SELF_TIMER)) {
            removeStatusBar();
        }

    }

    @Override
    public void onSoundVolumeUpdate(int amplitude) {
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        if (currentFragTag.equals(TTApp.FragmentTags.BANG)) {
            Fragment fragment = getFragmentManager().findFragmentByTag(
                    currentFragTag);
            if (fragment != null) {
                SoundSensorFragment soundFrag = (SoundSensorFragment) fragment;
                soundFrag.onVolumeUpdate(amplitude);
            }
        }

    }

    @Override
    public void onSoundExceedThreshold(int amplitude) {
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        if (currentFragTag.equals(TTApp.FragmentTags.BANG)
                && mService.getOnGoingAction() == TTApp.OnGoingAction.BANG) {
            Fragment fragment = getFragmentManager().findFragmentByTag(
                    currentFragTag);
            if (fragment != null) {
                SoundSensorFragment soundFrag = (SoundSensorFragment) fragment;
                soundFrag.onExceedThreshold(amplitude);

                mShotsTakenCount++;
            }
        }

    }

    @Override
    public void onDistanceUpdated(float distanceTraveled, float speed) {
        Log.d(TAG, "onDistanceUpdated: " + distanceTraveled);
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        if (currentFragTag.equals(TTApp.FragmentTags.DISTANCE_LAPSE)
                && mService.getOnGoingAction() == TTApp.OnGoingAction.DISTANCE_LAPSE) {
            Fragment fragment = getFragmentManager().findFragmentByTag(
                    currentFragTag);
            if (fragment != null) {
                DistanceLapseFragment disrFrag = (DistanceLapseFragment) fragment;
                disrFrag.onDistanceLapseUpdate(distanceTraveled, speed);
            }
        }

    }

    public void ignoreGPS(boolean ignore) {
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        if (currentFragTag.equals(TTApp.FragmentTags.DISTANCE_LAPSE)) {
            Fragment fragment = getFragmentManager().findFragmentByTag(
                    currentFragTag);
            if (fragment != null) {
                DistanceLapseFragment disrFrag = (DistanceLapseFragment) fragment;
                disrFrag.ignoreGPS(ignore);
            }
        }
    }

    @Override
    public void onPulseStart(int exposures, int totalExposures,
                             long timeToNextExposure, long timeRemaining) {

        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        Fragment fragment = getFragmentManager().findFragmentByTag(
                currentFragTag);
        if(fragment == null) {
            return;
        }
        switch (mService.getOnGoingAction()) {
            case TTApp.OnGoingAction.TIMELAPSE:
                // Is the timelapse fragment currently active?
                if (currentFragTag.equals(TTApp.FragmentTags.TIMELAPSE)) {
                    TimeLapseFragment timeLapseFrag = (TimeLapseFragment) fragment;
                    timeLapseFrag.onPulseStarted(exposures, timeToNextExposure);
                }
                break;
            case TTApp.OnGoingAction.TIMEWARP:
                // Is the timelapse fragment currently active?
                if (currentFragTag.equals(TTApp.FragmentTags.TIMEWARP)) {
                    TimeWarpFragment timewarpFrag = (TimeWarpFragment) fragment;
                    timewarpFrag.onPulseStarted(exposures, totalExposures,
                            (int) timeToNextExposure, timeRemaining);
                }
                break;
            case TTApp.OnGoingAction.HDR:
                if (currentFragTag.equals(TTApp.FragmentTags.HDR)) {
                    HdrFragment hdrFrag = (HdrFragment) fragment;
                    hdrFrag.onPulseStarted(exposures, totalExposures,
                            (int) timeToNextExposure, timeRemaining);
                }
                break;
            case TTApp.OnGoingAction.HDR_TIMELAPSE:
                if (currentFragTag.equals(TTApp.FragmentTags.HDR_LAPSE)) {
                    HdrTimeLapseFragment hdrFrag = (HdrTimeLapseFragment) fragment;
                    hdrFrag.onPulseStarted(exposures, totalExposures,
                            (int) timeToNextExposure, timeRemaining);
                }
                break;
            case TTApp.OnGoingAction.STAR_TRAIL:
                if (currentFragTag.equals(TTApp.FragmentTags.STARTRAIL)) {
                    StarTrailFragment startFrag = (StarTrailFragment) fragment;
                    startFrag.onPulseStarted(exposures, totalExposures,
                            (int) timeToNextExposure, timeRemaining);
                }
                break;
            case TTApp.OnGoingAction.BRAMPING:
                if (currentFragTag.equals(TTApp.FragmentTags.BRAMPING)) {
                    BrampingFragment brampFrag = (BrampingFragment) fragment;
                    brampFrag.onPulseStarted(exposures, totalExposures,
                            (int) timeToNextExposure, timeRemaining);
                }

            default:
                // Do nothing we can't identify current fragment with ongoing
                // action.

        }

    }

    @Override
    public void onPulseUpdate(long[] sequence, int exposures, long timeToNext,
                              long remainingPulseTime, long remainingSequenceTime) {

        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        Fragment fragment = getFragmentManager().findFragmentByTag(currentFragTag);

        //If we don't have a Fragment yet we don't need to update anything.
        if(fragment == null) {
            return;
        }

        switch (mService.getOnGoingAction()) {
            case TTApp.OnGoingAction.TIMELAPSE:
                // Is the timelapse fragment currently active?
                if (currentFragTag.equals(TTApp.FragmentTags.TIMELAPSE)) {
                    TimeLapseFragment timeLapseFrag = (TimeLapseFragment) fragment;
                    timeLapseFrag.onPulseUpdate(exposures, timeToNext,
                            remainingPulseTime);
                }
                break;
            case TTApp.OnGoingAction.TIMEWARP:
                // Is the timelapse fragment currently active?
                if (currentFragTag.equals(TTApp.FragmentTags.TIMEWARP)) {
                    TimeWarpFragment timeWarpFrag = (TimeWarpFragment) fragment;
                    timeWarpFrag.onPulseUpdate(sequence, exposures, timeToNext,
                            remainingPulseTime, remainingSequenceTime);
                }
                break;
            case TTApp.OnGoingAction.HDR:
                if (currentFragTag.equals(TTApp.FragmentTags.HDR)) {
                    HdrFragment hdrFrag = (HdrFragment) fragment;
                    hdrFrag.onPulseUpdate(sequence, exposures, timeToNext,
                            remainingPulseTime, remainingSequenceTime);
                }
                break;
            case TTApp.OnGoingAction.HDR_TIMELAPSE:
                if (currentFragTag.equals(TTApp.FragmentTags.HDR_LAPSE)) {
                    HdrTimeLapseFragment hdrFrag = (HdrTimeLapseFragment) fragment;
                    hdrFrag.onPulseUpdate(sequence, exposures, timeToNext,
                            remainingPulseTime, remainingSequenceTime);
                }
                break;
            case TTApp.OnGoingAction.STAR_TRAIL:
                if (currentFragTag.equals(TTApp.FragmentTags.STARTRAIL)) {
                    StarTrailFragment startFrag = (StarTrailFragment) fragment;
                    startFrag.onPulseUpdate(sequence, exposures, timeToNext,
                            remainingPulseTime, remainingSequenceTime);
                }
                break;
            case TTApp.OnGoingAction.BRAMPING:
                if (currentFragTag.equals(TTApp.FragmentTags.BRAMPING)) {
                    BrampingFragment brampFrag = (BrampingFragment) fragment;
                    brampFrag.onPulseUpdate(sequence, exposures, timeToNext,
                            remainingPulseTime, remainingSequenceTime);
                }
                break;
            default:
                // Do nothing we can't identify current fragment with ongoing
                // action.
        }

    }

    public void onPulseFinished() {
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();

        switch (mService.getOnGoingAction()) {
            case TTApp.OnGoingAction.TIMELAPSE:
                Log.i(TAG, "&&TImelapse finish");
                break;
            case TTApp.OnGoingAction.TIMEWARP:
                if (currentFragTag.equals(TTApp.FragmentTags.TIMEWARP)) {
                    Fragment fragment = getFragmentManager().findFragmentByTag(
                            currentFragTag);
                    TimeWarpFragment timewarpFrag = (TimeWarpFragment) fragment;
                    if (timewarpFrag != null) {
                        timewarpFrag.onPulseStop();
                    }
                } else {
                    removeStatusBar();
                }
                break;
            case TTApp.OnGoingAction.STAR_TRAIL:
                if (currentFragTag.equals(TTApp.FragmentTags.STARTRAIL)) {
                    Fragment fragment = getFragmentManager().findFragmentByTag(
                            currentFragTag);
                    StarTrailFragment startFrag = (StarTrailFragment) fragment;
                    if (startFrag != null) {
                        startFrag.onPulseStop();
                    }
                } else {
                    removeStatusBar();
                }
                break;
            case TTApp.OnGoingAction.BRAMPING:
                if (currentFragTag.equals(TTApp.FragmentTags.BRAMPING)) {
                    Fragment fragment = getFragmentManager().findFragmentByTag(
                            currentFragTag);
                    BrampingFragment brampFrag = (BrampingFragment) fragment;
                    if (brampFrag != null) {
                        brampFrag.onPulseStop();
                    }
                } else {
                    removeStatusBar();
                }
                break;
            case TTApp.OnGoingAction.HDR:
                if (currentFragTag.equals(TTApp.FragmentTags.HDR)) {
                    Fragment fragment = getFragmentManager().findFragmentByTag(
                            currentFragTag);
                    HdrFragment startFrag = (HdrFragment) fragment;
                    if (startFrag != null) {
                        startFrag.onPulseStop();
                    }
                } else {
                    removeStatusBar();
                }
                break;
            case TTApp.OnGoingAction.HDR_TIMELAPSE:
                if (currentFragTag.equals(TTApp.FragmentTags.HDR_LAPSE)) {
                    Fragment fragment = getFragmentManager().findFragmentByTag(
                            currentFragTag);
                    HdrTimeLapseFragment startFrag = (HdrTimeLapseFragment) fragment;
                    if (startFrag != null) {
                        startFrag.onPulseStop();
                    }
                } else {
                    removeStatusBar();
                }
                break;
            default:

        }
    }

    @Override
    public void onPulseSequenceIterate(long[] sequence) {
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        switch (mService.getOnGoingAction()) {
            case TTApp.OnGoingAction.HDR_TIMELAPSE:
                if (currentFragTag.equals(TTApp.FragmentTags.HDR_LAPSE)) {
                    Fragment fragment = getFragmentManager().findFragmentByTag(
                            currentFragTag);
                    HdrTimeLapseFragment startFrag = (HdrTimeLapseFragment) fragment;
                    if (startFrag != null) {
                        startFrag.onPulseSequenceIterated(sequence);
                    }
                }
        }
    }

    @Override
    public void onWifiMasterAdded(final TTServiceInfo info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String currentFragTag = mDrawerFragHandler
                        .getCurrentFragmentTag();
                if (currentFragTag.equals(TTApp.FragmentTags.WIFI_SLAVE)) {
                    Fragment fragment = getFragmentManager().findFragmentByTag(
                            currentFragTag);
                    WifiSlaveFragment slaveFrag = (WifiSlaveFragment) fragment;
                    slaveFrag.addMaster(info);
                }

            }
        });

    }

    @Override
    public void onWifiMasterRemoved(final TTServiceInfo info) {
        // Log.d(TAG,"onWifiMasterRemoved");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String currentFragTag = mDrawerFragHandler
                        .getCurrentFragmentTag();
                if (currentFragTag.equals(TTApp.FragmentTags.WIFI_SLAVE)) {
                    Fragment fragment = getFragmentManager().findFragmentByTag(
                            currentFragTag);
                    WifiSlaveFragment slaveFrag = (WifiSlaveFragment) fragment;
                    slaveFrag.removeMaster(info);
                }

            }
        });

    }

    @Override
    public void onWifiMasterRegsitered(final TTServiceInfo info) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String currentFragTag = mDrawerFragHandler
                        .getCurrentFragmentTag();
                if (currentFragTag.equals(TTApp.FragmentTags.WIFI_MASTER)) {
                    Fragment fragment = getFragmentManager().findFragmentByTag(
                            currentFragTag);
                    WifiMasterFragment masterFrag = (WifiMasterFragment) fragment;
                    masterFrag.wifimasterRegistered(info);
                }
            }
        });

    }

    @Override
    public void onWifiMasterUnregister() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDrawerFragHandler != null) {
                    try {
                        String currentFragTag = mDrawerFragHandler
                                .getCurrentFragmentTag();
                        if (currentFragTag
                                .equals(TTApp.FragmentTags.WIFI_MASTER)) {
                            Fragment fragment = getFragmentManager()
                                    .findFragmentByTag(currentFragTag);
                            WifiMasterFragment masterFrag = (WifiMasterFragment) fragment;
                            masterFrag.wifiMasterUnregister();
                        }
                    } catch (NullPointerException exp) {

                    }
                }

            }
        });

    }

    @Override
    public void onClientConnected(final String name, final String uniqueName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String currentFragTag = mDrawerFragHandler
                        .getCurrentFragmentTag();
                if (currentFragTag.equals(TTApp.FragmentTags.WIFI_MASTER)) {
                    Fragment fragment = getFragmentManager().findFragmentByTag(
                            currentFragTag);
                    WifiMasterFragment masterFrag = (WifiMasterFragment) fragment;
                    masterFrag.onClientConnected(name, uniqueName);
                }

            }
        });

    }

    @Override
    public void onClientDisconnected(final String name, final String uniqueName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String currentFragTag = mDrawerFragHandler
                        .getCurrentFragmentTag();
                if (currentFragTag.equals(TTApp.FragmentTags.WIFI_MASTER)) {
                    Fragment fragment = getFragmentManager().findFragmentByTag(
                            currentFragTag);
                    WifiMasterFragment masterFrag = (WifiMasterFragment) fragment;
                    masterFrag.onClientDisconnected(name, uniqueName);
                }

            }
        });

    }

    @Override
    public void onPebbleTrigger() {
        Log.d(TAG, "onPebbleTrigger");
        String currentFragTag = mDrawerFragHandler.getCurrentFragmentTag();
        Log.d(TAG, currentFragTag);
        if (currentFragTag.equals(TTApp.FragmentTags.PEBBLE)) {
            Fragment fragment = getFragmentManager().findFragmentByTag(
                    currentFragTag);
            PebbleFragment pebbleFrag = (PebbleFragment) fragment;
            if (pebbleFrag != null) {
                pebbleFrag.onPebbleTrigger();

                AnalyticTracker tracker = AnalyticTracker.getInstance(this);
                tracker.addProperty(AnalyticTracker.Property.MODE, "Pebble");
                tracker.trackEvent(AnalyticTracker.Event.SEQUENCE_COMPLETED);
            }
        }

    }

    @Override
    public void onPebbleConnected() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onInputSelected(DialPadInput dialPadInput) {
        Log.d(TAG, "onInputSelected");
        mDialPadManager.setActiveInput(dialPadInput);
        // trying to show down/forward key instead of back in the nav bar not
        // working..
        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION);
    }

    @Override
    public void onInputDeSelected() {
        mDialPadManager.deactiveInput();

    }

    @Override
    public void inputSetSize(int height, int width) {
        mDialPadManager.setKeyboardDimensions(height, width);
    }

    @Override
    public void onDisconnectSlave() {
        Log.d(TAG, "onDisconnectSlave");
        mService.disconnectFromMaster();
    }

    public void onConnectSlave(String name, String ipAddress, int port) {
        mService.connectToMaster(name, ipAddress, port);
    }

    public boolean checkWifiMasterOn() {
        return mService.isWifiMasterOn();
    }

	/*
     * Listener for Wifi Master Fragment
	 */

    @Override
    public void onStartWifiMaster() {
        mService.registerWifiMaster();

    }

    @Override
    public void onStopWifiMaster() {
        mService.unRegsiterMaster();
    }

    @Override
    public void onDisconnectSlaveFromMaster(String uniqueName) {
        mService.disconnectSlaveFromMaster(uniqueName);
    }

    /*
     * Listener for Pebble Fragment
     */
    @Override
    public void onStartPebbleApp() {
        mService.startPebble();

    }

    @Override
    public void onStopPebbleApp() {
        mService.stopPebble();
    }

    /*
     * Service binding and handling code
     */
    private ServiceConnection mTriggertrapServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get
            // LocalService instance
            Log.d(TAG, "Service connected");
            TiggertrapServiceBinder binder = (TiggertrapServiceBinder) service;
            mService = binder.getService();
            Log.d(TAG, "Service connected: " + mService.toString());
            mTriggertrapServiceBound = true;
            // Make sure the service is stopped just in case we started it in
            // foreground.
            // when we left the Main Activity
            Intent intent = new Intent(MainActivity.this,
                    TriggertrapService.class);
            mService.goTobackground();
            stopService(intent);
            mService.setListener(MainActivity.this);
            // Check if we need to display the status bar
            displaySatusBar();
            // Check we if need to set the transient state
            setFragmentTransientState();

            // Make sure the WifMaster is turned on if that was it last state.
            boolean isWifMasterOn = TTApp.getInstance(getApplicationContext())
                    .isMasterOn();
            if (isWifMasterOn) {
                mService.registerWifiMaster();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onService Disconnected ");
            mTriggertrapServiceBound = false;
        }
    };
}
