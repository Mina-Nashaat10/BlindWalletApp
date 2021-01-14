package com.example.blind_wallet;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;

public class Registeration extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText mFullName,mEmail,mPassword,mPhone,maddress;
    Button mRegisterBtn;
    TextView mLoginBtn;

    //it for register the user
    FirebaseAuth fAuth;

    ProgressBar progressBar;
    FirebaseFirestore fStore;
    String userID;
    Spinner spinner;
    int usertype = 0;
    String itemselect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);
        fAuth = FirebaseAuth.getInstance();
        mFullName   = findViewById(R.id.fullName);
        maddress    = findViewById(R.id.Address);
        mEmail      = findViewById(R.id.Email);
        mPassword   = findViewById(R.id.password);
        mPhone      = findViewById(R.id.phone);
        mRegisterBtn= findViewById(R.id.registerBtn);
        mLoginBtn   = findViewById(R.id.createText);
        spinner = (Spinner)findViewById(R.id.spinner1);
        String[] users = new String[] {"Blind Man","Volunteer"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, users);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //here we get the current instance of database from the firebase so we could perfom
        //various operation on database
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        progressBar = findViewById(R.id.progressBar);

        //it for check if the user login or not, if it login it will go to the MainActivity
        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                itemselect = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString().trim();
                final String address=maddress.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                final String fullName = mFullName.getText().toString();
                final String phone    = mPhone.getText().toString();

                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required.");
                    return;
                }

                if(TextUtils.isEmpty(address)){
                    mEmail.setError("Adress is Required.");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is Required.");
                    return;
                }

                if(password.length() < 6){
                    mPassword.setError("Password Must be >= 6 Characters");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                if(itemselect.equals("Blind Man"))
                {
                    usertype = 0;
                }
                else
                {
                    usertype = 1;
                }
                // register the user in firebase

                //(fAuth) firebase object instanse , .addOnCompleteListener:- it for know
                // the register is succesful or not
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            // send verification link
                            //fuser is firebase user object
                            FirebaseUser fuser = fAuth.getCurrentUser();
                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Registeration.this, "Verification Email Has been Sent.", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: Email not sent " + e.getMessage());
                                }
                            });

                            Toast.makeText(Registeration.this, "User Created.", Toast.LENGTH_SHORT).show();
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("fName",fullName);
                            user.put("address",address);
                            user.put("email",email);
                            user.put("phone",phone);
                            user.put("type",String.valueOf(usertype));
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: user Profile is created for "+ userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.toString());
                                }
                            });
                            Intent intent = new Intent(getApplicationContext(),EmailActivation.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("usertype",String.valueOf(usertype));
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }else {
                            Toast.makeText(Registeration.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                        if (usertype ==0)
                        {
                            FirebaseInstanceId.getInstance().getInstanceId()
                                    .addOnCompleteListener(task1 -> {
                                        if(task1.isSuccessful())
                                        {
                                            String token= task1.getResult().getToken();
                                            save_token(token);
                                        }else{

                                        }
                                    });
                        }

                    }
                });
            }
        });
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
            }
        });
    }
    private  void save_token(String token)
    {

        String email=mEmail.getText().toString();
        UserToken us = new UserToken(email,token);
        fAuth = FirebaseAuth.getInstance();
        DatabaseReference dbUsers= FirebaseDatabase.getInstance("https://blind-wallet-be409-default-rtdb.firebaseio.com/").getReference("BlindsToken");
        dbUsers.child(fAuth.getCurrentUser().getUid())
                .setValue(us).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(getApplicationContext(),"TOKEN SAVED",Toast.LENGTH_LONG).show();
                    }
                });
    }
    public static class UserToken {
        public String email;
        public String token;
        public UserToken(String e, String t)
        {
            this.email = e;
            this.token = t;
        }
        public UserToken(){}
    }

}
