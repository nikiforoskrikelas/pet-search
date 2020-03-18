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

import static nk00322.surrey.petsearch.utils.GeneralUtils.API_KEY;

public class LocationUtils {

    public final static int AUTOCOMPLETE_REQUEST_CODE = 1;


    /**
     * Launches intent for location autocomplete
     */
    public static Intent getLocationAutoCompleteIntent(@NonNull Context context) {
        Places.initialize(context, API_KEY);
        // Create a new Places client instance

        // Set the fields to specify which types of place data to return after the user has made a selection.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

        // Start the autocomplete intent.
        return new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(context);
    }
}