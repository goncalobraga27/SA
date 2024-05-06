package com.example.saapp.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.saapp.BuildConfig;
import com.example.saapp.CalculaDistancia;
import com.example.saapp.MainActivity;
import com.example.saapp.R;
import com.example.saapp.databinding.FragmentHomeBinding;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    public final static int INITIAL_CAMERA_ZOOM = 15;
    public final static List<Place.Field> PLACES_FIELDS = Arrays.asList(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.PHOTO_METADATAS,
            Place.Field.RATING);
    private FragmentHomeBinding binding;
    private GoogleMap googleMap;
    private Marker currentMarker;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY);
        }

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(PLACES_FIELDS);
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    if (place.getLatLng() != null) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), INITIAL_CAMERA_ZOOM));

                        if (currentMarker != null) {
                            currentMarker.remove();
                        }

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(place.getLatLng());
                        markerOptions.title(place.getName());
                        currentMarker = googleMap.addMarker(markerOptions);

                        showBottomSheetPlaceDetails(place);
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    Log.d("PlaceError", "Error: " + status.getStatusMessage());
                }
            });
        }
    }

    private void showBottomSheetPlaceDetails(Place place) {


        final Dialog bottomSheetDialog = new Dialog(requireContext());
        bottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_place_details);

        TextView placeNameTextView = bottomSheetDialog.findViewById(R.id.place_name);
        TextView placeAddressTextView = bottomSheetDialog.findViewById(R.id.place_address);
        TextView placeRatingTextView = bottomSheetDialog.findViewById(R.id.place_rating);
        ImageView placePhotoImageView = bottomSheetDialog.findViewById(R.id.place_photo);

        placeNameTextView.setText(place.getName());
        placeAddressTextView.setText(place.getAddress());
        placeRatingTextView.setText(place.getRating() != null ? place.getRating().toString() : "No rating");

        if (place.getPhotoMetadatas() != null && !place.getPhotoMetadatas().isEmpty()) {
            PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(500)
                    .build();

            PlacesClient placesClient = Places.createClient(requireContext());
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                placePhotoImageView.setImageBitmap(bitmap);
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.e("PlaceError", "Place not found: " + apiException.getStatusCode());
                }
            });
        }

        bottomSheetDialog.findViewById(R.id.cancelButton).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
        Window window = bottomSheetDialog.getWindow();
        if (window!= null){
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.getAttributes().windowAnimations = R.style.DialogAnimation;
            window.setGravity(Gravity.BOTTOM);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setAllGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.setPadding(0, binding.cardView.getHeight() + 32, 0, 0);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);

            LocationServices.getFusedLocationProviderClient(requireActivity()).getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, INITIAL_CAMERA_ZOOM));
                        }
                    });
            putCheckPointsInMap();
        }
    }

    private void putCheckPointsInMap(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("checkpoints")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        try {
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");
                            String name = document.getString("nome");

                            if (latitude != null && longitude != null && name != null) {
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(new LatLng(latitude, longitude));
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                                markerOptions.title(name);
                                googleMap.addMarker(markerOptions);
                            } else {
                                Log.e("AddMarkerError", "One or more fields are null in the document: " + document.getId());
                            }
                        } catch (Exception e) {
                            Log.e("AddMarkerError", "Error parsing document: " + document.getId(), e);
                        }
                    }
                } else {
                    Log.e("AddMarkerError", "Error getting checkpoints: " + task.getException());
                }
            });
    }

}