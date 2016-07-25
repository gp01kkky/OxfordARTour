package com.ar.oxford.oxfordtourar.MapHelper;

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
    public static final int SEARCH_BY_GEOCOORDINATE = 3;
    public static final int AUTOCOMPLETE = 4;
    public static final int PLACE_DETAIL = 5;

    private static final String GOOGLE_API_KEY = "AIzaSyDqJGehbvGCLpEUxbchILmGK_-3eWyBxgc";
    private int PROXIMITY_RADIUS = 5000; // in metres

    public StringBuilder getGoogleMapPlacesQueryURL(String userQuery, int type, double latitude, double longitude)
    {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/");
        String query = userQuery.trim();
        query = query.replaceAll("\\s","+");
        switch (type)
        {
            case NEARBY_PLACES:
                googlePlacesUrl.append("place/nearbysearch/json?");
                googlePlacesUrl.append("sensor=true");
                googlePlacesUrl.append("&location=" + latitude + "," + longitude);
                googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
                googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);
                break;
            case TEXT_SEARCH:
                googlePlacesUrl.append("place/textsearch/json?");
                googlePlacesUrl.append("query=" + query);
                googlePlacesUrl.append("&sensor=true");
                googlePlacesUrl.append("&location=" + latitude + "," + longitude);
                googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
                googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);
                break;
            case SEARCH_BY_TYPE:
                googlePlacesUrl.append("place/nearbysearch/json?");
                googlePlacesUrl.append("location=" + latitude + "," + longitude);
                googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
                googlePlacesUrl.append("&type=" + query);
                googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);
                break;
            case SEARCH_BY_GEOCOORDINATE:
                googlePlacesUrl.append("geocode/json?");
                googlePlacesUrl.append("latlng=" + latitude + "," + longitude);
                googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);
                break;
            case AUTOCOMPLETE:
                googlePlacesUrl.append("place/autocomplete/json?");
                googlePlacesUrl.append("input=" + query);
                googlePlacesUrl.append("&sensor=true");
                googlePlacesUrl.append("&location=" + latitude +","+longitude);
                googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
                googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);
                break;
            case PLACE_DETAIL:
                googlePlacesUrl.append("place/details/json?");
                googlePlacesUrl.append("placeid=" + query);
                googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);
                break;
            default:
                googlePlacesUrl.append("nearbysearch/json?");
                googlePlacesUrl.append("&sensor=true");
                googlePlacesUrl.append("location=" + latitude + "," + longitude);
                googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
                googlePlacesUrl.append("&type=" + query);
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
        String sensor = "sensor=true";

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
