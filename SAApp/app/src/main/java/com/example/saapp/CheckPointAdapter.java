package com.example.saapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

public class CheckPointAdapter extends ArrayAdapter<Map<String, Object>> {
    private Context mContext;
    private List<Map<String, Object>> mCheckpointsList;

    public CheckPointAdapter(Context context, List<Map<String, Object>> checkpointsList){
        super(context, 0, checkpointsList);
        mContext = context;
        mCheckpointsList = checkpointsList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.place_list_item, parent, false);
        }

        // Obtenha o checkpoint atual
        Map<String, Object> checkpoint = mCheckpointsList.get(position);

        // Configure as views com os dados do checkpoint
        TextView nomeTextView = listItem.findViewById(R.id.nomeTextView);
        TextView latitudeTextView = listItem.findViewById(R.id.latitudeTextView);
        TextView longitudeTextView = listItem.findViewById(R.id.longitudeTextView);
        TextView pontosTextView = listItem.findViewById(R.id.pontosTextView);

        // Extraia os valores do checkpoint e defina nos TextViews
        nomeTextView.setText(String.format("%s", checkpoint.get("nome")));
        latitudeTextView.setText(String.format("%s", checkpoint.get("latitude")));
        longitudeTextView.setText(String.format("%s", checkpoint.get("longitude")));
        pontosTextView.setText(String.format("%s", checkpoint.get("points")));

        return listItem;
    }

}
