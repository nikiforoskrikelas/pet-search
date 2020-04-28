package nk00322.surrey.petsearch.fragments.MeFragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.petsearch.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.rxjava3.core.Observable;
import nk00322.surrey.petsearch.FirestoreAdapter;
import nk00322.surrey.petsearch.FullscreenDisplaySearchParty;
import nk00322.surrey.petsearch.models.SearchParty;

import static nk00322.surrey.petsearch.utils.GeneralUtils.checkPermission;
import static nk00322.surrey.petsearch.utils.LocationUtils.API_KEY;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyPartiesFragment extends Fragment implements FirestoreAdapter.OnListItemCLick {
    private static final String TAG = "SearchPartiesFragment";

    private View view;
    private FirebaseAuth auth;
    private RecyclerView searchPartiesRecyclerView;
    private FirebaseUser currentUser;
    private Observable<Location> userLocation;
    private FirestoreAdapter adapter;

    public MyPartiesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_parties, container, false);
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        Places.initialize(getContext(), API_KEY);

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            userLocation = Observable.create(result ->
                    LocationServices.getFusedLocationProviderClient(getContext()).getLastLocation().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User location found");
                            if (task.getResult() == null) {
                                result.onError(new Exception("Location error"));
                            } else {
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


        searchPartiesRecyclerView = view.findViewById(R.id.my_parties_recycler_view);
        setupRecyclerView();

        return view;

    }

    private void setupRecyclerView() {

        Query query = FirebaseFirestore.getInstance()
                .collection("searchParties")
                .whereEqualTo("ownerUid", currentUser.getUid())
                .orderBy("timestampCreated", Query.Direction.DESCENDING);


        FirestoreRecyclerOptions<SearchParty> options = new FirestoreRecyclerOptions.Builder<SearchParty>()
                .setQuery(query, SearchParty.class)
                .build();

        adapter = new FirestoreAdapter(options, getContext(), userLocation, currentUser.getUid(), this);

        searchPartiesRecyclerView.setHasFixedSize(true);
        searchPartiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchPartiesRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(SearchParty searchParty, int position) {
        Log.d(TAG, "Item clicked: " + position + " and with title: " + searchParty.getTitle());
        DialogFragment dialogFragment = new FullscreenDisplaySearchParty(searchParty, currentUser.getUid());
        dialogFragment.show(getActivity().getSupportFragmentManager(), "FullscreenDisplaySearchParty");

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
