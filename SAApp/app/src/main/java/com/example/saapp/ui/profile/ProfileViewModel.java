package com.example.saapp.ui.profile;

import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileViewModel extends ViewModel {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final MutableLiveData<String> userEmail = new MutableLiveData<>();
    private final MutableLiveData<Integer> userPoints = new MutableLiveData<>();
    private final MutableLiveData<String> passwordResetEmailStatus = new MutableLiveData<>();

    public ProfileViewModel() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        userEmail.setValue(currentUser != null ? currentUser.getEmail() : null);
    }

    public LiveData<String> getUserEmail() {
        return userEmail;
    }

    public LiveData<Integer> getUserPoints() {
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

}