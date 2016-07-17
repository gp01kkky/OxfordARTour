package com.ar.oxford.oxfordtourar.MapHelper;

import android.content.Context;
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
    Context context;
    int queryType;
    public static final int PLACE_QUERY = 0;
    public static final int DIRECTION_QUERY = 1;

    @Override
    protected String doInBackground(Object... params) {
        try {
            //params[0] = map_fragment, params[1] = api query url
            googleMap = (GoogleMap) params[0];
            String googlePlacesUrl = (String) params[1];
            queryType = (int) params[2];
            context = (Context) params[3];
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
        Object[] toPass = new Object[4];
        toPass[0] = googleMap;
        toPass[1] = result;
        toPass[2] = queryType;
        toPass[3] = context;

        switch ((int) toPass[2])
        {
            case PLACE_QUERY:
                PlacesDisplayTask placesDisplayTask = new PlacesDisplayTask();
                placesDisplayTask.execute(toPass);
                break;
            case DIRECTION_QUERY:
                DirectionDisplayTask directionDisplayTask = new DirectionDisplayTask();
                directionDisplayTask.execute(toPass);
                break;
            default:
                break;
        }
    }
}
