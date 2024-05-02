package com.example.saapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddRewardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rewards);

        EditText editTextRewardName = findViewById(R.id.nameReward);
        EditText editTextRewardDescription = findViewById(R.id.descriptionReward);
        EditText editTextRewardPoints = findViewById(R.id.pointsReward);
        EditText editTextRewardPartner = findViewById(R.id.partnerReward);
        Button buttonAddReward = findViewById(R.id.buttonAddReward);

        buttonAddReward.setOnClickListener(v -> {
            String rewardName = editTextRewardName.getText().toString();
            String rewardDescription = editTextRewardDescription.getText().toString();
            String rewardPoints = editTextRewardPoints.getText().toString();
            String rewardPartner = editTextRewardPartner.getText().toString();

            if (rewardName.isEmpty()) {
                editTextRewardName.setError("Reward name is required");
            }
            else if (rewardPoints.isEmpty()) {
                editTextRewardPoints.setError("Reward points is required");
            }
            else {
                // Add reward to database
                int points = Integer.parseInt(rewardPoints);
                storeReward(rewardName, rewardDescription, points, rewardPartner);
            }
        });
    }

    void storeReward(String rewardName, String rewardDescription, int rewardPoints, String rewardPartner) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> reward = new HashMap<>();
        reward.put("name", rewardName);
        reward.put("description", rewardDescription);
        reward.put("points", rewardPoints);
        reward.put("partner", rewardPartner);
        db.collection("rewards")
                .add(reward)
                .addOnSuccessListener(documentReference -> {
                    // Reward added successfully
                    Log.d("AddRewardActivity", "DocumentSnapshot added with ID: " + documentReference.getId());
                    Toast.makeText(this, "Reward added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Error adding reward
                    Log.e("AddRewardActivity", "Error adding document", e);
                    Toast.makeText(this, "Error adding reward", Toast.LENGTH_SHORT).show();
                });

    }
}
