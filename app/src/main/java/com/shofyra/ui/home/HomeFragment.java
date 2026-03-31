package com.shofyra.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shofyra.R;
import com.shofyra.adapters.BannerAdapter;
import com.shofyra.adapters.CategoryAdapter;
import com.shofyra.adapters.ProductGridAdapter;
import com.shofyra.adapters.ProductHorizontalAdapter;
import com.shofyra.databinding.FragmentHomeBinding;
import com.shofyra.models.Product;
import com.shofyra.ui.product.ProductDetailActivity;
import com.shofyra.viewmodels.HomeViewModel;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;

    // Auto-scroll banner
    private Handler bannerHandler;
    private Runnable bannerRunnable;
    private int currentBannerPage = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding.btnSideMenu.setOnClickListener(v -> {
            if (getActivity() instanceof com.shofyra.ui.MainActivity) {
                ((com.shofyra.ui.MainActivity) getActivity()).openDrawer();
            }
        });

        setGreeting();
        setupSearch();
        setupBanner();
        setupCategories();
        setupFeaturedProducts();
        setupPopularProducts();
    }

    // ─── Greeting ────────────────────────────────
    private void setGreeting() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        
        String greeting;
        if (hour == 0 && minute >= 1 && minute <= 29) {
            greeting = "Good Night 🌙";
        } else if ((hour == 0 && minute >= 30) || (hour >= 1 && hour < 12)) {
            greeting = "Good Morning 🌤️";
        } else if (hour >= 12 && hour < 15) {
            greeting = "Good Afternoon ☀️";
        } else {
            greeting = "Good Evening 🌙";
        }
        binding.tvGreeting.setText(greeting);
    }

    // ─── Search ──────────────────────────────────
    private void setupSearch() {
        binding.tvSearchHint.setOnClickListener(v -> {
            androidx.navigation.Navigation.findNavController(v).navigate(R.id.action_home_to_search);
        });
        
        if (binding.tvSeeAllDeals != null) {
            binding.tvSeeAllDeals.setOnClickListener(v -> {
                androidx.navigation.Navigation.findNavController(v).navigate(R.id.action_home_to_search);
            });
        }
        
        if (binding.tvSeeAllProducts != null) {
            binding.tvSeeAllProducts.setOnClickListener(v -> {
                androidx.navigation.Navigation.findNavController(v).navigate(R.id.action_home_to_search);
            });
        }
    }

    // ─── Banner ──────────────────────────────────
    private void setupBanner() {
        viewModel.getBanners().observe(getViewLifecycleOwner(), banners -> {
            if (banners == null || banners.isEmpty()) return;
            BannerAdapter adapter = new BannerAdapter(banners, category -> {
                // Navigate to category
            });
            binding.vpBanner.setAdapter(adapter);
            binding.dotsIndicator.attachTo(binding.vpBanner);
            startBannerAutoScroll(banners.size());
        });
    }

    private void startBannerAutoScroll(int pageCount) {
        bannerHandler = new Handler(Looper.getMainLooper());
        bannerRunnable = () -> {
            if (pageCount == 0) return;
            currentBannerPage = (currentBannerPage + 1) % pageCount;
            binding.vpBanner.setCurrentItem(currentBannerPage, true);
            bannerHandler.postDelayed(bannerRunnable, 3500);
        };
        bannerHandler.postDelayed(bannerRunnable, 3500);
    }

    // ─── Categories ──────────────────────────────
    private void setupCategories() {
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            CategoryAdapter adapter = new CategoryAdapter(categories, category -> {
                // TODO: Navigate to category products
            });
            binding.rvCategories.setLayoutManager(
                    new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
            binding.rvCategories.setAdapter(adapter);
        });
        
        if (binding.tvSeeAllCategories != null) {
            binding.tvSeeAllCategories.setOnClickListener(v -> {
                CategoriesBottomSheet bottomSheet = new CategoriesBottomSheet();
                bottomSheet.show(getParentFragmentManager(), "CategoriesBottomSheet");
            });
        }
    }

    // ─── Featured Products ────────────────────────
    private void setupFeaturedProducts() {
        viewModel.getFeaturedProducts().observe(getViewLifecycleOwner(), products -> {
            ProductHorizontalAdapter adapter = new ProductHorizontalAdapter(
                    products, this::openProductDetail);
            binding.rvFeatured.setLayoutManager(
                    new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
            binding.rvFeatured.setAdapter(adapter);
        });
    }

    // ─── Popular Products Grid ────────────────────
    private void setupPopularProducts() {
        viewModel.getPopularProducts().observe(getViewLifecycleOwner(), products -> {
            ProductGridAdapter adapter = new ProductGridAdapter(products, this::openProductDetail);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
            binding.rvProductsGrid.setLayoutManager(gridLayoutManager);
            binding.rvProductsGrid.setAdapter(adapter);
        });
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_name", product.getName());
        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.postDelayed(bannerRunnable, 3500);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }
}