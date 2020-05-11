package nk00322.surrey.petsearch.utils;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;

import com.example.petsearch.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import nk00322.surrey.petsearch.models.SearchParty;

import static java.text.DateFormat.getDateTimeInstance;

//Code from https://www.androhub.com/login-signup-and-forgot-password-screen-design-android/
public class GeneralUtils {

    public final static int PICK_IMAGE_REQUEST = 2;


    public static String getNowTimestamp() {
        return LocalDateTime.now().toString();
    }

    public static String getTimeDate(long timestamp) {
        try {
            DateFormat dateFormat = getDateTimeInstance(2, 3, Locale.UK);
            Date netDate = (new Date(timestamp));
            return dateFormat.format(netDate);
        } catch (Exception e) {
            return "Error";
        }
    }

    public static String printDate(Date inputDate) {
        return new SimpleDateFormat("d/MMM/yyyy HH:mm").format(inputDate);

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
        for (View v : view) {
            v.setFocusable(makeFocusableAndClickable);
            v.setFocusableInTouchMode(makeFocusableAndClickable);
            v.setClickable(makeFocusableAndClickable);
            v.clearFocus();
        }
    }

    public static double getDistanceInKilometers(double p1Lat, double p1Long, double p2Lat, double p2Long) {
        Location locationA = new Location("point A");

        locationA.setLatitude(p1Lat / 1E6);
        locationA.setLongitude(p1Long / 1E6);

        Location locationB = new Location("point B");

        locationB.setLatitude(p2Lat / 1E6);
        locationB.setLongitude(p2Long / 1E6);

        return (double) Math.round((locationA.distanceTo(locationB) * 100000)) / 100;
    }

    public static void checkPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {//Can add more as per requirement

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
    }


    public static Comparator<DocumentSnapshot> getDistanceToUserComparator(double userLatitude, double userLongitude) {
        return (o1, o2) -> {
            double firstPartyDistance = getDistanceInKilometers(o1.toObject(SearchParty.class).getLatitude(), o1.toObject(SearchParty.class).getLongitude(), userLatitude, userLongitude);
            double secondPartyDistance = getDistanceInKilometers(o2.toObject(SearchParty.class).getLatitude(), o2.toObject(SearchParty.class).getLongitude(), userLatitude, userLongitude);

            return Double.compare(firstPartyDistance, secondPartyDistance);

        };
    }

    public static Comparator<DocumentSnapshot> getCreationDateComparator() {
        return (o1, o2) -> {

            Timestamp firstPartyCreationTimestamp = o1.toObject(SearchParty.class).getTimestampCreated();
            Timestamp secondPartyCreationTimestamp = o2.toObject(SearchParty.class).getTimestampCreated();

            return firstPartyCreationTimestamp.compareTo(secondPartyCreationTimestamp);

        };
    }

    public static BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static LatLng findCentroid(List<LatLng> points) {
        double x = 0;
        double y = 0;

        for (LatLng p : points) {
            x += p.longitude;
            y += p.latitude;
        }

        return new LatLng(y / points.size(), x / points.size());
    }

    public static List<LatLng> sortVertices(List<LatLng> points) {


        LatLng center = findCentroid(points);
        Collections.sort(points, (a, b) -> {
            double a1 = (Math.toDegrees(Math.atan2(a.longitude - center.longitude, a.latitude - center.latitude)) + 360) % 360;
            double a2 = (Math.toDegrees(Math.atan2(b.longitude - center.longitude, b.latitude - center.latitude)) + 360) % 360;

            return (int) (a1 - a2);
        });
        return points;
    }

    @com.google.firebase.firestore.IgnoreExtraProperties
    public static class Point {
        private double lat;
        private double lng;

        public Point() {

        }

        public Point(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public Point(LatLng latLng) {
            this.lat = latLng.latitude;
            this.lng = latLng.longitude;
        }

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }

        public LatLng convertToLatLng(){
            return new LatLng(lat, lng);
        }

    }


}
