package nk00322.surrey.petsearch;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
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
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.rxjava3.core.Observable;
import nk00322.surrey.petsearch.models.SearchParty;
import nk00322.surrey.petsearch.models.User;

import static nk00322.surrey.petsearch.utils.FirebaseUtils.getUserFromId;
import static nk00322.surrey.petsearch.utils.GeneralUtils.getDistanceInKilometers;
import static nk00322.surrey.petsearch.utils.GeneralUtils.printDate;
import static nk00322.surrey.petsearch.utils.LocationUtils.PLACE_FIELDS;

public class FirestoreAdapter extends FirestoreRecyclerAdapter<SearchParty, FirestoreAdapter.SearchPartyViewHolder> {
    private Context context;
    private static final String TAG = "FirestoreAdapter";
    private Observable<Location> userLocation;
    private String currentUserUid;
    private OnListItemCLick onListItemCLick;

    public FirestoreAdapter(@NonNull FirestoreRecyclerOptions<SearchParty> options, Context context, Observable<Location> userLocation, String currentUserUid, OnListItemCLick onListItemCLick) {
        super(options);
        this.context = context;
        this.userLocation = userLocation;
        this.currentUserUid = currentUserUid;
        this.onListItemCLick = onListItemCLick;
    }


    @Override
    protected void onBindViewHolder(@NonNull SearchPartyViewHolder holder, int position, @NonNull SearchParty model) {

        holder.title.setText(model.getTitle());
        holder.reward.setText(model.getReward());

        if (model.getLocationId() != null) {
            FetchPlaceRequest request = FetchPlaceRequest.newInstance(model.getLocationId(), PLACE_FIELDS);
            Runnable fetchUserInfo = () -> Places.createClient(context).fetchPlace(request).addOnSuccessListener((response) -> {
                holder.location.setText(response.getPlace().getName());
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

        Observable<User> userObservable = getUserFromId(model.getOwnerUid());
        userObservable.subscribe(
                user -> holder.owner.setText(user.getFullName()),
                throwable -> Log.i(TAG, "Throwable " + throwable.getMessage()));


        userLocation.subscribe(
                location -> {
                    double distance = getDistanceInKilometers(model.getLatitude(), model.getLongitude(), location.getLatitude(), location.getLongitude());
                    holder.distance.setText(distance + " km");
                },
                throwable -> Log.i(TAG, "Throwable " + throwable.getMessage()));


        Log.i(TAG, "Place found");


        holder.date.setText(printDate(model.getTimestampCreated().toDate()));

        holder.subscribed.setCompoundDrawablesWithIntrinsicBounds(
                context.getDrawable(model.getSubscriberUids() != null && model.getSubscriberUids().contains(currentUserUid) ?
                        R.drawable.ic_check_success_green_24dp :
                        R.drawable.ic_close_red_24dp)
                , null, null, null);

        try {
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(model.getImageUrl());
            Glide.with(context)
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

    @NonNull
    @Override
    public SearchPartyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_party_list_item, parent, false);
        return new SearchPartyViewHolder(view);
    }

    public class SearchPartyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView title, reward, location, date, owner, subscribed, distance;
        private ImageView image;
        private ProgressBar recyclerItemProgress;

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

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onListItemCLick.onItemClick(getItem(getAdapterPosition()), getAdapterPosition());
        }
    }

    public interface OnListItemCLick{
        void onItemClick(SearchParty searchParty, int position);
    }

}
