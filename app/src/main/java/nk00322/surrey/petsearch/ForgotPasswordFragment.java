package nk00322.surrey.petsearch;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petsearch.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import static nk00322.surrey.petsearch.Utils.isEmailValid;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForgotPasswordFragment extends Fragment implements View.OnClickListener {

    private View view;

    private EditText email;
    private TextView submit, back;
    private static Animation shakeAnimation;

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        initViews();
        setListeners();

        // Inflate the layout for this fragment
        return view;
    }

    // Initiate Views
    private void initViews() {
        email = view.findViewById(R.id.registered_email);
        submit = view.findViewById(R.id.forgot_button);
        back = view.findViewById(R.id.backToSigninBtn);

        // Load ShakeAnimation
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);

        ColorStateList textSelector = getResources().getColorStateList(R.color.text_selector);
        back.setTextColor(textSelector);
        submit.setTextColor(textSelector);

    }

    // Set Listeners over buttons
    private void setListeners() {
        back.setOnClickListener(this);
        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final NavController navController = Navigation.findNavController(view);

        switch (v.getId()) {
            case R.id.backToSigninBtn:
                // Replace Login Fragment on Back Presses
                navController.navigate(R.id.action_forgotPasswordFragment_to_signinFragment);
                break;

            case R.id.forgot_button:

                // Call Submit button task
                submitButtonTask(navController);
                break;

        }

    }

    private void submitButtonTask(NavController navController) {
        String getEmail = email.getText().toString();


        // First check if email id is not null else show error toast
        if (getEmail.equals("") || getEmail.length() == 0){
            email.startAnimation(shakeAnimation);
            new CustomToast().Show_Toast(getActivity(), view, "Please enter your Email");

        }
        // Check if email id is valid or not
        else if (!isEmailValid(getEmail)){
            email.startAnimation(shakeAnimation);
            new CustomToast().Show_Toast(getActivity(), view, "Your Email is Invalid.");
        }

            // TODO FORGET PASSWORD
        else{
            navController.navigate(R.id.action_forgotPasswordFragment_to_signinFragment);
            Toast.makeText(getActivity(), "Get Forgot Password.",
                    Toast.LENGTH_SHORT).show();
        }

    }
}
