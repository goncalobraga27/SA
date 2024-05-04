package com.example.saapp.ui.rewards;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saapp.R;
import com.example.saapp.RewardAdapter;

import java.util.List;
import java.util.Map;

public class RewardsListFragment extends Fragment {

    private List<Map<String,Object>> rewardList;
    private RecyclerView recyclerView;
    private RewardAdapter adapter;

    public void setArguments(Bundle args) {
    }

    @SuppressLint("WrongViewCast")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_rewards, container, false);
        recyclerView = root.findViewById(R.id.rewardsListView);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Configurar o RecyclerView e o adaptador
        adapter = new RewardAdapter(getContext(), rewardList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //recyclerView.setAdapter(adapter);
    }

}
