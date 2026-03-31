package com.shofyra.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.shofyra.databinding.ItemBannerBinding;
import com.shofyra.models.BannerItem;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    public interface OnBannerClickListener {
        void onBannerClick(String category);
    }

    private final List<BannerItem> banners;
    private final OnBannerClickListener listener;

    public BannerAdapter(List<BannerItem> banners, OnBannerClickListener listener) {
        this.banners = banners;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBannerBinding binding = ItemBannerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BannerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        holder.bind(banners.get(position));
    }

    @Override
    public int getItemCount() { return banners.size(); }

    class BannerViewHolder extends RecyclerView.ViewHolder {
        private final ItemBannerBinding binding;

        BannerViewHolder(ItemBannerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(BannerItem item) {
            binding.tvBannerTitle.setText(item.getTitle());
            binding.tvBannerSubtitle.setText(item.getSubtitle());
            binding.tvBannerTag.setText(item.getLabel());

            Glide.with(binding.ivBanner.getContext())
                    .load(item.getImageUrl())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(binding.ivBanner);

            binding.btnBannerShop.setOnClickListener(v -> {
                if (listener != null) listener.onBannerClick(item.getTargetCategory());
            });
        }
    }
}