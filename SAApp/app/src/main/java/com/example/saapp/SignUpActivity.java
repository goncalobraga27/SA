package com.example.saapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword,editTextRole;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.editTextEmailSignUp);
        editTextPassword = findViewById(R.id.editTextPasswordSignUp);
        editTextRole = findViewById(R.id.editRoleSignUp);

        Button buttonRegister = findViewById(R.id.buttonSignUp);
        TextView signUpRedirectText = findViewById(R.id.signUpRedirectText);

        buttonRegister.setOnClickListener(view -> {
            String user = editTextEmail.getText().toString().trim();
            String pass = editTextPassword.getText().toString().trim();
            String role = editTextRole.getText().toString().trim();

            if (user.isEmpty()){
                editTextEmail.setError("Email cannot be empty");
            }
            if(pass.isEmpty()){
                editTextPassword.setError("Password cannot be empty");
            } else{
                auth.createUserWithEmailAndPassword(user, pass).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
                        if (userFirebase != null) {
                            storeUserRole(userFirebase,role);
                            setupUserPoints(userFirebase);
                        }
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
    private void storeUserRole(FirebaseUser user,String role){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("role", role);
        db.collection("users")
                .document(user.getUid())
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ROLES", "Sucesso na atribuição de um role a um utilizador");
                })
                .addOnFailureListener(e -> {
                    Log.e("ROLES", "Erro na atribuição de um role a um utilizador: " + e.getMessage());
                });
    }

    public void setupUserPoints(FirebaseUser user){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("points", 0);
        db.collection("users")
                .document(user.getUid())
                .update(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d("POINTS", "Setup dos pontos com sucesso");
                })
                .addOnFailureListener(e -> {
                    Log.e("POINTS", "Erro na atribuição de um role a um utilizador: " + e.getMessage());
                });

    }
}
