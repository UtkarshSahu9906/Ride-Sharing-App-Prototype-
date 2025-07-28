package com.utkarsh.ridesharingappprototype.auth;



import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.utkarsh.ridesharingappprototype.R;

public class UserTypeSelectionActivity extends AppCompatActivity {
    private Button btnRider, btnDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type_selection);

        btnRider = findViewById(R.id.btnRider);
        btnDriver = findViewById(R.id.btnDriver);

        btnRider.setOnClickListener(v -> {
            updateUserType("rider");
            startActivity(new Intent(UserTypeSelectionActivity.this, RiderDashboardActivity.class));
            finish();
        });

        btnDriver.setOnClickListener(v -> {
            updateUserType("driver");
            startActivity(new Intent(UserTypeSelectionActivity.this, DriverDashboardActivity.class));
            finish();
        });
    }

    private void updateUserType(String userType) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("userType").setValue(userType);
    }
}