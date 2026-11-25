package com.example.nestmarket;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etConfirmPassword;
    MaterialButton btnBuyer, btnSeller, btnCreateAccount;

    TextView tvLogInRegister;
    FirebaseAuth firebaseAuth;
    private String userType = ""; // Initially unselected

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        //connect ui to backend

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnBuyer = findViewById(R.id.btnBuyer);
        btnSeller = findViewById(R.id.btnSeller);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        tvLogInRegister = findViewById(R.id.tvLogInRegister);

        firebaseAuth = FirebaseAuth.getInstance();

        btnBuyer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userType = "Buyer";
                // Buyer -> dark green background, white text & icon
                btnBuyer.setBackgroundTintList(getResources().getColorStateList(R.color.primary_green, getTheme()));
                btnBuyer.setTextColor(getResources().getColor(R.color.card_white, getTheme()));
                btnBuyer.setIconTint(getResources().getColorStateList(R.color.card_white, getTheme()));

                // Seller -> white background, dark green text & icon
                btnSeller.setBackgroundTintList(getResources().getColorStateList(R.color.card_white, getTheme()));
                btnSeller.setTextColor(getResources().getColor(R.color.primary_green, getTheme()));
                btnSeller.setIconTint(getResources().getColorStateList(R.color.primary_green, getTheme()));
            }
        });

        btnSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userType = "Seller";
                // Seller -> dark green background, white text & icon
                btnSeller.setBackgroundTintList(getResources().getColorStateList(R.color.primary_green, getTheme()));
                btnSeller.setTextColor(getResources().getColor(R.color.card_white, getTheme()));
                btnSeller.setIconTint(getResources().getColorStateList(R.color.card_white, getTheme()));

                // Buyer -> white background, dark green text & icon
                btnBuyer.setBackgroundTintList(getResources().getColorStateList(R.color.card_white, getTheme()));
                btnBuyer.setTextColor(getResources().getColor(R.color.primary_green, getTheme()));
                btnBuyer.setIconTint(getResources().getColorStateList(R.color.primary_green, getTheme()));
            }
        });

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        tvLogInRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            }
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Please enter your name");
            return;
        }

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Not a valid email");
            return;
        }

        if (password.length() < 6 && confirmPassword.length() > 10) {
            etPassword.setError("Password must be between 6 and 10 characters long");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(userType)) {
            Toast.makeText(this, "Please select whether you are a Buyer or Seller", Toast.LENGTH_SHORT).show();
            return;
        }

        //After all validations are passed, register the user
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,
            new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("fullName", name);
                        userData.put("email", email);
                        userData.put("userType", userType);

                        db.collection("users").document(userId)
                            .set(userData)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegistrationActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(RegistrationActivity.this, "Failed to save user profile", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                        Toast.makeText(RegistrationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }
}