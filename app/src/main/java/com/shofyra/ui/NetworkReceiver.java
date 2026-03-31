package com.shofyra.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * NetworkReceiver — Broadcast Receiver for network changes.
 * Covers: Broadcast Receivers
 *
 * Register dynamically in Activity (not in Manifest for API 24+):
 *   IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
 *   registerReceiver(networkReceiver, filter);
 *
 * Or statically (for older APIs only):
 *   <receiver android:name=".ui.NetworkReceiver">
 *     <intent-filter>
 *       <action android:name="android.net.conn.CONNECTIVITY_CHANGE"/>
 *     </intent-filter>
 *   </receiver>
 */
public class NetworkReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkReceiver";

    public interface NetworkChangeListener {
        void onNetworkConnected();
        void onNetworkDisconnected();
    }

    private NetworkChangeListener listener;

    public NetworkReceiver(NetworkChangeListener listener) {
        this.listener = listener;
    }

    public NetworkReceiver() {} // required for Manifest registration

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return;

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        Log.d(TAG, "Network changed: connected=" + isConnected);

        if (listener != null) {
            if (isConnected) listener.onNetworkConnected();
            else             listener.onNetworkDisconnected();
        }
    }
}