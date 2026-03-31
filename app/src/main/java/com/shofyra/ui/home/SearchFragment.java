package com.shofyra.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.shofyra.R;
import com.shofyra.adapters.ProductGridAdapter;
import com.shofyra.ui.product.ProductDetailActivity;
import com.shofyra.utils.AnimationUtils;
import com.shofyra.utils.CartManager;
import com.shofyra.utils.DatabaseHelper;
import com.shofyra.viewmodels.ProductViewModel;

/**
 * SearchFragment — live product search with:
 * - Debounced text input (300ms delay)
 * - Category chip filters
 * - SQLite search history
 * - Empty & loading states
 */
public class SearchFragment extends Fragment {

    private static final long DEBOUNCE_MS = 300;

    private ProductViewModel viewModel;
    private TextInputEditText etSearch;
    private RecyclerView rvResults;
    private View layoutEmpty, shimmerSearch;
    private ChipGroup chipGroupFilters;
    private ProductGridAdapter adapter;

    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;
    private String activeCategory = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        etSearch        = view.findViewById(R.id.et_search);
        rvResults       = view.findViewById(R.id.rv_search_results);
        layoutEmpty     = view.findViewById(R.id.layout_empty_search);
        shimmerSearch   = view.findViewById(R.id.shimmer_search);
        chipGroupFilters= view.findViewById(R.id.chip_group_filters);

        setupRecyclerView();
        setupSearchInput();
        setupCategoryChips();
        loadAllProducts();
    }

    // ─── RecyclerView ─────────────────────────────

    private void setupRecyclerView() {
        adapter = new ProductGridAdapter(requireContext(),
                product -> {
                    Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
                    intent.putExtra("product_id", product.getId());
                    startActivity(intent);
                },
                product -> CartManager.getInstance().addProduct(product, 1));

        rvResults.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvResults.setAdapter(adapter);
    }

    // ─── Search Input ─────────────────────────────

    private void setupSearchInput() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                if (debounceRunnable != null) debounceHandler.removeCallbacks(debounceRunnable);
                debounceRunnable = () -> performSearch(s.toString().trim());
                debounceHandler.postDelayed(debounceRunnable, DEBOUNCE_MS);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // ─── Category Chips ───────────────────────────

    private void setupCategoryChips() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                activeCategory = null;
            } else {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    String text = chip.getText().toString();
                    activeCategory = text.equalsIgnoreCase("All") ? null : resolveCategoryName(text);
                }
            }
            String query = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
            performSearch(query);
        });
    }

    private String resolveCategoryName(String chipText) {
        switch (chipText) {
            case "Mobile":   return "mobile_phones";
            case "TV":       return "televisions";
            case "Laptops":  return "laptops_desktops";
            case "Kitchen":  return "home_kitchen";
            case "Bathroom": return "bathroom_items";
            default:         return chipText.toLowerCase().replace(" ", "_");
        }
    }

    // ─── Search Logic ─────────────────────────────

    private void performSearch(String query) {
        showLoading(true);

        if (!query.isEmpty()) {
            // Save to SQLite history
            DatabaseHelper.getInstance(requireContext()).addSearchQuery(query);
        }

        // Use combined search in all cases
        viewModel.searchProducts(query, activeCategory).observe(getViewLifecycleOwner(), products -> {
            showLoading(false);
            if (products == null || products.isEmpty()) {
                showEmpty(true);
            } else {
                showEmpty(false);
                adapter.submitList(products);
                AnimationUtils.staggerIn(rvResults, 0);
            }
        });
    }

    private void loadAllProducts() {
        showLoading(true);
        viewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            showLoading(false);
            if (products != null && !products.isEmpty()) {
                showEmpty(false);
                adapter.submitList(products);
                AnimationUtils.staggerIn(rvResults, 0);
            } else {
                showEmpty(true);
            }
        });
    }

    // ─── State Helpers ────────────────────────────

    private void showLoading(boolean show) {
        if (shimmerSearch != null)
            shimmerSearch.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmpty(boolean show) {
        layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        rvResults.setVisibility(show ? View.GONE : View.VISIBLE);
        if (show) AnimationUtils.fadeIn(layoutEmpty, 300);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        debounceHandler.removeCallbacks(debounceRunnable);
    }
}