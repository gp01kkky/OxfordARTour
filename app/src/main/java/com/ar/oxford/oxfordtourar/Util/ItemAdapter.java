/**
 * Copyright 2014 Magnus Woxblom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ar.oxford.oxfordtourar.Util;

import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ar.oxford.oxfordtourar.R;
import com.ar.oxford.oxfordtourar.model.PlaceTrip;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

public class ItemAdapter extends DragItemAdapter<Pair<Long, PlaceTrip>, ItemAdapter.ItemHolder> {

    private int mLayoutId;
    private int mGrabHandleId;
    private OnItemLongClickListener onItemLongClickListener;
    private OnItemClickListener onItemClickListener;


    public ItemAdapter(ArrayList<Pair<Long, PlaceTrip>> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        super(dragOnLongPress);
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        setHasStableIds(true);
        setItemList(list);
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ItemHolder(view,this);
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        PlaceTrip place = mItemList.get(position).second;
        holder.itemView.setTag(place.getName());
        title.setText(place.getName());
        rating.setText(Float.toString(place.getRating()));
        ratingBar.setRating(place.getRating());
        address.setText(place.getAddress());
        int distanceFromUser = (int)place.getDistance();


        distance.setText(Integer.toString(distanceFromUser));
        String text = Integer.toString(place.getDuration())+"mins";
        duration.setText(text);
        // we must set tag because android use recycle view, getting the recycleview position and update is not accurate
        checkBox.setTag(place);
        String imageUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=200&photoreference="+place.getPhoto_reference()+"&key=AIzaSyDqJGehbvGCLpEUxbchILmGK_-3eWyBxgc";
        if(place.getChecked()==1)
        {
            checkBox.setChecked(true);
        }
        else
        {
            checkBox.setChecked(false);
        }

        thumbNail.setDefaultImageResId(R.drawable.no_image_icon);
        thumbNail.setErrorImageResId(R.drawable.no_image_icon);
        thumbNail.setImageUrl(imageUrl,imageLoader);
    }

    TextView title;
    TextView rating;
    TextView address;
    TextView distance;
    TextView duration;
    CheckBox checkBox;
    RatingBar ratingBar;
    ImageLoader imageLoader;
    NetworkImageView thumbNail;

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).first;
    }

    public class ItemHolder extends DragItemAdapter<Pair<Long, PlaceTrip>, ItemAdapter.ItemHolder>.ViewHolder  {

        private ItemAdapter adapter;

        public ItemHolder(View itemView, ItemAdapter parent) {
            super(itemView, mGrabHandleId);
            this.adapter = parent;
            thumbNail = (NetworkImageView) itemView.findViewById(R.id.thumbnail);
            title = (TextView) itemView.findViewById(R.id.title);
            ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBar);
            rating = (TextView) itemView.findViewById(R.id.rating);
            address = (TextView) itemView.findViewById(R.id.address);
            duration = (TextView) itemView.findViewById(R.id.duration);
            distance = (TextView) itemView.findViewById(R.id.distance);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
            imageLoader = AppController.getInstance().getImageLoader();

            checkBox.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    // we must set tag because android use recycle view, getting the recycleview position and update is not accurate
                    PlaceTrip place = (PlaceTrip) v.getTag();
                    if(place.getChecked()==1)
                    {
                        place.setChecked(0);
                    }
                    else
                    {
                        place.setChecked(1);
                    }
                }
            });

        }


        @Override
        public void onItemClicked(View view) {
            final OnItemClickListener listener = adapter.getOnItemClickListener();
            if (listener != null) {
                listener.onItemClick(this, getAdapterPosition());
            }

        }
        @Override
        public boolean onItemLongClicked(View view) {
            final OnItemLongClickListener listener = adapter.getOnItemLongClickListener();
            if (listener != null) {
                listener.onItemLongClick(this, getAdapterPosition());
            }
            return true;
        }
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        onItemLongClickListener = listener;
    }

    public OnItemLongClickListener getOnItemLongClickListener() {
        return onItemLongClickListener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(ItemHolder item, int position);
    }



    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(ItemHolder item, int position);
    }

    public void updateResults(ArrayList<Pair<Long, PlaceTrip>> list)
    {
        setItemList(list);
    }


}
