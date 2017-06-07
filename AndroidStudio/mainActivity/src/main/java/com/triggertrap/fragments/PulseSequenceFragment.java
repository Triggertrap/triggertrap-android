package com.triggertrap.fragments;

import android.app.Activity;

import com.triggertrap.util.PulseGenerator;

public class PulseSequenceFragment extends TriggertrapFragment {

    protected PulseSequenceListener mPulseSeqListener;
    protected PulseGenerator mPulseGenerator;
    protected long[] mPulseSequence;


    public interface PulseSequenceListener {
        public void onPulseSequenceCreated(int ongoingAction, long[] sequence, boolean repeat);

        public void onPulseSequenceCancelled();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mPulseGenerator = new PulseGenerator(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mPulseSeqListener = (PulseSequenceListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PulseSequenceListener");
        }


    }

    ;

}
