package com.utkarsh.ridesharingappprototype.rider;



import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.utkarsh.ridesharingappprototype.R;
import com.utkarsh.ridesharingappprototype.models.Ride;

public class RideRequestActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private TextView tvPickupLocation, tvDestination, tvEstimatedFare;
    private Spinner spinnerVehicleType;
    private Button btnConfirmRide;
    private DatabaseReference rideRequestsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_request);

        rideRequestsRef = FirebaseDatabase.getInstance().getReference("rideRequests");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        tvPickupLocation = findViewById(R.id.tvPickupLocation);
        tvDestination = findViewById(R.id.tvDestination);
        tvEstimatedFare = findViewById(R.id.tvEstimatedFare);
        spinnerVehicleType = findViewById(R.id.spinnerVehicleType);
        btnConfirmRide = findViewById(R.id.btnConfirmRide);

        btnConfirmRide.setOnClickListener(v -> confirmRide());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Configure map settings and markers
    }

    private void confirmRide() {
        String rideId = rideRequestsRef.push().getKey();
        String riderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String vehicleType = spinnerVehicleType.getSelectedItem().toString();

        Ride ride = new Ride(rideId, riderId, null, "requested",
                System.currentTimeMillis(), vehicleType);

        rideRequestsRef.child(rideId).setValue(ride)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Ride requested successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to request ride", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}