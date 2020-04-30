package nk00322.surrey.petsearch.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.petsearch.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Digits;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Pattern;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import nk00322.surrey.petsearch.CustomToast;
import nk00322.surrey.petsearch.ToastType;
import nk00322.surrey.petsearch.models.SearchParty;
import nk00322.surrey.petsearch.models.SearchedArea;
import nk00322.surrey.petsearch.models.Sighting;
import uk.co.mgbramwell.geofire.android.GeoFire;
import uk.co.mgbramwell.geofire.android.listeners.SetLocationListener;

import static android.app.Activity.RESULT_OK;
import static nk00322.surrey.petsearch.utils.FirebaseUtils.isLoggedIn;
import static nk00322.surrey.petsearch.utils.GeneralUtils.PICK_IMAGE_REQUEST;
import static nk00322.surrey.petsearch.utils.GeneralUtils.getFileExtension;
import static nk00322.surrey.petsearch.utils.GeneralUtils.setFocusableAndClickable;
import static nk00322.surrey.petsearch.utils.LocationUtils.AUTOCOMPLETE_REQUEST_CODE;
import static nk00322.surrey.petsearch.utils.LocationUtils.getLocationAutoCompleteIntent;
import static nk00322.surrey.petsearch.utils.ValidationUtils.DESCRIPTION_CHAR_LIMIT;
import static nk00322.surrey.petsearch.utils.ValidationUtils.clearTextInputEditTextErrors;
import static nk00322.surrey.petsearch.utils.ValidationUtils.setupTextInputLayoutValidator;


/**
 * A simple {@link Fragment} subclass.
 * <p>
 * Image upload adapted from https://www.youtube.com/watch?v=gqIWrNitbbk
 */
public class OrganizeFragment extends Fragment implements View.OnClickListener, Validator.ValidationListener, SetLocationListener {
    private static final String TAG = "OrganizeFragment";
    private View view;
    private FirebaseAuth auth;
    private StorageReference storageRef;
    private static Animation shakeAnimation;
    private long mLastClickTime = 0;

    @NotEmpty
    private TextInputEditText title, location;

    @NotEmpty
    @Pattern(regex = DESCRIPTION_CHAR_LIMIT, message = "Description must be less than 300 characters", sequence = 2)
    private TextInputEditText description;

    @NotEmpty
    @Digits(integer = 5, message = "Please use an integer with up to 5 digits", sequence = 2)
    private TextInputEditText reward;

    private ImageView photo;
    private Uri imageUri;

    private Button submit;
    private Validator validator;
    private String locationId;
    private double longitude;
    private double latitude;

    private DocumentReference currentUserReference;
    private ProgressBar uploadProgress;
    private FirebaseUser currentUser;
    private Drawable defaultImage;
    private Drawable errorImage;
    private StorageTask uploadTask;

