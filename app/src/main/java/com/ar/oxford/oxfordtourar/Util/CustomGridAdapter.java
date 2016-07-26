package com.ar.oxford.oxfordtourar.Util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ar.oxford.oxfordtourar.R;
import com.ar.oxford.oxfordtourar.model.Trip;

import java.util.List;

/**
 * Created by Kelvin Khoo on 26/07/2016.
 */
public class CustomGridAdapter extends BaseAdapter {
    private Context mContext;

    private List<Trip> tripList;

    public CustomGridAdapter(Context c,List<Trip> tripList) {
        mContext = c;
        this.tripList = tripList;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return tripList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return tripList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            grid = new View(mContext);
            grid = inflater.inflate(R.layout.grid_item, null);
            TextView textView = (TextView) grid.findViewById(R.id.textView);
            ImageView imageView = (ImageView)grid.findViewById(R.id.imageView);
            textView.setText(tripList.get(position).getTripName());

        } else {
            grid = (View) convertView;
        }

        return grid;
    }

    public void updateResults(List<Trip> results) {
        tripList.clear();
        tripList.addAll(results);
        //Triggers the list update
        notifyDataSetChanged();
        //notifyDataSetInvalidated();
    }





}
