package com.triggertrap.fragments.dialog;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import com.triggertrap.R;

import eu.inmite.android.lib.dialogs.BaseDialogFragment;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * Dialog which is displayed when the user encounters an error with the Google
 * Play Services.
 *
 * @author scottmellors
 * @since 2.1
 */
public class ErrorPlayServicesFragment extends SimpleDialogFragment {

    public void show(Activity activity) {
        new ErrorPlayServicesFragment().show(activity.getFragmentManager(),
                "error_play_services");
    }

    @Override
    public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
        builder.setView(LayoutInflater.from(getActivity()).inflate(
                R.layout.running_action_dialog, null));
        builder.setTitle(getResources().getString(
                R.string.google_play_error_title));
        builder.setMessage(getResources().getString(
                R.string.google_play_error_description));
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