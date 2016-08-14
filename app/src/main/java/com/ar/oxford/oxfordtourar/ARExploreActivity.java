package com.ar.oxford.oxfordtourar;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
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
import com.ar.oxford.oxfordtourar.Util.GetIconFromType;
import com.ar.oxford.oxfordtourar.Util.OnSwipeTouchListener;
import com.ar.oxford.oxfordtourar.model.Place;
import com.ar.oxford.oxfordtourar.model.PlaceAutoComplete;
import com.beyondar.android.fragment.BeyondarFragmentSupport;
import com.beyondar.android.opengl.colision.SquareMeshCollider;
import com.beyondar.android.opengl.util.LowPassFilter;
import com.beyondar.android.plugin.radar.RadarView;
import com.beyondar.android.plugin.radar.RadarWorldPlugin;
import com.beyondar.android.util.ImageUtils;
import com.beyondar.android.util.math.geom.Point3;
import com.beyondar.android.util.math.geom.Ray;
import com.beyondar.android.util.math.geom.Vector3;
import com.beyondar.android.view.BeyondarViewAdapter;
import com.beyondar.android.view.OnClickBeyondarObjectListener;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.BeyondarObjectList;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ARExploreActivity extends AppCompatActivity
        implements SensorEventListener,View.OnClickListener,OnClickBeyondarObjectListener, SeekBar.OnSeekBarChangeListener, NavigationView.OnNavigationItemSelectedListener, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    private String TAG = ARExploreActivity.class.getSimpleName();

    private BeyondarFragmentSupport mBeyondarFragment;
    private World mWorld;
    private RadarView radarView;
    private RadarWorldPlugin radarPlugin;

    private SeekBar distanceSeekBar;
    private SeekBar pushAwaySeekBar;
    private SeekBar pullCloserSeekBar;
    private SeekBar distanceFactorSeekBar;
    private TextView seekBarDistanceTextView;
    private TextView pushAwayTextView;
    private TextView pullCloserTextView;
    private TextView distanceFactorTextView;

    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private double latitude = 0;
    private double longitude = 0;
    private Location prevLocation;
    private boolean firstRun = true;
    private GetIconFromType getIconFromTypeHelper;


    private List<Place> mPlaceListMain; // to store placeMain
    private List<Place> mPlaceDetail; // to store placedetail
    private List<BeyondarObject> isFacingCameraList;
    private int viewSetting = TEXT_REVIEW_VIEW;
    private static final int TEXT_REVIEW_VIEW = 0;
    private static final int ICON_VIEW = 1;
    private static final int TEXT_VIEW = 2;
    private static final int IMAGE_VIEW = 3;
    private LinearLayout bottomSettingBar;

    private Point centrePoint;

    private HashMap<String, List<BeyondarObject>> listOfCollision;
    private GooglePlacesAutocompleteAdapter googlePlacesAutocompleteAdapter;
    private BottomSheetBehaviorGoogleMapsLike bottomsheetbehaviorgoogle;
    private LinearLayout bottomSheetCOntentLayout;
    private View bottomSheetPlaceDetails;


    private final int NO_RESULT_MODE =0;
    private final int DISPLAY_PLACE_LIST=1;
    private final int DISPLAY_PLACE_DETAIL=2;
    private int currentDisplayMode = NO_RESULT_MODE;
    private int previousDisplayMode = NO_RESULT_MODE;

    private GenerateGoogleMapApiUrl urlGenerator;

    public TextView placeNameTextView;
    private LinearLayout open_time_row;
    private LinearLayout phone_row;
    private LinearLayout address_row;
    private LinearLayout website_row;
    private TextView priceTextView;
    private TextView phoneTextView;
    private TextView addressTextView;
    private TextView websiteTextView;
    private RatingBar ratingBar;
    private ImageView imgPlaceDetail;
    private LayoutWrapContentUpdater layoutUpdater;
    private RelativeLayout extendButton;
    private LinearLayout topBar;

    private LinearLayout restaurantLayout;
    private LinearLayout barLayout;
    private LinearLayout cafeLayout;
    private LinearLayout universityLayout;
    private LinearLayout parkLayout;

    private int maxDistanceToRender = 20000;

    private float furthestPlace = 0;

    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;
    private UiSettings googleMapUiSettings;
    private ImageView myLocationButton;

    private GetIconFromType getIconHelper;
    int deviceWidth;
    int deviceHeight;
    FragmentTransaction ft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arexplore);
        urlGenerator = new GenerateGoogleMapApiUrl();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        windowmanager.getDefaultDisplay().getMetrics(displayMetrics);
        deviceWidth = displayMetrics.widthPixels;
        deviceHeight = displayMetrics.heightPixels;

        isFacingCameraList = new ArrayList<BeyondarObject>();
        getIconFromTypeHelper = new GetIconFromType();
        cleanTempFolder();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        centrePoint = getCentrePoint();
        listOfCollision = new HashMap<String, List<BeyondarObject>>();

        distanceButtonTextView = (TextView) findViewById(R.id.distanceTextButtonTextView);
        extendButton = (RelativeLayout) findViewById(R.id.extend_button_layout);

        // hide the setting Bar
        bottomSettingBar = (LinearLayout) findViewById(R.id.bottom_setting_bar);
        bottomSettingBar.setVisibility(View.GONE);
        layoutUpdater = new LayoutWrapContentUpdater();

        topBar = (LinearLayout) findViewById(R.id.top_bar);
        restaurantLayout = (LinearLayout) findViewById(R.id.restaurant);
        barLayout = (LinearLayout) findViewById(R.id.bar);
        cafeLayout = (LinearLayout) findViewById(R.id.cafe);
        universityLayout = (LinearLayout) findViewById(R.id.university);
        parkLayout = (LinearLayout) findViewById(R.id.park);

        restaurantLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        barLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPlaceTypeSearch("restaurant");
            }
        });

        cafeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPlaceTypeSearch("cafe");

            }
        });
        universityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPlaceTypeSearch("university");

            }
        });
        parkLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPlaceTypeSearch("park");

            }
        });

        extendButton.setOnTouchListener(new OnSwipeTouchListener(ARExploreActivity.this)
        {
            @Override
            public void onSwipeUp() {
                super.onSwipeUp();
                if(currentDisplayMode==DISPLAY_PLACE_DETAIL)
                {
                    bottomsheetbehaviorgoogle.setHideable(true);
                    bottomsheetbehaviorgoogle.setState(BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED);
                }
                else if (currentDisplayMode==DISPLAY_PLACE_LIST)
                {
                    placeListBottomSheetBehavior.setHideable(true);
                    placeListBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        mPlaceListMain = new ArrayList<>();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        initListeners();

        //==============================================================================================
        // Map View
        //==============================================================================================


        getIconHelper = new GetIconFromType();
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.googleMap);
        googleMap = mapFragment.getMap();
        // allows the ui of mylocation
        googleMap.setMyLocationEnabled(true);
        googleMapUiSettings = googleMap.getUiSettings();
        googleMap.setPadding(0,200,0,0);
        googleMapUiSettings.setCompassEnabled(true);
        googleMapUiSettings.setMyLocationButtonEnabled(false); // hide the mylocation button
        myLocationButton = (ImageView) findViewById(R.id.imgMyLocation);
        // mylocation button
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMyLocation();
            }
        });

        showGoogleMap=false;

        ft = getSupportFragmentManager().beginTransaction();
        ft.hide(mapFragment);
        ft.commit();
        myLocationButton.setVisibility(View.GONE);


        //==============================================================================================

        //==============================================================================================
        // AR View
        //==============================================================================================

        radarView = (RadarView) findViewById(R.id.radarView);
        mWorld = new World(this);
        //mWorld = CustomWorldHelper.generateObjects(this);

        mWorld.setDefaultImage(R.drawable.icn_attraction);
        mBeyondarFragment = (BeyondarFragmentSupport) getSupportFragmentManager().findFragmentById(R.id.beyondarFragment);
        mBeyondarFragment.setWorld(mWorld);
        mBeyondarFragment.setPullCloserDistance(10);
        LowPassFilter.ALPHA = (float) 0.010;

        // create Radar Plugin
        radarPlugin = new RadarWorldPlugin(this);
        // set the radar view in radar plugin
        radarPlugin.setRadarView(radarView);
        //set how far in meters we want to display in the view
        radarPlugin.setMaxDistance(300);

        // We can customize the color of the items
        radarPlugin.setListColor(CustomWorldHelper.LIST_TYPE_EXAMPLE_1, Color.RED);
        // and also the size
        radarPlugin.setListDotRadius(CustomWorldHelper.LIST_TYPE_EXAMPLE_1, 3);


        // this must be redeclared after the object created
        mWorld.addPlugin(radarPlugin);

        mBeyondarFragment.setWorld(mWorld);
        mBeyondarFragment.setOnClickBeyondarObjectListener(this);


        mBeyondarFragment.setDistanceFactor(2);
        mBeyondarFragment.setMaxDistanceToRender(20000);
        mBeyondarFragment.setPullCloserDistance(20);
        mBeyondarFragment.showFPS(true);
        //replaceImagesByStaticViews(mWorld);




        //==============================================================================================

        //toolbar.setVisibility(View.GONE);


        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




        //==============================================================================================
        // SeekBar
        //==============================================================================================
        distanceSeekBar = (SeekBar) findViewById(R.id.seekBar_distance);
        pullCloserSeekBar = (SeekBar) findViewById(R.id.seekBar_pullCloser);
        pushAwaySeekBar = (SeekBar) findViewById(R.id.seekBar_pushAway);
        distanceFactorSeekBar = (SeekBar) findViewById(R.id.seekBar_dstFactor);

        seekBarDistanceTextView = (TextView) findViewById(R.id.seekbar_distance_text);
        pushAwayTextView = (TextView) findViewById(R.id.seekBar_pushAway_text);
        pullCloserTextView = (TextView) findViewById(R.id.seekBar_pullCloser_text);
        distanceFactorTextView = (TextView) findViewById(R.id.seekBar_dstFactor_text);

        distanceSeekBar.setOnSeekBarChangeListener(this);
        pullCloserSeekBar.setOnSeekBarChangeListener(this);
        pushAwaySeekBar.setOnSeekBarChangeListener(this);
        distanceFactorSeekBar.setOnSeekBarChangeListener(this);

        distanceSeekBar.setMax(6000);
        distanceSeekBar.setProgress(150);
        pullCloserSeekBar.setMax(100);
        pullCloserSeekBar.setProgress(0);
        pushAwaySeekBar.setMax(100);
        pushAwaySeekBar.setProgress(0);
        distanceFactorSeekBar.setMax(50);
        distanceFactorSeekBar.setProgress(7);
        //==============================================================================================

        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .enableAutoManage(this, this)
                    .build();
        }
        //region obtain google api client for fusedlocationproviderapi
        createLocationRequest();

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        mCurrentLocation = locationManager.getLastKnownLocation(bestProvider); // get location
        if (mCurrentLocation != null) {
            onLocationChanged(mCurrentLocation);
        }
        mBeyondarFragment.showFPS(true);

        //==============================================================================================
        // AR object button
        //==============================================================================================
        mBeyondarFragment.setOnClickBeyondarObjectListener(this);
        CustomBeyondarViewAdapter customBeyondarViewAdapter = new CustomBeyondarViewAdapter(this);
        mBeyondarFragment.setBeyondarViewAdapter(customBeyondarViewAdapter);
        //==============================================================================================

        //==============================================================================================
        // Autocomplete
        //==============================================================================================
        final ActionBar action = getSupportActionBar(); //get the actionbar
        action.setDisplayShowCustomEnabled(true); //enable it to display a
        googlePlacesAutocompleteAdapter = new GooglePlacesAutocompleteAdapter(this,R.layout.autocomplete_row,this.latitude,this.longitude);

        // custom view in the action bar.
        action.setCustomView(R.layout.search_bar);//add the custom view
        action.setDisplayShowTitleEnabled(false); //hide the title
        edtSeach = (AutoCompleteTextView) action.getCustomView().findViewById(R.id.autocomplete_textview); //the text editor
        edtSeach.setDropDownWidth(-1);//autocomplete dropdown fill parent

        edtSeach.setAdapter(googlePlacesAutocompleteAdapter);
        edtSeach.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO change to placeDetail
                PlaceAutoComplete selected = (PlaceAutoComplete) parent.getItemAtPosition(position);
                // set distance to render because the search is up to 5000m
                radarPlugin.setMaxDistance(6000);
                mBeyondarFragment.setMaxDistanceToRender(6000);
                performPlaceDetailSearch(selected.getPlaceID());
            }
        });

        //this is a listener to do a search when the user clicks on search button
        edtSeach.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // if user click search on the keyboard
                    // set distance to render because the search is up to 5000m

                    performPlaceSearch(edtSeach.getText().toString());

                    return true;
                }
                return false;
            }
        });
        // switch to cross button when there is text on the autocomplete
        edtSeach.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    mSearchAction.setIcon(getResources().getDrawable(R.drawable.cross_icon));

                } else {
                    mSearchAction.setIcon(getResources().getDrawable(R.drawable.search_icon));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //==============================================================================================

        //==============================================================================================
        // bottomsheet place list settings
        //==============================================================================================
        View bottomSheetPlaceList = findViewById(R.id.bottom_sheet);
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
        placeListBottomSheetBehavior.setHideable(true);
        placeListBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        //==============================================================================================

        //==============================================================================================
        // bottomsheet place details settings
        //==============================================================================================
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        bottomSheetPlaceDetails = coordinatorLayout.findViewById(R.id.bottom_sheet_place_detail);
        bottomSheetCOntentLayout = (LinearLayout) bottomSheetPlaceDetails.findViewById(R.id.bottom_sheet_content);
        bottomsheetbehaviorgoogle = BottomSheetBehaviorGoogleMapsLike.from(bottomSheetPlaceDetails);

        // UI MAPPING
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

        bottomsheetbehaviorgoogle.setHideable(true);
        bottomsheetbehaviorgoogle.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
        bottomsheetbehaviorgoogle.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // React to state change
                switch (newState) {
                    case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
                        Log.d("bottomsheet-", "STATE_COLLAPSED");
                        action.show();
                        topBar.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                        Log.d("bottomsheet-", "STATE_DRAGGING");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:
                        Log.d("bottomsheet-", "STATE_EXPANDED");
                        action.hide();
                        topBar.setVisibility(View.GONE);

                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT:
                        Log.d("bottomsheet-", "STATE_ANCHOR_POINT");
                        action.hide();
                        topBar.setVisibility(View.GONE);

                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN:
                        Log.d("bottomsheet-", "STATE_HIDDEN");
                        topBar.setVisibility(View.VISIBLE);
                        action.show();
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

        //==============================================================================================

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.arexplore, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.simple_annotation:
                viewSetting = TEXT_VIEW;
                setARView(viewSetting);
                return true;
            case R.id.icon_annotation:
                viewSetting = ICON_VIEW;
                setARView(viewSetting);
                return true;
            case R.id.text_review_annotation:
                viewSetting = TEXT_REVIEW_VIEW;
                //mBeyondarFragment.setPullCloserDistance(10);
                setARView(viewSetting);
                return true;
            case R.id.image_annotation:
                viewSetting = IMAGE_VIEW;
                setARView(viewSetting);
                return true;
            case R.id.action_search:
                edtSeach.setText("");
                //handleMenuSearch();
                return true;
            case R.id.map_button:

                //ft = getSupportFragmentManager().beginTransaction();
                if(showGoogleMap==false)
                {
                    showGoogleMap=true;
                    ft = getSupportFragmentManager().beginTransaction();
                    ft.hide(mBeyondarFragment);
                    ft.show(mapFragment);
                    ft.commit();
                    myLocationButton.setVisibility(View.VISIBLE);
                }
                else
                {
                    showGoogleMap=false;
                    ft = getSupportFragmentManager().beginTransaction();
                    ft.hide(mapFragment);
                    ft.show(mBeyondarFragment);
                    ft.commit();
                    myLocationButton.setVisibility(View.GONE);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean showGoogleMap=false;

    final int NO_PRICE_SELECTED =6;
    int price_level1=NO_PRICE_SELECTED;
    int price_level2=NO_PRICE_SELECTED;
    int price_level3=NO_PRICE_SELECTED;
    int price_level4=NO_PRICE_SELECTED;

    int rating =0;

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id)
        {
            case R.id.rating_any:
                rating=0;
                if(currentDisplayMode==DISPLAY_PLACE_DETAIL)
                {

                }
                else
                {
                    generateWorld(mWorld,mPlaceListMain,viewSetting,true);
                }
                break;
            case R.id.rating_2:
                rating=1;
                if(currentDisplayMode==DISPLAY_PLACE_DETAIL)
                {

                }
                else
                {
                    generateWorld(mWorld,mPlaceListMain,viewSetting,true);
                }
                break;
            case R.id.rating_3:
                rating=2;
                if(currentDisplayMode==DISPLAY_PLACE_DETAIL)
                {

                }
                else
                {
                    generateWorld(mWorld,mPlaceListMain,viewSetting,true);
                }
                break;
            case R.id.rating_4:
                rating=3;
                if(currentDisplayMode==DISPLAY_PLACE_DETAIL)
                {

                }
                else
                {
                    generateWorld(mWorld,mPlaceListMain,viewSetting,true);
                }
                break;
            case R.id.open_now:
                rating=4;
                if(currentDisplayMode==DISPLAY_PLACE_DETAIL)
                {

                }
                else
                {
                    generateWorld(mWorld,mPlaceListMain,viewSetting,true);
                }
                break;
            case R.id.nav_route_planner:
                //item.setChecked(true);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mSearchAction = menu.findItem(R.id.action_search);
        return super.onPrepareOptionsMenu(menu);
    }

    private MenuItem mSearchAction;
    private boolean isSearchOpened = false;
    private AutoCompleteTextView edtSeach;


    //==============================================================================================
    // Seekbar Listener
    //==============================================================================================
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (radarPlugin == null) {
            return;
        }
        if (seekBar == this.distanceSeekBar) {
            seekBarDistanceTextView.setText("Max distance " + progress);
            mBeyondarFragment.setMaxDistanceToRender(progress);
            radarPlugin.setMaxDistance(progress);
        }
        if (seekBar == this.pullCloserSeekBar) {
            pullCloserTextView.setText("Pull Closer " + progress);
            mBeyondarFragment.setPullCloserDistance(progress);
        }
        if (seekBar == this.pushAwaySeekBar) {
            pushAwayTextView.setText("Push Away " + progress);
            mBeyondarFragment.setPushAwayDistance(progress);
        }
        if (seekBar == this.distanceFactorSeekBar) {
            distanceFactorTextView.setText("Distance Factor " + progress);
            mBeyondarFragment.setDistanceFactor(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
    //==============================================================================================

    //==============================================================================================
    // Google Location services
    //==============================================================================================

    /**
     * Check if google play service is available
     *
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

    /*
    Update the camera to user position
     */
    private void getMyLocation() {
        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        googleMap.animateCamera(cameraUpdate);
    }

    @Override
    public void onLocationChanged(Location location) {
        prevLocation = mCurrentLocation;

        mCurrentLocation = location;
        if(prevLocation==null)
        {
            prevLocation=mCurrentLocation;
        }
        latitude = mCurrentLocation.getLatitude();
        longitude = mCurrentLocation.getLongitude();
        googlePlacesAutocompleteAdapter.setLatitude(latitude);
        googlePlacesAutocompleteAdapter.setLongitude(longitude);

        /*mBeyondarFragment.setDistanceFactor(2);
        LowPassFilter.ALPHA = (float) 0.015;
        mBeyondarFragment.setMaxDistanceToRender(20000);
        mBeyondarFragment.setPullCloserDistance(20);*/
        //mWorld.setGeoPosition(latitude, longitude);
        // if distance of user greater than 80m then performsearch again also only change it when user not is search mode
        if ((locationChangedGreaterThan(150, prevLocation, mCurrentLocation) || firstRun)) {
            mWorld.setLocation(location);
            firstRun = false;
            if(currentDisplayMode==NO_RESULT_MODE)
            {
                performNearbySearch();
                // set distance to render because the search is up to 5000m
                radarPlugin.setMaxDistance(300);
                mBeyondarFragment.setMaxDistanceToRender(300);
                Toast.makeText(this, "Updated Nearby Places", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Location changed, reupdate", Toast.LENGTH_SHORT).show();
                generateWorld(mWorld,mPlaceListMain,viewSetting,true);
            }
        }
    }

    /**
     * Check whether the location is changed by how much
     *
     * @return
     */
    public boolean locationChangedGreaterThan(int metres, Location prevLocation, Location mCurrentLocation) {

        float diffDistance = mCurrentLocation.distanceTo(prevLocation);
        if (diffDistance > metres)
            return true;
        return false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(this);
        stopLocationUpdates();
        super.onPause();
    }

    @Override
    public void onResume() {
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
        initListeners();
        super.onResume();

    }

    @Override
    public void onDestroy()
    {
        mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    //==============================================================================================

    public void performNearbySearch() {
        urlGenerator.setProximityRadius(300);//in metres
        StringBuilder googlePlacesUrl = urlGenerator.getGoogleMapPlacesQueryURL("", GenerateGoogleMapApiUrl.NEARBY_PLACES, latitude, longitude);
        Log.d("Google Query", googlePlacesUrl.toString());

        final List<Place> newPlacesList = new ArrayList<Place>();
        // Creating volley request obj
        JsonObjectRequest placeReq = new JsonObjectRequest(Request.Method.GET, googlePlacesUrl.toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject result) {
                        Log.d("JSON Kelvin Error", result.toString());
                        JSONArray response = null;
                        try {
                            response = result.getJSONArray("results");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //mWorld.clearWorld();
                        // Parsing json
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                Place newPlace = new Place();
                                if (!obj.isNull("name")) {
                                    newPlace.setName(obj.getString("name"));
                                }
                                if (!obj.isNull("rating")) {
                                    newPlace.setRating(Float.parseFloat(obj.getString("rating")));
                                }
                                newPlace.setLat(obj.getJSONObject("geometry").getJSONObject("location").getString("lat"));
                                newPlace.setLng(obj.getJSONObject("geometry").getJSONObject("location").getString("lng"));
                                JSONArray types = obj.getJSONArray("types");
                                newPlace.setType(types.get(0).toString());
                                if (!obj.isNull("opening_hours")) {
                                    newPlace.setOpen(obj.getJSONObject("opening_hours").getBoolean("open_now"));
                                }
                                if (!obj.isNull("place_id")) {
                                    newPlace.setPlace_id(obj.getString("place_id"));
                                }
                                if (!obj.isNull("photos"))
                                    newPlace.setPhoto_reference(obj.getJSONArray("photos").getJSONObject(0).getString("photo_reference"));

                                if (!obj.isNull("price_level"))
                                    newPlace.setPrice_level(obj.getString("price_level"));
                                newPlacesList.add(newPlace);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        plotGoogleMap(newPlacesList);
                        mPlaceListMain = newPlacesList;
                        List<Place> filteredList = filterPlaceResult(mPlaceListMain);
                        // display to AR
                        zoomToLocationSearchResult(newPlacesList);
                        generateWorld(mWorld, mPlaceListMain,viewSetting, true);
                        mBeyondarFragment.setWorld(mWorld);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error", "Error: " + error.getMessage());
            }
        });
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(placeReq);
        //dismiss keyboard because it shows on application launch
        //hideSoftKeyboard(ARExploreActivity.this);
    }


    //==============================================================================================
    // ARObject static view
    //==============================================================================================
    private static final String TMP_IMAGE_PREFIX = "viewImage_";


    private void generateWorld(World world, List<Place> placeList,int viewSetting, boolean rerender) {
        String path = getTmpPath();
        if (rerender == true) {
            furthestPlace = 0;
            // data change, need rerender world
            mWorld.clearWorld();
            List<Place> filteredList = filterPlaceResult(placeList);
            cleanTempFolder();
            for (int i = 0; i < filteredList.size(); i++) {
                Place place = filteredList.get(i);
                //create object in AR world
                GeoObject newObject = new GeoObject(i);

                newObject.setGeoPosition(Double.parseDouble(place.getLat()), Double.parseDouble(place.getLng()));
                newObject.setName(trimString(place.getName()));
                Location placeLocation = new Location("");
                placeLocation.setLatitude(newObject.getLatitude());
                placeLocation.setLongitude(newObject.getLongitude());
                float distance = mCurrentLocation.distanceTo((placeLocation));
                newObject.setDistanceFromUser(distance);
                if(distance>furthestPlace&&distance<6000)
                {
                    furthestPlace=distance;
                }

                if (viewSetting == ICON_VIEW) {
                    //int icon_id = getIconFromTypeHelper.getIconIDFromType(place.getType());
                    newObject.setImageUri(getIconFromTypeHelper.getIconAssetFromType(placeList.get(i).getType()));
                } else {
                    //BitmapDescriptorFactory.fromBitmap(getIconFromTypeHelper.decodeSampledBitmapFromResource(getResources(),getIconFromTypeHelper.getIconIDFromType(place.getType()),32,32));
                    setARObjectImageWithStaticViews(newObject, i, path);
                }
                mWorld.addBeyondarObject(newObject);
            }
            //TODO: Done at adding the fix of the UI show nearest distance here
            showOnlyMainBeyondARObject(mWorld);

        } else {
            //just change the view set the image to become icon
            for (BeyondarObjectList beyondarList : world.getBeyondarObjectLists()) {
                for (int i = 0; i < beyondarList.size(); i++) {
                    BeyondarObject beyondarObject = beyondarList.get(i);
                    if (viewSetting == ICON_VIEW) {
                        //int icon_id = getIconFromTypeHelper.getIconIDFromType(mPlaceListMain.get(i).getType());
                        beyondarObject.setImageUri(getIconFromTypeHelper.getIconAssetFromType(mPlaceListMain.get(i).getType()));
                    } else {
                        setARObjectImageWithStaticViews(beyondarObject, i, path);
                    }
                }
            }
        }
        radarPlugin.setMaxDistance((int) furthestPlace+10);
        distanceSeekBar.setProgress((int) furthestPlace+10);
    }

    /**
     * Get the path to store temporally the images. Remember that you need to
     * set WRITE_EXTERNAL_STORAGE permission in your manifest in order to
     * write/read the storage
     */
    private String getTmpPath() {
        return getExternalFilesDir(null).getAbsoluteFile() + "/tmp/";
    }

    /**
     * Clean all the generated files
     */
    private void cleanTempFolder() {
        File tmpFolder = new File(getTmpPath());
        if (tmpFolder.isDirectory()) {
            String[] children = tmpFolder.list();
            for (int i = 0; i < children.length; i++) {
                if (children[i].startsWith(TMP_IMAGE_PREFIX)) {
                    new File(tmpFolder, children[i]).delete();
                }
            }
        }
    }

    public String trimString(String input) {
        String output = input.trim();
        output = output.substring(0, Math.min(output.length(), 20));
        output = output.replaceAll("\\s", "");
        output = output.replaceAll("/", "");
        return output;
    }

    /**
     * replace an arobject with static view
     *
     * @param beyondarObject
     * @param position
     * @param path
     */
    private void setARObjectImageWithStaticViews(BeyondarObject beyondarObject, int position, String path) {
        Place place = mPlaceListMain.get(position);
        // First let's get the view, inflate it and change some stuff
        View view = getLayoutInflater().inflate(R.layout.arview_object, null);
        TextView placeNameText = (TextView) view.findViewById(R.id.place_name);
        TextView distanceText = (TextView) view.findViewById(R.id.distance);
        TextView typeText = (TextView) view.findViewById(R.id.type);
        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
        LinearLayout arObjectLayout = (LinearLayout) view.findViewById(R.id.ar_object_layout);
        arObjectLayout.setAlpha(0.4f);

        Location placeLocation = new Location("");
        placeLocation.setLatitude(Double.parseDouble(place.getLat()));
        placeLocation.setLongitude(Double.parseDouble(place.getLng()));
        place.setDistance(mCurrentLocation.distanceTo(placeLocation));
        distanceText.setText(Integer.toString((int) (place.getDistance())) + " m");
        /*int test = (int) beyondarObject.getDistanceFromUser();
        distanceText.setText(Integer.toString(test) + " m");*/

        placeNameText.setText(place.getName());
        placeNameText.setMaxLines(1);
        int maxLength = 10;
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        placeNameText.setFilters(fArray);
        placeNameText.setEllipsize(TextUtils.TruncateAt.END);
        placeNameText.setSingleLine(true);


        typeText.setText(place.getType());
        ratingBar.setRating(4.0f);
        try {
            // Now that we have it we need to store this view in the
            // storage in order to allow the framework to load it when
            // it will be need it
            String imageName = TMP_IMAGE_PREFIX + beyondarObject.getName() + ".png";
            ImageUtils.storeView(view, path, imageName);
            Log.d("Set AR object", "name " + beyondarObject.getName());
            // If there are no errors we can tell the object to use the
            // view that we just stored
            beyondarObject.setImageUri(path + imageName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void replaceImagesByStaticViews(World world) {
        String path = getTmpPath();
        cleanTempFolder();
        isFacingCameraList.clear();
        for (BeyondarObjectList beyondarList : world.getBeyondarObjectLists()) {
            for (int i = 0; i < beyondarList.size(); i++) {
                BeyondarObject beyondarObject = beyondarList.get(i);
                Place place = mPlaceListMain.get(i);
                // First let's get the view, inflate it and change some stuff
                View view = getLayoutInflater().inflate(R.layout.arview_object, null);
                TextView placeNameText = (TextView) view.findViewById(R.id.place_name);
                TextView distanceText = (TextView) view.findViewById(R.id.distance);
                TextView typeText = (TextView) view.findViewById(R.id.type);
                RatingBar ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
                int test = (int) beyondarObject.getDistanceFromUser();
                placeNameText.setText(place.getName());
                distanceText.setText(Integer.toString(test) + " m");
                typeText.setText(place.getType());
                ratingBar.setRating(4.0f);
                try {
                    // Now that we have it we need to store this view in the
                    // storage in order to allow the framework to load it when
                    // it will be need it
                    String imageName = TMP_IMAGE_PREFIX + beyondarObject.getName() + ".png";
                    ImageUtils.storeView(view, path, imageName);
                    Log.d("Set AR object", "name " + beyondarObject.getName());
                    // If there are no errors we can tell the object to use the
                    // view that we just stored
                    beyondarObject.setImageUri(path + imageName);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (beyondarObject.isVisible()) {
                    isFacingCameraList.add(beyondarObject);
                }
            }
        }
        //getFacingCameraList(isFacingCameraList);
    }

    /**
     * For changing AR the view
     *
     * @param viewSetting
     */
    public void setARView(int viewSetting) {
        // rerender the view without recreate the object
        if(currentDisplayMode==DISPLAY_PLACE_DETAIL)
            generateWorld(mWorld, mPlaceDetail,viewSetting, false);
        else
            generateWorld(mWorld, mPlaceListMain,viewSetting, false);

    }
    //==============================================================================================

    /*private void replaceImagesByStaticViews(World world) {
        String path = getTmpPath();
        for (BeyondarObjectList beyondarList : world.getBeyondarObjectLists()) {
            for (int i=0; i<beyondarList.size();i++) {
                BeyondarObject beyondarObject = beyondarList.get(i);
                //Place place = mPlaceListMain.get(i);
                // First let's get the view, inflate it and change some stuff
                View view = getLayoutInflater().inflate(R.layout.arview_object, null);
                TextView placeNameText = (TextView) view.findViewById(R.id.place_name);
                TextView distanceText = (TextView) view.findViewById(R.id.distance);
                TextView typeText = (TextView) view.findViewById(R.id.type);
                RatingBar ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
                int test = (int) beyondarObject.getDistanceFromUser();
                placeNameText.setText(beyondarObject.getName());
                distanceText.setText(Integer.toString(test) + " m");
                typeText.setText("University");
                ratingBar.setRating(4.0f);
                try {
                    // Now that we have it we need to store this view in the
                    // storage in order to allow the framework to load it when
                    // it will be need it
                    String imageName = TMP_IMAGE_PREFIX + beyondarObject.getName() + ".png";
                    ImageUtils.storeView(view, path, imageName);

                    // If there are no errors we can tell the object to use the
                    // view that we just stored
                    beyondarObject.setImageUri(path + imageName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/

    public Point getDisplay() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        int midWidth = width / 2;
        int midHeight = height / 2;

        Point newPoint = new Point(midWidth, midHeight);
        return newPoint;
    }

    public void getFacingCameraList(List<BeyondarObject> list) {
        String text = "";
        for (int i = 0; i < list.size(); i++) {
            text = text + "," + list.get(i).getName();
        }
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    /*attach touch to the object*/
    @Override
    public void onClickBeyondarObject(ArrayList<BeyondarObject> beyondarObjects) {
        if (beyondarObjects.size() > 0) {
            Toast.makeText(this, "Clicked on: " + beyondarObjects.get(0).getId(), Toast.LENGTH_LONG).show();
            if(mPlaceDetail!=null)
            {
                if(currentDisplayMode!=DISPLAY_PLACE_DETAIL)
                {
                    if(!mPlaceListMain.get((int)beyondarObjects.get(0).getId()).getName().equals(mPlaceDetail.get(0).getName()))
                    {
                        performPlaceDetailSearch(mPlaceListMain.get((int)beyondarObjects.get(0).getId()).getPlace_id());
                    }
                    else
                    {
                        generateWorld(mWorld,mPlaceDetail,viewSetting,true);
                        previousDisplayMode=currentDisplayMode;
                        currentDisplayMode=DISPLAY_PLACE_DETAIL;
                        bottomsheetbehaviorgoogle.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                    }
                }
                else
                {

                    bottomsheetbehaviorgoogle.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                }
            }
            else
            {
                performPlaceDetailSearch(mPlaceListMain.get((int)beyondarObjects.get(0).getId()).getPlace_id());

            }
        }
    }

    /*
    Left and Right Button
     */
    private TextView distanceButtonTextView;
    public void loadButton(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.left_button:
                /*List<BeyondarObject> beyondarObjectList = mBeyondarFragment.getBeyondarObjectsOnScreenCoordinates(centrePoint.x,centrePoint.y);
                getFacingCameraList(beyondarObjectList);*/
                //checkIfvisible(mWorld);
                //getSizeOfObjectInWorld(mWorld);
                Toast.makeText(this, "clicked left", Toast.LENGTH_SHORT).show();

                if(bottomSettingBar.isShown())
                {
                    distanceButtonTextView.setTextColor(Color.parseColor("#FFFFFF"));
                    bottomSettingBar.setVisibility(View.GONE);
                }
                else
                {
                    distanceButtonTextView.setTextColor(Color.parseColor("#FFFF00"));
                    bottomSettingBar.setVisibility(View.VISIBLE);

                }
                break;
            case R.id.right_button:
                //performNearbySearch();
                //getSizeOfObjectInWorld(mWorld);
                showOnlyMainBeyondARObject(mWorld);

                //checkIfvisible(mWorld);
                break;
            case R.id.radarLayout:
                performNearbySearch();
                break;
            default:
                break;
        }
    }

    public void loadTopBar(View v){
        int id = v.getId();
        switch (id) {
            case R.id.restaurant:
                performPlaceTypeSearch("restaurant");
                break;
            case R.id.bar:
                performPlaceTypeSearch("bar");
                break;
            case R.id.cafe:
                performPlaceTypeSearch("cafe");
                break;
            case R.id.university:
                performPlaceTypeSearch("university");
                break;
            case R.id.park:
                performPlaceTypeSearch("park");
                break;
            default:
                break;
        }
    }

    public void showOnlyMainBeyondARObject(World world) {
        listOfCollision.clear();//always clear the list of collision hashmap
        Toast.makeText(this, "applying fix", Toast.LENGTH_SHORT).show();
        //List<List<BeyondarObject>> listOfCollision = new ArrayList<>(); // list of list of collision
        for (BeyondarObjectList beyondarList : world.getBeyondarObjectLists()) {
            // copy the list to another list
            //Toast.makeText(this, "List1", Toast.LENGTH_SHORT).show();

            String text = "";
            for (int i = 0; i < beyondarList.size(); i++) {
                // if the object is visible
                if (beyondarList.get(i).isVisible()) {
                    //List<BeyondarObject> collsionListdetect = mBeyondarFragment.getBeyondarObjectsOnScreenCoordinates(beyondarList.get(i).getPosition().x, beyondarList.get(i).getPosition().y);
                    //SquareMeshCollider beyondARObjectCollider = new SquareMeshCollider(beyondarList.get(i).getTopLeft(),beyondarList.get(i).getBottomLeft(),beyondarList.get(i).getBottomRight(),beyondarList.get(i).getTopRight());
                    //MeshCollider beyondARObjectCollider = beyondarList.get(i).getMeshCollider();

                    List<BeyondarObject> collisionList = new ArrayList<BeyondarObject>();// list of collision

                    text = text + beyondarList.get(i).getName()+ " collision ";
                    collisionList.add(beyondarList.get(i));

                    // check which they collide
                    for (int j = i + 1; j < beyondarList.size(); j++) {
                        //SquareMeshCollider beyondARObjectCollider2 = new SquareMeshCollider(beyondarList.get(j).getTopLeft(),beyondarList.get(j).getBottomLeft(),beyondarList.get(j).getBottomRight(),beyondarList.get(j).getTopRight());
                        if(beyondarList.get(j).isVisible())
                            if (checkCollision(beyondarList.get(i),beyondarList.get(j))) {
                                //if (beyondARObjectCollider.contains(beyondarList.get(j).getPosition())||beyondARObjectCollider2.contains(beyondarList.get(i).getPosition())) {
                                // they collides so add to collision list;
                                collisionList.add(beyondarList.get(j));
                                //text = text + beyondarList.get(j).getName() + ",";
                            }
                    }

                    //get the nearest object
                    if (collisionList.size() > 1) {
                        BeyondarObject nearest = getNearestARObject(collisionList);
                        // hide the rest only show nearest
                        for (int k = 0; k < collisionList.size(); k++) {
                            if (!(collisionList.get(k).getName().equals(nearest.getName()))) {
                                collisionList.get(k).setVisible(false);
                                //Log.d("Invisible", collisionList.get(k).getName());
                            }
                            else
                            {
                                collisionList.get(k).setVisible(true);
                            }
                        }

                        //put it into listOfCollision with the nearest object as key
                        listOfCollision.put(nearest.getName(), collisionList);

                        // for display testing
                        for (int k = 0; k < collisionList.size(); k++) {
                            text = text + collisionList.get(k).getName()+",";
                        }

                    }
                    text = text + "\n";
                    //printCollisionList(collisionList,i,beyondarList.get(i));
                }
            }
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }

    }

    public void getSizeOfObjectInWorld(World world) {
        String text = "";
        for (BeyondarObjectList beyondarList : world.getBeyondarObjectLists()) {
            // copy the list to another list
            for (int i = 0; i < beyondarList.size(); i++)
                text = text + beyondarList.get(i).getName() + beyondarList.get(i).getTopLeft() + beyondarList.get(i).getTopRight() +   "\n";
        }
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();

    }

    public void printCollisionList(List<BeyondarObject> beyondarObjectsList, int i, BeyondarObject originalCollision) {
        String text = originalCollision.getName() + " col with ";
        for (BeyondarObject beyondarObject : beyondarObjectsList) {
            text = text + beyondarObject.getName();
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
    }

    // this works
    public void checkIfvisible(World world) {
        String text = "";

        List<BeyondarObject> centreObject = mBeyondarFragment.getBeyondarObjectsOnScreenCoordinates(centrePoint.x, centrePoint.y);
        for (int i = 0; i < centreObject.size(); i++) {
            text = text + "," + centreObject.get(i).getName();
        }
        //isFacingCameraList.add(beyondarObject);



        Toast.makeText(this,text,Toast.LENGTH_SHORT).show();

        /*int nearestDistance = 10000;
        BeyondarObject nearest = null;
        for (BeyondarObject beyondarObject : centreObject) {
            if (beyondarObject.getDistanceFromUser() < nearestDistance) {
                nearestDistance = (int) beyondarObject.getDistanceFromUser();
                nearest = beyondarObject;
            }
        }*/
    }


    public boolean checkCollision(BeyondarObject a, BeyondarObject b)
    {

        SquareMeshCollider beyondARObjectColliderA = new SquareMeshCollider(a.getTopLeft(),a.getBottomLeft(),a.getBottomRight(),a.getTopRight());
        SquareMeshCollider beyondARObjectColliderB = new SquareMeshCollider(b.getTopLeft(),b.getBottomLeft(),b.getBottomRight(),b.getTopRight());

        //a.setPosition(a.getPosition().x,a.getPosition().y,a.getPosition().z);
        //b.setPosition(b.getPosition().x,b.getPosition().y,a.getPosition().z);

        Point3 aobjectLeft = new Point3(a.getBottomLeft().x,a.getPosition().y,a.getPosition().z);
        Point3 aobjectRight = new Point3(a.getBottomRight().x,a.getPosition().y,a.getPosition().z);

        Point3 bobjectLeft = new Point3(b.getBottomLeft().x,b.getPosition().y,b.getPosition().z);
        Point3 bobjectRight = new Point3(b.getBottomRight().x,b.getPosition().y,b.getPosition().z);





        BeyondarObject nearest = getNearestARObjectBetweenTwo(a,b);
        Point3 nearestobjectLeft = new Point3(nearest.getBottomLeft().x,nearest.getPosition().y,nearest.getPosition().z);
        Point3 nearestobjectRight = new Point3(nearest.getBottomRight().x,nearest.getPosition().y,nearest.getPosition().z);

        Point3 angle = nearest.getAngle();
        int magnitude = 50;

        float angleX = (float) (magnitude*Math.cos(angle.x));
        float angleY = (float) (magnitude*Math.cos(angle.y));
        float angleZ = (float) (magnitude*Math.cos(angle.z));

        Vector3 vectorA = new Vector3(angleX,angleY,angleZ);

        Ray rayforwardcentre = new Ray(nearest.getPosition(),vectorA); // centre ray
        Ray rayforwardTopLeft = new Ray(nearest.getTopLeft(),vectorA);
        Ray rayforwardTopRight = new Ray(nearest.getTopRight(),vectorA);
        Ray rayforwardBottomLeft = new Ray(nearest.getBottomLeft(),vectorA);
        Ray rayforwardBottomRight = new Ray(nearest.getBottomRight(),vectorA);
        Ray raynearestobjectLeft = new Ray(nearestobjectLeft,vectorA);
        Ray raynearestobjectRight = new Ray(nearestobjectRight,vectorA);


        //BeyondarObject further = null;
        if(nearest.getName().equals(a.getName()))
        {
            //further=b;
            if(beyondARObjectColliderB.intersects(rayforwardcentre)||beyondARObjectColliderB.intersects(rayforwardTopLeft)||beyondARObjectColliderB.intersects(rayforwardTopRight)||beyondARObjectColliderB.intersects(rayforwardBottomLeft)||beyondARObjectColliderB.intersects(rayforwardBottomRight)||beyondARObjectColliderB.intersects(raynearestobjectLeft)||beyondARObjectColliderB.intersects(raynearestobjectRight))
                return true;
        }
        else
        {
            //further=a;
            if(beyondARObjectColliderA.intersects(rayforwardcentre)||beyondARObjectColliderA.intersects(rayforwardTopLeft)||beyondARObjectColliderA.intersects(rayforwardTopRight)||beyondARObjectColliderA.intersects(rayforwardBottomLeft)||beyondARObjectColliderA.intersects(rayforwardBottomRight)||beyondARObjectColliderA.intersects(raynearestobjectLeft)||beyondARObjectColliderA.intersects(raynearestobjectRight))
                return true;
        }


        // check bottom left or bottom right of B in A
        if(beyondARObjectColliderA.contains(b.getBottomLeft())||beyondARObjectColliderA.contains(b.getBottomRight()))
            return true;
        // check top left or top right of B is in A
        if(beyondARObjectColliderA.contains(b.getTopLeft())||beyondARObjectColliderA.contains(b.getTopRight()))
            return true;
        // check bottom left or bottom right of A is in B
        if(beyondARObjectColliderB.contains(a.getBottomLeft())||beyondARObjectColliderB.contains(a.getBottomRight()))
            return true;
        // check top left or top right of A is in B
        if(beyondARObjectColliderB.contains(a.getTopLeft())||beyondARObjectColliderB.contains(a.getTopRight()))
            return true;
        // in case 2 same size and same position, then we check centre point
        if(beyondARObjectColliderA.contains(b.getPosition())||beyondARObjectColliderB.contains(a.getPosition()))
            return true;
        // in case 2 same size, then we check whether A collide B left and right
        if(beyondARObjectColliderA.contains(bobjectLeft)||beyondARObjectColliderA.contains(bobjectRight))
            return true;
        // in case 2 same size then we check whether B collide A left and right
        if(beyondARObjectColliderB.contains(aobjectLeft)||beyondARObjectColliderB.contains(aobjectRight))
            return true;
        return false;
    }


    public BeyondarObject getNearestARObjectBetweenTwo(BeyondarObject a, BeyondarObject b)
    {
        if(a.getDistanceFromUser()<b.getDistanceFromUser())
        {
            return a;
        }
        else
        {
            return b;
        }
    }

    /**
     * get the nearest beyondarobject
     * @param list
     * @return
     */
    public BeyondarObject getNearestARObject(List<BeyondarObject> list)
    {
        int nearestDistance = 10000;
        BeyondarObject nearest = null;
        for (BeyondarObject beyondarObject : list) {
            if (beyondarObject.getDistanceFromUser() < nearestDistance) {
                nearestDistance = (int) beyondarObject.getDistanceFromUser();
                nearest = beyondarObject;
            }
        }
        return nearest;
    }

    public Point getCentrePoint()
    {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        int midWidth = width/2;
        int midHeight = height/2;

        Point newPoint = new Point(midWidth,midHeight);
        return newPoint;
    }



    //==============================================================================================
    // ARObject attach button
    //==============================================================================================
    @Override
    public void onClick(View v) {
        String position=(String) v.getTag();
        Toast.makeText(this, "Click" + position, Toast.LENGTH_SHORT).show();
        //get the places that are collided in this section
        List<BeyondarObject> list = listOfCollision.get(position);
        //create new places list to store
        List<Place> placeList = new ArrayList<Place>();
        for(int i=0;i<list.size();i++)
        {
            int positionId = (int) list.get(i).getId();
            Place newPlace = mPlaceListMain.get(positionId);
            placeList.add(newPlace);
        }
        createPlacesListBottomSheet(placeList);
    }



    private class CustomBeyondarViewAdapter extends BeyondarViewAdapter {

        LayoutInflater inflater;

        public CustomBeyondarViewAdapter(Context context) {
            super(context);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(BeyondarObject beyondarObject, View recycledView, ViewGroup parent) {
            // this one control when to show, if it has more than one item
            if(!listOfCollision.containsKey(beyondarObject.getName()))
            {
                return null;
            }

            if (recycledView == null) {
                recycledView = inflater.inflate(R.layout.ar_object_button, null);
            }
            FloatingActionButton button = (FloatingActionButton) recycledView.findViewById(R.id.fab);
            button.setOnClickListener(ARExploreActivity.this);
            button.setTag(beyondarObject.getName()); // set the button tag as arobject name

            // Once the view is ready we specify the position
            setPosition(beyondarObject.getScreenPositionTopRight());

            return recycledView;
        }

    }
    //==============================================================================================


    //==============================================================================================
    // Create bottom view
    //==============================================================================================
    private BottomSheetBehavior placeListBottomSheetBehavior;
    private int peekHeight = 240;
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
                Toast.makeText(ARExploreActivity.this, mPlaceList.get(position).getName(), Toast.LENGTH_LONG).show();
                performPlaceDetailSearch(mPlaceList.get(position).getPlace_id());
            }
        });
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        placeListBottomSheetBehavior.setHideable(true); // so that the list can be hideded
        //placeListBottomSheetBehavior.setPeekHeight(peekHeight);
        extendButton.setVisibility(View.VISIBLE); //show extendbutton
        placeListBottomSheetBehavior.setState(placeListBottomSheetBehavior.STATE_EXPANDED);
        if(currentDisplayMode==DISPLAY_PLACE_DETAIL)
        {
            bottomsheetbehaviorgoogle.setHideable(true);
            bottomsheetbehaviorgoogle.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
            previousDisplayMode = currentDisplayMode;
        }
        currentDisplayMode = DISPLAY_PLACE_LIST;
    }

    /**
     * This is to perform search result based on user query
     * @param userQuery
     */
    private void performPlaceSearch(String userQuery)
    {
        StringBuilder googlePlacesUrl = urlGenerator.getGoogleMapPlacesQueryURL(userQuery,GenerateGoogleMapApiUrl.TEXT_SEARCH, mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
        Log.d("Google Query",googlePlacesUrl.toString());
        final List<Place> newPlacesList = new ArrayList<Place>();
        // Creating volley request obj
        JsonObjectRequest movieReq = new JsonObjectRequest(Request.Method.GET,googlePlacesUrl.toString(),null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject result) {
                        Log.d("JSON Error", result.toString());
                        //hidePDialog();
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
                                if(!obj.isNull("place_id"))
                                {
                                    newPlace.setPlace_id(obj.getString("place_id"));
                                }
                                if(!obj.isNull("photos"))
                                    newPlace.setPhoto_reference(obj.getJSONArray("photos").getJSONObject(0).getString("photo_reference"));

                                if(!obj.isNull("price_level"))
                                    newPlace.setPrice_level(obj.getString("price_level"));
                                Location placeLocation = new Location("");
                                placeLocation.setLatitude(Double.parseDouble(newPlace.getLat()));
                                placeLocation.setLongitude(Double.parseDouble(newPlace.getLng()));
                                newPlace.setDistance(mCurrentLocation.distanceTo(placeLocation));
                                newPlacesList.add(newPlace);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        //dismiss keyboard
                        hideSoftKeyboard(ARExploreActivity.this);
                        //dismiss autocomplete list
                        edtSeach.dismissDropDown();
                        plotGoogleMap(newPlacesList);
                        mPlaceListMain = newPlacesList;
                        //List<Place> filteredList = filterPlaceResult(mPlaceListMain);

                        generateWorld(mWorld,mPlaceListMain,viewSetting,true);
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

                        // set radar renderer to furthestPlace
                        radarPlugin.setMaxDistance(furthestPlace+200.0f);
                        distanceSeekBar.setProgress((int)(furthestPlace+200));

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                //hidePDialog();

            }
        });
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(movieReq);
    }

    /**
     * This is to perform search result based on user query
     * @param userQuery
     */
    public void performPlaceTypeSearch(String userQuery)
    {
        StringBuilder googlePlacesUrl = urlGenerator.getGoogleMapPlacesQueryURL(userQuery,GenerateGoogleMapApiUrl.SEARCH_BY_TYPE, mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
        Log.d("Google Query",googlePlacesUrl.toString());
        final List<Place> newPlacesList = new ArrayList<Place>();
        // Creating volley request obj
        JsonObjectRequest movieReq = new JsonObjectRequest(Request.Method.GET,googlePlacesUrl.toString(),null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject result) {
                        Log.d("JSON Error", result.toString());
                        //hidePDialog();
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
                                if(!obj.isNull("place_id"))
                                {
                                    newPlace.setPlace_id(obj.getString("place_id"));
                                }
                                if(!obj.isNull("photos"))
                                    newPlace.setPhoto_reference(obj.getJSONArray("photos").getJSONObject(0).getString("photo_reference"));

                                if(!obj.isNull("price_level"))
                                    newPlace.setPrice_level(obj.getString("price_level"));
                                Location placeLocation = new Location("");
                                placeLocation.setLatitude(Double.parseDouble(newPlace.getLat()));
                                placeLocation.setLongitude(Double.parseDouble(newPlace.getLng()));
                                newPlace.setDistance(mCurrentLocation.distanceTo(placeLocation));
                                newPlacesList.add(newPlace);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        //dismiss keyboard
                        hideSoftKeyboard(ARExploreActivity.this);
                        //dismiss autocomplete list
                        edtSeach.dismissDropDown();
                        plotGoogleMap(newPlacesList);
                        mPlaceListMain = newPlacesList;
                        //List<Place> filteredList = filterPlaceResult(mPlaceListMain);
                        generateWorld(mWorld,mPlaceListMain,viewSetting,true);
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

                        // set radar renderer to furthestPlace
                        radarPlugin.setMaxDistance(furthestPlace+200.0f);
                        distanceSeekBar.setProgress((int)(furthestPlace+200));

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                //hidePDialog();

            }
        });
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(movieReq);
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
     * perform detailed search for a place.
     * @param placeId
     */
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
                        //hidePDialog();
                        // Parsing json
                        //for (int i = 0; i < response.length(); i++) {
                        try {

                            JSONObject obj = result.getJSONObject("result");
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


                            Location placeLocation = new Location("");
                            placeLocation.setLatitude(Double.parseDouble(newPlace.getLat()));
                            placeLocation.setLongitude(Double.parseDouble(newPlace.getLng()));
                            newPlace.setDistance(mCurrentLocation.distanceTo(placeLocation));



                            newPlacesList.add(newPlace);

                            //currentPlaceDetail = newPlace; // set currentPlaceDetail


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //dismiss keyboard
                        hideSoftKeyboard(ARExploreActivity.this);
                        //dismiss autocomplete list
                        edtSeach.dismissDropDown();
                        plotGoogleMap(newPlacesList);
                        mPlaceDetail = newPlacesList;
                        generateWorld(mWorld,mPlaceDetail,viewSetting,true);//rerender
                        zoomToLocationSearchResult(newPlacesList);

                        if(currentDisplayMode==DISPLAY_PLACE_LIST)
                        {
                            placeListBottomSheetBehavior.setHideable(true);
                            placeListBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            previousDisplayMode = currentDisplayMode;
                        }
                        currentDisplayMode = DISPLAY_PLACE_DETAIL;


                        bottomsheetbehaviorgoogle.setHideable(true);
                        bottomsheetbehaviorgoogle.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                        extendButton.setVisibility(View.VISIBLE);
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
                        }
                        else
                        {
                            priceTextView.setText("");
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
                //hidePDialog();

            }
        });
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(placeDetailReq);
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
                if(currentDisplayMode==NO_RESULT_MODE)
                {
                    generateWorld(mWorld,mPlaceListMain,viewSetting,true);

                    plotGoogleMap(mPlaceListMain);
                    zoomToLocationSearchResult(mPlaceListMain);

                    //performNearbySearch();//we go back to no result mode, so we search nearby object again
                }
                if(previousDisplayMode==DISPLAY_PLACE_LIST)
                {
                    previousDisplayMode = NO_RESULT_MODE;
                    extendButton.setVisibility(View.VISIBLE);
                    //placeListBottomSheetBehavior.setHideable(false);
                    //placeListBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    // need to display the icon back
                    generateWorld(mWorld,mPlaceListMain,viewSetting,true);

                    plotGoogleMap(mPlaceListMain);
                    zoomToLocationSearchResult(mPlaceListMain);

                    return true;

                }
                extendButton.setVisibility(View.GONE);// no more thing
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
                    //bottomsheetbehaviorgoogle.setHideable(false);
                    //bottomsheetbehaviorgoogle.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                    extendButton.setVisibility(View.VISIBLE);// no more thing

                    // need to display the icon back

                    plotGoogleMap(mPlaceDetail);
                    generateWorld(mWorld,mPlaceDetail,viewSetting,true);
                    zoomToLocationSearchResult(mPlaceDetail);
                    return true;

                }
                extendButton.setVisibility(View.GONE);// no more thing
                googleMap.clear();
                return true;
            }
            else
            {
                mSensorManager.unregisterListener(this);
                return super.onKeyDown(keyCode, event);
            }
        }
        mSensorManager.unregisterListener(this);
        return super.onKeyDown(keyCode, event);
    }



    // filter result
    public List<Place> filterPlaceResult(List<Place> placeList)
    {
        List<Place> newPlaceList = new ArrayList<>();
        int rating = this.rating;
        for(Place place:placeList)
        {
            if(place.getRating()>=rating)
            {
                newPlaceList.add(new Place(place));
            }

        }
        return newPlaceList;
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
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getIconHelper.decodeSampledBitmapFromResource(getResources(),getIconHelper.getIconIDFromType(type),32,32)));
            //markerOptions.icon(BitmapDescriptorFactory.fromResource(getIconHelper.getIconIDFromType(type)));
            googleMap.addMarker(markerOptions);
        }
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

            // note need to set device screen size if not the application will crash before google map is layed out
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,deviceWidth,deviceHeight,0);
            googleMap.animateCamera(cameraUpdate);
        }
        else if (places.size()==1)
        {
            LatLng latLng = new LatLng(Double.parseDouble(places.get(0).getLat()),Double.parseDouble(places.get(0).getLng()));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
            googleMap.animateCamera(cameraUpdate);
        }
    }

    //==============================================================================================
    // Orientation Sensor
    //==============================================================================================
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    public void initListeners()
    {
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    float[] mGravity;

    @Override
    public void onSensorChanged(SensorEvent event) {
        //If type is accelerometer only assign values to global property mGravity
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            mGravity = event.values;

            float[] g = new float[3];
            g = event.values.clone();

            float norm_Of_g = (float) Math.sqrt(g[0] * g[0] + g[1] * g[1] + g[2] * g[2]);

            // Normalize the accelerometer vector
            g[0] = g[0] / norm_Of_g;
            g[1] = g[1] / norm_Of_g;
            g[2] = g[2] / norm_Of_g;

            int inclination = (int) Math.round(Math.toDegrees(Math.acos(g[2])));
            //Log.d("test", Integer.toString(inclination));

            // lower than 70 degree display google map view
            if(inclination<65)
            {
                if(showGoogleMap==false)
                {

                    showGoogleMap=true;
                    ft = getSupportFragmentManager().beginTransaction();
                    ft.hide(mBeyondarFragment);
                    ft.show(mapFragment);
                    ft.commit();
                    myLocationButton.setVisibility(View.VISIBLE);
                }

            }
            else
            {

                if(showGoogleMap==true)
                {
                    showGoogleMap=false;
                    ft = getSupportFragmentManager().beginTransaction();
                    ft.hide(mapFragment);
                    ft.show(mBeyondarFragment);
                    ft.commit();
                    myLocationButton.setVisibility(View.GONE);
                }
                // display AR view
            }
            /*if (inclination<25||inclination>155)
            {
                Log.d("test", "downwards");
            }
            else
            {
                Log.d("test", "upwards");
            }*/
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //==============================================================================================

}

