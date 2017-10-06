package com.loya.android.blogapp;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


/**
 * A simple {@link Fragment} subclass.
 * This fragment shows all posts made by the current user.
 */
public class PostFragment extends Fragment {
    private RecyclerView mUserPostRecycler;

    //database reference  to retrieve the posts from current user
    private DatabaseReference mDatabaseCurrentUser;

    //Query variable used to query the database
    private Query mQureryCurrentUser;


    //variable to check if the current has liked the post
    private boolean mProcessLike = false;

    //database reference for likes
    private DatabaseReference mDatabaseLike;


    private FirebaseAuth mAuth;
    //variable to get the current user id
    private String currentUserId;

    public PostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post, container, false);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        //reference to the Blog database to enable sorting of data via uid
        mDatabaseCurrentUser = FirebaseDatabase.getInstance().getReference().child("Blog");

        //queries the Blog node to get a child with uid equal to the current user uid
        mQureryCurrentUser = mDatabaseCurrentUser.orderByChild("uid").equalTo(currentUserId);
        mQureryCurrentUser.keepSynced(true);

        //reference to the "Likes" directory for storing likes of a post
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLike.keepSynced(true);

        mUserPostRecycler = (RecyclerView) rootView.findViewById(R.id.user_post_recycler);

        mUserPostRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mUserPostRecycler.setLayoutManager(linearLayoutManager);

        //This uses the firebase Recycler adapter to automatically fetch data and update the UI accordingly when the app starts
        FirebaseRecyclerAdapter<Blog, PostViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Blog, PostViewHolder>(

                        Blog.class,
                        R.layout.post,
                        PostViewHolder.class,
                        mQureryCurrentUser

                ) {
                    @Override
                    protected void populateViewHolder(PostViewHolder viewHolder, Blog model, int position) {


                        //get the post_key of the blog post
                        final String post_key = getRef(position).getKey();

                        viewHolder.setTitle(model.getTitle());
                        viewHolder.setDesc(model.getDesc_value());
                        viewHolder.setImage(getActivity(), model.getImage());
                        viewHolder.setUserName(model.getUsername());
                        viewHolder.setProfileImage(getActivity(), model.getProfile_image());
                        viewHolder.setPostTime(model.getPost_time());

                        viewHolder.setLikeBtn(post_key);

                        //you can set onclick listener for the whole recycler view i.e the post here
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(getActivity(), post_key, Toast.LENGTH_LONG).show();
                                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                                detailIntent.putExtra("post_key", post_key);
                                startActivity(detailIntent);
                            }
                        });

                        viewHolder.mLikeBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mProcessLike = true;//sets the boolean to true, since user has clicked it
                                //adding to the "Likes" node
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
                                                //user has not liked the post, so save the like to database with the uid
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
        mUserPostRecycler.setAdapter(firebaseRecyclerAdapter);


        // Inflate the layout for this fragment
        return rootView;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ImageButton mLikeBtn;

        //Database reference to the Likes node
        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;

        public PostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mLikeBtn = (ImageButton) mView.findViewById(R.id.likeBtn);

            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth = FirebaseAuth.getInstance();
            mDatabaseLike.keepSynced(true);

        }

        //method to change the color of the like button once it is clicked
        public void setLikeBtn(final String post_key) {

            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //check if the post_key has a child of the current uid i.e if the user has already liked the post
                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                        //user has liked the post already, change the color of the like button
                        mLikeBtn.setImageResource(R.drawable.ic_like);

                    } else {
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

}
