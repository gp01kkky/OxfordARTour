package com.ar.oxford.oxfordtourar.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kelvin Khoo on 12/08/2016.
 */
public class PlaceTrip extends Place {
    public int duration=0;
    public int position;
    public int placeTripid;
    public int checked; // this is to see whether the place is added into the itinary

    public PlaceTrip(Place place, int position, int duration, int checked)
    {
        super(place);
        this.duration=duration;
        this.position=position;
        this.checked=checked;
    }

    public int getChecked() {
        return checked;
    }

    public void setChecked(int checked) {
        this.checked = checked;
    }

    public int getPlaceTripid() {
        return placeTripid;
    }


    public void setPlaceTripid(int id) {
        this.placeTripid = id;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>()
    {
        public Place createFromParcel(Parcel in)
        {
            return new Place(in);
        }
        public Place[] newArray(int size)
        {
            return new Place[size];
        }
    };
}
