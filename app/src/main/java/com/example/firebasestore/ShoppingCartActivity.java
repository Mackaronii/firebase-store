package com.example.firebasestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;

public class ShoppingCartActivity extends AppCompatActivity {

    private final String TAG = ShoppingCartActivity.class.getSimpleName();

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ShoppingCartAdapter mShoppingCartAdapter;
    private ArrayList<StoreItem> mShoppingCart;
    private ArrayList<String> mStoreItemIds;

    private RecyclerView mShoppingCartRecyclerView;
    private TextView mEmptyShoppingCartTextView;
    private TextView mPurchaseTotalTextView;
    private Button mCheckOutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        mShoppingCart = new ArrayList<>();

        mPurchaseTotalTextView = findViewById(R.id.purchaseTotalTextView);
        mEmptyShoppingCartTextView = findViewById(R.id.emptyShoppingCartTextView);
        mCheckOutBtn = findViewById(R.id.checkOutBtn);

        mShoppingCartRecyclerView = findViewById(R.id.shoppingCartRecyclerView);
        mShoppingCartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mShoppingCartAdapter = new ShoppingCartAdapter(this, mShoppingCart);
        mShoppingCartRecyclerView.setAdapter(mShoppingCartAdapter);

        if (mAuth.getCurrentUser() != null) {

            DocumentReference userShoppingCartRef = db.collection("shoppingCarts").document(mAuth.getCurrentUser().getUid());

            // Get the user's shopping cart.
            userShoppingCartRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    if (documentSnapshot.exists()) {

                        mStoreItemIds = (ArrayList) documentSnapshot.get("items");

                        // Update private member mShoppingCart to update the RecyclerView.
                        for (String storeItemId : mStoreItemIds) {

                            db.collection("storeItems").document(storeItemId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    if (task.isSuccessful() && task.getResult() != null) {
                                        StoreItem current = task.getResult().toObject(StoreItem.class);
                                        mShoppingCart.add(current);
                                        updateUI();

                                    } else {
                                        Toast.makeText(ShoppingCartActivity.this, "Could not find cart item in store.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                    } else {
                        Toast.makeText(ShoppingCartActivity.this, "Could not find shopping cart.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } else {
            Toast.makeText(this, "Not signed in!", Toast.LENGTH_SHORT).show();
        }

        mCheckOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkOut();
            }
        });
    }

    public void updateUI() {

        mShoppingCartAdapter.notifyDataSetChanged();

        if (!mShoppingCart.isEmpty()) {

            // Update and display purchase total.
            double purchaseTotal = 0;
            NumberFormat formatter = NumberFormat.getCurrencyInstance();

            for (StoreItem item : mShoppingCart) {
                purchaseTotal += item.getPrice();
            }

            String stringTotal = "Total: " + formatter.format(purchaseTotal);
            mPurchaseTotalTextView.setText(stringTotal);

            mEmptyShoppingCartTextView.setVisibility(View.GONE);
            mPurchaseTotalTextView.setVisibility(View.VISIBLE);

        } else {
            mEmptyShoppingCartTextView.setVisibility(View.VISIBLE);
            mPurchaseTotalTextView.setVisibility(View.GONE);
        }
    }

    private void checkOut() {

        if (mAuth.getCurrentUser() != null) {

            // Get all store item IDs in the user's shopping cart.
            db.collection("shoppingCarts").document(mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    if (documentSnapshot.exists()) {

                        mStoreItemIds = (ArrayList) documentSnapshot.get("items");

                        for (final String storeItemId : mStoreItemIds) {

                            // Decrement the stock for each store item in the user's shopping cart.
                            db.collection("storeItems").document(storeItemId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    if (task.isSuccessful()) {

                                        final StoreItem current = task.getResult().toObject(StoreItem.class);


                                        if (current.getStock() > 0) {

                                            // Decrease stock by 1 if stock exists.
                                            db.collection("storeItems").document(storeItemId).update("stock", FieldValue.increment(-1)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(ShoppingCartActivity.this, "Purchase complete.", Toast.LENGTH_SHORT).show();

                                                    // Remove the store item from the user's shopping cart (both cloud and locally).
                                                    db.collection("shoppingCarts").document(mAuth.getCurrentUser().getUid()).update("items", FieldValue.arrayRemove(storeItemId));
                                                    mShoppingCart.remove(current);
                                                    updateUI();
                                                }
                                            });

                                        } else {
                                            String msg = current.getName() + " is out of stock!";
                                            Toast.makeText(ShoppingCartActivity.this, msg, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
    }
}
