package com.example.himalayabhagwani.chattingapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.EncodeStrategy;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private String mChatUser;

    Toolbar mChatActivityToolbar;

    String current_uid;

    FirebaseAuth mAuth;

    String mCurrentUserId;

    DatabaseReference mUsersDatabase;

    DatabaseReference mRootRef;

    String clickedName;

    ImageView chatting_bg;

    TextView txtDisplayName;
    TextView txtLastSeen;
    CircleImageView clicked_name_dp;

    String thumb_image;
    String from_user;
    String message_type;
    long time;

    Bitmap thumb_image_sent_bitmap;

    ImageButton btnAdd, btnSend;
    EditText txtMessage;

    RecyclerView mMessagesList;
    SwipeRefreshLayout mRefreshLayout;

    private List<Messages> messagesList;

    int totalItems;
    int msgCountAtPresent;

    MessageAdapter mAdapter;

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    LinearLayoutManager mLinearLayoutManager;

    Query messageQuery;

    ChildEventListener childEventListener;

    private int PICK_IMAGE_REQUEST = 1;

    private StorageReference mImageStorage;

    DatabaseReference mFromUserDatabase;
    DatabaseReference mMessageCountDatabase;

    int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messagesList = new ArrayList<>();

        btnAdd = (ImageButton) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);

        btnSend = (ImageButton) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        txtMessage = (EditText) findViewById(R.id.txtMessage);

        mAuth = FirebaseAuth.getInstance();

        current_uid = mAuth.getCurrentUser().getUid();

        mChatUser = getIntent().getStringExtra("user_id");

        mChatActivityToolbar = (Toolbar) findViewById(R.id.chat_activity_toolbar);
        setSupportActionBar(mChatActivityToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        chatting_bg = (ImageView) findViewById(R.id.chattingBg);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.custom_bar_layout, null);

        getSupportActionBar().setCustomView(action_bar_view);

        txtDisplayName = (TextView) findViewById(R.id.clicked_name_display_name);
        txtLastSeen = (TextView) findViewById(R.id.clicked_name_last_seen);
        clicked_name_dp = (CircleImageView) findViewById(R.id.clicked_name_dp);

        Glide.with(getApplicationContext()).load(R.drawable.chatting_background).into(chatting_bg);

        mImageStorage = FirebaseStorage.getInstance().getReference();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mChatUser);
        mFromUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mUsersDatabase.keepSynced(true);
        mFromUserDatabase.keepSynced(true);

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                clickedName = dataSnapshot.child("name").getValue().toString();
                txtDisplayName.setText(clickedName);

                thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                new DownloadClickedNameImage().execute(thumb_image);

                boolean isOnline = (Boolean) dataSnapshot.child("online").getValue();

                if (isOnline)
                {
                    txtLastSeen.setText("online");
                }
                else {
                    txtLastSeen.setText("Last Online: "+getTimeAgo(Long.parseLong(dataSnapshot.child("lastSeen").getValue().toString()),getApplicationContext()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mChatUser)) {

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser,chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId,chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {

                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mLinearLayoutManager = new LinearLayoutManager(this);

        mMessagesList = (RecyclerView) findViewById(R.id.mMessagesList);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.mRefreshLayout);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayoutManager);

        loadMessages();

        mMessageCountDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrentUserId).child(mChatUser);
        mMessageCountDatabase.limitToLast(mCurrentPage*10).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                /* for (DataSnapshot snap : dataSnapshot.getChildren()) {

                    msgCountAtPresent++;
                }
                Log.d("Total message count: ", String.valueOf(msgCountAtPresent));
                if (mCurrentPage == 1) {

                    mMessagesList.scrollToPosition((msgCountAtPresent/5)-1);
                }
                else {

                    mMessagesList.scrollToPosition((mCurrentPage*10) - 1);
                }  */

                mMessagesList.scrollToPosition((mCurrentPage*10) - 1);

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

        // mAdapter = new MessageAdapter(messagesList);

        // mMessagesList.setAdapter(mAdapter);

        // mRefreshLayout.setEnabled(false);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;

                mMessagesList.scrollToPosition(9);

                flag = 1;

                loadMessages();

            }
        });

    }

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        messageRef.keepSynced(true);
        // View firstView = mLinearLayoutManager.findViewByPosition(0);

        messageQuery = messageRef.orderByKey().limitToLast(mCurrentPage * 10);

        // messageQuery = messageRef.orderByKey().limitToLast(mCurrentPage * 10);

        FirebaseRecyclerAdapter<Messages, MessageViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Messages, MessageViewHolder>(

                Messages.class,
                R.layout.message_single_layout,
                MessageViewHolder.class,
                messageQuery

        ) {
            @Override
            protected void populateViewHolder(final MessageViewHolder messageViewHolder, Messages messages, int position) {

                    // totalItems += getItemCount();

                    // Log.d("Total Items: ", String.valueOf(totalItems));

                    // mMessagesList.scrollToPosition(totalItems-1);

                    message_type = messages.getType();
                    from_user = messages.getFrom();
                    time = messages.getTime();

                    messageViewHolder.setMessageText(messages.getMessage(), message_type, from_user, current_uid);

                    messageViewHolder.setMessageImage(messages.getMessage(), message_type, getApplicationContext());

                    messageViewHolder.setTime(time);

                    mFromUserDatabase.child(from_user).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String displayName = dataSnapshot.child("name").getValue().toString();
                            messageViewHolder.setName(displayName);

                            String eachMsgOwnerDp = dataSnapshot.child("thumb_image").getValue().toString();
                            messageViewHolder.setThumbImage(eachMsgOwnerDp, getApplicationContext());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
            }
        };

        mMessagesList.setAdapter(firebaseRecyclerAdapter);

        // mLinearLayoutManager.scrollToPosition(9);

        mRefreshLayout.setRefreshing(false);

        /* childEventListener = messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                messagesList.add(message);

                mAdapter = new MessageAdapter(messagesList);

                // mAdapter.notifyDataSetChanged();

                mAdapter.setHasStableIds(true);

                mMessagesList.setAdapter(mAdapter);

                if (mCurrentPage == 1) {

                    mMessagesList.scrollToPosition(messagesList.size()-1);
                }
                else {

                    mMessagesList.scrollToPosition(messagesList.size() - ((mCurrentPage*10) - 2));
                }

                mRefreshLayout.setRefreshing(false);

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
        }); */
    }

    private void setClickedNameImage(Drawable drawable)
    {
        // clicked_name_dp.setImageDrawable(drawable);
        if (!getApplicationContext().equals(null)) {

            Glide.with(getApplicationContext())
                    .load(drawable)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(clicked_name_dp);
        }

    }



    public class DownloadClickedNameImage extends AsyncTask<String, Integer, Drawable> {

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
            setClickedNameImage(image);
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

    public static String getTimeAgo(long time, Context ctx) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btnSend) {

            sendMessage();

            txtMessage.setText("");

            // msgCountAtPresent = 0;

            // messagesList.clear();



            // messagesList = new ArrayList<>();

            // loadMessages();
        }

        else if (v.getId() == R.id.btnAdd) {

            addMedia();

            // msgCountAtPresent = 0;
        }
    }

    private void addMedia() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);

        /* Intent intent = new Intent();

        // Show only images, no videos or anything else

        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        // Always show the chooser (if there are multiple options available)

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST); */
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == RESULT_OK) {

                    Uri resultUri = result.getUri();
                    //profile_picture.setImageURI(resultUri);

                    File thumb_filePath = new File(resultUri.getPath());

                    Bitmap thumb_bitmap = new Compressor(this)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
                    byte[] thumb_byte = baos.toByteArray();

                    final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
                    final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

                    DatabaseReference user_message_push = mRootRef.child("messages")
                            .child(mCurrentUserId).child(mChatUser).push();

                    final String push_id = user_message_push.getKey();

                    StorageReference filepath = mImageStorage.child("message_images").child( push_id + ".jpg");

                    UploadTask uploadTask = filepath.putBytes(thumb_byte);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                            // ...
                            String download_url = taskSnapshot.getDownloadUrl().toString();

                            Map messageMap = new HashMap();
                            messageMap.put("message", download_url);
                            messageMap.put("seen", false);
                            messageMap.put("type", "image");
                            messageMap.put("time", ServerValue.TIMESTAMP);
                            messageMap.put("from", mCurrentUserId);

                            Map messageUserMap = new HashMap();
                            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                    if(databaseError != null){

                                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                    }

                                }
                            });

                        }
                    });
                }
                else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    error.printStackTrace();
                }
            // Uri imageUri = data.getData();




            //File thumb_message_file_image_path = new File(data.getData().getPath());

            /*Log.d("Path to image: ", data.getData().getPath());

            thumb_image_sent_bitmap = new Compressor(this)
                    .setQuality(75)
                    .compressToBitmap(thumb_message_file_image_path);

            Log.d("bitmap", thumb_image_sent_bitmap.toString());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumb_image_sent_bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
            byte[] thumb_byte = baos.toByteArray();

            // Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(.toByteArray())); */



            /* filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){

                        String download_url = task.getResult().getDownloadUrl().toString();


                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if(databaseError != null){

                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                }

                            }
                        });


                    }

                }
            }); */

        }

    }


    private void sendMessage() {

        String message = txtMessage.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {

            Toast.makeText(ChatActivity.this,"Empty message cannot be sent",Toast.LENGTH_LONG).show();
        }
        else {

            String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (databaseError != null) {

                        Log.d("CHAT_LOG",databaseError.getMessage().toString());
                    }
                    else {

                        // loadMessages();

                        // mRefreshLayout.setRefreshing(true);
                    }
                }
            });

        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public CircleImageView profileImage;
        public TextView messageOwnerName;
        public TextView msgTime;
        public TextView messageText;
        public ImageView messageImage;

        public MessageViewHolder(View view) {
            super(view);

            mView = view;
            messageText = (TextView) mView.findViewById(R.id.txtMessage);
            messageImage = (ImageView) mView.findViewById(R.id.ChatConversationImage);
        }

        public void setThumbImage(String thumbImage, Context context) {

            profileImage = (CircleImageView) mView.findViewById(R.id.circleImageView);
            new DownloadImage(context).execute(thumbImage);
        }

        public void setName(String name) {

            messageOwnerName = (TextView) mView.findViewById(R.id.txtMsgOwnerName);
            messageOwnerName.setText(name);
        }

        public void setTime(long time) {

            msgTime = (TextView) mView.findViewById(R.id.txtTime);

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(time);
            SimpleDateFormat fmt = new SimpleDateFormat("hh:mm a - dd/MM/yyyy", Locale.US);
            String sentMsgTime = fmt.format(cal.getTime());

            msgTime.setText("  |   " + sentMsgTime);
        }

        public void setMessageText(String message, String message_type, String from_user, String current_uid) {

            if(message_type.equals("text")) {

                messageText.setVisibility(View.VISIBLE);
                messageImage.setVisibility(View.GONE);
                messageText.setText(message);

                if (from_user.equals(current_uid)) {

                    messageText.setBackgroundResource(R.drawable.message_bg_color);
                    messageText.setTextColor(Color.WHITE);
                }
                else {

                    messageText.setBackgroundResource(R.drawable.received_msg_bg_color);
                    messageText.setTextColor(Color.BLACK);
                }

            }

        }

        public void setMessageImage(String msgImage, String message_type, Context context) {

            if(message_type.equals("image")) {

                messageText.setVisibility(View.GONE);
                messageImage.setVisibility(View.VISIBLE);
                new DownloadSentImage(context).execute(msgImage);
            }

        }



        private void setImage(Drawable drawable, Context context)
        {

            // profileImage.setImageDrawable(drawable);
            if (!context.equals(null)) {

                Glide.with(context)
                        .load(drawable)
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                        .into(profileImage);
            }

        }



        public void setSentImage(Drawable sentImage, Context context) {

            // messageImage.setImageDrawable(sentImage);
            if (!context.equals(null)) {

                Glide.with(context)
                        .load(sentImage)
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                        .into(messageImage);
            }

        }










        public class DownloadImage extends AsyncTask<String, Integer, Drawable> {

            Context context;

            public DownloadImage(Context context) {

                this.context = context;
            }

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
                setImage(image, context);
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
                    /*BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap bMap = BitmapFactory.decodeStream(buf, null, options);  */
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








        public class DownloadSentImage extends AsyncTask<String, Integer, Drawable> {

            Context context;

            public DownloadSentImage(Context context) {

                this.context = context;
            }

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
                setSentImage(image, context);
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
