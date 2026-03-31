package com.shofyra.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.shofyra.databinding.ItemCartBinding;
import com.shofyra.models.CartItem;
import com.shofyra.models.Product;

import com.shofyra.utils.CartManager;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface CartItemListener {
        void onQuantityChanged();
        void onItemRemoved();
    }

    private final List<CartItem> items;
    private final CartItemListener listener;

    public CartAdapter(List<CartItem> items, CartItemListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartBinding binding;

        CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CartItem item) {
            Product product = item.getProduct();

            binding.tvProductName.setText(product.getName());
            binding.tvPrice.setText(String.format("Rs. %,.0f", product.getPrice()));
            binding.tvQuantity.setText(String.valueOf(item.getQuantity()));

            Glide.with(binding.ivProductImage.getContext())
                    .load(product.getImageUrl())
                    .centerCrop()
                    .into(binding.ivProductImage);

            binding.btnIncrease.setOnClickListener(v -> {
                if (item.getQuantity() < product.getStock()) {
                    item.incrementQuantity();
                    binding.tvQuantity.setText(String.valueOf(item.getQuantity()));
                    // Sync updated quantity to Firestore
                    CartManager.getInstance().updateQuantity(product.getId(), item.getQuantity());
                    if (listener != null) listener.onQuantityChanged();
                } else {
                    android.widget.Toast.makeText(v.getContext(), 
                            "Stock limit reached", android.widget.Toast.LENGTH_SHORT).show();
                }
            });

            binding.btnDecrease.setOnClickListener(v -> {
                item.decrementQuantity();
                binding.tvQuantity.setText(String.valueOf(item.getQuantity()));
                // Sync updated quantity to Firestore
                CartManager.getInstance().updateQuantity(product.getId(), item.getQuantity());
                if (listener != null) listener.onQuantityChanged();
            });

            binding.btnRemove.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_ID) {
                    items.remove(pos);
                    notifyItemRemoved(pos);
                    // Permanently delete from Firestore
                    CartManager.getInstance().removeProduct(product.getId());
                    if (listener != null) listener.onItemRemoved();
                }
            });
        }
    }
}