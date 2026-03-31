package com.shofyra.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shofyra.R;
import com.shofyra.repository.OrderRepository;
import com.shofyra.ui.MapsActivity;
import com.shofyra.ui.profile.EditProfileActivity;
import com.shofyra.utils.MultimediaHelper;
import com.shofyra.utils.PreferenceManager;
import com.shofyra.utils.TelephonyHelper;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

/**
 * ProfileFragment — User profile, settings, telephony, multimedia, maps.
 * Covers: SharedPreferences, SQLite stats, DayNight toggle, Google Maps,
 *         Camera/Gallery (Multimedia), Phone/SMS (Telephony).
 */
public class ProfileFragment extends Fragment {

    private PreferenceManager prefs;
    private FirebaseFirestore mDb;
    private FirebaseAuth mAuth;
    private TelephonyHelper telephonyHelper;
    private MultimediaHelper multimediaHelper;

    private TextView tvUserName, tvUserEmail, tvStatOrders;
    private ImageView ivAvatar;
    private SwitchMaterial switchDarkMode;
    private View btnEditProfile;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs            = PreferenceManager.getInstance(requireContext());
        mDb              = FirebaseFirestore.getInstance();
        mAuth            = FirebaseAuth.getInstance();
        telephonyHelper  = new TelephonyHelper(requireActivity());
            multimediaHelper = new MultimediaHelper(requireActivity());

