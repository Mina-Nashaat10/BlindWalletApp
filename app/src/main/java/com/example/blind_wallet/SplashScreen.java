package com.example.blind_wallet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blind_wallet.Volunteer.Main3Activity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.example.blind_wallet.Volunteer.MainActivity;
import javax.annotation.Nullable;

public class SplashScreen extends AppCompatActivity {

    ImageView imageView;
    FirebaseAuth fAuth;
    String userId;
    FirebaseFirestore fStore;
    String usertype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        imageView = (ImageView)findViewById(R.id.imageView2);
        fAuth = FirebaseAuth.getInstance();
        FirebaseUser user =fAuth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (user == null)
                {
                    Intent intent =  new Intent(SplashScreen.this, Registeration.class);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    fAuth = FirebaseAuth.getInstance();
                    userId=fAuth.getCurrentUser().getUid();
                    fStore = FirebaseFirestore.getInstance();
                    DocumentReference documentReference = fStore.collection("users").document(userId);
                    documentReference.addSnapshotListener((documentSnapshot, e) -> {
                        if (e==null)
                        {
                            if(documentSnapshot.exists())
                            {
                                if(documentSnapshot.exists()){
                                    usertype = documentSnapshot.getString("type");
                                }else {
                                    Log.d("tag", "onEvent: Document do not exists");
                                }
                            }
                            if (usertype.equals("0"))
                            {
                                Intent intent =  new Intent(SplashScreen.this, com.example.blind_wallet.MainActivity.class);
                                startActivity(intent);
                            }
                            else if(usertype.equals("1"))
                            {
                                Intent intent =  new Intent(SplashScreen.this, Main3Activity.class);
                                startActivity(intent);
                            }
                            else
                            {
                                Intent intent =  new Intent(SplashScreen.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }
                    });

                    finish();
                }

            }
        },4000);
    }
}
