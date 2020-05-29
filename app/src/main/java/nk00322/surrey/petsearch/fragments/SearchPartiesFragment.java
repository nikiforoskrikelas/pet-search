package nk00322.surrey.petsearch.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.petsearch.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import nk00322.surrey.petsearch.CustomToast;
import nk00322.surrey.petsearch.FirestoreAdapter;
import nk00322.surrey.petsearch.FullscreenDisplaySearchParty;
import nk00322.surrey.petsearch.ToastType;
import nk00322.surrey.petsearch.models.SearchParty;
import uk.co.mgbramwell.geofire.android.GeoFire;
import uk.co.mgbramwell.geofire.android.model.Distance;
import uk.co.mgbramwell.geofire.android.model.DistanceUnit;
import uk.co.mgbramwell.geofire.android.model.QueryLocation;

import static nk00322.surrey.petsearch.utils.FirebaseUtils.isLoggedIn;
import static nk00322.surrey.petsearch.utils.GeneralUtils.checkPermission;
import static nk00322.surrey.petsearch.utils.GeneralUtils.getCreationDateComparator;
import static nk00322.surrey.petsearch.utils.GeneralUtils.getDistanceToUserComparator;
import static nk00322.surrey.petsearch.utils.LocationUtils.API_KEY;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchPartiesFragment extends Fragment implements View.OnClickListener, FirestoreAdapter.OnListItemCLick {
    private static final String TAG = "SearchPartiesFragment";
    private View view;
    private FirebaseAuth auth;
    private RecyclerView searchPartiesRecyclerView;
    private FirebaseUser currentUser;
    private long mLastClickTime = 0;
    private boolean dialogActive = false;
    private FirestoreAdapter adapter;
    private FloatingActionButton sortFilterButton;
    private int sortOptionIndex = 0;
    private int filterOptionIndex = 1;
    private Observable<Location> userLocation;
    private Double userLatitude;
    private Double userLongitude;
    private float distanceSliderValue = 20;

    public SearchPartiesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search_parties, container, false);
        final NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);

        if (!isLoggedIn()) {
            FirebaseAuth.getInstance().signOut();
            navController.navigate(R.id.welcomeFragment);
        }
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        Places.initialize(getContext(), API_KEY);


        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            userLocation = Observable.create(result ->
                    LocationServices.getFusedLocationProviderClient(getContext()).getLastLocation().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User location found");
                            if(task.getResult() == null){
                                navController.navigate(R.id.welcomeFragment);
                                FirebaseAuth.getInstance().signOut();
                                new CustomToast().showToast(getContext(), view, "Error while retrieving location", ToastType.ERROR, false);
                                result.onError(new Exception("Location error"));
                            }else {
                                userLatitude = task.getResult().getLatitude();
                                userLongitude = task.getResult().getLongitude();
                                result.onNext(task.getResult());
                            }
                        } else {
                            Log.w(TAG, "Error getting current location.", task.getException());
                            result.onError(task.getException());
                        }
                    })
            );

        } else {
            checkPermission(getActivity());
        }
        initViews();
        setupRecyclerView();
        setListeners();
        return view;

    }


    private void initViews() {
        searchPartiesRecyclerView = view.findViewById(R.id.search_parties_recycler_view);
        sortFilterButton = view.findViewById(R.id.sort_filter_fab);


    }

    private void setupRecyclerView() {
        Query query = FirebaseFirestore.getInstance().collection("searchParties")
                .orderBy("timestampCreated", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<SearchParty> options = new FirestoreRecyclerOptions.Builder<SearchParty>()
                .setQuery(query, SearchParty.class)
                .build();

        adapter = new FirestoreAdapter(options, getContext(), userLocation, currentUser.getUid(), this);

        searchPartiesRecyclerView.setHasFixedSize(true);
        searchPartiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchPartiesRecyclerView.setAdapter(adapter);
    }

    private void setListeners() {

        sortFilterButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000 || dialogActive) { //To prevent double clicking
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        switch (view.getId()) {
            case R.id.sort_filter_fab:
                sortFilterPopUp();
                break;

        }

    }

    @Override
    public void onItemClick(SearchParty searchParty, int position) {
        Log.d(TAG, "Item clicked: " + position + " and with title: " + searchParty.getTitle()) ;
        DialogFragment dialogFragment = new FullscreenDisplaySearchParty(searchParty, currentUser.getUid());
        dialogFragment.show(getActivity().getSupportFragmentManager(), "FullscreenDisplaySearchParty");

    }

    private void sortFilterPopUp() {
        //display alert dialog for sort filter options
        dialogActive = true;

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.sort_filter_dialog, (ViewGroup) getView(), false);
        final RadioGroup sortRadioGroup = viewInflated.findViewById(R.id.sort_radio_group);
        final RadioGroup filterRadioGroup = viewInflated.findViewById(R.id.filter_radio_group);

        final Slider distanceSlider = viewInflated.findViewById(R.id.distance_slider);

        filterRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // checkedId is the RadioButton selected
            if (checkedId == R.id.distance_filter_radio) {
                distanceSlider.setVisibility(View.VISIBLE);

            } else {
                distanceSlider.setVisibility(View.GONE);
            }

        });

        ((RadioButton) sortRadioGroup.getChildAt(sortOptionIndex)).setChecked(true);
        ((RadioButton) filterRadioGroup.getChildAt(filterOptionIndex)).setChecked(true);

        distanceSlider.addOnChangeListener((slider, value, fromUser) -> {
            distanceSliderValue = value;
        });

        distanceSlider.setValue(distanceSliderValue);

        final Query[] query = new Query[1];

        query[0] = FirebaseFirestore.getInstance().collection("searchParties");
        AtomicReference<FirestoreRecyclerOptions<SearchParty>> sortFilterOptions = new AtomicReference<>();

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Sort and Filter")
                .setIcon(R.drawable.ic_sort_white_24dp)
                .setView(viewInflated)
                .setPositiveButton("OK", (dialog, id) -> {
                    //Save user selection
                    switch (sortRadioGroup.getCheckedRadioButtonId()) {
                        case R.id.newest_radio:
                            sortOptionIndex = 0;
                            break;
                        case R.id.oldest_radio:
                            sortOptionIndex = 1;
                            break;
                        case R.id.distance_radio:
                            sortOptionIndex = 2;
                            break;
                    }

                    //Build query based on options
                    switch (filterRadioGroup.getCheckedRadioButtonId()) {
                        case R.id.my_subscriptions_radio:
                            // My Subscriptions
                            query[0] = query[0].whereArrayContains("subscriberUids", currentUser.getUid()); // filter out results where the user is not subscribed
                            switch (sortRadioGroup.getCheckedRadioButtonId()) {
                                case R.id.newest_radio:
                                    sortFilterOptions.set(new FirestoreRecyclerOptions.Builder<SearchParty>()
                                            .setQuery(query[0].orderBy("timestampCreated", Query.Direction.DESCENDING), SearchParty.class)
                                            .build());
                                    adapter.updateOptions(sortFilterOptions.get());
                                    break;
                                case R.id.oldest_radio:
                                    sortFilterOptions.set(new FirestoreRecyclerOptions.Builder<SearchParty>()
                                            .setQuery(query[0].orderBy("timestampCreated", Query.Direction.ASCENDING), SearchParty.class)
                                            .build());
                                    adapter.updateOptions(sortFilterOptions.get());
                                    break;
                                case R.id.distance_radio:
                                    query[0].get().addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            QuerySnapshot document = task.getResult();
                                            Log.i("DB", "Got Documents.");
                                            ObservableSnapshotArray<SearchParty> snapshotArray = new ObservableSnapshotArray<SearchParty>(snapshot -> snapshot.toObject(SearchParty.class)) {
                                                @NonNull
                                                @Override
                                                protected List<DocumentSnapshot> getSnapshots() {
                                                    //List already filtered by query
                                                    List<DocumentSnapshot> filteredList = document.getDocuments();

                                                    //Then sort by distance
                                                    filteredList.sort(getDistanceToUserComparator(userLatitude, userLongitude));

                                                    //Return filtered and sorted list
                                                    return filteredList;
                                                }
                                            };
                                            sortFilterOptions.set(new FirestoreRecyclerOptions.Builder<SearchParty>()
                                                    .setSnapshotArray(snapshotArray)
                                                    .build());
                                            adapter.updateOptions(sortFilterOptions.get());
                                        }
                                    });

                                    break;
                            }
                            filterOptionIndex = 0;
                            break;
                        case R.id.all_search_parties_radio:
                            // Not Subscribed not possible with current structure due to the nature of the DB queries. Show all instead
                            filterOptionIndex = 1;
                            switch (sortRadioGroup.getCheckedRadioButtonId()) {
                                case R.id.newest_radio:
                                    sortFilterOptions.set(new FirestoreRecyclerOptions.Builder<SearchParty>()
                                            .setQuery(query[0].orderBy("timestampCreated", Query.Direction.DESCENDING), SearchParty.class)
                                            .build());
                                    adapter.updateOptions(sortFilterOptions.get());
                                    break;
                                case R.id.oldest_radio:
                                    sortFilterOptions.set(new FirestoreRecyclerOptions.Builder<SearchParty>()
                                            .setQuery(query[0].orderBy("timestampCreated", Query.Direction.ASCENDING), SearchParty.class)
                                            .build());
                                    adapter.updateOptions(sortFilterOptions.get());
                                    break;
                                case R.id.distance_radio:
                                    query[0].get().addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            QuerySnapshot document = task.getResult();
                                            Log.i("DB", "Got Documents.");
                                            ObservableSnapshotArray<SearchParty> snapshotArray = new ObservableSnapshotArray<SearchParty>(snapshot -> snapshot.toObject(SearchParty.class)) {
                                                @NonNull
                                                @Override
                                                protected List<DocumentSnapshot> getSnapshots() { //todo make it to show only unsubscribed?
                                                    //No filtering applied for all subscriptions
                                                    List<DocumentSnapshot> unfilteredList = document.getDocuments();

                                                    //Then sort by distance
                                                    unfilteredList.sort(getDistanceToUserComparator(userLatitude, userLongitude));

                                                    //Return filtered and sorted list
                                                    return unfilteredList;
                                                }
                                            };
                                            sortFilterOptions.set(new FirestoreRecyclerOptions.Builder<SearchParty>()
                                                    .setSnapshotArray(snapshotArray)
                                                    .build());
                                            adapter.updateOptions(sortFilterOptions.get());
                                        }
                                    });


                                    break;
                            }
                            break;
                        case R.id.distance_filter_radio:
                            // Show only within distance from user input
                            filterOptionIndex = 2;
                            GeoFire geoFire = new GeoFire(FirebaseFirestore.getInstance().collection("searchParties"));
                            QueryLocation queryLocation = QueryLocation.fromDegrees(userLatitude, userLongitude);
                            Distance searchDistance = new Distance(distanceSlider.getValue(), DistanceUnit.KILOMETERS); //set by user
                            geoFire.query()
                                    .whereNearTo(queryLocation, searchDistance)
                                    .build()
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Log.i("DB", "Got Documents.");
                                            QuerySnapshot document = task.getResult();
                                            ObservableSnapshotArray<SearchParty> snapshotArray = new ObservableSnapshotArray<SearchParty>
                                                    (snapshot -> snapshot.toObject(SearchParty.class)) {
                                                @NonNull
                                                @Override
                                                protected List<DocumentSnapshot> getSnapshots() {
                                                    //Already filtered by geoFire query to only show within the set distance
                                                    List<DocumentSnapshot> filteredList = document.getDocuments();
                                                    switch (sortRadioGroup.getCheckedRadioButtonId()) {
                                                        case R.id.newest_radio:
                                                            filteredList.sort(getCreationDateComparator().reversed());
                                                            break;
                                                        case R.id.oldest_radio:
                                                            filteredList.sort(getCreationDateComparator());
                                                            break;
                                                        case R.id.distance_radio:
                                                            //Sort by distance
                                                            filteredList.sort(getDistanceToUserComparator(userLatitude, userLongitude));

                                                            break;
                                                    }


                                                    //Return filtered and sorted list
                                                    return filteredList;
                                                }
                                            };
                                            sortFilterOptions.set(new FirestoreRecyclerOptions.Builder<SearchParty>()
                                                    .setSnapshotArray(snapshotArray)
                                                    .build());
                                            adapter.updateOptions(sortFilterOptions.get());
                                        } else {
                                            Log.w("DB", "Error getting documents.", task.getException());
                                        }
                                    });


                            break;

                    }

                    dialogActive = false;
                })
                .setOnCancelListener(dialog -> dialogActive = false)
                .show();

    }


    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();

    }


}

