package nk00322.surrey.petsearch.models;

import com.google.firebase.Timestamp;

@com.google.firebase.firestore.IgnoreExtraProperties
public class Sighting {

    private Timestamp timestampCreated;
    private double latitude;
    private double longitude;
    private String placeName;

    public Sighting() {

    }

    public Sighting(Timestamp timestampCreated, double latitude, double longitude, String placeName) {
        this.timestampCreated = timestampCreated;
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeName = placeName;
    }


    public Timestamp getTimestampCreated() {
        return timestampCreated;
    }



    public String getPlaceName() {
        return placeName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
