package com.example.blind_wallet.Volunteer.Notifications;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.blind_wallet.R;
import com.example.blind_wallet.Volunteer.Notifications.SendNotificationPack.APIService;
import com.example.blind_wallet.Volunteer.Notifications.SendNotificationPack.Client;
import com.example.blind_wallet.Volunteer.Notifications.SendNotificationPack.Data;
import com.example.blind_wallet.Volunteer.Notifications.SendNotificationPack.MyResponse;
import com.example.blind_wallet.Volunteer.Notifications.SendNotificationPack.NotificationSender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddBlindFragment extends Fragment {

    TextView email;
    Button button;
    FirebaseFirestore firestore;
    public static final String TAG = AddBlindFragment.class.getName();
    String Gmail;
    DocumentReference documentReference;
    private APIService apiService;
    String statues = "false";
    Boolean emailexist = false;
    Boolean request = false;
    String message ;
    public AddBlindFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_add_blind, container, false);
        email = (TextView)view.findViewById(R.id.email);
        firestore = FirebaseFirestore.getInstance();
        button = (Button)view.findViewById(R.id.sendnot);
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userID = auth.getCurrentUser().getUid();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (email.getText().toString().equals(""))
                {
                    Toast.makeText(getContext(), "Enter The Email Of Blind...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    FirebaseDatabase.getInstance("https://blind-wallet-be409-default-rtdb.firebaseio.com/").getReference().child("BlindsToken").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot postSnapshot:dataSnapshot.getChildren()) {
                                UserToken userToken = new UserToken();
                                userToken = postSnapshot.getValue(UserToken.class);
                                if (email.getText().toString().equals(userToken.email)) {
                                    emailexist = true;
                                }
                            }
                            if (emailexist == true)
                            {
                                FirebaseFirestore.getInstance().collection("RelationShips").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        for (QueryDocumentSnapshot snapshot:task.getResult())
                                        {
                                            if(snapshot.getString("Blind Emali").equals(email.getText().toString()))
                                            {
                                                if (snapshot.getString("statues").equals("true"))
                                                {
                                                    statues = "true";
                                                    message = "Sorry The Blind have a Volunteer";
                                                }
                                                else
                                                {
                                                    request = true;
                                                    message = "Please Send in Another Time";
                                                }
                                            }
                                        }
                                        if (statues == "false" && request == false)
                                        {
                                            FirebaseDatabase.getInstance("https://blind-wallet-be409-default-rtdb.firebaseio.com/").getReference().child("BlindsToken").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                                                    {
                                                        UserToken userToken = new UserToken();
                                                        userToken = postSnapshot.getValue(UserToken.class);
                                                        if(email.getText().toString().equals(userToken.email))
                                                        {
                                                            sendNotifications(userToken.token,"Add Friend","You Want To Add Volunteer");
                                                            //Get Gmail For Volunteer
                                                            documentReference = fStore.collection("users").document(userID);
                                                            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                                                    if (e==null )
                                                                    {
                                                                        if(documentSnapshot.exists()){
                                                                            Gmail = documentSnapshot.getString("email");
                                                                        }
                                                                    }
                                                                    documentReference = fStore.collection("RelationShip").document(Gmail);
                                                                    Map<String,Object> user = new HashMap<>();
                                                                    user.put("Volunteer Email",Gmail);
                                                                    user.put("Blind Email",email.getText().toString());
                                                                    user.put("Statuse","false");
                                                                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Log.d(TAG, "onSuccess: user Profile is created for "+ userID);
                                                                            Toast.makeText(requireContext(), "Request send successfully", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.d(TAG, "onFailure: " + e.toString());
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
//
                                        }
                                        else
                                        {
                                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                Toast.makeText(getContext(), "Email Not Exist...", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    
                }
            }
        });
        return view;
    }
    public void sendNotifications(String usertoken, String title, String message) {
        Data data = new Data(title, message);
        NotificationSender sender = new NotificationSender(data, usertoken);
        apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(retrofit2.Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    if (response.body().success != 1) {
                        Toast.makeText(getContext(), "Failed ", Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }

}
class UserToken {
    public String email;
    public String token;
    public UserToken(String e, String t)
    {
        this.email = e;
        this.token = t;
    }
    public UserToken(){}
}