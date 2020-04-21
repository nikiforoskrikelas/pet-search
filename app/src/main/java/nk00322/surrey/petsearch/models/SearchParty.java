package nk00322.surrey.petsearch.models;

public class SearchParty {
    private String title;
    private String description;
    private String imageUrl;
    private String locationId;
    private String reward;
    private String ownerUid;
    private String dateCreated;

    public SearchParty() {
        // Default constructor required for calls to DataSnapshot.getValue(SearchParty.class)
    }
    public SearchParty(String title, String description, String imageUrl, String locationId, String reward, String ownerUid, String dateCreated){
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;

        this.locationId = locationId;
        this.reward = reward;
        this.ownerUid = ownerUid;
        this.dateCreated = dateCreated;
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

    public String getDateCreated() {
        return dateCreated;
    }
}
