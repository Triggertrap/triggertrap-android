package com.triggertrap.fragments.dialog;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import com.triggertrap.R;

import eu.inmite.android.lib.dialogs.BaseDialogFragment;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * SimpleDialogFragment extension for displaying errors with Google Play
 * Services.
 * 
 * @author scottmellors
 * @since 2.1
 */
public class AlertErrorDialog extends SimpleDialogFragment {

	public void show(Activity activity) {
		new AlertErrorDialog().show(activity.getFragmentManager(),
				"alert_error");
	}

	@Override
	public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
		builder.setView(LayoutInflater.from(getActivity()).inflate(
				R.layout.running_action_dialog, null));

		builder.setTitle(
				getResources().getString(R.string.google_play_error_title))
				.setMessage(
						getResources().getString(
								R.string.google_play_error_description));
		builder.setPositiveButton(R.string.ok, new View.OnClickListener() {
			public void onClick(View v) {
				dismiss();
			}
		});
		return builder;
	}
}
