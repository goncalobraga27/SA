package com.example.saapp.ui.rewards;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.saapp.AddRewardActivity;
import com.example.saapp.R;
import com.example.saapp.RewardAdapter;
import com.example.saapp.databinding.FragmentRewardsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class RewardsFragment extends Fragment {

    private FragmentRewardsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {

            binding = FragmentRewardsBinding.inflate(inflater, container, false);
            View root = binding.getRoot();

            FloatingActionButton buttonAddNewReward = binding.buttonAddNewReward;
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
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            getPartnersByUser(user)
                .addOnSuccessListener(partners -> {
                    if (!partners.isEmpty()) {
                        CollectionReference rewardsRef = db.collection("rewards");
                        rewardsRef.whereIn("partner", partners)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    List<Map<String, Object>> rewardsList = new ArrayList<>();
                                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                                        Map<String, Object> reward = document.getData();
                                        if (reward != null) {
                                            List<String> ownedBy = (List<String>) reward.get("ownedBy");
                                            if (ownedBy == null || !ownedBy.contains(user.getUid())) {
                                                rewardsList.add(reward);
                                            }
                                        }
                                    }

                                    RewardAdapter adapter = new RewardAdapter(getContext(), rewardsList);
                                    rewardsListView.setAdapter(adapter);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("PlacesFragment", "Erro ao obter as recompensas filtradas: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Trate falhas ao buscar os parceiros do usuário
                    Log.e("Erro", "Erro na obtenção dos partners: " + e.getMessage());
                });


            return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public Task<List<String>> getPartnersByUser(FirebaseUser user) {
        CollectionReference checkpointsRef = FirebaseFirestore.getInstance().collection("checkpoints");

        return checkpointsRef.get()
                .continueWith(task -> {
                    List<String> partners = new ArrayList<>();
                    if (task.isSuccessful()) {
                        // Operação bem-sucedida, processa os documentos
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> checkpoint = document.getData();
                            List<String> ids = (List<String>) checkpoint.get("visitedBy");
                            String partner = (String) checkpoint.get("partner");
                            if (ids != null && ids.contains(user.getUid())) {
                                partners.add(partner);
                            }
                        }
                        return partners;
                    } else {
                        return new ArrayList<>();
                    }
                });
    }



}