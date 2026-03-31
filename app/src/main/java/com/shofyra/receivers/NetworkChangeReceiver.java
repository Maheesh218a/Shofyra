package com.shofyra.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import com.shofyra.utils.NetworkManager;

/**
 * NetworkChangeReceiver — Broadcast Receiver for connectivity changes.
 *
 * Register in AndroidManifest for legacy support:
 *   <receiver android:name=".receivers.NetworkChangeReceiver"
 *       android:exported="false">
 *     <intent-filter>
 *       <action android:name="android.net.conn.CONNECTIVITY_CHANGE"/>
 *     </intent-filter>
 *   </receiver>
 *
 * For API 24+, prefer NetworkManager.startMonitoring() with NetworkCallback.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    public interface NetworkStateListener {
        void onNetworkConnected();
        void onNetworkDisconnected();
    }

    private static NetworkStateListener listener;

    public static void setListener(NetworkStateListener l) { listener = l; }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean connected = isConnected(context);
        if (listener != null) {
            if (connected) listener.onNetworkConnected();
            else listener.onNetworkDisconnected();
        }
    }

    private boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        android.net.Network network = cm.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities cap = cm.getNetworkCapabilities(network);
        return cap != null && (
                cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }
}