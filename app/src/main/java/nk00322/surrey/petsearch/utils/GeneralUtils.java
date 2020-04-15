package nk00322.surrey.petsearch.utils;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.petsearch.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.fragment.app.FragmentActivity;

//Code from https://www.androhub.com/login-signup-and-forgot-password-screen-design-android/
public class GeneralUtils {
    private final static SimpleDateFormat ISO_8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'");


    public static String getNow() {
        return ISO_8601_FORMAT.format(new Date());
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

}