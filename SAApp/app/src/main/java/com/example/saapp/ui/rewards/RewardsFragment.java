package com.example.saapp.ui.rewards;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saapp.AddRewardActivity;
import com.example.saapp.CheckPointAdapter;
import com.example.saapp.R;
import com.example.saapp.RewardAdapter;
import com.example.saapp.databinding.FragmentRewardsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RewardsFragment extends Fragment {

    private FragmentRewardsBinding binding;

public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentRewardsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button buttonAddNewReward = binding.buttonAddNewReward;
        ListView rewardsListView = binding.rewardsListView;

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String role = documentSnapshot.getString("role");
                    if (role != null && role.equals("admin")) {
                        buttonAddNewReward.setVisibility(View.VISIBLE);
                    } else {
                        buttonAddNewReward.setVisibility(View.INVISIBLE);
                    }
                }
            }).addOnFailureListener(e -> Log.e("UserRole", "Error getting user role: " + e.getMessage()));
        }
        
        binding.buttonAddNewReward.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddRewardActivity.class)));

        CollectionReference rewardsRef = db.collection("rewards");
        rewardsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Map<String, Object>> rewardsList = new ArrayList<>();

            for (DocumentSnapshot document : queryDocumentSnapshots) {
                Map<String, Object> reward = document.getData();
                rewardsList.add(reward);
            }
            RewardAdapter adapter = new RewardAdapter(getContext(), rewardsList);
            rewardsListView.setAdapter(adapter);
        }).addOnFailureListener(e -> {
            // Trate falhas ao buscar os checkpoints
            Log.e("PlacesFragment", "Erro ao obter os checkpoints: " + e.getMessage());
        });



        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}