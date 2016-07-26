package com.ar.oxford.oxfordtourar.model;

/**
 * Created by Kelvin Khoo on 25/07/2016.
 */
public class PlaceForDB {

    private int id;
    private String place_id = "";
    private String lat = "";
    private String lng = "";
    private String name = "";
    private String photo_reference = "";
    private String price_level = "";
    private float rating;
    private String type;
    private boolean open;
    private String phone_number = "";
    private String address = "";
    private String website ="";

    public PlaceForDB() {
    }

    /**
     * Just without id
     * @param place_id
     * @param lat
     * @param lng
     * @param name
     * @param photo_reference
     * @param price_level
     * @param rating
     * @param type
     * @param open
     * @param phone_number
     * @param address
     * @param website
     */
    public PlaceForDB(String place_id, String lat, String lng, String name, String photo_reference, String price_level, float rating, String type, boolean open, String phone_number, String address, String website) {
        this.place_id = place_id;
        this.lat = lat;
        this.lng = lng;
        this.name = name;
        this.photo_reference = photo_reference;
        this.price_level = price_level;
        this.rating = rating;
        this.type = type;
        this.open = open;
        this.phone_number = phone_number;
        this.address = address;
        this.website = website;
    }

    // With id
    public PlaceForDB(int id, String place_id, String lat, String lng, String name, String photo_reference, String price_level, float rating, String type, boolean open, String phone_number, String address, String website) {
        this.id = id;
        this.place_id = place_id;
        this.lat = lat;
        this.lng = lng;
        this.name = name;
        this.photo_reference = photo_reference;
        this.price_level = price_level;
        this.rating = rating;
        this.type = type;
        this.open = open;
        this.phone_number = phone_number;
        this.address = address;
        this.website = website;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto_reference() {
        return photo_reference;
    }

    public void setPhoto_reference(String photo_reference) {
        this.photo_reference = photo_reference;
    }

    public String getPrice_level() {
        return price_level;
    }

    public void setPrice_level(String price_level) {
        this.price_level = price_level;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
