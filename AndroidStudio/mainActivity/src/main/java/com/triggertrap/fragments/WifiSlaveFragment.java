package com.triggertrap.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.triggertrap.R;
import com.triggertrap.TTApp;
import com.triggertrap.fragments.dialog.MasterRunningDialog;
import com.triggertrap.widget.OngoingButton;
import com.triggertrap.wifi.TTServiceInfo;

public class WifiSlaveFragment extends TriggertrapFragment {

    private static final String TAG = WifiSlaveListener.class.getSimpleName();
    public static final int DIALOG_FRAGMENT = 1;
    private LinearLayout mMasterListLayout;
    private WifiSlaveListener mlistener;
    private OnClickListener mCheckBoxListener;
    private OngoingButton mButton;
    private View mRootView;
    private LinearLayout mSlaveInfoView;

    private Animation mSlideInFromTop;
    private Animation mSlideOutToTop;

    public interface WifiSlaveListener {
        public boolean checkInProgressState();

        public boolean checkWifiMasterOn();

        public void onWatchMaster();

        public void onUnwatchMaster();

        public void onConnectSlave(String name, String ipAddress, int port);

        public void onDisconnectSlave();

        public void onStopWifiMaster();
    }

    public WifiSlaveFragment() {
        mRunningAction = TTApp.OnGoingAction.WI_FI_SLAVE;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mlistener = (WifiSlaveListener) activity;
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");
        mRootView = inflater.inflate(R.layout.wifi_slave, container, false);
        mMasterListLayout = (LinearLayout) mRootView
                .findViewById(R.id.wifi_slave_list);

        mSlaveInfoView = (LinearLayout) mRootView
                .findViewById(R.id.slaceInfoView);

        Bundle fragmentState = getArguments();
        if (fragmentState != null) {
            String tag = fragmentState
                    .getString(TriggertrapFragment.BundleKey.FRAGMENT_TAG);
            // Is this bundle for this Fragment?
            if (tag.equals(getTag())) {
                boolean isActive = fragmentState.getBoolean(
                        TriggertrapFragment.BundleKey.IS_ACTION_ACTIVE, false);
                if (isActive) {
                    mState = State.STARTED;
                } else {
                    mState = State.STOPPED;
                }

                MasterList masterList = (MasterList) fragmentState
                        .getParcelable(TriggertrapFragment.BundleKey.WIFI_SLAVE_INFO);
                ArrayList<MasterInfo> masters = masterList.getMasterlist();
                for (MasterInfo master : masters) {
                    Log.d(TAG, "Matser with name: " + master.name);
                    createMasterView(master.name, master.ipAddress,
                            master.port, master.isChecked);
                }

            }

        } else {
            // Restore state of wifi slave from persistent storage e.g..
            // mInterval =
            // TTApp.getInstance(getActivity()).getTimeLapseInterval();
        }

        setUpAnimations();
        setUpButton();
        resetVolumeWarning();
        return mRootView;
    }

    private void setUpButton() {
        mButton = (OngoingButton) mRootView.findViewById(R.id.slaveButton);
        mButton.setToggleListener(new OngoingButton.OnToggleListener() {

            @Override
            public void onToggleOn() {
                Log.d(TAG, "onToggleON");
                startWifiSlave();
                checkVolume();
            }

            @Override
            public void onToggleOff() {
                Log.d(TAG, "onToggleOff");
                stopWifiSlave();

            }
        });
    }

    private void setUpAnimations() {
        mSlideInFromTop = AnimationUtils.loadAnimation(getActivity(),
                R.anim.slide_in_from_top);
        mSlideOutToTop = AnimationUtils.loadAnimation(getActivity(),
                R.anim.slide_out_to_top);

    }

