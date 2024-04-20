package com.example.saapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.editTextEmailSignUp);
        editTextPassword = findViewById(R.id.editTextPasswordSignUp);
        Button buttonRegister = findViewById(R.id.buttonSignUp);
        TextView signUpRedirectText = findViewById(R.id.signUpRedirectText);

        buttonRegister.setOnClickListener(view -> {
            String user = editTextEmail.getText().toString().trim();
            String pass = editTextPassword.getText().toString().trim();

            if (user.isEmpty()){
                editTextEmail.setError("Email cannot be empty");
            }
            if(pass.isEmpty()){
                editTextPassword.setError("Password cannot be empty");
            } else{
                auth.createUserWithEmailAndPassword(user, pass).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(SignUpActivity.this, "Signup Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                    } else{
                        Toast.makeText(SignUpActivity.this, "Signup Failed" + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        signUpRedirectText.setOnClickListener(view -> startActivity(new Intent(SignUpActivity.this, LoginActivity.class)));
    }
}
