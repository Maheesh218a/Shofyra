package com.shofyra.ui;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.shofyra.utils.PreferenceManager;
import com.shofyra.utils.ShofyraNotificationManager;

/**
 * ShofyraMessagingService — handles Firebase Cloud Messaging (FCM).
 * Covers: Notifications → FCM push notifications
 * Register in AndroidManifest.xml under <application>:
 *
 *   <service android:name=".ui.ShofyraMessagingService"
 *       android:exported="false">
 *     <intent-filter>
 *       <action android:name="com.google.firebase.MESSAGING_EVENT" />
 *     </intent-filter>
 *   </service>
 */
public class ShofyraMessagingService extends FirebaseMessagingService {

    private static final String TAG = "ShofyraFCM";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);
        // Save token locally and optionally sync to Firestore
        PreferenceManager.getInstance(this).saveFcmToken(token);
        // TODO: upload token to Firestore user document for targeted push
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d(TAG, "FCM from: " + message.getFrom());

        String title   = "Shofyra";
        String body    = "";
        String type    = "general";

        // Extract notification payload
        if (message.getNotification() != null) {
            title = message.getNotification().getTitle() != null
                    ? message.getNotification().getTitle() : title;
            body  = message.getNotification().getBody() != null
                    ? message.getNotification().getBody() : body;
        }

        // Extract data payload
        if (message.getData().size() > 0) {
            type = message.getData().getOrDefault("type", "general");
        }

        ShofyraNotificationManager notifMgr = ShofyraNotificationManager.getInstance(this);

        switch (type) {
            case "order_shipped":
                String trackingCode = message.getData().getOrDefault("tracking", "N/A");
                notifMgr.showOrderShipped(0, trackingCode);
                break;
            case "promotion":
                notifMgr.showPromotion(title, body);
                break;
            default:
                notifMgr.showPromotion(title, body);
                break;
        }
    }
}