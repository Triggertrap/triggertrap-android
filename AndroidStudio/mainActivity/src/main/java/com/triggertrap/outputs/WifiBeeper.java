package com.triggertrap.outputs;

import android.content.Context;

import com.triggertrap.TTApp;
import com.triggertrap.wifi.MasterServer;

public class WifiBeeper implements IBeeper {

    private Context mAppContext;

    public WifiBeeper(Context context) {
        mAppContext = context.getApplicationContext();
    }

    public void play(long length) {
        //Check this is not a bulb mode trigger
        if (TTApp.getInstance(mAppContext).getBeepLength() == length) {
            MasterServer.getInstance().beep();
        }
    }

    public void play(long length, long pauseLength) {
        //Check this is not a bulb mode trigger
        if (TTApp.getInstance(mAppContext).getBeepLength() == length) {
            MasterServer.getInstance().beep();
        }
        //Don't need callback after pause, all that is handled in the Audio Beeper
    }

    public void stop() {
        // TODO Auto-generated method stub
    }

}
