package com.example.saapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.saapp.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private static final String CHANNEL_ID = "PlacesChannel";
    private static final String CHANNEL_NAME = "Places Channel";
    private static final int NOTIFICATION_ID = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private String previousNotificationsName = "";
    public final static int REQUEST_LOCATION_UPDATES_INTERVAL = 30000;

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
                    Toast.makeText(this, "Functionality limited due to no access to location", Toast.LENGTH_LONG);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.saapp.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() == null) {
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                finish();
            }
        };

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
                        calculatePoints(user, lastKnownLocation);
                    }
                }
            }
        };

        LocationManager locationManager = getSystemService(LocationManager.class);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        } else {
            locationPermissionRequest.launch(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }

        createNotificationChannel();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void createNotificationChannel() {
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
        channel.setDescription("Places Channel");
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        if (!notificationManager.areNotificationsEnabled()) {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
        }
    }

    public void showNotification(String title, String name) {
        if (previousNotificationsName.equals(name)) {
            return;
        }

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        previousNotificationsName = name;

        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentTitle(title)
                .setContentText("You are near " + name + "!")
                .setAutoCancel(true);

        Notification notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID,notification);
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

    private void calculatePoints(FirebaseUser user,Location location){
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
                            String name = document.getString("nome");

                            if (latitude != null && longitude != null && points!= null) {
                                double distance = CalculaDistancia.calcularDistanciaEntrePontos(location.getLatitude(), location.getLongitude(), latitude, longitude);
                                List<String> visitedBy = (List<String>) document.get("visitedBy");
                                if (visitedBy != null && !visitedBy.contains(user.getUid())) {
                                    if (distance < 0.1) {
                                        increaseUserPoints(user, points, idCheckpoint);
                                        showPointsEarnedDialog(points);
                                    }
                                    if (distance < 2) {
                                        Log.i("LocationsUtils", "You are near " + name + "!");
                                        showNotification("Near Place", name);
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

    private void showPointsEarnedDialog(double pointsEarned) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Congratulations!");
        builder.setMessage("You earned " + pointsEarned + " points!");
        builder.setCancelable(false);
        builder.setPositiveButton("CLOSE", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
                .update("visitedBy", FieldValue.arrayUnion(user.getUid()))
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

    @Override
    public void onStart(){
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

}