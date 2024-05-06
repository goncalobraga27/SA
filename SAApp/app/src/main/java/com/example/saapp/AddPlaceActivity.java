package com.example.saapp;

import static com.example.saapp.ui.home.HomeFragment.INITIAL_CAMERA_ZOOM;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.saapp.ui.home.HomeFragment;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPlaceActivity extends AppCompatActivity {
    public final static List<Place.Field> PLACES_FIELDS = Arrays.asList(
            Place.Field.ID,
            Place.Field.LAT_LNG,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.PHOTO_METADATAS);
    public Place foundPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        Button buttonAddPlace = findViewById(R.id.buttonAddPlace);
        EditText editTextPoint = findViewById(R.id.editTextPoints);
        EditText editTextPartner = findViewById(R.id.editPartnerText);

        if (!Places.isInitialized()) {
            Places.initialize(this, BuildConfig.MAPS_API_KEY);
        }

        foundPlace = null;

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(PLACES_FIELDS);
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    foundPlace = place;
                }

                @Override
                public void onError(@NonNull Status status) {
                    Log.d("PlaceError", "Error: " + status.getStatusMessage());
                }
            });
        }

        buttonAddPlace.setOnClickListener(view -> {
            String pointsStr = editTextPoint.getText().toString();
            String partner = editTextPartner.getText().toString();

            if (foundPlace != null) {
                if (!pointsStr.isEmpty()) {
                    int points = Integer.parseInt(pointsStr);
                    if (!partner.isEmpty()) {
                        storePlaceInDB(foundPlace, points, partner);
                        finish();
                    } else {
                        editTextPartner.setError("Partner not defined");
                    }
                } else {
                    editTextPoint.setError("Points not defined");
                }
            } else {
                buttonAddPlace.setError("Place not selected");
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

    public void storePlaceInDB (Place place, int points, String partner){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        LatLng latLng = place.getLatLng();
        if (latLng == null) {
            return;
        }

        double latitude = latLng.latitude;
        double longitude = latLng.longitude;

        // Create map to store data in firestore
        Map<String, Object> data = new HashMap<>();
        data.put("nome", place.getName());
        data.put("latitude", latitude);
        data.put("longitude", longitude);
        data.put("points",points);
        data.put("visitedBy",new ArrayList<>());
        data.put("partner",partner);

        db.collection("checkpoints").add(data)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Added place: " + documentReference.getId());
                    Toast.makeText(AddPlaceActivity.this, "Place successfully added!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Place not added!", e);
                    Toast.makeText(AddPlaceActivity.this, "Error adding the place", Toast.LENGTH_SHORT).show();
                });
    }

}