package nk00322.surrey.petsearch;

import java.util.regex.Pattern;

//Code from https://www.androhub.com/login-signup-and-forgot-password-screen-design-android/
public class Utils {
    public final static String API_KEY = "AIzaSyBoVHKsY1l_2v73jyL75-czkFNI_mpxmVY";

    public static boolean isEmailValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }
}
