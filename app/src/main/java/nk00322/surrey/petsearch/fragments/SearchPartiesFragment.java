package nk00322.surrey.petsearch.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.petsearch.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import static nk00322.surrey.petsearch.utils.GeneralUtils.getTimeDate;
import static nk00322.surrey.petsearch.utils.LocationUtils.API_KEY;
import static nk00322.surrey.petsearch.utils.LocationUtils.PLACE_FIELDS;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchPartiesFragment extends Fragment {
    private static final String TAG = "SearchPartiesFragment";
    private View view;
    private FirebaseAuth auth;
    private RecyclerView searchPartiesRecyclerView;
    private FirebaseUser currentUser;

    private FirebaseRecyclerAdapter adapter;
    private Disposable disposable;

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

    }

    private void setupRecyclerView() {

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("searchParties");

        FirebaseRecyclerOptions<SearchParty> options = new FirebaseRecyclerOptions.Builder<SearchParty>()
                .setQuery(query, SearchParty.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<SearchParty, SearchPartyViewHolder>(options) {
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

                holder.date.setText(getTimeDate(model.getTimestampCreatedLong()));

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
    }

    private static class SearchPartyViewHolder extends RecyclerView.ViewHolder {

        TextView title, reward, location, date, owner;
        ImageView image;
        ProgressBar recyclerItemProgress;


        SearchPartyViewHolder(View itemView) {
            super(itemView);


            title = itemView.findViewById(R.id.title);
            reward = itemView.findViewById(R.id.reward);
            location = itemView.findViewById(R.id.location);
            date = itemView.findViewById(R.id.date);
            owner = itemView.findViewById(R.id.owner);
            recyclerItemProgress = itemView.findViewById(R.id.recycler_item_progress);

            image = itemView.findViewById(R.id.image);

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

