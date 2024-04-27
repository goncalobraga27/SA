package com.example.saapp.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.saapp.databinding.FragmentPlacesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.saapp.AddPlaceActivity;

public class PlacesFragment extends Fragment {

    private FragmentPlacesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PlacesViewModel placesViewModel = new ViewModelProvider(this).get(PlacesViewModel.class);

        binding = FragmentPlacesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button buttonAddNewPlace = binding.buttonAddNewPlace;
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String role = documentSnapshot.getString("role");
                    if (role != null && role.equals("admin")) {
                        buttonAddNewPlace.setVisibility(View.VISIBLE);
                    } else {
                        buttonAddNewPlace.setVisibility(View.INVISIBLE);
                    }
                }
            }).addOnFailureListener(e -> Log.e("UserRole", "Error getting user role: " + e.getMessage()));
        }

        binding.buttonAddNewPlace.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddPlaceActivity.class)));


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}