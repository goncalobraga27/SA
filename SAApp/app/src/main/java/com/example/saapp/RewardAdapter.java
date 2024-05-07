package com.example.saapp;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
        rewardName.setText(String.format("%s", reward.get("name")));
        rewardDescription.setText(String.format("%s", reward.get("description")));
        rewardPartner.setText(String.format("%s", reward.get("partner")));
        rewardPoints.setText(String.format("%s", reward.get("points")));

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
                    userPoints(currentUser, new OnPointsReceivedListener() {
                        @Override
                        public void onPointsReceived(int points) {
                            if (points >= rewardPoints) {
                                List<String> ownedByList = (List<String>) reward.get("ownedBy");
                                ownedByList.add(currentUser.getUid());
                                reward.put("ownedBy", ownedByList);
                                // Atualiza a lista de recompensas do usuário no Firestore
                                userRef.update("rewardList", FieldValue.arrayUnion(reward), "points", FieldValue.increment(-rewardPoints))
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
                                updateRewards(reward);
                            }
                            else{
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Points!");
                                builder.setMessage("User points are not enough.");
                                builder.setCancelable(false);
                                builder.setPositiveButton("CLOSE", (dialog, which) -> {
                                    dialog.dismiss();
                                });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.d("RewardAdapter","Erro no RewardAdapter");
                        }
                    });

                }
            }
        });


        return listItem;
    }

    public void updateRewards(Map<String, Object> reward){
        CollectionReference rewardsRef = FirebaseFirestore.getInstance().collection("rewards");
        rewardsRef.whereEqualTo("name", reward.get("name"))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Obtém o ID do documento
                                String idDocumento = document.getId();

                                // Atualiza os campos desejados do documento
                                rewardsRef.document(idDocumento)
                                        .update(reward)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> updateTask) {
                                                if (updateTask.isSuccessful()) {
                                                    Log.d("RewardAdapter", "Atualização realizada com sucesso");
                                                } else {
                                                    Log.e("RewardAdapter", "Erro ao atualizar documento", updateTask.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.e("RewardAdapter", "Erro ao buscar documento", task.getException());
                        }
                    }
                });
    }
    public void userPoints(FirebaseUser user, OnPointsReceivedListener listener) {
        // Obtém uma instância do Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Obtém a referência para o documento do usuário
        DocumentReference userRef = db.collection("users").document(user.getUid());

        // Busca o documento do usuário de forma assíncrona
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // O documento do usuário existe, agora podemos obter os pontos
                    Long pontos = document.getLong("points");
                    if (pontos != null) {
                        // Retorna os pontos do usuário através do listener
                        listener.onPointsReceived(pontos.intValue());
                    } else {
                        // Se o campo "points" não existir ou for nulo
                        listener.onError(new IllegalStateException("O campo 'points' não foi encontrado ou é nulo."));
                    }
                } else {
                    // Se o documento do usuário não existir
                    listener.onError(new IllegalStateException("O documento do usuário não foi encontrado."));
                }
            } else {
                // Se ocorrer um erro ao buscar o documento
                listener.onError(task.getException());
            }
        });
    }


    public interface OnPointsReceivedListener {
        void onPointsReceived(int points);
        void onError(Exception e);
    }

}
