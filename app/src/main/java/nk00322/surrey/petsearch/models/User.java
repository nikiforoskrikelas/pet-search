package nk00322.surrey.petsearch.models;

import java.util.Date;
import java.util.Map;

public class User {
    private String mobileNumber;
    private String locationId;
    private String dateCreated;

    public User() {
    }

    public User(String mobileNumber, String locationId, String dateCreated) {
        this.mobileNumber = mobileNumber;
        this.locationId = locationId;
        this.dateCreated = dateCreated;

    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }


    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
}
