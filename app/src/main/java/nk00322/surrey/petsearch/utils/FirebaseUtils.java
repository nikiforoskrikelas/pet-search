package nk00322.surrey.petsearch.utils;

import android.util.Log;

import com.example.petsearch.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;
import nk00322.surrey.petsearch.models.SearchParty;
import nk00322.surrey.petsearch.models.User;

public class FirebaseUtils {

    public static DatabaseReference getDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference();
    }

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
        return Observable.create(e -> getDatabaseReference().child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);
                        e.onNext(user);
                        // ...
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("TAG", "getUser:onCancelled", databaseError.toException());
                        e.onError(databaseError.toException());
                        // ...
                    }
                }));
    }

}


