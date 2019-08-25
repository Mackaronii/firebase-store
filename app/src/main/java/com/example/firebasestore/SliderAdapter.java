package com.example.firebasestore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;

public class SliderAdapter extends SliderViewAdapter<SliderAdapter.SliderAdapterVH> {

    private ArrayList<String> mImages;

    SliderAdapter(ArrayList<String> images) {
        this.mImages = images;
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_item_layout, parent, false);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, int position) {
        Glide.with(viewHolder.itemView)
                .load(mImages.get(position))
                .into(viewHolder.imageView);
    }

    @Override
    public int getCount() {
        return mImages.size();
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        private View itemView;
        private ImageView imageView;

        SliderAdapterVH(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.imageView = itemView.findViewById(R.id.imageSliderImageView);
        }

    }
}
