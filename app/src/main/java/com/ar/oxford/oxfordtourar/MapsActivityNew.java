package com.ar.oxford.oxfordtourar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ar.oxford.oxfordtourar.BottomViewLayout.BottomSheetBehaviorGoogleMapsLike;
import com.ar.oxford.oxfordtourar.MapHelper.GenerateGoogleMapApiUrl;
import com.ar.oxford.oxfordtourar.MapHelper.GooglePlacesAutocompleteAdapter;
import com.ar.oxford.oxfordtourar.MapHelper.GooglePlacesDisplayAdapterCustom;
import com.ar.oxford.oxfordtourar.MapHelper.GooglePlacesReadTask;
import com.ar.oxford.oxfordtourar.Util.AppController;
import com.ar.oxford.oxfordtourar.model.Place;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
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
import java.util.List;

public class MapsActivityNew extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    private static Context context;
    private GoogleMap googleMap;
    private BottomSheetDialog placeListDialog;
    private UiSettings googleMapUiSettings;
    private GenerateGoogleMapApiUrl generateGoogleAPIHelper;
    GoogleApiClient mGoogleApiClient = null;
    AutoCompleteTextView autocompleteTextView;
    BottomSheetBehavior behavior;
    String TAG = MapsActivityNew.class.getSimpleName();

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

    LocationRequest mLocationRequest;
    private static final int MILLISECONDS_PER_SECOND = 1000;

    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    public TextView bottomSheetTextView;
    BottomSheetBehaviorGoogleMapsLike bottomsheetbehaviorgoogle;
    LinearLayout price_row;
    LinearLayout open_time_row;
    LinearLayout phone_row;
    LinearLayout address_row;
    LinearLayout website_row;
    TextView priceTextView;
    TextView phoneTextView;
    TextView addressTextView;
    TextView websiteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        generateGoogleAPIHelper = new GenerateGoogleMapApiUrl(); // helper for google api
        //show error dialog if GooglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        setContentView(R.layout.activity_maps);
        context = MapsActivityNew.this;
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
        googleMapUiSettings.setMyLocationButtonEnabled(false); // hide the mylocation button

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
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);


        //-----------Auto Complete Code----------------
        autocompleteTextView = (AutoCompleteTextView)
                findViewById(R.id.autocomplete_textview);

        final ImageView autoCompleteImage = (ImageView) findViewById(R.id.search);
        autoCompleteImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                autocompleteTextView.setText(""); // clear textfield when clear button pressed
            }
        });
        final RelativeLayout searchBar = (RelativeLayout) findViewById(R.id.search_bar_layout);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.fly_in_anim_from_top);
        searchBar.startAnimation(anim);// let search bar fly in
        autocompleteTextView.setAdapter(new GooglePlacesAutocompleteAdapter(this,R.layout.autocomplete_row,latitude,longitude));
        autocompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                performPlaceSearch(selected);
            }
        });
        // switch to cross button when there is text on the autocomplete
        autocompleteTextView.addTextChangedListener(new TextWatcher() {
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
        autocompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // if user click search on the keyboard
                    performPlaceSearch(autocompleteTextView.getText().toString());
                    return true;
                }
                return false;
            }
        });
        //----------End of Auto complete implementation---------------

        // mylocation button
        imgMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMyLocation();
            }
        });

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


        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){

            @Override
            public void onMapClick(LatLng latLng) {
                if(displayMenu)
                {
                    Animation anim1 = AnimationUtils.loadAnimation(context, R.anim.fly_out_anim_to_top);
                    Animation anim2 = AnimationUtils.loadAnimation(context, R.anim.fly_out_anim_to_bottom);

                    searchBar.startAnimation(anim1);
                    //btnDraw.startAnimation(anim2);
                    hideSoftKeyboard(MapsActivityNew.this);
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

        // this activity is registered when user click on a ground overlay
        googleMap.setOnGroundOverlayClickListener(new GoogleMap.OnGroundOverlayClickListener() {
            @Override
            public void onGroundOverlayClick(GroundOverlay groundOverlay) {
                LatLng position = groundOverlay.getPosition();
                Toast.makeText(context, "this is my Toast message!!! =)", Toast.LENGTH_LONG).show();
                performGeolocationSearch(position);

            }
        });



     /*   Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(" ");
        }*/

        /**
         * If we want to listen for states callback
         */

        // this part for place detail bottomsheet
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet_place_detail);
        bottomsheetbehaviorgoogle = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet);
        bottomsheetbehaviorgoogle.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
                        Log.d("bottomsheet-", "STATE_COLLAPSED");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                        Log.d("bottomsheet-", "STATE_DRAGGING");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:
                        Log.d("bottomsheet-", "STATE_EXPANDED");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT:
                        Log.d("bottomsheet-", "STATE_ANCHOR_POINT");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN:
                        Log.d("bottomsheet-", "STATE_HIDDEN");
                        break;
                    default:
                        Log.d("bottomsheet-", "STATE_SETTLING");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        phone_row = (LinearLayout) bottomSheet.findViewById(R.id.phone_row);
        address_row = (LinearLayout) bottomSheet.findViewById(R.id.address_row);
        website_row = (LinearLayout) bottomSheet.findViewById(R.id.website_row);
        bottomSheetTextView = (TextView) bottomSheet.findViewById(R.id.bottom_sheet_title);
        priceTextView = (TextView) bottomSheet.findViewById(R.id.price);
        phoneTextView = (TextView) bottomSheet.findViewById(R.id.phone);
        addressTextView = (TextView) bottomSheet.findViewById(R.id.address);
        websiteTextView = (TextView) bottomSheet.findViewById(R.id.website);

        phoneTextView.setText("Hello World");


    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    /*
    Update the camera to user position
     */
    private void getMyLocation() {
        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        googleMap.animateCamera(cameraUpdate);
    }


    // TODO Fix the search complete behavior null problem
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
                                if(!obj.isNull("ratingTextView"))
                                {
                                    newPlace.setRating(Float.parseFloat(obj.getString("ratingTextView")));
                                }
                                newPlace.setLat(obj.getJSONObject("geometry").getJSONObject("location").getString("lat"));
                                newPlace.setLng(obj.getJSONObject("geometry").getJSONObject("location").getString("lng"));
                                JSONArray types = obj.getJSONArray("types");
                                newPlace.setType(types.get(0).toString());
                                if(!obj.isNull("opening_hours"))
                                {
                                    newPlace.setOpen(obj.getJSONObject("opening_hours").getBoolean("open_now"));
                                }
                                if(!obj.isNull("place_id"))
                                {
                                    newPlace.setPlace_id(obj.getString("place_id"));
                                }
                                if(!obj.isNull("photos"))
                                    newPlace.setPhoto_reference(obj.getJSONArray("photos").getJSONObject(0).getString("photo_reference"));

                                if(!obj.isNull("price_level"))
                                    newPlace.setPrice_level(obj.getString("price_level"));

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
                        hideSoftKeyboard(MapsActivityNew.this);
                        autocompleteTextView.dismissDropDown();
                        plotGoogleMap(newPlacesList);
                        zoomToLocationSearchResult(newPlacesList);

                        if(newPlacesList.size()>1)
                        {
                            createBottomSheetTest(newPlacesList);
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            //behavior.setPeekHeight(300);
                        }
                        else if(newPlacesList.size()==1)
                        {
                            performPlaceDetailSearch(newPlacesList.get(0).getPlace_id());
                            //bottomsheetbehaviorgoogle.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                            //bottomsheetbehaviorgoogle.setPeekHeight(240);
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

    private void performPlaceDetailSearch(String placeId)
    {
        GenerateGoogleMapApiUrl urlGenerator = new GenerateGoogleMapApiUrl();
        StringBuilder googlePlacesUrl = urlGenerator.getGoogleMapPlacesQueryURL(placeId,GenerateGoogleMapApiUrl.PLACE_DETAIL,latitude,longitude);
        Log.d("Google Query",googlePlacesUrl.toString());
        bottomsheetbehaviorgoogle.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
        bottomsheetbehaviorgoogle.setPeekHeight(240);
        phoneTextView.setText("hhhhh");
        final List<Place> newPlacesList = new ArrayList<Place>();
        // Creating volley request obj
        JsonObjectRequest placeDetailReq = new JsonObjectRequest(Request.Method.GET,googlePlacesUrl.toString(),null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject result) {
                        Log.d("JSON Error", result.toString());
                        hidePDialog();


                        // Parsing json
                        //for (int i = 0; i < response.length(); i++) {
                            try {

                                JSONObject obj = result.getJSONObject("result");
                                Place newPlace = new Place();
                                if (!obj.isNull("name")) {
                                    newPlace.setName(obj.getString("name"));
                                }
                                if(!obj.isNull("ratingTextView"))
                                {
                                    newPlace.setRating(Float.parseFloat(obj.getString("ratingTextView")));
                                }
                                newPlace.setLat(obj.getJSONObject("geometry").getJSONObject("location").getString("lat"));
                                newPlace.setLng(obj.getJSONObject("geometry").getJSONObject("location").getString("lng"));
                                JSONArray types = obj.getJSONArray("types");
                                newPlace.setType(types.get(0).toString());
                                if(!obj.isNull("opening_hours"))
                                {
                                    newPlace.setOpen(obj.getJSONObject("opening_hours").getBoolean("open_now"));
                                }
                                if(!obj.isNull("place_id"))
                                {
                                    newPlace.setPlace_id(obj.getString("place_id"));
                                }
                                if(!obj.isNull("price_level"))
                                {
                                    newPlace.setPrice_level(obj.getString("price_level"));
                                }
                                if(!obj.isNull("international_phone_number"))
                                {
                                    newPlace.setPhone_number(obj.getString("international_phone_number"));
                                }
                                if(!obj.isNull("formated_address"))
                                {
                                    newPlace.setAddress(obj.getString("formated_address"));
                                }
                                if(!obj.isNull("website"))
                                {
                                    newPlace.setWebsite(obj.getString("website"));
                                }
                                newPlacesList.add(newPlace);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }



                        // notifying list adapter about data changes
                        // so that it renders the list view with updated data
                        hideSoftKeyboard(MapsActivityNew.this);
                        autocompleteTextView.dismissDropDown();
                        plotGoogleMap(newPlacesList);
                        zoomToLocationSearchResult(newPlacesList);
                        if(!newPlacesList.get(0).getPrice_level().equals(""))
                        {
                            String priceString="";
                            int price = Integer.parseInt(newPlacesList.get(0).getPrice_level());
                            for(int i=0;i<=price;i++)
                            {
                                priceString = priceString+"£";
                            }
                            price_row.setVisibility(View.VISIBLE);
                            priceTextView.setText(priceString);
                        }
                        else
                        {
                            price_row.setVisibility(View.GONE);
                        }

                        if(!newPlacesList.get(0).getPhone_number().equals(""))
                        {

                            phone_row.setVisibility(View.VISIBLE);
                            phoneTextView.setText(newPlacesList.get(0).getPhone_number());
                        }
                        else
                        {
                            phone_row.setVisibility(View.GONE);
                        }

                        if(!newPlacesList.get(0).getAddress().equals(""))
                        {

                            address_row.setVisibility(View.VISIBLE);
                            addressTextView.setText(newPlacesList.get(0).getAddress());
                        }
                        else
                        {
                            phone_row.setVisibility(View.GONE);
                        }

                        if(!newPlacesList.get(0).getWebsite().equals(""))
                        {

                            website_row.setVisibility(View.VISIBLE);
                            websiteTextView.setText(newPlacesList.get(0).getWebsite());
                        }
                        else
                        {
                            website_row.setVisibility(View.GONE);
                        }

                        bottomsheetbehaviorgoogle.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                        bottomsheetbehaviorgoogle.setPeekHeight(240);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                hidePDialog();

            }
        });
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(placeDetailReq);
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

    @Override
    public void onLocationChanged(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
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



