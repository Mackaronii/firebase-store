package com.example.firebasestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class StoreFragment extends Fragment {

    private static final String TAG = StoreFragment.class.getSimpleName();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<StoreItem> mStoreItems;
    private RecyclerView mRecyclerView;
    private StoreItemAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.store_fragment_layout, container, false);

        mStoreItems = new ArrayList<>();

        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mAdapter = new StoreItemAdapter(getActivity(), mStoreItems);
        mRecyclerView.setAdapter(mAdapter);

        // Initialize store items.
        db.collection("storeItems")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful() && task.getResult() != null) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                StoreItem item = document.toObject(StoreItem.class);
                                mStoreItems.add(item);
                            }

                            mAdapter.notifyDataSetChanged();

                        } else {
                            Log.d(TAG, "Failed to load store items!");
                        }
                    }
                });

        // Add listener to detect real time updates.
        db.collection("storeItems")
                .addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (queryDocumentSnapshots != null) {

                    mStoreItems.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        StoreItem item = document.toObject(StoreItem.class);
                        mStoreItems.add(item);
                    }

                    mAdapter.notifyDataSetChanged();

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        return view;
    }
}
