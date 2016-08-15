package com.ar.oxford.oxfordtourar;

import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.ar.oxford.oxfordtourar.MapHelper.GooglePlacesDisplayAdapterCustom;
import com.ar.oxford.oxfordtourar.Util.DatabaseHelper;
import com.ar.oxford.oxfordtourar.Util.ListFragmentPlacesInTrip;
import com.ar.oxford.oxfordtourar.model.Place;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.List;

public class PlacesInTrip extends AppCompatActivity implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    private List<Place> placeList;
    private DatabaseHelper db;
    private int tripId;
    private ListFragmentPlacesInTrip fragment;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //db = new DatabaseHelper(getApplicationContext());

        tripId = getIntent().getExtras().getInt("trip_id"); // get id from previous activity
        //placeList = db.getAllPlacesByTrip(tripId); // get all places list

        setContentView(R.layout.activity_places_in_trip);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fragment = ListFragmentPlacesInTrip.newInstance();
        fragment.setTripId(tripId);
        fragment.setContext(PlacesInTrip.this);
        showFragment(fragment);

        //==============================================================================================
        // Google Location services
        //==============================================================================================
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
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
        //region obtain google api client for fusedlocationproviderapi
        createLocationRequest();

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        mCurrentLocation = locationManager.getLastKnownLocation(bestProvider); // get location
        if(mCurrentLocation!=null)
        {
            onLocationChanged(mCurrentLocation);
        }
        //==============================================================================================





        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        //createPlacesList(placeList);




        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, "fragment").commit();
    }

    /**
     * Create list of places
     *
     * @param mPlaceList
     */
    private void createPlacesList(final List<Place> mPlaceList) {
        GooglePlacesDisplayAdapterCustom adapter = new GooglePlacesDisplayAdapterCustom(mPlaceList);
        adapter.setOnItemClickListener(new GooglePlacesDisplayAdapterCustom.OnItemClickListener() {
            @Override
            public void onItemClick(GooglePlacesDisplayAdapterCustom.ItemHolder item, int position) {
                //dismissDialog();
                //Toast.makeText(context, mPlaceList.get(position).getName(), Toast.LENGTH_LONG).show();
                //performPlaceDetailSearch(mPlaceList.get(position).getPlace_id());

            }
        });
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_places_in_trip, menu);
        return true;
    }

    // for action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.map_button:
                Intent intent = new Intent(getApplicationContext(), DisplayTripPlacesActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("places", (ArrayList<? extends Parcelable>) placeList);
                intent.putExtras(bundle);
                intent.putExtra("trip_id", tripId);
                startActivity(intent);
            default:
                break;
        }

        return true;
    }


    //==============================================================================================
    // Google Location services
    //==============================================================================================
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private Location mCurrentLocation,prevLocation;
    double latitude = 0;
    double longitude = 0;
    boolean firstRun=true;

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
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
        //mCurrentLocation = location;
        //latitude = mCurrentLocation.getLatitude();
        //longitude = mCurrentLocation.getLongitude();
        prevLocation = mCurrentLocation;
        if(prevLocation==null)
        {
            prevLocation=mCurrentLocation;
        }
        mCurrentLocation = location;
        if ((locationChangedGreaterThan(100, prevLocation, mCurrentLocation) || firstRun)) {
            firstRun=false;
            fragment.setLocation(location);
            Toast.makeText(this,"Location Changed",Toast.LENGTH_SHORT).show();
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    //==============================================================================================
}
