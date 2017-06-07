package com.triggertrap.inputs;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;

import com.triggertrap.TTApp;

public class MicVolumeMonitor {

    private static final String TAG = MicVolumeMonitor.class.getSimpleName();

    private static final int VOLUME_UPDATE_INTERVAL = 10;
    private static final int TRIGGER_UPDATE_INTERVAL = 10;
    public static final int MAX_VOL_RANGE = Short.MAX_VALUE;
    public static final int MIN_VOL_RANGE = 500;
    public static final int DEFAULT_VOL_RANGE = 5000;

    public static final int SAMPLE_RATE = 16000;

    private AudioRecord mRecorder;
    private int mVolumeRange = DEFAULT_VOL_RANGE;
    private int mThreshold = (DEFAULT_VOL_RANGE / 2);
    private short[] mBuffer;
    private boolean mIsRecording = false;
    private VolumeListener mListener = null;
    private boolean mIsThresholdEnabled = false;
    private long lastUpdateTime = System.currentTimeMillis();
    private long lastTriggerTime = System.currentTimeMillis();
    private Handler handler = new Handler();

    public MicVolumeMonitor() {
        initRecorder();
    }

    public MicVolumeMonitor(VolumeListener listener) {
        initRecorder();
        mListener = listener;
    }

    public interface VolumeListener {
        public void onVolumeUpdate(int amplitude);

        public void onExceedThreshold(int amplitude);
    }

    private void initRecorder() {
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        if (bufferSize > 0) {
            mBuffer = new short[bufferSize];
//		Log.d(TAG, "Min IN Buffer size bytes  is: " + bufferSize);
//		Log.d(TAG, "Min IN Buffer size samples is: " + bufferSize / 2);
//		Log.d(TAG, "Min IN Buffer in secs: " + ((float) bufferSize / 2) / 44100);

            mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        }
    }

    private void startBufferedRead() {
        new Thread(new Runnable() {

            public void run() {
                while (mIsRecording) {
                    double sum = 0;
                    int readSize = mRecorder.read(mBuffer, 0, mBuffer.length);
                    for (int i = 0; i < readSize; i++) {
                        sum += mBuffer[i] * mBuffer[i];
                    }
                    if (readSize > 0) {
                        final double amplitude = sum / readSize;
                        int rmsAmplitude = (int) Math.sqrt(amplitude);
                        //Pass amplitude to listener
                        if (mListener != null) {
                            rmsAmplitude = (rmsAmplitude <= mVolumeRange) ? rmsAmplitude : mVolumeRange;
                            //Get the amplitude as a percentage in the range 0-100
                            final int percentAmplitude = (int) ((float) (rmsAmplitude) / mVolumeRange * 100);

                            final long currentTime = System.currentTimeMillis();

                            if (currentTime - lastUpdateTime > VOLUME_UPDATE_INTERVAL) {
                                lastUpdateTime = currentTime;
                                mListener.onVolumeUpdate(percentAmplitude);
                            }
                            if (rmsAmplitude > mThreshold && mIsThresholdEnabled == true) {
                                if (currentTime - lastTriggerTime > TTApp.getInstance(null).getSensorResetDelay()) {
                                    lastTriggerTime = currentTime;

                                    //Trigger the output after the sensor delay
                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            mListener.onExceedThreshold(percentAmplitude);
                                        }
                                    }, TTApp.getInstance(null).getSensorDelay());

                                }
                            }
                        }
                    } else {
                        //Pass 0 amplitude if we have no sample.
                        if (mListener != null) {
                            mListener.onVolumeUpdate(0);
                        }
                    }
                }
            }
        }).start();
    }

    public void start() {
        if (mIsRecording == false) {
            mIsRecording = true;
            mRecorder.startRecording();
            startBufferedRead();
        }
    }

    public void stop() {
        if (mIsRecording == true) {
            mIsRecording = false;
            mRecorder.stop();
        }
    }

    public void enabledThreshold() {
        mIsThresholdEnabled = true;
    }

    public void disableThreshold() {
        mIsThresholdEnabled = false;
    }

    public boolean getThresholdEnabled() {
        return mIsThresholdEnabled;
    }

    public void release() {
        mRecorder.release();
    }

    public void setMicSensitivity(float percentage) {
        //Calculate current threshold percentage
        float thresholdPercentage = ((float) mThreshold / mVolumeRange) * 100;

        percentage = (percentage < 0) ? 0 : percentage;
        percentage = (percentage > 100) ? 100 : percentage;
        final int volumeRange = MAX_VOL_RANGE - MIN_VOL_RANGE;
        final int percentageRange = (int) (volumeRange * percentage / 100);
        int upperVolumeRange = MAX_VOL_RANGE - percentageRange;
        mVolumeRange = upperVolumeRange;
        //Reset threshold
        setThreshold(thresholdPercentage);
    }

    public void setThreshold(float percentage) {
        percentage = (percentage < 0) ? 0 : percentage;
        percentage = (percentage > 100) ? 100 : percentage;
        mThreshold = (int) (mVolumeRange * percentage / 100);
        //Log.d(TAG, "Threshold pecentage: " +  percentage + " threshold value:" + mThreshold + " Volume range: " + mVolumeRange);
    }

}
