package nk00322.surrey.petsearch.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import nk00322.surrey.petsearch.utils.GeneralUtils;

@IgnoreExtraProperties
public class SearchedArea {

    private ArrayList<GeneralUtils.Point> polygonVertices;
    private Timestamp timestampCreated;
    private String ownerUid;

    public SearchedArea() {

    }

    public SearchedArea(ArrayList<LatLng> polygonVerticesLatLng, Timestamp timestampCreated, String ownerUid) {
        polygonVertices = new ArrayList<>();
        for(LatLng latLng: polygonVerticesLatLng){
            this.polygonVertices.add(new GeneralUtils.Point(latLng));
        }
        this.timestampCreated = timestampCreated;

        this.ownerUid = ownerUid;
    }


    public Timestamp getTimestampCreated() {
        return timestampCreated;
    }


    public List<GeneralUtils.Point> getPolygonVertices() {
        return polygonVertices;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public List<LatLng> getPolygonVerticesInLatLng() {

        return polygonVertices.stream().map(GeneralUtils.Point::getLatLng)
                .collect(Collectors.toList());
    }
}



