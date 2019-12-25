package com.example.himalayabhagwani.chattingapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.URL;

public class UsersActivity extends AppCompatActivity {

    Toolbar mToolbar;

    RecyclerView mUsersList;

    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = (Toolbar) findViewById(R.id.users_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ImageView thumb_ImageView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name) {

            TextView userNameView = (TextView) mView.findViewById(R.id.all_users_username);
            userNameView.setText(name);

        }

        public void setStatus(String status) {

            TextView userStatusView = (TextView) mView.findViewById(R.id.all_users_userstatus);
            userStatusView.setText(status);

        }

        public void setThumb_image(String thumb_image, Context context) {

            thumb_ImageView = (ImageView) mView.findViewById(R.id.all_users_profile_image);
            /* Picasso.with(context)
                    .load(thumb_image)
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.default_dp)
                    .into(thumb_ImageView); */

            new DownloadImage().execute(thumb_image);
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

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(

                Users.class,
                R.layout.single_user_layout,
                UsersViewHolder.class,
                mUserDatabase
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder usersViewHolder, Users users, int position) {

                usersViewHolder.setName(users.getName());
                usersViewHolder.setStatus(users.getStatus());
                usersViewHolder.setThumb_image(users.getThumb_image(), getApplicationContext());

                final String user_id = getRef(position).getKey();

                usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);

                    }
                });
            }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }
}
