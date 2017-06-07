package com.triggertrap.util;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;

import com.triggertrap.R;

public class WarningMessageManager {

    public static final String TAG = WarningMessageManager.class.getSimpleName();
    private final int TIMEOUT = 3000;

    /*Actions and types */
    public static final String ACTION = "warning_message_manager_action";
    public static final String ACTION_TYPE = "action_type";

    public interface Action {
        public final int INVALID = -1;
        public final int SHOW = 0;
        public final int DISMISS = 1;
        public final int RESET = 2;
    }

    private interface State {
        public int READY_TO_SHOW = 0;
        public int SHOWING = 1;
        public int WAITING_FOR_RESET = 2;
    }

    AudioManager mAudioManager;

    private int mState = State.READY_TO_SHOW;
    private Context mContext;
    private View mMessageView;
    private Handler mHandler = new Handler();
    private int mLastVolume = 0;

    private AnimationSet mSlideInFromBottom;
    private Animation mSlideOutToBottom;

    private Runnable mCancelTask = new Runnable() {
        public void run() {
            mMessageView.startAnimation(mSlideOutToBottom);
            mMessageView.setVisibility(View.GONE);
            mState = State.WAITING_FOR_RESET;
        }
    };

    //Handler for received Events from Settings Fragment
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            int type = intent.getIntExtra(ACTION_TYPE, Action.INVALID);
            if (type == Action.SHOW) {
                int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int currentMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                //If the volume has change and is still less than max then reset state
                if (mLastVolume != currentVolume && mLastVolume < currentMaxVolume) {
                    resetState();
                }

                if (mState == State.READY_TO_SHOW && (currentVolume < currentMaxVolume)) {
                    mLastVolume = currentVolume;
                    show();
                    mState = State.SHOWING;
                }
            } else if (type == Action.DISMISS) {
                if (mState == State.SHOWING) {
                    dismiss();
                }

            } else if (type == Action.RESET) {
                if (mState == State.SHOWING) {
                    dismiss();
                }
                resetState();
            }

        }
    };

    public WarningMessageManager(Context ctx, View messageView) {
        mContext = ctx;
        mMessageView = messageView;

        //Set up click listener for dismiss
        View dismiss = mMessageView.findViewById(R.id.dismissWarning);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mState == State.SHOWING) {
                    dismiss();
                }
            }
        });

        //Tweak the animations
        mSlideInFromBottom = (AnimationSet) AnimationUtils.loadAnimation(mContext,
                R.anim.show_warning_message);
        mSlideInFromBottom.setInterpolator(new OvershootInterpolator(4.0f));
        mSlideOutToBottom = AnimationUtils.loadAnimation(mContext,
                R.anim.slide_out_to_bottom);
        mSlideOutToBottom.setDuration(300);

        //Get AudioManager
        mAudioManager = (AudioManager) mContext.getSystemService(Activity.AUDIO_SERVICE);

    }


    public void startListening() {
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(mContext).registerReceiver(
                mMessageReceiver,
                new IntentFilter(ACTION));
    }

    public void stopListening() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiver);
    }

    private void show() {
        mMessageView.setVisibility(View.VISIBLE);
        mMessageView.startAnimation(mSlideInFromBottom);
        mHandler.removeCallbacks(mCancelTask);
        mHandler.postDelayed(mCancelTask, TIMEOUT);
    }

    private void dismiss() {
        mHandler.removeCallbacks(mCancelTask);
        mMessageView.startAnimation(mSlideOutToBottom);
        mMessageView.setVisibility(View.GONE);
        mState = State.WAITING_FOR_RESET;
    }

    private void resetState() {
        mLastVolume = 0;
        mState = State.READY_TO_SHOW;
    }
}
