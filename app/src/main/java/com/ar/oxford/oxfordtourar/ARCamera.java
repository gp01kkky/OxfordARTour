package com.ar.oxford.oxfordtourar;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.ar.oxford.oxfordtourar.ARHelper.ARViewReadTask;
import com.ar.oxford.oxfordtourar.MapHelper.GenerateGoogleMapApiUrl;
import com.beyondar.android.fragment.BeyondarFragmentSupport;
import com.beyondar.android.opengl.util.LowPassFilter;
import com.beyondar.android.plugin.googlemap.GoogleMapWorldPlugin;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Kelvin Khoo on 29/06/2016.
 */
public class ARCamera extends FragmentActivity implements LocationListener{
    private View mLayout;
    double latitude = 0;
    double longitude = 0;
    private static final int REQUEST_CAMERA = 0;
    private GoogleMapWorldPlugin mGoogleMapPlugin;
    private World mWorld;
    private GoogleMap mMap;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private BeyondarFragmentSupport mBeyondarFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.beyond_ar_camera);

        mBeyondarFragment = (BeyondarFragmentSupport) getSupportFragmentManager().findFragmentById(R.id.beyondarFragment);

        mWorld = new World(this);
        mWorld.setDefaultImage(R.drawable.ar_icon);

        //mWorld.setGeoPosition(mWorld.getLatitude(), mWorld.getLongitude());

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider); // get location
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);





        /*GeoObject go1 = new GeoObject(1l);
        go1.setGeoPosition(51.522941, -0.402354);
        go1.setImageResource(R.drawable.ar_icon);
        go1.setName("Creature 1");

        GeoObject go2 = new GeoObject(2l);
        go2.setGeoPosition(51.520378, -0.402037);
        go2.setImageResource(R.drawable.ar_icon);
        go2.setName("Creature 1");

        mBeyondarFragment.setDistanceFactor(2);

        mWorld.addBeyondarObject(go1);
        mWorld.addBeyondarObject(go2);


        LowPassFilter.ALPHA = (float) 0.015;
        mBeyondarFragment.setMaxDistanceToRender(20000);
        mBeyondarFragment.setPullCloserDistance(20);
        mBeyondarFragment.setWorld(mWorld);*/


    }


    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        //location.getLatitude may produce null value
        mWorld.setGeoPosition(location.getLatitude(), location.getLongitude());
        Log.e("Lat", Double.toString(location.getLatitude()));
        Log.e("Lnd", Double.toString(location.getLongitude()));

        mBeyondarFragment.setDistanceFactor(2);
        LowPassFilter.ALPHA = (float) 0.015;
        mBeyondarFragment.setMaxDistanceToRender(20000);
        mBeyondarFragment.setPullCloserDistance(20);

        performNearbySearch();
        //performNearbySearch();
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

    public void performNearbySearch()
    {
        GenerateGoogleMapApiUrl urlGenerator = new GenerateGoogleMapApiUrl();
        StringBuilder googlePlacesUrl = urlGenerator.getGoogleMapPlacesQueryURL("",GenerateGoogleMapApiUrl.NEARBY_PLACES,latitude,longitude);
        ARViewReadTask arViewReadTask = new ARViewReadTask();
        Object[] toPass = new Object[3];
        toPass[0] = mWorld;
        toPass[1] = mBeyondarFragment;
        toPass[2] = googlePlacesUrl.toString();

        arViewReadTask.execute(toPass);
    }

}
