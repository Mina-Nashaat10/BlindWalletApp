package com.example.blind_wallet.Volunteer;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.blind_wallet.R;
import com.example.blind_wallet.Registeration;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddAdminFragment extends Fragment {

    EditText fullname;
    EditText address;
    EditText email;
    EditText password;
    EditText phone;
    Button addadmin;
    FirebaseFirestore firestore;
    FirebaseAuth fAuth;
    String userID;
    public AddAdminFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_admin, container, false);
        fullname = view.findViewById(R.id.fullname);
        address = view.findViewById(R.id.address);
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        phone = view.findViewById(R.id.phone);
        addadmin = view.findViewById(R.id.addadmin);
        fAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        addadmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fullname.getText().toString().equals("")) {
                    fullname.setError("Enter Full Name");
                } else if (address.getText().toString().equals("")) {
                    address.setError("Enter Address");
                } else if (email.getText().toString().equals("")) {
                    email.setError("Enter Email");
                } else if (password.getText().toString().equals("")) {
                    password.setError("Enter Pass");
                } else if (password.getText().toString().length() < 6) {
                    password.setError("Pass must be grater than 6 character");
                } else if (phone.getText().toString().equals("")) {
                    phone.setError("Enter Phone");
                } else if (phone.getText().toString().length() < 11) {
                    password.setError("Phone must be 11 Number");
                }
                else
                {
                    fAuth.createUserWithEmailAndPassword(email.getText().toString().trim(),password.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                Map<String,Object> admin = new HashMap<>();
                                admin.put("fullname",fullname.getText().toString());
                                admin.put("address",address.getText().toString());
                                admin.put("email",email.getText().toString());
                                admin.put("password",password.getText().toString());
                                admin.put("phone",phone.getText().toString());
                                admin.put("type","2");
                                userID = fAuth.getCurrentUser().getUid();
                                DocumentReference reference = firestore.collection("users").document(userID);
                                reference.set(admin).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getContext(), "Admin Added", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), "Cannot add Admin", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                fullname.setText("");
                                address.setText("");
                                email.setText("");
                                password.setText("");
                                phone.setText("");
                            }
                            else {
                                Toast.makeText(getContext(), "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        return view;
    }
}