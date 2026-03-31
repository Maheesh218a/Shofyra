package com.shofyra.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shofyra.models.BannerItem;
import com.shofyra.models.Category;
import com.shofyra.models.Product;
import com.shofyra.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final ProductRepository repository = ProductRepository.getInstance();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<List<Product>> getFeaturedProducts() {
        return repository.getFeaturedProducts();
    }

    public LiveData<List<Product>> getPopularProducts() {
        return repository.getPopularProducts();
    }

    public LiveData<List<Category>> getCategories() {
        return repository.getAllCategories();
    }

    public LiveData<Boolean> isLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public LiveData<List<BannerItem>> getBanners() {
        return repository.getBanners();
    }
}