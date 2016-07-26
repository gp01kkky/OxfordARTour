package com.ar.oxford.oxfordtourar.Util;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ar.oxford.oxfordtourar.R;
import com.ar.oxford.oxfordtourar.model.Trip;

import java.util.List;

/**
 * Created by Kelvin Khoo on 26/07/2016.
 */
public class CustomGridAdapter2 extends RecyclerView.Adapter<CustomGridAdapter2.ItemHolder> {

    private List<Trip> tripList;
    private OnItemClickListener onItemClickListener;

    public CustomGridAdapter2(List<Trip> tripList)
    {
        this.tripList = tripList;
    }
    @Override
    public CustomGridAdapter2.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        return new ItemHolder(itemView, this);    }

    @Override
    public void onBindViewHolder(CustomGridAdapter2.ItemHolder holder, int position) {
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

        private CustomGridAdapter2 adapter;
        TextView textView;
        ImageView imageView;

        public ItemHolder(View itemView, CustomGridAdapter2 parent) {
            super(itemView);
            //imageLoader = AppController.getInstance().getImageLoader();
            itemView.setOnClickListener(this);
            this.adapter = parent;
            textView = (TextView) itemView.findViewById(R.id.textView);
            imageView = (ImageView)itemView.findViewById(R.id.imageView);
        }

        public void bind(Trip item) {
            textView.setText(item.getTripName());
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
