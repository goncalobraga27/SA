package com.example.saapp.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.saapp.R;
import com.example.saapp.databinding.FragmentHomeBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    public final static int INITIAL_CAMERA_ZOOM = 15;
    public final static int REQUEST_LOCATION_UPDATES_INTERVAL = 1000 * 10;
    private FragmentHomeBinding binding;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
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

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        locationRequest = new LocationRequest.Builder(REQUEST_LOCATION_UPDATES_INTERVAL).build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location lastKnownLocation = locationResult.getLastLocation();
                if (lastKnownLocation != null) {
                    Log.i("LocationUpdate", "Latitude: " + lastKnownLocation.getLatitude() + ", Longitude: " + lastKnownLocation.getLongitude());
                }
            }
        };

        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        return root;
    }


    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
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
}