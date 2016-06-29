package com.ar.oxford.oxfordartour;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ar.oxford.oxfordartour.MapHelper.GenerateGoogleMapApiUrl;
import com.ar.oxford.oxfordartour.MapHelper.GooglePlacesAutocompleteAdapter;
import com.ar.oxford.oxfordartour.MapHelper.GooglePlacesReadTask;
import com.ar.oxford.oxfordartour.MapHelper.OnTaskCompleted;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements LocationListener,OnTaskCompleted {
    private static Context context;
    private GoogleMap googleMap;


    private UiSettings googleMapUiSettings;

    private GenerateGoogleMapApiUrl generateGoogleAPIHelper;
    private static final String GOOGLE_API_KEY = "AIzaSyDqJGehbvGCLpEUxbchILmGK_-3eWyBxgc";
    EditText placeText;
    double latitude = 0;
    double longitude = 0;
    private int PROXIMITY_RADIUS = 50; // in metres
    ArrayList<LatLng> markerPoints; // for multiple waypoint route

    private boolean displayMenu = true;

    private BottomSheetDialog bottomDialog;
    private Button showList;
    private boolean searchPlaceCompleted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        generateGoogleAPIHelper = new GenerateGoogleMapApiUrl(); // helper for google api
        // Request permissions to support Android Marshmallow and above devices
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions();
        }

        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        setContentView(R.layout.activity_maps);
        context = MapsActivity.this;

        showList = (Button) findViewById(R.id.show_list);

        // to check for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.googleMap);


        googleMap = fragment.getMap();
        // allows the ui of mylocation
        googleMap.setMyLocationEnabled(true);
        googleMapUiSettings = googleMap.getUiSettings();
        googleMap.setPadding(0,200,0,0);
        googleMapUiSettings.setCompassEnabled(true);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider); // get location
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);

        // this activity is registered when user click on a ground overlay
        googleMap.setOnGroundOverlayClickListener(new GoogleMap.OnGroundOverlayClickListener() {
            @Override
            public void onGroundOverlayClick(GroundOverlay groundOverlay) {
                LatLng position = groundOverlay.getPosition();
                Toast.makeText(context, "this is my Toast message!!! =)", Toast.LENGTH_LONG).show();
                performGeolocationSearch(position);

            }
        });

        showList.setVisibility(View.GONE);


        /*
        This is for route activity
         *//*
        markerPoints = new ArrayList<LatLng>();
        // Setting onclick event listener for the map
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            *//*
            onMapClick will return the geocoordinate of the point tapped on the map
             *//*
            @Override
            public void onMapClick(LatLng point) {
                // Already 10 locations with 8 waypoints and 1 start location and 1 end location.
                // Upto 8 waypoints are allowed in a query for non-business users
                if (markerPoints.size() >= 10) {
                    return;
                }
                // Adding new item to the ArrayList
                markerPoints.add(point);

                // Creating MarkerOptions
                MarkerOptions options = new MarkerOptions();

                // Setting the position of the marker
                options.position(point);

                *//**
                 * For the start location, the color of marker is GREEN and
                 * for the end location, the color of marker is RED and
                 * for the rest of markers, the color is AZURE
                 *//*
                if (markerPoints.size() == 1) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else if (markerPoints.size() == 2) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                } else {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                }

                // Add new marker to the Google Map Android API V2
                googleMap.addMarker(options);
            }
        });*/

        // The map will be cleared on long click
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng point) {
                // Removes all the points from Google Map
                googleMap.clear();

                // Removes all the points in the ArrayList
                //markerPoints.clear();
                //createDialog();
            }
        });

        //------- For Routing----------------------
        // Getting reference to Button
        final Button btnDraw = (Button)findViewById(R.id.btn_draw);

        // Click event handler for Button btn_draw
        btnDraw.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Checks, whether start and end locations are captured
                if(markerPoints.size() >= 2){
                    LatLng origin = markerPoints.get(0);
                    LatLng dest = markerPoints.get(1);

                    // Getting URL to the Google Directions API
                    String url = generateGoogleAPIHelper.getDirectionsUrl(origin, dest,markerPoints);

                    GooglePlacesReadTask googlePlacesReadTask = new GooglePlacesReadTask();
                    Object[] toPass = new Object[4];
                    toPass[0] = googleMap;
                    toPass[1] = url;
                    toPass[2] = GooglePlacesReadTask.DIRECTION_QUERY;
                    toPass[3] = context;
                    googlePlacesReadTask.execute(toPass);
                    // Start downloading json data from Google Directions API
                    //downloadTask.execute(url);
                }
            }
        });
        //--------------- end of routing------------------

        //-----------Auto Complete Code----------------

        final AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.autocomplete_textview);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.fly_in_anim_from_top);
        textView.startAnimation(anim);


        textView.setAdapter(new GooglePlacesAutocompleteAdapter(this,R.layout.support_simple_spinner_dropdown_item,latitude,longitude));
        textView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
               if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                   // if user click enter search
                    performNearbyTextSearch(textView.getText().toString());
                    return true;
                }
                return false;
                }
            });

        //----------End of Auto complete implementation---------------


        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){

            @Override
            public void onMapClick(LatLng latLng) {
                if(displayMenu)
                {
                    Animation anim1 = AnimationUtils.loadAnimation(context, R.anim.fly_out_anim_to_top);
                    Animation anim2 = AnimationUtils.loadAnimation(context, R.anim.fly_out_anim_to_bottom);

                    textView.startAnimation(anim1);
                    btnDraw.startAnimation(anim2);
                    Log.e("AnimFlyOut","");
                    displayMenu = false;
                    textView.setVisibility(View.GONE);
                    btnDraw.setVisibility(View.GONE);
                    showList.setVisibility(View.GONE);

                }
                else
                {
                    Animation anim1 = AnimationUtils.loadAnimation(context, R.anim.fly_in_anim_from_top);
                    Animation anim2 = AnimationUtils.loadAnimation(context, R.anim.fly_in_anim_from_bottom);

                    textView.startAnimation(anim1);
                    btnDraw.startAnimation(anim2);
                    Log.e("AnimFlyIn","");
                    textView.setVisibility(View.VISIBLE);
                    btnDraw.setVisibility(View.VISIBLE);
                    if(searchPlaceCompleted)
                        showList.setVisibility(View.VISIBLE);
                    displayMenu = true;
                }
            }
        });

    }


    /*private boolean dismissDialog() {
        if (bottomDialog != null && bottomDialog.isShowing()) {
            bottomDialog.dismiss();
            return true;
        }

        return false;
    }



    /*
    Perform search query when user entered text
     */
    private void performGeolocationSearch(LatLng location)
    {
        // build the query url
        String userQuery = placeText.getText().toString();
        GenerateGoogleMapApiUrl urlGenerator = new GenerateGoogleMapApiUrl();
        StringBuilder googlePlacesUrl = urlGenerator.getGoogleMapPlacesQueryURL("",GenerateGoogleMapApiUrl.SEARCH_BY_GEOCOORDINATE,location.latitude,location.longitude);
        Log.d("Google Query",googlePlacesUrl.toString());

        // create the asynctask
        GooglePlacesReadTask googlePlacesReadTask = new GooglePlacesReadTask();
        Object[] toPass = new Object[4];
        toPass[0] = googleMap;
        toPass[1] = googlePlacesUrl.toString();
        toPass[2] = GooglePlacesReadTask.PLACE_QUERY;
        toPass[3] = context;
        googlePlacesReadTask.execute(toPass);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(searchPlaceCompleted) {
                googleMap.clear();
                showList.setVisibility(View.GONE);
                searchPlaceCompleted = false;
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /*
    Perform search query when user entered text
     */
    private void performNearbyTextSearch(String userQuery)
    {

        GenerateGoogleMapApiUrl urlGenerator = new GenerateGoogleMapApiUrl();
        StringBuilder googlePlacesUrl = urlGenerator.getGoogleMapPlacesQueryURL(userQuery,GenerateGoogleMapApiUrl.TEXT_SEARCH,latitude,longitude);
        Log.d("Google Query",googlePlacesUrl.toString());

        // create the asynctask
        GooglePlacesReadTask googlePlacesReadTask = new GooglePlacesReadTask();
        Object[] toPass = new Object[4];
        toPass[0] = googleMap;
        toPass[1] = googlePlacesUrl.toString();
        toPass[2] = GooglePlacesReadTask.PLACE_QUERY;
        toPass[3] = context;
        googlePlacesReadTask.execute(toPass);
    }

    /*
    Check if google play service is available
     */
    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    // START PERMISSION CHECK
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        String message = "osmdroid permissions:";
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            message += "\nLocation to show user location.";
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            message += "\nStorage access to store map tiles.";
        }
        if (!permissions.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            String[] params = permissions.toArray(new String[permissions.size()]);
            requestPermissions(params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        } // else: We already have permissions, so handle as normal
    }
    /*
    Returned result of the request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:	{
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION and WRITE_EXTERNAL_STORAGE
                Boolean location = perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                Boolean storage = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if (location && storage) {
                    // All Permissions Granted
                    Toast.makeText(MapsActivity.this, "All permissions granted", Toast.LENGTH_SHORT).show();
                } else if (location) {
                    Toast.makeText(this, "Storage permission is required to store map tiles to reduce data usage and for offline usage.", Toast.LENGTH_LONG).show();
                } else if (storage) {
                    Toast.makeText(this,"Location permission is required to show the user's location on map.", Toast.LENGTH_LONG).show();
                } else { // !location && !storage case
                    // Permission Denied
                    Toast.makeText(MapsActivity.this, "Storage permission is required to store map tiles to reduce data usage and for offline usage." +
                            "\nLocation permission is required to show the user's location on map.", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    // END PERMISSION CHECK


    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng)); //change the focus of the camera
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    @Override
    public void onTaskCompleted(Boolean response) {
        if(response)
        {
            searchPlaceCompleted = true;
            showList.setVisibility(View.VISIBLE);
            //Toast.makeText(context, "Task Completed", Toast.LENGTH_LONG).show();

        }
    }

}



