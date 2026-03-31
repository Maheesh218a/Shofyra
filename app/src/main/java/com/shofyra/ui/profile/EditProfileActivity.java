package com.shofyra.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shofyra.R;
import com.shofyra.utils.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etName, etPhone;
    private TextInputLayout tilName, tilPhone;
    private MaterialButton btnSave;
    private View progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    private PreferenceManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        prefs = PreferenceManager.getInstance(this);

        initViews();
        loadCurrentData();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar_edit_profile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etEmail = findViewById(R.id.et_edit_email);
        etName = findViewById(R.id.et_edit_name);
        etPhone = findViewById(R.id.et_edit_phone);
        tilName = findViewById(R.id.til_edit_name);
        tilPhone = findViewById(R.id.til_edit_phone);
        btnSave = findViewById(R.id.btn_save_profile);
        progressBar = findViewById(R.id.progress_edit_profile);

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadCurrentData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        etEmail.setText(user.getEmail());
        etName.setText(prefs.getUserName());
        etPhone.setText(prefs.getUserPhone());

        // Sync from Firestore to be 100% sure
        mDb.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String phone = doc.getString("phone");
                        if (name != null) etName.setText(name);
                        if (phone != null) etPhone.setText(phone);
                    }
                });
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // 1. Basic Name Validation
        if (TextUtils.isEmpty(name)) {
            tilName.setError("Name is required");
            return;
        } else {
            tilName.setError(null);
        }

        // 2. Sri Lankan Phone Validation (07XXXXXXXX)
        if (!phone.isEmpty() && !phone.matches("^07[0-9]{8}$")) {
            tilPhone.setError("Invalid Sri Lankan mobile number (e.g. 0767900101)");
            return;
        } else {
            tilPhone.setError(null);
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        showLoading(true);

        // 3. Unique Mobile Check
        if (!phone.isEmpty()) {
            mDb.collection("users")
                    .whereEqualTo("phone", phone)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Check if the number belongs to ANOTHER user
                            boolean existsForOther = false;
                            for (com.google.firebase.firestore.DocumentSnapshot doc : task.getResult()) {
                                if (!doc.getId().equals(user.getUid())) {
                                    existsForOther = true;
                                    break;
                                }
                            }

                            if (existsForOther) {
                                showLoading(false);
                                tilPhone.setError("Already Register Mobile Number");
                                return;
                            }
                        }
                        
                        // Proceed to update if unique or same user
                        updateProfile(user.getUid(), name, phone);
                    });
        } else {
            updateProfile(user.getUid(), name, phone);
        }
    }

    private void updateProfile(String uid, String name, String phone) {
        Map<String, Object> update = new HashMap<>();
        update.put("name", name);
        update.put("phone", phone);

        mDb.collection("users").document(uid)
                .update(update)
                .addOnSuccessListener(aVoid -> {
                    prefs.saveUserName(name);
                    prefs.saveUserPhone(phone);
                    showLoading(false);
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean isLoading) {
        btnSave.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        etName.setEnabled(!isLoading);
        etPhone.setEnabled(!isLoading);
    }
}
