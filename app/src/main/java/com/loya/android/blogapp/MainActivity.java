package com.loya.android.blogapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mBlogList;
    private DatabaseReference mDatabaseReference;

    //database reference for offline capabilities
    private DatabaseReference mDatabaseUsers;

    //reference to the firebase auth object
    private FirebaseAuth mAuth;
    //reference to the firebase authstatelistener which detects changes in the authentication and triggers a change accordingly
    private FirebaseAuth.AuthStateListener mAuthListener;

    private boolean mProcessLike = false;

    //database reference for likes
    private DatabaseReference mDatabaseLike;

    //database reference  to retrieve the posts from current user
    private  DatabaseReference mDatabaseCurrentUser;

    //Query variable used to query the database
    private Query mQureryCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        //checks the login status of the user
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    //user not logged in
                    //redirect user to the login page
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    //clear flags so the user won't be able to go back to the previous page
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);


                }
            }
        };
        //reference to the blog_post directory so as to retrieve dta from it and display via the Firebase Adapter and recycler view
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");
        //enable offline capabilities for the mDatabaseReference
        mDatabaseReference.keepSynced(true);
        //reference to the "Users" directory so as to sync it offline and perform check offline
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        //reference to the "Likes" directory for storing likes of a post
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLike.keepSynced(true);


        //user id of the current user

       // String currentUserId = mAuth.getCurrentUser().getUid();
        //reference to the Blog database to enable sorting of data via uid
        mDatabaseCurrentUser = FirebaseDatabase.getInstance().getReference().child("Blog");

        //queries the Blog node to get a child with uid oequal to the current user uid
      //  mQureryCurrentUser = mDatabaseCurrentUser.orderByChild("uid").equalTo(currentUserId);


        mBlogList = (RecyclerView) findViewById(R.id.blog_list);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));

        //checks if the user exists both offline and online and redirects the user to the setupActivity
        checkUserExist();

    }

    @Override
    protected void onStart() {
        super.onStart();


        //add the authListener so that it checks if the user is signed in or not each time the app is started
        mAuth.addAuthStateListener(mAuthListener);

        //This uses the firebase Recycler adapter to automatically fetch data and update the UI accordingly when the app starts
        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(

                Blog.class,
                R.layout.post,
                BlogViewHolder.class,
                mDatabaseReference

        ) {
            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Blog model, int position) {

                //get the post_key of the blog post
                final String post_key = getRef(position).getKey();


                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc_value());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setUserName(model.getUsername());
                viewHolder.setProfileImage(getApplicationContext(), model.getProfile_image());
                viewHolder.setPostTime(model.getPost_time());

                viewHolder.setLikeBtn(post_key);

                //you can set onclick listener for the whole recycler view i.e the post here
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(MainActivity.this, post_key, Toast.LENGTH_LONG).show();
                        Intent detailIntent = new Intent(MainActivity.this, DetailActivity.class);
                        detailIntent.putExtra("post_key", post_key);
                        startActivity(detailIntent);
                    }
                });

                viewHolder.mLikeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mProcessLike = true;

                        mDatabaseLike.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (mProcessLike) {
                                    //check if the user has already liked the post
                                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                                        //user already likes the post, so if clicked again, it should delete it i.e unlike
                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        mProcessLike = false;

                                    } else {
                                        //user has not liked the post, so save the like to database
                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("Ananymous");
                                        mProcessLike = false;
                                    }
                                }
                            }


                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                });
            }
        };
        mBlogList.setAdapter(firebaseRecyclerAdapter);

        checkUserExist();

    }

    private void checkUserExist() {
        if (mAuth.getCurrentUser() != null) {


            //gets the user_id of the current user i.e user just logged in
            final String user_id = mAuth.getCurrentUser().getUid();

            //check if the user is already in the database
            //use the "addValueEventListener" to retrieve a single data from the database,
            // use onChildEventListener to retrieve multiple data
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {
                        //user does not exist database with a complete profile, so proceed to setup  Activity
                        //this is done offline as well since the database is synced offline
                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                        //clear flags so the user won't be able to go back to the previous page
                        setupIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView; //reference for the whole view in the Recycler view

        ImageButton mLikeBtn;

        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;

        public BlogViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mLikeBtn = (ImageButton) mView.findViewById(R.id.likeBtn);

            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth = FirebaseAuth.getInstance();
            mDatabaseLike.keepSynced(true);

        }

        public void setLikeBtn(final String post_key){
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())){
                        //user has liked the post already, change the color of the like button
                        mLikeBtn.setImageResource(R.drawable.ic_like);

                    }else{
                        //user has not liked the post
                        mLikeBtn.setImageResource(R.drawable.ic_dislike);

                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        public void setTitle(String title) {
            TextView post_title = mView.findViewById(R.id.post_title);
            post_title.setText(title);
        }

        public void setDesc(String desc) {
            TextView post_desc = mView.findViewById(R.id.post_desc);
            post_desc.setText(desc);
        }

        public void setPostTime(String postTime) {
            TextView post_time = mView.findViewById(R.id.post_time);
            post_time.setText(postTime);
        }

        public void setImage(final Context context, final String image) {
            final ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);

            //gets the image from offline if it has already synced the data online
            Picasso.with(context).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(post_image, new Callback() {
                @Override
                public void onSuccess() {
                    //data is retrieved from cache
                    //do nothing!
                }

                @Override
                public void onError() {
                    //data not in cache, so sync data online!
                    Picasso.with(context).load(image).into(post_image);

                }
            });

        }

        public void setUserName(String userName) {
            TextView profile_username = mView.findViewById(R.id.username);
            profile_username.setText(String.format("posted by %s", userName));

        }

        public void setProfileImage(final Context context, final String profileImage) {
            final ImageView profile_image = (ImageView) mView.findViewById(R.id.profile_image);

            //gets the image from offline if it has already synced the data online
            Picasso.with(context).load(profileImage).networkPolicy(NetworkPolicy.OFFLINE).into(profile_image, new Callback() {
                @Override
                public void onSuccess() {
                    //data is retrieved from cache
                    //do nothing!
                }

                @Override
                public void onError() {
                    //data not in cache, so sync data online!
                    Picasso.with(context).load(profileImage).into(profile_image);

                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent addIntent = new Intent(MainActivity.this, PostActivity.class);
                startActivity(addIntent);
                break;
            case R.id.action_settings:
                break;
            case R.id.action_logOut:
                logOut();
                break;
            case R.id.action_profile:
                Intent profileIntent= new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void logOut() {
        //signs out of the app
        mAuth.signOut();
    }
}
