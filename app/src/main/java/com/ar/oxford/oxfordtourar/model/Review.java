package com.ar.oxford.oxfordtourar.model;

/**
 * Created by Kelvin Khoo on 13/08/2016.
 */
public class Review
{
    private String authorName="";
    private float rating=0;
    private long timeStamp;
    private String review="";
    private String photoUrl="";

    public Review()
    {

    }

    public Review(String authorName, float rating, long timeStamp, String review, String photoUrl) {
        this.authorName = authorName;
        this.rating = rating;
        this.timeStamp = timeStamp;
        this.review = review;
        this.photoUrl = photoUrl;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
