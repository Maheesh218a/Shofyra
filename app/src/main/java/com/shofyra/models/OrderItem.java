package com.shofyra.models;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class OrderItem implements Serializable {

    @PropertyName("product_id")
    private String productId;

    @PropertyName("product_name")
    private String productName;

    @PropertyName("quantity")
    private int quantity;

    @PropertyName("unit_price")
    private double unitPrice;

    public OrderItem() {}

    public OrderItem(String productId, String productName, int quantity, double unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    @PropertyName("product_id")
    public String getProductId() { return productId; }
    @PropertyName("product_id")
    public void setProductId(String productId) { this.productId = productId; }

    @PropertyName("product_name")
    public String getProductName() { return productName; }
    @PropertyName("product_name")
    public void setProductName(String productName) { this.productName = productName; }

    @PropertyName("quantity")
    public int getQuantity() { return quantity; }
    @PropertyName("quantity")
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @PropertyName("unit_price")
    public double getUnitPrice() { return unitPrice; }
    @PropertyName("unit_price")
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
}
