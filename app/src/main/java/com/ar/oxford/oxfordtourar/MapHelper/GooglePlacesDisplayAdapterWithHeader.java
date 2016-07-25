package com.ar.oxford.oxfordtourar.MapHelper;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ar.oxford.oxfordtourar.R;
import com.ar.oxford.oxfordtourar.model.Place;

import java.util.List;

/**
 * Created by Kelvin Khoo on 29/06/2016.
 */
public class GooglePlacesDisplayAdapterWithHeader extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private List<Place> list;
    private OnItemClickListener onItemClickListener;


    public GooglePlacesDisplayAdapterWithHeader(List<Place> list) {
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_HEADER)
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_item, parent, false);
            return  new VHHeader(v);
        }
        else if (viewType == TYPE_ITEM)
        {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sheet_main, parent, false);
            return new ItemHolder(itemView,this);

        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position){
        if(holder instanceof VHHeader)
        {
            VHHeader VHheader = (VHHeader)holder;
            if(list.size()==0)
            {
                VHheader.txtTitle.setText("No Result Found");
            }
            else
            {
                VHheader.txtTitle.setText("List of Places");
            }
        }
        else if(holder instanceof ItemHolder)
        {
            ItemHolder itemHolder = (ItemHolder)holder;
            itemHolder.textView.setText(list.get(position).getName());
            //holder.bind(list.get(position));
        }
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

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private GooglePlacesDisplayAdapterWithHeader adapter;
        TextView textView;
        ImageView imageView;

        public ItemHolder(View itemView, GooglePlacesDisplayAdapterWithHeader parent) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.adapter = parent;
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            textView = (TextView) itemView.findViewById(R.id.textView);
        }

        public void bind(Place item) {

            textView.setText(item.getName());
        }

        @Override
        public void onClick(View v) {
            final OnItemClickListener listener = adapter.getOnItemClickListener();
            if (listener != null) {
                listener.onItemClick(this, getAdapterPosition());
            }
        }
    }

    //    need to override this method
    @Override
    public int getItemViewType(int position) {
        if(isPositionHeader(position))
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position)
    {
        return position == 0;
    }


    class VHHeader extends RecyclerView.ViewHolder{
        TextView txtTitle;
        public VHHeader(View itemView) {
            super(itemView);
            this.txtTitle = (TextView)itemView.findViewById(R.id.txtHeader);
        }
    }
}
