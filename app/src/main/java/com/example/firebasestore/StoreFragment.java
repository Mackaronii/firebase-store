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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class StoreFragment extends Fragment implements AdapterView.OnItemSelectedListener {

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

        // Initialize RecyclerView.
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mAdapter = new StoreItemAdapter(getActivity(), mStoreItems);
        mRecyclerView.setAdapter(mAdapter);

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

        // Initialize spinners.
        Spinner filterSpinner = view.findViewById(R.id.filterSpinner);
        Spinner sortSpinner = view.findViewById(R.id.sortSpinner);

        // Initialize spinner adapters.
        ArrayAdapter<CharSequence> filterAdapter =
                ArrayAdapter.createFromResource(
                        view.getContext(), R.array.filter_array,
                        android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> sortAdapter =
                ArrayAdapter.createFromResource(
                        view.getContext(), R.array.sort_array,
                        android.R.layout.simple_spinner_item);

        filterAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);

        sortAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);

        // Link spinners to adapters and listeners.
        if (filterSpinner != null) {
            filterSpinner.setAdapter(filterAdapter);
            filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    String filter = adapterView.getItemAtPosition(position).toString().toLowerCase();
                    Query query;

                    if (filter.equals("no filter")) {
                        query = db.collection("storeItems");
                    } else {
                        query = db.collection("storeItems").whereEqualTo("category", filter);
                    }

                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful() && task.getResult() != null) {

                                mStoreItems.clear();

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    StoreItem item = document.toObject(StoreItem.class);
                                    mStoreItems.add(item);
                                }

                                mAdapter.notifyDataSetChanged();

                            } else {
                                Log.d(TAG, "Failed to apply filter!");
                            }
                        }
                    });
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    // Do nothing.
                }
            });
        }

        if (sortSpinner != null) {
            sortSpinner.setAdapter(sortAdapter);
            sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    // Find a way to get both spinner values in order to combine filter and sort.
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    // Do nothing.
                }
            });
        }

        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        String text = adapterView.getItemAtPosition(position).toString();
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
