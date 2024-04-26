package com.example.saapp;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.saapp.ui.admin.AdminFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.editTextEmailLogin);
        editTextPassword = findViewById(R.id.editTextPasswordLogin);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView signUpRedirectText = findViewById(R.id.signUpRedirectText);
        TextView forgotPasswordRedirectText = findViewById(R.id.forgotPasswordRedirectText);

        buttonLogin.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString();
            String pass = editTextPassword.getText().toString();

            if(!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (!pass.isEmpty()) {
                    auth.signInWithEmailAndPassword(email, pass)
                            .addOnSuccessListener(authResult -> {
                                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class ));
                                finish();
                            }).addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show());
                } else {
                    editTextPassword.setError("Password cannot be empty");
                }

            } else if (email.isEmpty()) {
                editTextEmail.setError("Email cannot be empty");
            } else {
                editTextEmail.setError("Please enter a valid email");
            }
        });

        signUpRedirectText.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));
        forgotPasswordRedirectText.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

    }


}
