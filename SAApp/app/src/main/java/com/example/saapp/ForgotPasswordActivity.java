package com.example.saapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        auth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.editTextEmailForgotPass);
        Button buttonForgotPassword = findViewById(R.id.buttonForgotPassword);
        Button buttonBack = findViewById(R.id.buttonBack);

        buttonForgotPassword.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString();
            if (!email.isEmpty()) {

                auth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(ForgotPasswordActivity.this, "Reset Password link has been sent to your registered Email", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(ForgotPasswordActivity.this, "Forgot Password Failed :- " + e.getMessage(), Toast.LENGTH_SHORT).show());

            } else {
                editTextEmail.setError("Email cannot be empty");
            }
        });

        buttonBack.setOnClickListener(view -> startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class)));
    }

}
