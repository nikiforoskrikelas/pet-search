package nk00322.surrey.petsearch;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Map;

public class CloudFunctions {
    private static final String TAG = "CloudFunctions";

    public static Task<String> sendNotification(String title, String description, String uid, String searchPartyUid) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("description", description);
        data.put("owner", uid);
        data.put("searchParty", searchPartyUid);

        return FirebaseFunctions.getInstance()
                .getHttpsCallable("addMessage")
                .call(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "sendNotification request success");
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "sendNotification request error: ", e);
                })
                .continueWith(task -> {
                    // This continuation runs on either success or failure, but if the task
                    // has failed then getResult() will throw an Exception which will be
                    // propagated down.
                    String result = (String) task.getResult().getData();
                    return result;
                });
    }
}
