package nk00322.surrey.petsearch.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;

@IgnoreExtraProperties
public class User {
    private String email;
    private String fullName;
    private String mobileNumber;
    private String locationId;
    private String dateCreated;
    private String imageUrl;
    private HashMap<String, SearchParty> searchParties;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, String fullName, String mobileNumber, String locationId, String dateCreated, String imageUrl, HashMap<String, SearchParty> searchParties) {
        this.email = email;
        this.fullName = fullName;
        this.mobileNumber = mobileNumber;
        this.locationId = locationId;
        this.dateCreated = dateCreated;
        this.imageUrl = imageUrl;

        this.searchParties = searchParties;
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

    public HashMap<String, SearchParty> getSearchParties() {
        return searchParties;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }
}
