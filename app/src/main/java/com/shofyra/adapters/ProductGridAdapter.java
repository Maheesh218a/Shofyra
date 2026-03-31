package com.shofyra.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.shofyra.databinding.ItemProductGridBinding;
import com.shofyra.models.Product;
import com.shofyra.utils.CartManager;

import java.util.ArrayList;
import java.util.List;

public class ProductGridAdapter extends RecyclerView.Adapter<ProductGridAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnAddToCartListener {
        void onAddToCart(Product product);
    }

    private List<Product> products;
    private final OnProductClickListener listener;
    private OnAddToCartListener cartListener;

    // ─── Constructor 1: from HomeFragment (2 args) ────────
    public ProductGridAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products != null ? products : new ArrayList<>();
        this.listener = listener;
        this.cartListener = null;
    }

    // ─── Constructor 2: from SearchFragment (3 args) ──────
    public ProductGridAdapter(Context context, OnProductClickListener listener, OnAddToCartListener cartListener) {
        this.products = new ArrayList<>();
        this.listener = listener;
        this.cartListener = cartListener;
    }

    // ─── Update methods ───────────────────────────────────
    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts != null ? newProducts : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void submitList(List<Product> newProducts) {
        this.products = newProducts != null ? new ArrayList<>(newProducts) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductGridBinding binding = ItemProductGridBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductGridBinding binding;

        ProductViewHolder(ItemProductGridBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product product) {
            binding.tvProductName.setText(product.getName());
            binding.tvPrice.setText(String.format("Rs. %,.0f", product.getPrice()));

            // Discount badge
            if (product.getOriginalPrice() > product.getPrice()) {
                int discount = (int) (((product.getOriginalPrice() - product.getPrice())
                        / product.getOriginalPrice()) * 100);
                binding.tvDiscount.setText("-" + discount + "%");
                binding.tvDiscount.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvDiscount.setVisibility(android.view.View.GONE);
            }

            // Rating
            binding.tvRating.setText(String.valueOf(product.getRating()));

            // Image
            Glide.with(binding.ivProductImage.getContext())
                    .load(product.getImageUrl())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(binding.ivProductImage);

            // Click
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onProductClick(product);
            });

            // Add to cart
            binding.btnAddCart.setOnClickListener(v -> {
                if (cartListener != null) {
                    cartListener.onAddToCart(product);
                } else {
                    CartManager.getInstance().addProduct(product);
                }
            });
        }
    }
}