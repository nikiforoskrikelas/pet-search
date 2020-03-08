package nk00322.surrey.petsearch.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petsearch.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Pattern;

import java.util.List;
import java.util.Objects;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import nk00322.surrey.petsearch.CustomToast;

import static nk00322.surrey.petsearch.utils.ValidationUtils.EMAIL_REGEX;
import static nk00322.surrey.petsearch.utils.ValidationUtils.areAllFieldsCompleted;
import static nk00322.surrey.petsearch.utils.ValidationUtils.clearTextInputEditTextErrors;
import static nk00322.surrey.petsearch.utils.ValidationUtils.setupTextInputLayoutValidator;


/**
 * Fragment responsible for user sign in
 * Uses Saripaar annotation based field validation
 */
public class SigninFragment extends Fragment implements View.OnClickListener, Validator.ValidationListener {

    private static Animation shakeAnimation;
    private View view;

    @NotEmpty(sequence = 1)
    @Pattern(regex = EMAIL_REGEX, message = "Invalid email", sequence = 2)
    private TextInputEditText email;

    @NotEmpty
    private TextInputEditText password;


    private Button signinButton;
    private TextView forgotPassword, signUp;
    private ConstraintLayout signinLayout;
    private CheckBox keepMeSignedIn;
    private ImageView closeActivityImage;
    private Validator validator;

    public SigninFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_signin, container, false);
        initViews();
        validator = setupTextInputLayoutValidator(validator, this, view);
        setListeners();

        // Inflate the layout for this fragment
        return view;
    }

    // Initiate Views
    private void initViews() {
        email = view.findViewById(R.id.signin_email);
        password = view.findViewById(R.id.signin_password);
        signinButton = view.findViewById(R.id.signinBtn);
        forgotPassword = view.findViewById(R.id.forgot_password);
        signUp = view.findViewById(R.id.createAccount);
        keepMeSignedIn = view.findViewById(R.id.keep_me_signed_in);
        signinLayout = view.findViewById(R.id.signin_layout);
        closeActivityImage = view.findViewById(R.id.close_activity);

        // Load ShakeAnimation
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);

        // Setting text selector over textviews
        ColorStateList textSelector = getResources().getColorStateList(R.color.text_selector);
        forgotPassword.setTextColor(textSelector);
        keepMeSignedIn.setTextColor(textSelector);
        signUp.setTextColor(textSelector);

        if (getArguments() != null)
            email.setText(SigninFragmentArgs.fromBundle(getArguments()).getRegisteredEmail());


    }

    // Set Listeners
    private void setListeners() {
        closeActivityImage.setOnClickListener(this);
        signinButton.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        signUp.setOnClickListener(this);
        validator.setValidationListener(this);

        // Set check listener over checkbox for keeping the user signed in
        keepMeSignedIn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                // If it is checked then keep user signed in
                //TODO KEEP ME SIGNED IN CHECKBOX
                if (isChecked) {

                } else {

                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        final NavController navController = Navigation.findNavController(view);

        switch (v.getId()) {
            case R.id.close_activity:
                navController.navigate(R.id.action_signinFragment_to_welcomeFragment);
                break;
            case R.id.signinBtn:
                //Validate fields before logging in
                if (!areAllFieldsCompleted(email, password)) {
                    signinLayout.startAnimation(shakeAnimation);
                    new CustomToast().Show_Toast(Objects.requireNonNull(getActivity()), view, "All fields are required.");
                    break;
                }
                clearTextInputEditTextErrors(email, password);
                validator.validate();
                break;
            case R.id.forgot_password:
                // Navigate to forgot password fragment
                navController.navigate(R.id.action_signinFragment_to_forgotPasswordFragment);
                break;
            case R.id.createAccount:
                // Navigate to signup fragment
                navController.navigate(R.id.action_signinFragment_to_signupFragment);
                break;
        }

    }

    @Override
    public void onValidationSucceeded() {
        // Else TODO SIGNIN
        final NavController navController = Navigation.findNavController(view);
        Toast.makeText(getActivity(), "Do Sign In.", Toast.LENGTH_SHORT).show();
        navController.navigate(R.id.action_signinFragment_to_mapFragment);
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