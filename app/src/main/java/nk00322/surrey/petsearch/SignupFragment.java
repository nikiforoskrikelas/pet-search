package nk00322.surrey.petsearch;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petsearch.R;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static nk00322.surrey.petsearch.Utils.API_KEY;
import static nk00322.surrey.petsearch.Utils.isEmailValid;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignupFragment extends Fragment implements View.OnClickListener {

    private final static int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static Animation shakeAnimation;
    private View view;
    private EditText fullName, email, mobileNumber, location, password, confirmPassword;
    private TextView signin;
    private Button signUpButton;
    private CheckBox terms_conditions;
    private ConstraintLayout signupLayout;

    public SignupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_signup, container, false);
        initViews();
        setListeners();
        // Inflate the layout for this fragment
        return view;
    }

    // Initialize all views
    private void initViews() {
        fullName = view.findViewById(R.id.fullName);
        email = view.findViewById(R.id.userEmail);
        mobileNumber = view.findViewById(R.id.mobileNumber);
        location = view.findViewById(R.id.location);
        password = view.findViewById(R.id.password);
        confirmPassword = view.findViewById(R.id.confirmPassword);
        signUpButton = view.findViewById(R.id.signUpBtn);
        signin = view.findViewById(R.id.already_user);
        terms_conditions = view.findViewById(R.id.terms_conditions);
        signupLayout = view.findViewById(R.id.signup_layout);
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);

        ColorStateList textSelector = getResources().getColorStateList(R.color.text_selector);

        signin.setTextColor(textSelector);
        terms_conditions.setTextColor(textSelector);

    }

    // Set Listeners
    private void setListeners() {
        signUpButton.setOnClickListener(this);
        signin.setOnClickListener(this);
        location.setOnClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                location.setText(place.getName());
                //TODO store ID and or name or other data required to user
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                new CustomToast().Show_Toast(Objects.requireNonNull(getActivity()), view, "Error with Google Maps");
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("ERROR AUTOCOMPLETE", status.getStatusMessage());
            }
        }
    }


    @Override
    public void onClick(View v) {
        final NavController navController = Navigation.findNavController(view);

        switch (v.getId()) {
            case R.id.location:
                launchLocationAutoComplete();
                break;
            case R.id.signUpBtn:
                // Call checkValidation method
                checkValidation(navController);
                break;

            case R.id.already_user:
                // Replace login fragment
                navController.navigate(R.id.action_signupFragment_to_signinFragment);
                break;
        }
    }

    private void launchLocationAutoComplete() {
        Places.initialize(Objects.requireNonNull(getContext()).getApplicationContext(), API_KEY);
        // Create a new Places client instance

        // Set the fields to specify which types of place data to return after the user has made a selection.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(getContext());
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    // Check Validation Method
    private void checkValidation(NavController navController) {

        // Get all edittext texts
        String getFullName = fullName.getText().toString();
        String getEmail = email.getText().toString();
        String getMobileNumber = mobileNumber.getText().toString();
        String getLocation = location.getText().toString();
        String getPassword = password.getText().toString();
        String getConfirmPassword = confirmPassword.getText().toString();

        // Check if all strings are null or not
        if (getFullName.equals("") || getFullName.length() == 0
                || getEmail.equals("") || getEmail.length() == 0
                || getMobileNumber.equals("") || getMobileNumber.length() == 0
                || getLocation.equals("") || getLocation.length() == 0
                || getPassword.equals("") || getPassword.length() == 0
                || getConfirmPassword.equals("")
                || getConfirmPassword.length() == 0) {
            signupLayout.startAnimation(shakeAnimation);
            new CustomToast().Show_Toast(Objects.requireNonNull(getActivity()), view, "All fields are required.");
        }

        // Check if email id valid or not
        else if (!isEmailValid(getEmail)) {
            email.startAnimation(shakeAnimation);
            new CustomToast().Show_Toast(Objects.requireNonNull(getActivity()), view, "Your Email  is Invalid.");
        }
        // Check if both password should be equal
        else if (!getConfirmPassword.equals(getPassword)) {
            password.startAnimation(shakeAnimation);
            confirmPassword.startAnimation(shakeAnimation);
            new CustomToast().Show_Toast(Objects.requireNonNull(getActivity()), view, "Passwords do not match");
        }
        // Make sure user should check Terms and Conditions checkbox
        else if (!terms_conditions.isChecked()) {
            terms_conditions.startAnimation(shakeAnimation);
            new CustomToast().Show_Toast(Objects.requireNonNull(getActivity()), view, "Please select Terms and Conditions.");
        }

        // Else TODO SIGNUP
        else {
            Toast.makeText(getActivity(), "Do SignUp.", Toast.LENGTH_SHORT).show();
            navController.navigate(R.id.action_signupFragment_to_mapFragment);
        }
    }
}