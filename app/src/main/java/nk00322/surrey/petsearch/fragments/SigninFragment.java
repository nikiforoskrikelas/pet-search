package nk00322.surrey.petsearch.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.petsearch.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Pattern;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import nk00322.surrey.petsearch.CustomToast;
import nk00322.surrey.petsearch.ToastType;

import static nk00322.surrey.petsearch.utils.ValidationUtils.EMAIL_REGEX;
import static nk00322.surrey.petsearch.utils.ValidationUtils.areAllFieldsCompleted;
import static nk00322.surrey.petsearch.utils.ValidationUtils.clearTextInputEditTextErrors;
import static nk00322.surrey.petsearch.utils.ValidationUtils.setupTextInputLayoutValidator;


/**
 * Fragment responsible for user sign in
 * Uses Saripaar annotation based field validation
 */
public class SigninFragment extends Fragment implements View.OnClickListener, Validator.ValidationListener {
    private static String TAG = "SigninFragment";

    private static Animation shakeAnimation;
    private View view;
    private long mLastClickTime = 0;

    @NotEmpty(sequence = 1)
    @Pattern(regex = EMAIL_REGEX, message = "Invalid email", sequence = 2)
    private TextInputEditText email;

    @NotEmpty
    private TextInputEditText password;
    private Button signinButton;
    private TextView forgotPassword;
    private TextView signUp;
    private ConstraintLayout signinLayout;
    private ImageView closeActivityImage;
    private Validator validator;

    private FirebaseAuth auth;

    public SigninFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_signin, container, false);
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
            Navigation.findNavController(view).navigate(R.id.action_signinFragment_to_mapFragment);
        }
    }

    // Initiate Views
    private void initViews() {
        email = view.findViewById(R.id.signin_email);
        password = view.findViewById(R.id.signin_password);
        signinButton = view.findViewById(R.id.signinBtn);
        forgotPassword = view.findViewById(R.id.forgot_password);
        signUp = view.findViewById(R.id.createAccount);
        signinLayout = view.findViewById(R.id.signin_layout);
        closeActivityImage = view.findViewById(R.id.close_activity);

        //TODO REMOVE - ONLY FOR TESTING
        password.setText("1234qwerQWER");
        //TODO REMOVE - ONLY FOR TESTING

        // Load ShakeAnimation
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);

        // Setting text selector over textviews
        ColorStateList textSelector = getResources().getColorStateList(R.color.text_selector);
        forgotPassword.setTextColor(textSelector);

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
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) { //To prevent double clicking
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        final NavController navController = Navigation.findNavController(view);
        switch (v.getId()) {
            case R.id.close_activity:
                navController.navigate(R.id.action_signinFragment_to_welcomeFragment);
                break;
            case R.id.signinBtn:
                //Validate fields before logging in
                if (!areAllFieldsCompleted(email, password)) {
                    signinLayout.startAnimation(shakeAnimation);
                    new CustomToast().showToast(getContext(), view, "All fields are required.", ToastType.ERROR, false);
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
        loginAccount(email.getText().toString(), password.getText().toString());
    }

    private void loginAccount(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            final NavController navController = Navigation.findNavController(view);

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success");
                    FirebaseUser user = auth.getCurrentUser();
                    if(user.isEmailVerified()) {
                        navController.navigate(R.id.action_signinFragment_to_mapFragment);
                    }else{
                        user.sendEmailVerification();
                        FirebaseAuth.getInstance().signOut();
                        new CustomToast().showToast(getContext(), view, "This account is not verified. Please check your email, a verification link has been sent", ToastType.ERROR, true);
                    }
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            final NavController navController = Navigation.findNavController(view);

            @Override
            public void onFailure(@NonNull Exception e) {
                String message = "";
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    message = "Invalid password";
                } else if (e instanceof FirebaseAuthInvalidUserException) {
                    String errorCode =
                            ((FirebaseAuthInvalidUserException) e).getErrorCode();
                    if (errorCode.equals("ERROR_USER_NOT_FOUND")) {
                        message = "No matching account found";
                    } else if (errorCode.equals("ERROR_USER_DISABLED")) {
                        message = "User account has been disabled";
                    } else {
                        message = e.getLocalizedMessage();
                    }
                } else {
                    new CustomToast().showToast(getContext(), view, e.getMessage(), ToastType.ERROR, false);
                    navController.navigate(R.id.action_signinFragment_to_welcomeFragment);
                }
                new CustomToast().showToast(getContext(), view, message, ToastType.ERROR, false);
            }
        });
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