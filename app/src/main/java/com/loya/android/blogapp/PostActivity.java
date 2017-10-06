package com.loya.android.blogapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class PostActivity extends AppCompatActivity {
    private static final int MAX_LENGTH = 5;
    private ImageButton mSelectImage;
    private EditText mPostTitle;
    private EditText mPostDescription;
    private Button submitButton;

    private TextView hintText;

    private static final int GALLERY_REQUEST = 1;


    Uri mImageUri = null;

    //Firebase storage object for saving large files such as images, audios, videos , etc
    private StorageReference mStorage;

    //Firebase Database reference object for saving files to database
    private DatabaseReference mDatabase;

    //Firebase Database reference object for retrieving the user id already saved in the database
    private DatabaseReference mDatabaseUsers;

    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        //gets the current user that is logged in
        mCurrentUser = mAuth.getCurrentUser();

        //reference to the "Users" node  and query the current user_id node
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        mSelectImage = (ImageButton) findViewById(R.id.imageSelect);
        mPostTitle = (EditText) findViewById(R.id.titleField);
        mPostDescription = (EditText) findViewById(R.id.descField);
        submitButton = (Button) findViewById(R.id.submitBtn);

        hintText = (TextView) findViewById(R.id.hintText);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);


        //a reference to the firebase storage
        mStorage = FirebaseStorage.getInstance().getReference().child("Images");

        //a refrence to the firebase database reference
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");


        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hintText.setVisibility(View.GONE);
                //image picker intent
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //save to database
                startPosting();
            }
        });

    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        // mImageUri = null;
//
//        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
//            //successful
//            mImageUri = data.getData();
//            mSelectImage.setImageURI(mImageUri);
//
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();


            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                mSelectImage.setImageURI(mImageUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }


    private void startPosting() {
        // mProgressBar.setVisibility(View.VISIBLE);
        final String title_val = mPostTitle.getText().toString().trim();
        final String desc_val = mPostDescription.getText().toString().trim();

        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && mImageUri != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            String path = randomString();
            //input fields not empty and the image uri is not null so we can post tp Database
            StorageReference filePath = mStorage.child(mImageUri.getLastPathSegment());
            //upload to firebase storage
            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //get the image url of the uploaded image if upload successful
                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    //post to database with a unique id
                    final DatabaseReference newPost = mDatabase.push();

                    //retrieving the name of the current user from the "Users" directory using addValueEventListener
                    mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mProgressBar.setVisibility(View.VISIBLE);

                            newPost.child("title").setValue(title_val);
                            newPost.child("desc_value").setValue(desc_val);
                            newPost.child("image").setValue(downloadUrl.toString());
                            newPost.child("uid").setValue(mCurrentUser.getUid());
                            newPost.child("post_time").setValue(getCurrentTime());
                            //get user name from the dataSnapshot
                            newPost.child("username").setValue(dataSnapshot.child("name").getValue());
                            newPost.child("profile_image").setValue(dataSnapshot.child("image").getValue());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

            mProgressBar.setVisibility(View.GONE);
            Toast.makeText(PostActivity.this, "post successful", Toast.LENGTH_SHORT).show();
            finish();
            finish();
        }


    }

    public static String randomString() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    public String getCurrentTime() {

        long currentTime = System.currentTimeMillis();
        //SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM DD yyyy HH:mm a");
        //convert the time in milliseconds into a Date object by calling the Date constructor.
        Date dateObject = new Date(currentTime);
        //Then we can initialize a SimpleDateFormat instance
        // and configure it to provide a more readable representation according to the given format.
        SimpleDateFormat monthAndDay = new SimpleDateFormat("MMM d"); //hour and minute  e.g Sep 26
        SimpleDateFormat hourAndMinute = new SimpleDateFormat(" HH:mm a"); //hour and minute  e.g 12:15 PM
        String postTime = new StringBuilder().append(monthAndDay.format(dateObject))
                .append(" at ").append(hourAndMinute.format(dateObject)).toString();


        return postTime;
    }
}
