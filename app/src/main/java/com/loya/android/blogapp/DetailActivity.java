package com.loya.android.blogapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private ImageView post_image;
    private TextView detailPostTitle;
    private TextView post_desc;

    private DatabaseReference mDatabaseReference;
    private String mPost_key;

    private Button deletePostBtn;

    private FirebaseAuth mAuth;

    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();


        //reference to the Blog node on the database,
        // it is used to retrieve the various fields under the post_key obtained from the intent
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");

        mPost_key = getIntent().getExtras().getString("post_key");

        Toast.makeText(DetailActivity.this, mPost_key, Toast.LENGTH_LONG).show();


        post_image = (ImageView) findViewById(R.id.detail_imageView);
        detailPostTitle = (TextView) findViewById(R.id.detail_titleText);
        post_desc = (TextView) findViewById(R.id.detail_Text);

        deletePostBtn = (Button) findViewById(R.id.deletePostBtn);


        mDatabaseReference.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String post_Title = (String) dataSnapshot.child("title").getValue();
                String desc_value = (String) dataSnapshot.child("desc_value").getValue();
                final String image = (String) dataSnapshot.child("image").getValue();
                String post_uid = (String) dataSnapshot.child("uid").getValue();

                detailPostTitle.setText(post_Title);
                post_desc.setText(desc_value);

                //gets the image from offline if it has already synced the data online
                Picasso.with(getApplicationContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(post_image, new Callback() {
                    @Override
                    public void onSuccess() {
                        //data is retrieved from cache
                        //do nothing!
                    }

                    @Override
                    public void onError() {
                        //data not in cache, so sync data online!
                        Picasso.with(getApplicationContext()).load(image).into(post_image);

                    }
                });

                if (mCurrentUser.getUid().equals(post_uid)) {
                    //show the delete post button
                    deletePostBtn.setVisibility(View.VISIBLE);

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        deletePostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //delete the post if the user is the author of the post
                mDatabaseReference.child(mPost_key).removeValue();
                Intent deleteIntent= new Intent(DetailActivity.this, MainActivity.class);
                startActivity(deleteIntent);
            }
        });
    }
}
