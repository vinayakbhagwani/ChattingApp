package com.example.himalayabhagwani.chattingapp;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView profile_ImageView;
    TextView profile_DisplayName, profile_Status, profile_Friends;
    Button btnSendRequest;

    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;

    String current_uid;

    ProgressDialog progressDialog;

    String mCurrentState;

    private FirebaseUser mCurrentUser;

    String current_date;

    String name;

    int totalFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mRootRef = FirebaseDatabase.getInstance().getReference();

        current_uid = getIntent().getStringExtra("user_id");

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");

        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();


        profile_ImageView = (ImageView) findViewById(R.id.profile_ImageView);
        profile_DisplayName = (TextView) findViewById(R.id.profile_DisplayName);
        profile_Status = (TextView) findViewById(R.id.profile_Status);
        profile_Friends = (TextView) findViewById(R.id.profile_Friends);
        btnSendRequest = (Button) findViewById(R.id.btnSendRequest);
        btnSendRequest.setOnClickListener(this);

        if (mCurrentUser.getUid().equals(current_uid)) {

            btnSendRequest.setVisibility(View.INVISIBLE);
            btnSendRequest.setEnabled(false);
        }
        else {

            btnSendRequest.setVisibility(View.VISIBLE);
            btnSendRequest.setEnabled(true);
        }

        mCurrentState = "not_friends";

        progressDialog = new ProgressDialog(ProfileActivity.this);

        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("Fetching Data");
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.show();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                profile_DisplayName.setText(name);
                profile_Status.setText(status);

                new DownloadImage().execute(image);

                /* Picasso.with(SettingsActivity.this)
                        .load(image)
                        .fit()
                        .centerInside()
                        .into(profile_picture); */

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(current_uid)) {

                    String req_type = dataSnapshot.child(current_uid).child("request_type").getValue().toString();

                    if(req_type.equals("sent")) {

                        mCurrentState = "req_sent";
                        btnSendRequest.setText("Cancel Friend Request");
                    }
                    else if(req_type.equals("received")) {

                        btnSendRequest.setEnabled(true);
                        mCurrentState = "req_received";
                        btnSendRequest.setText("Accept Friend Request");
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mFriendDatabase.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(current_uid)) {

                    mCurrentState = "friends";
                    btnSendRequest.setText("Unfriend "+name);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mFriendDatabase.child(current_uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                for (DataSnapshot snap: dataSnapshot.getChildren()) {

                    totalFriends+=dataSnapshot.getChildrenCount();
                }

                profile_Friends.setText(String.valueOf(totalFriends)+" Friends");

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

    }

    private void setImage(Drawable drawable)
    {
        profile_ImageView.setImageDrawable(drawable);
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
            progressDialog.dismiss();
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

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.btnSendRequest) {

            btnSendRequest.setEnabled(false);

            // ------------------ NOT FRIENDS STATE ------------------------ //

            if(mCurrentState.equals("not_friends")) {

                mFriendRequestDatabase.child(mCurrentUser.getUid()).child(current_uid).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()) {

                            mFriendRequestDatabase.child(current_uid).child(mCurrentUser.getUid()).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    HashMap<String, String> notificationData = new HashMap<>();
                                    notificationData.put("from", mCurrentUser.getUid());
                                    notificationData.put("type", "request");

                                    mNotificationDatabase.child(current_uid).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            btnSendRequest.setEnabled(true);
                                            mCurrentState = "req_sent";
                                            btnSendRequest.setText("Cancel Friend Request");

                                            Toast.makeText(ProfileActivity.this, "Friend Request Sent", Toast.LENGTH_LONG).show();
                                        }
                                    });

                                }
                            });
                        }
                        else {
                            btnSendRequest.setEnabled(true);
                            Toast.makeText(ProfileActivity.this, "Unable to send request... Try Again", Toast.LENGTH_LONG).show();
                        }

                    }
                });
            }

            // ------------------ CANCEL FRIEND REQUEST ------------------------ //

            else if(mCurrentState.equals("req_sent"))
            {

                mFriendRequestDatabase.child(mCurrentUser.getUid()).child(current_uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        mFriendRequestDatabase.child(current_uid).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                btnSendRequest.setEnabled(true);
                                mCurrentState = "not_friends";
                                btnSendRequest.setText("Send Friend Request");
                                Toast.makeText(ProfileActivity.this, "Friend Request Cancelled", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }

            // ----------------------------- REQUEST RECEIVED -------------------------------

            else if (mCurrentState.equals("req_received")) {

                current_date = DateFormat.getDateTimeInstance().format(new Date());

                mFriendDatabase.child(mCurrentUser.getUid()).child(current_uid).child("date").setValue(current_date).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        mFriendDatabase.child(current_uid).child(mCurrentUser.getUid()).child("date").setValue(current_date).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                mFriendRequestDatabase.child(mCurrentUser.getUid()).child(current_uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mFriendRequestDatabase.child(current_uid).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                btnSendRequest.setEnabled(true);
                                                mCurrentState = "friends";
                                                btnSendRequest.setText("Unfriend "+name);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }


            else if (mCurrentState.equals("friends")) {

                mFriendDatabase.child(mCurrentUser.getUid()).child(current_uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        mFriendDatabase.child(current_uid).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                mRootRef.child("Chat").child(mCurrentUser.getUid()).child(current_uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mRootRef.child("Chat").child(current_uid).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                mRootRef.child("messages").child(mCurrentUser.getUid()).child(current_uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        mRootRef.child("messages").child(current_uid).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                btnSendRequest.setEnabled(true);
                                                                mCurrentState = "not_friends";
                                                                btnSendRequest.setText("Send Friend Request");
                                                                Toast.makeText(ProfileActivity.this, "Unfriending Successful", Toast.LENGTH_LONG).show();

                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });

            }

        }

    }

}
