package nk00322.surrey.petsearch.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.core.Observable;
import nk00322.surrey.petsearch.models.SearchParty;
import nk00322.surrey.petsearch.models.User;

public class FirebaseUtils {
    private static final String TAG = "FirebaseUtils";

    public static DatabaseReference getDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference();
    }
    public static AtomicInteger ACTIVE_SEARCH_PARTY_LISTENER_COUNTER;

    public static boolean isLoggedIn() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null;
    }

    DatabaseReference mDatabaseReference;
    Boolean saved = null;


    ArrayList<User> mClients = new ArrayList<>();

    public FirebaseUtils(DatabaseReference databaseReference) {
        mDatabaseReference = databaseReference;
    }


    //IMPLEMENT FETCH DATA AND FILL ARRAYLIST
    private void fetchClientData(DataSnapshot dataSnapshot) {
        mClients.clear();
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            User client = snapshot.getValue(User.class);
            mClients.add(client);
        }
    }

    //IMPLEMENT FETCH DATA AND FILL ARRAYLIST
    public ArrayList<User> retrieveClients() {
        mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                fetchClientData(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                fetchClientData(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return mClients;
    }


    public static Observable<User> getUserFromId(String userId) {
        return Observable.create(result -> FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "Document found");
                            result.onNext(document.toObject(User.class)); // return user to observable
                        } else {
                            Log.d(TAG, "No such document");
                            result.onNext(null);
                        }

                    } else {
                        Log.w(TAG, "Error getting document.", task.getException());
                        result.onError(task.getException());
                    }
                }));
    }

    public static Observable<Boolean> deleteUserWithId(String userId) {
        return Observable.create(result -> FirebaseFirestore.getInstance().collection("users").document(userId).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User successfully deleted!");
                    result.onNext(true);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error deleting document", e);
                    result.onNext(false);
                }));
    }

    public static Observable<Boolean> deleteSearchPartyWithId(String searchPartyId) {
        return Observable.create(result -> FirebaseFirestore.getInstance().collection("searchParties").document(searchPartyId).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Search Party successfully deleted!");
                    result.onNext(true);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error deleting document", e);
                    result.onNext(false);
                }));
    }


    public static void deleteAllUserSearchParties(String userUid) {
        FirebaseFirestore.getInstance().collection("searchParties").whereEqualTo("ownerUid", userUid).get()
                .addOnSuccessListener(task -> {
                    for (DocumentSnapshot doc : task.getDocuments()) {
                        doc.getReference().delete();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error getting search parties.", e);
                });
    }

    public static void deleteAllUserSearchPartySubscriptions(String userUid) {
        FirebaseFirestore.getInstance().collection("searchParties").whereArrayContains("subscriberUids", userUid).get()
                .addOnSuccessListener(task -> {
                    for (DocumentSnapshot doc : task.getDocuments()) {
                        doc.getReference().update("subscriberUids", FieldValue.arrayRemove(userUid));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error getting search parties.", e);
                });
    }

    public static Observable<ArrayList<SearchParty>> getUserSubscriptions(String id) {
        Query query = FirebaseFirestore.getInstance().collection("searchParties")
                .whereArrayContains("subscriberUids", id);

        return Observable.create(result -> query.get()
                .addOnSuccessListener(task -> {
                    ArrayList<SearchParty> searchParties = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getDocuments())
                        searchParties.add(doc.toObject(SearchParty.class));

                    result.onNext(searchParties);

                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error getting search parties.", e);
                    result.onError(e);
                }));
    }

    public static Observable<SearchParty> getSearchPartyUpdates(SearchParty searchParty) {
        if (searchParty != null) {
            ACTIVE_SEARCH_PARTY_LISTENER_COUNTER = new AtomicInteger(0);

            CollectionReference searchPartiesRef = FirebaseFirestore.getInstance().collection("searchParties");

            Query searchPartiesQuery = searchPartiesRef
                    .whereEqualTo("locationId", searchParty.getLocationId())
                    .whereEqualTo("ownerUid", searchParty.getOwnerUid())
                    .whereEqualTo("timestampCreated", searchParty.getTimestampCreated());


            return Observable.create(result -> searchPartiesQuery.addSnapshotListener((snapshot, e) -> {
                ACTIVE_SEARCH_PARTY_LISTENER_COUNTER.getAndIncrement();
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    result.onError(e);
                    return;
                }
                if (snapshot != null && !snapshot.isEmpty()) {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        SearchParty i = doc.toObject(SearchParty.class);
                        if (Objects.requireNonNull(i).equals(searchParty)) {
                            Log.d(TAG, "Search Party getSearchPartyUpdates successful");
                            result.onNext(i);
                        }
                    }
                    Log.d(TAG, "Current data: " + snapshot);

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }));

        }
        return null;
    }
}


