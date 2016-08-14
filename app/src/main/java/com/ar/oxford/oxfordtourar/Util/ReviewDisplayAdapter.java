package com.ar.oxford.oxfordtourar.Util;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ar.oxford.oxfordtourar.R;
import com.ar.oxford.oxfordtourar.model.Review;

import java.util.Date;
import java.util.List;

/**
 * Created by Kelvin Khoo on 29/06/2016.
 */
public class  ReviewDisplayAdapter extends RecyclerView.Adapter<ReviewDisplayAdapter.ItemHolder>{

    private List<Review> list;
    private OnItemClickListener onItemClickListener;


    public ReviewDisplayAdapter(List<Review> list) {
        this.list = list;
    }

    @Override
    public ReviewDisplayAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_row, parent, false);
        return new ItemHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(ReviewDisplayAdapter.ItemHolder holder, int position) {
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

        private ReviewDisplayAdapter adapter;
        ImageLoader imageLoader;
        NetworkImageView thumbNail;
        TextView authorName;
        RatingBar ratingBar;
        TextView ratings;
        TextView timestamp;
        TextView review;



        public ItemHolder(View itemView, ReviewDisplayAdapter parent) {
            super(itemView);
            imageLoader = AppController.getInstance().getImageLoader();
            itemView.setOnClickListener(this);
            this.adapter = parent;

            thumbNail = (NetworkImageView) itemView.findViewById(R.id.thumbnail);
            authorName = (TextView) itemView.findViewById(R.id.author_name);
            ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBar);
            ratings = (TextView) itemView.findViewById(R.id.ratings);
            timestamp = (TextView) itemView.findViewById(R.id.time_stamp);
            review = (TextView) itemView.findViewById(R.id.review);
        }

        public void bind(Review item) {
            authorName.setText(item.getAuthorName());
            ratings.setText(Float.toString(item.getRating()));
            ratingBar.setRating(item.getRating());
            Date date = new Date(item.getTimeStamp()*1000);
            timestamp.setText(date.toString());
            review.setText(item.getReview());
            String imageUrl = "http:"+item.getPhotoUrl();
            thumbNail.setErrorImageResId(R.drawable.no_profile_pic_icon);
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


