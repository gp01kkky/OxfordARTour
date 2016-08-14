package com.ar.oxford.oxfordtourar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
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
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ar.oxford.oxfordtourar.BottomViewLayout.BottomSheetBehaviorGoogleMapsLike;
import com.ar.oxford.oxfordtourar.BottomViewLayout.LayoutWrapContentUpdater;
import com.ar.oxford.oxfordtourar.MapHelper.GenerateGoogleMapApiUrl;
import com.ar.oxford.oxfordtourar.MapHelper.GooglePlacesAutocompleteAdapter;
import com.ar.oxford.oxfordtourar.MapHelper.GooglePlacesDisplayAdapterCustom;
import com.ar.oxford.oxfordtourar.Util.AppController;
import com.ar.oxford.oxfordtourar.Util.CustomTripDialogAdapter;
import com.ar.oxford.oxfordtourar.Util.DatabaseHelper;
import com.ar.oxford.oxfordtourar.model.Place;
import com.ar.oxford.oxfordtourar.model.PlaceAutoComplete;
import com.ar.oxford.oxfordtourar.model.Trip;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DisplayTripPlacesActivity extends AppCompatActivity implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    private GenerateGoogleMapApiUrl generateGoogleMapApiUrlHelper;
    private Context context;
    private String TAG = DisplayTripPlacesActivity.class.getSimpleName();

    private final int NO_RESULT_MODE =0;
    private final int DISPLAY_PLACE_LIST=1;
    private final int DISPLAY_PLACE_DETAIL=2;

    private Location mCurrentLocation;
    double latitude = 0;
    double longitude = 0;
    private GoogleApiClient mGoogleApiClient = null;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    LocationRequest mLocationRequest;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private int peekHeight = 240;

    private int currentDisplayMode = NO_RESULT_MODE;
    private int previousDisplayMode = NO_RESULT_MODE;

    // UI Declaration
    private Button bottomButton;
    private ImageView imgMyLocation;
    private RelativeLayout bottomBar = null;
    private GoogleMap googleMap;
    private UiSettings googleMapUiSettings;
    private AutoCompleteTextView autocompleteTextView;
    private BottomSheetBehavior placeListBottomSheetBehavior;
    BottomSheetBehaviorGoogleMapsLike bottomsheetbehaviorgoogle;
    private RelativeLayout searchBar;
    private Boolean displayMenu = true;
    private List<Place> mPlaceListMain; // to store place list

    private List<Place> mPlaceDetail; // to store placedetail

    public TextView placeNameTextView;
    LinearLayout price_row;
    LinearLayout open_time_row;
    LinearLayout phone_row;
    LinearLayout address_row;
    LinearLayout website_row;
    TextView priceTextView;
    TextView phoneTextView;
    TextView addressTextView;
    TextView websiteTextView;
    RatingBar ratingBar;
    View bottomSheetPlaceDetails;

    LinearLayout bottomSheetCOntentLayout;

    LayoutWrapContentUpdater layoutUpdater;

    ImageView imgPlaceDetail;
    Bitmap bitmap;
    // End UI Declaration

    Place currentPlaceDetail;//to store current place detail.


    CustomTripDialogAdapter customTripDialogAdapter;

    List<Place> storedPlaces;
    /**
     * Main method operation on first run
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Bundle bundle = getIntent().getExtras();
        //storedPlaces = bundle.getParcelableArrayList("places");

        //==============================================================================================
        // Generate Direction Section
        //==============================================================================================
        db = new DatabaseHelper(getApplicationContext());
        int tripId = getIntent().getExtras().getInt("trip_id");
        storedPlaces = db.getAllPlacesByTripWithoutPlaceTrip(tripId); // get all places list





        generateGoogleMapApiUrlHelper = new GenerateGoogleMapApiUrl();
        setContentView(R.layout.activity_display_trip_places);
        context = DisplayTripPlacesActivity.this;

        layoutUpdater = new LayoutWrapContentUpdater();
        //region For 3 stage bottomsheetview to display place details
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        bottomSheetPlaceDetails = coordinatorLayout.findViewById(R.id.bottom_sheet_place_detail);
        View bottomSheetPlaceList = findViewById(R.id.bottom_sheet);
        bottomSheetCOntentLayout = (LinearLayout) bottomSheetPlaceDetails.findViewById(R.id.bottom_sheet_content);

        placeListBottomSheetBehavior = BottomSheetBehavior.from(bottomSheetPlaceList);
        placeListBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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

        bottomsheetbehaviorgoogle = BottomSheetBehaviorGoogleMapsLike.from(bottomSheetPlaceDetails);
        bottomsheetbehaviorgoogle.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // React to state change
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



        //endregion end of 3-way bottomsheetview

        //-------------- UI Mapping --------------------------
        imgMyLocation = (ImageView) findViewById(R.id.imgMyLocation);
        bottomButton = (Button) findViewById(R.id.get_route); // for navigation

        //bottomBar = (RelativeLayout) findViewById(R.id.bottom_bar_layout);
        autocompleteTextView = (AutoCompleteTextView)
                findViewById(R.id.autocomplete_textview);

        phone_row = (LinearLayout) bottomSheetPlaceDetails.findViewById(R.id.phone_row);
        address_row = (LinearLayout) bottomSheetPlaceDetails.findViewById(R.id.address_row);
        website_row = (LinearLayout) bottomSheetPlaceDetails.findViewById(R.id.website_row);
        placeNameTextView = (TextView) bottomSheetPlaceDetails.findViewById(R.id.bottom_sheet_title);
        ratingBar = (RatingBar) bottomSheetPlaceDetails.findViewById(R.id.ratingBar);
        priceTextView = (TextView) bottomSheetPlaceDetails.findViewById(R.id.price);
        phoneTextView = (TextView) bottomSheetPlaceDetails.findViewById(R.id.phone);
        addressTextView = (TextView) bottomSheetPlaceDetails.findViewById(R.id.address);
        websiteTextView = (TextView) bottomSheetPlaceDetails.findViewById(R.id.website);
        imgPlaceDetail = (ImageView) findViewById(R.id.bottomsheet_backdrop2);
        imgPlaceDetail.setImageResource(R.drawable.no_image_backdrop);


        //-------------- End of UI Mapping --------------------

        placeListBottomSheetBehavior.setHideable(true);
        placeListBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        bottomsheetbehaviorgoogle.setHideable(true);
        bottomsheetbehaviorgoogle.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
        if (!isGooglePlayServicesAvailable()) {
            finish();
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
        googleMapUiSettings.setMyLocationButtonEnabled(false); // hide the mylocation button

        //region obtain google api client for fusedlocationproviderapi
        createLocationRequest();
        // This is needed for fusedLocationProviderApi
        if(mGoogleApiClient==null)
        {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .enableAutoManage(this, this)
                    .build();
        }

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        mCurrentLocation = locationManager.getLastKnownLocation(bestProvider); // get location
        if(mCurrentLocation!=null)
        {
            onLocationChanged(mCurrentLocation);
            getMyLocation();
        }
        //endregion



        //region AutoComplete
        //-----------Auto Complete Code----------------
        // this is for the clear button
        final ImageView autoCompleteImage = (ImageView) findViewById(R.id.search);
        autoCompleteImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                autocompleteTextView.setText(""); // clear textfield when clear button pressed
            }
        });
        searchBar = (RelativeLayout) findViewById(R.id.search_bar_layout);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.fly_in_anim_from_top);
        searchBar.startAnimation(anim);// let search bar fly in
        autocompleteTextView.setAdapter(new GooglePlacesAutocompleteAdapter(this,R.layout.autocomplete_row,latitude,longitude));
        autocompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO change to placeDetail
                PlaceAutoComplete selected = (PlaceAutoComplete) parent.getItemAtPosition(position);
                performPlaceDetailSearch(selected.getPlaceID());

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
        //endregion


        // mylocation button
        imgMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMyLocation();
            }
        });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                if(displayMenu)
                {
                    dismissMenu();
                }
                else
                {
                    appearMenu();
                }
            }
        });

        plotGoogleMap(storedPlaces);

        bottomButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(storedPlaces.size()>=2 && storedPlaces.size()<9){
                    List<LatLng> markers = new ArrayList<LatLng>();
                    LatLng origin = new LatLng(Double.parseDouble(storedPlaces.get(0).getLat()),Double.parseDouble(storedPlaces.get(0).getLng()));
                    LatLng dest = new LatLng(Double.parseDouble(storedPlaces.get(1).getLat()),Double.parseDouble(storedPlaces.get(1).getLng()));
                    for(int i=0;i<storedPlaces.size();i++)
                    {
                        LatLng newMarker = new LatLng(Double.parseDouble(storedPlaces.get(i).getLat()),Double.parseDouble(storedPlaces.get(i).getLng()));
                        markers.add(newMarker);
                    }
                    String url = generateGoogleMapApiUrlHelper.getDirectionsUrl(origin,dest, (ArrayList<LatLng>) markers);
                    performDirectionSearch(url);
                }
            }
        });

    }

    //region Android Location Update
    //------------ Android Location Update -----------------------
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    /**
     * Check if google play service is available
     * @return
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
    /**
     * For connect to Google Play Services
     */
    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }
    /**
     * For stopping Google Play Services
     */
    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    /*
    Called when googleapi client is called
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }
    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        latitude = mCurrentLocation.getLatitude();
        longitude = mCurrentLocation.getLongitude();
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }
    //endregion android location update

    /**
     * This is to perform search result based on user query
     * @param userQuery
     */
    private void performPlaceSearch(String userQuery)
    {
        StringBuilder googlePlacesUrl = generateGoogleMapApiUrlHelper.getGoogleMapPlacesQueryURL(userQuery,GenerateGoogleMapApiUrl.TEXT_SEARCH, mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
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
                                newPlacesList.add(newPlace);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        //dismiss keyboard
                        hideSoftKeyboard(DisplayTripPlacesActivity.this);
                        //dismiss autocomplete list
                        autocompleteTextView.dismissDropDown();
                        plotGoogleMap(newPlacesList);
                        mPlaceListMain = newPlacesList;
                        zoomToLocationSearchResult(newPlacesList);
                        if(newPlacesList.size()==1)
                        {
                            performPlaceDetailSearch(newPlacesList.get(0).getPlace_id());
                        }
                        else if(newPlacesList.size()>1)
                        {

                            if(currentDisplayMode==DISPLAY_PLACE_DETAIL)
                            {
                                bottomsheetbehaviorgoogle.setHideable(true);
                                bottomsheetbehaviorgoogle.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
                                previousDisplayMode = currentDisplayMode;
                            }
                            currentDisplayMode = DISPLAY_PLACE_LIST;
                            createPlacesListBottomSheet(newPlacesList);
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
        StringBuilder googlePlacesUrl = urlGenerator.getGoogleMapPlacesQueryURL(placeId,GenerateGoogleMapApiUrl.PLACE_DETAIL,mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
        Log.d("Google Query",googlePlacesUrl.toString());
        final List<Place> newPlacesList = new ArrayList<Place>();
        // Creating volley request obj
        final JsonObjectRequest placeDetailReq = new JsonObjectRequest(Request.Method.GET,googlePlacesUrl.toString(),null,
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
                            if(!obj.isNull("formatted_address"))
                            {
                                newPlace.setAddress(obj.getString("formatted_address"));
                            }
                            if(!obj.isNull("website"))
                            {
                                newPlace.setWebsite(obj.getString("website"));
                            }
                            newPlacesList.add(newPlace);

                            currentPlaceDetail = newPlace; // set currentPlaceDetail


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //dismiss keyboard
                        hideSoftKeyboard(DisplayTripPlacesActivity.this);
                        //dismiss autocomplete list
                        autocompleteTextView.dismissDropDown();
                        plotGoogleMap(newPlacesList);
                        mPlaceDetail = newPlacesList;
                        zoomToLocationSearchResult(newPlacesList);

                        if(currentDisplayMode ==DISPLAY_PLACE_LIST)
                        {
                            placeListBottomSheetBehavior.setHideable(true);
                            placeListBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            previousDisplayMode = currentDisplayMode;
                        }
                        currentDisplayMode = DISPLAY_PLACE_DETAIL;


                        bottomsheetbehaviorgoogle.setHideable(false);
                        bottomsheetbehaviorgoogle.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                        //region for putting the result into place details

                        placeNameTextView.setText(newPlacesList.get(0).getName()); // set place name
                        ratingBar.setRating(newPlacesList.get(0).getRating()); // set Rating

                        if(!newPlacesList.get(0).getPrice_level().equals(""))
                        {
                            String priceString="";
                            int price = Integer.parseInt(newPlacesList.get(0).getPrice_level()); // set price
                            for(int i=0;i<=price;i++)
                            {
                                priceString = priceString+"£";
                            }
                            priceTextView.setText(priceString);
                            /*priceTextView.requestLayout();
                            priceTextView.invalidate();*/
                            price_row.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            price_row.setVisibility(View.GONE);
                        }

                        if(!newPlacesList.get(0).getPhone_number().equals(""))
                        {

                            phoneTextView.setText(newPlacesList.get(0).getPhone_number()); // set phone number
                            /*phoneTextView.requestLayout();
                            phoneTextView.invalidate();*/
                            phone_row.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            phone_row.setVisibility(View.GONE);
                        }

                        if(!newPlacesList.get(0).getAddress().equals(""))
                        {

                            addressTextView.setText(newPlacesList.get(0).getAddress()); // set address
                            /*addressTextView.requestLayout();
                            addressTextView.invalidate();*/
                            address_row.setVisibility(View.VISIBLE);

                        }
                        else
                        {
                            phone_row.setVisibility(View.GONE);
                        }

                        if(!newPlacesList.get(0).getWebsite().equals(""))
                        {
                            websiteTextView.setText(newPlacesList.get(0).getWebsite()); // set website
                            /*websiteTextView.requestLayout();
                            websiteTextView.invalidate();*/
                            website_row.setVisibility(View.VISIBLE);

                        }
                        else
                        {
                            website_row.setVisibility(View.GONE);
                        }
                        //endregion
                        bottomSheetCOntentLayout.requestLayout();
                        bottomSheetCOntentLayout.invalidate();

                        layoutUpdater.wrapContentAgain(bottomSheetCOntentLayout,true);
                        imgPlaceDetail.requestLayout();
                        imgPlaceDetail.invalidate();
                        placePhotosTask(newPlacesList.get(0).getPlace_id());
                        //bottomSheetPlaceDetails.requestLayout();


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

    /*
    Make google map zoom to location search result
     */
    private void zoomToLocationSearchResult(List<Place> places) {

        if(places.size()>1)
        {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for(Place place:places)
            {
                LatLng latLng = new LatLng(Double.parseDouble(place.getLat()), Double.parseDouble(place.getLng()));
                builder.include(latLng);
            }
            LatLngBounds bounds = builder.build();

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,0);
            googleMap.animateCamera(cameraUpdate);
        }
        else if (places.size()==1)
        {
            LatLng latLng = new LatLng(Double.parseDouble(places.get(0).getLat()),Double.parseDouble(places.get(0).getLng()));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
            googleMap.animateCamera(cameraUpdate);
        }
    }

    /*
    Update the camera to user position
     */
    private void getMyLocation() {
        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        googleMap.animateCamera(cameraUpdate);
    }

    /**
     * To hidesoftkeyboard
     * @param activity
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    /**
     * Plot marker on google map
     * @param list
     */
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

    /**
     * Choose the google map marker based on type
     * @param type
     * @return
     */
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

    /**
     * Create list of places
     * @param mPlaceList
     */
    private void createPlacesListBottomSheet(final List<Place> mPlaceList)
    {
        GooglePlacesDisplayAdapterCustom adapter = new GooglePlacesDisplayAdapterCustom(mPlaceList);
        adapter.setOnItemClickListener(new GooglePlacesDisplayAdapterCustom.OnItemClickListener() {
            @Override
            public void onItemClick(GooglePlacesDisplayAdapterCustom.ItemHolder item, int position) {
                //dismissDialog();
                //Toast.makeText(context, mPlaceList.get(position).getName(), Toast.LENGTH_LONG).show();
                performPlaceDetailSearch(mPlaceList.get(position).getPlace_id());


            }
        });
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        placeListBottomSheetBehavior.setPeekHeight(peekHeight);
        placeListBottomSheetBehavior.setState(placeListBottomSheetBehavior.STATE_EXPANDED);
    }

    private void appearMenu()
    {
        Animation anim1 = AnimationUtils.loadAnimation(context, R.anim.fly_in_anim_from_top);
        searchBar.startAnimation(anim1);
        searchBar.setVisibility(View.VISIBLE);
        imgMyLocation.setVisibility(View.VISIBLE);
        displayMenu = true;
        /*if(currentDisplayMode ==DISPLAY_PLACE_LIST&&placeListBottomSheetBehavior.getState()==BottomSheetBehavior.STATE_HIDDEN)
        {
            placeListBottomSheetBehavior.setHideable(false);
            placeListBottomSheetBehavior.setState(placeListBottomSheetBehavior.STATE_COLLAPSED);
        }*/
        if(currentDisplayMode==DISPLAY_PLACE_LIST)
        {
            placeListBottomSheetBehavior.setHideable(false);
            placeListBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        else if (currentDisplayMode==DISPLAY_PLACE_DETAIL)
        {
            bottomsheetbehaviorgoogle.setHideable(false);
            bottomsheetbehaviorgoogle.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
        }
    }

    private void dismissMenu()
    {
        Animation anim1 = AnimationUtils.loadAnimation(context, R.anim.fly_out_anim_to_top);
        Animation anim2 = AnimationUtils.loadAnimation(context, R.anim.fly_out_anim_to_bottom);
        searchBar.startAnimation(anim1);
        searchBar.setVisibility(View.GONE);
        imgMyLocation.setVisibility(View.GONE);
        hideSoftKeyboard(DisplayTripPlacesActivity.this);
        displayMenu = false;
        /*if(currentDisplayMode==DISPLAY_PLACE_LIST&&(placeListBottomSheetBehavior.getState()==BottomSheetBehavior.STATE_COLLAPSED||placeListBottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED))
        {
            placeListBottomSheetBehavior.setHideable(true);
            placeListBottomSheetBehavior.setState(placeListBottomSheetBehavior.STATE_HIDDEN);
        }*/
        if(currentDisplayMode == DISPLAY_PLACE_LIST)
        {
            placeListBottomSheetBehavior.setHideable(true);
            placeListBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
        else if (currentDisplayMode==DISPLAY_PLACE_DETAIL)
        {
            bottomsheetbehaviorgoogle.setHideable(true);
            bottomsheetbehaviorgoogle.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
        }
    }

    /*
    This happen when the keyboard back key is pressed
    */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(currentDisplayMode==DISPLAY_PLACE_DETAIL){
                bottomsheetbehaviorgoogle.setHideable(true);
                bottomsheetbehaviorgoogle.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
                currentDisplayMode = previousDisplayMode;
                if(previousDisplayMode==DISPLAY_PLACE_LIST)
                {
                    previousDisplayMode = NO_RESULT_MODE;
                    placeListBottomSheetBehavior.setHideable(false);
                    placeListBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    // need to display the icon back
                    plotGoogleMap(mPlaceListMain);
                    zoomToLocationSearchResult(mPlaceListMain);
                    return true;

                }
                googleMap.clear();
                return true;
            }
            else if(currentDisplayMode==DISPLAY_PLACE_LIST)
            {
                placeListBottomSheetBehavior.setHideable(true);
                placeListBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                currentDisplayMode = previousDisplayMode;
                if(previousDisplayMode==DISPLAY_PLACE_DETAIL)
                {
                    previousDisplayMode = NO_RESULT_MODE;
                    bottomsheetbehaviorgoogle.setHideable(false);
                    bottomsheetbehaviorgoogle.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                    // need to display the icon back
                    plotGoogleMap(mPlaceDetail);
                    zoomToLocationSearchResult(mPlaceDetail);
                    return true;

                }
                googleMap.clear();
                return true;
            }
            else
            {
                return super.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
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

    private void placePhotosTask(String placeId) {
        //final String placeId = "ChIJrTLr-GyuEmsRBfy61i59si0"; // Australian Cruise Group

        // Create a new AsyncTask that displays the bitmap and attribution once loaded.
        new PhotoTask(imgPlaceDetail.getWidth(), imgPlaceDetail.getHeight()) {
            @Override
            protected void onPreExecute() {
                // Display a temporary image to show while bitmap is loading.
                imgPlaceDetail.setImageBitmap(getBitmapFromAssets("no_image_backdrop.png"));
                //imgPlaceDetail.setImageResource(R.drawable.no_image_backdrop);
                //imgPlaceDetail.invalidate();

            }

            @Override
            protected void onPostExecute(AttributedPhoto attributedPhoto) {
                if (attributedPhoto != null) {
                    // Photo has been loaded, display it.
                    imgPlaceDetail.setImageBitmap(attributedPhoto.bitmap);
                    // Display the attribution as HTML content if set.
                    /*if (attributedPhoto.attribution == null) {
                        mText.setVisibility(View.GONE);
                    } else {
                        mText.setVisibility(View.VISIBLE);
                        mText.setText(Html.fromHtml(attributedPhoto.attribution.toString()));
                    }
*/
                }
                imgPlaceDetail.invalidate();
            }
        }.execute(placeId);
    }

    abstract class PhotoTask extends AsyncTask<String, Void, PhotoTask.AttributedPhoto> {

        private int mHeight;

        private int mWidth;

        public PhotoTask(int width, int height) {
            mHeight = height;
            mWidth = width;
        }

        /**
         * Loads the first photo for a place id from the Geo Data API.
         * The place id must be the first (and only) parameter.*/

        @Override
        protected AttributedPhoto doInBackground(String... params) {
            if (params.length != 1) {
                return null;
            }
            final String placeId = params[0];
            AttributedPhoto attributedPhoto = null;

            PlacePhotoMetadataResult result = Places.GeoDataApi
                    .getPlacePhotos(mGoogleApiClient, placeId).await();

            if (result.getStatus().isSuccess()) {
                PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
                if (photoMetadataBuffer.getCount() > 0 && !isCancelled()) {
                    // Get the first bitmap and its attributions.
                    PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                    CharSequence attribution = photo.getAttributions();
                    // Load a scaled bitmap for this photo.
                    Bitmap image = photo.getScaledPhoto(mGoogleApiClient, mWidth, mHeight).await()
                            .getBitmap();

                    attributedPhoto = new AttributedPhoto(attribution, image);
                }
                // Release the PlacePhotoMetadataBuffer.
                photoMetadataBuffer.release();
            }
            return attributedPhoto;
        }

        /**
         * Holder for an image and its attribution.*/

        class AttributedPhoto {

            public final CharSequence attribution;

            public final Bitmap bitmap;

            public AttributedPhoto(CharSequence attribution, Bitmap bitmap) {
                this.attribution = attribution;
                this.bitmap = bitmap;
            }
        }
    }

    public Bitmap getBitmapFromAssets(String fileName) {
        AssetManager assetManager = getAssets();

        InputStream istr = null;
        try {
            istr = assetManager.open(fileName);
            Bitmap bitmap = BitmapFactory.decodeStream(istr);
            return bitmap;

        } catch (IOException e) {
            try {
                if(istr!=null)
                    istr.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return null;

    }

    /**
     * This is for the place details button click
     * @param v
     */
    public void loadMode(View v)
    {
        int id = v.getId();
        switch (id){
            case R.id.add_trip_button:
                //Toast.makeText(context, "Add trip", Toast.LENGTH_LONG).show();
                final Dialog dialog = new Dialog(context);
                final List<Trip> tripList = db.getAllTrip();
                dialog.setContentView(R.layout.custom_add_places_to_trip_dialog);
                customTripDialogAdapter = new CustomTripDialogAdapter(tripList);
                customTripDialogAdapter.setOnItemClickListener(new CustomTripDialogAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(CustomTripDialogAdapter.ItemHolder item, int position) {
                        Toast.makeText(context, tripList.get(position).getTripName(), Toast.LENGTH_LONG).show();
                        // this part
                        db.addPlaceToTrip(tripList.get(position),currentPlaceDetail);
                        dialog.dismiss();
                    }
                });
                RecyclerView recyclerView = (RecyclerView) dialog.findViewById(R.id.recyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(customTripDialogAdapter);
                dialog.show();
                break;
            case R.id.navigate_to_button:
                //Toast.makeText(context, "Navigate to", Toast.LENGTH_LONG).show();
                break;
            case R.id.create_new_trip_textview:
                final EditText taskEditText = new EditText(DisplayTripPlacesActivity.this);
                final AlertDialog innerDialog = new AlertDialog.Builder(DisplayTripPlacesActivity.this)
                        .setTitle("Add a new Trip")
                        .setView(taskEditText)
                        .setCancelable(false)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String tripName = String.valueOf(taskEditText.getText());
                                Boolean wantToCloseDialog = (tripName.toString().trim().isEmpty());
                                if(!wantToCloseDialog) {
                                    Trip newTrip = new Trip();
                                    newTrip.setTripName(tripName);
                                    db.createTrip(newTrip);
                                    updateUI();
                                    Log.d(TAG, "Trip to add: " + tripName);
                                }
                                else {

                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                innerDialog.show();
                break;
            default:
                break;
        }
    }

    private void updateUI() {
        List<Trip> tripList = db.getAllTrip();
        customTripDialogAdapter.updateResults(tripList);
    }


    private void performDirectionSearch(String url)
    {
        Log.d("QUERY", url);
        JsonObjectRequest directionReq = new JsonObjectRequest(Request.Method.GET,url,null,
                new Response.Listener<JSONObject>(){

                    @Override
                    public void onResponse(JSONObject result){
                        Log.d("JSON Error", result.toString());

                        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>();
                        JSONArray jRoutes = null;
                        JSONArray jLegs = null;
                        JSONArray jSteps = null;
                        try {

                            jRoutes = result.getJSONArray("routes");

                            /** Traversing all routes */
                            for(int i=0;i<jRoutes.length();i++){
                                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                                List path = new ArrayList<HashMap<String, String>>();

                                /** Traversing all legs */
                                for(int j=0;j<jLegs.length();j++){
                                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");

                                    /** Traversing all steps */
                                    for(int k=0;k<jSteps.length();k++){
                                        String polyline = "";
                                        polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                                        List<LatLng> list = decodePoly(polyline);

                                        /** Traversing all points */
                                        for(int l=0;l<list.size();l++){
                                            HashMap<String, String> hm = new HashMap<String, String>();
                                            hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                                            hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                                            path.add(hm);
                                        }
                                    }
                                    routes.add(path);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }catch (Exception e){
                        }

                        plotNavigationLine(routes);


                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                hidePDialog();

            }
        });
        AppController.getInstance().addToRequestQueue(directionReq);

    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    private void plotNavigationLine(List<List<HashMap<String, String>>> result)
    {
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;

        // Traversing through all the routes
        for(int i=0;i<result.size();i++){
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route
            for(int j=0;j<path.size();j++){
                HashMap<String,String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(2);
            lineOptions.color(Color.RED);
        }

        // Drawing polyline in the Google Map for the i-th route
        googleMap.addPolyline(lineOptions);
    }


    //==============================================================================================
    // Google Maps Display Navigation
    //==============================================================================================
    DatabaseHelper db;
}
