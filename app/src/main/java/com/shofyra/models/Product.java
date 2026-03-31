package com.shofyra.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

@IgnoreExtraProperties
public class Product {

    // ✅ FIX: Use @Exclude — NOT @PropertyName("id")
    // Firestore finds "id" stored inside the document data (prod_001 has id field).
    // @PropertyName("id") makes Firestore try to map it AND treat it as @DocumentId → CRASH.
    // @Exclude tells Firestore to skip this field entirely during deserialization.
    // We assign it manually: product.setId(doc.getId()) in the repository.
    @Exclude
    private String id;

    @PropertyName("name")
    private String name;

    @PropertyName("price")
    private double price;

    @PropertyName("original_price")
    private double originalPrice;

    @PropertyName("category_id")
    private String categoryId;

    @PropertyName("image_url")
    private String imageUrl;

    @PropertyName("description")
    private String description;

    @PropertyName("stock")
    private int stock;

    @PropertyName("rating")
    private double rating;

    @PropertyName("review_count")
    private int reviewCount;

    @PropertyName("is_featured")
    private boolean isFeatured;

    @PropertyName("badge")
    private String badge;

    @PropertyName("created_at")
    private long createdAt;

    // Required empty constructor for Firestore
    public Product() {}

    public Product(String id, String name, double price, String categoryId,
                   String imageUrl, String description, int stock) {
        this.id          = id;
        this.name        = name;
        this.price       = price;
        this.categoryId  = categoryId;
        this.imageUrl    = imageUrl;
        this.description = description;
        this.stock       = stock;
    }

    // ── Getters ──────────────────────────────────────────────────────
    @Exclude public String getId()          { return id; }

    @PropertyName("name")
    public String getName()                 { return name; }

    @PropertyName("price")
    public double getPrice()                { return price; }

    @PropertyName("original_price")
    public double getOriginalPrice()        { return originalPrice; }

    @PropertyName("category_id")
    public String getCategoryId()           { return categoryId; }

    @PropertyName("image_url")
    public String getImageUrl()             { return imageUrl; }

    @PropertyName("description")
    public String getDescription()          { return description; }

    @PropertyName("stock")
    public int getStock()                   { return stock; }

    @PropertyName("rating")
    public double getRating()               { return rating; }

    @PropertyName("review_count")
    public int getReviewCount()             { return reviewCount; }

    @PropertyName("is_featured")
    public boolean isFeatured()             { return isFeatured; }

    @PropertyName("badge")
    public String getBadge()                { return badge; }

    @PropertyName("created_at")
    public long getCreatedAt()              { return createdAt; }

    // ── Setters ──────────────────────────────────────────────────────
    @Exclude public void setId(String id)   { this.id = id; }

    @PropertyName("name")
    public void setName(String name)                        { this.name = name; }

    @PropertyName("price")
    public void setPrice(double price)                      { this.price = price; }

    @PropertyName("original_price")
    public void setOriginalPrice(double v)                  { this.originalPrice = v; }

    @PropertyName("category_id")
    public void setCategoryId(String categoryId)            { this.categoryId = categoryId; }

    @PropertyName("image_url")
    public void setImageUrl(String imageUrl)                { this.imageUrl = imageUrl; }

    @PropertyName("description")
    public void setDescription(String description)          { this.description = description; }

    @PropertyName("stock")
    public void setStock(int stock)                         { this.stock = stock; }

    @PropertyName("rating")
    public void setRating(double rating)                    { this.rating = rating; }

    @PropertyName("review_count")
    public void setReviewCount(int reviewCount)             { this.reviewCount = reviewCount; }

    @PropertyName("is_featured")
    public void setFeatured(boolean featured)               { isFeatured = featured; }

    @PropertyName("badge")
    public void setBadge(String badge)                      { this.badge = badge; }

    @PropertyName("created_at")
    public void setCreatedAt(long createdAt)                { this.createdAt = createdAt; }

    // ── Helpers ───────────────────────────────────────────────────────
    @Exclude
    public String getFormattedPrice() {
        return String.format("Rs. %,.0f", price);
    }

    @Exclude
    public String getFormattedOriginalPrice() {
        return String.format("Rs. %,.0f", originalPrice);
    }

    @Exclude
    public boolean hasDiscount() {
        return originalPrice > price && originalPrice > 0;
    }

    @Exclude
    public boolean isInStock() {
        return stock > 0;
    }

    @Exclude
    public int getDiscountPercent() {
        if (!hasDiscount()) return 0;
        return (int) ((originalPrice - price) / originalPrice * 100);
    }
}