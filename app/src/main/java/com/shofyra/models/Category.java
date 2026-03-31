package com.shofyra.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

@IgnoreExtraProperties
public class Category {

    // ✅ FIX: Exclude id from Firestore mapping
    @Exclude
    private String id;

    @PropertyName("name")
    private String name;

    @PropertyName("icon")
    private String icon;

    @PropertyName("color_hex")
    private String colorHex;

    @PropertyName("sort_order")
    private int sortOrder;

    // Optional legacy fields
    @PropertyName("categoryId")
    private String categoryId;

    @PropertyName("categoryImageUrl")
    private String categoryImageUrl;

    // Required empty constructor
    public Category() {}

    public Category(String id, String name, String icon, String colorHex) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.colorHex = colorHex;
    }

    // ───── Getters ─────
    @Exclude
    public String getId() { return id; }

    @PropertyName("name")
    public String getName() { return name; }

    @PropertyName("icon")
    public String getIcon() { return icon; }

    @PropertyName("color_hex")
    public String getColorHex() { return colorHex; }

    @PropertyName("sort_order")
    public int getSortOrder() { return sortOrder; }

    @PropertyName("categoryId")
    public String getCategoryId() {
        return categoryId != null ? categoryId : id;
    }

    @PropertyName("categoryImageUrl")
    public String getCategoryImageUrl() { return categoryImageUrl; }

    // ───── Setters ─────
    @Exclude
    public void setId(String id) { this.id = id; }

    @PropertyName("name")
    public void setName(String name) { this.name = name; }

    @PropertyName("icon")
    public void setIcon(String icon) { this.icon = icon; }

    @PropertyName("color_hex")
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    @PropertyName("sort_order")
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    @PropertyName("categoryId")
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    @PropertyName("categoryImageUrl")
    public void setCategoryImageUrl(String categoryImageUrl) { this.categoryImageUrl = categoryImageUrl; }
}