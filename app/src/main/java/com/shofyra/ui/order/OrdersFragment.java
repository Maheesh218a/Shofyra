package com.shofyra.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shofyra.R;
import com.shofyra.models.Order;
import com.shofyra.models.OrderItem;
import com.shofyra.repository.OrderRepository;
import com.shofyra.ui.auth.LoginActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrdersFragment extends Fragment {

    private RecyclerView rvOrders;
    private View layoutEmpty;
    private View progressBar;
    private OrdersAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvOrders = view.findViewById(R.id.rv_orders);
        layoutEmpty = view.findViewById(R.id.layout_empty_orders);
        progressBar = view.findViewById(R.id.progress_orders);

        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OrdersAdapter(new ArrayList<>());
        rvOrders.setAdapter(adapter);

        loadOrders();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders(); // Refresh when returning to the fragment
    }

    private void loadOrders() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Should not happen (MainActivity guards this), but safe fallback
        if (user == null) {
            startActivity(new Intent(requireContext(), LoginActivity.class));
            return;
        }

        showLoading();

        OrderRepository.getInstance().getUserOrders(user.getUid(), new OrderRepository.OrdersListCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                if (!isAdded()) return;
                hideLoading();
                if (orders.isEmpty()) {
                    showEmpty();
                } else {
                    showOrders(orders);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) return;
                hideLoading();
                showEmpty();
            }
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        rvOrders.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showOrders(List<Order> orders) {
        adapter.setOrders(orders);
        rvOrders.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void showEmpty() {
        rvOrders.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }

    // ─── Inner Adapter ─────────────────────────────────────────────
    private static class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

        private List<Order> orders;

        OrdersAdapter(List<Order> orders) {
            this.orders = orders;
        }

        void setOrders(List<Order> orders) {
            this.orders = orders;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Order order = orders.get(position);

            // Order ID
            String displayId = order.getOrderId() != null ? order.getOrderId() : "N/A";
            holder.tvOrderId.setText("Order #" + displayId);

            // Date & Time
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            holder.tvOrderDate.setText(sdf.format(new Date(order.getOrderDate())));

            // Items summary (e.g. "2x Shirt, 1x Shoes")
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < order.getItems().size(); i++) {
                    OrderItem item = order.getItems().get(i);
                    if (i > 0) sb.append(", ");
                    sb.append(item.getQuantity()).append("x ").append(item.getProductName());
                }
                holder.tvItemsSummary.setText(sb.toString());
            } else {
                holder.tvItemsSummary.setText("—");
            }

            // Total
            holder.tvTotal.setText(String.format(Locale.getDefault(), "Rs. %,.2f", order.getTotalAmount()));
        }

        @Override
        public int getItemCount() {
            return orders == null ? 0 : orders.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderId, tvOrderDate, tvItemsSummary, tvTotal;

            ViewHolder(View itemView) {
                super(itemView);
                tvOrderId = itemView.findViewById(R.id.tv_order_id);
                tvOrderDate = itemView.findViewById(R.id.tv_order_date);
                tvItemsSummary = itemView.findViewById(R.id.tv_order_items_summary);
                tvTotal = itemView.findViewById(R.id.tv_order_total);
            }
        }
    }
}
