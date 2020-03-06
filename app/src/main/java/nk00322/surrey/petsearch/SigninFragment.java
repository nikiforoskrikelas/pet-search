package nk00322.surrey.petsearch;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petsearch.R;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import static nk00322.surrey.petsearch.Utils.isEmailValid;


/**
 * A simple {@link Fragment} subclass.
 */
public class SigninFragment extends Fragment implements View.OnClickListener {

    private static Animation shakeAnimation;
    private View view;
    private EditText email, password;
    private Button signinButton;
    private TextView forgotPassword, signUp;
    private CheckBox showHidePassword, keepMeSignedIn;
    private ConstraintLayout signinLayout;

    public SigninFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_signin, container, false);
        initViews();
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
        showHidePassword = view.findViewById(R.id.show_hide_password);
        keepMeSignedIn = view.findViewById(R.id.keep_me_signed_in);
        signinLayout = view.findViewById(R.id.signin_layout);

        // Load ShakeAnimation
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);

        // Setting text selector over textviews
        ColorStateList textSelector = getResources().getColorStateList(R.color.text_selector);
        forgotPassword.setTextColor(textSelector);
        showHidePassword.setTextColor(textSelector);
        keepMeSignedIn.setTextColor(textSelector);
        signUp.setTextColor(textSelector);

    }

    // Set Listeners
    private void setListeners() {
        signinButton.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        signUp.setOnClickListener(this);

        // Set check listener over checkbox for showing and hiding password
        showHidePassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                String getPassword = password.getText().toString();
                // If it is checked then show password else hide
                if(!getPassword.equals("") && getPassword.length() > 0) {
                    if (isChecked) {
                        // change checkbox text
                        showHidePassword.setText(R.string.hide_pwd);// change
                        password.setInputType(InputType.TYPE_CLASS_TEXT);
                        password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());// show password
                    } else {
                        // change checkbox text
                        showHidePassword.setText(R.string.show_pwd);
                        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        password.setTransformationMethod(PasswordTransformationMethod.getInstance());// hide password
                    }
                }
            }
        });

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
            case R.id.signinBtn:
                //Validate fields before logging in
                checkValidation(navController);
                break;
            case R.id.forgot_password:
                // Replace forgot password fragment with animation
                navController.navigate(R.id.action_signinFragment_to_forgotPasswordFragment);
                break;
            case R.id.createAccount:
                // Replace signup frgament with animation
                navController.navigate(R.id.action_signinFragment_to_signupFragment);
                break;
        }

    }

    // Check Validation before signin
    private void checkValidation(NavController navController) {
        // Get email id and password
        String getEmail = email.getText().toString();
        String getPassword = password.getText().toString();


        // Check for both field is empty or not
        if (getEmail.equals("") || getEmail.length() == 0 || getPassword.equals("") || getPassword.length() == 0) {
            signinLayout.startAnimation(shakeAnimation);
            new CustomToast().Show_Toast(getActivity(), view, "Enter both credentials.");

        }//todo more password validation
        // Check if email id is valid or not
        else if (!isEmailValid(getEmail)){
            email.startAnimation(shakeAnimation);
            new CustomToast().Show_Toast(getActivity(), view, "Your Email is Invalid.");
        }
        // Else TODO LOGIN
        else {
            Toast.makeText(getActivity(), "Do Login.", Toast.LENGTH_SHORT).show();
            navController.navigate(R.id.action_signinFragment_to_mapFragment);
        }

    }

}