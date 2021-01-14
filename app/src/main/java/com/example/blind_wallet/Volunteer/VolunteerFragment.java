package com.example.blind_wallet.Volunteer;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.blind_wallet.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class VolunteerFragment extends Fragment {

    FirebaseAuth fAuth;
    String userId;
    ArrayList<Person> persons;
    View view;
    StorageReference storageReference;
    CircleImageView circleImageView;
    Person person;
    public VolunteerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_volunteer, container, false);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("type","1").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    persons = new ArrayList<Person>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        person = new Person();
                        person.fName = (String) document.getString("fName");
                        person.address =(String)  document.getString("address");
                        person.phone = (String) document.getString("phone");
                        person.email = (String) document.getString("email");
                        person.id = document.getId();
                        persons.add(person);
                    }
                    Volunteer voulenteer = new Volunteer(persons);
                    ListView ls = (ListView)view.findViewById(R.id.listview);
                    ls.setAdapter(voulenteer);
                } else {
                    Log.d("MissionActivity", "Error getting documents: ", task.getException());
                }
            }
        });

        return view;
    }
    class Volunteer extends BaseAdapter{
        ArrayList<Person> people = new ArrayList<>();
        Volunteer(ArrayList<Person> s){
            this.people = s;
        }
        @Override
        public int getCount() {
            return people.size();
        }

        @Override
        public Object getItem(int position) {
            return people.get(position).fName;
        }

        @Override
        public long getItemId(int position) {
            return people.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.home_admin,null);
            TextView textView = view.findViewById(R.id.txt);
            Button b = view.findViewById(R.id.btn);
            textView.setText(people.get(position).fName);
            circleImageView = view.findViewById(R.id.circleimage1);
            storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference profileRef=storageReference.child("users/"+people.get(position).id+"/profile.jpg");
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(circleImageView);
                }
            });
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext() , Details.class);
                    Bundle b = new Bundle();
                    b.putString("email",people.get(position).email);
                    b.putString("fname",people.get(position).fName);
                    b.putString("address",people.get(position).address);
                    b.putString("phone",people.get(position).phone);
                    b.putString("image",people.get(position).id);
                    intent.putExtras(b);
                    startActivity(intent);
                }

            });
            return view;
        }
    }

}