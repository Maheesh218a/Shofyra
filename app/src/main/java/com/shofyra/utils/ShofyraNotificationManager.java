package com.shofyra.utils;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.shofyra.R;
import com.shofyra.ui.MainActivity;

/**
 * ShofyraNotificationManager — creates channels & posts local notifications.
 * Covers: Notifications topic
 */
public class ShofyraNotificationManager {

    // Channel IDs
    public static final String CHANNEL_ORDERS        = "channel_orders";
    public static final String CHANNEL_PROMOTIONS    = "channel_promotions";
    public static final String CHANNEL_DELIVERY      = "channel_delivery";

    // Notification IDs
    public static final int NOTIF_ORDER_PLACED       = 1001;
    public static final int NOTIF_ORDER_SHIPPED      = 1002;
    public static final int NOTIF_ORDER_DELIVERED    = 1003;
    public static final int NOTIF_PROMO              = 2001;

    private final Context context;
    private static ShofyraNotificationManager instance;

    private ShofyraNotificationManager(Context ctx) {
        this.context = ctx.getApplicationContext();
        createChannels();
    }

    public static ShofyraNotificationManager getInstance(Context ctx) {
        if (instance == null) instance = new ShofyraNotificationManager(ctx);
        return instance;
    }

    // ── Channel Registration (required API 26+) ──

    private void createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        nm.createNotificationChannel(new NotificationChannel(
                CHANNEL_ORDERS, "Order Updates",
                NotificationManager.IMPORTANCE_HIGH));

        nm.createNotificationChannel(new NotificationChannel(
                CHANNEL_PROMOTIONS, "Deals & Promotions",
                NotificationManager.IMPORTANCE_DEFAULT));

        NotificationChannel deliveryCh = new NotificationChannel(
                CHANNEL_DELIVERY, "Delivery Tracking",
                NotificationManager.IMPORTANCE_HIGH);
        deliveryCh.enableVibration(true);
        deliveryCh.setVibrationPattern(new long[]{0, 400, 200, 400});
        nm.createNotificationChannel(deliveryCh);
    }

    // ── Order Placed ──────────────────────────────

    public void showOrderPlaced(long orderId, double total) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("open_tab", "orders");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ORDERS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Order Placed! 🎉")
                .setContentText(String.format("Order #%d confirmed — Rs. %,.0f", orderId, total))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(String.format(
                                "Your Shofyra order #%d has been placed successfully!\n" +
                                        "Total: Rs. %,.0f\nWe'll notify you when it ships.", orderId, total)))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setColor(0xFF0057FF);

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

    // ── Order Shipped ─────────────────────────────

    public void showOrderShipped(long orderId, String trackingCode) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_DELIVERY)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Your order is on the way! 🚚")
                .setContentText("Order #" + orderId + " has been shipped")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Order #" + orderId + " shipped!\nTracking: " + trackingCode))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 400, 200, 400})
                .setColor(0xFF00C896);

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

    // ── Promo Notification ────────────────────────

    public void showPromotion(String title, String message) {
        if (!PreferenceManager.getInstance(context).isNotificationsEnabled()) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_PROMOTIONS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setColor(0xFF0057FF);

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

    // ── Cancel ────────────────────────────────────

    public void cancelAll() {
        NotificationManagerCompat.from(context).cancelAll();
    }
}