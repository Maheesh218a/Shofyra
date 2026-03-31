package com.shofyra.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.shofyra.databinding.ItemProductHorizontalBinding;
import com.shofyra.models.Product;

import java.util.List;

public class ProductHorizontalAdapter extends RecyclerView.Adapter<ProductHorizontalAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private final List<Product> products;
    private final OnProductClickListener listener;

    public ProductHorizontalAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductHorizontalBinding binding = ItemProductHorizontalBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() { return products.size(); }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductHorizontalBinding binding;

        ProductViewHolder(ItemProductHorizontalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product product) {
            binding.tvProductName.setText(product.getName());
            binding.tvPrice.setText(String.format("Rs. %,.0f", product.getPrice()));

            Glide.with(binding.ivProductImage.getContext())
                    .load(product.getImageUrl())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(binding.ivProductImage);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onProductClick(product);
            });
        }
    }
}