        bindViews(view);
        loadUserData();
        loadStats();
        setupDarkModeToggle();
        setupMenuItems(view);
        setupPhotoChange(view);
    }

    // ─── Views ────────────────────────────────────

    private void bindViews(View root) {
        tvUserName     = root.findViewById(R.id.tv_user_name);
        tvUserEmail    = root.findViewById(R.id.tv_user_email);
        tvStatOrders   = root.findViewById(R.id.tv_stat_orders);
        ivAvatar       = root.findViewById(R.id.iv_avatar);
        switchDarkMode = root.findViewById(R.id.switch_dark_mode);
        btnEditProfile = root.findViewById(R.id.btn_edit_profile);
        
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> 
                startActivity(new Intent(requireContext(), EditProfileActivity.class)));
        }
    }

    // ─── SharedPrefs: Load User ───────────────────

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            tvUserName.setText("Guest User");
            tvUserEmail.setText("Sign in to sync your profile");
            return;
        }

        // Show what we have in prefs immediately
        tvUserName.setText(prefs.getUserName());
        tvUserEmail.setText(user.getEmail());

        // Sync from Firestore for "Database Saving Name"
        mDb.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && isAdded()) {
                        String name = doc.getString("name");
                        String phone = doc.getString("phone");
                        String picUrl = doc.getString("profilePicUrl");

                        if (name != null) {
                            tvUserName.setText(name);
                            prefs.saveUserName(name);
                        }
                        if (phone != null) {
                            prefs.saveUserPhone(phone);
                        }
                        if (picUrl != null && !picUrl.isEmpty() && ivAvatar != null) {
                            Glide.with(this)
                                    .load(picUrl)
                                    .transform(new CircleCrop())
                                    .placeholder(R.drawable.ic_profile)
                                    .error(R.drawable.ic_profile)
                                    .into(ivAvatar);
                        }
                    }
                });
    }

    // ─── SQLite: Load Stats ───────────────────────

    private void loadStats() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        OrderRepository.getInstance().getUserOrders(user.getUid(), new OrderRepository.OrdersListCallback() {
            @Override
            public void onSuccess(java.util.List<com.shofyra.models.Order> orders) {
                if (isAdded() && tvStatOrders != null) {
                    tvStatOrders.setText(String.valueOf(orders.size()));
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Silently fail or log
            }
        });
    }

    // ─── Dark Mode Toggle ─────────────────────────

    private void setupDarkModeToggle() {
        int currentMode = requireContext().getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        switchDarkMode.setChecked(currentMode == Configuration.UI_MODE_NIGHT_YES);

        switchDarkMode.setOnCheckedChangeListener((btn, isChecked) -> {
            int mode = isChecked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO;
            prefs.setThemeMode(mode);
            AppCompatDelegate.setDefaultNightMode(mode);
            // Activity will recreate automatically — DayNight handles the rest
        });
    }

    // ─── Menu Items ───────────────────────────────

    private void setupMenuItems(View root) {
        // Orders
        View menuOrders = root.findViewById(R.id.menu_orders);
        if (menuOrders != null) {
            setMenuRow(menuOrders, R.drawable.ic_orders, "My Orders");
            menuOrders.setOnClickListener(v ->
                    Navigation.findNavController(v).navigate(R.id.nav_orders));
        }

        // Wishlist
        View menuWishlist = root.findViewById(R.id.menu_wishlist);
        if (menuWishlist != null) {
            setMenuRow(menuWishlist, R.drawable.ic_heart_outline, "Wishlist");
            menuWishlist.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Wishlist", Toast.LENGTH_SHORT).show());
        }

        // Store Locator → Google Maps
        View menuStore = root.findViewById(R.id.menu_store_locator);
        if (menuStore != null) {
            setMenuRow(menuStore, R.drawable.ic_location, "Store Locator");
            menuStore.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), MapsActivity.class)));
        }

        // Notifications
        View menuNotif = root.findViewById(R.id.menu_notifications);
        if (menuNotif != null) {
            setMenuRow(menuNotif, R.drawable.ic_notification, "Notifications");
            menuNotif.setOnClickListener(v -> {
                boolean current = prefs.isNotificationsEnabled();
                prefs.setNotificationsEnabled(!current);
                Toast.makeText(requireContext(),
                        "Notifications " + (!current ? "enabled" : "disabled"),
                        Toast.LENGTH_SHORT).show();
            });
        }

        // Support → Telephony
        View menuSupport = root.findViewById(R.id.menu_support);
        if (menuSupport != null) {
            setMenuRow(menuSupport, R.drawable.ic_phone, "Call Support");
            menuSupport.setOnClickListener(v -> showSupportDialog());
        }

        // Logout
        View btnLogout = root.findViewById(R.id.btn_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                com.shofyra.utils.CartManager.getInstance().clearLocalCartOnly();
                prefs.clearAll();
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show();

                // Restart MainActivity to refresh all UI states including Navigation Drawer
                requireActivity().finishAffinity();
                startActivity(new Intent(requireContext(), com.shofyra.ui.MainActivity.class));
            });
        }
    }

    private void setMenuRow(View row, int iconRes, String label) {
        ImageView icon = row.findViewById(R.id.iv_menu_icon);
        TextView  text = row.findViewById(R.id.tv_menu_label);
        if (icon != null) icon.setImageResource(iconRes);
        if (text != null) text.setText(label);
    }

    // ─── Support Dialog (Telephony) ───────────────

    private void showSupportDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Customer Support")
                .setMessage("Shofyra Support: +94 11 234 5678\nAvailable 8AM–8PM, Mon–Sat")
                .setPositiveButton("Call Now", (d, w) ->
                        telephonyHelper.dialSupport())
                .setNeutralButton("Send SMS", (d, w) ->
                        telephonyHelper.composeSms("+94112345678",
                                "Hello Shofyra Support, I need help with my order."))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─── Camera / Gallery (Multimedia) ────────────

    private void setupPhotoChange(View root) {
        View btnChangePhoto = root.findViewById(R.id.btn_change_photo);
        if (btnChangePhoto != null) {
            btnChangePhoto.setOnClickListener(v -> showPhotoSourceDialog());
        }
    }

    private void showPhotoSourceDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Change Profile Photo")
                .setItems(new String[]{"Take Photo", "Choose from Gallery"}, (d, which) -> {
                    if (which == 0) {
                        // Camera — check permission first
                        if (ContextCompat.checkSelfPermission(requireContext(),
                                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            multimediaHelper.openCamera();
                        } else {
                            requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    MultimediaHelper.REQUEST_CAMERA);
                        }
                    } else {
                        multimediaHelper.openGallery();
                    }
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        multimediaHelper.release();
    }
}