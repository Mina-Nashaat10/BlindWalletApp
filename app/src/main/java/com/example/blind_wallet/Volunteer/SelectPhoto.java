package com.example.blind_wallet.Volunteer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.blind_wallet.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import de.hdodenhof.circleimageview.CircleImageView;

public class SelectPhoto extends AppCompatActivity {

    CircleImageView circleImageView;
    Button button,next;
    public static final int IMAGE_CODE = 1;
    StorageReference storageReference;
    FirebaseAuth fAuth;
    FirebaseUser user;
    boolean ischanged = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_photo);
        storageReference = FirebaseStorage.getInstance().getReference();
        fAuth = FirebaseAuth.getInstance();
        user =fAuth.getCurrentUser();

        StorageReference profileRef=storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        circleImageView = (CircleImageView) findViewById(R.id.imageview);
        next = (Button)findViewById(R.id.next);
        button =(Button)findViewById(R.id.loadimage);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openimageform();
            }
        });
    }
    private void openimageform()
    {
        Intent i = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, IMAGE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null)
        {
            Toast.makeText(this, "Please Choose The Image Profile...", Toast.LENGTH_SHORT).show();
        }
        if(requestCode == IMAGE_CODE && resultCode == RESULT_OK && data !=null)
        {
                Uri uri = data.getData();
                CropImage.activity(uri)
                        .setAspectRatio(1,1)
                        .setMaxCropResultSize(500,500)
                        .start(SelectPhoto.this);
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK)
            {
                Uri resulturi = result.getUri();
                circleImageView.setImageURI(resulturi);
                UploadeImageToFirebase(resulturi);
                ischanged = true;
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
            if(ischanged == true)
            {
                next.setVisibility(View.VISIBLE);
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getApplicationContext(), com.example.blind_wallet.Volunteer.Main3Activity.class));
                    }
                });
            }
            else
            {
                Toast.makeText(this, "Please Choose The Image Profile...", Toast.LENGTH_SHORT).show();
            }
            
        }
    }
    private void UploadeImageToFirebase(final Uri imageUri) {

        final StorageReference fileref=storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");

        fileref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                fileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Picasso.get().load(uri).into(circleImageView);

                    }
                });

                //Toast.makeText(MainActivity.this,"Image uploaded",Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(SelectPhoto.this,"Faild To Upload",Toast.LENGTH_SHORT).show();

            }
        });


    }
}
