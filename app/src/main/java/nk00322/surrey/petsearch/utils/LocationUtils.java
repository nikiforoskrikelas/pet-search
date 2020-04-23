package nk00322.surrey.petsearch.utils;

import android.content.Context;
import android.content.Intent;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;

public class LocationUtils {
    public final static int AUTOCOMPLETE_REQUEST_CODE = 1;
    public final static String API_KEY = "AIzaSyBoVHKsY1l_2v73jyL75-czkFNI_mpxmVY";
    private static final String TAG = "FirebaseUtils";
    public final static List<Place.Field> PLACE_FIELDS = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,Place.Field.ADDRESS);

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
}
