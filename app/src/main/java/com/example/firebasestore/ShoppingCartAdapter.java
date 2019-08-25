package com.example.firebasestore;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ShoppingCartVH> {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private ArrayList<StoreItem> mShoppingCart;
    private LayoutInflater mInflater;
    private Context mContext;

    ShoppingCartAdapter(Context context, ArrayList<StoreItem> storeItems) {
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mShoppingCart = storeItems;
    }

    @NonNull
    @Override
    public ShoppingCartVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Choose which item layout to use.
        View mItemView = mInflater.inflate(R.layout.shopping_cart_item_layout, parent, false);
        return new ShoppingCartVH(mItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingCartVH holder, int position) {
        StoreItem current = mShoppingCart.get(position);
        holder.bindTo(current);
    }

    @Override
    public int getItemCount() {
        return mShoppingCart.size();
    }

    class ShoppingCartVH extends RecyclerView.ViewHolder {

        private ImageView mShoppingCartImageView;

        ShoppingCartVH(View itemView) {
            super(itemView);
            mShoppingCartImageView = itemView.findViewById(R.id.shoppingCartImageView);
        }

        void bindTo(final StoreItem currentItem) {

            // Load URL image into ImageView.
            Glide.with(mContext)
                    .load(currentItem.getImages().get(0).trim())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(mShoppingCartImageView);

            // Remove from cart button OnClickListener.
            itemView.findViewById(R.id.removeBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    CollectionReference storeItemsRef = db.collection("storeItems");

                    // Query to find document ID for the selected shopping cart item.
                    Query query = storeItemsRef
                            .whereEqualTo("brand", currentItem.getBrand())
                            .whereEqualTo("name", currentItem.getName())
                            .whereEqualTo("price", currentItem.getPrice());

                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful() && task.getResult() != null) {

                                String storeItemId = "";

                                // NOTE: Only one document ID should match with the query.
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    storeItemId = document.getId();
                                }

                                if (mAuth.getCurrentUser() != null) {

                                    // Remove the shopping cart item from the cloud.
                                    db.collection("shoppingCarts")
                                            .document(mAuth.getCurrentUser().getUid())
                                            .update("items", FieldValue.arrayRemove(storeItemId))
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                // Remove the shopping cart item from mShoppingCart and update UI.
                                                mShoppingCart.remove(currentItem);
                                                notifyDataSetChanged();
                                                ((ShoppingCartActivity) mContext).updateUI();
                                            }
                                        }
                                    });

                                } else {
                                    Toast.makeText(mContext, "Not signed in!", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(mContext, "Could not find store item!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            });
        }
    }
}
