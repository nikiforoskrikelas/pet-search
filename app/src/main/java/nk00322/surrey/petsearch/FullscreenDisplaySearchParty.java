package nk00322.surrey.petsearch;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.petsearch.R;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import nk00322.surrey.petsearch.models.SearchParty;
import nk00322.surrey.petsearch.models.User;

import static nk00322.surrey.petsearch.utils.FirebaseUtils.deleteSearchPartyWithId;
import static nk00322.surrey.petsearch.utils.FirebaseUtils.getUserFromId;
import static nk00322.surrey.petsearch.utils.GeneralUtils.checkPermission;
import static nk00322.surrey.petsearch.utils.GeneralUtils.getDistanceInKilometers;
import static nk00322.surrey.petsearch.utils.GeneralUtils.printDate;
import static nk00322.surrey.petsearch.utils.LocationUtils.getPlaceFromId;

public class FullscreenDisplaySearchParty extends DialogFragment implements View.OnClickListener, OnMapReadyCallback {
    private SearchParty searchParty;
    private String currentUserUid;

    private TextView title, reward, date, owner, distance, deleteAction, completed, subscriberCount, searchPartyDescription;
    private ImageView image;
    private CheckBox subscribeCheckbox, completedCheckbox;
    private static final String TAG = "FullscreenDisplaySearchParty";
    private final CompositeDisposable disposables = new CompositeDisposable();
    private ProgressBar imageProgress;
    private CustomMapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private View view;

    public FullscreenDisplaySearchParty(SearchParty searchParty, String currentUserUid) {
        this.searchParty = searchParty;
        this.currentUserUid = currentUserUid;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fullscreen_display_search_party, container, false);
        ImageButton close = view.findViewById(R.id.close_dialog);

        title = view.findViewById(R.id.search_party_title);
        reward = view.findViewById(R.id.search_party_reward);
        date = view.findViewById(R.id.search_party_date);
        owner = view.findViewById(R.id.search_party_owner);
        subscribeCheckbox = view.findViewById(R.id.search_party_subscribed);
        distance = view.findViewById(R.id.search_party_distance);
        image = view.findViewById(R.id.search_party_image);
        imageProgress = view.findViewById(R.id.image_progress);
        completed = view.findViewById(R.id.search_party_completed);
        deleteAction = view.findViewById(R.id.search_party_delete_action);
        completedCheckbox = view.findViewById(R.id.search_party_completed_checkbox);
        subscriberCount = view.findViewById(R.id.search_party_subscriber_count);
        searchPartyDescription = view.findViewById(R.id.search_party_description);

        title.setText(searchParty.getTitle());
        reward.setText(searchParty.getReward());
        searchPartyDescription.setText(searchParty.getDescription());

        Observable<User> userObservable = getUserFromId(searchParty.getOwnerUid());
        disposables.add(userObservable.subscribe(
                user -> owner.setText(user.getFullName()),
                throwable -> Log.i(TAG, "Throwable " + throwable.getMessage())));

