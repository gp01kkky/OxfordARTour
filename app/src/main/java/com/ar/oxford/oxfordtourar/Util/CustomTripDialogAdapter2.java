package com.ar.oxford.oxfordtourar.Util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ar.oxford.oxfordtourar.R;
import com.ar.oxford.oxfordtourar.model.Trip;

import java.util.List;

/**
 * Created by Kelvin Khoo on 26/07/2016.
 */
public class CustomTripDialogAdapter2 extends BaseAdapter {
    private Context mContext;

    private List<Trip> tripList;
    private int counter;

    public CustomTripDialogAdapter2(Context c, List<Trip> tripList)
    {
        mContext = c;
        counter=0;
        this.tripList = tripList;
    }
    @Override
    public int getCount() {
        return tripList.size();
    }

    @Override
    public Object getItem(int position) {
        return tripList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    View listView;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        if (convertView == null) {

            System.out.println("getview:"+position+" "+convertView);
            listView = new View(mContext);
            listView = inflater.inflate(R.layout.row_show_trip_list_in_dialog, null);
            TextView tripName = (TextView) listView.findViewById(R.id.trip_name);
            TextView numOfPlaces = (TextView) listView.findViewById(R.id.num_of_item);
            TextView dateCreated = (TextView) listView.findViewById(R.id.date_created);
            tripName.setText(tripList.get(position).getTripName());
            numOfPlaces.setText("0");
            dateCreated.setText(tripList.get(position).getCreated_at());

        } else {
            listView = (View) convertView;
        }

        return listView;
    }
}
