package com.ar.oxford.oxfordtourar.MapHelper;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.ar.oxford.oxfordtourar.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.util.List;

/**
 * this is to show the places in google map
 * Created by Kelvin Khoo on 21/06/2016.
 */
public class PlacesDisplayTask extends AsyncTask<Object, Integer, List<Place>> {

    JSONObject googlePlacesJson;
    GoogleMap googleMap;
    Context context;
    BottomSheetDialog placesListDialog;

    BottomSheetBehavior behavior;


    private OnTaskCompleted taskCompleted;

    protected List<Place> doInBackground(Object... params) {
        //params[1] = result returned
        List<Place> googlePlacesList = null;
        GooglePlacesParser placeJsonParser = new GooglePlacesParser();

        try {
            googleMap = (GoogleMap) params[0];
            googlePlacesJson = new JSONObject((String) params[1]);
            context = (Context) params[3];
            this.taskCompleted = (OnTaskCompleted) params[3];
            googlePlacesList = placeJsonParser.parse(googlePlacesJson);
            Log.e("",googlePlacesJson.toString());

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        }
        return googlePlacesList;    }

    /*
    To add the marker for each places list returned by json
     */
    protected void onPostExecute(List<Place> list) {


        googleMap.clear(); // clear the map
        for (int i = 0; i < list.size(); i++) {
            MarkerOptions markerOptions = new MarkerOptions();
            Place googlePlace = list.get(i);
            String placeName = googlePlace.getName();
            double lat = Double.parseDouble(googlePlace.getLat());
            double lng = Double.parseDouble(googlePlace.getLng());
            String type = googlePlace.getType();
            //Bitmap bmImg = getBitmapFromURL(icon);
            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            markerOptions.title(placeName);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(getIconFromTypes(type)));
            googleMap.addMarker(markerOptions);
        }
        //createDialog(list);
        taskCompleted.onTaskCompleted(true,list);
        //createBottomSheet(list);


    }

    public int getIconFromTypes(String type)
    {
        int resource_id;
        switch (type)
        {
            case "restaurant":
                resource_id = R.drawable.places_restaurant;
                break;
            default:
                resource_id = R.drawable.places_default;
                break;
        }
        return resource_id;
    }



    private void createDialog(final List<Place> placeList) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);

        GooglePlacesDisplayAdapter adapter = new GooglePlacesDisplayAdapter(placeList);
        adapter.setOnItemClickListener(new GooglePlacesDisplayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GooglePlacesDisplayAdapter.ItemHolder item, int position) {
                //dismissDialog();
                Toast.makeText(context, placeList.get(position).getName(), Toast.LENGTH_LONG).show();

            }
        });

        View view = inflater.inflate(R.layout.sheet_main, null);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        /*placesListDialog = new BottomSheetDialog(context);
        placesListDialog.setContentView(view);
        placesListDialog.show();
        taskCompleted.onTaskCompleted(true, placesListDialog, view);*/
    }

    private void createBottomSheet(final List<Place> placeList)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view1 = inflater.inflate(R.layout.activity_maps, null);
        View bottomSheet = view1.findViewById(R.id.bottom_sheet);

        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // React to state change
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
            }
        });

        View view = inflater.inflate(R.layout.sheet_main, null);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));


        GooglePlacesDisplayAdapter adapter = new GooglePlacesDisplayAdapter(placeList);
        adapter.setOnItemClickListener(new GooglePlacesDisplayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GooglePlacesDisplayAdapter.ItemHolder item, int position) {
                //dismissDialog();
                Toast.makeText(context, placeList.get(position).getName(), Toast.LENGTH_LONG).show();

            }
        });
        recyclerView.setAdapter(adapter);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    }


}
