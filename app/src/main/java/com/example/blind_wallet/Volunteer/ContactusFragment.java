package com.example.blind_wallet.Volunteer;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.blind_wallet.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactusFragment extends Fragment {


    public ContactusFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_contactus, container, false);
        final EditText your_name = (EditText) view.findViewById(R.id.your_name);
        final EditText your_email = (EditText)view.findViewById(R.id.your_email);
        final EditText your_subject = (EditText) view.findViewById(R.id.your_subject);
        final EditText your_message = (EditText)view.findViewById(R.id.your_message);
        Button email = (Button) view.findViewById(R.id.post_message);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = your_name.getText().toString();
                String email = your_email.getText().toString();
                String subject = your_subject.getText().toString();
                String message = your_message.getText().toString();

                if (TextUtils.isEmpty(name)){
                    your_name.setError("Enter Your Name");
                    your_name.requestFocus();
                    return;
                }

                Boolean onError = false;
                if (!isValidEmail(email)) {
                    onError = true;
                    your_email.setError("Invalid Email");
                    return;
                }

                if (TextUtils.isEmpty(subject)){
                    your_subject.setError("Enter Your Subject");
                    your_subject.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(message)){
                    your_message.setError("Enter Your Message");
                    your_message.requestFocus();
                    return;
                }

                Intent sendEmail = new Intent(Intent.ACTION_SEND);

                /* a7ot el data fel gmail */
                sendEmail.setType("plain/text");
                sendEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{"fareedkhaled70@gmail.com"});
                sendEmail.putExtra(Intent.EXTRA_SUBJECT, subject);
                sendEmail.putExtra(Intent.EXTRA_TEXT,
                        "Message:"+'\n'+message);

                /* Send it off to the Activity-Chooser */
                startActivity(Intent.createChooser(sendEmail, "Send mail..."));

            }
        });
        return view;
    }
// validating email id

    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
