package nk00322.surrey.petsearch.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.petsearch.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import nk00322.surrey.petsearch.models.SearchParty;
import nk00322.surrey.petsearch.models.User;
import uk.co.mgbramwell.geofire.android.GeoFire;
import uk.co.mgbramwell.geofire.android.model.Distance;
import uk.co.mgbramwell.geofire.android.model.DistanceUnit;
import uk.co.mgbramwell.geofire.android.model.QueryLocation;

import static nk00322.surrey.petsearch.utils.FirebaseUtils.getUserFromId;
import static nk00322.surrey.petsearch.utils.FirebaseUtils.isLoggedIn;
import static nk00322.surrey.petsearch.utils.GeneralUtils.checkPermission;
import static nk00322.surrey.petsearch.utils.GeneralUtils.getCreationDateComparator;
import static nk00322.surrey.petsearch.utils.GeneralUtils.getDistanceInKilometers;
import static nk00322.surrey.petsearch.utils.GeneralUtils.getDistanceToUserComparator;
import static nk00322.surrey.petsearch.utils.GeneralUtils.printDate;
import static nk00322.surrey.petsearch.utils.LocationUtils.API_KEY;
import static nk00322.surrey.petsearch.utils.LocationUtils.PLACE_FIELDS;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchPartiesFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "SearchPartiesFragment";
    private View view;
    private FirebaseAuth auth;
    private RecyclerView searchPartiesRecyclerView;
    private FirebaseUser currentUser;
    private long mLastClickTime = 0;
    private boolean dialogActive = false;
    private FirestoreRecyclerAdapter adapter;
    private Disposable disposable;
    private FloatingActionButton sortFilterButton;
    private int sortOptionIndex = 0;
    private int filterOptionIndex = 1;
    private Double userLatitude;
    private Double userLongitude;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationClient;
    private float distanceSliderValue = 20;

    public SearchPartiesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search_parties, container, false);
        if (!isLoggedIn()) {
            final NavController navController = Navigation.findNavController(view);
            FirebaseAuth.getInstance().signOut();
            navController.navigate(R.id.action_organizeFragment_to_welcomeFragment);
        }
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        Places.initialize(getContext(), API_KEY);
        placesClient = Places.createClient(getContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext()); //todo only works if google maps has been used? find another sollution?

        checkPermission(getActivity());
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

        Query query = FirebaseFirestore.getInstance().collection("searchParties").orderBy("timestampCreated", Query.Direction.DESCENDING);


        FirestoreRecyclerOptions<SearchParty> options = new FirestoreRecyclerOptions.Builder<SearchParty>()
                .setQuery(query, SearchParty.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<SearchParty, SearchPartyViewHolder>(options) {

            @NonNull
            @Override
            public SearchPartyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_party_list_item, parent, false);
                return new SearchPartyViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull SearchPartyViewHolder holder, int position, @NonNull SearchParty model) {
                holder.title.setText(model.getTitle());
                holder.reward.setText(model.getReward());


                if (model.getLocationId() != null) {
                    FetchPlaceRequest request = FetchPlaceRequest.newInstance(model.getLocationId(), PLACE_FIELDS);
                    Runnable fetchUserInfo = () -> placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                        holder.location.setText(response.getPlace().getName());
                    }).addOnFailureListener((exception) -> {
                        if (exception instanceof ApiException) {
                            Log.e(TAG, "Place not found: " + exception.getMessage());
                            holder.location.setText("N/A");
                        }
                    });
                    Thread thread = new Thread(fetchUserInfo);
                    thread.start();


                    Observable<User> userObservable = getUserFromId(model.getOwnerUid());
                    disposable = userObservable.subscribe(
                            user -> holder.owner.setText(user.getFullName()),
                            throwable -> Log.i(TAG, "Throwable " + throwable.getMessage()));

                    // Re-check before enabling. You can add an else statement to warn the user about the lack of functionality if it's disabled.
                    // "or" is used instead of "and" as per the error. If it requires both, flip it over to &&. (I'm not sure, I haven't used GPS stuff before)
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(location -> {
                                    Log.i(TAG, "Location retrieved");
                                    userLatitude = location.getLatitude();
                                    userLongitude = location.getLongitude();
                                    double distance = getDistanceInKilometers(
                                            model.getLatitude(), model.getLongitude(), userLatitude, userLongitude);

                                    holder.distance.setText(distance + " km");
                                }).addOnFailureListener(e -> {
                            Log.i(TAG, "Location not retrieved: " + e.getMessage());

                        });
                    } else {
                        holder.distance.setText("N/A");
                    }

                    Log.i(TAG, "Place found");


                } else {
                    holder.location.setText("N/A");
                    Log.i(TAG, "No location stored in user");
                }
                holder.date.setText(printDate(model.getTimestampCreated().toDate()));

                holder.subscribed.setCompoundDrawablesWithIntrinsicBounds(
                        getContext().getDrawable(model.getSubscriberUids() != null && model.getSubscriberUids().contains(currentUser.getUid()) ?
                                R.drawable.ic_check_success_green_24dp :
                                R.drawable.ic_close_red_24dp)
                        , null, null, null);

                try {
                    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(model.getImageUrl());
                    Glide.with(getContext())
                            .load(imageRef)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    holder.recyclerItemProgress.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    holder.recyclerItemProgress.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .into(holder.image);
                } catch (IllegalArgumentException e) {
                    Log.i(TAG, "Error loading model with title: " + model.getTitle());
                }


            }
        };

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
                            geoFire.query() //todo change api ? This isn't that accurate
                                    .whereNearTo(queryLocation, searchDistance)
                                    .build()
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Log.i("DB", "Got Documents.");
                                            QuerySnapshot document = task.getResult();
                                            ObservableSnapshotArray<SearchParty> snapshotArray = new ObservableSnapshotArray<SearchParty>(snapshot -> snapshot.toObject(SearchParty.class)) {
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


    private static class SearchPartyViewHolder extends RecyclerView.ViewHolder {

        TextView title, reward, location, date, owner, subscribed, distance;
        ImageView image;
        ProgressBar recyclerItemProgress;


        SearchPartyViewHolder(View itemView) {
            super(itemView);


            title = itemView.findViewById(R.id.title);
            reward = itemView.findViewById(R.id.reward);
            location = itemView.findViewById(R.id.location);
            date = itemView.findViewById(R.id.date);
            owner = itemView.findViewById(R.id.owner);
            subscribed = itemView.findViewById(R.id.subscribed);
            distance = itemView.findViewById(R.id.distance);

            image = itemView.findViewById(R.id.image);
            recyclerItemProgress = itemView.findViewById(R.id.recycler_item_progress);

        }
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
        if (disposable != null) {
            disposable.dispose();
        }

    }
}

