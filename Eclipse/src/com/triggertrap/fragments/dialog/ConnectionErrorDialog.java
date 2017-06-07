package com.triggertrap.fragments.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;

import com.triggertrap.R;
import com.triggertrap.location.TTLocationService;

import eu.inmite.android.lib.dialogs.BaseDialogFragment;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * SimpleDialogFragment extension shown when there is a connection error.
 * 
 * @author scottmellors
 * @since 2.1
 */
public class ConnectionErrorDialog extends SimpleDialogFragment {

	private Dialog mServiceErrorDialog;
	private TTLocationService mLocationService;

	public void setDialog(Dialog serviceErrorDialog) {
		this.mServiceErrorDialog = serviceErrorDialog;
	}

	public void setLocationService(TTLocationService locService) {
		this.mLocationService = locService;
	}

	public void show(Activity activity, TTLocationService locService,
			Dialog dialog) {
		ConnectionErrorDialog connectionDialog = new ConnectionErrorDialog();

		connectionDialog.setDialog(dialog);
		connectionDialog.setLocationService(locService);

		connectionDialog
				.show(activity.getFragmentManager(), "connection_error");
	}

	@Override
	public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
		builder.setView(LayoutInflater.from(getActivity()).inflate(
				R.layout.running_action_dialog, null));
		builder.setTitle(R.string.connect_error_title).setMessage(
				R.string.connect_error_description);
		builder.setPositiveButton(R.string.yes, new View.OnClickListener() {
			public void onClick(View v) {
				dismiss();
				mLocationService.servicesConnected(mServiceErrorDialog);
			}
		}).setNegativeButton(R.string.no, new View.OnClickListener() {
			public void onClick(View v) {
				dismiss();
			}
		});

		return builder;
	}
}
