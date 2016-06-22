package com.ar.oxford.oxfordartour.MapHelper;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

/**
 * Created by Kelvin Khoo on 21/06/2016.
 * This class is to perform the asynctask to query google for poi result, it takes the string query
 * generated from the mapactivity
 */
public class GooglePlacesReadTask extends AsyncTask<Object,Integer,String> {

    String googlePlacesData = null;
    GoogleMap googleMap;

    @Override
    protected String doInBackground(Object... params) {
        try {
            //params[0] = map_fragment, params[1] = api query url
            googleMap = (GoogleMap) params[0];
            String googlePlacesUrl = (String) params[1];
            Http http = new Http();
            googlePlacesData = http.read(googlePlacesUrl);
        } catch (Exception e) {
            Log.d("Google Place Read Task", e.toString());
        }
        return googlePlacesData;    }


    /**
     * once the asynctask finishes, we call another asynchtask to place the marker
     * @param result
     */
    @Override
    protected void onPostExecute(String result) {
        PlacesDisplayTask placesDisplayTask = new PlacesDisplayTask();
        Object[] toPass = new Object[2];
        toPass[0] = googleMap;
        toPass[1] = result;
        placesDisplayTask.execute(toPass);
    }
}
