package com.triggertrap.util;

import java.util.UUID;

import android.app.Service;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.triggertrap.outputs.OutputDispatcher;

public class PebbleController {

	private static final String TAG = PebbleController.class.getSimpleName();
	// Pebble stuff
	private String appURLStr = "http://tri.gg/tt-pebble.pbw";
	private Uri appURI = Uri.parse(appURLStr);

	private final static UUID PEBBLE_APP_UUID = UUID
			.fromString("96439271-2DE4-4209-A68C-5571CF2C418E");
	private final static int CMD_KEY = 0x01;
	private final static int CMD_DOWN = 0x01;
	private PebbleKit.PebbleDataReceiver dataReceiver;
	private Handler mHandler = new Handler();
	private PebbleTriggerListener mListener;
	
	private OutputDispatcher mOutputDispatcher;
	private Service mService;
	
	public interface PebbleTriggerListener {
		public void onPebbleTriggerReceived();
	}
	
	public PebbleController(OutputDispatcher outputDispatcher, PebbleTriggerListener listener, Service service) {
		mListener = listener;
		mOutputDispatcher = outputDispatcher;
		mService = service;
	}
	
	public void startPebble() {
	
		dataReceiver = new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
			@Override
			public void receiveData(final Context context,
					final int transactionId, final PebbleDictionary data) {
				final int cmd = data.getInteger(CMD_KEY).intValue();

				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// All data received from the Pebble must be ACK'd,
						// otherwise you'll hit time-outs in the
						// watch-app which will cause the watch to feel "laggy"
						// during periods of frequent
						// communication.
						PebbleKit.sendAckToPebble(context, transactionId);

						switch (cmd) {
						// send SMS when the up button is pressed
						case CMD_DOWN:
							// requestLocationForSms();
							Log.d(TAG, "Got Pebble down Trigger");
							mOutputDispatcher.trigger(150);
							if(mListener != null) {
								mListener.onPebbleTriggerReceived();
							}
							break;
						default:
							break;
						}
					}
				});
			}
		};

		PebbleKit.registerReceivedDataHandler(mService, dataReceiver);

		// Intent watchIntent = new Intent();
		// //watchIntent.setAction(android.content.Intent.ACTION_VIEW);
		// watchIntent.setDataAndType(appURI, "application/octet-stream");
		// //watchIntent.setData(appURI);
		// getActivity().startActivity(watchIntent);

		startWatchApp(null);
	}

	public void stopPebble() {
		
		if (dataReceiver != null) {
			mService.unregisterReceiver(dataReceiver);
			dataReceiver = null;
		}
		stopWatchApp(null);

	}

	// Send a broadcast to launch the specified application on the connected
	// Pebble
	public void startWatchApp(View view) {

		PebbleKit.startAppOnPebble(mService.getApplicationContext(),
				PEBBLE_APP_UUID);
		
	}

	// Send a broadcast to close the specified application on the connected
	// Pebble
	public void stopWatchApp(View view) {
		PebbleKit.closeAppOnPebble(mService.getApplicationContext(),
				PEBBLE_APP_UUID);
	}
}
