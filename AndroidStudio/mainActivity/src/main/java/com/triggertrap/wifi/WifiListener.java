package com.triggertrap.wifi;

public interface WifiListener {

    public void onWatchService();

    public void onUnWatchService();

    public void onMasterRegisterMaster();

    public void onMasterUnregister();
}