        subscribeCheckbox.setOnClickListener(this);
        deleteAction.setOnClickListener(this);
        completedCheckbox.setOnClickListener(this);


        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(getContext()).getLastLocation().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "User location found");
                    if (task.getResult() != null) {
                        double distanceKm = getDistanceInKilometers(searchParty.getLatitude(), searchParty.getLongitude(),
                                task.getResult().getLatitude(), task.getResult().getLongitude());
                        distance.setText(distanceKm + " km");
                    } else {
                        distance.setText("N/A");
                    }
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

        subscriberCount.setText("[" + searchParty.getSubscriberUids().size() + "]");
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
                            if (getContext() != null) {
                                Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.textview_outline);
                                if (drawable != null)
                                    imageProgress.setBackground(drawable);
                            }
                            return false;
                        }
                    })
                    .into(image);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "Error loading image");
        }


        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = view.findViewById(R.id.search_party_map);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);

        if (searchParty.getOwnerUid().equals(currentUserUid)) {
            //todo add edit search party
            deleteAction.setVisibility(View.VISIBLE);
            completedCheckbox.setVisibility(View.VISIBLE);
            completedCheckbox.setChecked(searchParty.isCompleted());

            completed.setVisibility(View.GONE);

            deleteAction.setFocusable(true);
            deleteAction.setClickable(true);
        } else {
            ConstraintLayout.LayoutParams newLayoutParams = (ConstraintLayout.LayoutParams) distance.getLayoutParams();
            newLayoutParams.topMargin = 12;
            distance.setLayoutParams(newLayoutParams);

            if (searchParty.isCompleted()) {
                completed.setText("Status: Completed");
                completed.setCompoundDrawablesWithIntrinsicBounds(
                        getContext().getDrawable(R.drawable.ic_check_success_green_24dp), null, null, null);
            } else {
                completed.setText("Status: In progress");
                completed.setCompoundDrawablesWithIntrinsicBounds(
                        getContext().getDrawable(R.drawable.ic_searching_status_24dp), null, null, null);
            }
        }


        close.setOnClickListener(this);

        return view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View view) {
        CollectionReference searchPartiesRef = FirebaseFirestore.getInstance().collection("searchParties");

        switch (view.getId()) {
            case R.id.close_dialog:
                dismiss();
                break;
            case R.id.search_party_subscribed:
                if (subscribeCheckbox.isChecked()) {
                    searchPartiesRef.document(searchParty.getId()).update("subscriberUids", FieldValue.arrayUnion(currentUserUid));
                    searchParty.getSubscriberUids().add(currentUserUid); // mirror db changes to local search party in case the user tries to subscribe again
                    subscriberCount.setText("[" + searchParty.getSubscriberUids().size() + "]");

                    FirebaseMessaging.getInstance().subscribeToTopic(searchParty.getId());
                } else {
                    searchPartiesRef.document(searchParty.getId()).update("subscriberUids", FieldValue.arrayRemove(currentUserUid));
                    searchParty.getSubscriberUids().remove(currentUserUid);
                    subscriberCount.setText("[" + searchParty.getSubscriberUids().size() + "]");
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(searchParty.getId())
                            .addOnCompleteListener(task -> {
                                String msg = "SUCCESS UNSUBSCRIBE";
                                if (!task.isSuccessful()) {
                                    msg = "ERROR UNSUBSCRIBE";
                                }
                                Log.d(TAG, msg);
                                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                            });

                }
                break;
            case R.id.search_party_delete_action:
                deleteConfirmDialog();
                break;
            case R.id.search_party_completed_checkbox:
                if (completedCheckbox.isChecked()) {
                    searchPartiesRef.document(searchParty.getId()).update("completed", true);
                    searchParty.setCompleted(true); // mirror db changes to local search party in case the user tries action again
                } else {
                    searchPartiesRef.document(searchParty.getId()).update("completed", false);
                    searchParty.setCompleted(false);
                }
                break;


        }
    }

    private void deleteConfirmDialog() {
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.password_input_dialog, (ViewGroup) getView(), false);
        final EditText input = viewInflated.findViewById(R.id.password_input);


        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Delete Search Party")
                .setView(viewInflated)
                .setMessage("Please verify your password first")
                .setPositiveButton("Delete", (dialog, id) -> {
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (!input.getText().toString().isEmpty()) {
                        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), input.getText().toString());
                        user.reauthenticate(credential)
                                .addOnCompleteListener(reauthorizeTask -> {
                                    if (reauthorizeTask.isSuccessful()) {
                                        Observable<Boolean> searchPartyDeleteObservable = deleteSearchPartyWithId(searchParty.getId());
                                        disposables.add(searchPartyDeleteObservable.subscribe(dataDeleteResult -> {
                                            if (dataDeleteResult != null && dataDeleteResult) {
                                                dismiss();
                                                new CustomToast().showToast(getContext(), view, "Search Party Deleted Successfully", ToastType.SUCCESS, false);
                                            } else {
                                                dismiss();
                                                new CustomToast().showToast(getContext(), view, "Error: Search Party was not deleted", ToastType.ERROR, false);
                                            }
                                        }, throwable -> {
                                            new CustomToast().showToast(getContext(), view, "Error: Search Party was not deleted", ToastType.ERROR, false);
                                            Log.i(TAG, "Throwable " + throwable.getMessage());
                                        }));

                                    } else {
                                        new CustomToast().showToast(getContext(), view, "Incorrect password, please try again", ToastType.ERROR, false);
                                    }
                                });

                    } else {
                        new CustomToast().showToast(getContext(), view, "Password can not be empty", ToastType.ERROR, false);
                        input.setError("Password can not be empty");
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> {

                })
                .show();

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
    public void onStop() {
        super.onStop();
        if (disposables.size() != 0) {
            disposables.clear();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Observable<Place> searchPartyLocation = getPlaceFromId(searchParty.getLocationId(), getContext());


        disposables.add(searchPartyLocation.subscribe(
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
                throwable -> Log.i(TAG, "Throwable " + throwable.getMessage())));


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
}
