package com.triggertrap.fragments.dialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;

import com.triggertrap.R;
import com.triggertrap.TTApp;

import eu.inmite.android.lib.dialogs.BaseDialogFragment;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * Secondary Dialog Fragment, using Styled Dialogs lib, pushing the user towards
 * the Play store, remind them in a few sessions or to not remind them at all.
 *
 * @author scottmellors
 * @since 2.2
 */
public class ToPlayStoreDialog extends SimpleDialogFragment {

    private static final String TAG = "to_play_store";
    private static final int RESETCOUNTVAL = 0;

    private boolean mIsLoveDialog = false;

    public void show(Activity activity) {
        ToPlayStoreDialog dialog = new ToPlayStoreDialog();
        dialog.show(activity.getFragmentManager(), TAG);
    }

    public void show(Activity activity, boolean b) {
        ToPlayStoreDialog dialog = new ToPlayStoreDialog();
        dialog.setIsLoveDialog(b);
        dialog.show(activity.getFragmentManager(), TAG);
    }

    @Override
    public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {


        if (mIsLoveDialog) {
            builder.setTitle(getString(R.string.to_play_love_title));
            builder.setMessage(getString(R.string.to_play_love_message));
        } else {
            builder.setTitle(getString(R.string.to_play_title));
            builder.setMessage(getString(R.string.to_play_message));
        }

        builder.setView(LayoutInflater.from(getActivity()).inflate(
                R.layout.running_action_dialog, null));
        builder.setPositiveButton(R.string.to_play_positive,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TTApp.getInstance(getActivity()).setShowDialogAgain(false);
                        getActivity()
                                .startActivity(
                                        new Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("market://details?id=com.triggertrap")));
                        dismiss();
                    }
                });

        builder.setNeutralButton(R.string.to_play_neutral,
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        TTApp.getInstance(getActivity()).setLaunchCount(RESETCOUNTVAL);
                        dismiss();
                    }
                });

        builder.setNegativeButton(R.string.to_play_negative,
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        TTApp.getInstance(getActivity()).setShowDialogAgain(false);
                        dismiss();
                    }
                });

        return builder;
    }

    public void setIsLoveDialog(boolean b) {
        this.mIsLoveDialog = b;
    }
}
