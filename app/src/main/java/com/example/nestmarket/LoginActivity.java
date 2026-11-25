package com.example.nestmarket;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    TextView tvSignUp, tvForgotPassword;
    MaterialButton btnLogIn;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogIn = findViewById(R.id.btnLogIn);

        firebaseAuth = FirebaseAuth.getInstance();

        btnLogIn.setOnClickListener(v -> loginUser());

        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class))
        );

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPassword.class))
        );

        btnLogIn.setOnClickListener(v -> loginUser());
    }

    private void loginUser(){
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        //validation
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
    this, new OnCompleteListener<AuthResult>() {
               @Override
               public void onComplete(Task<AuthResult> task) {
                   if (task.isSuccessful()) {
                       routeByUserType();
                   } else {
                       String errorMessage = task.getException() != null
                               ? task.getException().getMessage()
                               : "Login failed";
                       Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                   }
               }
        });

    }

    private void routeByUserType() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Could not find your profile", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                            Toast.makeText(LoginActivity.this, "Could not find your profile", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String userType = task.getResult().getString("userType");
                        Intent intent;
                        if ("Buyer".equals(userType)) {
                            Toast.makeText(LoginActivity.this, "Successful login as buyer", Toast.LENGTH_SHORT).show();
                            intent = new Intent(LoginActivity.this, BuyerDashboardActivity.class);
                        } else if ("Seller".equals(userType)) {
                            Toast.makeText(LoginActivity.this, "Successful login as seller", Toast.LENGTH_SHORT).show();
                            intent = new Intent(LoginActivity.this, SellerDashboard.class);
                        } else {
                            Toast.makeText(LoginActivity.this, "Could not find your profile", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        startActivity(intent);
                        finish();
                    }
                });
    }
}