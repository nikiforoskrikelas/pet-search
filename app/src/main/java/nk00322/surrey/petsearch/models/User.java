package nk00322.surrey.petsearch.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String mobileNumber;
    private String locationId;
    private String dateCreated;
    private String imageUrl;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String mobileNumber, String locationId, String dateCreated, String imageUrl) {
        this.mobileNumber = mobileNumber;
        this.locationId = locationId;
        this.dateCreated = dateCreated;
        this.imageUrl = imageUrl;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
