package com.example.blind_wallet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.blind_wallet.Volunteer.VolunteerFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.example.blind_wallet.Volunteer.* ;
public class EmailActivation extends AppCompatActivity {

    Button resendCode;
    FirebaseUser user;
    FirebaseAuth fAuth;
    String username;
    FirebaseFirestore fStore;
    String userId;
    Intent intent;
    String usertype;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (user.isEmailVerified())
                    {

                        if(usertype.equals("0"))
                        {
                            intent = new Intent(EmailActivation.this, MainActivity.class);
                        }
                        else {
                            intent = new Intent(EmailActivation.this,SelectPhoto.class);
                        }
                        startActivity(intent);
                        handler.removeCallbacks(runnable);
                    }
                    else {
                        Toast.makeText(EmailActivation.this, "Please Verify your Email...", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            handler.postDelayed(this,5000);
        }
    };
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_activation);
        Bundle  bundle = getIntent().getExtras();
        usertype = bundle.getString("usertype");
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userId=fAuth.getCurrentUser().getUid();
        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    username =documentSnapshot.getString("fName");
                }else {
                    Log.d("tag", "onEvent: Document do not exists");
                }
            }
        });
        user = FirebaseAuth.getInstance().getCurrentUser();
        handler = new Handler();
        runnable.run();
        setContentView(R.layout.activity_email_activation);
        resendCode = findViewById(R.id.resendCode2);
        resendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendemail();
            }
        });
    }
    public void sendemail()
    {
        FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(EmailActivation.this,
                            "Verification email sent to " + user.getEmail(),
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(EmailActivation.this,
                            "Failed to send verification email.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
