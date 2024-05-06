package com.example.saapp.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
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
    public final static int REQUEST_LOCATION_UPDATES_INTERVAL = 30000;
    public final static List<Place.Field> PLACES_FIELDS = Arrays.asList(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.PHOTO_METADATAS,
            Place.Field.RATING);
    private FragmentHomeBinding binding;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Marker currentMarker;

    private final ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    Log.d("Permissions", "Permissions Granted");
                    startLocationUpdates();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    Log.d("Permissions", "Permissions Granted");
                    startLocationUpdates();
                } else {
                    Log.d("Permissions", "Permissions Denied");
                    Toast.makeText(requireContext(), "Functionality limited due to no access to location", Toast.LENGTH_LONG);
                }
            }
    );


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        locationRequest = new LocationRequest.Builder(REQUEST_LOCATION_UPDATES_INTERVAL).build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location lastKnownLocation = locationResult.getLastLocation();
                if (lastKnownLocation != null) {
                    Log.i("LocationUpdate", "Latitude: " + lastKnownLocation.getLatitude() + ", Longitude: " + lastKnownLocation.getLongitude());
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        storeUserLocation(user, lastKnownLocation.getLongitude(), lastKnownLocation.getLatitude());
                        calculaPontos(user, lastKnownLocation);
                    }
                }
            }
        };

        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

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

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, INITIAL_CAMERA_ZOOM));
                        }
                    });
            putCheckPointsInMap();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void storeUserLocation(FirebaseUser user, double longitude, double latitude) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("latitude", latitude);
        data.put("longitude", longitude);

        db.collection("users")
                .document(user.getUid())
                .collection("user_location")
                .add(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d("LocationUpdate", "Location data saved successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("LocationUpdate", "Error saving location data: " + e.getMessage());
                });
    }

    private void calculaPontos(FirebaseUser user,Location location){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("checkpoints")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    try {
                        Double latitude = document.getDouble("latitude");
                        Double longitude = document.getDouble("longitude");
                        String idCheckpoint = document.getId();
                        Double points = document.getDouble("points");

                        if (latitude != null && longitude != null && points!= null) {
                            double distance = CalculaDistancia.calcularDistanciaEntrePontos(location.getLatitude(), location.getLongitude(), latitude, longitude);
                            List<String> visitedBy = (List<String>) document.get("visitedBy");
                            if (visitedBy != null && !visitedBy.contains(user.getUid())) {
                                if (distance < 0.1) {
                                    increaseUserPoints(user, points, idCheckpoint);
                                    showPointsEarnedDialog(points);
                                }
                            }

                        }
                    } catch (Exception e) {
                        Log.e("LocationsUtils", "Error reading firestore document: " + document.getId(), e);
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e("LocationUtils", "Error getting places: " + e.getMessage());
            });
    }

    public void increaseUserPoints(FirebaseUser user, double points,String idCheckPoint){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(user.getUid())
                .update("points", increment(points))
                .addOnSuccessListener(aVoid -> {
                    Log.d("LocationUtils", "User points increased successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("LocationUtils", "Error increasing user points: " + e.getMessage());
                });
        checkpointDone(user,idCheckPoint);
    }

    public void checkpointDone(FirebaseUser user,String idCheckpoint ){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("checkpoints")
                .document(idCheckpoint)
                .update("visitedBy",FieldValue.arrayUnion(user.getUid()))
                .addOnSuccessListener(aVoid -> {
                    Log.d("SAVE CHECKPOINT STATUS", "Checkpoint done by user!");
                })
                .addOnFailureListener(e -> {
                    Log.e("SAVE CHECKPOINT STATUS", "Error saving checkpoint status: " + e.getMessage());
                });
    }

    private static FieldValue increment(double value) {
        return com.google.firebase.firestore.FieldValue.increment(value);
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

    private void showPointsEarnedDialog(double pointsEarned) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Congratulations!");
        builder.setMessage("You earned " + pointsEarned + " points!");
        builder.setCancelable(false); // Impede que o diálogo seja fechado clicando fora dele
        builder.setPositiveButton("CLOSE", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}