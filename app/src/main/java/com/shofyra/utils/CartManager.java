package com.shofyra.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.shofyra.models.CartItem;
import com.shofyra.models.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CartManager — Singleton managing cart state with Firestore sync.
 *
 * Firestore structure:
 *   users/{userId}/cart/{productId}  →  { productId, name, price, imageUrl,
 *                                         categoryId, quantity, addedAt }
 *
 * Rules:
 *   - Only authenticated users may add/remove items.
 *   - Call loadCartFromFirebase() after login to restore persisted cart.
 *   - Call clearLocalCartOnly() on logout to wipe memory without touching Firestore.
 */
public class CartManager {

    private static final String TAG = "CartManager";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_CART  = "cart";

    private static CartManager instance;

    private final List<CartItem> cartItems = new ArrayList<>();
    private CartChangeListener listener;

    private CartManager() {}

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public interface CartChangeListener {
        void onCartChanged(int totalItems);
    }

    public void setCartChangeListener(CartChangeListener listener) {
        this.listener = listener;
    }

    // ─── Auth helper ───────────────────────────────────────
    private String currentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }

    private boolean isLoggedIn() {
        return currentUserId() != null;
    }

    // ─── Firestore ref ─────────────────────────────────────
    private com.google.firebase.firestore.CollectionReference cartRef(String uid) {
        return FirebaseFirestore.getInstance()
                .collection(COLLECTION_USERS)
                .document(uid)
                .collection(COLLECTION_CART);
    }

    // ─── Add Product ───────────────────────────────────────
    /**
     * Adds product to the local cart AND syncs to Firestore.
     * Returns false and does nothing if the user is not logged in.
     */
    public boolean addProduct(@NonNull Product product, int quantity) {
        if (!isLoggedIn()) return false;

        // Check if addition would exceed stock
        int stockAvailable = product.getStock();

        // Update in memory
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(product.getId())) {
                int totalNeeded = item.getQuantity() + quantity;
                if (totalNeeded > stockAvailable) {
                    item.setQuantity(stockAvailable);
                } else {
                    item.setQuantity(totalNeeded);
                }
                syncItemToFirestore(item);
                notifyListener();
                return true;
            }
        }
        
        // Capping new item quantity to stock
        int initialQty = Math.min(quantity, stockAvailable);
        CartItem newItem = new CartItem(product, initialQty);
        cartItems.add(newItem);
        syncItemToFirestore(newItem);
        notifyListener();
        return true;
    }

    /** Convenience — add 1 by default. Returns false if not logged in. */
    public boolean addProduct(@NonNull Product product) {
        return addProduct(product, 1);
    }

    // ─── Remove Product ────────────────────────────────────
    public void removeProduct(String productId) {
        cartItems.removeIf(item -> item.getProduct().getId().equals(productId));
        notifyListener();

        // Delete from Firestore
        String uid = currentUserId();
        if (uid != null) {
            cartRef(uid).document(productId)
                    .delete()
                    .addOnSuccessListener(a -> Log.d(TAG, "Cart item deleted: " + productId))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to delete cart item", e));
        }
    }

    // ─── Update Quantity ───────────────────────────────────
    public void updateQuantity(String productId, int newQuantity) {
        if (newQuantity <= 0) {
            removeProduct(productId);
            return;
        }
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(productId)) {
                // Enforce stock limit
                int stockLimit = item.getProduct().getStock();
                int finalQty = Math.min(newQuantity, stockLimit);
                
                item.setQuantity(finalQty);
                syncItemToFirestore(item);
                notifyListener();
                return;
            }
        }
    }

    // ─── Load cart from Firestore (call after login) ───────
    public void loadCartFromFirebase(Runnable onComplete) {
        String uid = currentUserId();
        if (uid == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        cartRef(uid).get()
                .addOnSuccessListener(snapshot -> {
                    cartItems.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        try {
                            // Reconstruct Product from stored fields
                            Product product = new Product();
                            product.setId(doc.getId());
                            product.setName(doc.getString("name"));
                            Object priceObj = doc.get("price");
                            if (priceObj instanceof Number) {
                                product.setPrice(((Number) priceObj).doubleValue());
                            }
                            Object origPriceObj = doc.get("original_price");
                            if (origPriceObj instanceof Number) {
                                product.setOriginalPrice(((Number) origPriceObj).doubleValue());
                            }
                            product.setImageUrl(doc.getString("image_url"));
                            product.setCategoryId(doc.getString("category_id"));
                            
                            Long stockLong = doc.getLong("stock");
                            if (stockLong != null) {
                                product.setStock(stockLong.intValue());
                            }

                            Long quantityLong = doc.getLong("quantity");
                            int quantityValue = (quantityLong != null) ? quantityLong.intValue() : 1;

                            cartItems.add(new CartItem(product, quantityValue));
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing cart item: " + doc.getId(), e);
                        }
                    }
                    notifyListener();
                    if (onComplete != null) onComplete.run();
                    Log.d(TAG, "Cart loaded from Firebase: " + cartItems.size() + " items");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load cart from Firebase", e);
                    if (onComplete != null) onComplete.run();
                });
    }

    // ─── Sync single item to Firestore ─────────────────────
    private void syncItemToFirestore(CartItem item) {
        String uid = currentUserId();
        if (uid == null) return;

        Product p = item.getProduct();
        Map<String, Object> data = new HashMap<>();
        data.put("name",           p.getName());
        data.put("price",          p.getPrice());
        data.put("original_price", p.getOriginalPrice());
        data.put("image_url",      p.getImageUrl());
        data.put("category_id",    p.getCategoryId());
        data.put("stock",          p.getStock());
        data.put("quantity",       item.getQuantity());
        data.put("addedAt",        System.currentTimeMillis());

        cartRef(uid).document(p.getId())
                .set(data)
                .addOnSuccessListener(a -> Log.d(TAG, "Cart item synced: " + p.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to sync cart item", e));
    }

    // ─── Getters ───────────────────────────────────────────
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public int getTotalItemCount() {
        int total = 0;
        for (CartItem item : cartItems) total += item.getQuantity();
        return total;
    }

    public double getSubtotal() {
        double total = 0;
        for (CartItem item : cartItems) total += item.getTotalPrice();
        return total;
    }

    public String getFormattedSubtotal() {
        return String.format("Rs. %,.0f", getSubtotal());
    }

    public String getFormattedTotal() {
        return String.format("Rs. %,.0f", getSubtotal());
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    // ─── Clear ─────────────────────────────────────────────

    /** Clears in-memory cart AND deletes all items from Firestore. */
    public void clearCart() {
        String uid = currentUserId();
        if (uid != null) {
            // Delete every document in the cart sub-collection
            cartRef(uid).get().addOnSuccessListener(snapshot -> {
                for (QueryDocumentSnapshot doc : snapshot) {
                    doc.getReference().delete();
                }
            });
        }
        cartItems.clear();
        notifyListener();
    }

    /** Clears in-memory cart ONLY — does NOT touch Firestore.
     *  Use this on logout so the user's cart is preserved for next login. */
    public void clearLocalCartOnly() {
        cartItems.clear();
        notifyListener();
    }

    // ─── Notify ────────────────────────────────────────────
    private void notifyListener() {
        if (listener != null) {
            listener.onCartChanged(getTotalItemCount());
        }
    }
}