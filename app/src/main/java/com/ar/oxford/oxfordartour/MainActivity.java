package com.ar.oxford.oxfordartour;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    /**
     * Called whenever any button is clicked
     *
     * @param v
     */
    public void loadMode(View v)
    {
        int id = v.getId();
        Intent intent;
        switch (id) {
            case R.id.attraction:
                /*intent = new Intent(getApplicationContext(), ExploreMode.class);
                startActivity(intent);*/
                break;
            case R.id.map:
                intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
                break;
            case R.id.suggested_tour:
                /*intent = new Intent(getApplicationContext(), CategoryActivity.class);
                startActivity(intent);*/
                break;
            case R.id.ar_explore_mode:
                /*intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);*/
                break;
            case R.id.route_planner:
                /*intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);*/
                break;
            case R.id.restaurant:
                /*intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);*/
                break;
            case R.id.nightlife:
                /*intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);*/
                break;
            case R.id.shopping:
                /*intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);*/
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
