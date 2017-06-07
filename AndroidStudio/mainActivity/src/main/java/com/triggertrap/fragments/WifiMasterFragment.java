package com.triggertrap.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.triggertrap.R;
import com.triggertrap.TTApp;
import com.triggertrap.fragments.dialog.DisconnectSlaveDialogFrag;
import com.triggertrap.wifi.TTServiceInfo;
import com.triggertrap.wifi.TTSlaveInfo;

public class WifiMasterFragment extends TriggertrapFragment {

    private static final String TAG = WifiMasterFragment.class.getSimpleName();
    public static final int DIALOG_FRAGMENT = 1;

    private LayoutInflater mInflater;
    private View mRootView;
    private Switch mButton;
    private LinearLayout mMasterInfoView;
    private LinearLayout mSlaveListLayout;
    private ProgressBar mMasterConnectProgress;
    private TextSwitcher mBroadcastingTextSwitch;
    private CheckBox mClickedCheckBox;
    private OnClickListener mCheckBoxListener;
    private SlaveInfo mClickedcheckBoxInfo;
    private boolean uiSwitchInteraction = false;

    private Animation mSlideInFromTop;
    private Animation mSlideOutToTop;

    private WifiMasterListener mlistener;

    public interface WifiMasterListener {
        public boolean checkInProgressState();

        public void onStartWifiMaster();

        public void onStopWifiMaster();

        public void onDisconnectSlaveFromMaster(String uniqueName);
    }

