package com.example.blind_wallet.Volunteer;


import android.content.Intent;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class BlindFragment extends Fragment {

    FirebaseAuth fAuth;
    String userId;
    ArrayList<Person> persons;
    View view;
    StorageReference storageReference;
    CircleImageView circleImageView;
    public BlindFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_blind, container, false);
        persons = new ArrayList<Person>();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        db.collection("users").whereEqualTo("type","0").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> list = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Person person = new Person();
                        document.getId();
                        person.fName = (String) document.getString("fName");
                        person.address =(String)  document.getString("address");
                        person.phone = (String) document.getString("phone");
                        person.email = (String) document.getString("email");
                        persons.add(person);
                    }
                    Blind blind = new Blind(persons);
                    ListView ls = (ListView)view.findViewById(R.id.listview);
                    ls.setAdapter(blind);
                } else {
                    Log.d("MissionActivity", "Error getting documents: ", task.getException());
                }
            }
        });

        return view;
    }
    class Blind extends BaseAdapter{
        ArrayList<Person> people = new ArrayList<>();
        Blind(ArrayList<Person> s){
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
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.home_admin,null);
            TextView textView = view.findViewById(R.id.txt);
            Button b = view.findViewById(R.id.btn);
            circleImageView = view.findViewById(R.id.circleimage1);
            textView.setText(persons.get(position).fName);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext() , Details.class);
                    Bundle b = new Bundle();
                    b.putString("email",persons.get(position).email);
                    b.putString("fname",persons.get(position).fName);
                    b.putString("address",persons.get(position).address);
                    b.putString("phone",persons.get(position).phone);
                    intent.putExtras(b);
                    startActivity(intent);
                }

            });
            return view;
        }
    }
}

class Person
{
    public String fName;
    public String email;
    public String address;
    public String phone;
    public String type;
    public String id;
    Person(){

    }
    Person(String FirstName , String Email , String Address , String Phone , String Type,String u)
    {
        this.fName = FirstName;
        this.email = Email;
        this.address = Address;
        this.phone = Phone;
        this.type = Type;
        this.id = u;
    }



}

