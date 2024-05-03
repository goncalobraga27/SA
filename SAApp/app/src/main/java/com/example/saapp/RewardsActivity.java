package com.example.saapp;



import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saapp.ui.profile.ProfileViewModel;

public class RewardsActivity extends AppCompatActivity {

    private ProfileViewModel profileViewModel;
    private ListView rewardsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        rewardsListView = findViewById(R.id.rewardsView);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        profileViewModel.getUserRewardList().observe(this, rewards -> {
            // Atualize o RecyclerView com as novas recompensas
            RewardAdapterView adapter = new RewardAdapterView(this, rewards);
            rewardsListView.setAdapter(adapter);
        });
    }
}