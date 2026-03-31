package com.shofyra.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.shofyra.databinding.ActivityLoginBinding;
import com.shofyra.ui.MainActivity;
import com.shofyra.utils.FirebaseSeeder;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        setupListeners();
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> loginUser());

        binding.tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Reset link sent functionality coming soon.", Toast.LENGTH_SHORT).show();
        });

        binding.btnSeedDatabase.setOnClickListener(v -> {
            binding.progressAuth.setVisibility(View.VISIBLE);
            binding.btnSeedDatabase.setEnabled(false);
            new FirebaseSeeder().seedDatabase(this, success -> {
                binding.progressAuth.setVisibility(View.GONE);
                binding.btnSeedDatabase.setEnabled(true);
                if (success) {
                    Toast.makeText(this, "Database Seeded Successfully! You can now login.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Seeding failed.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loginUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Please enter a valid email");
            return;
        } else {
            binding.tilEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Password is required");
            return;
        } else {
            binding.tilPassword.setError(null);
        }

        binding.progressAuth.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    binding.progressAuth.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finishAffinity();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
