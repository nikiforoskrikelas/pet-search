package nk00322.surrey.petsearch;


import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petsearch.R;

import androidx.annotation.NonNull;


//Code from https://www.androhub.com/login-signup-and-forgot-password-screen-design-android/
public class CustomToast {

    // Custom Toast Method
    public void showToast(Context context, View view, String message,@NonNull ToastType toastType, boolean isLongDuration) {

        // Layout Inflater for inflating custom view
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // inflate the layout over view
        View layout = null;

        // Get TextView id and set message
        int id = 0;
        switch (toastType) {
            case ERROR:
                id = R.id.toast_error;
                layout = inflater.inflate(R.layout.custom_error_toast, (ViewGroup) view.findViewById(R.id.error_toast_root));
                break;
            case SUCCESS:
                id = R.id.toast_success;
                layout = inflater.inflate(R.layout.custom_success_toast, (ViewGroup) view.findViewById(R.id.success_toast_root));
                break;
            case INFO:
                id = R.id.toast_info;
                layout = inflater.inflate(R.layout.custom_info_toast, (ViewGroup) view.findViewById(R.id.info_toast_root));
                break;
        }

        TextView text = layout.findViewById(id);
        text.setText(message);

        Toast toast = new Toast(context);// Get Toast Context
        toast.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 0);// Set

        toast.setDuration(isLongDuration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);// Set Duration
        toast.setView(layout); // Set Custom View over toast

        toast.show();// Finally show toast
    }

}
