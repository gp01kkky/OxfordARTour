package com.ar.oxford.oxfordartour.MapHelper;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * this is to show the places in google map
 * Created by Kelvin Khoo on 21/06/2016.
 */
public class PlacesDisplayTask extends AsyncTask<Object, Integer, List<HashMap<String, String>>> {

    JSONObject googlePlacesJson;
    GoogleMap googleMap;

    @Override
    protected List<HashMap<String, String>> doInBackground(Object... params) {
        //params[1] = result returned
        List<HashMap<String, String>> googlePlacesList = null;
        Places placeJsonParser = new Places();

        try {
            googleMap = (GoogleMap) params[0];
            googlePlacesJson = new JSONObject((String) params[1]);
            googlePlacesList = placeJsonParser.parse(googlePlacesJson);
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        }
        return googlePlacesList;    }

    /*
    To add the marker for each places list returned by json
     */
    @Override
    protected void onPostExecute(List<HashMap<String, String>> list) {
        googleMap.clear(); // clear the map
        for (int i = 0; i < list.size(); i++) {
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = list.get(i);
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));
            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            markerOptions.title(placeName + " : " + vicinity);
            googleMap.addMarker(markerOptions);
        }
    }
}
