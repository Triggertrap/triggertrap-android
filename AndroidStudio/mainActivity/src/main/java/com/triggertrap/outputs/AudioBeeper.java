/**
 *
 */
package com.triggertrap.outputs;


import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * @author matt
 */
public class AudioBeeper implements IBeeper {

    private static final String TAG = AudioBeeper.class.getSimpleName();
    public static final long FOREVER = Long.MAX_VALUE;

    private static final double WAVE_FREQUENCY = 17000;


    private AudioTrack track;
    private short[] buffer;
    private short[] samples;
    private long limit = 0;
    private long pause = FOREVER;
    private boolean writingSamples = true;
    private boolean pausingAudioWrite = true;

//	   private SoundPool spool;
//	   private int soundID;
//	   float volume;

    private AudioBeeperListener mListener;

    public interface AudioBeeperListener {
        public void onAudioPlayStart();

        public void onAudioPlayStop();

        public void onAudioPlayPauseDone();
    }

    public AudioBeeper() {
        this(null, null);
    }

    public AudioBeeper(AudioBeeperListener listener, Context context) {


//		   spool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
//		   soundID = spool.load(context, R.raw.tone, 1);
//		   AudioManager audioManager = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
//	       volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//		  
        mListener = listener;
        //Create the samples to play the sound wave;
        double increment = (2 * Math.PI) * WAVE_FREQUENCY / 44100; // angular increment for each sample
        double angle = 0;
        int sampleCount = 4410;

        samples = new short[sampleCount];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = (short) (Math.sin(angle) * Short.MAX_VALUE);
            angle += increment;
            if (angle > (2 * Math.PI)) {
                angle -= 2 * Math.PI;
            }
        }
        buffer = new short[samples.length * 2];

        int minSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);

        //minSize = minSize/8;
        //minSize =  1764;
//	       Log.d(TAG,"Min Buffer size bytes  is: " + minSize);
//	       Log.d(TAG,"Min Buffer size samples is: " + minSize/4);
//	       Log.d(TAG,"Min Buffer in secs: " + ((float)minSize/4)/44100);

        track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                minSize, AudioTrack.MODE_STREAM);


        new Thread(new Runnable() {
            public void run() {
                do {
                    if (!pausingAudioWrite) {
                        writeSamples(samples);
                    } else {
                        //Whole thread handling needs to be refactored
                        // This small sleep does for now, stops this thread
                        // from hogging resources while it's not doing much.
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } while (writingSamples);
            }
        }).start();

        track.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
        track.play();

    }

    public void play(long length) {

        play(length, FOREVER);
    }

    public void play(long length, long pauseLength) {
//		   spool.play(soundID, volume, volume, 1, 0, 1f);

        long cycles = length * 441 / 5;
        long pauseCycles = pauseLength * 441 / 5;

        if (pauseCycles == 0) {
            pauseCycles = FOREVER;
        }
        if (cycles == 0) {
            cycles = 1;
        }
//		   Log.i("Beeper", "Channel: " + channel + " cycles: " + cycles + " pause: " + pauseCycles);

        limit = cycles;
        pause = pauseCycles;
        pausingAudioWrite = false;
        if (mListener != null) {
            mListener.onAudioPlayStart();
        }
    }

    public long timeRemaining(int channel) {
        if (limit == FOREVER) {
            return -1;
        }
        return limit / 441 * 5;
    }

    public long pauseRemaining(int channel) {
        if (pause == FOREVER) {
            return -1;
        }
        return pause / 441 * 5;
    }


    //TODO Not a hundred percent sure about this stop
    //Need to check that this is the best way.
    public void stop() {
        pausingAudioWrite = true;
        limit = 1;
        pause = FOREVER;
    }

    public void close() {
        if (track != null) {
            writingSamples = false;
            track.flush();
            track.stop();
            track.release();
        }
    }

    private void writeSamples(short[] samples) {

        fillBuffer(samples);
        track.write(buffer, 0, samples.length);
    }

    private void fillBuffer(short[] samples) {

		  /* 
           * Play back on Both channels left and right
		   */
        for (int i = 0; i < samples.length; i++) {
            if (limit > 1) {
                // Add delay of 100 microsecond to left(shutter) channel
                // to support Samsung NX cameras
                if (i < 44) {
                    buffer[i * 2] = 0;
                } else {
                    buffer[i * 2] = samples[i];
                }
                buffer[(i * 2) + 1] = (short) -(samples[i]);
                if (limit != FOREVER) {
                    limit--;
                }
            } else if (limit == 1) {
                buffer[i * 2] = samples[i];
                buffer[(i * 2) + 1] = samples[i];
                limit--;
                if (pause == FOREVER) {
                    pausingAudioWrite = true;
                }
                if (mListener != null) {
                    mListener.onAudioPlayStop();
                }
            } else {
                //Sshh, silent...
                buffer[i * 2] = 0;
                buffer[(i * 2) + 1] = 0;
                if (pause == 0) {
                    //Log.d("BEEP", "Pause done");
                    pause = FOREVER;
                    pausingAudioWrite = true;
                    if (mListener != null) {
                        mListener.onAudioPlayPauseDone();
                    }
                } else if (pause != FOREVER) {
                    pause--;
                }
            }
        }
    }


}