package com.loya.android.blogapp;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class ProfileActivity extends AppCompatActivity {


    private TextView mProfileUsername;
    private TextView mProfileEmail;

    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private String user_id;

    private ImageView mProfileImage;

    private String numOfPosts;
    private String numOfLikes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("Profile");


        mAuth = FirebaseAuth.getInstance();
        //reference to the "Users" node so as to retrieve data from it...
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        mCurrentUser = mAuth.getCurrentUser();
        user_id = mCurrentUser.getUid();

        //views
        mProfileUsername = (TextView) findViewById(R.id.profile_username);
        mProfileImage = (ImageView) findViewById(R.id.user_profile_image);
        mProfileEmail = (TextView) findViewById(R.id.profile_email);


        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("POSTS"));
        tabLayout.addTab(tabLayout.newTab().setText("PROFILE"));
        tabLayout.addTab(tabLayout.newTab().setText("LIKES"));

        CustomPagerAdapter customPagerAdapter = new CustomPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());


        final ViewPager mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(customPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        Toast.makeText(ProfileActivity.this, user_id, Toast.LENGTH_LONG).show();

        mDatabaseUsers.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //retrieve the user's detail
                final String image = (String) dataSnapshot.child("image").getValue();
                String username = (String) dataSnapshot.child("name").getValue();
                String email = mCurrentUser.getEmail();

                mProfileUsername.setText(username);
                mProfileEmail.setText(email);

                //gets the image from offline if it has already synced the data online
                Picasso.with(getApplicationContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(mProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        //data is retrieved from cache
                        //do nothing!
                    }

                    @Override
                    public void onError() {
                        //data not in cache, so sync data online!
                        Picasso.with(getApplicationContext()).load(image).into(mProfileImage);

                    }
                });



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

}
