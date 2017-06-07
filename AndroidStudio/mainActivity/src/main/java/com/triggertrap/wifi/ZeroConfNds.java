package com.triggertrap.wifi;

import java.util.ArrayList;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.triggertrap.service.TriggertrapService;

public class ZeroConfNds implements IZeroConf {

    private static final String TAG = ZeroConfNds.class.getSimpleName();
    private static final String TT_SERVICE_TYPE = "_triggertrap._tcp.";
    public static int SERVICE_RESOLVED = 0;
    public static int SERVICE_LOST = 1;

    private String mServiceName;

    private TriggertrapService mParentService;
    private DiscoveryListener mDiscoveryListener = null;
    private ResolveListener mResolveListener = null;
    private NsdManager mNsdManager;

    Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {

            NsdServiceInfo serviceInfo = (NsdServiceInfo) msg.obj;
            String resovledServiceIP = "";
            if (serviceInfo.getHost() != null) {
                resovledServiceIP = serviceInfo.getHost().getHostAddress();
            }
            String resolvedServiceName = serviceInfo.getServiceName();
            int resovledServicePort = serviceInfo.getPort();

            if (msg.what == ZeroConfNds.SERVICE_RESOLVED) {
                Log.d(TAG, "mParentService.wiFiMasterAdded");
                resolvedServiceName = resolvedServiceName.replaceAll("032", " ");
                resolvedServiceName = resolvedServiceName.replaceAll("\\\\", "");
                mParentService.wiFiMasterAdded(resolvedServiceName, resovledServiceIP, resovledServicePort);
            }
            if (msg.what == ZeroConfNds.SERVICE_LOST) {
                Log.d(TAG, "mParentService.wiFiMasterLost");
                mParentService.wiFiMasterRemoved(resolvedServiceName, resovledServiceIP, resovledServicePort);
            }
        }

        ;
    };

    public ZeroConfNds(TriggertrapService parentService) {
        mParentService = parentService;
        mNsdManager = (NsdManager) parentService.getSystemService(Context.NSD_SERVICE);
    }


    public void setupResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }

                //Handle on UI thread
                Message message = handler.obtainMessage();
                message.what = ZeroConfNds.SERVICE_RESOLVED;
                message.obj = serviceInfo;
                handler.sendMessage(message);
            }


        };
    }

    private void setupDiscoveryListener() {
        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(TT_SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else {
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "service lost" + serviceInfo);
                //Handle on UI thread
                Message message = handler.obtainMessage();
                message.what = ZeroConfNds.SERVICE_LOST;
                message.obj = serviceInfo;
                handler.sendMessage(message);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };

    }

    @Override
    public void watch() {
        Log.d(TAG, "Watching " + TT_SERVICE_TYPE);
        if (mDiscoveryListener == null) {
            setupResolveListener();
            setupDiscoveryListener();
        }
        mNsdManager.discoverServices(
                TT_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

    }

    @Override
    public void unwatch() {
        Log.d(TAG, "Unwatching " + TT_SERVICE_TYPE);
        if (mDiscoveryListener == null) {
            return;
        }
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);

    }

    @Override
    public void close() {
        if (mDiscoveryListener == null) {
            return;
        }
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);

    }


    @Override
    public void registerMaster() {
        // TODO Auto-generated method stub

    }


    @Override
    public void unregisterMaster() {
        // TODO Auto-generated method stub

    }

    public void disconnectSlaveFromMaster(String uniqueSlaveName) {

    }

    public ArrayList<TTSlaveInfo> getConnectedSlaves() {
        return new ArrayList<TTSlaveInfo>();
    }
}
