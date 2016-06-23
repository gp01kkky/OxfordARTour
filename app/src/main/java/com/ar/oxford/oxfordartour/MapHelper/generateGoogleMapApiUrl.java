package com.ar.oxford.oxfordartour.MapHelper;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Kelvin Khoo on 22/06/2016.
 */
public class GenerateGoogleMapApiUrl {

    public static final int NEARBY_PLACES = 0;
    public static final int TEXT_SEARCH = 1;
    public static final int SEARCH_BY_TYPE = 2;

    private static final String GOOGLE_API_KEY = "AIzaSyDqJGehbvGCLpEUxbchILmGK_-3eWyBxgc";
    private int PROXIMITY_RADIUS = 50; // in metres

    public StringBuilder getGoogleMapPlacesQueryURL(String userQuery, int type, double latitude, double longitude)
    {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/");

        switch (type)
        {
            case NEARBY_PLACES:
                googlePlacesUrl.append("nearbysearch/json?");
                googlePlacesUrl.append("location=" + latitude + "," + longitude);
                googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
                googlePlacesUrl.append("&sensor=true");
                googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);
                break;
            case TEXT_SEARCH:
                googlePlacesUrl.append("textsearch/json?");
                googlePlacesUrl.append("query=" + userQuery);
                googlePlacesUrl.append("&location=" + latitude + "," + longitude);
                googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
                googlePlacesUrl.append("&sensor=true");
                googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);
                break;
            case SEARCH_BY_TYPE:
                googlePlacesUrl.append("nearbysearch/json?");
                googlePlacesUrl.append("location=" + latitude + "," + longitude);
                googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
                googlePlacesUrl.append("&type=" + userQuery);
                googlePlacesUrl.append("&sensor=true");
                googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);
                break;
            default:
                googlePlacesUrl.append("nearbysearch/json?");
                googlePlacesUrl.append("location=" + latitude + "," + longitude);
                googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
                googlePlacesUrl.append("&type=" + userQuery);
                googlePlacesUrl.append("&sensor=true");
                googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);
                break;


        }
        return googlePlacesUrl;
    }

    public String getDirectionsUrl(LatLng origin, LatLng dest, ArrayList<LatLng> markerPoints){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Waypoints
        String waypoints = "";
        for(int i=2;i<markerPoints.size();i++){
            LatLng point  = (LatLng) markerPoints.get(i);
            if(i==2)
                waypoints = "waypoints=";
            waypoints += point.latitude + "," + point.longitude + "|";
        }

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+waypoints;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        Log.d("Direction url: ",url);

        return url;
    }
}
