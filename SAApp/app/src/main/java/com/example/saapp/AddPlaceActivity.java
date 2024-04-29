package com.example.saapp;

import static com.example.saapp.ui.home.HomeFragment.INITIAL_CAMERA_ZOOM;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.saapp.ui.home.HomeFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPlaceActivity extends AppCompatActivity {
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        EditText editTextLocation = findViewById(R.id.editTextLocation);
        Button buttonAddPlace = findViewById(R.id.buttonAddPlace);
        TextView textViewPlaceDetails =findViewById(R.id.textViewPlaceDetails);
        EditText editTextPoint = findViewById(R.id.editTextPoints);

        if (!Places.isInitialized()) {
            Places.initialize(this, BuildConfig.MAPS_API_KEY);
        }

        placesClient = Places.createClient(this);

        buttonAddPlace.setOnClickListener(view -> {
            String location = editTextLocation.getText().toString();
            int points = Integer.parseInt(editTextPoint.getText().toString());

            if (!location.isEmpty()) {
                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
                AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        .setSessionToken(token)
                        .setQuery(location)
                        .build();

                placesClient.findAutocompletePredictions(request).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FindAutocompletePredictionsResponse response = task.getResult();
                        if (response != null && !response.getAutocompletePredictions().isEmpty()) {
                            AutocompletePrediction prediction = response.getAutocompletePredictions().get(0);
                            String placeId = prediction.getPlaceId();
                            // Cria um FetchPlaceRequest com o ID do lugar e os campos desejados
                            FetchPlaceRequest fetchRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                            // Enviar a solicitação de busca de lugar para obter informações detalhadas sobre o lugar
                            placesClient.fetchPlace(fetchRequest).addOnSuccessListener(fetchResponse -> {
                                Place place = fetchResponse.getPlace();
                                storePlaceInDB(place,points);
                                StringBuilder detailsBuilder = new StringBuilder();
                                detailsBuilder.append("Place Coords: ").append(place.getLatLng()).append("\n");
                                textViewPlaceDetails.setVisibility(View.VISIBLE);
                                textViewPlaceDetails.setText(detailsBuilder.toString());
                            }).addOnFailureListener(exception -> {
                                editTextLocation.setError("Cannot obtain location");
                            });
                        } else {
                            editTextLocation.setError("Location does not exist");
                        }
                    } else {
                        Exception exception = task.getException();
                        String errorMessage = exception != null ? exception.getMessage() : "Unknown error occurred";
                        editTextLocation.setError("Error: " + errorMessage);
                    }
                });
            } else {
                editTextLocation.setError("Location cannot be empty");
            }
        });


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void storePlaceInDB (Place place,int points){
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        LatLng latLng = place.getLatLng();
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;

        // Crie um mapa para armazenar os dados no Firestore
        Map<String, Object> data = new HashMap<>();
        data.put("nome", place.getName());
        data.put("latitude", latitude);
        data.put("longitude", longitude);
        data.put("points",points);

        db.collection("locals").add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("Firestore", "Local adicionado: " + documentReference.getId());
                        Toast.makeText(AddPlaceActivity.this, "Local adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Erro ao adicionar local", e);
                        Toast.makeText(AddPlaceActivity.this, "Erro ao adicionar local", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}