package com.triggertrap.fragments.dialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;

import com.triggertrap.R;

import eu.inmite.android.lib.dialogs.BaseDialogFragment;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * SimpleDialogFragment shown when the user has selected the negative review
 * option, asking them to email us.
 * 
 * @author scottmellors
 * @since 2.1
 */
public class EmailUsDialogFrag extends SimpleDialogFragment {

	public void show(Activity activity) {
		new EmailUsDialogFrag().show(activity.getFragmentManager(), "email_us");
	}

	@Override
	public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
		builder.setView(LayoutInflater.from(getActivity()).inflate(
				R.layout.running_action_dialog, null));
		builder.setTitle(getResources().getString(R.string.meh_title));
		builder.setMessage(getResources().getString(R.string.meh_message));
		builder.setPositiveButton(
				getResources().getString(R.string.send_email),
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();
						Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
								Uri.fromParts("mailto",
										"hello@triggertrap.com", null));
						startActivity(Intent.createChooser(emailIntent,
								getResources().getString(R.string.send_email)));
					}
				});

		builder.setNegativeButton(
				getResources().getString(R.string.to_play_negative),
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();
					}
				});
		return builder;
	}
}