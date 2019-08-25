package com.example.firebasestore;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.text.NumberFormat;
import java.util.ArrayList;

public class StoreItemAdapter extends RecyclerView.Adapter<StoreItemAdapter.StoreItemViewHolder> {

    public static final String EXTRA_STORE_ITEM = "com.example.firebasestore.extra.ITEM";

    private ArrayList<StoreItem> mStoreItems;
    private LayoutInflater mInflater;
    private Context mContext;

    StoreItemAdapter(Context context, ArrayList<StoreItem> storeItems) {
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mStoreItems = storeItems;
    }

    @NonNull
    @Override
    public StoreItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.store_item_layout, parent, false);
        return new StoreItemViewHolder(mItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreItemViewHolder holder, int position) {
        StoreItem current = mStoreItems.get(position);
        holder.bindTo(current);
    }

    @Override
    public int getItemCount() {
        return mStoreItems.size();
    }

    class StoreItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView mItemImage;
        private TextView mItemBrand;
        private TextView mItemName;
        private TextView mItemPrice;

        StoreItemViewHolder(View itemView) {
            super(itemView);
            mItemImage = itemView.findViewById(R.id.itemImage);
            mItemBrand = itemView.findViewById(R.id.itemBrand);
            mItemName = itemView.findViewById(R.id.itemName);
            mItemPrice = itemView.findViewById(R.id.itemPrice);
        }

        void bindTo(final StoreItem currentItem) {

            // Load URL image into ImageView.
            Glide.with(mContext)
                    .load(currentItem.getImages().get(0).trim())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(mItemImage);

            mItemBrand.setText(currentItem.getBrand());
            mItemName.setText(currentItem.getName());

            // OUT OF STOCK styling.
            if (currentItem.getStock() <= 0) {
                mItemPrice.setText("OUT OF STOCK");
                mItemPrice.setTextColor(mContext.getResources().getColor(R.color.colorRed));

            } else {
                NumberFormat formatter = NumberFormat.getCurrencyInstance();
                mItemPrice.setText(formatter.format(currentItem.getPrice()));
                mItemPrice.setTextColor(mContext.getResources().getColor(android.R.color.tab_indicator_text));
            }

            // Open item details.
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Pass currentItem to StoreItemDetailsActivity.
                    Intent intent = new Intent(view.getContext(), StoreItemDetailsActivity.class);
                    intent.putExtra(EXTRA_STORE_ITEM, currentItem);
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
