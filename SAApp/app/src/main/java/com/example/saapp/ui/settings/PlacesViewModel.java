package com.example.saapp.ui.settings;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class PlacesViewModel extends ViewModel {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public PlacesViewModel() {
    }


}