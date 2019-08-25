package com.example.firebasestore;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerificationAsyncTask extends AsyncTask<Void, Void, Void> {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final Activity mActivity;
    private final ProgressDialog mProgressDialog;

    VerificationAsyncTask(Activity activity) {
        mActivity = activity;

        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setTitle("Signing in...");
        mProgressDialog.setMessage("Verifying email.");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        if (mAuth.getCurrentUser() != null) {

            try {
                // Refresh the cache in case the user verified email while keeping the activity open.
                mAuth.getCurrentUser().reload();

                // Delay required for cache to process.
                Thread.sleep(2000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (mAuth.getCurrentUser() != null) {

            boolean emailVerified = mAuth.getCurrentUser().isEmailVerified();

            if (emailVerified) {

                mProgressDialog.setMessage("Setting up your shopping cart.");

                // Set up new user shopping cart.
                final CollectionReference shoppingCartsRef = db.collection("shoppingCarts");
                Map<String, Object> data = new HashMap<>();
                List<String> items = new ArrayList<>();
                data.put("items", items);

                shoppingCartsRef.document(mAuth.getCurrentUser().getUid()).set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            try {
                                // Launch MainActivity after a delay for style.
                                Thread.sleep(2000);
                                mProgressDialog.dismiss();
                                mActivity.startActivity(new Intent(mActivity, MainActivity.class));

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        } else {
                            mProgressDialog.dismiss();
                            Toast.makeText(mActivity, "Failed to create shopping cart.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else {
                mProgressDialog.dismiss();
                Toast.makeText(mActivity, "Your email is not verified!", Toast.LENGTH_SHORT).show();
            }

        } else {
            mProgressDialog.dismiss();
            Toast.makeText(mActivity, "Not signed in.", Toast.LENGTH_SHORT).show();
        }
    }
}
