package nk00322.surrey.petsearch.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.petsearch.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import nk00322.surrey.petsearch.CustomToast;

import static nk00322.surrey.petsearch.utils.ValidationUtils.areAllFieldsCompleted;
import static nk00322.surrey.petsearch.utils.ValidationUtils.clearTextInputEditTextErrors;


/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeFragment extends Fragment implements View.OnClickListener{
    private FirebaseAuth mAuth;
    private View view;
    private Button signUpButton;
    private Button signinButton;

    public WelcomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_welcome, container, false);
        mAuth = FirebaseAuth.getInstance();
        signUpButton = view.findViewById(R.id.welcome_signup);
        signinButton = view.findViewById(R.id.welcome_signin);

        signUpButton.setOnClickListener(this);
        signinButton.setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View v) {
        final NavController navController = Navigation.findNavController(view);

        switch (v.getId()) {
            case R.id.welcome_signup:
                navController.navigate(R.id.action_welcomeFragment_to_signupFragment);
                break;
            case R.id.welcome_signin:
                navController.navigate(R.id.action_welcomeFragment_to_signinFragment);
        }

    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            new CustomToast().showToast(Objects.requireNonNull(getActivity()), view, "User already signed in", ToastType.INFO, false);
            Navigation.findNavController(view).navigate(R.id.action_welcomeFragment_to_mapFragment);
        }
    }


}