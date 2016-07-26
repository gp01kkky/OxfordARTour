package com.ar.oxford.oxfordtourar.Util;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ar.oxford.oxfordtourar.R;
import com.ar.oxford.oxfordtourar.model.Trip;

import java.util.List;

/**
 * Created by Kelvin Khoo on 26/07/2016.
 */
public class CustomTripDialogAdapter extends RecyclerView.Adapter<CustomTripDialogAdapter.ItemHolder>{

    private List<Trip> tripList;
    private OnItemClickListener onItemClickListener;


    public CustomTripDialogAdapter(List<Trip> tripList) {
        this.tripList = tripList;
    }

    @Override
    public CustomTripDialogAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_show_trip_list_in_dialog, parent, false);
        return new ItemHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(CustomTripDialogAdapter.ItemHolder holder, int position) {
        holder.bind(tripList.get(position));
    }

    @Override
    public int getItemCount() {
        return tripList.size();
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

    public static class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CustomTripDialogAdapter adapter;
        TextView tripName;
        TextView numOfPlaces;
        TextView dateCreated;
        NetworkImageView thumbNail;
        ImageLoader imageLoader;

        public ItemHolder(View itemView, CustomTripDialogAdapter parent) {
            super(itemView);
            //imageLoader = AppController.getInstance().getImageLoader();
            itemView.setOnClickListener(this);
            this.adapter = parent;
            //thumbNail = (NetworkImageView) itemView.findViewById(R.id.thumbnail);
            tripName = (TextView) itemView.findViewById(R.id.trip_name);
            numOfPlaces = (TextView) itemView.findViewById(R.id.num_of_item);
            dateCreated = (TextView) itemView.findViewById(R.id.date_created);
        }

        public void bind(Trip item) {
            tripName.setText(item.getTripName());
            //numOfPlaces.setText(Double.toString(item.getRating()));
            dateCreated.setText(item.getCreated_at());
            //String imageUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=200&photoreference=" + item.getPhoto_reference()+"&key=AIzaSyDqJGehbvGCLpEUxbchILmGK_-3eWyBxgc";
            //thumbNail.setImageUrl(imageUrl,imageLoader);
        }

        @Override
        public void onClick(View v) {
            final OnItemClickListener listener = adapter.getOnItemClickListener();
            if (listener != null) {
                listener.onItemClick(this, getAdapterPosition());
            }
        }
    }

    public void updateResults(List<Trip> results) {
        tripList.clear();
        tripList.addAll(results);
        //Triggers the list update
        notifyDataSetChanged();
        //notifyDataSetInvalidated();
    }


}
