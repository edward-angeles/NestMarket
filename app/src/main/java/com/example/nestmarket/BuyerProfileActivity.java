package com.example.nestmarket;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class BuyerProfileActivity extends AppCompatActivity {

    private TextView txtProfileName;
    private TextView txtProfileEmail;
    private TextView txtProfileInitial;

    private Button btnEditProfile;
    private Button btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_profile);

        // Initialize db
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtProfileName = findViewById(R.id.txtProfileName);
        txtProfileEmail = findViewById(R.id.txtProfileEmail);
        txtProfileInitial = findViewById(R.id.txtProfileInitial);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // Load user info
        loadBuyerProfile();

        // edit btn
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(
                    BuyerProfileActivity.this,
                    EditBuyerProfileActivity.class
            );
            startActivity(intent);
        });

        // Logout btn
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();

            Toast.makeText(
                    BuyerProfileActivity.this,
                    "Logged out successfully",
                    Toast.LENGTH_SHORT
            ).show();

            Intent intent = new Intent(
                    BuyerProfileActivity.this,
                    LoginActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        });
    }

    private void loadBuyerProfile() {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(
                    this,
                    "No user is currently logged in",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String userId = currentUser.getUid();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");

                        if (name != null && !name.isEmpty()) {
                            txtProfileName.setText(name);
                            txtProfileInitial.setText(
                                    name.substring(0, 1).toUpperCase()
                            );
                        }

                        if (email != null) {
                            txtProfileEmail.setText(email);
                        } else if (currentUser.getEmail() != null) {
                            txtProfileEmail.setText(
                                    currentUser.getEmail()
                            );
                        }

                    } else {
                        Toast.makeText(
                                this,
                                "Profile information not found",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            this,
                            "Unable to load profile: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}
