package com.triggertrap.util;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;

import com.triggertrap.TTApp;
import com.triggertrap.fragments.dialog.ScopeFeelingsDialog;

/**
 * Class to handle the display of dialogs coercing users to rate the app in the
 * play store.
 * 
 * @author scottmellors
 * @since 2.2
 */
public class AppRater {

	private static final int DAYS_UNTIL_PROMPT = 3;
	private static final int LAUNCHES_UNTIL_PROMPT = 7;
	private static final long DAY_IN_MILLI = 86400000;

	public static void appLaunched(Activity activity, Context mContext,
			FragmentManager fm) {

		TTApp app = TTApp.getInstance(mContext);
		if (app.getDoNotShowAgain()) {
			return;
		}

		// Increment launch counter
		int launchCount = app.getLaunchCount() + 1;
		app.setLaunchCount(launchCount);

		// Get date of first launch
		Long dateFirstLaunch = app.getFirstLaunchDate();
		
		if (dateFirstLaunch == 0) {
			dateFirstLaunch = System.currentTimeMillis();
			app.setFirstLaunchDate(dateFirstLaunch);
		}

		if (launchCount >= LAUNCHES_UNTIL_PROMPT) {
			if (System.currentTimeMillis() >= (long) (dateFirstLaunch + (DAYS_UNTIL_PROMPT * DAY_IN_MILLI))) {
				
				
				
				showRateDialog(mContext, activity, fm);
			}
		}

	}

	public static void showRateDialog(final Context mContext,
			final Activity activity, final FragmentManager fm) {

		ScopeFeelingsDialog dialog = new ScopeFeelingsDialog();
		dialog.show(activity);

	}

}
