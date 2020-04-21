package nk00322.surrey.petsearch.utils;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;

import com.example.petsearch.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.fragment.app.FragmentActivity;

import static java.text.DateFormat.getDateTimeInstance;

//Code from https://www.androhub.com/login-signup-and-forgot-password-screen-design-android/
public class GeneralUtils {

    public final static int PICK_IMAGE_REQUEST = 2;



    public static String getNowTimestamp() {
        return LocalDateTime.now().toString();
    }

    public static String getTimeDate(long timestamp){
        try{
            DateFormat dateFormat = getDateTimeInstance(2,3, Locale.UK);
            Date netDate = (new Date(timestamp));
            return dateFormat.format(netDate);
        } catch(Exception e) {
            return "Error";
        }
    }

    public static String printDate(String inputDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:m d/MMM/yyyy", Locale.ENGLISH);
        LocalDateTime date = LocalDateTime.parse(inputDate);

        return date.format(formatter);
    }

    //https://stackoverflow.com/questions/8817377/android-how-to-find-multiple-views-with-common-attribute
    //Retrieve multiple views with the same tag
    public static ArrayList<View> getViewsByTag(ViewGroup root, String tag) {
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }


    public static void slideView(View view,
                                 int currentHeight,
                                 int newHeight) {

        ValueAnimator slideAnimator = ValueAnimator
                .ofInt(currentHeight, newHeight)
                .setDuration(500);

        /* We use an update listener which listens to each tick
         * and manually updates the height of the view  */

        slideAnimator.addUpdateListener(animation1 -> {
            Integer value = (Integer) animation1.getAnimatedValue();
            view.getLayoutParams().height = value.intValue();
            view.requestLayout();
        });

        /*  We use an animationSet to play the animation  */

        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet.play(slideAnimator);
        animationSet.start();
    }

    public static void textViewSlideOut(View view, FragmentActivity activity) {
        view.setClickable(false);
        view.setFocusable(false);
        view.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_out_right));
        view.setVisibility(View.GONE);

    }

    public static void textViewSlideIn(View view, FragmentActivity activity) {
        view.setClickable(true);
        view.setFocusable(true);
        view.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_in_right));
        view.setVisibility(View.VISIBLE);
    }

    public static String getFileExtension(Uri uri, Context context) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getContentResolver().getType(uri));
    }

    public static void setFocusableAndClickable(boolean makeFocusableAndClickable, View... view) {
        for (View v : view){
            v.setFocusable(makeFocusableAndClickable);
            v.setFocusableInTouchMode(makeFocusableAndClickable);
            v.setClickable(makeFocusableAndClickable);
            v.clearFocus();
        }
    }



}