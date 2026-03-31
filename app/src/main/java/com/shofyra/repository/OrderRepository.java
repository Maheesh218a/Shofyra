package com.shofyra.repository;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.shofyra.models.Order;
import com.shofyra.models.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    private static final String TAG = "OrderRepository";
    private static final String COLLECTION_ORDERS = "orders";
    private static final String COLLECTION_PRODUCTS = "products";

    private static OrderRepository instance;
    private final FirebaseFirestore db;

    private OrderRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static OrderRepository getInstance() {
        if (instance == null) {
            instance = new OrderRepository();
        }
        return instance;
    }

    public interface OrderCallback {
        void onSuccess(String orderId);
        void onFailure(Exception e);
    }

    public interface OrdersListCallback {
        void onSuccess(List<Order> orders);
        void onFailure(Exception e);
    }

    /**
     * Atomically saves the order and reduces the stock counts for each purchased item.
     * Uses WriteBatch for atomicity.
     */
    public void placeOrder(Order order, OrderCallback callback) {
        WriteBatch batch = db.batch();

        // 1. Create a new document reference for the order
        DocumentReference orderRef;
        if (order.getOrderId() != null && !order.getOrderId().isEmpty()) {
            orderRef = db.collection(COLLECTION_ORDERS).document(order.getOrderId());
        } else {
            orderRef = db.collection(COLLECTION_ORDERS).document();
        }

        batch.set(orderRef, order);

        // 2. Reduce the stock quantity for each product purchased
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                if (item.getProductId() != null && !item.getProductId().isEmpty()) {
                    DocumentReference productRef = db.collection(COLLECTION_PRODUCTS).document(item.getProductId());
                    batch.update(productRef, "stock", FieldValue.increment(-item.getQuantity()));
                }
            }
        }

        // 3. Commit the batch
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Order successfully placed: " + orderRef.getId());
                    callback.onSuccess(orderRef.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to place order", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Fetch all orders for a specific user, sorted newest-first.
     * Sorting is done in Java to avoid requiring a Firestore composite index.
     */
    public void getUserOrders(String userId, OrdersListCallback callback) {
        db.collection(COLLECTION_ORDERS)
                .whereEqualTo("customer_id", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Order> orders = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Order order = doc.toObject(Order.class);
                        order.setOrderId(doc.getId());
                        orders.add(order);
                    }
                    // Sort newest first in memory — avoids needing a Firestore composite index
                    orders.sort((a, b) -> Long.compare(b.getOrderDate(), a.getOrderDate()));
                    callback.onSuccess(orders);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch user orders", e);
                    callback.onFailure(e);
                });
    }
}
