package nk00322.surrey.petsearch.fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.example.petsearch.R;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import nk00322.surrey.petsearch.CustomMapView;
import nk00322.surrey.petsearch.models.SearchParty;

import static android.content.Context.MODE_PRIVATE;
import static nk00322.surrey.petsearch.utils.FirebaseUtils.getUserSubscriptions;
import static nk00322.surrey.petsearch.utils.GeneralUtils.checkPermission;
import static nk00322.surrey.petsearch.utils.GeneralUtils.getMarkerIconFromDrawable;
import static nk00322.surrey.petsearch.utils.GeneralUtils.printDate;
import static nk00322.surrey.petsearch.utils.LocationUtils.API_KEY;
import static nk00322.surrey.petsearch.utils.LocationUtils.getPlaceFromId;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {
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
    private ArrayList<Marker> markers = new ArrayList<>();

    public MapFragment() {
    }


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

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        Places.initialize(getContext(), API_KEY);


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

        return view;
    }

    private void updateMapWith(SearchParty searchParty) {
        if (searchParty != null) {
            Observable<Place> searchPartyLocation = getPlaceFromId(searchParty.getLocationId(), getContext());


            disposable = searchPartyLocation.subscribe(
                    place -> {
                        //Remove data from previously tracked search party
                        for (Marker marker : markers) {
                            marker.remove();
                        }

                        Drawable circleDrawable = getContext().getDrawable(R.drawable.ic_location_red_24dp);
                        BitmapDescriptor markerIcon = getMarkerIconFromDrawable(circleDrawable);

                        LatLng placeLocation = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude); //Make them global
                        Marker placeMarker = googleMap.addMarker(new MarkerOptions().position(placeLocation)
                                .title(place.getName())
                                .icon(markerIcon)
                                .snippet("Created on: " + printDate(searchParty.getTimestampCreated().toDate())));
                        markers.add(placeMarker);
                    },
                    throwable -> Log.i(TAG, "Throwable " + throwable.getMessage()));
        }
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
    public void onMapReady(GoogleMap googleMap) {

        Observable<ArrayList<SearchParty>> searchPartiesObservable = getUserSubscriptions(currentUser.getUid());
        disposable = searchPartiesObservable.subscribe(
                searchParties -> {

                    ArrayAdapter<SearchParty> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_dropdown_item, searchParties);
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinner.setPrompt("Track a Search Party");

                    spinner.setAdapter(
                            new NothingSelectedSpinnerAdapter(
                                    adapter,
                                    R.layout.contact_spinner_row_nothing_selected,
                                    getContext()));
                    dropdownIcon.setVisibility(View.VISIBLE);

                    int position = getContext().getSharedPreferences(SPINNER_PREFS, MODE_PRIVATE).getInt("selectedSearchPartyPosition", -1);
                    if (position > 0)
                        spinner.setSelection(position);


                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    try {
                        googleMap.setMyLocationEnabled(true);
                    } catch (SecurityException se) {

                    }

                    //Edit the following as per you needs
                    googleMap.setBuildingsEnabled(true);
                    googleMap.getUiSettings().setZoomGesturesEnabled(true);


                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        LocationServices.getFusedLocationProviderClient(getContext()).getLastLocation().addOnCompleteListener(task -> { //todo only works if google maps has been used? find another solution?
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User location found");

                                LatLng userLocation = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude()); //Make them global

                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLocation, 10);
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


    /**
     * Decorator Adapter to allow a Spinner to show a 'Nothing Selected...' initially
     * displayed instead of the first choice in the Adapter.
     */
    public class NothingSelectedSpinnerAdapter implements SpinnerAdapter, ListAdapter {

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

}

