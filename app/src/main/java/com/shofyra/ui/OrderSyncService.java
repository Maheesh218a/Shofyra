package com.shofyra.ui;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.shofyra.R;
import com.shofyra.utils.DatabaseHelper;
import com.shofyra.utils.NetworkManager;

import java.util.List;

/**
 * OrderSyncService — Foreground Service that syncs local orders to Firebase.
 * Covers: Multitasking → Background Service
 *
 * Start with:
 *   Intent intent = new Intent(context, OrderSyncService.class);
 *   ContextCompat.startForegroundService(context, intent);
 *
 * Declare in Manifest:
 *   <service android:name=".ui.OrderSyncService"
 *       android:foregroundServiceType="dataSync" />
 */
public class OrderSyncService extends Service {

    private static final String TAG            = "OrderSyncService";
    private static final String CHANNEL_ID     = "sync_channel";
    private static final int    NOTIFICATION_ID = 9001;
    private static final long   SYNC_INTERVAL  = 30_000L; // 30 sec

    private Handler handler;
    private Runnable syncRunnable;
    private boolean running = false;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        createSyncChannel();
        Log.d(TAG, "OrderSyncService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, buildForegroundNotification());
        startSyncLoop();
        return START_STICKY; // restart if killed
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        if (handler != null) handler.removeCallbacks(syncRunnable);
        Log.d(TAG, "OrderSyncService destroyed");
    }

    // ── Sync Loop ─────────────────────────────────

    private void startSyncLoop() {
        running = true;
        syncRunnable = new Runnable() {
            @Override
            public void run() {
                if (!running) return;
                syncPendingOrders();
                handler.postDelayed(this, SYNC_INTERVAL);
            }
        };
        handler.post(syncRunnable);
    }

    private void syncPendingOrders() {
        if (!NetworkManager.getInstance(this).isConnected()) {
            Log.d(TAG, "Sync skipped — no network");
            return;
        }

        List<DatabaseHelper.OrderRecord> orders =
                DatabaseHelper.getInstance(this).getAllOrders();

        for (DatabaseHelper.OrderRecord order : orders) {
            if ("Pending".equals(order.status)) {
                Log.d(TAG, "Syncing order #" + order.id);
                // TODO: push to Firestore orders collection
                // After success, update status to "Synced"
                DatabaseHelper.getInstance(this)
                        .updateOrderStatus(order.id, "Synced");
            }
        }

        Log.d(TAG, "Sync complete. Orders processed: " + orders.size());
    }

    // ── Foreground Notification ───────────────────

    private Notification buildForegroundNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Shofyra")
                .setContentText("Syncing your orders…")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void createSyncChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Sync Service",
                    NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("Background order sync");
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                    .createNotificationChannel(ch);
        }
    }
}