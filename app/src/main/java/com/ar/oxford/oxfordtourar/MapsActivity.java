package com.ar.oxford.oxfordtourar;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ar.oxford.oxfordtourar.MapHelper.GenerateGoogleMapApiUrl;
import com.ar.oxford.oxfordtourar.MapHelper.GooglePlacesAutocompleteAdapter;
import com.ar.oxford.oxfordtourar.MapHelper.GooglePlacesDisplayAdapter;
import com.ar.oxford.oxfordtourar.MapHelper.GooglePlacesDisplayAdapterCustom;
import com.ar.oxford.oxfordtourar.MapHelper.GooglePlacesReadTask;
import com.ar.oxford.oxfordtourar.MapHelper.OnTaskCompleted;
import com.ar.oxford.oxfordtourar.Util.AppController;
import com.ar.oxford.oxfordtourar.model.Place;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements LocationListener,OnTaskCompleted, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    private static Context context;
    private GoogleMap googleMap;
    private BottomSheetDialog placeListDialog;
    private UiSettings googleMapUiSettings;
    private GenerateGoogleMapApiUrl generateGoogleAPIHelper;
    GoogleApiClient mGoogleApiClient = null;
    AutoCompleteTextView textView;
    BottomSheetBehavior behavior;
    String TAG = MapsActivity.class.getSimpleName();

    EditText placeText;
    double latitude = 0;
    double longitude = 0;
    List<Place> mPlaceList;

    ArrayList<LatLng> markerPoints; // for multiple waypoint route

    private boolean displayMenu = true;
    private BottomSheetDialog bottomDialog;
    private Button bottomButton;
    private ImageView imgMyLocation;
    private RelativeLayout bottomBar = null;
    private boolean searchPlaceCompleted = false;
    private View placesListView;
    Location location = null;


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
        imgMyLocation = (ImageView) findViewById(R.id.imgMyLocation);
        bottomButton = (Button) findViewById(R.id.show_list);
        bottomBar = (RelativeLayout) findViewById(R.id.bottom_bar_layout);


        // This is needed to get last known location
        if(mGoogleApiClient==null)
        {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        //------------------------------------------------

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.googleMap);

        googleMap = fragment.getMap();
        // allows the ui of mylocation
        googleMap.setMyLocationEnabled(true);
        googleMapUiSettings = googleMap.getUiSettings();
        googleMap.setPadding(0,200,0,0);
        googleMapUiSettings.setCompassEnabled(true);
        googleMapUiSettings.setMyLocationButtonEnabled(false);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        location = locationManager.getLastKnownLocation(bestProvider); // get location
        if (location != null) {
            onLocationChanged(location);
        }
        else
        {
            locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);
        }

        // this activity is registered when user click on a ground overlay
        googleMap.setOnGroundOverlayClickListener(new GoogleMap.OnGroundOverlayClickListener() {
            @Override
            public void onGroundOverlayClick(GroundOverlay groundOverlay) {
                LatLng position = groundOverlay.getPosition();
                Toast.makeText(context, "this is my Toast message!!! =)", Toast.LENGTH_LONG).show();
                performGeolocationSearch(position);

            }
        });

        //bottomButton.setVisibility(View.GONE);


        //This is for route activity

        //markerPoints = new ArrayList<LatLng>();
        // Setting onclick event listener for the map
        /*googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {


            //onMapClick will return the geocoordinate of the point tapped on the map

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

               /* *
                 * For the start location, the color of marker is GREEN and
                 * for the end location, the color of marker is RED and
                 * for the rest of markers, the color is AZURE*/

                /*if (markerPoints.size() == 1) {
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



        //------- For Routing----------------------
        // Getting reference to Button
        //final Button btnDraw = (Button)findViewById(R.id.btn_draw);

        // Click event handler for Button btn_draw
        /*btnDraw.setOnClickListener(new View.OnClickListener() {

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
        });*/
        //--------------- end of routing------------------
        // The map will be cleared on long click
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng point) {
                // Removes all the points from Google Map
                googleMap.clear();
                searchPlaceCompleted = false;
                bottomButton.setText("EXPLORE AROUND ME");

                // Removes all the points in the ArrayList
                //markerPoints.clear();
                //createDialog();
            }
        });

        bottomButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                /*if(placeListDialog!=null)
                {
                    //placeListDialog.setContentView(placesListView);
                    placeListDialog.show();
                }*/

                if(searchPlaceCompleted) {
                    if(mPlaceList.size()>0) {
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }

                    //createDialog();
                }
            }
        });
        //-----------Auto Complete Code----------------

        textView = (AutoCompleteTextView)
                findViewById(R.id.autocomplete_textview);

        final ImageView autoCompleteImage = (ImageView) findViewById(R.id.search);
        autoCompleteImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                textView.setText("");
            }
        });

        final RelativeLayout searchBar = (RelativeLayout) findViewById(R.id.search_bar_layout);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.fly_in_anim_from_top);
        searchBar.startAnimation(anim);



        textView.setAdapter(new GooglePlacesAutocompleteAdapter(this,R.layout.autocomplete_row,latitude,longitude));

        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                performPlaceSearch(selected);
            }
        });
        // for creating cross button in search bar
        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()>0)
                {
                    autoCompleteImage.setImageResource(R.drawable.cross_icon);
                }
                else
                {
                    autoCompleteImage.setImageResource(R.drawable.search_icon);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        textView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
               if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                   // if user click enter search
                   performPlaceSearch(textView.getText().toString());
                    return true;
                }
                return false;
                }
            });
        //----------End of Auto complete implementation---------------

        // location button
        imgMyLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getMyLocation();
            }
        });


        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){

            @Override
            public void onMapClick(LatLng latLng) {
                if(displayMenu)
                {
                    Animation anim1 = AnimationUtils.loadAnimation(context, R.anim.fly_out_anim_to_top);
                    Animation anim2 = AnimationUtils.loadAnimation(context, R.anim.fly_out_anim_to_bottom);

                    searchBar.startAnimation(anim1);
                    //btnDraw.startAnimation(anim2);
                    hideSoftKeyboard(MapsActivity.this);
                    //Log.e("AnimFlyOut","");
                    displayMenu = false;
                    searchBar.setVisibility(View.GONE);
                    //btnDraw.setVisibility(View.GONE);
                    bottomButton.startAnimation(anim2);
                    imgMyLocation.startAnimation(anim2);
                    bottomButton.setVisibility(View.GONE);
                    imgMyLocation.setVisibility(View.GONE);
                    bottomBar.startAnimation(anim2);
                    bottomBar.setVisibility(View.GONE);
                    if(searchPlaceCompleted)
                    {
                        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }
                else
                {
                    Animation anim1 = AnimationUtils.loadAnimation(context, R.anim.fly_in_anim_from_top);
                    Animation anim2 = AnimationUtils.loadAnimation(context, R.anim.fly_in_anim_from_bottom);
                    searchBar.startAnimation(anim1);
                    bottomBar.startAnimation(anim2);
                    bottomButton.startAnimation(anim2);
                    imgMyLocation.startAnimation(anim2);
                    //btnDraw.startAnimation(anim2);
                    Log.e("AnimFlyIn","");
                    searchBar.setVisibility(View.VISIBLE);
                    bottomButton.setVisibility(View.VISIBLE);
                    imgMyLocation.setVisibility(View.VISIBLE);
                    //btnDraw.setVisibility(View.VISIBLE);
                    /*if(searchPlaceCompleted)
                        bottomButton.setVisibility(View.VISIBLE);*/
                    bottomBar.setVisibility(View.VISIBLE);
                    displayMenu = true;
                }
            }
        });

    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    private void getMyLocation() {
        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        googleMap.animateCamera(cameraUpdate);
    }



    /*
    This happen when the keyboard back key is pressed
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(searchPlaceCompleted){
                googleMap.clear();
                bottomButton.setText("EXPLORE AROUND ME");
                searchPlaceCompleted = false;
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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


    private void performPlaceSearch(String userQuery)
    {
        GenerateGoogleMapApiUrl urlGenerator = new GenerateGoogleMapApiUrl();
        StringBuilder googlePlacesUrl = urlGenerator.getGoogleMapPlacesQueryURL(userQuery,GenerateGoogleMapApiUrl.TEXT_SEARCH,latitude,longitude);
        Log.d("Google Query",googlePlacesUrl.toString());

        final List<Place> newPlacesList = new ArrayList<Place>();
        // Creating volley request obj
        JsonObjectRequest movieReq = new JsonObjectRequest(Request.Method.GET,googlePlacesUrl.toString(),null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject result) {
                        Log.d("JSON Error", result.toString());
                        hidePDialog();
                        JSONArray response = null;
                        try {
                           response = result.getJSONArray("results");
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                        // Parsing json
                        for (int i = 0; i < response.length(); i++) {
                            try {

                                JSONObject obj = response.getJSONObject(i);
                                Place newPlace = new Place();
                                if (!obj.isNull("name")) {
                                    newPlace.setName(obj.getString("name"));
                                }
                                if(!obj.isNull("rating"))
                                {
                                    newPlace.setRating(Float.parseFloat(obj.getString("rating")));
                                }
                                newPlace.setLat(obj.getJSONObject("geometry").getJSONObject("location").getString("lat"));
                                newPlace.setLng(obj.getJSONObject("geometry").getJSONObject("location").getString("lng"));
                                JSONArray types = obj.getJSONArray("types");
                                newPlace.setType(types.get(0).toString());
                                if(!obj.isNull("opening_hours"))
                                {
                                    newPlace.setOpen(obj.getJSONObject("opening_hours").getBoolean("open_now"));
                                }

                                if(!obj.isNull("photos"))
                                    newPlace.setPhoto_reference(obj.getJSONArray("photos").getJSONObject(0).getString("photo_reference"));

                                // adding movie to movies array
                                newPlacesList.add(newPlace);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        // notifying list adapter about data changes
                        // so that it renders the list view with updated data

                        searchPlaceCompleted = true;
                        bottomButton.setVisibility(View.VISIBLE);
                        if(newPlacesList.size()<1)
                        {
                            bottomButton.setText("No Places Found");
                        }
                        else
                        {
                            bottomButton.setText("Show List");
                        }
                        mPlaceList = newPlacesList;
                        //Toast.makeText(context, "Task Completed", Toast.LENGTH_LONG).show();
                        //this.placeListDialog = placeListDialog;
                        //this.placesListView = placesListView;
                        //createDialog();
                        hideSoftKeyboard(MapsActivity.this);
                        textView.dismissDropDown();
                        plotGoogleMap(newPlacesList);
                        zoomToLocationSearchResult(newPlacesList);
                        createBottomSheetTest(newPlacesList);

                        if(newPlacesList.size()>0)
                        {
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            //behavior.setPeekHeight(300);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                hidePDialog();

            }
        });
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(movieReq);
    }

    private void plotGoogleMap(List<Place> list)
    {
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

    private void createBottomSheetTest(final List<Place> mPlaceList)
    {
        View bottomSheet = findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull final View bottomSheet, int newState) {
                // React to state change
                bottomSheet.requestLayout();
                bottomSheet.invalidate();
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
            }
        });
        GooglePlacesDisplayAdapterCustom adapter = new GooglePlacesDisplayAdapterCustom(mPlaceList);
        adapter.setOnItemClickListener(new GooglePlacesDisplayAdapterCustom.OnItemClickListener() {
            @Override
            public void onItemClick(GooglePlacesDisplayAdapterCustom.ItemHolder item, int position) {
                //dismissDialog();
                Toast.makeText(context, mPlaceList.get(position).getName(), Toast.LENGTH_LONG).show();

            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private ProgressDialog pDialog;
    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
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
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(20));

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




    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        location = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng)); //change the focus of the camera
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onTaskCompleted(Boolean response, List<Place> placeList) {
        if(response)
        {
            searchPlaceCompleted = true;
            bottomButton.setVisibility(View.VISIBLE);
            if(placeList.size()<1)
            {
                bottomButton.setText("No Places Found");
            }
            else
            {
                bottomButton.setText("Show List");
            }
            mPlaceList = placeList;
            //Toast.makeText(context, "Task Completed", Toast.LENGTH_LONG).show();
            //this.placeListDialog = placeListDialog;
            //this.placesListView = placesListView;
            //createDialog();
            hideSoftKeyboard(MapsActivity.this);
            textView.dismissDropDown();
            createBottomSheet();
            zoomToLocationSearchResult(placeList);
            if(mPlaceList.size()>0)
            {
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                //behavior.setPeekHeight(300);
            }
        }
    }


    private void createBottomSheet()
    {
        View bottomSheet = findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull final View bottomSheet, int newState) {
                // React to state change
                    bottomSheet.requestLayout();
                    bottomSheet.invalidate();
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
            }
        });
        GooglePlacesDisplayAdapter adapter = new GooglePlacesDisplayAdapter(mPlaceList);
        adapter.setOnItemClickListener(new GooglePlacesDisplayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GooglePlacesDisplayAdapter.ItemHolder item, int position) {
                //dismissDialog();
                Toast.makeText(context, mPlaceList.get(position).getName(), Toast.LENGTH_LONG).show();

            }
        });
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    private void createDialog() {

        GooglePlacesDisplayAdapter adapter = new GooglePlacesDisplayAdapter(mPlaceList);
        adapter.setOnItemClickListener(new GooglePlacesDisplayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GooglePlacesDisplayAdapter.ItemHolder item, int position) {
                //dismissDialog();
                Toast.makeText(context, mPlaceList.get(position).getName(), Toast.LENGTH_LONG).show();

            }
        });

        View view = getLayoutInflater().inflate(R.layout.sheet_main, null);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


        placeListDialog = new BottomSheetDialog(this);
        placeListDialog.setContentView(view);
        placeListDialog.show();
    }

    private void zoomToLocationSearchResult(List<Place> places) {

        if(places.size()>1)
        {
            LatLng latLng = new LatLng(Double.parseDouble(places.get(0).getLat()),Double.parseDouble(places.get(0).getLng()));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
            googleMap.animateCamera(cameraUpdate);
        }
        else if (places.size()==1)
        {
            LatLng latLng = new LatLng(Double.parseDouble(places.get(0).getLat()),Double.parseDouble(places.get(0).getLng()));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
            googleMap.animateCamera(cameraUpdate);
        }
    }

}



