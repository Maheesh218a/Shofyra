package com.shofyra.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shofyra.R;
import com.shofyra.databinding.ActivityMainBinding;
import com.shofyra.ui.auth.LoginActivity;
import com.shofyra.ui.cart.CartActivity;
import com.shofyra.utils.CartManager;
import com.shofyra.utils.PreferenceManager;

public class MainActivity extends AppCompatActivity implements CartManager.CartChangeListener {

    private ActivityMainBinding binding;
    private NavController navController;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_cart) {
                if (mAuth.getCurrentUser() == null) {
                    startActivity(new Intent(this, LoginActivity.class));
                    return false;
                }
                startActivity(new Intent(this, CartActivity.class));
                return false;
            }
            if (id == R.id.nav_orders) {
                if (mAuth.getCurrentUser() == null) {
                    startActivity(new Intent(this, LoginActivity.class));
                    return false;
                }
                // authenticated — let Navigation handle it
            }
            return NavigationUI.onNavDestinationSelected(item, navController)
                    || onSupportNavigateUp();
        });

        CartManager.getInstance().setCartChangeListener(this);
        updateCartBadge(CartManager.getInstance().getTotalItemCount());

        setupDrawerNavigation();
    }

    private void setupDrawerNavigation() {
        binding.navViewDrawer.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            
            if (id == R.id.nav_login) {
                startActivity(new Intent(this, LoginActivity.class));
            } else if (id == R.id.nav_logout) {
                CartManager.getInstance().clearLocalCartOnly();
                PreferenceManager.getInstance(this).clearAll();
                mAuth.signOut();
                updateUIState();
                
                finishAffinity();
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.nav_home) {
                if (navController != null) navController.navigate(id);
            } else if (id == R.id.nav_profile) {
                if (navController != null) navController.navigate(id);
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
            } else if (id == R.id.nav_orders) {
                if (mAuth.getCurrentUser() != null) {
                    if (navController != null) navController.navigate(id);
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                }
            } else if (id == R.id.nav_watchlist) {
                // Future Watchlist Implementation
            }

            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    public void openDrawer() {
        if (binding.drawerLayout != null) {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUIState();
        // If logged in, sync the cart from Firestore (restores cart after logout/re-login)
        if (mAuth.getCurrentUser() != null) {
            CartManager.getInstance().loadCartFromFirebase(() -> {
                updateCartBadge(CartManager.getInstance().getTotalItemCount());
            });
        } else {
            updateCartBadge(CartManager.getInstance().getTotalItemCount());
        }
    }

    private void updateUIState() {
        FirebaseUser user = mAuth.getCurrentUser();
        boolean isLoggedIn = (user != null);

        binding.navViewDrawer.getMenu().setGroupVisible(R.id.group_guest, !isLoggedIn);
        binding.navViewDrawer.getMenu().setGroupVisible(R.id.group_authenticated, isLoggedIn);
        binding.navViewDrawer.getMenu().findItem(R.id.nav_logout).setVisible(isLoggedIn);

        // Update Nav Header
        View headerView = binding.navViewDrawer.getHeaderView(0);
        if (headerView != null) {
            TextView title = headerView.findViewById(R.id.nav_header_title);
            TextView subtitle = headerView.findViewById(R.id.nav_header_subtitle);
            
            if (isLoggedIn) {
                // Use cached name if available
                String cachedName = PreferenceManager.getInstance(this).getUserName();
                title.setText("Welcome Back, " + (cachedName != null ? cachedName : ""));
                subtitle.setText(user.getEmail());

                // Refresh from Firestore for "Database Saving Name"
                FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                        .get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                String name = doc.getString("name");
                                if (name != null) {
                                    title.setText("Welcome Back, " + name);
                                    PreferenceManager.getInstance(this).saveUserName(name);
                                }
                            }
                        });
            } else {
                title.setText("Welcome!");
                subtitle.setText("Sign in to browse customized content");
            }
        }
    }

    @Override
    public void onCartChanged(int totalItems) {
        updateCartBadge(totalItems);
    }

    private void updateCartBadge(int count) {
        if (count > 0) {
            binding.bottomNavigation.getOrCreateBadge(R.id.nav_cart).setNumber(count);
        } else {
            binding.bottomNavigation.removeBadge(R.id.nav_cart);
        }
    }
}