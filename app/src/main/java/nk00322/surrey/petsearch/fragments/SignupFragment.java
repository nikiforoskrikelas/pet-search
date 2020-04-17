package nk00322.surrey.petsearch.fragments;

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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.petsearch.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Checked;
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.mobsandgeeks.saripaar.annotation.Pattern;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import nk00322.surrey.petsearch.CustomToast;
import nk00322.surrey.petsearch.ToastType;
import nk00322.surrey.petsearch.models.User;

import static android.app.Activity.RESULT_OK;
import static nk00322.surrey.petsearch.utils.FirebaseUtils.getDatabaseReference;
import static nk00322.surrey.petsearch.utils.GeneralUtils.getNow;
import static nk00322.surrey.petsearch.utils.LocationUtils.AUTOCOMPLETE_REQUEST_CODE;
import static nk00322.surrey.petsearch.utils.LocationUtils.getLocationAutoCompleteIntent;
import static nk00322.surrey.petsearch.utils.ValidationUtils.EMAIL_REGEX;
import static nk00322.surrey.petsearch.utils.ValidationUtils.PASSWORD_FORMAT_ERROR;
import static nk00322.surrey.petsearch.utils.ValidationUtils.areAllFieldsCompleted;
import static nk00322.surrey.petsearch.utils.ValidationUtils.clearTextInputEditTextErrors;
import static nk00322.surrey.petsearch.utils.ValidationUtils.setupTextInputLayoutValidator;


/**
 * Fragment responsible for user sign up
 * Uses Saripaar annotation based field validation
 */
public class SignupFragment extends Fragment implements View.OnClickListener, Validator.ValidationListener {
    private static String TAG = "SignupFragment";
    private static Animation shakeAnimation;
    private View view;

    @NotEmpty
    private TextInputEditText fullName;

    @NotEmpty(sequence = 1)
    @Pattern(regex = EMAIL_REGEX, message = "Invalid email", sequence = 2)
    private TextInputEditText email;

    @NotEmpty(sequence = 1)
    @Pattern(regex = "^\\s?((\\+[1-9]{1,4}[ \\-]*)|(\\([0-9]{2,3}\\)[ \\-]*)|([0-9]{2,4})[ \\-]*)*?[0-9]{3,4}?[ \\-]*[0-9]{3,4}?\\s?",
            message = "Invalid Phone Number", sequence = 2)
    private TextInputEditText mobileNumber;

    @NotEmpty
    private TextInputEditText location;

    @Password(min = 8, scheme = Password.Scheme.ALPHA_NUMERIC_MIXED_CASE, message = PASSWORD_FORMAT_ERROR)
    private TextInputEditText password;

    @ConfirmPassword
    private TextInputEditText confirmPassword;

    private TextView signin;
    private Button signUpButton;

    @Checked(message = "You must agree to the terms.")
    private CheckBox terms_conditions;

    private ConstraintLayout signupLayout;
    private Validator validator;
    private ImageView closeActivityImage;

