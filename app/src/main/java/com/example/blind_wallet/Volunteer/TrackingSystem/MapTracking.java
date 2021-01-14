package com.example.blind_wallet.Volunteer.TrackingSystem;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.blind_wallet.R;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.blind_wallet.Volunteer.TrackingSystem.Model.Tracking;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import Classes.Speech;

public class MapTracking extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private String email;

    DatabaseReference locations;
    double lat,lng;
    LatLng friendLocation;
    MarkerOptions markerOptions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locations = FirebaseDatabase.getInstance("https://blind-wallet-be409-default-rtdb.firebaseio.com/").getReference("Locations");

        if(getIntent() != null){
            email = getIntent().getStringExtra("email");
            lat = getIntent().getDoubleExtra("lat",0);
            lng = getIntent().getDoubleExtra("lng",0);
        }

    }

    private double distance(Location currentUser, Location friend) {
        double theta = currentUser.getLongitude() - friend.getLongitude();
        double dist = Math.sin(deg2rad(currentUser.getLatitude())) * Math.sin(deg2rad(friend.getLatitude()))
                        * Math.cos(deg2rad(currentUser.getLatitude())) * Math.cos(friend.getLatitude())
                        *Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    private double deg2rad(double latitude) {
        return (latitude * Math.PI / 180.0);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE)
    {
        Geocoder geocoder;
        List<Address> addresses ;
        geocoder = new Geocoder(this, Locale.getDefault());
        String address = null;
        try {
            addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null && addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Query user_location = locations.orderByChild("email").equalTo(email);
        user_location.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot postSnapShot:snapshot.getChildren()) {
                    Tracking tracking = postSnapShot.getValue(Tracking.class);

                    friendLocation = new LatLng(Double.parseDouble(tracking.getLat()),Double.parseDouble(tracking.getLng()));
                    Location currentUser = new Location("");
                    currentUser.setLatitude(lat);
                    currentUser.setLongitude(lng);
                    Toast.makeText(MapTracking.this, "Fareed "+currentUser.getLongitude() + "  "+currentUser.getLatitude(), Toast.LENGTH_SHORT).show();

                    String address = getCompleteAddressString(Double.parseDouble(tracking.getLat()),Double.parseDouble(tracking.getLng()));
                    Speech s = new Speech();
                    s.Texttospeech(getApplicationContext(),address);
                    Toast.makeText(MapTracking.this, "Paula "+currentUser.getLongitude() + "  "+currentUser.getLatitude(), Toast.LENGTH_SHORT).show();

                    markerOptions = new MarkerOptions()
                            .position(friendLocation)
                            .title(tracking.getEmail())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),12.0f));

                }
                googleMap.clear();
                googleMap.addMarker(markerOptions);
                LatLng current = new LatLng(lat,lng);
                googleMap.addMarker(new MarkerOptions()
                        .position(current)
                        .title(FirebaseAuth.getInstance().getCurrentUser().getEmail()));

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
