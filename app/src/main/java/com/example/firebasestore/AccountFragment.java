package com.example.firebasestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class AccountFragment extends Fragment {

    private FragmentActivity mFragmentActivity;
    private FragmentManager mFragmentManager;

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private TextView mEmailField;
    private EditText mUpdateField;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mFragmentActivity = (FragmentActivity) context;
        mFragmentManager = mFragmentActivity.getSupportFragmentManager();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.account_fragment_layout, container, false);

        mEmailField = view.findViewById(R.id.emailField);
        mUpdateField = view.findViewById(R.id.updateField);

        Button mUpdateBtn = view.findViewById(R.id.updateBtn);
        Button mSignoutBtn = view.findViewById(R.id.signoutBtn);
        Button mDeleteBtn = view.findViewById(R.id.deleteBtn);

        mUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateEmail();
            }
        });

        mSignoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displaySignOutDialog();
            }
        });

        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayDeleteDialog();
            }
        });

        initializeUI();
        return view;
    }

    private void initializeUI() {

        String msg;

        if (mAuth.getCurrentUser() != null) {
            msg = "Logged In\n" + "User: " + mAuth.getCurrentUser().getEmail();
        } else {
            msg = "Signed Out.";
        }

        mEmailField.setText(msg);
    }

    private void updateEmail() {

        String newEmail = mUpdateField.getText().toString().trim();

        if (TextUtils.isEmpty(newEmail)) {
            Toast.makeText(mFragmentActivity, "New Email field is empty!", Toast.LENGTH_SHORT).show();

        } else {

            if (mAuth.getCurrentUser() != null) {

                mAuth.getCurrentUser().updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(mFragmentActivity, "Failed to update email.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mFragmentActivity, "Email updated.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else {
                Toast.makeText(mFragmentActivity, "You are not currently signed in!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayDeleteDialog() {
        new AlertDialog.Builder(mFragmentActivity)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (mAuth.getCurrentUser() != null) {
                            mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(mFragmentActivity, "Failed to delete user account.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        mAuth.signOut();
                                        loadSignIn();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(mFragmentActivity, "You are not currently signed in!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void displaySignOutDialog() {
        new AlertDialog.Builder(mFragmentActivity)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAuth.signOut();
                        loadSignIn();
                    }
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

    private void loadSignIn() {
        // Replace AccountFragment with SigninFragment.
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.root_frame, new SigninFragment());
        transaction.addToBackStack(null);
        transaction.commit();
        ((MainActivity) mFragmentActivity).setViewPager(1);
    }
}
