package com.utkarsh.ridesharingappprototype.driver;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utkarsh.ridesharingappprototype.R;

public class DriverDashboardActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference rideRequestsRef, driversRef;
    private ValueEventListener rideRequestListener;
    private String currentRideId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_dashboard);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        rideRequestsRef = FirebaseDatabase.getInstance().getReference("rideRequests");
        driversRef = FirebaseDatabase.getInstance().getReference("drivers");

        listenForRideRequests();
        startLocationUpdates();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,

        new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult != null) {
                            Location location = locationResult.getLastLocation();
                            updateDriverLocation(location);
                        }
                    }
                },
                Looper.getMainLooper());
    }

    private void updateDriverLocation(Location location) {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        GeoLocation geoLocation = new GeoLocation(location.getLatitude(), location.getLongitude());

        driversRef.child(driverId).child("location").setValue(geoLocation);
    }

    private void listenForRideRequests() {
        rideRequestListener = rideRequestsRef.orderByChild("status").equalTo("requested")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                            RideRequest rideRequest = rideSnapshot.getValue(RideRequest.class);
                            if (rideRequest != null && currentRideId == null) {
                                showRideRequest(rideSnapshot.getKey(), rideRequest);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    private void showRideRequest(String rideId, RideRequest rideRequest) {
        // Show dialog with ride request info
        new AlertDialog.Builder(this)
                .setTitle("New Ride Request")
                .setMessage("Do you want to accept this ride?")
                .setPositiveButton("Accept", (dialog, which) -> acceptRide(rideId))
                .setNegativeButton("Decline", null)
                .show();
    }

    private void acceptRide(String rideId) {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rideRequestsRef.child(rideId).child("driverId").setValue(driverId);
        rideRequestsRef.child(rideId).child("status").setValue("accepted");
        currentRideId = rideId;

        // Stop listening for new requests
        rideRequestsRef.removeEventListener(rideRequestListener);
    }
}