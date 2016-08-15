/**
 * Copyright 2014 Magnus Woxblom
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ar.oxford.oxfordtourar.Util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ar.oxford.oxfordtourar.BottomViewLayout.LayoutWrapContentUpdater;
import com.ar.oxford.oxfordtourar.PlacesInTrip;
import com.ar.oxford.oxfordtourar.R;
import com.ar.oxford.oxfordtourar.model.PlaceTrip;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListFragmentPlacesInTrip extends Fragment {

    private ArrayList<Pair<Long, PlaceTrip>> mItemArray;
    private DragListView mDragListView;
    private MySwipeRefreshLayout mRefreshLayout;
    private List<PlaceTrip> placeList;
    private DatabaseHelper db;
    private int tripId;
    private Context context;
    private LayoutWrapContentUpdater layoutUpdater;
    private LinearLayout layout;
    private ItemAdapter listAdapter;
    private Location location;

    public static ListFragmentPlacesInTrip newInstance() {

        return new ListFragmentPlacesInTrip();
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setLocation(Location location) {
        this.location = location;
        if (mItemArray != null&&location!=null) {
            for (int i = 0; i < mItemArray.size(); i++) {
                Location placeLocation = new Location("");
                placeLocation.setLatitude(Double.parseDouble(mItemArray.get(i).second.getLat()));
                placeLocation.setLongitude(Double.parseDouble(mItemArray.get(i).second.getLng()));
                mItemArray.get(i).second.setDistance(location.distanceTo(placeLocation));
            }
        }
        updateUI();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItemArray = new ArrayList<>();
        db = new DatabaseHelper(context);
        layoutUpdater = new LayoutWrapContentUpdater();
        placeList = db.getAllPlacesByTrip(tripId); // get all places list
        // auto sort the object list based on position
        Collections.sort(placeList, new PositionComparator());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.places_in_trip_list_layout, container, false);
        View view2 = inflater.inflate(R.layout.place_trip_row, container, false);

        mRefreshLayout = (MySwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        layout = (LinearLayout) view2.findViewById(R.id.row);

        mDragListView = (DragListView) view.findViewById(R.id.drag_list_view);
        mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        mDragListView.setDragListListener(new DragListView.DragListListenerAdapter() {
            @Override
            public void onItemDragStarted(int position) {
                mRefreshLayout.setEnabled(false);
                //Toast.makeText(mDragListView.getContext(), "Start - position: " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                mRefreshLayout.setEnabled(true);
                if (fromPosition != toPosition) {
                    String test = "";
                    for (int i = 0; i < mItemArray.size(); i++) {
                        test = test + mItemArray.get(i).second.getName() + ", ";
                    }
                    Toast.makeText(mDragListView.getContext(), test, Toast.LENGTH_LONG).show();
                    updateUI();


                }
            }
        });


        for (int i = 0; i < placeList.size(); i++) {

            //Place place = new Place();
            mItemArray.add(new Pair<>(Long.valueOf(i), placeList.get(i)));
        }

        mRefreshLayout.setScrollingView(mDragListView.getRecyclerView());
        mRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.list_row_end_color));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });

        setupListRecyclerView();
        setLocation(location);
        return view;
    }

    /*@Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("List and Grid");
    }*/

    private void setupListRecyclerView() {
        mDragListView.setLayoutManager(new LinearLayoutManager(getContext()));

        listAdapter = new ItemAdapter(mItemArray, R.layout.place_trip_row, R.id.thumbnail, false);


        listAdapter.setOnItemLongClickListener(new ItemAdapter.OnItemLongClickListener() {

            @Override
            public void onItemLongClick(ItemAdapter.ItemHolder item, final int position) {
                Toast.makeText(context, mItemArray.get(position).second.getName() + "Long Click", Toast.LENGTH_LONG).show();

                final EditText taskEditText = new EditText(context);
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Set Tour Duration")
                        .setView(taskEditText)
                        .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String duration = String.valueOf(taskEditText.getText());
                                Boolean wantToCloseDialog = (duration.toString().trim().isEmpty());
                                if (!wantToCloseDialog) {
                                    mItemArray.get(position).second.setDuration(Integer.parseInt(duration));
                                    db.updateTripPlaceDuration(Integer.parseInt(duration), mItemArray.get(position).second.getPlaceTripid());
                                    updateUI();
                                } else {

                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();


            }

        });

        listAdapter.setOnItemClickListener(new ItemAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(ItemAdapter.ItemHolder item, int position) {
                Toast.makeText(context, mItemArray.get(position).second.getName() + "Click", Toast.LENGTH_LONG).show();
            }

        });

        mDragListView.setAdapter(listAdapter, true);
        mDragListView.setCanDragHorizontally(false);
        mDragListView.setCustomDragItem(new MyDragItem(getContext(), R.layout.place_trip_row));
    }

    private static class MyDragItem extends DragItem {

        public MyDragItem(Context context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {

            CharSequence text = ((TextView) clickedView.findViewById(R.id.title)).getText();
            ((TextView) dragView.findViewById(R.id.title)).setText(text);
            CharSequence rating = ((TextView) clickedView.findViewById(R.id.rating)).getText();
            ((TextView) dragView.findViewById(R.id.rating)).setText(rating);
            CharSequence address = ((TextView) clickedView.findViewById(R.id.address)).getText();
            ((TextView) dragView.findViewById(R.id.address)).setText(address);
            CharSequence distance = ((TextView) clickedView.findViewById(R.id.distance)).getText();
            ((TextView) dragView.findViewById(R.id.distance)).setText(distance);
            CharSequence duration = ((TextView) clickedView.findViewById(R.id.duration)).getText();
            ((TextView) dragView.findViewById(R.id.duration)).setText(duration);
            boolean checked = ((CheckBox) clickedView.findViewById(R.id.checkBox)).isChecked();
            ((CheckBox) dragView.findViewById(R.id.checkBox)).setChecked(checked);
            Float rating_bar = ((RatingBar) clickedView.findViewById(R.id.ratingBar)).getRating();
            ((RatingBar) dragView.findViewById(R.id.ratingBar)).setRating(rating_bar);
            dragView.setBackgroundColor(dragView.getResources().getColor(R.color.list_row_end_color));
        }
    }

    public void updateUI() {
        if (listAdapter != null && mDragListView != null) {
            listAdapter.updateResults(mItemArray);
            mDragListView.setAdapter(listAdapter, true);
        }

        //LayoutWrapContentUpdater updater = new LayoutWrapContentUpdater();
        //updater.wrapContentAgain(grid,true);
    }

    @Override
    public void onDestroy() {
        for (int i = 0; i < mItemArray.size(); i++) {
            db.updateTripPlaceWithPosition(i, mItemArray.get(i).second.getChecked(), mItemArray.get(i).second.getPlaceTripid());
        }
        super.onDestroy();

    }

    @Override
    public void onPause() {
        for (int i = 0; i < mItemArray.size(); i++) {
            db.updateTripPlaceWithPosition(i, mItemArray.get(i).second.getChecked(), mItemArray.get(i).second.getPlaceTripid());
        }
        super.onPause();
    }

    public class PositionComparator implements Comparator<PlaceTrip> {

        @Override
        public int compare(PlaceTrip lhs, PlaceTrip rhs) {
            return Integer.compare(lhs.getPosition(), rhs.getPosition());
        }
    }
}




