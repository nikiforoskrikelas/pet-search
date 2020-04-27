package nk00322.surrey.petsearch.models;

import com.google.firebase.Timestamp;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Objects;

@IgnoreExtraProperties
public class SearchParty {
    private String title;
    private String description;
    private String imageUrl;
    private String locationId;
    private String reward;
    private String ownerUid;
    @ServerTimestamp
    private Timestamp timestampCreated;
    private ArrayList<String> subscriberUids;
    private double latitude;
    private double longitude;

    public SearchParty() {
        // Default constructor required for calls to DataSnapshot.getValue(SearchParty.class)
    }

    public SearchParty(String title, String description, String imageUrl, String locationId, String reward, String ownerUid, ArrayList<String> subscriberUids, double latitude, double longitude) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;

        this.locationId = locationId;
        this.reward = reward;
        this.ownerUid = ownerUid;
//        HashMap<String, Object> timestampNow = new HashMap<>();
//        timestampNow.put("timestamp", FieldValue.serverTimestamp());

        this.subscriberUids = subscriberUids;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getReward() {
        return reward;
    }

    public String getOwnerUid() {
        return ownerUid;
    }


    public Timestamp getTimestampCreated() {
        return timestampCreated;
    }

    public ArrayList<String> getSubscriberUids() {
        return subscriberUids;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchParty that = (SearchParty) o;
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(locationId, that.locationId) &&
                Objects.equals(reward, that.reward) &&
                Objects.equals(ownerUid, that.ownerUid) &&
                Objects.equals(timestampCreated, that.timestampCreated) &&
                Objects.equals(subscriberUids, that.subscriberUids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, imageUrl, locationId, reward, ownerUid, timestampCreated, subscriberUids, latitude, longitude);
    }
}
