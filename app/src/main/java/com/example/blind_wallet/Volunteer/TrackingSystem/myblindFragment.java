package com.example.blind_wallet.Volunteer.TrackingSystem;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.blind_wallet.R;
import com.example.blind_wallet.Volunteer.TrackingSystem.Model.Tracking;
import com.example.blind_wallet.Volunteer.TrackingSystem.Model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;


/**
 * A simple {@link Fragment} subclass.
 */
public class myblindFragment extends Fragment implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    DatabaseReference onlineRef, currentUserRef, counterRef, locations;
    FirebaseRecyclerAdapter<User, ListOnlineViewHolder> adapter;

    RecyclerView listOnline;
    RecyclerView.LayoutManager layoutManager;

    private static final int MY_PERMISSION_REQUEST_CODE = 7171;
    private static final int PLAY_SERVICES_RES_REQUEST = 7172;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISTANCE = 10;

    String Bemail = null, Vemail = null, Statues = null;

    public myblindFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_myblind, container, false);

        listOnline = view.findViewById(R.id.onlinelist);
        listOnline.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        listOnline.setLayoutManager(layoutManager);

        locations = FirebaseDatabase.getInstance("https://blind-wallet-be409-default-rtdb.firebaseio.com/").getReference("Locations");
        onlineRef = FirebaseDatabase.getInstance("https://blind-wallet-be409-default-rtdb.firebaseio.com/").getReference().child(".info/connected");
        counterRef = FirebaseDatabase.getInstance("https://blind-wallet-be409-default-rtdb.firebaseio.com/").getReference("lastOnline");
        currentUserRef = FirebaseDatabase.getInstance("https://blind-wallet-be409-default-rtdb.firebaseio.com/").getReference("lastOnline")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);
        } else {
            buildGoogleApiClient();
            createLocationRequest();
            displayLocation();
        }
        updateList();
        setupSystem();
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
            }
            break;
        }
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(getContext()).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mLastLocation = location;
            }
        });
        if (mLastLocation != null) {
            Toast.makeText(getContext(), String.valueOf(mLastLocation.getLatitude()), Toast.LENGTH_SHORT).show();
            locations.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(new Tracking(FirebaseAuth.getInstance().getCurrentUser().getEmail()
                            , FirebaseAuth.getInstance().getCurrentUser().getUid()
                            , String.valueOf(mLastLocation.getLatitude())
                            , String.valueOf(mLastLocation.getLongitude())));
        }
    }

    @SuppressLint("RestrictedApi")
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISTANCE);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity) getContext(), PLAY_SERVICES_RES_REQUEST).show();
            } else {
                Toast.makeText(getContext(), "This Device Is Not Supported", Toast.LENGTH_LONG).show();
            }
            return false;
        }
        return true;
    }

    private void updateList() {
        Vemail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = fstore.collection("RelationShip").document(Vemail);
        documentReference.addSnapshotListener((Activity) getContext(), (documentSnapshot, e) -> {
            if (e == null) {
                Bemail = documentSnapshot.getString("Blind Email");
                Statues = documentSnapshot.getString("Statuse");
            }
            if (Bemail != null && Statues != "false") {
                Query query = FirebaseDatabase.getInstance("https://blind-wallet-be409-default-rtdb.firebaseio.com/").getReference("lastOnline").orderByChild("email").equalTo(Bemail);
                FirebaseDatabase.getInstance("https://blind-wallet-be409-default-rtdb.firebaseio.com/").getReference("lastOnline").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            User user = new User();
                            user = postSnapshot.getValue(User.class);
                            if (user.getEmail().equals(Bemail) && user.getStatus().equals("Online")) {
                                adapter = new FirebaseRecyclerAdapter<User, ListOnlineViewHolder>
                                        (User.class, R.layout.user_layout, ListOnlineViewHolder.class, query) {
                                    @Override
                                    protected void populateViewHolder(ListOnlineViewHolder listOnlineViewHolder, final User user, int i) {

                                        listOnlineViewHolder.txtEmail.setText(user.getEmail());
                                        listOnlineViewHolder.button.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (!user.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                                    Intent map = new Intent(getContext(), MapTracking.class);
                                                    map.putExtra("email", user.getEmail());
                                                    map.putExtra("lat", mLastLocation.getLatitude());
                                                    map.putExtra("lng", mLastLocation.getLongitude());
                                                    startActivity(map);
                                                } else {
                                                    Toast.makeText(getContext(), "getContext() Me", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                };
                                adapter.notifyDataSetChanged();
                                listOnline.setAdapter(adapter);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            } else {
                Toast.makeText(getContext(), "No Found Any Blind...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSystem() {
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue(Boolean.class)) {
                    currentUserRef.onDisconnect().removeValue();
                    counterRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(new User(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "Online"));
                    if (adapter != null)
                        adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        counterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    User user = postSnapshot.getValue(User.class);
                    Log.d("LOG", "" + user.getEmail() + "is" + user.getStatus());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();
    }

}
