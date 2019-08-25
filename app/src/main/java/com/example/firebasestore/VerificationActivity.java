package com.example.firebasestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerificationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private Button mVerifyBtn;
    private Button mSignInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        mAuth = FirebaseAuth.getInstance();

        mUser = mAuth.getCurrentUser();
        mVerifyBtn = findViewById(R.id.verifyBtn);
        mSignInBtn = findViewById(R.id.signinBtn);

        mVerifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmailVerification();
            }
        });

        mSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new VerificationAsyncTask(VerificationActivity.this).execute();
            }
        });
    }

    private void sendEmailVerification() {
        mUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(VerificationActivity.this, "Verification already sent!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(VerificationActivity.this, "Email sent.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Prevent SigninFragment from looping back.
        mAuth.signOut();
        super.onBackPressed();
    }
}
