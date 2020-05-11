package nk00322.surrey.petsearch.models;

@com.google.firebase.firestore.IgnoreExtraProperties
public class User {
    private String email;
    private String fullName;
    private String mobileNumber;
    private String locationId;
    private String dateCreated;
    private String imageUrl;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, String fullName, String mobileNumber, String locationId, String dateCreated, String imageUrl) {
        this.email = email;
        this.fullName = fullName;
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

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }
}
