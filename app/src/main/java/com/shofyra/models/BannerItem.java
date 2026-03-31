package com.shofyra.models;

import com.google.firebase.firestore.PropertyName;

public class BannerItem {
    private String id;
    private String title;
    private String subtitle;
    private String label;
    private String imageUrl;
    private String targetCategory;
    private String ctaText;
    private int sortOrder;
    private boolean isActive;

    public BannerItem() {}

    public BannerItem(String title, String subtitle, String label, String imageUrl, String targetCategory) {
        this.title = title;
        this.subtitle = subtitle;
        this.label = label;
        this.imageUrl = imageUrl;
        this.targetCategory = targetCategory;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    @PropertyName("image_url")
    public String getImageUrl() { return imageUrl; }
    @PropertyName("image_url")
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @PropertyName("target_category")
    public String getTargetCategory() { return targetCategory; }
    @PropertyName("target_category")
    public void setTargetCategory(String targetCategory) { this.targetCategory = targetCategory; }

    @PropertyName("cta_text")
    public String getCtaText() { return ctaText; }
    @PropertyName("cta_text")
    public void setCtaText(String ctaText) { this.ctaText = ctaText; }

    @PropertyName("sort_order")
    public int getSortOrder() { return sortOrder; }
    @PropertyName("sort_order")
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    @PropertyName("is_active")
    public boolean isActive() { return isActive; }
    @PropertyName("is_active")
    public void setActive(boolean active) { isActive = active; }
}