package com.example.blind_wallet.Volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.blind_wallet.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailActivation extends AppCompatActivity {

    Button resendCode;
    FirebaseUser user;
    FirebaseAuth fAuth;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (user.isEmailVerified())
                    {
                        startActivity(new Intent(EmailActivation.this, SelectPhoto.class));
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
