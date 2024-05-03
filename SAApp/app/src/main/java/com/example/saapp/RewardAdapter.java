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

public class RewardAdapter extends ArrayAdapter<Map<String, Object>> {
    private Context mContext;
    private List<Map<String, Object>> mRewardList;

    public RewardAdapter(Context context, List<Map<String, Object>> rewardList){
        super(context, 0, rewardList);
        mContext = context;
        mRewardList = rewardList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.reward_list_item, parent, false);
        }

        // Obtenha o checkpoint atual
        Map<String, Object> reward = mRewardList.get(position);

        // Configure as views com os dados do checkpoint
        TextView rewardName = listItem.findViewById(R.id.rewardName);
        TextView rewardDescription = listItem.findViewById(R.id.rewardDescription);
        TextView rewardPartner = listItem.findViewById(R.id.rewardPartner);
        TextView rewardPoints = listItem.findViewById(R.id.rewardPoints);
        Button claimRewardButton = listItem.findViewById(R.id.claimRewardButton);

        // Extraia os valores do checkpoint e defina nos TextViews
        rewardName.setText(String.format("Name: %s", reward.get("name")));
        rewardDescription.setText(String.format("Description: %s", reward.get("description")));
        rewardPartner.setText(String.format("Partner: %s", reward.get("partner")));
        rewardPoints.setText(String.format("Points: %s", reward.get("points")));

        claimRewardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRewardToUserList(reward);
            }

            private void addRewardToUserList(Map<String, Object> reward) {
                // Verifica se o usuário está logado
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    // Obtém a referência do documento do usuário no Firestore
                    DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid());
                    long rewardPoints = (long) reward.get("points");
                    // Atualiza a lista de recompensas do usuário no Firestore
                    userRef.update("rewardList", FieldValue.arrayUnion(reward),"points",FieldValue.increment(-rewardPoints))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Exibe uma mensagem de sucesso
                                Toast.makeText(getContext(), "Recompensa reclamada com sucesso!", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Exibe uma mensagem de erro em caso de falha
                                Log.e("Erro", "Erro ao adicionar recompensa à lista do usuário: " + e.getMessage());
                                Toast.makeText(getContext(), "Erro ao reclamar recompensa. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
                            }
                        });
                }
            }
        });


        return listItem;
    }

}
