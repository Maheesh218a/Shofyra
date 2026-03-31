package com.shofyra.utils;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.shofyra.R;
import com.shofyra.ui.MainActivity;

/**
 * NotificationHelper — Centralized notification management for Shofyra.
 * Covers: order updates, deal alerts, delivery tracking, promotional.
 */
public class NotificationHelper {

    public static final String CHANNEL_ORDERS    = "shofyra_orders";
    public static final String CHANNEL_DEALS     = "shofyra_deals";
    public static final String CHANNEL_DELIVERY  = "shofyra_delivery";

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);

            manager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ORDERS, "Order Updates", NotificationManager.IMPORTANCE_HIGH));
            manager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_DEALS, "Deal Alerts", NotificationManager.IMPORTANCE_DEFAULT));
            manager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_DELIVERY, "Delivery Tracking", NotificationManager.IMPORTANCE_HIGH));
        }
    }

    public static void sendOrderNotification(Context context, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ORDERS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Order Update")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Request permission (if inside Activity)
                if (context instanceof Activity) {
                    ActivityCompat.requestPermissions((Activity) context,
                            new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                            101);
                }

                return; // stop notification until permission granted
            }
        }

// Send notification
        NotificationManagerCompat.from(context).notify(1001, builder.build());
    }
}