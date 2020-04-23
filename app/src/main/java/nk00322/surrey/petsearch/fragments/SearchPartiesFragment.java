package nk00322.surrey.petsearch.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
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
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

import static nk00322.surrey.petsearch.utils.FirebaseUtils.getUserFromId;
import static nk00322.surrey.petsearch.utils.FirebaseUtils.isLoggedIn;
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

        initViews();
        setupRecyclerView();
        setListeners();
        return view;

    }


    private void initViews() {
        searchPartiesRecyclerView = view.findViewById(R.id.search_parties_recycler_view);
        sortFilterButton = view.findViewById(R.id.sort_filter_fab);


    }

    //TODO QUERYING WITH FIRESTORE
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
                    Places.initialize(getContext(), API_KEY);
                    FetchPlaceRequest request = FetchPlaceRequest.newInstance(model.getLocationId(), PLACE_FIELDS);
                    PlacesClient placesClient = Places.createClient(getContext());
                    Runnable fetchUserInfo = () -> placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                        holder.location.setText(response.getPlace().getName());
                        Log.i(TAG, "Place found");
                    }).addOnFailureListener((exception) -> {
                        if (exception instanceof ApiException) {
                            Log.e(TAG, "Place not found: " + exception.getMessage());
                            holder.location.setText("N/A");
                        }
                    });
                    Thread thread = new Thread(fetchUserInfo);
                    thread.start();


                } else {
                    holder.location.setText("N/A");
                    Log.i(TAG, "No location stored in user");
                }
                holder.date.setText(printDate(model.getTimestampCreated().toDate()));

                if (model.getSubscriberUids() != null && model.getSubscriberUids().contains(currentUser.getUid())) {
                    Drawable drawable = getContext().getDrawable(R.drawable.ic_check_success_green_24dp);
                    holder.subscribed.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                }else{
                    Drawable drawable = getContext().getDrawable(R.drawable.ic_close_red_24dp);
                    holder.subscribed.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                }
                Observable<User> userObservable = getUserFromId(model.getOwnerUid());
                disposable = userObservable.subscribe(
                        user -> holder.owner.setText(user.getFullName()),
                        throwable -> Log.i(TAG, "Throwable " + throwable.getMessage()));


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

        if (sortOptionIndex != -1) {
            ((RadioButton) sortRadioGroup.getChildAt(sortOptionIndex)).setChecked(true);

        }
        if (filterOptionIndex != -1) {
            ((RadioButton) filterRadioGroup.getChildAt(filterOptionIndex)).setChecked(true);
        }

        final Query[] query = new Query[1];

        query[0] = FirebaseFirestore.getInstance().collection("searchParties");
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Sort and Filter")
                .setIcon(R.drawable.ic_sort_white_24dp)
                .setView(viewInflated)
                .setPositiveButton("OK", (dialog, id) -> {
                    //Build query based on options
                    switch (sortRadioGroup.getCheckedRadioButtonId()) {
                        case R.id.newest_radio:
                            // newest

                            query[0] = query[0].orderBy("timestampCreated", Query.Direction.DESCENDING);

                            sortOptionIndex = 0;
                            break;
                        case R.id.oldest_radio:
                            // Oldest
                            query[0] = query[0].orderBy("timestampCreated", Query.Direction.ASCENDING);

                            sortOptionIndex = 1;

                            break;
                        case R.id.distance_radio:
                            // Distance from me
                            sortOptionIndex = 2;

                            break;
                    }
                    switch (filterRadioGroup.getCheckedRadioButtonId()) {
                        case R.id.my_subscriptions_radio:
                            // My Subscriptions
                            query[0] = query[0].whereArrayContains("subscriberUids", currentUser.getUid());
                            filterOptionIndex = 0;
                            break;
                        case R.id.all_search_parties_radio:
                            // Not Subscribed not possible with current structure due to the nature of the DB queries. Show all instead
                            filterOptionIndex = 1;

                            break;

                    }
                    dialogActive = false;


                    adapter.updateOptions(new FirestoreRecyclerOptions.Builder<SearchParty>()
                            .setQuery(query[0], SearchParty.class)
                            .build());

                })
                .setOnCancelListener(dialog -> dialogActive = false)
                .show();

    }


    private static class SearchPartyViewHolder extends RecyclerView.ViewHolder {

        TextView title, reward, location, date, owner, subscribed;
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

