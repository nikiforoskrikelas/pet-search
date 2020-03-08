package nk00322.surrey.petsearch.fragments;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petsearch.R;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Checked;
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.mobsandgeeks.saripaar.annotation.Pattern;

import java.util.List;
import java.util.Objects;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import nk00322.surrey.petsearch.CustomToast;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
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

    @Password(min = 8, scheme = Password.Scheme.ALPHA_NUMERIC_MIXED_CASE_SYMBOLS, message = PASSWORD_FORMAT_ERROR)
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

    public SignupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_signup, container, false);
        initViews();
        validator = setupTextInputLayoutValidator(validator, this, view);
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
        closeActivityImage = view.findViewById(R.id.close_activity);
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
            case R.id.close_activity:
                navController.navigate(R.id.action_signupFragment_to_welcomeFragment);
                break;
            case R.id.location:
                startActivityForResult(getLocationAutoCompleteIntent(Objects.requireNonNull(getContext()).getApplicationContext()), AUTOCOMPLETE_REQUEST_CODE);
                break;
            case R.id.signUpBtn:
                if (!areAllFieldsCompleted(fullName, email, mobileNumber, location, password, confirmPassword)) {
                    signupLayout.startAnimation(shakeAnimation);
                    new CustomToast().Show_Toast(Objects.requireNonNull(getActivity()), view, "All fields are required.");
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
        // Else TODO SIGNUP
        final NavController navController = Navigation.findNavController(view);
        Toast.makeText(getActivity(), "Do SignUp.", Toast.LENGTH_SHORT).show();
        navController.navigate(R.id.action_signupFragment_to_mapFragment);
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
                new CustomToast().Show_Toast(Objects.requireNonNull(getActivity()), view, message);
            }
        }
    }

}