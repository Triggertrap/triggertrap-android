package com.triggertrap.fragments.dialog;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import com.triggertrap.R;
import com.triggertrap.activities.MainActivity;

import eu.inmite.android.lib.dialogs.BaseDialogFragment;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * Dialog which is displayed if a user tried to enable a mode while another is
 * already running. Giving them option to stop the currently running task.
 * 
 * @author scottmellors
 * @since 2.1
 */
public class RunningActionDialog extends SimpleDialogFragment {

	private String mRunningMode = "";

	public void show(Activity activity, String title) {
		RunningActionDialog dialog = new RunningActionDialog();
		dialog.setRunningMode(title);
		dialog.show(activity.getFragmentManager(), "running_action");
	}

	@Override
	public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
		builder.setView(LayoutInflater.from(getActivity()).inflate(
				R.layout.running_action_dialog, null));
		builder.setTitle(mRunningMode);
		builder.setMessage(getResources().getString(
				R.string.would_you_like_to_stop_this_running_));
		builder.setPositiveButton(getResources().getString(R.string.yes),
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						MainActivity mainActivity = (MainActivity) getActivity();
						mainActivity.stopRunningAction();

						dismiss();
					}
				});

		builder.setNegativeButton(getResources().getString(R.string.no),
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						dismiss();
					}
				});
		return builder;
	}

	public void setRunningMode(String runningMode) {
		mRunningMode = runningMode;
	}
}
