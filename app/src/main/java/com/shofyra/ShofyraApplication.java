package com.shofyra;

import android.app.Application;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.google.firebase.FirebaseApp;
import com.shofyra.receivers.NetworkChangeReceiver;
import com.shofyra.utils.NotificationHelper;

public class ShofyraApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Firebase
        FirebaseApp.initializeApp(this);

        // 2. Notification Channels
        NotificationHelper.createChannels(this);

        // 3. Network Change Broadcast Receiver
        NetworkChangeReceiver receiver = new NetworkChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);

        // 4. Global network state listener
        NetworkChangeReceiver.setListener(new NetworkChangeReceiver.NetworkStateListener() {
            @Override
            public void onNetworkConnected() {
            }
            @Override
            public void onNetworkDisconnected() {
            }
        });
    }
}