    private void onClickCheckBox(View checkBoxView) {
        // CheckBox clickedCheckBox = (CheckBox) checkBoxView;
        LinearLayout checkBoxParent = (LinearLayout) checkBoxView;
        CheckBox clickedCheckBox = (CheckBox) checkBoxParent
                .findViewById(R.id.wifi_slave_check_box);

        MasterInfo clickedcheckBoxInfo = (MasterInfo) checkBoxParent.getTag();
        int childCount = mMasterListLayout.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View itemLayout = mMasterListLayout.getChildAt(i);
            Object tag = itemLayout.getTag();
            if (tag != null && tag instanceof MasterInfo) {
                MasterInfo checkboxInfo = (MasterInfo) tag;
                if (checkboxInfo.name.equals(clickedcheckBoxInfo.name)) {
                    if (!clickedcheckBoxInfo.isChecked) {
                        clickedcheckBoxInfo.isChecked = true;
                        clickedCheckBox.setChecked(true);
                        Log.d(TAG, "Connecting to Master " + checkboxInfo.name);
                        TTApp.getInstance(getActivity()).setSlaveLastMaster(checkboxInfo.name);
                        if (mlistener != null) {
                            mlistener.onConnectSlave(checkboxInfo.name, checkboxInfo.ipAddress,
                                    checkboxInfo.port);
                        }
                    } else {
                        clickedcheckBoxInfo.isChecked = false;
                        clickedCheckBox.setChecked(false);
                        Log.d(TAG, "Disconnecting from master... "
                                + checkboxInfo.name);
                        if (mlistener != null) {
                            mlistener.onDisconnectSlave();
                        }
                        TTApp.getInstance(getActivity()).setSlaveLastMaster("");
                    }

                } else {
                    if (checkboxInfo.isChecked) {
                        Log.d(TAG, "Disconnecting from master... "
                                + checkboxInfo.name);
                        checkboxInfo.isChecked = false;
                        CheckBox checkBox = (CheckBox) itemLayout
                                .findViewById(R.id.wifi_slave_check_box);
                        checkBox.setChecked(false);
                    }
                }
            }

        }
    }

    private void startWifiSlave() {
        if (mlistener.checkInProgressState()) {
            return;
        }

        if (mlistener.checkWifiMasterOn()) {
            mButton.stopAnimation();
            FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
            Fragment prev = getActivity().getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            MasterRunningDialog dialogFrag = new MasterRunningDialog();
            dialogFrag.setTargetFragment(this, DIALOG_FRAGMENT);
            dialogFrag.show(getActivity());
            ft.commit();
            return;
        }

        mlistener.onWatchMaster();
        mSlaveInfoView.startAnimation(mSlideOutToTop);
        mSlaveInfoView.setVisibility(View.GONE);
    }

    private void stopWifiSlave() {
        mlistener.onUnwatchMaster();
        mSlaveInfoView.setVisibility(View.VISIBLE);
        mSlaveInfoView.startAnimation(mSlideInFromTop);
        clearMasterList();
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
            mSlaveInfoView.setVisibility(View.GONE);
            mButton.startAnimation();
        } else {
            if (mSlaveInfoView != null) {
                mSlaveInfoView.setVisibility(View.VISIBLE);
                mButton.stopAnimation();
            }
        }
    }

    public void addAvaiableMasters(ArrayList<TTServiceInfo> masters) {
        for (TTServiceInfo masterInfo : masters) {
            addMaster(masterInfo);
        }
    }

    @Override
    public Bundle getStateBundle() {
        super.getStateBundle();
        if (mState == State.STARTED) {
            mStateBundle.putBoolean(
                    TriggertrapFragment.BundleKey.IS_ACTION_ACTIVE, true);
        } else {
            mStateBundle.putBoolean(
                    TriggertrapFragment.BundleKey.IS_ACTION_ACTIVE, false);
        }

        // Create ArrayList of MasterInfo and put in Parcelable
        ArrayList<MasterInfo> masters = new ArrayList<MasterInfo>();
        int childCount = mMasterListLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = mMasterListLayout.getChildAt(i);
            Object tag = child.getTag();
            if (tag != null && tag instanceof MasterInfo) {
                masters.add((MasterInfo) tag);
            }

        }
        MasterList parcableMasterList = new MasterList(masters);
        mStateBundle.putParcelable(
                TriggertrapFragment.BundleKey.WIFI_SLAVE_INFO,
                parcableMasterList);
        return mStateBundle;
    }

    public void addMaster(final TTServiceInfo info) {
        Log.d(TAG, "Attaching layout");
        createMasterView(info.getName(), info.getIpAddress(), info.getPort(), false);
    }

    private void createMasterView(String name, String ipAddress, int port,
                                  boolean isChecked) {
        // Check is this master already exists
        if (!isMasterUnqiue(name)) {
            Log.d(TAG, "Master has already been resolved");
            return;
        }

        // Don't attach the view straight away we want to set the tag first.
        View masterItem = getActivity().getLayoutInflater().inflate(
                R.layout.wifi_slave_item, mMasterListLayout, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        float vertMargin = getActivity().getResources().getDimension(R.dimen.activity_vertical_margin);
        float horizontalMargin = getActivity().getResources().getDimension(R.dimen.activity_horizontal_margin);
        layoutParams.setMargins((int) vertMargin, 0, (int) vertMargin, (int) horizontalMargin);
        masterItem.setLayoutParams(layoutParams);

        ImageView coloredBackground = (ImageView) masterItem.findViewById(R.id.drawlist_item_image);

        int childCount = mMasterListLayout.getChildCount();
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

        MasterInfo masterInfo = new MasterInfo();
        masterInfo.name = name;
        masterInfo.ipAddress = ipAddress;
        masterInfo.port = port;

        masterInfo.isChecked = isChecked;
        masterItem.setTag(masterInfo);

        TextView masterName = (TextView) masterItem
                .findViewById(R.id.wifi_slave_name);
        masterName.setText(name);
        CheckBox checkBox = (CheckBox) masterItem
                .findViewById(R.id.wifi_slave_check_box);
        checkBox.setClickable(false);


        masterItem.setOnClickListener(mCheckBoxListener);
        if (isChecked) {
            checkBox.setChecked(true);
        }

        mMasterListLayout.addView(masterItem, 0);

        View noDevicesView = mMasterListLayout.findViewWithTag(getResources()
                .getString(R.string.no_devices_found));
        if (noDevicesView != null) {
            mMasterListLayout.removeView(noDevicesView);
            coloredBackground.setColorFilter(0x00000000);
        }

        if (checkForAutoConnect(name)) {
            Log.d(TAG, "Master recognised.. Auto connecting");
            masterInfo.isChecked = true;
            checkBox.setChecked(true);
            Log.d(TAG, "Connecting to Master " + masterInfo.name);
//			Let the service Handle auto connect
//			if (mlistener != null) {
//				mlistener.onConnectSlave(masterInfo.name, masterInfo.ipAddress,
//						masterInfo.port);
//			}


        }

    }

    private boolean checkForAutoConnect(String discoveredMasterName) {
        boolean shouldAutoConnected = false;
        String lastMasterName = TTApp.getInstance(getActivity()).getSlaveLastMaster();
        Log.d(TAG, "Last Connected Master: " + lastMasterName);
        if (lastMasterName.equals(discoveredMasterName) == true) {
            shouldAutoConnected = true;
        }
        return shouldAutoConnected;
    }

    private boolean isMasterUnqiue(String name) {
        boolean isUnique = true;
        int childCount = mMasterListLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = mMasterListLayout.getChildAt(i);
            Object tag = child.getTag();
            if (tag != null && tag instanceof MasterInfo) {
                MasterInfo masterInfo = (MasterInfo) tag;
                if (masterInfo.name.equals(name)) {
                    isUnique = false;
                    break;
                }
            }
        }
        return isUnique;
    }

    public void removeMaster(final TTServiceInfo info) {
        Log.d(TAG, "Detaching layout");

        int childCount = mMasterListLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = mMasterListLayout.getChildAt(i);
            Object tag = child.getTag();
            if (tag != null && tag instanceof MasterInfo) {
                MasterInfo masterInfo = (MasterInfo) tag;
                if (masterInfo.name.equals(info.getName())) {
                    mMasterListLayout.removeViewAt(i);
                    break;
                }
            }
        }

        if (mMasterListLayout.getChildCount() < 1) {
            getActivity().getLayoutInflater().inflate(
                    R.layout.wifi_slave_item_empty, mMasterListLayout, true);
        }

    }

    private void clearMasterList() {
        mMasterListLayout.removeAllViews();
        getActivity().getLayoutInflater().inflate(
                R.layout.wifi_slave_item_empty, mMasterListLayout, true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case DIALOG_FRAGMENT:

                if (resultCode == Activity.RESULT_OK) {
                    mlistener.onStopWifiMaster();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    //If we hit cancel do nothing
                }
                break;
        }
    }

    /**
     * Classes for saving the list of Wifi slaves on rotation.
     */
    private static class MasterList implements Parcelable {
        private ArrayList<MasterInfo> masterlist;

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedList(masterlist);
        }

        public MasterList(Parcel in) {
            in.readTypedList(masterlist, MasterInfo.CREATOR);

        }

        public MasterList(ArrayList<MasterInfo> list) {
            this.masterlist = list;
        }

        public static final Creator<MasterList> CREATOR = new Creator<MasterList>() {
            public MasterList createFromParcel(Parcel s) {
                return new MasterList(s);
            }

            public MasterList[] newArray(int size) {
                return new MasterList[size];
            }
        };

        public int describeContents() {
            // TODO Auto-generated method stub
            return 0;
        }

        public ArrayList<MasterInfo> getMasterlist() {
            return masterlist;
        }

        public void setMasterlist(ArrayList<MasterInfo> masterlist) {
            this.masterlist = masterlist;
        }

    }


    private static class MasterInfo implements Parcelable {

        String name;
        String ipAddress;
        int port;

        boolean isChecked = false;

        public MasterInfo() {
        }

        public MasterInfo(Parcel parcel) {
            name = parcel.readString();
            ipAddress = parcel.readString();
            port = parcel.readInt();
        }

        @Override
        public int describeContents() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(ipAddress);
            dest.writeInt(port);

        }

        public static Creator<MasterInfo> CREATOR = new Creator<MasterInfo>() {

            public MasterInfo createFromParcel(Parcel s) {
                return new MasterInfo(s);
            }

            public MasterInfo[] newArray(int size) {
                return new MasterInfo[size];
            }
        };

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
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
