package com.shofyra.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shofyra.R;
import com.shofyra.models.Order;
import com.shofyra.models.OrderItem;
import com.shofyra.ui.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReceiptActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER = "extra_order";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        findViewById(R.id.btn_back_to_home).setOnClickListener(v -> finishToHome());
        
        View toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setOnClickListener(v -> finishToHome());
        }

        Order order = (Order) getIntent().getSerializableExtra(EXTRA_ORDER);
        if (order != null) {
            populateReceipt(order);
        }
    }

    private void populateReceipt(Order order) {
        TextView tvOrderId = findViewById(R.id.tv_receipt_order_id);
        TextView tvDate = findViewById(R.id.tv_receipt_date);
        TextView tvName = findViewById(R.id.tv_receipt_cust_name);
        TextView tvAddress = findViewById(R.id.tv_receipt_cust_address);
        TextView tvContact = findViewById(R.id.tv_receipt_cust_contact);
        TextView tvTotal = findViewById(R.id.tv_receipt_total);

        tvOrderId.setText(order.getOrderId() != null ? order.getOrderId() : "N/A");

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        tvDate.setText(sdf.format(new Date(order.getOrderDate())));

        tvName.setText(order.getCustomerName());
        tvAddress.setText(order.getShippingAddress());
        tvContact.setText(order.getCustomerPhone() + "\n" + order.getCustomerEmail());

        tvTotal.setText(String.format(Locale.getDefault(), "Rs. %,.2f", order.getTotalAmount()));

        RecyclerView recyclerView = findViewById(R.id.rv_receipt_items);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            if (order.getItems() != null) {
                recyclerView.setAdapter(new ReceiptItemAdapter(order.getItems()));
            }
        }
    }

    private void finishToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // Inner Adapter
    private static class ReceiptItemAdapter extends RecyclerView.Adapter<ReceiptItemAdapter.ViewHolder> {

        private final List<OrderItem> items;

        ReceiptItemAdapter(List<OrderItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_receipt_product, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            OrderItem item = items.get(position);
            holder.tvName.setText(item.getProductName());
            holder.tvQtyPrice.setText(item.getQuantity() + " x Rs. " + String.format(Locale.getDefault(), "%,.0f", item.getUnitPrice()));
            double subtotal = item.getQuantity() * item.getUnitPrice();
            holder.tvSubtotal.setText(String.format(Locale.getDefault(), "Rs. %,.0f", subtotal));
        }

        @Override
        public int getItemCount() {
            return items == null ? 0 : items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvQtyPrice, tvSubtotal;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_receipt_item_name);
                tvQtyPrice = itemView.findViewById(R.id.tv_receipt_item_qty_price);
                tvSubtotal = itemView.findViewById(R.id.tv_receipt_item_subtotal);
            }
        }
    }
}
