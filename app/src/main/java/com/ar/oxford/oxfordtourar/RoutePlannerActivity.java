package com.ar.oxford.oxfordtourar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.ar.oxford.oxfordtourar.Util.CustomGridAdapter2;
import com.ar.oxford.oxfordtourar.Util.DatabaseHelper;
import com.ar.oxford.oxfordtourar.Util.ItemOffsetDecoration;
import com.ar.oxford.oxfordtourar.model.Trip;

import java.util.List;

public class RoutePlannerActivity extends AppCompatActivity {

    final static String TAG = "RoutePlannerActivity";
    GridView grid;
    DatabaseHelper db;
    List<Trip> tripList;
    CustomGridAdapter2 adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_planner);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db = new DatabaseHelper(getApplicationContext());
        tripList = db.getAllTrip();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText taskEditText = new EditText(RoutePlannerActivity.this);
                AlertDialog dialog = new AlertDialog.Builder(RoutePlannerActivity.this)
                        .setTitle("Add a new Trip")
                        .setView(taskEditText)
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
                dialog.show();
            }
        });

        /*adapter = new CustomGridAdapter(RoutePlannerActivity.this,tripList);
        grid = (GridView) findViewById(R.id.grid);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(RoutePlannerActivity.this, "You Clicked at " +tripList.get(position).getTripName(), Toast.LENGTH_SHORT).show();

            }
        });*/

        adapter = new CustomGridAdapter2(tripList);
        adapter.setOnItemClickListener(new CustomGridAdapter2.OnItemClickListener() {
            @Override
            // when clicked on a trip we go and see how many places in the trip
            public void onItemClick(CustomGridAdapter2.ItemHolder item, int position) {
                Toast.makeText(RoutePlannerActivity.this, tripList.get(position).getTripName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(),PlacesInTrip.class);
                intent.putExtra("trip_id",tripList.get(position).getId());
                startActivity(intent);
            }
        });
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_grid);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen.item_offset);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setAdapter(adapter);
    }


    private void updateUI() {
        tripList = db.getAllTrip();
        adapter.updateResults(tripList);
        //LayoutWrapContentUpdater updater = new LayoutWrapContentUpdater();
        //updater.wrapContentAgain(grid,true);
    }

}
