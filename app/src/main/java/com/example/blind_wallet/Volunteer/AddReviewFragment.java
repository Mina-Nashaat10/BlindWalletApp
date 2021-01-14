package com.example.blind_wallet.Volunteer;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.blind_wallet.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddReviewFragment extends Fragment {

    TextView rateCount, showRating;
    EditText review;
    Button submit;
    RatingBar ratingBar;
    float rateValue;
    String temp;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public AddReviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_review, container, false);
        rateCount = view.findViewById(R.id.rateCount);
        ratingBar = view.findViewById(R.id.ratingBar);
        review = view.findViewById(R.id.review);
        submit = view.findViewById(R.id.submitBtn);
        showRating = view.findViewById(R.id.showRating);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {

                rateValue = ratingBar.getRating();
                if(rateValue<=1 && rateValue>0)
                    rateCount.setText("Bad" + rateValue);
                else if(rateValue<=2 && rateValue>1)
                    rateCount.setText("ok" + rateValue);
                else if(rateValue<=3 && rateValue>2)
                    rateCount.setText("good" + rateValue);
                else if(rateValue<=4 && rateValue>3)
                    rateCount.setText("very good" + rateValue);
                else if(rateValue<=5 && rateValue>4)
                    rateCount.setText("excellent" + rateValue);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                temp = rateCount.getText().toString();
                showRating.setText("Your Rating Is: " + temp + "\n" + review.getText());
                Map<String,Object> newreview = new HashMap<>();
                newreview.put("rate",temp);
                newreview.put("comment",review.getText().toString());
                db.collection("Reviews").add(newreview)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(getContext(), "Added Successfully", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Cannot Add Review", Toast.LENGTH_SHORT).show();
                            }
                        });
                ratingBar.setRating(0);
                rateCount.setText("");
            }
        });
        return view;
    }

}