    public OrganizeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_organize, container, false);
        if (!isLoggedIn()) {
            final NavController navController = Navigation.findNavController(view);
            FirebaseAuth.getInstance().signOut();
            navController.navigate(R.id.action_organizeFragment_to_welcomeFragment);
        }
        auth = FirebaseAuth.getInstance();
        validator = setupTextInputLayoutValidator(this, view);
        initViews();
        setListeners();
        return view;
    }

    private void initViews() {
        currentUser = auth.getCurrentUser();
        currentUserReference = FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid());

        storageRef = FirebaseStorage.getInstance().getReference("searchPartyImages");
        title = view.findViewById(R.id.organize_title);
        description = view.findViewById(R.id.organize_description);
        location = view.findViewById(R.id.last_location);
        reward = view.findViewById(R.id.reward);
        photo = view.findViewById(R.id.add_pet_image);
        submit = view.findViewById(R.id.organize_submit);
        uploadProgress = view.findViewById(R.id.upload_progress);
        defaultImage = ContextCompat.getDrawable(getContext(), R.drawable.add_photo);
        errorImage = ContextCompat.getDrawable(getContext(), R.drawable.add_photo_error);
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);

    }

    private void setListeners() {
        photo.setOnClickListener(this);
        location.setOnClickListener(this);
        submit.setOnClickListener(this);
        validator.setValidationListener(this);
    }

    @Override
    public void onClick(View view) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) { //To prevent double clicking
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        switch (view.getId()) {
            case R.id.add_pet_image:
                initiateUserInputImage();
                break;
            case R.id.last_location:
                startActivityForResult(getLocationAutoCompleteIntent(Objects.requireNonNull(
                        getContext()).getApplicationContext()), AUTOCOMPLETE_REQUEST_CODE);
                break;
            case R.id.organize_submit:
                if (uploadTask != null && uploadTask.isInProgress()) {
                    new CustomToast().showToast(getContext(), view, "Upload in progress", ToastType.ERROR, false);

                } else {
                    clearTextInputEditTextErrors(title, description, location, reward);
                    validator.validate();
                }
                break;
        }

    }

    private void initiateUserInputImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Responsible for retrieving result from autocomplete location
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                location.setText(place.getName());
                locationId = place.getId();
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                new CustomToast().showToast(getContext(), view, "Error with Google Maps", ToastType.ERROR, false);
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("ERROR AUTOCOMPLETE", status.getStatusMessage());
            }
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(photo);
        }
    }

    @Override
    public void onValidationSucceeded() {
        createSearchParty();
    }

    private void createSearchParty() {
        if (imageUri != null) {
            StorageReference searchPartyImageRef = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri, getContext()));
            new CustomToast().showToast(getContext(), view, "Image upload in progress. Please wait", ToastType.INFO, false);

            setFocusableAndClickable(false, title, description, reward);
            photo.setOnClickListener(null);
            photo.setClickable(false);
            submit.setOnClickListener(null);
            location.setOnClickListener(null);
            uploadProgress.setVisibility(View.VISIBLE);
            uploadProgress.startAnimation(shakeAnimation);

            uploadTask = searchPartyImageRef.putFile(imageUri).addOnSuccessListener((response) -> {
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    uploadProgress.setProgress(0);
                    uploadProgress.setVisibility(View.GONE);
                }, 3500); // delay reset by 5 seconds
                Log.i(TAG, "Image upload successful");

                ArrayList<String> subscriberUids = new ArrayList<>();
                subscriberUids.add(currentUser.getUid()); // users are subscribed to their own search parties by default

                ArrayList<Sighting> sightings = new ArrayList<>();
                ArrayList<SearchedArea> searchedAreas = new ArrayList<>();
                SearchParty searchParty = new SearchParty(title.getText().toString(), description.getText().toString(),
                        searchPartyImageRef.toString(), locationId, reward.getText().toString(), currentUser.getUid(), subscriberUids, latitude, longitude, false, sightings, searchedAreas);

                FirebaseFirestore.getInstance().collection("searchParties").add(searchParty).addOnCompleteListener(task -> { //todo fix
                    if (task.isSuccessful()) {
                        new GeoFire(FirebaseFirestore.getInstance().collection("searchParties")).setLocation(task.getResult().getId(), latitude, longitude, this);

                        new CustomToast().showToast(getContext(), view, "Search Party has been created", ToastType.SUCCESS, true);

                    } else
                        new CustomToast().showToast(getContext(), view, "Error while creating searh party", ToastType.SUCCESS, true);
                });


                title.setText("");
                description.setText("");
                location.setText("");
                reward.setText("");
                photo.setImageDrawable(defaultImage);
                photo.setOnClickListener(this);
                photo.setClickable(true);
                setFocusableAndClickable(true, title, description, reward);
                location.setOnClickListener(this);
                submit.setOnClickListener(this);
                locationId = null;
                imageUri = null;
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    new CustomToast().showToast(getContext(), view, "Image upload has failed", ToastType.ERROR, false);
                    Log.e(TAG, "Image upload unsuccessful: " + exception.getMessage());
                }
            }).addOnProgressListener((response) -> {
                double progress = (100.0 * response.getBytesTransferred() / response.getTotalByteCount());
                uploadProgress.setProgress((int) progress);
            });

        } else {
            photo.setImageDrawable(errorImage);
            photo.startAnimation(shakeAnimation);
            new CustomToast().showToast(getContext(), view, "Please choose an image", ToastType.ERROR, false);
        }
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(getContext());

            // Display error messages
            if (view instanceof TextInputEditText) {
                // this will get TextInputEditText parent which is TextInputLayout
                ((TextInputLayout) this.view.findViewById(view.getId()).getParent().getParent()).setError(message);
            } else {
                new CustomToast().showToast(getContext(), view, message, ToastType.ERROR, false);
            }
        }
        if (imageUri != null)
            photo.setImageDrawable(defaultImage);
        else {
            photo.setImageDrawable(errorImage);
            photo.startAnimation(shakeAnimation);
        }
    }

    @Override
    public void onCompleted(Exception exception) {
        Log.w(TAG, "onCompleted ", exception);
    }
}
