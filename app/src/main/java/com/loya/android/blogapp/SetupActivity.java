package com.loya.android.blogapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupActivity extends AppCompatActivity {

    private ImageButton mSetupImageBtn;
    private EditText mSetupDisplayNameField;
    private Button mFinishSetupBtn;

    private static final int GALLERY_REQUEST = 1;
    //FirebaseAuth object used to get the current user
    private FirebaseAuth mAuth;

    //DatabaseReference to be used to store the user's profile data
    private DatabaseReference mDatabaseUsers;

    private StorageReference mStorageRef;

    private Uri mImageUri = null;

    private ProgressBar mSetupProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        //reference to the "Users" node
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        //a directory is created to store all profile images to the Firebase Storage
        mStorageRef = FirebaseStorage.getInstance().getReference().child("Profile_images");

        mSetupImageBtn = (ImageButton) findViewById(R.id.setupImageBtn);
        mSetupDisplayNameField = (EditText) findViewById(R.id.setupDisplayNameField);
        mFinishSetupBtn = (Button) findViewById(R.id.finishSetupBtn);

        mSetupProgressBar = (ProgressBar) findViewById(R.id.setupProgressBar);


        mFinishSetupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSetupAccount();

            }
        });


        mSetupImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //image picker intent
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);


            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void startSetupAccount() {

        final String name = mSetupDisplayNameField.getText().toString().trim();
        final String user_id = mAuth.getCurrentUser().getUid();

        if (!TextUtils.isEmpty(name) && mImageUri != null) {
            mSetupProgressBar.setVisibility(View.VISIBLE);
            //upload the image to the Firebase Storage so that the download url can be retrieved
            //and saved to the database
            StorageReference filePath = mStorageRef.child(mImageUri.getLastPathSegment());
            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                    //creates a new child inside the User directory
                    //stores the name and profile image of the current user to the database using the user id
                    mDatabaseUsers.child(user_id).child("name").setValue(name);
                    mDatabaseUsers.child(user_id).child("image").setValue(downloadUrl.toString());
                    Toast.makeText(SetupActivity.this, "Profile set up complete!", Toast.LENGTH_LONG).show();
                    mSetupProgressBar.setVisibility(View.GONE);

                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);

                }
            });
        }
    }

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

                mSetupImageBtn.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}

