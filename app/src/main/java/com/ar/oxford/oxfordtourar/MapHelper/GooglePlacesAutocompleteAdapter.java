package com.ar.oxford.oxfordtourar.MapHelper;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.ar.oxford.oxfordtourar.R;
import com.ar.oxford.oxfordtourar.model.PlaceAutoComplete;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by Kelvin Khoo on 27/06/2016.
 */
public class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
    private ArrayList<PlaceAutoComplete> result = null;
    Context context;
    //List<PlaceAutoComplete> placeList;

    public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId, double latitude, double longitude){
        super(context, textViewResourceId);
        this.context = context;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    @Override
    public int getCount(){
        return result.size();
    }
    @Override
    public PlaceAutoComplete getItem(int index){
        return result.get(index);
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.autocomplete_row, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) rowView.findViewById(R.id.place_name);
            holder.location = (TextView) rowView.findViewById(R.id.place_detail);
            rowView.setTag(holder);

        } else
            holder = (ViewHolder) rowView.getTag();
        /***** Get each Model object from ArrayList ********/
        holder.Place = result.get(position);
        StringTokenizer st=new StringTokenizer(holder.Place.getPlaceDesc(), ",");
        /************  Set Model values in Holder elements ***********/

        holder.name.setText(st.nextToken());
        String desc_detail="";
        for(int i=1; i<st.countTokens(); i++) {
            if(i==st.countTokens()-1){
                desc_detail = desc_detail + st.nextToken();
            }else {
                desc_detail = desc_detail + st.nextToken() + ",";
            }
        }
        holder.location.setText(desc_detail);
        return rowView;
    }

    class ViewHolder {
        PlaceAutoComplete Place;
        TextView name, location;
    }



    @Override
    public Filter getFilter(){
        Filter filter = new Filter(){
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint !=null){
                    result = autocomplete(constraint.toString());
                    filterResults.values = result;
                    filterResults.count = result.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results){
                if(results!=null&& results.count>0){
                    notifyDataSetChanged();
                }
                else
                {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    private static final String LOG_TAG = "Google GooglePlacesParser Autocomplete";

    private static final String GOOGLE_API_KEY = "AIzaSyDqJGehbvGCLpEUxbchILmGK_-3eWyBxgc";

    private static int PROXIMITY_RADIUS = 50; // in metres


    private static double latitude =0;
    private static double longitude = 0;

    public static void setLatitude(double latitude) {
        GooglePlacesAutocompleteAdapter.latitude = latitude;
    }

    public static void setLongitude(double longitude) {
        GooglePlacesAutocompleteAdapter.longitude = longitude;
    }

    public static ArrayList<PlaceAutoComplete> autocomplete(String input) {
        ArrayList<PlaceAutoComplete> resultList = null;
        HttpURLConnection conn = null;
        String query = input.trim();
        query = query.replaceAll("\\s","+");
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/");
            sb.append("place/autocomplete/json?");
            sb.append("input=" + query);
            sb.append("&location=" + latitude +","+longitude);
            sb.append("&radius=" + PROXIMITY_RADIUS);
            sb.append("&sensor=true");
            sb.append("&key=" + GOOGLE_API_KEY);
            URL url = new URL(sb.toString());
            Log.e("URL", sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing GooglePlacesParser API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to GooglePlacesParser API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
            // Extract the Place descriptions from the results
            resultList = new ArrayList<PlaceAutoComplete>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                PlaceAutoComplete autoCompleteItem = new PlaceAutoComplete();
                autoCompleteItem.setPlaceDesc(predsJsonArray.getJSONObject(i).getString("description"));
                autoCompleteItem.setPlaceID(predsJsonArray.getJSONObject(i).getString("place_id"));

                resultList.add(autoCompleteItem);

            }

        } catch (JSONException e) {

            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        return resultList;

    }








}
