package com.ar.oxford.oxfordtourar.model;

/**
 * Created by Kelvin Khoo on 25/07/2016.
 */
public class Trip {

    int id;
    String tripName;
    String created_at;

    public Trip(){

    }
    public Trip(String tripName, String created_at)
    {
        this.tripName = tripName;
        this.created_at = created_at;
    }
    public Trip(int id, String tripName, String create_at)
    {
        this.id = id;
        this.tripName = tripName;
        this.created_at = create_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
