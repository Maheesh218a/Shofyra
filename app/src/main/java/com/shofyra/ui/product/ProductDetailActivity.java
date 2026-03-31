package com.shofyra.ui.product;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.shofyra.R;
import com.shofyra.adapters.ProductHorizontalAdapter;
import com.shofyra.databinding.ActivityProductDetailBinding;
import com.shofyra.models.Product;
import com.shofyra.ui.auth.LoginActivity;
import com.shofyra.ui.cart.CartActivity;
import com.shofyra.utils.CartManager;
import com.shofyra.viewmodels.ProductViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductDetailActivity extends AppCompatActivity implements SensorEventListener {

    private ActivityProductDetailBinding binding;
    private ProductViewModel viewModel;
    private Product currentProduct;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float lastX, lastY, lastZ;
    private boolean sensorInitialized = false;
    private static final float SHAKE_THRESHOLD = 12f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Toolbar back
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Get product ID from intent
        String productId = getIntent().getStringExtra("product_id");
        if (productId != null) {
            loadProduct(productId);
        }

        // Setup sensor (shake to add to cart)
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Quantity
        binding.btnIncrement.setOnClickListener(v -> {
            if (currentProduct != null) {
                viewModel.incrementQuantity(currentProduct.getStock());
            }
        });
        binding.btnDecrement.setOnClickListener(v -> viewModel.decrementQuantity());
        viewModel.getQuantity().observe(this, qty -> binding.tvQuantity.setText(String.valueOf(qty)));

        // Buttons
        binding.btnAddToCart.setOnClickListener(v -> addToCart());
        binding.btnBuyNow.setOnClickListener(v -> buyNow());
        binding.fabCart.setOnClickListener(v ->
                startActivity(new Intent(this, CartActivity.class)));
    }

    // ─── Load Product ─────────────────────────────────────
    private void loadProduct(String productId) {
        viewModel.resetQuantity();
        viewModel.getProduct(productId).observe(this, product -> {
            if (product != null) {
                currentProduct = product;
                bindProduct(product);
                loadRelatedProducts(product.getCategoryId(), productId);
            }
        });
    }

    private void bindProduct(Product product) {
        binding.tvProductName.setText(product.getName());
        binding.tvPrice.setText(String.format("Rs. %,.0f", product.getPrice()));
        binding.tvDescription.setText(product.getDescription());
        binding.tvRating.setText(String.valueOf(product.getRating()));
        binding.tvCategory.setText(product.getCategoryId());

        // Stock Display
        binding.tvStock.setText("Remaining Products : " + product.getStock());
        if (product.getStock() <= 0) {
            binding.tvStock.setTextColor(getColor(R.color.error));
            binding.tvStock.setText("Out of Stock");
            binding.btnAddToCart.setEnabled(false);
            binding.btnBuyNow.setEnabled(false);
            binding.btnIncrement.setEnabled(false);
            binding.btnDecrement.setEnabled(false);
        } else {
            binding.tvStock.setTextColor(getColor(R.color.text_secondary));
            binding.btnAddToCart.setEnabled(true);
            binding.btnBuyNow.setEnabled(true);
            binding.btnIncrement.setEnabled(true);
            binding.btnDecrement.setEnabled(true);
        }

        // Original price & discount
        if (product.getOriginalPrice() > product.getPrice()) {
            binding.tvOriginalPrice.setVisibility(View.VISIBLE);
            binding.tvOriginalPrice.setText(String.format("Rs. %,.0f", product.getOriginalPrice()));
            int discount = (int) (((product.getOriginalPrice() - product.getPrice())
                    / product.getOriginalPrice()) * 100);
            binding.tvDiscount.setVisibility(View.VISIBLE);
            binding.tvDiscount.setText("-" + discount + "%");
        } else {
            binding.tvOriginalPrice.setVisibility(View.GONE);
            binding.tvDiscount.setVisibility(View.GONE);
        }

        // Load image
        Glide.with(this)
                .load(product.getImageUrl())
                .placeholder(R.drawable.bg_image_placeholder)
                .centerCrop()
                .into(binding.ivProduct);

        // Animate entry
        animateProductEntry();
    }

    private void loadRelatedProducts(String categoryId, String currentProductId) {
        if (categoryId == null) return;
        
        viewModel.getProductsByCategory(categoryId).observe(this, products -> {
            if (products != null && !products.isEmpty()) {
                // Filter out current product
                List<Product> related = products.stream()
                        .filter(p -> !p.getId().equals(currentProductId))
                        .limit(6)
                        .collect(Collectors.toList());

                if (!related.isEmpty()) {
                    binding.layoutRelatedProducts.setVisibility(View.VISIBLE);
                    setupRelatedRecyclerView(related);
                } else {
                    binding.layoutRelatedProducts.setVisibility(View.GONE);
                }
            } else {
                binding.layoutRelatedProducts.setVisibility(View.GONE);
            }
        });
    }

    private void setupRelatedRecyclerView(List<Product> products) {
        ProductHorizontalAdapter adapter = new ProductHorizontalAdapter(products, product -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        binding.rvRelatedProducts.setLayoutManager(
                new androidx.recyclerview.widget.LinearLayoutManager(this, 
                        androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        binding.rvRelatedProducts.setAdapter(adapter);
    }

    // ─── Add to Cart ──────────────────────────────────────
    private void addToCart() {
        if (currentProduct == null) return;
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please login to add items to cart", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        int qty = viewModel.getQuantity().getValue() != null ? viewModel.getQuantity().getValue() : 1;
        boolean added = CartManager.getInstance().addProduct(currentProduct, qty);
        if (added) {
            animateCartButton();
            Toast.makeText(this, currentProduct.getName() + " added to cart (" + qty + ")", Toast.LENGTH_SHORT).show();
        }
    }

    private void buyNow() {
        if (currentProduct == null) return;
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please login to continue", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        int qty = viewModel.getQuantity().getValue() != null ? viewModel.getQuantity().getValue() : 1;
        CartManager.getInstance().addProduct(currentProduct, qty);
        startActivity(new Intent(this, CartActivity.class));
    }

    // ─── Animations ───────────────────────────────────────
    private void animateProductEntry() {
        binding.ivProduct.setAlpha(0f);
        binding.ivProduct.setTranslationY(50f);
        binding.ivProduct.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .start();
    }

    private void animateCartButton() {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.btnAddToCart, "scaleX", 1f, 0.9f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.btnAddToCart, "scaleY", 1f, 0.9f, 1f);
        set.playTogether(scaleX, scaleY);
        set.setDuration(200);
        set.setInterpolator(new OvershootInterpolator());
        set.start();
    }

    // ─── Sensor (Shake to add to cart) ────────────────────
    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if (sensorInitialized) {
                float deltaX = Math.abs(x - lastX);
                float deltaY = Math.abs(y - lastY);
                float deltaZ = Math.abs(z - lastZ);

                if (deltaX > SHAKE_THRESHOLD || deltaY > SHAKE_THRESHOLD || deltaZ > SHAKE_THRESHOLD) {
                    addToCart();
                }
            }

            lastX = x;
            lastY = y;
            lastZ = z;
            sensorInitialized = true;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}