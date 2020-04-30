package nk00322.surrey.petsearch.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.example.petsearch.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import nk00322.surrey.petsearch.CustomMapView;
import nk00322.surrey.petsearch.CustomToast;
import nk00322.surrey.petsearch.ToastType;
import nk00322.surrey.petsearch.models.SearchParty;
import nk00322.surrey.petsearch.models.SearchedArea;
import nk00322.surrey.petsearch.models.Sighting;
import nk00322.surrey.petsearch.models.User;
import uk.co.mgbramwell.geofire.android.listeners.SetLocationListener;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static nk00322.surrey.petsearch.utils.FirebaseUtils.getUidFromSearchParty;
import static nk00322.surrey.petsearch.utils.FirebaseUtils.getUserFromId;
import static nk00322.surrey.petsearch.utils.FirebaseUtils.getUserSubscriptions;
import static nk00322.surrey.petsearch.utils.GeneralUtils.checkPermission;
import static nk00322.surrey.petsearch.utils.GeneralUtils.getMarkerIconFromDrawable;
import static nk00322.surrey.petsearch.utils.GeneralUtils.printDate;
import static nk00322.surrey.petsearch.utils.GeneralUtils.sortVertices;
import static nk00322.surrey.petsearch.utils.LocationUtils.API_KEY;
import static nk00322.surrey.petsearch.utils.LocationUtils.AUTOCOMPLETE_REQUEST_CODE;
import static nk00322.surrey.petsearch.utils.LocationUtils.getLocationAutoCompleteIntent;
import static nk00322.surrey.petsearch.utils.LocationUtils.getPlaceFromId;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener, SetLocationListener {
    private static final String TAG = "MapFragment";
    private static final String SPINNER_PREFS = "spinnerPrefs";
    private CustomMapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private View view;
    private Spinner spinner;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private Disposable disposable;
    private GoogleMap googleMap;
    private ImageView dropdownIcon;
    private FloatingActionButton sightingFab, areaFab, help_fab;
    private ArrayList<Marker> markers = new ArrayList<>();
    private ArrayList<Polygon> polygons = new ArrayList<>();

    private SearchParty activeSearchParty;
    private BitmapDescriptor markerSightingIcon;
    private ArrayList<SearchParty> subscribedSearchParties;
    private TextView redMarkerLegend, greenMarkerLegend, noSearchPartySubscriptions;
    private CoordinatorLayout fabMapLayout;
    private ConstraintLayout helpInfoLayout;
    private static Animation  slideOutBottom, slideInBottom;


    private ArrayList<LatLng> latLngList = new ArrayList<>();
    private List<Marker> markerList = new ArrayList<>();
    boolean addAreaActive = false;
    private boolean isHelpActive = false;

    public MapFragment() {
    }

    //todo listen for updates to search party and update UI
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map, container, false);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = view.findViewById(R.id.map_view);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        spinner = view.findViewById(R.id.active_search_party_spinner);
        dropdownIcon = view.findViewById(R.id.dropdown_icon);
        sightingFab = view.findViewById(R.id.add_sighting_fab);
        areaFab = view.findViewById(R.id.add_area_fab);
        help_fab = view.findViewById(R.id.help_fab);
        redMarkerLegend = view.findViewById(R.id.red_marker_legend);
        greenMarkerLegend = view.findViewById(R.id.green_marker_legend);
        fabMapLayout = view.findViewById(R.id.fab_map_layout);
        noSearchPartySubscriptions = view.findViewById(R.id.no_search_party_subscriptions);
        helpInfoLayout = view.findViewById(R.id.help_info_layout);

        Drawable iconDrawable = getContext().getDrawable(R.drawable.ic_location_green_24dp);
        markerSightingIcon = getMarkerIconFromDrawable(iconDrawable);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        Places.initialize(getContext(), API_KEY);


        areaFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
        help_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));

        slideOutBottom = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_bottom);
        slideInBottom = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_bottom);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SearchParty searchParty = (SearchParty) parent.getSelectedItem();
                SharedPreferences.Editor editor = getContext().getSharedPreferences(SPINNER_PREFS, MODE_PRIVATE).edit();
                editor.putInt("selectedSearchPartyPosition", parent.getSelectedItemPosition());
                editor.apply();
                updateMapWith(searchParty);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sightingFab.setOnClickListener(this);
        areaFab.setOnClickListener(this);
        help_fab.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_sighting_fab:
                startActivityForResult(getLocationAutoCompleteIntent(Objects.requireNonNull(getContext()).getApplicationContext()), AUTOCOMPLETE_REQUEST_CODE);
                break;
            case R.id.add_area_fab:
                areaFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.background_color)));
                addArea();
                break;
            case R.id.help_fab:
                toggleHelpInfo();
                break;
        }

    }

    private void toggleHelpInfo() {
        //Check flag that represents the state of the help fab toggle state
        if (isHelpActive) { // hide help

            help_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
            DrawableCompat.setTintList(DrawableCompat.wrap(help_fab.getDrawable()), ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.grey_neutral)));
            helpInfoLayout.setVisibility(View.GONE);
            helpInfoLayout.startAnimation(slideOutBottom);
            isHelpActive = false;
        } else { // User wants to view help info
            help_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.background_color)));
            DrawableCompat.setTintList(DrawableCompat.wrap(help_fab.getDrawable()), ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white))); // <- background
            helpInfoLayout.setVisibility(View.VISIBLE);
            helpInfoLayout.startAnimation(slideInBottom);
            isHelpActive = true;


        }
    }

    /**
     * Receive result from place request. Gets called when a new sighting is created to place a marker.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //Result from location search for adding a pet sighting. First get location from place then update model with new Sighting
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());

                LatLng placeLocation = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude); // get place coordinates
                if (activeSearchParty != null) {
                    Observable<String> searchPartyUidObservable = getUidFromSearchParty(activeSearchParty);
                    disposable = searchPartyUidObservable.subscribe( // get db reference for current search party
                            id -> {
                                Sighting newSighting = new Sighting(new Timestamp(new Date()),
                                        place.getLatLng().latitude, place.getLatLng().longitude, place.getName());

                                FirebaseFirestore.getInstance().collection("searchParties")
                                        .document(id)
                                        .update("sightings", FieldValue.arrayUnion(newSighting)); // update sightings array with new sighting

                                //also update sightings locally
                                subscribedSearchParties.get(subscribedSearchParties.indexOf(activeSearchParty)).addToSightings(newSighting);

                                //update the ui with new search party
                                ArrayAdapter<SearchParty> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_dropdown_item, subscribedSearchParties);
                                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                                spinner.setPrompt("Track a Search Party");

                                spinner.setAdapter(
                                        new NothingSelectedSpinnerAdapter(
                                                adapter,
                                                R.layout.contact_spinner_row_nothing_selected,
                                                getContext()));
                                int position = getContext().getSharedPreferences(SPINNER_PREFS, MODE_PRIVATE).getInt("selectedSearchPartyPosition", -1);
                                if (position > 0 && spinner.getAdapter().getCount() >= position + 1)
                                    spinner.setSelection(position);

                            },
                            throwable -> Log.i(TAG, "Throwable " + throwable.getMessage()));

                    // place marker on sighting coordinates
                    Marker placeMarker = googleMap.addMarker(new MarkerOptions().position(placeLocation)
                            .title(place.getName())
                            .icon(markerSightingIcon)
                            .snippet("Created on: " + printDate(new Timestamp(new Date()).toDate())));
                    markers.add(placeMarker);
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                new CustomToast().showToast(getContext(), view, "Error with Google Maps. Couldn't retrieve location", ToastType.ERROR, false);
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("ERROR AUTOCOMPLETE", status.getStatusMessage());
            }
        }
    }

    /**
     * Uses a search party to update the map with relevant information and deletes information overlaid on the map by previously active search party.
     * There are three different kinds of information added to the map
     * 1. First disappearance red marker
     * 2. Array of green sighting markers
     * 3. Array of Searched Area polygons, coloured according to ownership
     *
     * @param searchParty to update Map UI with
     */
    private void updateMapWith(SearchParty searchParty) {
        if (searchParty != null) {
            activeSearchParty = searchParty;

            //Remove data from previously tracked search party
            for (Marker marker : markers)
                marker.remove();
            markers.clear();

            for (Polygon polygon : polygons)
                polygon.remove();
            polygons.clear();

            //Get search party from ID
            Observable<Place> searchPartyLocation = getPlaceFromId(searchParty.getLocationId(), getContext());
            disposable = searchPartyLocation.subscribe(
                    place -> {
                        //Set first disappearance location marker
                        Drawable iconDrawable = getContext().getDrawable(R.drawable.ic_location_red_24dp);
                        BitmapDescriptor markerIcon = getMarkerIconFromDrawable(iconDrawable);

                        LatLng placeLocation = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);

                        Marker placeMarker = googleMap.addMarker(new MarkerOptions().position(placeLocation)
                                .title(place.getName())
                                .icon(markerIcon)
                                .snippet("Created on: " + printDate(searchParty.getTimestampCreated().toDate())));
                        markers.add(placeMarker);
                    },
                    throwable -> Log.i(TAG, "Throwable " + throwable.getMessage()));

            //Add stored markers for pet sightings
            if (searchParty.getSightings() != null && !searchParty.getSightings().isEmpty())
                for (Sighting sighting : searchParty.getSightings()) {
                    if (sighting != null) {
                        LatLng placeLocation = new LatLng(sighting.getLatitude(), sighting.getLongitude());

                        Marker placeMarker = googleMap.addMarker(new MarkerOptions().position(placeLocation)
                                .title(sighting.getPlaceName())
                                .icon(markerSightingIcon)
                                .snippet("Created on: " + printDate(sighting.getTimestampCreated().toDate())));
                        markers.add(placeMarker);
                    }
                }

            //Add stored polygons aka searched areas
            if (searchParty.getSearchedAreas() != null && !searchParty.getSearchedAreas().isEmpty())
                for (SearchedArea searchedArea : searchParty.getSearchedAreas()) {
                    if (searchedArea != null && searchedArea.getPolygonVerticesInLatLng() != null && !searchedArea.getPolygonVerticesInLatLng().isEmpty()) {
                        createPolygon(searchedArea);
                    }
                }
        }
    }


    /**
     * Handles onClick for addArea FAB. Works like a toggle button. When first pressed it activates the searched area input mode and when
     * pressed again it compiles the user inputted vertices, draws the corresponding shape on the map and stores it in the SearchParty model.
     */
    private void addArea() {
        //Check flag that represents the state of the fab toggle state
        if (addAreaActive) {  // User has finished inputting a searched area
            googleMap.setOnMapClickListener(null); //Deactivate listener that adds polygon vertices
            areaFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
            DrawableCompat.setTintList(DrawableCompat.wrap(areaFab.getDrawable()), ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.map_green)));
            googleMap.setOnPolygonClickListener(polygon -> showInfoDialog((SearchedAreaTag) polygon.getTag())); // re-enable info dialog
            for (Polygon polygon : polygons) {
                polygon.setClickable(true);
            }
            //Remove grey vertex markers
            for (Marker marker : markerList) {
                marker.remove();
            }
            markerList.clear();     //TODO DEDSCRIPTION

            if (!latLngList.isEmpty() && latLngList.size() > 2) { // Polygon is required to have more than 2 vertices
                //Save searched area
                if (activeSearchParty != null) {
                    Observable<String> searchPartyUidObservable = getUidFromSearchParty(activeSearchParty);
                    disposable = searchPartyUidObservable.subscribe( // Retrieve active search party and save searched area
                            id -> {
                                SearchedArea searchedArea = new SearchedArea(latLngList, new Timestamp(new Date()), currentUser.getUid());

                                FirebaseFirestore.getInstance().collection("searchParties")
                                        .document(id)
                                        .update("searchedAreas", FieldValue.arrayUnion(searchedArea));

                                //also update locally
                                subscribedSearchParties.get(subscribedSearchParties.indexOf(activeSearchParty)).addToSearchedAreas(searchedArea);

                                ArrayAdapter<SearchParty> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_dropdown_item, subscribedSearchParties);
                                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                                spinner.setPrompt("Track a Search Party");

                                spinner.setAdapter(
                                        new NothingSelectedSpinnerAdapter(
                                                adapter,
                                                R.layout.contact_spinner_row_nothing_selected,
                                                getContext()));
                                int position = getContext().getSharedPreferences(SPINNER_PREFS, MODE_PRIVATE).getInt("selectedSearchPartyPosition", -1);
                                if (position > 0 && spinner.getAdapter().getCount() >= position + 1)
                                    spinner.setSelection(position);
                                latLngList.clear();
                            },
                            throwable -> Log.i(TAG, "Throwable " + throwable.getMessage()));
                }


            } else {
                if (latLngList.size() > 0) {
                    new CustomToast().showToast(getContext(), view, "Please select at least 3 points", ToastType.INFO, false);
                }
                latLngList.clear();
            }

            addAreaActive = false;
        } else { // User wants to input a new searched area
            areaFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.background_color)));
            DrawableCompat.setTintList(DrawableCompat.wrap(areaFab.getDrawable()), ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white))); // <- background

            googleMap.setOnPolygonClickListener(null); // disable info dialog
            for (Polygon polygon : polygons) {
                polygon.setClickable(false);
            }
            addAreaActive = true;

            googleMap.setOnMapClickListener(latLng -> { // Listener allows user to add polygon vertices when he clicks the map
                //Create Marker Options
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).icon(getMarkerIconFromDrawable(getContext().getDrawable(R.drawable.grey_rectangle)));
                Marker marker = googleMap.addMarker(markerOptions);
                //Add to list
                latLngList.add(latLng);
                markerList.add(marker);
            });

        }
    }

    /**
     * Creates a polygon from a set of parameters and adds it to the map.
     * The search party it belongs to is added to the tag for future reference
     * The tag is also used to uniquely identify polygons
     *
     * @param searchedArea associated with polygon
     */
    private void createPolygon(SearchedArea searchedArea) {
        ArrayList<LatLng> latLngList = (ArrayList<LatLng>) searchedArea.getPolygonVerticesInLatLng();

        PolygonOptions polygonOptions = new PolygonOptions().addAll(sortVertices(latLngList))
                .clickable(true);
        Polygon polygon = googleMap.addPolygon(polygonOptions);
        polygon.setStrokeColor(Color.argb(80, 128, 128, 128));
        polygon.setFillColor(
                searchedArea.getOwnerUid().equals(currentUser.getUid()) ? // Set color based on if the user owns this searched area
                        Color.argb(80, 33, 182, 118) :
                        Color.argb(80, 128, 128, 128)

        );
        polygon.setTag(new SearchedAreaTag(activeSearchParty, searchedArea));

        polygons.add(polygon);
    }

    /**
     * Setup map properties and populate active search party spinner.
     *
     * @param googleMap returns the Map object that represents the MapView
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        Observable<ArrayList<SearchParty>> searchPartiesObservable = getUserSubscriptions(currentUser.getUid());
        disposable = searchPartiesObservable.subscribe( // get user subscriptions to populate spinner
                searchParties -> {
                    subscribedSearchParties = searchParties;

                    //setup spinner
                    if (subscribedSearchParties.size() != 0) {
                        ArrayAdapter<SearchParty> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_dropdown_item, searchParties);
                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        spinner.setPrompt("Track a Search Party");

                        spinner.setAdapter(
                                new NothingSelectedSpinnerAdapter(
                                        adapter,
                                        R.layout.contact_spinner_row_nothing_selected,
                                        getContext()));
                        dropdownIcon.setVisibility(View.VISIBLE);

                        fabMapLayout.setVisibility(View.VISIBLE);

                        int position = getContext().getSharedPreferences(SPINNER_PREFS, MODE_PRIVATE).getInt("selectedSearchPartyPosition", -1);
                        if (position > 0 && spinner.getAdapter().getCount() >= position + 1)
                            spinner.setSelection(position);
                    } else {
                        noSearchPartySubscriptions.setVisibility(View.VISIBLE);
                    }
                    googleMap.getUiSettings().setMapToolbarEnabled(false);
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    try {
                        googleMap.setMyLocationEnabled(true);
                    } catch (SecurityException ignored) {

                    }

                    //Edit the following as per you needs
                    googleMap.setBuildingsEnabled(true);
                    googleMap.getUiSettings().setZoomGesturesEnabled(true);

                    // Get user location so the map can be zoomed into the user
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        LocationServices.getFusedLocationProviderClient(getContext()).getLastLocation().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User location found");

                                LatLng userLocation = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude()); //Make them global

                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLocation, 15);
                                googleMap.animateCamera(cameraUpdate);

                            } else {
                                Log.w(TAG, "Error getting current location.", task.getException());

                            }
                        });

                    } else {
                        checkPermission(getActivity());
                    }
                    this.googleMap = googleMap;
                },
                throwable -> Log.i(TAG, "Throwable " + throwable));

        // Polygons can be clicked to show a dialog with information
        googleMap.setOnPolygonClickListener(polygon -> {
            //LatLng polygonCentroid = findCentroid(polygon.getPoints());
            //Alternatively, display a text bubble from the centroid of the polygon
            //https://stackoverflow.com/questions/42170114/google-maps-text-overlay-android

            showInfoDialog((SearchedAreaTag) polygon.getTag());
        });
    }

    /**
     * Displays an information dialog for searched areas and allows owners to delete their searched areas.
     *
     * @param searchedAreaTag
     */
    @SuppressLint("SetTextI18n")
    private void showInfoDialog(SearchedAreaTag searchedAreaTag) {
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.searched_area_info_dialog, (ViewGroup) getView(), false);
        final TextView date = viewInflated.findViewById(R.id.searched_area_date);
        final TextView owner = viewInflated.findViewById(R.id.searched_area_owner);

        Observable<User> userObservable = getUserFromId(searchedAreaTag.searchParty.getOwnerUid());
        disposable = userObservable.subscribe(
                user -> {

                    //If Created by user, allow them to delete
                    if (searchedAreaTag.searchedArea.getOwnerUid().equals(currentUser.getUid())) {
                        owner.setVisibility(View.GONE);
                        date.setText("Created on: " + searchedAreaTag.printDate);
                        new MaterialAlertDialogBuilder(getContext())
                                .setTitle("My Searched area")
                                .setView(viewInflated)
                                .setMessage("for " + searchedAreaTag.searchParty.getTitle() + " search party")
                                .setNegativeButton("DISMISS", (dialog, id) -> {
                                })
                                .setPositiveButton("DELETE", (dialog, id) -> {
                                    FirebaseFirestore.getInstance().collection("searchParties").whereArrayContains("searchedAreas", searchedAreaTag.searchedArea).get()
                                            .addOnSuccessListener(task -> {
                                                for (DocumentSnapshot doc : task.getDocuments()) {
                                                    doc.getReference().update("searchedAreas", FieldValue.arrayRemove(searchedAreaTag.searchedArea));
                                                }

                                                for (Polygon polygon : polygons) //remove from UI
                                                    if (polygon.getTag().equals(searchedAreaTag)) {
                                                        polygon.remove();
                                                        polygons.remove(polygon);

                                                        subscribedSearchParties.get(subscribedSearchParties.indexOf(activeSearchParty)).getSearchedAreas().remove(searchedAreaTag.searchedArea);

                                                        ArrayAdapter<SearchParty> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_dropdown_item, subscribedSearchParties);
                                                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                                                        spinner.setPrompt("Track a Search Party");

                                                        spinner.setAdapter(
                                                                new NothingSelectedSpinnerAdapter(
                                                                        adapter,
                                                                        R.layout.contact_spinner_row_nothing_selected,
                                                                        getContext()));
                                                        int position = getContext().getSharedPreferences(SPINNER_PREFS, MODE_PRIVATE).getInt("selectedSearchPartyPosition", -1);
                                                        if (position > 0 && spinner.getAdapter().getCount() >= position + 1)
                                                            spinner.setSelection(position);

                                                        break;
                                                    }
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.w(TAG, "Error getting search parties.", e);
                                            });

                                })
                                .show();
                    } else { // Otherwise just show them information only
                        owner.setText("Created by: " + user.getFullName());
                        date.setText("Created on: " + searchedAreaTag.printDate);
                        new MaterialAlertDialogBuilder(getContext())
                                .setTitle("Searched area")
                                .setView(viewInflated)
                                .setMessage("for " + searchedAreaTag.searchParty.getTitle() + " search party")
                                .setNeutralButton("OK", (dialog, id) -> {
                                })
                                .show();
                    }
                },
                throwable -> Log.i(TAG, "Throwable " + throwable.getMessage()));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mMapView != null) {
            mMapView.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (disposable != null) {
            disposable.dispose();
        }

    }

    @Override
    public void onCompleted(Exception exception) {
        Log.w(TAG, "onCompleted ", exception);
    }

    /**
     * Sollution from: https://stackoverflow.com/questions/867518/how-to-make-an-android-spinner-with-initial-text-select-one
     * Decorator Adapter to allow a Spinner to show a 'Nothing Selected...' initially
     * displayed instead of the first choice in the Adapter.
     */
    public static class NothingSelectedSpinnerAdapter implements SpinnerAdapter, ListAdapter {

        protected static final int EXTRA = 1;
        protected SpinnerAdapter adapter;
        protected Context context;
        protected int nothingSelectedLayout;
        protected int nothingSelectedDropdownLayout;
        protected LayoutInflater layoutInflater;

        /**
         * Use this constructor to have NO 'Select One...' item, instead use
         * the standard prompt or nothing at all.
         *
         * @param spinnerAdapter        wrapped Adapter.
         * @param nothingSelectedLayout layout for nothing selected, perhaps
         *                              you want text grayed out like a prompt...
         * @param context
         */
        public NothingSelectedSpinnerAdapter(
                SpinnerAdapter spinnerAdapter,
                int nothingSelectedLayout, Context context) {

            this(spinnerAdapter, nothingSelectedLayout, -1, context);
        }

        /**
         * Use this constructor to Define your 'Select One...' layout as the first
         * row in the returned choices.
         * If you do this, you probably don't want a prompt on your spinner or it'll
         * have two 'Select' rows.
         *
         * @param spinnerAdapter                wrapped Adapter. Should probably return false for isEnabled(0)
         * @param nothingSelectedLayout         layout for nothing selected, perhaps you want
         *                                      text grayed out like a prompt...
         * @param nothingSelectedDropdownLayout layout for your 'Select an Item...' in
         *                                      the dropdown.
         * @param context
         */
        public NothingSelectedSpinnerAdapter(SpinnerAdapter spinnerAdapter,
                                             int nothingSelectedLayout, int nothingSelectedDropdownLayout, Context context) {
            this.adapter = spinnerAdapter;
            this.context = context;
            this.nothingSelectedLayout = nothingSelectedLayout;
            this.nothingSelectedDropdownLayout = nothingSelectedDropdownLayout;
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public final View getView(int position, View convertView, ViewGroup parent) {
            // This provides the View for the Selected Item in the Spinner, not
            // the dropdown (unless dropdownView is not set).
            if (position == 0) {
                return getNothingSelectedView(parent);
            }
            return adapter.getView(position - EXTRA, null, parent); // Could re-use
            // the convertView if possible.
        }

        /**
         * View to show in Spinner with Nothing Selected
         * Override this to do something dynamic... e.g. "37 Options Found"
         *
         * @param parent
         * @return
         */
        protected View getNothingSelectedView(ViewGroup parent) {
            return layoutInflater.inflate(nothingSelectedLayout, parent, false);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            // Android BUG! http://code.google.com/p/android/issues/detail?id=17128 -
            // Spinner does not support multiple view types
            if (position == 0) {
                return nothingSelectedDropdownLayout == -1 ?
                        new View(context) :
                        getNothingSelectedDropdownView(parent);
            }

            // Could re-use the convertView if possible, use setTag...
            return adapter.getDropDownView(position - EXTRA, null, parent);
        }

        /**
         * Override this to do something dynamic... For example, "Pick your favorite
         * of these 37".
         *
         * @param parent
         * @return
         */
        protected View getNothingSelectedDropdownView(ViewGroup parent) {
            return layoutInflater.inflate(nothingSelectedDropdownLayout, parent, false);
        }

        @Override
        public int getCount() {
            int count = adapter.getCount();
            return count == 0 ? 0 : count + EXTRA;
        }

        @Override
        public Object getItem(int position) {
            return position == 0 ? null : adapter.getItem(position - EXTRA);
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position >= EXTRA ? adapter.getItemId(position - EXTRA) : position - EXTRA;
        }

        @Override
        public boolean hasStableIds() {
            return adapter.hasStableIds();
        }

        @Override
        public boolean isEmpty() {
            return adapter.isEmpty();
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            adapter.registerDataSetObserver(observer);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            adapter.unregisterDataSetObserver(observer);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return position != 0; // Don't allow the 'nothing selected'
            // item to be picked.
        }

    }

    private static class SearchedAreaTag {
        SearchParty searchParty;
        String printDate;
        SearchedArea searchedArea;

        SearchedAreaTag(SearchParty searchParty, SearchedArea searchedArea) {

            this.searchParty = searchParty;
            this.searchedArea = searchedArea;
            this.printDate = printDate(searchedArea.getTimestampCreated().toDate());
        }
    }
}

