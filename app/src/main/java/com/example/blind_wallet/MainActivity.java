package com.example.blind_wallet;

import android.Manifest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blind_wallet.Volunteer.TrackingSystem.Model.Tracking;
import com.example.blind_wallet.Volunteer.TrackingSystem.Model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Classes.Speech;


public class MainActivity extends AppCompatActivity implements com.google.android.gms.location.LocationListener,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //track system

    DatabaseReference onlineRef,currentUserRef,counterRef,locations;



    private static final int MY_PERMISSION_REQUEST_CODE=7171;
    private static final int PLAY_SERVICES_RES_REQUEST=7172;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private static int  UPDATE_INTERVAL = 5000;
    private static int  FASTEST_INTERVAL = 3000;
    private static int  DISTANCE = 10;

    String volunteeremail= null ;







    private static final int REQUEST_CODE_LOCATION = 1;
    private static final int REQUEST_CODE_VOICE = 3;
    private static final int REQUEST_CODE_VOICE_Requests = 7;
    private static final int REQUEST_CODE_LOCATION_IMAGE = 4;
    private static final int REQUEST_CODE_CALL_PHONE = 5;
    private static final int CODE_CALL_PHONE = 10;
    private static final int REQUEST_CODE_ENABLE_LOCATION = 6;

    Context context;
    String username;
    FirebaseFirestore fStore;
    String userId;
    FirebaseAuth fAuth;
    String command;
    Speech s;
    boolean isavailable;
    private Handler handler;
    private Handler handlervolunteer;
    TextView textView;
    String text;
    ArrayList<String> textcolorformat;
    String VGmail;
    String BGmail;
    FirebaseUser user;
    Boolean request = false;
    Boolean statues = false;
    String volunteername;
    String volunteerphone;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.txt);
        context = this;
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        textView.setMovementMethod(new ScrollingMovementMethod());
        isavailable = false;
        textcolorformat = new ArrayList<>();
        Bundle b = getIntent().getExtras();
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
        if(b != null)
        {
            textcolorformat = b.getStringArrayList("returnedText");
            textView.setText("");
            for (String n:textcolorformat) {
                textView.append(Html.fromHtml(n));
                textView.append("\n");
            }
        }
        else
        {
            text = "<font color=#2874A6>BlindWallet@</font><font color = #B7950B>USER</font><font color = #5F6A6A> >> </font><font color=#F0F3F4>Start Application</font>";
            textcolorformat.add(text);
            textView.setText(Html.fromHtml(text));
            textView.append("\n");
        }

        //Get Gmail For Current Blind
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userID = auth.getCurrentUser().getUid();
        documentReference = fStore.collection("users").document(userID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e==null )
                {
                    if(documentSnapshot.exists()){
                        BGmail = documentSnapshot.getString("email");
                    }
                }
            }
        });
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_IMAGE);
            return;
        }
        //tracking system

        locations = FirebaseDatabase.getInstance("https://blind-wallet-be409-default-rtdb.firebaseio.com/").getReference("Locations");
        onlineRef = FirebaseDatabase.getInstance("https://blind-wallet-be409-default-rtdb.firebaseio.com/").getReference().child(".info/connected");
        counterRef = FirebaseDatabase.getInstance("https://blind-wallet-be409-default-rtdb.firebaseio.com/").getReference("lastOnline");
        currentUserRef = FirebaseDatabase.getInstance("https://blind-wallet-be409-default-rtdb.firebaseio.com/").getReference("lastOnline")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSION_REQUEST_CODE);
        }
        else{
            buildGoogleApiClient();
            createLocationRequest();
            displayLocation();
        }

        setupSystem();
        handlervolunteer = new Handler();
        runnablevolunteer.run();
        handler = new Handler();














    }

    ArrayList<String> strings;

    private long backPressedTime;
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis())
        {
            super.onBackPressed();
            finish();
            return;
        }
        else {
            s = new Speech();
            s.Texttospeech(this,"Please Press Again To Exit...");
        }
        backPressedTime = System.currentTimeMillis();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION_IMAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            handler = new Handler();
            runnable.run();

            return;
        }
        else if(requestCode == REQUEST_CODE_CALL_PHONE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            CallPhone();
        }

        else{

            //tracking sytem
            switch(requestCode)
            {
                case MY_PERMISSION_REQUEST_CODE:
                {
                    if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                        if(checkPlayServices()){
                            buildGoogleApiClient();
                            createLocationRequest();
                            displayLocation();
                        }
                    }
                }
                break;
            }
        }
    }
    /////////////////////

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    public void Start() {
        s = new Speech();
        s.Texttospeech(getApplicationContext(), "How can I help you.");
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-US");
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ar-JO");
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 50000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10000000);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech to text");
        startActivityForResult(intent, REQUEST_CODE_VOICE);
    }
    public void AddVolunteer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-US");
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ar-JO");
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 500000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10000000);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech to text");
        startActivityForResult(intent, REQUEST_CODE_VOICE_Requests);
    }
    public void CheckAddVolunteer()
    {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("RelationShip").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult())
                {
                    if(document.getString("Blind Email").equals(BGmail) && document.getString("Statuse").equals("false"))
                    {
                        handler.removeCallbacks(runnable);
                        request = true;
                        VGmail = (String) document.getString("Volunteer Email");
                        Speech speech = new Speech();
                        speech.Texttospeech(getApplicationContext(),"You Have A Request From "+VGmail);
                        AddVolunteer();
                    }
                }
                if (request == false)
                {
                    handlervolunteer.removeCallbacks(runnablevolunteer);
                    runnable.run();
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_VOICE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> strings = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            command = strings.get(0);
            if (command.contains("time") || command.contains("date")) {
                // Get Current Date
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);      // 0 to 11
                int day = cal.get(Calendar.DAY_OF_MONTH);
                DateFormatSymbols dfs = new DateFormatSymbols();
                String[] months = dfs.getMonths();
                String m = months[month];
                //Get Current Time
                Date date = new Date();
                String strDateFormat = "hh:mm:ss a";
                DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
                String formattedDate= dateFormat.format(date);
                Speech s = new Speech();
                s.Texttospeech(this, "Date "+ day + m + String.valueOf(year) + "Time"+formattedDate);
                text = "<font color=#2874A6>BlindWallet@</font><font color = #B7950B>"+username+"</font><font color = #5F6A6A> >> </font><font color=#F0F3F4>Get Date and Time Now</font>";
                textView.append(Html.fromHtml(text));
                textcolorformat.add(text);
                textView.append("\n");
                text = "<font color=#B9770E> Date : "+ day + m + String.valueOf(year) +"</font>"+"  "+ "<font color=#A04000>   Time : "+formattedDate+"</font>";
                textView.append(Html.fromHtml(text));
                textView.append("\n");
                textcolorformat.add(text);
            }
            else if (command.contains("location") || command.contains("address")) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
                    return;
                }
                else if(!isLocationEnabled(getApplicationContext()))
                {
                    displayLocationSettingsRequest(getApplicationContext());
                }
                else
                {
                    Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
                    Bundle b = new Bundle();
                    b.putStringArrayList("TEXTVIEW_KEY",textcolorformat);
                    intent.putExtras(b);
                    startActivity(intent);
                    handler.removeCallbacks(runnable);
                }
            }
            else if (command.contains("camera") || command.contains("take photo")) {
                Intent myintent = new Intent(this,ClassificationActivity.class);
                Bundle b = new Bundle();
                b.putStringArrayList("TEXTVIEW_KEY",textcolorformat);
                myintent.putExtras(b);
                startActivity(myintent);
                handler.removeCallbacks(runnable);
            }
            else if (command.contains("phone") || command.contains("call"))
            {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CODE_CALL_PHONE);
                    return;
                }
                else
                {
                    CallPhone();
                }
            }
            else if (command.contains("thank you")) {
                s = new Speech();
                s.Texttospeech(getApplicationContext(), "Thank you for using the service. Good By");
                handler.removeCallbacks(runnable);
            }
            else {
                s = new Speech();
                s.Texttospeech(getApplicationContext(), "try again");
            }
        }
        else if(requestCode == CODE_CALL_PHONE)
        {
            String blindemail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            handler.removeCallbacks(runnable);
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("RelationShip").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            if (snapshot.getString("Blind Email").equals(blindemail) && snapshot.getString("Statuse").equals("true")) {
                                volunteeremail = snapshot.getString("Volunteer Email");
                            }
                        }
                        if (volunteeremail == null) {
                            Speech speech = new Speech();
                            speech.Texttospeech(getApplicationContext(), "No Found any Volunteer");
                            runnable.run();
                        } else {
                            firestore.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                            if (snapshot.getString("email").equals(volunteeremail)) {
                                                volunteername = snapshot.getString("fName");
                                                volunteerphone = snapshot.getString("phone");
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            });
            text = "<font color=#2874A6>BlindWallet@</font><font color = #B7950B>"+username+"</font><font color = #5F6A6A> >> </font><font color=#F0F3F4>Calling Phone</font>";
            textView.append(Html.fromHtml(text));
            textView.append("\n");
            textcolorformat.add(text);
            text = "<font color=#A04000>Call To "+volunteername+" : "+volunteerphone+"</font>";
            textView.append(Html.fromHtml(text));
            textView.append("\n");
            textcolorformat.add(text);
            runnable.run();
        }
        else if(requestCode == REQUEST_CODE_ENABLE_LOCATION)
        {
            if(isLocationEnabled(getApplicationContext()))
            {
                new Intent(this,MainActivity.class);
            }
        }
        else if(requestCode == REQUEST_CODE_VOICE_Requests && resultCode == RESULT_OK && data != null)
        {
            ArrayList<String> strings = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            command = strings.get(0);
            if (command.contains("accept"))
            {
                handlervolunteer.removeCallbacks(runnablevolunteer);
                fStore = FirebaseFirestore.getInstance();
                    user = fAuth.getCurrentUser();
                    user.updateEmail(BGmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DocumentReference docRef = fStore.collection("RelationShip").document(VGmail);
                        Map<String,Object> edited = new HashMap<>();
                        edited.put("Blind Email",BGmail);
                        edited.put("Volunteer Email",VGmail);
                        edited.put("Statuse","true");
                        docRef.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Speech speech = new Speech();
                                speech.Texttospeech(getApplicationContext(),"Volunteer Added");
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
                    handlervolunteer.removeCallbacks(runnablevolunteer);
                    runnable.run();
            }
            else if(command.contains("not accept"))
            {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                DocumentReference deletedoc = firestore.collection("RelationShips").document(VGmail);
                deletedoc.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Speech speech = new Speech();
                        speech.Texttospeech(getApplicationContext(),"Request Deleted..");
                    }
                });
                handlervolunteer.removeCallbacks(runnablevolunteer);
                runnable.run();
            }
        }
    }
    int firststep = 0;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(isOnline())
            {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_IMAGE);
                    return;
                }
                Start();
                handler = new Handler();
                handler.postDelayed(this, 12000);
            }
            else
            {
                if(firststep == 0)
                {
                    s = new Speech();
                    s.Texttospeech(getApplicationContext(),"Please Turn on Wifi or Mobile Network.....");
                }
                firststep++;
                handler.postDelayed(this, 10000);
            }
        }
    };

    private Runnable runnablevolunteer = new Runnable() {
        @Override
        public void run() {
            statues = false;
            FirebaseFirestore.getInstance().collection("RelationShips").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    for (QueryDocumentSnapshot snapshot:task.getResult())
                    {
                        if (snapshot.getString("Blind Email").equals(BGmail))
                        {
                            String s = snapshot.getString("Statuse");
                            if (s.equals("true"))
                            {
                                statues = true;
                            }
                            else
                            {
                                statues = false;
                            }
                        }
                    }
                    if(statues==true)
                    {
                        handlervolunteer.removeCallbacks(runnablevolunteer);
                        runnable.run();
                    }
                    else
                    {
                        CheckAddVolunteer();
                    }
                }
            });
            handlervolunteer.postDelayed(this,30000);
        }
    };
    // Check Location is enable and if is not enable will displayLocationSettingsRequest
    //****************
    public static Boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // This is new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            // This is Deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }
    private void displayLocationSettingsRequest(Context context) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, locationSettingsResponse -> {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
        });
        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MainActivity.this,
                            REQUEST_CODE_ENABLE_LOCATION);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }
    //****************


    //Check Internet Connection
    //**************************
    public boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isConnected())
        {
            Toast.makeText(MainActivity.this, "Internet Available",
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
    //**************************


    // To Perform Call Phone
    //**********
    private void CallPhone()
    {
        handler.removeCallbacks(runnable);
        String blindemail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        handler.removeCallbacks(runnable);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("RelationShip").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
         @Override
         public void onComplete(@NonNull Task<QuerySnapshot> task) {
             if (task.isSuccessful()) {
                 for (QueryDocumentSnapshot snapshot : task.getResult()) {
                     if (snapshot.getString("Blind Email").equals(blindemail) && snapshot.getString("Statuse").equals("true")) {
                         volunteeremail = snapshot.getString("Volunteer Email");
                     }
                 }
                 if (volunteeremail == null) {
                     Speech speech = new Speech();
                     speech.Texttospeech(getApplicationContext(), "No Found any Volunteer");
                     runnable.run();
                 } else {
                     firestore.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                         @Override
                         public void onComplete(@NonNull Task<QuerySnapshot> task) {
                             if (task.isSuccessful()) {
                                 for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                     if (snapshot.getString("email").equals(volunteeremail)) {
                                         volunteername = snapshot.getString("fName");
                                         volunteerphone = snapshot.getString("phone");
                                     }
                                 }
                                 String num = "tel:"+volunteerphone;
                                 if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                     ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CALL_PHONE},REQUEST_CODE_CALL_PHONE);
                                     return;
                                 }
                                 //startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(num)));
                                 startActivityForResult(new Intent(Intent.ACTION_CALL, Uri.parse(num)),CODE_CALL_PHONE);
                             }
                         }
                     });
                 }
             }
         }
     });

    }



    //tracking system


    @SuppressLint("RestrictedApi")
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISTANCE);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }




    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mLastLocation = location;
            }
        });
        if(mLastLocation != null){
            locations.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(new Tracking(FirebaseAuth.getInstance().getCurrentUser().getEmail()
                            ,FirebaseAuth.getInstance().getCurrentUser().getUid()
                            ,String.valueOf(mLastLocation.getLatitude())
                            ,String.valueOf(mLastLocation.getLongitude())));
        }
    }



    private void setupSystem() {
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue(Boolean.class))
                {
                    currentUserRef.onDisconnect().removeValue();
                    counterRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(new User(FirebaseAuth.getInstance().getCurrentUser().getEmail(),"Online"));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        counterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot postSnapshot:snapshot.getChildren()){
                    User user = postSnapshot.getValue(User.class);
                    Log.d("LOG",""+user.getEmail()+"is"+user.getStatus());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RES_REQUEST).show();
            }
            else{
                Toast.makeText(this, "This Device Is Not Supported", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest, (com.google.android.gms.location.LocationListener) this);
    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();

    }
    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if(mGoogleApiClient != null){
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}



















































   /* File image_file;
    private File get_image_file()throws IOException
    {
        String timestamps=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imageName="jpg"+timestamps+"_";

        File StorageDir=getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile=File.createTempFile(imageName,"jpg",StorageDir);

        currentImagePath=imageFile.getAbsolutePath();

        return imageFile;
    }*/
/*
    public void display()
    {
        Bitmap bitmap= BitmapFactory.decodeFile(currentImagePath);
        imageView.setImageBitmap(bitmap);

        //Intent intent=new Intent(this,Display.class);
        //intent.putExtra("image_path",currentImagePath);
        //startActivity(intent);
    }*/
    /*
    public boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        }
        else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }
     public boolean statusCheck() {
            final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps();
                return true;
            }
            return false;
        }

        private void buildAlertMessageNoGps() {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
            builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }

    */
    /*
    Intent image_take=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(image_take.resolveActivity(getPackageManager())!=null)
                {
                    image_file =null;
                    try {
                        image_file = get_image_file();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    if(image_file!=null)
                    {
                        Uri imageUri= FileProvider.getUriForFile(this,"com.example.blind_wallet.fileprovider",image_file);
                        image_take.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                        startActivityForResult(image_take,Request_image);
                    }
                }
     */