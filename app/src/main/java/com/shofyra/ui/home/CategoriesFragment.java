package com.shofyra.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shofyra.R;
import com.shofyra.adapters.CategoryAdapter;
import com.shofyra.viewmodels.HomeViewModel;

public class CategoriesFragment extends Fragment {

    private HomeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_all_categories);
        View progress = view.findViewById(R.id.progress_categories);

        rv.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            progress.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
            CategoryAdapter adapter = new CategoryAdapter(categories, category -> {
                // TODO: Navigate to products filtered by category
            });
            rv.setAdapter(adapter);
        });
    }
}
