package com.triggertrap.fragments.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.triggertrap.R;
import com.triggertrap.TTApp;

import eu.inmite.android.lib.dialogs.BaseDialogFragment;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * SimpleDialogExtension that shows a dialog asking if the user wants to
 * disconnect the slave device.
 *
 * @author scottmellors
 * @since 2.1
 */
public class DisconnectSlaveDialogFrag extends SimpleDialogFragment {

    public static DisconnectSlaveDialogFrag newInstance(int num) {

        DisconnectSlaveDialogFrag dialogFragment = new DisconnectSlaveDialogFrag();
        Bundle bundle = new Bundle();
        bundle.putInt("num", num);
        dialogFragment.setArguments(bundle);

        return dialogFragment;

    }

    public void show(Activity activity) {
        new DisconnectSlaveDialogFrag().show(activity.getFragmentManager(),
                "disconnect_slave");
    }

    @Override
    public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {

        builder.setIcon(R.drawable.ic_dialog_alert_holo_light);
        builder.setView(LayoutInflater.from(getActivity()).inflate(
                R.layout.running_action_dialog, null));
        builder.setTitle(getResources().getString(
                R.string.disconnect_slave_device));
        builder.setMessage(getResources().getString(
                R.string.do_you_want_to_slave_device));
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getFragmentManager().findFragmentByTag(TTApp.FragmentTags.WIFI_MASTER).onActivityResult(
                                getTargetRequestCode(), Activity.RESULT_OK,
                                getActivity().getIntent());
                        dismiss();
                    }
                });

        builder.setNegativeButton(R.string.cancel, new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                getFragmentManager().findFragmentByTag(TTApp.FragmentTags.WIFI_MASTER).onActivityResult(getTargetRequestCode(),
                        Activity.RESULT_CANCELED, getActivity().getIntent());
                dismiss();
            }
        });

        return builder;
    }
}
