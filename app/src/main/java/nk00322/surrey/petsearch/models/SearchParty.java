package nk00322.surrey.petsearch.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

public class SearchParty {
    private String title;
    private String description;
    private String imageUrl;
    private String locationId;
    private String reward;
    private String ownerUid;
    private HashMap<String, Object> timestampCreated;


    public SearchParty() {
        // Default constructor required for calls to DataSnapshot.getValue(SearchParty.class)
    }

    public SearchParty(String title, String description, String imageUrl, String locationId, String reward, String ownerUid) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;

        this.locationId = locationId;
        this.reward = reward;
        this.ownerUid = ownerUid;
        HashMap<String, Object> timestampNow = new HashMap<>();
        timestampNow.put("timestamp", ServerValue.TIMESTAMP);
        this.timestampCreated = timestampNow;
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

    public HashMap<String, Object> getTimestampCreated(){
        return timestampCreated;
    }

    @Exclude
    public long getTimestampCreatedLong(){
        return (long)timestampCreated.get("timestamp");
    }
}
