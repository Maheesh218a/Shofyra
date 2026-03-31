package com.shofyra.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * TelephonyHelper — phone call, SMS, and telephony utilities.
 * Covers: Telephony
 *
 * Required permissions in Manifest:
 *   <uses-permission android:name="android.permission.CALL_PHONE"/>
 *   <uses-permission android:name="android.permission.SEND_SMS"/>
 *   <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
 */
public class TelephonyHelper {

    public static final int REQUEST_CALL     = 301;
    public static final int REQUEST_SMS      = 302;

    private static final String SUPPORT_NUMBER = "+94112345678"; // Shofyra customer support

    private final Activity activity;

    public TelephonyHelper(Activity activity) {
        this.activity = activity;
    }

    // ── Phone Calls ───────────────────────────────

    /** Dial support number (opens dialer without calling) */
    public void dialSupport() {
        dial(SUPPORT_NUMBER);
    }

    public void dial(String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        activity.startActivity(intent);
    }

    /** Direct call — requires CALL_PHONE permission */
    public void callDirect(String phone) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
        activity.startActivity(intent);
    }

    // ── SMS ───────────────────────────────────────

    /** Open SMS composer */
    public void composeSms(String phone, String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone));
        intent.putExtra("sms_body", message);
        activity.startActivity(intent);
    }

    /** Send SMS programmatically — requires SEND_SMS permission */
    public boolean sendSms(String phone, String message) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS);
            return false;
        }
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phone, null, message, null, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Send order confirmation SMS to customer.
     * Called after order is placed successfully.
     */
    public void sendOrderConfirmationSms(String customerPhone, long orderId, double total) {
        String msg = String.format(
                "Hi! Your Shofyra order #%d has been confirmed.\nTotal: Rs. %,.0f\n" +
                        "Thank you for shopping with Shofyra!", orderId, total);
        composeSms(customerPhone, msg);
    }

    // ── Network / Carrier Info ────────────────────

    public String getCarrierName() {
        TelephonyManager tm = (TelephonyManager)
                activity.getSystemService(Activity.TELEPHONY_SERVICE);
        return tm != null ? tm.getNetworkOperatorName() : "Unknown";
    }

    public boolean isSimAvailable() {
        TelephonyManager tm = (TelephonyManager)
                activity.getSystemService(Activity.TELEPHONY_SERVICE);
        return tm != null && tm.getSimState() == TelephonyManager.SIM_STATE_READY;
    }
}