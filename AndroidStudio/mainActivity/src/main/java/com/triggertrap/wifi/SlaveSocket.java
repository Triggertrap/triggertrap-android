package com.triggertrap.wifi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.util.Log;

public class SlaveSocket {
    private static final String TAG = SlaveSocket.class.getSimpleName();

    private Handler handler = new Handler();
    public String callback;
    public String deviceName = "Android device";

    private boolean connected = false;
    private Thread thread = null;
    private Socket socket;
    private SlaveListener mListener = null;

    public interface SlaveListener {
        public void onSlaveBeep();
    }

    public SlaveSocket(SlaveListener listener) {
        mListener = listener;
    }

    public void close() {
        Log.d(TAG, "Closing Slave socket");
        if (thread != null) {
            connected = false;
        }
        try {
            socket.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    public void connect(final String ipAddress, final int serverPort) {

        handler.post(new Runnable() {
            public void run() {
                Log.d("SlaveSocket", "android.os.Build.MODEL " + android.os.Build.MODEL + " android.os.Build.PRODUCT: " + android.os.Build.PRODUCT);
                BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
                if (bluetooth != null) {
                    String name = bluetooth.getName();
                    if (name != null) {
                        deviceName = name;
                    } else if (android.os.Build.MODEL != null) {
                        deviceName = android.os.Build.MODEL;
                    }
                    Log.d("SlaveSocket", "btname: " + deviceName);
                }
            }
        });


        class ClientThread implements Runnable {

            public void run() {

                try {
                    Log.d("SlaveSocket", "Connecting to " + ipAddress);
                    InetAddress serverAddr = InetAddress.getByName(ipAddress);
                    Log.d("SlaveSocket", "C: Connecting..." + serverAddr + " port: " + serverPort);
                    socket = new Socket(serverAddr, serverPort);
                    connected = true;
                    Log.i("SlaveSocket", "Sending command. : " + deviceName);
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    out.println(deviceName + "\r");
                    Log.i("SlaveSocket", "Sent.");
                    sendCallback("connected");
                    while (connected) {
                        try {
                            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String line = null;
                            while ((line = in.readLine()) != null) {
                                final String send = line;
                                handler.post(new Runnable() {
                                    public void run() {
                                        gotMessage(send);
                                    }
                                });
                            }
                            Log.d("SlaveSocket", "Closed");
                            connected = false;
                        } catch (Exception e) {
                            //e.printStackTrace();
                            Log.e("SlaveSocket", "S: Error", e);
                            connected = false;

                        }
                    }
                    Log.d("SlaveSocket", "C: Closing.");

                    socket.close();
                    closed();
                    Log.d("SlaveSocket", "C: Closed.");
                } catch (Exception e) {
                    //e.printStackTrace();

                    Log.e("SlaveSocket", "C: Error", e);
                    connected = false;
                } finally {
                    connected = false;
                    closed();
                }
            }
        }

        thread = new Thread(new ClientThread());
        thread.start();
    }

    public void closed() {
        //this.success("closed", this.callback);
    }

    public void sendCallback(String action) {
        //TODO: Implement call backs for UI
//		PluginResult result = new PluginResult(PluginResult.Status.OK, action);
//		result.setKeepCallback(true);
//		this.success(result, this.callback);
    }

    private void gotMessage(String msg) {
        //TODO: Implement call backs for UI
        Log.d("SlaveSocket", "Got message: " + msg);
        if (mListener != null) {
            mListener.onSlaveBeep();
        }


    }

}
