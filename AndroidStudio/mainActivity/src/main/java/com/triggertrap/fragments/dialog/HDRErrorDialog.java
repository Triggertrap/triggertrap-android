package com.triggertrap.fragments.dialog;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import com.triggertrap.R;

import eu.inmite.android.lib.dialogs.BaseDialogFragment;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * Dialog displayed when an error occurs relating to HDR.
 *
 * @author scottmellors
 * @since 2.1
 */
public class HDRErrorDialog extends SimpleDialogFragment {

    public void show(Activity activity) {
        new HDRErrorDialog().show(activity.getFragmentManager(), "hdr_error");
    }

    @Override
    public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
        builder.setView(LayoutInflater.from(getActivity()).inflate(
                R.layout.running_action_dialog, null));
        builder.setTitle(getResources().getString(R.string.hdr_error_title));
        builder.setMessage(getResources().getString(R.string.hdr_error));
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dismiss();
                    }
                });
        return builder;
    }

}
