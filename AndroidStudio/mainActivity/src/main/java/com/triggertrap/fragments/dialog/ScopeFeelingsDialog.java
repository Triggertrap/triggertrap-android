package com.triggertrap.fragments.dialog;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.triggertrap.R;
import com.triggertrap.TTApp;

import eu.inmite.android.lib.dialogs.BaseDialogFragment;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * Dialog fragment using Styled Dialogs gauging user satisfaction and guiding
 * them to the Play store should they like it.
 *
 * @author scottmellors
 * @since 2.2
 */
public class ScopeFeelingsDialog extends SimpleDialogFragment {

    private static final String TAG = "scopefeelings";
    private static final int RESETCOUNTVAL = 4;

    public void show(Context context, Activity activity) {
        ScopeFeelingsDialog dialog = new ScopeFeelingsDialog();

        TTApp.getInstance(context).setLaunchCount(RESETCOUNTVAL);
        dialog.show(activity.getFragmentManager(), TAG);
    }

    @Override
    public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
        builder.setTitle(getString(R.string.scope_dialog_title));
        builder.setMessage(getString(R.string.scope_dialog_message));
        builder.setView(LayoutInflater.from(getActivity()).inflate(
                R.layout.running_action_dialog, null));
        builder.setPositiveButton(R.string.scope_positive, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                ToPlayStoreDialog dialog = new ToPlayStoreDialog();
                dialog.show(getActivity(), true);
            }
        });

        builder.setNeutralButton(R.string.scope_neutral, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                ToPlayStoreDialog dialog = new ToPlayStoreDialog();
                dialog.show(getActivity());
            }
        });

        builder.setNegativeButton(R.string.scope_negative, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TTApp.getInstance(getActivity()).setShowDialogAgain(false);

                dismiss();

                EmailUsDialogFrag dialog = new EmailUsDialogFrag();
                dialog.show(getActivity());
            }
        });


        return builder;
    }
}