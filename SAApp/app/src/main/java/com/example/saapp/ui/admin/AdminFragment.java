package com.example.saapp.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.saapp.BuildConfig;
import com.example.saapp.R;
import com.example.saapp.databinding.FragmentAdminBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.Objects;

public class AdminFragment extends Fragment {
    private FragmentAdminBinding binding;
    private PlacesClient placesClient;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY);
        }
        placesClient = Places.createClient(requireContext());
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AdminViewModel adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);
        adminViewModel.setPlacesClient(placesClient);

        binding = FragmentAdminBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        EditText editTextLocation = root.findViewById(R.id.editTextLocation);

        binding.buttonAddPlace.setOnClickListener(v -> {
            String location = editTextLocation.getText().toString();
            adminViewModel.addPlace(location);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
