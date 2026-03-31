package com.shofyra.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.shofyra.models.Category;
import com.shofyra.models.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * ProductRepository — Single source of truth for product data from Firestore.
 * Uses LiveData to expose reactive streams to ViewModels.
 */
public class ProductRepository {

    private static final String TAG = "ProductRepository";
    private static final String COLLECTION_PRODUCTS = "products";
    private static final String COLLECTION_CATEGORIES = "categories";

    private static ProductRepository instance;
    private final FirebaseFirestore db;

    private ProductRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static ProductRepository getInstance() {
        if (instance == null) {
            instance = new ProductRepository();
        }
        return instance;
    }

    // ─────────────────────────────────────────────
    //  PRODUCTS
    // ─────────────────────────────────────────────

    /** Fetch ALL products */
    public LiveData<List<Product>> getAllProducts() {
        MutableLiveData<List<Product>> liveData = new MutableLiveData<>();

        db.collection(COLLECTION_PRODUCTS)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Product> products = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Product product = doc.toObject(Product.class);
                        product.setId(doc.getId());
                        products.add(product);
                    }
                    liveData.setValue(products);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching products", e);
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    /** Fetch products by category */
    public LiveData<List<Product>> getProductsByCategory(String categoryId) {
        MutableLiveData<List<Product>> liveData = new MutableLiveData<>();

        db.collection(COLLECTION_PRODUCTS)
                .whereEqualTo("category_id", categoryId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Product> products = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Product product = doc.toObject(Product.class);
                        product.setId(doc.getId());
                        products.add(product);
                    }
                    // Sort client-side to avoid Firestore index requirement
                    products.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                    liveData.setValue(products);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching products by category: " + categoryId, e);
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    /** Fetch featured products */
    public LiveData<List<Product>> getFeaturedProducts() {
        MutableLiveData<List<Product>> liveData = new MutableLiveData<>();

        db.collection(COLLECTION_PRODUCTS)
                .whereEqualTo("is_featured", true)
                .limit(10)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Product> products = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Product product = doc.toObject(Product.class);
                        product.setId(doc.getId());
                        products.add(product);
                    }
                    liveData.setValue(products);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching featured products", e);
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    /** Fetch popular products (top rated) */
    public LiveData<List<Product>> getPopularProducts() {
        MutableLiveData<List<Product>> liveData = new MutableLiveData<>();

        db.collection(COLLECTION_PRODUCTS)
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Product> products = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Product product = doc.toObject(Product.class);
                        product.setId(doc.getId());
                        products.add(product);
                    }
                    liveData.setValue(products);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching popular products", e);
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    /** Fetch single product by ID */
    public LiveData<Product> getProductById(String productId) {
        MutableLiveData<Product> liveData = new MutableLiveData<>();

        db.collection(COLLECTION_PRODUCTS)
                .document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Product product = doc.toObject(Product.class);
                        if (product != null) product.setId(doc.getId());
                        liveData.setValue(product);
                    } else {
                        liveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching product: " + productId, e);
                    liveData.setValue(null);
                });

        return liveData;
    }

    /** Search products by name and optional category (Client-side matching) */
    public LiveData<List<Product>> searchProducts(String query, String categoryId) {
        MutableLiveData<List<Product>> liveData = new MutableLiveData<>();
        String finalQuery = (query != null) ? query.toLowerCase().trim() : "";
        String finalCatId = (categoryId != null) ? categoryId.trim() : "";

        db.collection(COLLECTION_PRODUCTS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Product> products = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            boolean matchesName = finalQuery.isEmpty() || 
                                    (product.getName() != null && product.getName().toLowerCase().contains(finalQuery));
                            boolean matchesCategory = finalCatId.isEmpty() || 
                                    (product.getCategoryId() != null && product.getCategoryId().equals(finalCatId));

                            if (matchesName && matchesCategory) {
                                product.setId(doc.getId());
                                products.add(product);
                            }
                        }
                    }
                    // Sort client-side
                    products.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                    liveData.setValue(products);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching products with cat: " + finalCatId, e);
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    // ─────────────────────────────────────────────
    //  CATEGORIES
    // ─────────────────────────────────────────────

    public LiveData<List<Category>> getAllCategories() {
        MutableLiveData<List<Category>> liveData = new MutableLiveData<>();

        db.collection(COLLECTION_CATEGORIES)
                .orderBy("name")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Category> categories = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Category category = doc.toObject(Category.class);
                        category.setId(doc.getId());
                        categories.add(category);
                    }
                    liveData.setValue(categories);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching categories", e);
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    // ─────────────────────────────────────────────
    //  BANNERS
    // ─────────────────────────────────────────────

    public LiveData<List<com.shofyra.models.BannerItem>> getBanners() {
        MutableLiveData<List<com.shofyra.models.BannerItem>> liveData = new MutableLiveData<>();

        db.collection("banners")
                .orderBy("sort_order")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<com.shofyra.models.BannerItem> banners = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        com.shofyra.models.BannerItem banner = doc.toObject(com.shofyra.models.BannerItem.class);
                        banner.setId(doc.getId());
                        banners.add(banner);
                    }
                    liveData.setValue(banners);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching banners", e);
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }
}