package com.shofyra.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shofyra.R;
import com.shofyra.databinding.ItemCategoryBinding;
import com.shofyra.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    private final List<Category> categories;
    private final OnCategoryClickListener listener;

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(categories.get(position));
    }

    @Override
    public int getItemCount() { return categories.size(); }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBinding binding;

        CategoryViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Category category) {
            binding.tvCategoryName.setText(category.getName());
            
            if (category.getCategoryImageUrl() != null && !category.getCategoryImageUrl().isEmpty()) {
                com.bumptech.glide.Glide.with(binding.ivCategoryIcon.getContext())
                        .load(category.getCategoryImageUrl())
                        .centerCrop()
                        .into(binding.ivCategoryIcon);
            } else {
                binding.ivCategoryIcon.setImageResource(getCategoryIcon(category.getName()));
            }

            binding.getRoot().setOnClickListener(v -> {
                binding.getRoot().animate().scaleX(0.93f).scaleY(0.93f).setDuration(100)
                        .withEndAction(() -> binding.getRoot().animate()
                                .scaleX(1f).scaleY(1f).setDuration(100).start()).start();
                if (listener != null) listener.onCategoryClick(category);
            });
        }

        private int getCategoryIcon(String name) {
            if (name == null) return R.drawable.ic_category_default;
            switch (name) {
                case "Mobile Phones & Tablets": return R.drawable.ic_cat_mobile;
                case "Televisions":             return R.drawable.ic_cat_tv;
                case "Laptops & Desktops":      return R.drawable.ic_cat_laptop;
                case "Home & Kitchen":          return R.drawable.ic_cat_home;
                case "Bathroom Items":          return R.drawable.ic_cat_bathroom;
                default:                        return R.drawable.ic_category_default;
            }
        }
    }
}