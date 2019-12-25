package com.example.himalayabhagwani.chattingapp;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    RecyclerView friend_requests_list;

    DatabaseReference mFriendRequestsDatabase;
    DatabaseReference mUsersDatabase;
    // DatabaseReference requestType;

    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;

    public View mMainView;

    String list_user_id;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mCurrentUser != null) {

            mFriendRequestsDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrentUser.getUid());
        }
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        // requestType = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrentUser.getUid());

        friend_requests_list = (RecyclerView) mMainView.findViewById(R.id.friend_requests_list);
        friend_requests_list.setHasFixedSize(true);
        friend_requests_list.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;

    }


    @Override
    public void onStart() {
        super.onStart();

        if(mCurrentUser != null) {

            final FirebaseRecyclerAdapter<Requests, RequestsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Requests, RequestsViewHolder>(

                    Requests.class,
                    R.layout.users_single_layout,
                    RequestsViewHolder.class,
                    mFriendRequestsDatabase

            ) {
                @Override
                protected void populateViewHolder(final RequestsViewHolder requestsViewHolder, Requests requests, final int position) {

                    requestsViewHolder.setRequestType(requests.getRequest_type());

                    list_user_id = getRef(position).getKey();

                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String userName = dataSnapshot.child("name").getValue().toString();
                            requestsViewHolder.setName(userName);

                            String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                            requestsViewHolder.setUserImage(thumb_image);

                            requestsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent openProfile = new Intent(getContext(), ProfileActivity.class);
                                    openProfile.putExtra("user_id", getRef(position).getKey());
                                    startActivity(openProfile);

                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }

                    });
                }
            };

            friend_requests_list.setAdapter(firebaseRecyclerAdapter);

        }

    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        CircleImageView userImageView;

        public RequestsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setRequestType(String request_type) {

            TextView txtRequestStatus = (TextView) mView.findViewById(R.id.user_single_status);

            if (request_type.equals("sent")) {

                txtRequestStatus.setText("friend request sent");
            }
            else if (request_type.equals("received")) {

                txtRequestStatus.setText("confirm friend request");
            }
        }

        public void setName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image){

            userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);

            new DownloadImage().execute(thumb_image);

            // Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_dp).into(userImageView);

        }

        private void setImage(Drawable drawable)
        {
            userImageView.setImageDrawable(drawable);
        }

        public class DownloadImage extends AsyncTask<String, Integer, Drawable> {

            @Override
            protected Drawable doInBackground(String... arg0) {
                // This is done in a background thread
                return downloadImage(arg0[0]);
            }

            /**
             * Called after the image has been downloaded
             * -> this calls a function on the main thread again
             */
            protected void onPostExecute(Drawable image)
            {
                setImage(image);
            }


            /**
             * Actually download the Image from the _url
             * @param _url
             * @return
             */
            private Drawable downloadImage(String _url)
            {
                //Prepare to download image
                URL url;
                BufferedOutputStream out;
                InputStream in;
                BufferedInputStream buf;

                //BufferedInputStream buf;
                try {
                    url = new URL(_url);
                    in = url.openStream();



                    // Read the inputstream
                    buf = new BufferedInputStream(in);

                    // Convert the BufferedInputStream to a Bitmap
                    Bitmap bMap = BitmapFactory.decodeStream(buf);
                    if (in != null) {
                        in.close();
                    }
                    if (buf != null) {
                        buf.close();
                    }

                    return new BitmapDrawable(bMap);

                } catch (Exception e) {
                    Log.e("Error reading file", e.toString());
                }

                return null;
            }

        }

    }


}
