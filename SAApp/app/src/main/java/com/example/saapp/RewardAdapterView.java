package com.example.saapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardAdapterView extends ArrayAdapter<Map<String, Object>> {
    private Context mContext;
    private List<Map<String, Object>> mRewardList;

    public RewardAdapterView(Context context, List<Map<String, Object>> rewardList) {
        super(context, 0, rewardList);
        mContext = context;
        mRewardList = rewardList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.reward_list_user, parent, false);
        }

        // Obtenha o checkpoint atual
        Map<String, Object> reward = mRewardList.get(position);

        // Configure as views com os dados do checkpoint
        TextView rewardName = listItem.findViewById(R.id.rewardName);
        TextView rewardDescription = listItem.findViewById(R.id.rewardDescription);
        TextView rewardPartner = listItem.findViewById(R.id.rewardPartner);
        TextView rewardPoints = listItem.findViewById(R.id.rewardPoints);

        // Extraia os valores do checkpoint e defina nos TextViews
        rewardName.setText(String.format("Name: %s", reward.get("name")));
        rewardDescription.setText(String.format("Description: %s", reward.get("description")));
        rewardPartner.setText(String.format("Partner: %s", reward.get("partner")));
        rewardPoints.setText(String.format("Points: %s", reward.get("points")));


        return listItem;
    }

}