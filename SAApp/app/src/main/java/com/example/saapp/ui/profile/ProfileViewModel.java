package com.example.saapp.ui.profile;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class ProfileViewModel extends ViewModel {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final MutableLiveData<String> userEmail = new MutableLiveData<>();
    private final MutableLiveData<Double> userPoints = new MutableLiveData<>();
    private final MutableLiveData<String> passwordResetEmailStatus = new MutableLiveData<>();
    private final MutableLiveData<String> userRole= new MutableLiveData<>();

    private final MutableLiveData<List<Map<String,Object>>> userRewards= new MutableLiveData<>();

    public MutableLiveData<List<Map<String, Object>>> getUserRewards() {
        return userRewards;
    }

    public ProfileViewModel() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        userEmail.setValue(currentUser != null ? currentUser.getEmail() : null);
    }

    public LiveData<String> getUserEmail() {
        return userEmail;
    }

    public LiveData<Double> getUserPoints() {
        return userPoints;
    }

    public LiveData<String> getPasswordResetEmailStatus() {
        return passwordResetEmailStatus;
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    public void sendPasswordResetEmail() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            firebaseAuth.sendPasswordResetEmail(user.getEmail())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            passwordResetEmailStatus.setValue("Reset email sent to " + user.getEmail());
                        } else {
                            passwordResetEmailStatus.setValue("Failed to send reset email");
                        }
                    });
        } else {
            passwordResetEmailStatus.setValue("There is no email available for reset");
        }
    }

    public LiveData<String> getUserRole() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(currentUser.getUid());

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String role = documentSnapshot.getString("role");
                    userRole.setValue(role);
                    Log.d("UserRole", "O role do utilizador é: " + role);
                } else {
                    Log.d("UserRole", "O role do utilizador não existe");
                }
            }).addOnFailureListener(e -> {
                Log.e("UserRole", "Erro ao obter o role do utilizador: " + e.getMessage());
            });
        }
        return userRole;
    }

    public LiveData<Double> getUserPontos() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(currentUser.getUid());

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    double pontos = documentSnapshot.getDouble("points");
                    userPoints.setValue(pontos);
                    Log.d("UserPoints", "Os pontos do utilizador são: " + pontos);
                } else {
                    Log.d("UserPoints", "Não existem pontos do utilizador");
                }
            }).addOnFailureListener(e -> {
                Log.e("UserPoints", "Erro ao obter os pontos do utilizador: " + e.getMessage());
            });
        }
        return userPoints;
    }

    public LiveData<List<Map<String,Object>>> getUserRewardList() {
        // Verifica se o usuário está logado
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Obtém a referência do documento do usuário no Firestore
            DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid());

            // Obtém a lista de recompensas do usuário
            userRef.get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            // Verifica se o documento do usuário existe
                            if (documentSnapshot.exists()) {
                                // Recupera a lista de recompensas do usuário
                                List<Map<String, Object>> rewardList = (List<Map<String, Object>>) documentSnapshot.get("rewardList");
                                userRewards.setValue(rewardList);
                            } else {
                                // Se o documento do usuário não existir, trata o caso adequadamente
                                Log.d("UserNotFound", "Usuário não encontrado no Firestore.");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Trata falhas na recuperação da lista de recompensas do usuário
                            Log.e("GetUserRewardListError", "Erro ao obter a lista de recompensas do usuário: " + e.getMessage());
                        }
                    });
        }
        return userRewards;
    }


}