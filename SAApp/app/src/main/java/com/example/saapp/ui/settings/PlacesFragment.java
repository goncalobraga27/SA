package com.example.saapp.ui.settings;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.saapp.CheckPointAdapter;
import com.example.saapp.R;
import com.example.saapp.databinding.FragmentPlacesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.saapp.AddPlaceActivity;
import com.google.firebase.firestore.Query;



import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PlacesFragment extends Fragment {

    private FragmentPlacesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PlacesViewModel placesViewModel = new ViewModelProvider(this).get(PlacesViewModel.class);

        binding = FragmentPlacesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button buttonAddNewPlace = binding.buttonAddNewPlace;
        ListView checkpointListView = binding.checkpointListView;

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

        CollectionReference checkpointsRef = db.collection("checkpoints");
        checkpointsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Map<String, Object>> checkpointsList = new ArrayList<>();

            for (DocumentSnapshot document : queryDocumentSnapshots) {
                Map<String, Object> checkpoint = document.getData();
                checkpointsList.add(checkpoint);
            }
            Location lastKnownLocation = getLatestUserLocation(currentUser);
            Log.e("DEBUG - PLACES LIST", String.valueOf(lastKnownLocation));
            Log.e("DEBUG - CHECKPOINTS", checkpointsList.toString());
            orderCheckPointList(checkpointsList,lastKnownLocation);
            CheckPointAdapter adapter = new CheckPointAdapter(getContext(), checkpointsList);
            checkpointListView.setAdapter(adapter);
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

    public void orderCheckPointList(List<Map<String, Object>> checkpointsList, Location lastKnownLocation) {
        if (lastKnownLocation == null) {
            Log.e("OrderCheckpoints", "Last known location is null");
            return;
        }
        checkpointsList.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> checkpoint1, Map<String, Object> checkpoint2) {
                double checkpoint1Latitude = (double) checkpoint1.get("latitude");
                double checkpoint1Longitude = (double) checkpoint1.get("longitude");


                double checkpoint2Latitude = (double) checkpoint2.get("latitude");
                double checkpoint2Longitude = (double) checkpoint2.get("longitude");


                float distanceToCheckpoint1 = lastKnownLocation.distanceTo(createLocation(checkpoint1Latitude, checkpoint1Longitude));

                float distanceToCheckpoint2 = lastKnownLocation.distanceTo(createLocation(checkpoint2Latitude, checkpoint2Longitude));

                return Float.compare(distanceToCheckpoint1, distanceToCheckpoint2);
            }
        });
    }

    // Método auxiliar para criar uma instância de Location a partir de latitude e longitude
    private Location createLocation(double latitude, double longitude) {
        Location location = new Location("DummyProvider");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }


    public Location getLatestUserLocation(FirebaseUser user){
        Location res = new Location("Last user location");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
            .document(user.getUid())
            .collection("user_location")
            .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING) // Ordenar pelo ID do documento
            .limit(1) // Apenas precisamos do último documento
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                    double latitude = document.getDouble("latitude");
                    double longitude = document.getDouble("longitude");

                    res.setLatitude(latitude);
                    res.setLongitude(longitude);

                    Log.d("GET last user location", "Latitude: " + latitude + ", Longitude: " + longitude);
                } else {
                    Log.d("GET last user location", "No location data found for this user");
                }
            })
            .addOnFailureListener(e -> {
                Log.e("GET last user location", "Error getting last location: " + e.getMessage());
            });
        return res;
    }
}