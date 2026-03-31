package com.shofyra.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shofyra.models.Product;
import com.shofyra.repository.ProductRepository;

import java.util.List;

public class ProductViewModel extends ViewModel {

    private final ProductRepository repository = ProductRepository.getInstance();
    private final MutableLiveData<Integer> quantity = new MutableLiveData<>(1);

    public LiveData<Product> getProduct(String productId) {
        return repository.getProductById(productId);
    }

    public LiveData<List<Product>> getAllProducts() {
        return repository.getAllProducts();
    }

    public LiveData<List<Product>> getProductsByCategory(String category) {
        return repository.getProductsByCategory(category);
    }

    public LiveData<List<Product>> searchProducts(String query, String categoryId) {
        return repository.searchProducts(query, categoryId);
    }

    public LiveData<Integer> getQuantity() { return quantity; }

    public void incrementQuantity(int maxStock) {
        Integer current = quantity.getValue();
        if (current != null && current < maxStock) {
            quantity.setValue(current + 1);
        }
    }

    public void decrementQuantity() {
        Integer current = quantity.getValue();
        if (current != null && current > 1) quantity.setValue(current - 1);
    }

    public void resetQuantity() { quantity.setValue(1); }
}