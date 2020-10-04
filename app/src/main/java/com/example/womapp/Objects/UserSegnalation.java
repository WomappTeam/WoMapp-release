package com.example.womapp.Objects;



import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class UserSegnalation {

    private User user;
    private GeoPoint geo_point;
    private @ServerTimestamp Date timestamp;
    private String motivation;


    public UserSegnalation(User user, GeoPoint geo_point, Date timestamp,String motivation) {
        this.user = user;
        this.geo_point = geo_point;
        this.timestamp = timestamp;
        this.motivation = motivation;
    }

    public UserSegnalation() {

    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public GeoPoint getGeo_point() {
        return geo_point;
    }

    public void setGeo_point(GeoPoint geo_point) {
        this.geo_point = geo_point;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "UserLocation{" +
                "user=" + user +
                ", geo_point=" + geo_point +
                ", timestamp=" + timestamp +
                '}';
    }

}