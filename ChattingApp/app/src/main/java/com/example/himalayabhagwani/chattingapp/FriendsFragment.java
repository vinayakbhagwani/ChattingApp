package com.example.himalayabhagwani.chattingapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {


    RecyclerView friends_list;

    DatabaseReference mFriendDatabase;
    DatabaseReference mUsersDatabase;

    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;

    public View mMainView;

    boolean isOnline;

    String list_user_id;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUser.getUid());
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        friends_list = (RecyclerView) mMainView.findViewById(R.id.friends_list);
        friends_list.setHasFixedSize(true);
        friends_list.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.single_user_layout,
                FriendsViewHolder.class,
                mFriendDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, Friends friends, final int position) {

                friendsViewHolder.setDate(friends.getDate());

                list_user_id = getRef(position).getKey();
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userName = dataSnapshot.child("name").getValue().toString();
                        friendsViewHolder.setName(userName);

                        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                        friendsViewHolder.setThumbImage(thumb_image);

                        if (dataSnapshot.hasChild("online")) {

                            isOnline = (Boolean) dataSnapshot.child("online").getValue();
                            friendsViewHolder.setOnline(isOnline, getContext());
                        }

                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CharSequence options[] = new CharSequence[]{"   Open Profile", "   Send Message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                builder.setTitle("Select Options");

                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {

                                        if(which == 0) {

                                            Intent openProfile = new Intent(getContext(), ProfileActivity.class);
                                            openProfile.putExtra("user_id", getRef(position).getKey());
                                            startActivity(openProfile);
                                        }

                                        else if (which == 1) {

                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id",getRef(position).getKey());
                                            startActivity(chatIntent);
                                        }
                                    }
                                });

                                builder.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        friends_list.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageView thumb_ImageView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDate(String date) {

            TextView userDateView = (TextView) mView.findViewById(R.id.all_users_userstatus);
            userDateView.setText("Friends since: "+date);

        }

        public void setName(String name) {

            TextView userNameView = (TextView) mView.findViewById(R.id.all_users_username);
            userNameView.setText(name);
        }


        public void setThumbImage(String thumbImage) {

            thumb_ImageView = (ImageView) mView.findViewById(R.id.all_users_profile_image);
            new DownloadImage().execute(thumbImage);
        }

        public void setOnline(boolean isOnline, Context context) {

            ImageView img_online = (ImageView) mView.findViewById(R.id.img_online);

            if (isOnline == true && context!=null) {

                Glide.with(context).load(R.drawable.online_indicator).into(img_online);
                img_online.setVisibility(View.VISIBLE);
            }
            else if (isOnline == false && context!=null) {

                Glide.with(context).load(R.drawable.offline_indicator).into(img_online);
                img_online.setVisibility(View.VISIBLE);
            }
        }

        private void setImage(Drawable drawable)
        {
            thumb_ImageView.setImageDrawable(drawable);
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
