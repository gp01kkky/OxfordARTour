package com.ar.oxford.oxfordartour;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ar.oxford.oxfordartour.MapHelper.GooglePlacesReadTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements LocationListener {

    private GoogleMap googleMap;
    private static final String GOOGLE_API_KEY = "AIzaSyDqJGehbvGCLpEUxbchILmGK_-3eWyBxgc";
    EditText placeText;
    double latitude = 0;
    double longitude = 0;
    private int PROXIMITY_RADIUS = 50; // in metres

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Request permissions to support Android Marshmallow and above devices
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions();
        }

        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        setContentView(R.layout.activity_maps);

        // edittext box to get the text and button
        placeText = (EditText) findViewById(R.id.placeText);
        Button btnFind = (Button) findViewById(R.id.btnFind);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.googleMap);

        googleMap = fragment.getMap();

        // to check for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }

        // allows the ui of mylocation
        googleMap.setMyLocationEnabled(true);


        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider); // get location
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);


        // on button click we request the nearby places
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // build the query url
                String type = placeText.getText().toString();
                /*StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
                googlePlacesUrl.append("location=" + latitude + "," + longitude);
                googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
                googlePlacesUrl.append("&type=" + type);
                googlePlacesUrl.append("&sensor=true");
                googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);*/

                StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/textsearch/json?");
                googlePlacesUrl.append("query=" + type);
                googlePlacesUrl.append("&location=" + latitude + "," + longitude);
                googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
                googlePlacesUrl.append("&sensor=true");
                googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);

                /*StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
                googlePlacesUrl.append("location=" + latitude + "," + longitude);
                googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
                googlePlacesUrl.append("&sensor=true");
                googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);*/

                Log.d("Google Query",googlePlacesUrl.toString());

                // create the asynctask
                GooglePlacesReadTask googlePlacesReadTask = new GooglePlacesReadTask();
                Object[] toPass = new Object[2];
                toPass[0] = googleMap;
                toPass[1] = googlePlacesUrl.toString();
                googlePlacesReadTask.execute(toPass);
            }
        });    }

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
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
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
}
