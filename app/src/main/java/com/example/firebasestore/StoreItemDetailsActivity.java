package com.example.firebasestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.text.NumberFormat;
import java.util.ArrayList;

public class StoreItemDetailsActivity extends AppCompatActivity {

    private final String TAG = StoreItemDetailsActivity.class.getSimpleName();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private StoreItem mItem;

    private SliderView mImageSlider;
    private TextView mItemBrand;
    private TextView mItemName;
    private TextView mItemPrice;
    private TextView mItemDescription;
    private Button mAddToCartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.store_item_details_layout);

        // Extract store item information.
        Intent intent = getIntent();
        mItem = (StoreItem) intent.getSerializableExtra(StoreItemAdapter.EXTRA_STORE_ITEM);

        ArrayList<String> images= mItem.getImages();
        String brand = mItem.getBrand();
        String name = mItem.getName();
        double price = mItem.getPrice();
        String description = mItem.getDescription();
        int stock = mItem.getStock();

        // Set up slider adapter and animations.
        mImageSlider = findViewById(R.id.imageSlider);
        mImageSlider.setSliderAdapter(new SliderAdapter(images));
        mImageSlider.setIndicatorAnimation(IndicatorAnimations.WORM);
        mImageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);

        // Get view handles.
        mItemBrand = findViewById(R.id.itemBrand);
        mItemName = findViewById(R.id.itemName);
        mItemPrice = findViewById(R.id.itemPrice);
        mItemDescription = findViewById(R.id.itemDescription);
        mAddToCartBtn = findViewById(R.id.addToCartBtn);

        mItemBrand.setText(brand);
        mItemName.setText(name);

        // OUT OF STOCK conditions.
        if (stock <= 0) {
            mItemPrice.setText("OUT OF STOCK");
            mItemPrice.setTextColor(getResources().getColor(R.color.colorRed));
            mAddToCartBtn.setEnabled(false);
            mAddToCartBtn.setBackgroundColor(getResources().getColor(R.color.colorGrey));

        } else {
            NumberFormat formatter = NumberFormat.getCurrencyInstance();
            mItemPrice.setText(formatter.format(price));
        }

        mItemDescription.setText(description);

        mAddToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToCart();
            }
        });
    }

    private void addToCart() {

        if (mAuth.getCurrentUser() != null) {

            if (mItem.getStock() <= 0) {

                Toast.makeText(this, "This item is out of stock!", Toast.LENGTH_SHORT).show();

            } else {

                CollectionReference storeItemsRef = db.collection("storeItems");

                // Query to find document ID for the added store item.
                Query query = storeItemsRef
                        .whereEqualTo("brand", mItem.getBrand())
                        .whereEqualTo("name", mItem.getName())
                        .whereEqualTo("price", mItem.getPrice());

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful() && task.getResult() != null) {

                            // Found document ID based on the item.
                            final DocumentSnapshot storeItem = task.getResult().getDocuments().get(0);
                            final String storeItemId = storeItem.getId();
                            final CollectionReference shoppingCartsRef = db.collection("shoppingCarts");

                            // Add document ID to shopping cart of the current user.
                            shoppingCartsRef.document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    if (task.isSuccessful() && task.getResult() != null) {
                                        DocumentSnapshot userShoppingCart = task.getResult();
                                        userShoppingCart.getReference().update("items", FieldValue.arrayUnion(storeItemId));
                                        Toast.makeText(StoreItemDetailsActivity.this, storeItem.getString("name") + " has been added to your cart!", Toast.LENGTH_SHORT).show();

                                    } else {
                                        Toast.makeText(StoreItemDetailsActivity.this, "Failed to add item to cart!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(StoreItemDetailsActivity.this, "Failed to find store item!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        } else {
            Toast.makeText(this, "Please sign in to add to cart!", Toast.LENGTH_SHORT).show();
        }
    }
}
