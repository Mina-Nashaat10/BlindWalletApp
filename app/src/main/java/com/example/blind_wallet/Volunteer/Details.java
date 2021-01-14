package com.example.blind_wallet.Volunteer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.blind_wallet.R;
import com.example.blind_wallet.Volunteer.TrackingSystem.Model.User;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Details extends AppCompatActivity {

    StorageReference storageReference;
    FirebaseAuth fAuth;
    String UserId;
    CircleImageView circleImageView;
    boolean isfound = false;
    String usertype = "0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        TextView name = (TextView) findViewById(R.id.name);
        TextView email = (TextView) findViewById(R.id.email);
        TextView fname = (TextView) findViewById(R.id.fname);
        TextView phone = (TextView) findViewById(R.id.phone);
        TextView address = (TextView)findViewById(R.id.location);
        circleImageView = findViewById(R.id.circleimage2);
        Button delete = findViewById(R.id.delete);
        Bundle b = getIntent().getExtras();

        String x = b.getString("fname");
        String z = b.getString("email");
        String m = b.getString("phone");
        String w = b.getString("address");
        String id = b.getString("image");



        name.setText(x);
        fname.setText(x);
        email.setText(z);
        phone.setText(m);
        address.setText(w);
        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference profileRef=storageReference.child("users/"+id+"/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(circleImageView);
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection("RelationShip").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            for (QueryDocumentSnapshot snapshot:task.getResult())
                            {
                                if (snapshot.getString("Blind Email").equals(z) || snapshot.getString("Volunteer Email").equals(z))
                                {
                                    isfound = true;
                                }
                            }
                        }
                        if (isfound == true)
                        {
                            Toast.makeText(Details.this, "User has Relationship with anthor user", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            firestore.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful())
                                    {
                                        for (QueryDocumentSnapshot snapshot:task.getResult())
                                        {
                                            if (snapshot.getString("email").equals(z))
                                            {
                                                UserId = snapshot.getId();
                                                usertype = snapshot.getString("type");
                                            }
                                        }
                                    }
                                    DocumentReference reference = firestore.collection("users").document(UserId);
                                    reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(Details.this, "User Delete...", Toast.LENGTH_SHORT).show();
                                            if (usertype == "0")
                                                startActivity(new Intent(getApplicationContext(),BlindFragment.class));
                                            else
                                                startActivity(new Intent(getApplicationContext(), VolunteerFragment.class));
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(Details.this, "Cannot To Delete User...", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });

                        }
                    }
                });

            }
        });
    }
}
