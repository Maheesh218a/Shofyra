package com.shofyra.ui.checkout;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;

import com.shofyra.R;
import com.shofyra.databinding.ActivityCheckoutBinding;
import com.shofyra.models.CartItem;
import com.shofyra.utils.CartManager;
import com.shofyra.utils.PayHereConfig;

import java.util.List;
import java.util.UUID;

public class CheckoutActivity extends AppCompatActivity {

    private static final String TAG           = "CheckoutActivity";
    private ActivityCheckoutBinding binding;
    private static final String PREFS_NAME    = "shofyra_prefs";
    private static final String KEY_NAME      = "saved_name";
    private static final String KEY_ADDRESS   = "saved_address";
    private static final String KEY_CITY      = "saved_city";
    private static final String KEY_PHONE     = "saved_phone";
    private static final String NOTIF_CHANNEL = "shofyra_orders";
    
    private String currentOrderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        CartManager cm = CartManager.getInstance();
        binding.tvSummarySubtotal.setText(cm.getFormattedSubtotal());
        binding.tvSummaryTotal.setText(cm.getFormattedTotal());

        restoreSavedAddress();
        createNotificationChannel();

        binding.btnPlaceOrder.setOnClickListener(v -> validateAndProceedToPayment());
    }

    // ─── Validate & Launch ─────────────────────────────────────────
    private void validateAndProceedToPayment() {
        String name    = getFieldText(binding.etFullName);
        String address = getFieldText(binding.etAddress);
        String city    = getFieldText(binding.etCity);
        String phone   = getFieldText(binding.etPhone);

        if (name.isEmpty() || address.isEmpty() || city.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all billing fields", Toast.LENGTH_SHORT).show();
            return;
        }
        saveAddress(name, address, city, phone);
        launchPayHere(name, address, city, phone);
    }

    private void launchPayHere(String fullName, String address, String city, String phone) {
        CartManager cm = CartManager.getInstance();

        // Amount must be formatted to exactly 2 decimal places
        double realAmount = Double.parseDouble(String.format("%.2f", cm.getSubtotal()));
        double amount = realAmount > 10000 ? 100.00 : realAmount; // cap at 100 LKR for sandbox

        Log.d(TAG, "Real amount: " + realAmount + " | Testing with: " + amount);

        // Split full name into first & last
        String firstName = fullName, lastName = "";
        int spaceIdx = fullName.indexOf(' ');
        if (spaceIdx > 0) {
            firstName = fullName.substring(0, spaceIdx);
            lastName  = fullName.substring(spaceIdx + 1);
        }

        // Get Firebase user email
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = (user != null && user.getEmail() != null)
                ? user.getEmail() : "customer@shofyra.com";

        // Generate unique order ID
        currentOrderId = "SHF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Log for debugging
        Log.d(TAG, "=== PayHere Debug ===");
        Log.d(TAG, "Merchant ID: " + PayHereConfig.MERCHANT_ID);
        Log.d(TAG, "Order ID:    " + currentOrderId);
        Log.d(TAG, "Amount:      " + amount);
        Log.d(TAG, "Currency:    " + PayHereConfig.CURRENCY);
        Log.d(TAG, "Email:       " + email);

        // ✅ SANDBOX — set base URL BEFORE building the request
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);

        InitRequest req = new InitRequest();
        req.setMerchantId(PayHereConfig.MERCHANT_ID);
        req.setMerchantSecret(PayHereConfig.MERCHANT_SECRET); // ✅ raw secret — SDK hashes internally
        req.setCurrency(PayHereConfig.CURRENCY);
        req.setAmount(amount);
        req.setOrderId(currentOrderId);
        req.setItemsDescription("Shofyra Order " + currentOrderId);
        req.setItemsDescription("Shofyra Order");

        // Customer details
        req.getCustomer().setFirstName(firstName);
        req.getCustomer().setLastName(lastName);
        req.getCustomer().setEmail(email);
        req.getCustomer().setPhone(phone);
        req.getCustomer().getAddress().setAddress(address);
        req.getCustomer().getAddress().setCity(city);
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        // Add cart line items
        List<CartItem> cartItems = cm.getCartItems();
        for (CartItem item : cartItems) {
            req.getItems().add(new Item(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getProduct().getPrice()
            ));
        }

        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        //noinspection deprecation
        startActivityForResult(intent, PayHereConfig.PAYHERE_REQUEST_CODE);
    }

    // ─── PayHere Result ────────────────────────────────────────────
    @Override
    @SuppressWarnings({"deprecation", "unchecked"})
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != PayHereConfig.PAYHERE_REQUEST_CODE) return;

        // Log full response for debugging
        if (data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response =
                    (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

            Log.d(TAG, "PayHere resultCode: " + resultCode);
            Log.d(TAG, "PayHere response:   " + (response != null ? response.toString() : "null"));

            if (resultCode == Activity.RESULT_OK && response != null && response.isSuccess()) {
                // ✅ SUCCESS
                processSuccessfulOrder();

            } else if (resultCode == Activity.RESULT_OK) {
                String msg = response != null ? response.toString() : "Unknown error";
                Toast.makeText(this, "Payment failed: " + msg, Toast.LENGTH_LONG).show();

            } else {
                String msg = response != null ? response.toString() : "Cancelled";
                Toast.makeText(this, "Payment cancelled: " + msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ─── Order Processing ──────────────────────────────────────────
    private void processSuccessfulOrder() {
        CartManager cm = CartManager.getInstance();
        List<CartItem> cartItems = cm.getCartItems();

        if (cartItems.isEmpty()) return;

        String name = getFieldText(binding.etFullName);
        String address = getFieldText(binding.etAddress) + ", " + getFieldText(binding.etCity);
        String phone = getFieldText(binding.etPhone);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = (user != null && user.getEmail() != null) ? user.getEmail() : "customer@shofyra.com";
        String uid = (user != null) ? user.getUid() : "guest";

        List<com.shofyra.models.OrderItem> orderItems = new java.util.ArrayList<>();
        for (CartItem item : cartItems) {
            orderItems.add(new com.shofyra.models.OrderItem(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getProduct().getPrice()
            ));
        }

        com.shofyra.models.Order order = new com.shofyra.models.Order(
                uid, name, email, phone, address, cm.getSubtotal(), System.currentTimeMillis(), orderItems
        );
        order.setOrderId(currentOrderId != null ? currentOrderId : "SHF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        binding.btnPlaceOrder.setEnabled(false);
        binding.btnPlaceOrder.setText("Processing Order...");

        com.shofyra.repository.OrderRepository.getInstance().placeOrder(order, new com.shofyra.repository.OrderRepository.OrderCallback() {
            @Override
            public void onSuccess(String dbOrderId) {
                order.setOrderId(dbOrderId); // Use Firestore ID or our own generated ID based on how repo is setup
                cm.clearCart();
                sendOrderNotification();
                
                Toast.makeText(CheckoutActivity.this, "🎉 Order confirmed!", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(CheckoutActivity.this, com.shofyra.ui.order.ReceiptActivity.class);
                intent.putExtra(com.shofyra.ui.order.ReceiptActivity.EXTRA_ORDER, order);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(CheckoutActivity.this, "Payment succeeded but order saving failed.", Toast.LENGTH_LONG).show();
                binding.btnPlaceOrder.setEnabled(true);
                binding.btnPlaceOrder.setText("Place Order");
            }
        });
    }

    // ─── Address helpers ───────────────────────────────────────────
    private void restoreSavedAddress() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        binding.etFullName.setText(prefs.getString(KEY_NAME, ""));
        binding.etAddress.setText(prefs.getString(KEY_ADDRESS, ""));
        binding.etCity.setText(prefs.getString(KEY_CITY, ""));
        binding.etPhone.setText(prefs.getString(KEY_PHONE, ""));
    }

    private void saveAddress(String name, String address, String city, String phone) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putString(KEY_NAME, name)
                .putString(KEY_ADDRESS, address)
                .putString(KEY_CITY, city)
                .putString(KEY_PHONE, phone)
                .apply();
    }

    private String getFieldText(com.google.android.material.textfield.TextInputEditText field) {
        if (field == null) return "";
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    // ─── Notification ──────────────────────────────────────────────
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIF_CHANNEL, "Order Updates", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Shofyra order status notifications");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void sendOrderNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIF_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Order Confirmed! 🎉")
                .setContentText("Your Shofyra order has been placed successfully.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Thank you for shopping with Shofyra! Your order is being processed."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.notify(1001, builder.build());
    }
}