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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.saapp.ui.home.HomeFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.MarkerOptions;
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


import java.util.Arrays;
import java.util.List;

public class AddPlaceActivity extends AppCompatActivity implements HomeFragment.showLocationPinListener {
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        EditText editTextLocation = findViewById(R.id.editTextLocation);
        Button buttonAddPlace = findViewById(R.id.buttonAddPlace);
        TextView textViewPlaceDetails =findViewById(R.id.textViewPlaceDetails);
        Button buttonViewMap = findViewById(R.id.buttonViewMap);

        if (!Places.isInitialized()) {
            Places.initialize(this, BuildConfig.MAPS_API_KEY);
        }

        placesClient = Places.createClient(this);

        buttonAddPlace.setOnClickListener(view -> {
            String location = editTextLocation.getText().toString();

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
                                showLocationPinListener(place);
                                StringBuilder detailsBuilder = new StringBuilder();
                                detailsBuilder.append("Place Coords: ").append(place.getLatLng()).append("\n");
                                textViewPlaceDetails.setVisibility(View.VISIBLE);
                                textViewPlaceDetails.setText(detailsBuilder.toString());
                                buttonViewMap.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // Inicie a nova atividade que contém o fragmento do mapa
                                        Intent intent = new Intent(AddPlaceActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                });
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

    @Override
    public void showLocationPinListener(Place place) {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_home);
        if (homeFragment != null) {
            homeFragment.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), INITIAL_CAMERA_ZOOM));

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(place.getLatLng());
            markerOptions.title(place.getName());
            homeFragment.googleMap.addMarker(markerOptions);
        }
    }

}