package com.shofyra.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.List;

@IgnoreExtraProperties
public class Order implements Serializable {

    @Exclude
    private String orderId;

    @PropertyName("customer_id")
    private String customerId;

    @PropertyName("customer_name")
    private String customerName;

    @PropertyName("customer_email")
    private String customerEmail;

    @PropertyName("customer_phone")
    private String customerPhone;

    @PropertyName("shipping_address")
    private String shippingAddress;

    @PropertyName("total_amount")
    private double totalAmount;

    @PropertyName("order_date")
    private long orderDate;

    @PropertyName("items")
    private List<OrderItem> items;

    public Order() {}

    public Order(String customerId, String customerName, String customerEmail, String customerPhone,
                 String shippingAddress, double totalAmount, long orderDate, List<OrderItem> items) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.shippingAddress = shippingAddress;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
        this.items = items;
    }

    @Exclude
    public String getOrderId() { return orderId; }
    @Exclude
    public void setOrderId(String orderId) { this.orderId = orderId; }

    @PropertyName("customer_id")
    public String getCustomerId() { return customerId; }
    @PropertyName("customer_id")
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    @PropertyName("customer_name")
    public String getCustomerName() { return customerName; }
    @PropertyName("customer_name")
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    @PropertyName("customer_email")
    public String getCustomerEmail() { return customerEmail; }
    @PropertyName("customer_email")
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    @PropertyName("customer_phone")
    public String getCustomerPhone() { return customerPhone; }
    @PropertyName("customer_phone")
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    @PropertyName("shipping_address")
    public String getShippingAddress() { return shippingAddress; }
    @PropertyName("shipping_address")
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    @PropertyName("total_amount")
    public double getTotalAmount() { return totalAmount; }
    @PropertyName("total_amount")
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    @PropertyName("order_date")
    public long getOrderDate() { return orderDate; }
    @PropertyName("order_date")
    public void setOrderDate(long orderDate) { this.orderDate = orderDate; }

    @PropertyName("items")
    public List<OrderItem> getItems() { return items; }
    @PropertyName("items")
    public void setItems(List<OrderItem> items) { this.items = items; }
}
