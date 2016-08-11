package com.ar.oxford.oxfordtourar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ar.oxford.oxfordtourar.MapHelper.GooglePlacesDisplayAdapterCustom;
import com.ar.oxford.oxfordtourar.Util.DatabaseHelper;
import com.ar.oxford.oxfordtourar.model.Place;

import java.util.List;

public class PlacesInTrip extends AppCompatActivity {

    private List<Place> placeList;
    private DatabaseHelper db;
    private int tripId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(getApplicationContext());

        tripId = getIntent().getExtras().getInt("trip_id"); // get id from previous activity
        placeList = db.getAllPlacesByTrip(tripId); // get all places list

       setContentView(R.layout.activity_places_in_trip);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        createPlacesList(placeList);
    }

    /**
     * Create list of places
     * @param mPlaceList
     */
    private void createPlacesList(final List<Place> mPlaceList)
    {
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
                Intent intent = new Intent(getApplicationContext(),DisplayTripPlacesActivity.class);
                /*Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("places", (ArrayList<? extends Parcelable>) placeList);
                intent.putExtras(bundle);*/
                intent.putExtra("trip_id",tripId);
                //startActivity(intent);
            default:
                break;
        }

        return true;
    }
}