    private FirebaseAuth auth;
    private String locationId;
    public SignupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_signup, container, false);
        initViews();
        validator = setupTextInputLayoutValidator(this, view);
        setListeners();
        auth = FirebaseAuth.getInstance();
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            new CustomToast().showToast(getContext(), view, "User already signed in", ToastType.INFO, false);
            Navigation.findNavController(view).navigate(R.id.action_signupFragment_to_mapFragment);
        }
    }

    // Initialize all views
    private void initViews() {
        fullName = view.findViewById(R.id.fullName);
        email = view.findViewById(R.id.userEmail);
        mobileNumber = view.findViewById(R.id.mobileNumber);
        location = view.findViewById(R.id.location);
        password = view.findViewById(R.id.password);
        confirmPassword = view.findViewById(R.id.confirmPassword);
        closeActivityImage = view.findViewById(R.id.close_activity);
        signUpButton = view.findViewById(R.id.signUpBtn);
        signin = view.findViewById(R.id.already_user);
        terms_conditions = view.findViewById(R.id.terms_conditions);
        signupLayout = view.findViewById(R.id.signup_layout);
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);

        //TODO REMOVE - ONLY FOR TESTING
        int rand = new Random().nextInt(10000);
        fullName.setText("Test user");
        email.setText("test"+rand+"@iillii.org"); // https://www.fakemail.net/
        mobileNumber.setText("555555555");
        location.setText("TEST LOCATION");
        locationId = "ChIJZxWJ268aDTkRhwqTHHNw0hA";
        password.setText("1234qwerQWER");
        confirmPassword.setText("1234qwerQWER");
        //TODO REMOVE - ONLY FOR TESTING

        ColorStateList textSelector = getResources().getColorStateList(R.color.text_selector);

        signin.setTextColor(textSelector);
        terms_conditions.setTextColor(textSelector);

    }

    // Set Listeners
    private void setListeners() {
        closeActivityImage.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
        signin.setOnClickListener(this);
        location.setOnClickListener(this);
        validator.setValidationListener(this);
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
                //TODO store ID and or name or other data required to user
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                new CustomToast().showToast(getContext(), view, "Error with Google Maps", ToastType.ERROR, false);
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("ERROR AUTOCOMPLETE", status.getStatusMessage());
            }
        }
    }

    @Override
    public void onClick(View v) {
        final NavController navController = Navigation.findNavController(view);

        switch (v.getId()) {
            case R.id.close_activity:
                navController.navigate(R.id.action_signupFragment_to_welcomeFragment);
                break;
            case R.id.location:
                startActivityForResult(getLocationAutoCompleteIntent(Objects.requireNonNull(getContext()).getApplicationContext()), AUTOCOMPLETE_REQUEST_CODE);
                break;
            case R.id.signUpBtn:
                if (!areAllFieldsCompleted(fullName, email, mobileNumber, location, password, confirmPassword)) {
                    signupLayout.startAnimation(shakeAnimation);
                    new CustomToast().showToast(getContext(), view, "All fields are required.", ToastType.ERROR, false);
                    break;
                }
                clearTextInputEditTextErrors(fullName, email, mobileNumber, location, password, confirmPassword);

                validator.validate();
                break;
            case R.id.already_user:
                // Replace login fragment
                navController.navigate(R.id.action_signupFragment_to_signinFragment);
                break;
        }
    }


    @Override
    public void onValidationSucceeded() {
        createAccount();
    }

    private void createAccount() {
        final String email = this.email.getText().toString();
        String password = this.password.getText().toString();
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success");
                    createUser();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            final NavController navController = Navigation.findNavController(view);

            @Override
            public void onFailure(@NonNull Exception e) {
                String message = "";
                if (e instanceof FirebaseAuthUserCollisionException) {
                    message = "Email already in use by another account.";
                } else {
                    new CustomToast().showToast(getContext(), view, e.getMessage(), ToastType.ERROR, false);
                    navController.navigate(R.id.action_signupFragment_to_welcomeFragment);
                }
                new CustomToast().showToast(getContext(), view, message, ToastType.ERROR, false);
            }
        });
    }

    private void createUser() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName.getText().toString())
                    .setPhotoUri(Uri.parse("https://picsum.photos/200")) // default picture
                    .build();
            firebaseUser.updateProfile(profileUpdates);
            firebaseUser.sendEmailVerification();

            User user = new User(mobileNumber.getText().toString(), locationId, getNow());

            getDatabaseReference().child("user").child(firebaseUser.getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                NavController navController = Navigation.findNavController(view);
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        //the Firebase method instantly logs in the user after sign up
                        FirebaseAuth.getInstance().signOut();
                        new CustomToast().showToast(getContext(), view, "Account created successfully", ToastType.SUCCESS, false);
                        SignupFragmentDirections.ActionSignupFragmentToSigninFragment action =
                                SignupFragmentDirections.actionSignupFragmentToSigninFragment();
                        action.setRegisteredEmail(email.getText().toString());
                        navController.navigate(action);
                    }else{
                        FirebaseAuth.getInstance().signOut();
                        new CustomToast().showToast(getContext(), view, "Error while creating account", ToastType.ERROR, false);
                        navController.navigate(R.id.action_signupFragment_to_welcomeFragment);

                    }

                }
            });

        }else{
            new CustomToast().showToast(getContext(), view, "Error while creating account", ToastType.ERROR, false);
            Navigation.findNavController(view).navigate(R.id.action_signupFragment_to_welcomeFragment);
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
    }
}