package com.triggertrap.fragments.dialog;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.triggertrap.R;
import com.triggertrap.activities.MainActivity;

import eu.inmite.android.lib.dialogs.BaseDialogFragment;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * Shows a dialog asking users to enable GPS functionality.
 *
 * @author scottmellors
 * @since 2.1
 */
public class EnableGPSDialog extends SimpleDialogFragment {

    public void show(Activity activity) {
        new EnableGPSDialog().show(activity.getFragmentManager(), "enable_gps");
    }

    @Override
    public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
        builder.setView(LayoutInflater.from(getActivity()).inflate(
                R.layout.running_action_dialog, null));
        builder.setTitle(getResources().getString(R.string.enable_gps));
        builder.setMessage(getResources().getString(R.string.gps_descripition));
        builder.setPositiveButton(getResources().getString(R.string.yes),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);

                        dismiss();
                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.no),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        MainActivity mainActivity = (MainActivity) getActivity();
                        mainActivity.ignoreGPS(true);

                        dismiss();
                    }
                });
        return builder;
    }
}
