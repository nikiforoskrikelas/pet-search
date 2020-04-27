package nk00322.surrey.petsearch;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.petsearch.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import nk00322.surrey.petsearch.models.SearchParty;
import nk00322.surrey.petsearch.models.User;

import static nk00322.surrey.petsearch.utils.FirebaseUtils.getUserFromId;
import static nk00322.surrey.petsearch.utils.GeneralUtils.checkPermission;
import static nk00322.surrey.petsearch.utils.GeneralUtils.getDistanceInKilometers;
import static nk00322.surrey.petsearch.utils.GeneralUtils.printDate;
import static nk00322.surrey.petsearch.utils.LocationUtils.PLACE_FIELDS;
import static nk00322.surrey.petsearch.utils.LocationUtils.getPlaceFromId;

public class FullscreenDisplaySearchParty extends DialogFragment implements View.OnClickListener, OnMapReadyCallback {

    private SearchParty searchParty;
    private String currentUserUid;

    private TextView title, reward, date, owner, distance;
    private ImageView image;
    private CheckBox subscribeCheckbox;
    private static final String TAG = "FullscreenDisplaySearchParty";
    private Disposable disposable;
    private ProgressBar imageProgress;
    private CustomMapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";


    public FullscreenDisplaySearchParty(SearchParty searchParty, String currentUserUid) {
        this.searchParty = searchParty;
        this.currentUserUid = currentUserUid;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fullscreen_display_search_party, container, false);
        ImageButton close = view.findViewById(R.id.close_dialog);

        title = view.findViewById(R.id.search_party_title);
        reward = view.findViewById(R.id.search_party_reward);
        date = view.findViewById(R.id.search_party_date);
        owner = view.findViewById(R.id.search_party_owner);
        subscribeCheckbox = view.findViewById(R.id.search_party_subscribed);
        distance = view.findViewById(R.id.search_party_distance);
        image = view.findViewById(R.id.search_party_image);
        imageProgress = view.findViewById(R.id.image_progress);

        title.setText(searchParty.getTitle());
        reward.setText(searchParty.getReward());

        Observable<User> userObservable = getUserFromId(searchParty.getOwnerUid());
        disposable = userObservable.subscribe(
                user -> owner.setText(user.getFullName()),
                throwable -> Log.i(TAG, "Throwable " + throwable.getMessage()));

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(getContext()).getLastLocation().addOnCompleteListener(task -> { //todo only works if google maps has been used? find another solution?
                if (task.isSuccessful()) {
                    Log.d(TAG, "User location found");
                    double distanceKm = getDistanceInKilometers(searchParty.getLatitude(), searchParty.getLongitude(),
                            task.getResult().getLatitude(), task.getResult().getLongitude());
                    distance.setText(distanceKm + " km");
                } else {
                    Log.w(TAG, "Error getting current location.", task.getException());

                }
            });

        } else {
            checkPermission(getActivity());
        }

        date.setText(printDate(searchParty.getTimestampCreated().toDate()));

        if (searchParty.getSubscriberUids() != null && searchParty.getSubscriberUids().contains(currentUserUid))
            subscribeCheckbox.setChecked(true);
        else
            subscribeCheckbox.setChecked(false);


        try {
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(searchParty.getImageUrl());
            Glide.with(getContext())
                    .load(imageRef)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            imageProgress.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            imageProgress.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(image);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "Error loading image");
        }


        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = view.findViewById(R.id.search_party_map);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);


        close.setOnClickListener(this);
        view.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
            @Override
            public void onSwipeLeft() {
                // Whatever //todo make it so that swipe shows the map
            }
        });

        subscribeCheckbox.setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.close_dialog:
                dismiss();
                break;
            case R.id.search_party_subscribed:
                CollectionReference searchPartiesRef = FirebaseFirestore.getInstance().collection("searchParties");
                searchPartiesRef
                        .whereEqualTo("locationId", searchParty.getLocationId())
                        .whereEqualTo("ownerUid", searchParty.getOwnerUid())
                        .whereEqualTo("timestampCreated", searchParty.getTimestampCreated()).get().addOnSuccessListener(task -> {

                    for (DocumentSnapshot doc : task.getDocuments()) {
                        if (Objects.requireNonNull(doc.toObject(SearchParty.class)).equals(searchParty)) {
                            if(subscribeCheckbox.isChecked()){
                                searchPartiesRef.document(doc.getId()).update("subscriberUids", FieldValue.arrayUnion(currentUserUid));
                                searchParty.getSubscriberUids().add(currentUserUid); // mirror db changes to local search party in case the user tries to subscribe again
                            }else{
                                searchPartiesRef.document(doc.getId()).update("subscriberUids", FieldValue.arrayRemove(currentUserUid));
                                searchParty.getSubscriberUids().remove(currentUserUid);
                            }
                            break;
                        }

                    }
                });
                break;

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
        mMapView.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (disposable != null) {
            disposable.dispose();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Observable<Place> userLocationObservable = getPlaceFromId(searchParty.getLocationId(), getContext());


        disposable = userLocationObservable.subscribe(
                place -> {
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    try {
                        googleMap.setMyLocationEnabled(true);
                    } catch (SecurityException se) {

                    }

                    //Edit the following as per you needs
                    googleMap.setTrafficEnabled(true);
                    googleMap.setIndoorEnabled(true);
                    googleMap.setBuildingsEnabled(true);
                    googleMap.getUiSettings().setZoomControlsEnabled(true);
                    //

                    LatLng placeLocation = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude); //Make them global
                    Marker placeMarker = googleMap.addMarker(new MarkerOptions().position(placeLocation)
                            .title(place.getName()));

                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(placeLocation));
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 1000, null);
                },
                throwable -> Log.i(TAG, "Throwable " + throwable.getMessage()));


    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
