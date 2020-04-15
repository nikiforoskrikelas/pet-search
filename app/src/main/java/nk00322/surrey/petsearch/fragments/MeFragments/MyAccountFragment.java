package nk00322.surrey.petsearch.fragments.MeFragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.example.petsearch.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Pattern;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import nk00322.surrey.petsearch.CustomToast;
import nk00322.surrey.petsearch.ToastType;
import nk00322.surrey.petsearch.models.User;

import static android.app.Activity.RESULT_OK;
import static android.text.TextUtils.isEmpty;
import static nk00322.surrey.petsearch.utils.FirebaseUtils.getDatabaseReference;
import static nk00322.surrey.petsearch.utils.GeneralUtils.getViewsByTag;
import static nk00322.surrey.petsearch.utils.GeneralUtils.slideView;
import static nk00322.surrey.petsearch.utils.GeneralUtils.textViewSlideIn;
import static nk00322.surrey.petsearch.utils.GeneralUtils.textViewSlideOut;
import static nk00322.surrey.petsearch.utils.LocationUtils.API_KEY;
import static nk00322.surrey.petsearch.utils.LocationUtils.AUTOCOMPLETE_REQUEST_CODE;
import static nk00322.surrey.petsearch.utils.LocationUtils.getLocationAutoCompleteIntent;
import static nk00322.surrey.petsearch.utils.ValidationUtils.EMAIL_REGEX;
import static nk00322.surrey.petsearch.utils.ValidationUtils.clearTextInputEditTextErrors;
import static nk00322.surrey.petsearch.utils.ValidationUtils.setupTextInputLayoutValidator;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyAccountFragment extends Fragment implements View.OnClickListener, Validator.ValidationListener {
    private static final String TAG = "MyAccountFragment";
    private static Animation slideOut, slideIn, slideOutUp, slideInUp;
    private View view, optionsView;
    private TextView editProfile, signOut, deleteAccount, fullNameText, emailText, locationText, phoneText;
    private FirebaseAuth mAuth;
    private ImageView backButton, addPhoto;
    private int originalViewYPosition;
    private float originalProfileYPosition;
    private NestedScrollView scrollView;
    private View topView;
    private Validator validator;
    private String locationId;

    @NotEmpty
    private TextInputEditText fullName, location;

    @NotEmpty(sequence = 1)
    @Pattern(regex = EMAIL_REGEX, message = "Invalid email", sequence = 2)
    private TextInputEditText email;

    @NotEmpty(sequence = 1)
    @Pattern(regex = "^\\s?((\\+[1-9]{1,4}[ \\-]*)|(\\([0-9]{2,3}\\)[ \\-]*)|([0-9]{2,4})[ \\-]*)*?[0-9]{3,4}?[ \\-]*[0-9]{3,4}?\\s?",
            message = "Invalid Phone Number", sequence = 2)
    private TextInputEditText mobileNumber;

    @Pattern(regex = "^$|(?=.*[a-z])(?=.*[A-Z])(?=.*[\\d]).+", message = "Invalid Password")
    private TextInputEditText newPassword;

    private TextInputEditText confirmNewPassword;

    private String userMobileNumber, userLocationId;
    private Button saveChanges;
    private FirebaseUser currentUser;
    private DatabaseReference usersReference;
    private ConstraintLayout constraintLayout;
    private String userDateCreated = "";

    public MyAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_account, container, false);
        mAuth = FirebaseAuth.getInstance();
        validator = setupTextInputLayoutValidator(validator, this, view);

        initViews();
        setListeners();
        return view;

    }

    private void initViews() {
        currentUser = mAuth.getCurrentUser();
        usersReference = getDatabaseReference().child("user").child(currentUser.getUid());

        editProfile = view.findViewById(R.id.edit_profile);
        signOut = view.findViewById(R.id.sign_out);
        deleteAccount = view.findViewById(R.id.delete_account);
        optionsView = view.findViewById(R.id.options_view);
        slideOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_right);
        slideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
        slideOutUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_up);
        slideInUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_up);

        backButton = view.findViewById(R.id.back_to_profile);
        addPhoto = view.findViewById(R.id.add_photo);
        scrollView = view.findViewById(R.id.edit_scrollview);
        topView = view.findViewById(R.id.view);

        constraintLayout = view.findViewById(R.id.edit_constraint_layout);

        fullNameText = view.findViewById(R.id.full_name_text);
        emailText = view.findViewById(R.id.email_text);
        locationText = view.findViewById(R.id.location_text);
        phoneText = view.findViewById(R.id.phone_text);

        fullNameText.setText(currentUser.getDisplayName());
        emailText.setText(currentUser.getEmail());

        fullName = view.findViewById(R.id.edit_full_name);
        email = view.findViewById(R.id.edit_email);
        mobileNumber = view.findViewById(R.id.edit_mobile_number);
        location = view.findViewById(R.id.edit_location);
        newPassword = view.findViewById(R.id.edit_password);
        confirmNewPassword = view.findViewById(R.id.edit_confirm_password);
        saveChanges = view.findViewById(R.id.save_changes);
        newPassword = view.findViewById(R.id.edit_password);
        confirmNewPassword = view.findViewById(R.id.edit_confirm_password);

        ColorStateList textSelector = getResources().getColorStateList(R.color.text_selector_grey_text);
        editProfile.setTextColor(textSelector);
        signOut.setTextColor(textSelector);
        deleteAccount.setTextColor(textSelector);

    }

    private void setListeners() {
        editProfile.setOnClickListener(this);
        signOut.setOnClickListener(this);
        deleteAccount.setOnClickListener(this);
        backButton.setOnClickListener(this);
        addPhoto.setOnClickListener(this);
        saveChanges.setOnClickListener(this);
        validator.setValidationListener(this);
        location.setOnClickListener(this);
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataSnapshot.getValue();
                User user = dataSnapshot.getValue(User.class);
                userMobileNumber = user.getMobileNumber();
                userLocationId = user.getLocationId();
                userDateCreated = user.getDateCreated();
                if (userLocationId != null) {

                    Places.initialize(getContext(), API_KEY);

                    // Specify the fields to return.
                    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

                    FetchPlaceRequest request = FetchPlaceRequest.newInstance(userLocationId, placeFields);
                    PlacesClient placesClient = Places.createClient(getContext());
                    Runnable fetchUserInfo = () -> placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                        locationText.setText(response.getPlace().getName());
                        Log.i(TAG, "Place found");
                    }).addOnFailureListener((exception) -> {
                        if (exception instanceof ApiException) {
                            Log.e(TAG, "Place not found: " + exception.getMessage());
                            locationText.setText("N/A");
                        }
                    });
                    Thread thread = new Thread(fetchUserInfo);
                    thread.start();


                } else {
                    locationText.setText("N/A");
                    Log.i(TAG, "No location stored in user");
                }

                view.findViewById(R.id.loading_location).setVisibility(View.GONE);
                locationText.setVisibility(View.VISIBLE);
                editProfile.setFocusable(true);
                editProfile.setClickable(true);
                editProfile.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
                phoneText.setText(userMobileNumber);
                view.findViewById(R.id.loading_phone).setVisibility(View.GONE);
                phoneText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MyAccountFragment", databaseError.toString());

            }
        });


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.edit_profile:
                if (!isEmpty(locationText.getText().toString()) && !isEmpty(phoneText.getText().toString())) {
                    userEditProfile();
                }
                break;
            case R.id.edit_location:
                startActivityForResult(getLocationAutoCompleteIntent(Objects.requireNonNull(getContext()).getApplicationContext()), AUTOCOMPLETE_REQUEST_CODE);
                break;
            case R.id.back_to_profile:
                cancelEditProfile();
                break;
            case R.id.add_photo:
                addPhotoPopup();
                break;
            case R.id.save_changes:
                clearTextInputEditTextErrors(fullName, email, mobileNumber, location);
                validator.validate();
                break;
            case R.id.sign_out:
                userSignOut();
                break;
            case R.id.delete_account:
                userDeleteAccount();
                break;

        }
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
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                new CustomToast().showToast(Objects.requireNonNull(getActivity()), view, "Error with Google Maps", ToastType.ERROR, false);
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("ERROR AUTOCOMPLETE", status.getStatusMessage());
            }
        }
    }

    private void userEditProfile() {
        setupEditProfileAnimations();

        fullName.setText(fullNameText.getText().toString());
        email.setText(emailText.getText().toString());
        mobileNumber.setText(phoneText.getText().toString());
        location.setText(locationText.getText().toString());


    }

    private void setupEditProfileAnimations() {
        optionsView.startAnimation(slideOut);

        getViewsByTag((ViewGroup) view, "textView").forEach((n) -> textViewSlideOut(n, getActivity()));

        getViewsByTag((ViewGroup) view, "divider").forEach((n) -> n.setVisibility(View.GONE));

        getViewsByTag((ViewGroup) view, "text").forEach((n) -> n.startAnimation(slideOut));

        ImageView profileImage = view.findViewById(R.id.profile_image);
        originalViewYPosition = topView.getLayoutParams().height;
        originalProfileYPosition = profileImage.getY();

        slideView(topView, originalViewYPosition, view.getHeight());
        profileImage.animate().y(10f).setDuration(500);

        backButton.setVisibility(View.VISIBLE);
        backButton.startAnimation(slideInUp);
        backButton.setClickable(true);
        backButton.setFocusable(true);

        addPhoto.setVisibility(View.VISIBLE);
        addPhoto.startAnimation(slideInUp);
        addPhoto.setClickable(true);
        addPhoto.setFocusable(true);

        ViewGroup group = view.findViewById(R.id.edit_constraint_layout);
        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
            View view = group.getChildAt(i);
            if (view instanceof TextInputLayout) {
                view.startAnimation(slideInUp);
                view.setVisibility(View.VISIBLE);
                ((TextInputLayout) view).getEditText().setClickable(true);
                ((TextInputLayout) view).getEditText().setFocusable(true);
                ((TextInputLayout) view).getEditText().setFocusableInTouchMode(true);
                ((TextInputLayout) view).getEditText().setCursorVisible(true);

                if (((TextInputLayout) view).getEditText().getTag() != null &&
                        ((TextInputLayout) view).getEditText().getTag().equals("locationInput")) {
                    ((TextInputLayout) view).getEditText().setFocusable(false);
                    ((TextInputLayout) view).getEditText().setFocusableInTouchMode(false);
                    ((TextInputLayout) view).getEditText().setCursorVisible(false);
                }
            }
        }

        editProfile.bringToFront();
        saveChanges.setVisibility(View.VISIBLE);
        saveChanges.startAnimation(slideInUp);
        scrollView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.background_color));
    }

    private void cancelEditProfile() {
        getViewsByTag((ViewGroup) view, "textView").forEach((n) -> textViewSlideIn(n, getActivity()));

        optionsView.startAnimation(slideIn);

        getViewsByTag((ViewGroup) view, "divider").forEach((n) -> n.setVisibility(View.VISIBLE));

        getViewsByTag((ViewGroup) view, "text").forEach((n) -> n.startAnimation(slideIn));

        View topView = view.findViewById(R.id.view);
        ImageView profileImage = view.findViewById(R.id.profile_image);

        slideView(topView, topView.getLayoutParams().height, originalViewYPosition);
        profileImage.animate().y(originalProfileYPosition).setDuration(500);

        backButton.setClickable(false);
        backButton.setFocusable(false);
        backButton.startAnimation(slideOutUp);
        backButton.setVisibility(View.GONE);


        addPhoto.setClickable(false);
        addPhoto.setFocusable(false);
        addPhoto.startAnimation(slideOutUp);
        addPhoto.setVisibility(View.GONE);

        ViewGroup group = view.findViewById(R.id.edit_constraint_layout);
        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
            View view = group.getChildAt(i);
            if (view instanceof TextInputLayout) {
                view.startAnimation(slideOutUp);
                view.setVisibility(View.GONE);
                ((TextInputLayout) view).getEditText().setClickable(false);
                ((TextInputLayout) view).getEditText().setFocusable(false);
                ((TextInputLayout) view).getEditText().setFocusableInTouchMode(false);
                ((TextInputLayout) view).getEditText().setCursorVisible(false);
            }

        }
        saveChanges.setVisibility(View.GONE);
        saveChanges.startAnimation(slideOutUp);
        scrollView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.background));

    }

    private void addPhotoPopup() {

    }

    private void submitProfileChanges() {
        if (fullName.getText().toString().equals(fullNameText.getText()) &&
                email.getText().toString().equals(emailText.getText()) &&
                mobileNumber.getText().toString().equals(phoneText.getText()) &&
                location.getText().toString().equals(locationText.getText()) &&
                isEmpty(newPassword.getText().toString()) &&
                isEmpty(confirmNewPassword.getText().toString())) { //if no changes were made, do not update user
            cancelEditProfile();
            new CustomToast().showToast(Objects.requireNonNull(getActivity()), view, "No changes were made", ToastType.INFO, false);
        } else {
            reAuthenticateAndSubmit();


        }


    }

    private void updateUser() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName.getText().toString())
                    .setPhotoUri(Uri.parse("https://picsum.photos/200")) // TODO CHANGE TO INPUT PICTURE
                    .build();
            firebaseUser.updateProfile(profileUpdates);

            User user = new User(mobileNumber.getText().toString(), locationId, userDateCreated);

            getDatabaseReference().child("user").child(firebaseUser.getUid()).setValue(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    fullNameText.setText(fullName.getText().toString());
                    locationText.setText(location.getText().toString());
                    phoneText.setText(mobileNumber.getText().toString());
                    //TODO also update image if edited

                    new CustomToast().showToast(Objects.requireNonNull(getActivity()), view, "User information updated successfully", ToastType.SUCCESS, false);
                } else {
                    Log.e(TAG, "User information was not updated." + task.getException().getMessage());

                    new CustomToast().showToast(Objects.requireNonNull(getActivity()), view, "Error while updating account", ToastType.ERROR, false);
                }
                if (!email.getText().toString().equals(emailText.getText())) {
                    updateEmail(firebaseUser);
                }

                if (!isEmpty(newPassword.getText().toString()) && newPassword.getText().toString().equals(confirmNewPassword.getText().toString())) {
                    updatePassword(firebaseUser);
                }

                cancelEditProfile();

            });

        } else {
            new CustomToast().showToast(Objects.requireNonNull(getActivity()), view, "Error while updating account", ToastType.ERROR, false);
            cancelEditProfile();
        }
    }

    private void updatePassword(FirebaseUser firebaseUser) {

        firebaseUser.updatePassword(newPassword.getText().toString()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                newPassword.setText("");
                confirmNewPassword.setText("");
                Log.d(TAG, "User password updated.");
            }
        }).addOnFailureListener((exception) -> {
            Log.e(TAG, "User password was not updated." + exception.getMessage());
            new CustomToast().showToast(Objects.requireNonNull(getActivity()), view,
                    "Error while updating password", ToastType.ERROR, true);
        });


    }

    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.updateEmail(email.getText().toString()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "User email address updated.");
                emailText.setText(email.getText().toString());
                final NavController navController = Navigation.findNavController(view);
                FirebaseAuth.getInstance().signOut();
                firebaseUser.sendEmailVerification();
                navController.navigate(R.id.action_meFragment_to_welcomeFragment);
                new CustomToast().showToast(Objects.requireNonNull(getActivity()), view,
                        "Email changed. Please check your email, a verification link has been sent.", ToastType.INFO, true);

            }
        }).addOnFailureListener((exception) -> {
            Log.e(TAG, "User email address was not updated." + exception.getMessage());
            new CustomToast().showToast(Objects.requireNonNull(getActivity()), view,
                    exception.getMessage().equals("The email address is already in use by another account.") ?
                            "The email address is already in use by another account. Other changes have been saved." :
                            "Error while updating email", ToastType.ERROR, true);
            cancelEditProfile();
        });
    }

    private void reAuthenticateAndSubmit() {
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.password_input_dialog, (ViewGroup) getView(), false);
        final EditText input = viewInflated.findViewById(R.id.password_input);
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Re-authenticate")
                .setView(viewInflated)
                .setMessage("Please verify your password to save your changes")
                .setPositiveButton("Submit", (dialog, id) -> {
                    final FirebaseUser user = mAuth.getCurrentUser();
                    if (!input.getText().toString().isEmpty()) {
                        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), input.getText().toString());
                        user.reauthenticate(credential)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        updateUser();
                                    } else {
                                        new CustomToast().showToast(Objects.requireNonNull(getActivity()), view, "Incorrect password, please try again", ToastType.ERROR, false);
                                    }
                                });

                    } else {
                        new CustomToast().showToast(Objects.requireNonNull(getActivity()), view, "Password can not be empty", ToastType.ERROR, false);
                        input.setError("Password can not be empty");
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> {

                })
                .show();
    }

    private void userSignOut() {
        final NavController navController = Navigation.findNavController(view);
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Sign out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out", (dialog, id) -> {
                    FirebaseAuth.getInstance().signOut();
                    navController.navigate(R.id.action_meFragment_to_welcomeFragment);
                })
                .setNegativeButton("Cancel", (dialog, id) -> {

                })
                .show();
    }

    private void userDeleteAccount() {
        final NavController navController = Navigation.findNavController(view);

// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.password_input_dialog, (ViewGroup) getView(), false);
        final EditText input = viewInflated.findViewById(R.id.password_input);

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Delete Account")
                .setView(viewInflated)
                .setMessage("Please verify your password first")
                .setPositiveButton("Delete", (dialog, id) -> {
                    final FirebaseUser user = mAuth.getCurrentUser();
                    if (!input.getText().toString().isEmpty()) {
                        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), input.getText().toString());
                        user.reauthenticate(credential)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        user.delete()
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        navController.navigate(R.id.action_meFragment_to_welcomeFragment);
                                                        new CustomToast().showToast(Objects.requireNonNull(getActivity()), view, "Account Deleted Successfully", ToastType.SUCCESS, false);
                                                    } else {
                                                        new CustomToast().showToast(Objects.requireNonNull(getActivity()), view, "Authentication Error: Account was not deleted", ToastType.ERROR, false);
                                                    }
                                                });

                                    } else {
                                        new CustomToast().showToast(Objects.requireNonNull(getActivity()), view, "Incorrect password, please try again", ToastType.ERROR, false);
                                    }
                                });

                    } else {
                        new CustomToast().showToast(Objects.requireNonNull(getActivity()), view, "Password can not be empty", ToastType.ERROR, false);
                        input.setError("Password can not be empty");
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> {

                })
                .show();


    }

    /**
     * Called when all {@link Rule}s pass.
     */
    @Override
    public void onValidationSucceeded() {
        if (!newPassword.getText().toString().equals(confirmNewPassword.getText().toString())) {
            ((TextInputLayout) newPassword.getParent().getParent()).setError(null);
            ((TextInputLayout) confirmNewPassword.getParent().getParent()).setError("Passwords don't match");
        } else {
            ((TextInputLayout) confirmNewPassword.getParent().getParent()).setError(null);
            submitProfileChanges();

        }
    }

    /**
     * Called when one or several {@link Rule}s fail.
     *
     * @param errors List containing references to the {@link View}s and
     *               {@link Rule}s that failed.
     */
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
                new CustomToast().showToast(Objects.requireNonNull(getActivity()), view, message, ToastType.ERROR, false);
            }
        }
    }
}
