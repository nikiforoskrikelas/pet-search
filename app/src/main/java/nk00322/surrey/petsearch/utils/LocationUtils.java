package nk00322.surrey.petsearch.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Observable;

public class LocationUtils {
    public final static int AUTOCOMPLETE_REQUEST_CODE = 1;
    public final static String API_KEY = "AIzaSyBoVHKsY1l_2v73jyL75-czkFNI_mpxmVY";
    private static final String TAG = "FirebaseUtils";
    public final static List<Place.Field> PLACE_FIELDS = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

    /**
     * Launches intent for location autocomplete
     */
    public static Intent getLocationAutoCompleteIntent(@NonNull Context context) {
        if (!Places.isInitialized()) {
            Places.initialize(context, API_KEY);
        }
        // Create a new Places client instance

        // Set the fields to specify which types of place data to return after the user has made a selection.

        // Start the autocomplete intent.
        return new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, PLACE_FIELDS).build(context);
    }


    public static Observable<Place> getPlaceFromId(String placeUid, Context context) {
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeUid, PLACE_FIELDS);


        return Observable.create(result -> Places.createClient(context).fetchPlace(request)
                .addOnCompleteListener(response -> {
                    if (response.isSuccessful()) {

                        Log.d(TAG, "Document found");
                        result.onNext(response.getResult().getPlace()); // return user to observable


                    } else {
                        Log.w(TAG, "Error getting document.", response.getException());
                        result.onError(response.getException());
                    }

                }));
    }
}
