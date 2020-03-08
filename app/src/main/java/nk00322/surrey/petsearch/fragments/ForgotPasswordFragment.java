package nk00322.surrey.petsearch.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import static nk00322.surrey.petsearch.utils.ValidationUtils.clearTextInputEditTextErrors;
import static nk00322.surrey.petsearch.utils.ValidationUtils.setupTextInputLayoutValidator;


/**
 * Fragment responsible for resetting user passwords
 * Uses Saripaar annotation based field validation
 */
public class ForgotPasswordFragment extends Fragment implements View.OnClickListener, Validator.ValidationListener {

    private static Animation shakeAnimation;
    private View view;

    @NotEmpty(sequence = 1)
    @Pattern(regex = EMAIL_REGEX, message = "Invalid email", sequence = 2)
    private TextInputEditText email;
    private Button submit;
    private ConstraintLayout forgotPasswordLayout;
    private ImageView closeActivityImage;
    private Validator validator;
    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        initViews();
        validator = setupTextInputLayoutValidator(validator, this, view);
        setListeners();

        // Inflate the layout for this fragment
        return view;
    }

    // Initiate Views
    private void initViews() {
        email = view.findViewById(R.id.registered_email);
        submit = view.findViewById(R.id.password_reset_button);
        forgotPasswordLayout = view.findViewById(R.id.forgot_password_layout);
        closeActivityImage = view.findViewById(R.id.close_activity);
        // Load ShakeAnimation
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);

    }

    // Set validator and attributes

    // Set Listeners over buttons
    private void setListeners() {
        closeActivityImage.setOnClickListener(this);
        submit.setOnClickListener(this);
        validator.setValidationListener(this);
    }

    @Override
    public void onClick(View v) {
        final NavController navController = Navigation.findNavController(view);

        switch (v.getId()) {
            case R.id.close_activity:
                navController.navigate(R.id.action_forgotPasswordFragment_to_signinFragment);
                break;

            case R.id.password_reset_button:
                clearTextInputEditTextErrors(email);
                validator.validate();
                break;

        }

    }

    @Override
    public void onValidationSucceeded() {
        // Else TODO forgot password checks
        final NavController navController = Navigation.findNavController(view);
        Toast.makeText(getActivity(), "Do Forgot password.", Toast.LENGTH_SHORT).show();

        //Safe Args to pass data with type safety
        //https://developer.android.com/guide/navigation/navigation-pass-data#java
        ForgotPasswordFragmentDirections.ActionForgotPasswordFragmentToSigninFragment action =
                ForgotPasswordFragmentDirections.actionForgotPasswordFragmentToSigninFragment();
        action.setRegisteredEmail(email.getText().toString());
        navController.navigate(action);        //todo only if email is registered


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
