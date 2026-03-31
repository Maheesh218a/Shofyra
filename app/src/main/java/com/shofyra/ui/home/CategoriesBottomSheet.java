package com.shofyra.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.shofyra.R;
import com.shofyra.adapters.CategoryAdapter;
import com.shofyra.viewmodels.HomeViewModel;

public class CategoriesBottomSheet extends BottomSheetDialogFragment {

    private HomeViewModel viewModel;
    private RecyclerView rvCategories;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCategories = view.findViewById(R.id.rv_bottom_sheet_categories);
        rvCategories.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            CategoryAdapter adapter = new CategoryAdapter(categories, category -> {
                // Clicking a category from the bottom sheet
                // Can dismiss sheet and trigger logic, or simply dismiss
                dismiss();
            });
            rvCategories.setAdapter(adapter);
        });
    }
}
