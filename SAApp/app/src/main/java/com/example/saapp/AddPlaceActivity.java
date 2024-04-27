package com.example.saapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

public class AddPlaceActivity extends AppCompatActivity {
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        EditText editTextLocation = findViewById(R.id.editTextLocation);
        Button buttonAddPlace = findViewById(R.id.buttonAddPlace);

        if (!Places.isInitialized()) {
            Places.initialize(this, BuildConfig.MAPS_API_KEY);
        }

        placesClient = Places.createClient(this);

        buttonAddPlace.setOnClickListener(view -> {
            String location = editTextLocation.getText().toString();

            if (!location.isEmpty()) {
                Place place = Place.builder()
                        .setName(editTextLocation.getText().toString())
                        .build();

                if (place.getId() != null) {

                    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
                    FetchPlaceRequest request = FetchPlaceRequest.builder(place.getId(), placeFields).build();

                    placesClient.fetchPlace(request).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FetchPlaceResponse response = task.getResult();
                            Place addedPlace = response.getPlace();
                            Toast.makeText(AddPlaceActivity.this, "Place added successfully: " + addedPlace, Toast.LENGTH_SHORT).show();
                        } else {
                            Exception exception = task.getException();
                            String errorMessage = exception != null ? exception.getMessage() : "Unknown error occurred";
                            Toast.makeText(AddPlaceActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    editTextLocation.setError("Cannot fetch place " + location);
                }
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

}