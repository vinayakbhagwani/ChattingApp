package com.example.himalayabhagwani.chattingapp;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView mConvList;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    String type;

    // CircleImageView userImageView;

    private View mMainView;

    // TextView userStatusView;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mConvList = (RecyclerView) mMainView.findViewById(R.id.conv_list);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            mCurrent_user_id = mAuth.getCurrentUser().getUid();

            mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);

            mConvDatabase.keepSynced(true);
            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
            mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
            mUsersDatabase.keepSynced(true);
        }



        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);


        // Inflate the layout for this fragment
        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {

        Query conversationQuery = mConvDatabase.orderByChild("timestamp");

        FirebaseRecyclerAdapter<Conv, ConvViewHolder> firebaseConvAdapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(
                Conv.class,
                R.layout.users_single_layout,
                ConvViewHolder.class,
                conversationQuery
        ) {
            @Override
            protected void populateViewHolder(final ConvViewHolder convViewHolder, final Conv conv, int i) {


                final String list_user_id = getRef(i).getKey();

                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String data = dataSnapshot.child("message").getValue().toString();
                        type = dataSnapshot.child("type").getValue().toString();
                        convViewHolder.setMessage(data, conv.isSeen(), type);

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        if (dataSnapshot.hasChild("online")) {

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            convViewHolder.setUserOnline(userOnline);

                        }

                        convViewHolder.setName(userName);
                        convViewHolder.setUserImage(userThumb, getContext());

                        convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", list_user_id);
                                chatIntent.putExtra("user_name", userName);
                                startActivity(chatIntent);

                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        mConvList.setAdapter(firebaseConvAdapter);

    }

    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder {

        View mView;
        CircleImageView userImageView;

        public ConvViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setMessage(String message, boolean isSeen, String type){

            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);

            if(type.equals("text")){

                userStatusView.setText(message);
            }
            else {

                userStatusView.setText("last conversation was an Image File");
            }

            if(!isSeen){
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            } else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
            }

        }

        public void setName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image, Context ctx){

            userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);

            new DownloadImage().execute(thumb_image);

            // Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_dp).into(userImageView);

        }

        public void setUserOnline(String online_status) {

            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_icon);

            if(online_status.equals("true")){

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

            }

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
