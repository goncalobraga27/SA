package com.example.saapp.ui.admin;


import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;


import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;


import java.util.Arrays;
import java.util.List;

public class AdminViewModel extends ViewModel {
    private PlacesClient placesClient;

    public AdminViewModel() {
    }
    public void setPlacesClient(PlacesClient placesClient) {
        this.placesClient = placesClient;
    }

    public void addPlace(String location) {
        Place place = Place.builder()
                .setName(location)
                .build();

        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        FetchPlaceRequest request = FetchPlaceRequest.builder(place.getId(), placeFields).build();
        placesClient.fetchPlace(request).addOnCompleteListener(new OnCompleteListener<FetchPlaceResponse>() {
            @Override
            public void onComplete(Task<FetchPlaceResponse> task) {
                if (task.isSuccessful()) {
                    FetchPlaceResponse response = task.getResult();
                    Place addedPlace = response.getPlace();
                    System.out.println("Local adicionado com sucesso: " + addedPlace);
                } else {
                    Exception exception = task.getException();
                    System.out.println("Erro ao adicionar o local: " + exception);
                }
            }
        });

    }
}
