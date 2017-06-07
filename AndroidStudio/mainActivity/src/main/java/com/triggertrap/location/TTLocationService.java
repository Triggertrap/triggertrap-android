package com.triggertrap.location;

import android.app.Activity;
import android.app.Dialog;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.triggertrap.fragments.dialog.AlertErrorDialog;
import com.triggertrap.fragments.dialog.ConnectionErrorDialog;
import com.triggertrap.fragments.dialog.ServiceErrorDialogFragment;

public class TTLocationService implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {

    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final String TAG = TTLocationService.class.getSimpleName();
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 1;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    private Activity mParentActivty;
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    private double mStartLatitude = 0;
    private double mStartLongtitude = 0;
    private LocationListener mListener;


    public interface LocationListener {
        public void onDistanceChanged(float distance, float speed);
    }


    public TTLocationService(Activity parentActivity) {
        mParentActivty = parentActivity;
    }

    public boolean servicesConnected(Dialog serviceErrorDialog) {
        int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mParentActivty);

        //Just used for debug
        //errorCode = ConnectionResult.NETWORK_ERROR;
//		errorCode = ConnectionResult.DEVELOPER_ERROR;
//		errorCode = ConnectionResult.INTERNAL_ERROR;
//		errorCode = ConnectionResult.INVALID_ACCOUNT;
//		errorCode = ConnectionResult.LICENSE_CHECK_FAILED;
//		errorCode = ConnectionResult.SERVICE_INVALID;
//		errorCode = ConnectionResult.RESOLUTION_REQUIRED;
//		errorCode = ConnectionResult.SERVICE_MISSING;
//		errorCode = ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED;


        switch (errorCode) {
            case ConnectionResult.NETWORK_ERROR:
            case ConnectionResult.INTERNAL_ERROR:
                //Null serviceDialog
                //Need to Retry if we get these errors
                ConnectionErrorDialog connectionDialog = new ConnectionErrorDialog();
                connectionDialog.show(mParentActivty, this, serviceErrorDialog);

                break;
            case ConnectionResult.SERVICE_INVALID:
            case ConnectionResult.SERVICE_DISABLED:
            case ConnectionResult.SERVICE_MISSING:
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                serviceErrorDialog = GooglePlayServicesUtil.getErrorDialog(
                        errorCode,
                        mParentActivty,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);

                // If Google Play services can provide an error dialog
                if (serviceErrorDialog != null) {
                    // Create a new DialogFragment for the error dialog
                    ServiceErrorDialogFragment errorFragment =
                            new ServiceErrorDialogFragment();
                    // Set the dialog in the DialogFragment
                    errorFragment.setDialog(serviceErrorDialog);
                    // Show the error dialog in the DialogFragment
                    errorFragment.show(mParentActivty.getFragmentManager(),
                            "Location Updates");
                }
                break;
            case ConnectionResult.SIGN_IN_REQUIRED:
                //Some kind of resolution needed here?
            case ConnectionResult.INVALID_ACCOUNT:
            case ConnectionResult.LICENSE_CHECK_FAILED:
                //Null serviceDialog
                //Need to give dialog stating they need a Valid google play account;
            case ConnectionResult.RESOLUTION_REQUIRED:
                //Not sure what to do here drop through to default
                //ConnectionResult connectionResult = new ConnectionResult(statusCode, pendingIntent)
                AlertErrorDialog errorPlayServices = new AlertErrorDialog();
                errorPlayServices.show(mParentActivty);
            default:
                //If we don't have any of these error cases then the chances are we have SUCCESS which is handled below.
                break;

        }

        if (ConnectionResult.SUCCESS == errorCode) {
            // In debug mode, log the status
            Log.d(TAG, "Google Play services are available.");
            mLocationRequest = LocationRequest.create();
            // Use high accuracy
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

            if (mLocationClient == null) {
                mLocationClient = new LocationClient(mParentActivty, this, this);
            }

            return true;
            // Google Play services was not available for some reason
        } else {
            return false;

        }
    }


    public void startLocationService() {
        mStartLatitude = 0;
        mStartLongtitude = 0;
        mLocationClient.connect();
    }

    public void stopLocationService() {
        mLocationClient.disconnect();
    }

    public void setListener(LocationListener listener) {
        mListener = listener;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        mParentActivty,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            AlertErrorDialog errorPlayServices = new AlertErrorDialog();
            errorPlayServices.show(mParentActivty);
        }

    }


    @Override
    public void onConnected(Bundle arg0) {
        //Toast.makeText(mParentActivty, "Connected", Toast.LENGTH_SHORT).show();
        mLocationClient.requestLocationUpdates(mLocationRequest, this);

    }


    @Override
    public void onDisconnected() {
        //Toast.makeText(mParentActivty, "Disconnected", Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onLocationChanged(Location location) {
        //Zeroth position gives distance in meters.
        float[] results = new float[3];

        if (mStartLatitude != 0) {
            Location.distanceBetween(mStartLatitude, mStartLongtitude, location.getLatitude(), location.getLongitude(), results);
        } else {
            results[0] = 0;
        }

	     /*
	     String msg = "Updated Location: " +
	    		 Double.toString(location.getLatitude()) + "," +
	    		 Double.toString(location.getLongitude()) + " Distance: " + results[0];
	     Toast.makeText(mParentActivty, msg, Toast.LENGTH_SHORT).show();	
	     */

        mStartLatitude = location.getLatitude();
        mStartLongtitude = location.getLongitude();

        if (mListener != null) {
            mListener.onDistanceChanged(results[0], location.getSpeed());
        }

        Log.d(TAG, "Speed is: " + location.getSpeed());
    }
}