    public WifiMasterFragment() {
        mRunningAction = TTApp.OnGoingAction.WI_FI_MASTER;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mlistener = (WifiMasterListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement WifiSlaveListener");
        }
    }

    ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mCheckBoxListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCheckBox(v);

            }
        };

        //boolean isWifOn  = TTApp.getInstance(getActivity()).isMasterOn();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.wifi_master, container, false);
        mSlaveListLayout = (LinearLayout) mRootView
                .findViewById(R.id.wifi_slave_list);

        mMasterInfoView = (LinearLayout) mRootView
                .findViewById(R.id.slaceInfoView);
        mMasterConnectProgress = (ProgressBar) mRootView.findViewById(R.id.masterConnectProgress);

        mInflater = LayoutInflater.from(getActivity());


        setUpTextSwitcher();
        setUpAnimations();
        setUpButton();

        return mRootView;
    }


    private void setUpTextSwitcher() {

        final TextView broadcastingText = (TextView) mInflater.inflate(R.layout.wifi_master_broadcast_textview, null);

        final ColorStateList textColors = broadcastingText.getTextColors();
        final Typeface typeface = broadcastingText.getTypeface();
        final int gravity = broadcastingText.getGravity();
        final int paddingBottom = broadcastingText.getPaddingBottom();
        final float textSize = broadcastingText.getTextSize();

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        final int textPixelSize = (int) (textSize / dm.scaledDensity);

        mBroadcastingTextSwitch = (TextSwitcher) mRootView.findViewById(R.id.broadcastingTextSwitch);
        Animation inAnimation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        Animation outAnimation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
        mBroadcastingTextSwitch.setInAnimation(inAnimation);
        mBroadcastingTextSwitch.setOutAnimation(outAnimation);

        mBroadcastingTextSwitch.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {

                TextView t = new TextView(getActivity());
                t.setGravity(gravity);
                t.setTextSize(textPixelSize);
                t.setTextColor(textColors);
                t.setTypeface(typeface);
                t.setPadding(0, 0, 0, paddingBottom);

                return t;
            }
        });


    }

    private void setUpAnimations() {
        mSlideInFromTop = AnimationUtils.loadAnimation(getActivity(),
                R.anim.slide_in_from_top);
        mSlideOutToTop = AnimationUtils.loadAnimation(getActivity(),
                R.anim.slide_out_to_top);

    }

    private void setUpButton() {
        mButton = (Switch) mRootView.findViewById(R.id.masterOnOff);

//		mButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				boolean on = ((Switch) v).isChecked();
//			    
//			    if (on) {
//			    	startWifiMaster();
//			    } else {
//			    	stopWifiMaster();
//			    }
//				
//			}
//		});

        mButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                uiSwitchInteraction = true;
                return false;
            }
        });

        mButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, " uiswitchinteraction is: " + uiSwitchInteraction);
                    if (uiSwitchInteraction) {
                        startWifiMaster();
                    }

                } else {
                    if (uiSwitchInteraction) {
                        stopWifiMaster();
                    }
                }
            }
        });
    }


    private void onClickCheckBox(View checkBoxView) {


        LinearLayout checkBoxParent = (LinearLayout) checkBoxView;
        mClickedCheckBox = (CheckBox) checkBoxParent
                .findViewById(R.id.wifi_slave_check_box);

        mClickedcheckBoxInfo = (SlaveInfo) checkBoxParent.getTag();
        Log.d(TAG, "Click checkbox :" + mClickedcheckBoxInfo.uniqueName);

        int childCount = mSlaveListLayout.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View itemLayout = mSlaveListLayout.getChildAt(i);
            Object tag = itemLayout.getTag();
            if (tag != null && tag instanceof SlaveInfo) {
                SlaveInfo checkboxInfo = (SlaveInfo) tag;
                if (checkboxInfo.uniqueName.equals(mClickedcheckBoxInfo.uniqueName)) {
                    if (mClickedcheckBoxInfo.isChecked) {
                        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
                        Fragment prev = getActivity().getFragmentManager().findFragmentByTag("dialog");
                        if (prev != null) {
                            Log.i("REMOVING", "Removeing");
                            ft.remove(prev);
                        }
                        ft.addToBackStack(null);
                        showDialog();
                        ft.commit();
                    }
                }
            }

        }
    }

    public void wifimasterRegistered(TTServiceInfo serviceInfo) {
        String masterName = serviceInfo.getName();
        Log.d(TAG, "Registered master: " + masterName);
        mMasterConnectProgress.setVisibility(View.GONE);
        mBroadcastingTextSwitch.setText(getActivity().getResources().getString(R.string.my_name_is) + ": " + serviceInfo.getName());
        TTApp.getInstance(getActivity()).setMasterOn(true);

        if (!mButton.isChecked()) {
            uiSwitchInteraction = false;
            mButton.setChecked(true);
            mMasterInfoView.startAnimation(mSlideOutToTop);
            mMasterInfoView.setVisibility(View.GONE);
        }
    }

    public void wifiMasterUnregister() {
        mMasterConnectProgress.setVisibility(View.GONE);
        mBroadcastingTextSwitch.setText("");
        TTApp.getInstance(getActivity()).setMasterOn(false);
    }

    private void startWifiMaster() {
        Log.d(TAG, "Starting Wifi Master");
        if (mlistener.checkInProgressState()) {
            uiSwitchInteraction = false;
            mButton.setChecked(false);
            return;
        }
        mlistener.onStartWifiMaster();
        mMasterInfoView.startAnimation(mSlideOutToTop);
        mMasterInfoView.setVisibility(View.GONE);
        mMasterConnectProgress.setVisibility(View.VISIBLE);

    }

    public void stopWifiMaster() {
        Log.d(TAG, "Stoping Wifi Master");
        mlistener.onStopWifiMaster();
        mBroadcastingTextSwitch.setText("");
        mMasterInfoView.setVisibility(View.VISIBLE);
        mMasterInfoView.startAnimation(mSlideInFromTop);
        clearSlaveList();

    }

    public void onClientConnected(String name, String uniqueName) {
        Log.d(TAG, "Attaching layout");
        createSlaveView(name, uniqueName, true);
    }

    public void onClientDisconnected(String name, String uniqueName) {
        Log.d(TAG, "Detaching layout");

        int childCount = mSlaveListLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = mSlaveListLayout.getChildAt(i);
            Object tag = child.getTag();
            if (tag != null && tag instanceof SlaveInfo) {
                SlaveInfo slaveInfo = (SlaveInfo) tag;
                if (slaveInfo.uniqueName.equals(uniqueName)) {
                    mSlaveListLayout.removeViewAt(i);
                    break;
                }
            }
        }

        if (mSlaveListLayout.getChildCount() < 1) {
            getActivity().getLayoutInflater().inflate(
                    R.layout.wifi_slave_item_empty, mSlaveListLayout, true);
        }

    }

    public void addConnectedSlaves(ArrayList<TTSlaveInfo> slaves) {

        clearSlaveList();

        for (TTSlaveInfo slaveInfo : slaves) {
            createSlaveView(slaveInfo.getName(), slaveInfo.getUniqueName(), true);
        }
    }

    @Override
    public void setActionState(boolean actionState) {
        if (actionState == true) {
            mState = State.STARTED;
        } else {
            mState = State.STOPPED;
        }
        setInitialUiState();
    }

    private void setInitialUiState() {
        if (mState == State.STARTED) {
            mMasterInfoView.setVisibility(View.GONE);
            uiSwitchInteraction = false;
            mButton.setChecked(true);
        } else {
            if (mMasterInfoView != null) {
                mMasterInfoView.setVisibility(View.VISIBLE);
                uiSwitchInteraction = false;
                mButton.setChecked(false);
            }
        }
    }

    private void createSlaveView(String name, String uniqueName,
                                 boolean isChecked) {


        // Don't attach the view straight away we want to set the tag first.
        View slaveItem = getActivity().getLayoutInflater().inflate(
                R.layout.wifi_slave_item, mSlaveListLayout, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        float vertMargin = getActivity().getResources().getDimension(R.dimen.activity_vertical_margin);
        float horizontalMargin = getActivity().getResources().getDimension(R.dimen.activity_horizontal_margin);
        layoutParams.setMargins((int) vertMargin, 0, (int) vertMargin, (int) horizontalMargin);
        slaveItem.setLayoutParams(layoutParams);

        ImageView coloredBackground = (ImageView) slaveItem.findViewById(R.id.drawlist_item_image);

        int childCount = mSlaveListLayout.getChildCount();
        int colorPosition = 0;
        if (childCount < 4) {
            colorPosition = childCount;
        } else {
            colorPosition = childCount % 4;
        }

        switch (colorPosition) {
            case 1:
                coloredBackground.setColorFilter(0x19000000);
                break;
            case 2:
                coloredBackground.setColorFilter(0x33000000);
                break;
            case 3:
                coloredBackground.setColorFilter(0x4c000000);
                break;

        }

        SlaveInfo slaveInfo = new SlaveInfo();
        slaveInfo.name = name;
        slaveInfo.uniqueName = uniqueName;


        slaveInfo.isChecked = isChecked;
        slaveItem.setTag(slaveInfo);

        TextView masterName = (TextView) slaveItem
                .findViewById(R.id.wifi_slave_name);
        masterName.setText(name);
        CheckBox checkBox = (CheckBox) slaveItem
                .findViewById(R.id.wifi_slave_check_box);
        checkBox.setClickable(false);


        slaveItem.setOnClickListener(mCheckBoxListener);
        if (isChecked) {
            checkBox.setChecked(true);
        }

        mSlaveListLayout.addView(slaveItem, 0);

        View noDevicesView = mSlaveListLayout.findViewWithTag(getResources()
                .getString(R.string.no_devices_found));
        if (noDevicesView != null) {
            mSlaveListLayout.removeView(noDevicesView);
            coloredBackground.setColorFilter(0x00000000);
        }
    }


    public void removeSlaveView(String uniqueName) {
        Log.d(TAG, "Detaching layout");

        int childCount = mSlaveListLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = mSlaveListLayout.getChildAt(i);
            Object tag = child.getTag();
            if (tag != null && tag instanceof SlaveInfo) {
                SlaveInfo slaveInfo = (SlaveInfo) tag;
                if (slaveInfo.uniqueName.equals(uniqueName)) {
                    mSlaveListLayout.removeViewAt(i);
                    break;
                }
            }
        }

        if (mSlaveListLayout.getChildCount() < 1) {
            getActivity().getLayoutInflater().inflate(
                    R.layout.wifi_slave_item_empty, mSlaveListLayout, true);
        }

    }

    private void clearSlaveList() {
        mSlaveListLayout.removeAllViews();
        getActivity().getLayoutInflater().inflate(
                R.layout.wifi_slave_item_empty, mSlaveListLayout, true);
    }

    private void showDialog() {
        DisconnectSlaveDialogFrag dialogFrag = DisconnectSlaveDialogFrag.newInstance(123);
        dialogFrag.setTargetFragment(this, DIALOG_FRAGMENT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "dialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case DIALOG_FRAGMENT:
                if (resultCode == Activity.RESULT_OK) {
                    mClickedCheckBox.setChecked(false);
                    Log.d(TAG, "Disconnecting from master... "
                            + mClickedcheckBoxInfo.name);
                    if (mlistener != null) {
                        mlistener.onDisconnectSlaveFromMaster(mClickedcheckBoxInfo.uniqueName);
                    }
                    removeSlaveView(mClickedcheckBoxInfo.uniqueName);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    //If we hit cancel do nothing
                }
                break;
        }
    }

    private static class SlaveInfo implements Parcelable {

        String name;
        String uniqueName;


        boolean isChecked = true;

        public SlaveInfo() {
        }

        public SlaveInfo(Parcel parcel) {
            name = parcel.readString();
            uniqueName = parcel.readString();

        }

        @Override
        public int describeContents() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(uniqueName);

        }

        public static Creator<SlaveInfo> CREATOR = new Creator<SlaveInfo>() {

            public SlaveInfo createFromParcel(Parcel s) {
                return new SlaveInfo(s);
            }

            public SlaveInfo[] newArray(int size) {
                return new SlaveInfo[size];
            }
        };

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUniqueName() {
            return uniqueName;
        }

        public void setUniqueName(String uniqueName) {
            this.uniqueName = uniqueName;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public boolean getChecked() {
            return isChecked;
        }

        public void setChecked(boolean isChecked) {
            this.isChecked = isChecked;
        }

    }


}
