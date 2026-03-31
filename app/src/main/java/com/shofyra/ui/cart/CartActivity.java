package com.shofyra.ui.cart;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shofyra.adapters.CartAdapter;
import com.shofyra.databinding.ActivityCartBinding;
import com.shofyra.models.CartItem;
import com.shofyra.ui.checkout.CheckoutActivity;
import com.shofyra.utils.CartManager;
import android.content.Intent;

import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {

    private ActivityCartBinding binding;
    private CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        setupRecyclerView();
        updateSummary();

        binding.btnCheckout.setOnClickListener(v ->
                startActivity(new Intent(this, CheckoutActivity.class)));
    }

    private void setupRecyclerView() {
        List<CartItem> items = CartManager.getInstance().getCartItems();
        adapter = new CartAdapter(items, this);
        binding.rvCart.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCart.setAdapter(adapter);

        if (items.isEmpty()) {
            binding.layoutEmptyCart.setVisibility(View.VISIBLE);
            binding.layoutCart.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyCart.setVisibility(View.GONE);
            binding.layoutCart.setVisibility(View.VISIBLE);
        }
    }

    private void updateSummary() {
        CartManager cm = CartManager.getInstance();
        binding.tvSubtotal.setText(cm.getFormattedSubtotal());
        binding.tvTotal.setText(cm.getFormattedTotal());
    }

    @Override
    public void onQuantityChanged() {
        updateSummary();
    }

    @Override
    public void onItemRemoved() {
        updateSummary();
        if (CartManager.getInstance().getCartItems().isEmpty()) {
            binding.layoutEmptyCart.setVisibility(View.VISIBLE);
            binding.layoutCart.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSummary();
    }
}