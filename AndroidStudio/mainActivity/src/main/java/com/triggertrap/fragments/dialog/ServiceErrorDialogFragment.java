package com.triggertrap.fragments.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

/**
 * Fragment that takes the returned Play Services error dialog and handles it
 * appropriately.
 *
 * @author scottmellors
 * @since 2.1
 */
public class ServiceErrorDialogFragment extends DialogFragment {
    // Global field to contain the error dialog
    private Dialog mDialog;

    // Default constructor. Sets the dialog field to null
    public ServiceErrorDialogFragment() {
        super();
        mDialog = null;
    }

    // Set the dialog to display
    public void setDialog(Dialog dialog) {
        mDialog = dialog;
    }

    // Return a Dialog to the DialogFragment.
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return mDialog;
    }
}
