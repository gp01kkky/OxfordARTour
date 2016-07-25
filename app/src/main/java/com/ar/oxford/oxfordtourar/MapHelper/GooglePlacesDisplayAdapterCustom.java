package com.ar.oxford.oxfordtourar.MapHelper;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ar.oxford.oxfordtourar.R;
import com.ar.oxford.oxfordtourar.Util.AppController;
import com.ar.oxford.oxfordtourar.model.Place;

import java.util.List;

/**
 * Created by Kelvin Khoo on 29/06/2016.
 */
public class GooglePlacesDisplayAdapterCustom extends RecyclerView.Adapter<GooglePlacesDisplayAdapterCustom.ItemHolder>{

    private List<Place> list;
    private OnItemClickListener onItemClickListener;


    public GooglePlacesDisplayAdapterCustom(List<Place> list) {
        this.list = list;
    }

    @Override
    public GooglePlacesDisplayAdapterCustom.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.places_list_row, parent, false);
        return new ItemHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(GooglePlacesDisplayAdapterCustom.ItemHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
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

        private GooglePlacesDisplayAdapterCustom adapter;
        TextView title;
        TextView rating;
        TextView address;
        TextView distance;
        ImageLoader imageLoader;
        NetworkImageView thumbNail;

        public ItemHolder(View itemView, GooglePlacesDisplayAdapterCustom parent) {
            super(itemView);
            imageLoader = AppController.getInstance().getImageLoader();
            itemView.setOnClickListener(this);
            this.adapter = parent;

            thumbNail = (NetworkImageView) itemView.findViewById(R.id.thumbnail);
            title = (TextView) itemView.findViewById(R.id.title);
            rating = (TextView) itemView.findViewById(R.id.rating);
            address = (TextView) itemView.findViewById(R.id.address);
            distance = (TextView) itemView.findViewById(R.id.distance);
        }

        public void bind(Place item) {
            title.setText(item.getName());
            rating.setText(Double.toString(item.getRating()));
            address.setText(item.getName());
            String imageUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=200&photoreference=" + item.getPhoto_reference()+"&key=AIzaSyDqJGehbvGCLpEUxbchILmGK_-3eWyBxgc";
            thumbNail.setImageUrl(imageUrl,imageLoader);
        }

        @Override
        public void onClick(View v) {
            final OnItemClickListener listener = adapter.getOnItemClickListener();
            if (listener != null) {
                listener.onItemClick(this, getAdapterPosition());
            }
        }
    }

}

