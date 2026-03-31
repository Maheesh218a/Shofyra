package com.shofyra.models;

public class CartItem {

    private Product product;
    private int quantity;

    public CartItem() {}

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }

    public void setProduct(Product product) { this.product = product; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() {
        return product.getPrice() * quantity;
    }

    public String getFormattedTotalPrice() {
        return String.format("Rs. %,.0f", getTotalPrice());
    }

    public void incrementQuantity() { this.quantity++; }

    public void decrementQuantity() {
        if (quantity > 1) this.quantity--;
    }
}