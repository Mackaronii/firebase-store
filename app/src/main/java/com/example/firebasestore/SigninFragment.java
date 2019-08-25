package com.example.firebasestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SigninFragment extends Fragment {

    private FragmentActivity mFragmentActivity;

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mSignInBtn;
    private TextView mCreateTextView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mFragmentActivity = (FragmentActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signin_fragment_layout, container, false);

        mEmailField = view.findViewById(R.id.emailField);
        mPasswordField = view.findViewById(R.id.passwordField);
        mSignInBtn = view.findViewById(R.id.signinBtn);
        mCreateTextView = view.findViewById(R.id.createTextView);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {

                    if (!user.isEmailVerified()) {
                        // Launch VerificationActivity if the user has not verified email.
                        mAuth.removeAuthStateListener(mAuthListener);
                        startActivity(new Intent(mFragmentActivity, VerificationActivity.class));

                    } else {
                        // Replace SigninFragment with AccountFragment if the user has verified email.
                        mAuth.removeAuthStateListener(mAuthListener);
                        FragmentTransaction transaction = mFragmentActivity.getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.root_frame, new AccountFragment());
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
            }
        };

        mSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSignIn();
            }
        });

        mCreateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mFragmentActivity, CreateAccountActivity.class));
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void startSignIn() {

        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        // Make sure both email and password are filled.
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {

            Toast.makeText(mFragmentActivity, "Fields must not be empty.", Toast.LENGTH_SHORT).show();

        } else {

            // Register new user.
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mFragmentActivity, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (!task.isSuccessful()) {
                        Toast.makeText(mFragmentActivity, "Incorrect username or password.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
}
