package nk00322.surrey.petsearch.utils;

import android.text.TextUtils;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

//Code from https://www.androhub.com/login-signup-and-forgot-password-screen-design-android/
public class GeneralUtils {
    final static String API_KEY = "AIzaSyBoVHKsY1l_2v73jyL75-czkFNI_mpxmVY";
    private final static SimpleDateFormat ISO_8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'");


    public static String getNow(){
        return ISO_8601_FORMAT.format(new Date());
    }

}