package Classes;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Speech  {

    TextToSpeech textToSpeech;
    public void Texttospeech(Context context, final String text)
    {

        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                if(status == TextToSpeech.SUCCESS)
                {
                    int result = textToSpeech.setLanguage(new Locale("en", "IN"));
                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        Log.e("TEXTTOSPEECH", "LANGUAGE NOT SUPPORTED...");
                    }
                    else
                    {
                        textToSpeech.setPitch(1.0f);
                        textToSpeech.setSpeechRate(0.5f);
                        if(Build.VERSION.SDK_INT >= 21)
                        {
                            textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
                        }
                        else
                        {
                            textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null);
                        }
                    }
                }
                else
                {
                    Log.e("Message!","Initialization Failed...");
                }
                //textToSpeech.shutdown();
            }
        });
    }

    public void Speechtotext(final Context context, final Intent intent)
    {
        final SpeechRecognizer recognizer = SpeechRecognizer.createSpeechRecognizer(context);
        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                recognizer.startListening(intent);
            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                List<String> strings = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String command = strings.get(0);
                command = command.toLowerCase();
                if(command.indexOf("whta") != -1)
                    if(command.indexOf("time") != -1)
                    {
                        Date now = new Date();
                        String time = DateUtils.formatDateTime(context,now.getTime(),DateUtils.FORMAT_SHOW_TIME);
                        Texttospeech(context,time);
                    }
            }
            @Override
            public void onPartialResults(Bundle partialResults) {

            }
            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });
    }
}
//Get My Location Now
/*
package com.example.blind_wallet;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import Classes.Speech;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback{

    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    String address;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        fetchlastlocation();
    }

    private void fetchlastlocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION} ,REQUEST_CODE);
            return;
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null)
                {
                    currentLocation = location;
                    String c = getCompleteAddressString(currentLocation.getLatitude(),currentLocation.getLongitude());
                    if(!c.equals(""))
                    {
                        Toast.makeText(getApplicationContext(),c,Toast.LENGTH_LONG).show();
                    }
                    SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.google_map)  ;
                    mapFragment.getMapAsync(MainActivity.this);
                }
            }
        });

    }
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE)
    {
        Geocoder geocoder;
        List<Address> addresses ;
        geocoder = new Geocoder(this, Locale.getDefault());

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
        LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I Am Here..");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,5));
        googleMap.addMarker(markerOptions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_CODE:
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    fetchlastlocation();
                }
                break;
        }
    }

    public void currentlocation(View view) {
        Speech speech = new Speech();
        speech.Texttospeech(this,address);

    }
}
*/
/*<fragment
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/google_map"
        android:name="com.google.android.gms.maps.SupportMapFragment"
        />*/

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Speech To Text
/*package com.example.blind_wallet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.circularreveal.CircularRevealHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Classes.Speech;


public class MainActivity extends AppCompatActivity {

    String command;
    SpeechRecognizer recognizer;
    Speech s;
    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        runnable.run();
    }

    public void Start()
    {
        s = new Speech();
        s.Texttospeech(getApplicationContext(), "How can I help you.");
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10000000);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech to text");
        startActivityForResult(intent, 14);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 14 && resultCode == RESULT_OK && data != null)
        {
            ArrayList<String> strings = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            command = strings.get(0);
            if(command.contains("your name"))
            {
                Speech s = new Speech();
                s.Texttospeech(getApplicationContext(),"Mina");
            }
            else if(command.contains("time now"))
            {
                Date date = new Date();
                String time = DateUtils.formatDateTime(getApplicationContext(),date.getTime(),DateUtils.FORMAT_SHOW_TIME);
                Speech s = new Speech();
                s.Texttospeech(getApplicationContext(),"Time Now is "+time);
            }
            else if(command.equals("thank you"))
            {
                s = new Speech();
                s.Texttospeech(getApplicationContext(),"Thank you for using the service. Good By");
                handler.removeCallbacks(runnable);
            }
        }
    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Start();
            handler.postDelayed(this,10000);
        }
    };*/

///////////////////////////////////////////////////////////////////////////
//Check if location is turn on or not
/* public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }*/