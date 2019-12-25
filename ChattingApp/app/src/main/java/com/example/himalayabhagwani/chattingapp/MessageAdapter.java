package com.example.himalayabhagwani.chattingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    public CircleImageView profileImage;
    public TextView messageOwnerName;
    public TextView msgTime;
    public TextView messageText;
    public ImageView messageImage;

    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    private ValueEventListener listener;

    int flag = 0;

    public MessageAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;
        mAuth = FirebaseAuth.getInstance();

        mUserDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder messageViewHolder, int position) {

        String current_uid = mAuth.getCurrentUser().getUid();

        Messages c = new Messages();

        c = mMessageList.get(position);

        String from_user = c.getFrom();
        String message = c.getMessage();
        long time = c.getTime();
        String message_type = c.getType();

        if(message_type.equals("text")) {

            messageText.setVisibility(View.VISIBLE);
            messageImage.setVisibility(View.GONE);
            messageViewHolder.setMessageText(message);

        } else {

            messageText.setVisibility(View.GONE);
            messageImage.setVisibility(View.VISIBLE);
            messageViewHolder.setMessageImage(message);

        }

        if (from_user.equals(current_uid)) {

            messageText.setBackgroundResource(R.drawable.message_bg_color);
            messageText.setTextColor(Color.WHITE);
        }
        else {

            messageText.setBackgroundResource(R.drawable.received_msg_bg_color);
            messageText.setTextColor(Color.BLACK);
        }

        messageViewHolder.setTime(time);

        mUserDatabase.keepSynced(true);
        listener = mUserDatabase.child("Users").child(from_user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                messageViewHolder.setName(name);

                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                messageViewHolder.setThumbImage(thumb_image);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout ,parent, false);

        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public MessageViewHolder(View view) {
            super(view);

            mView = view;
            messageText = (TextView) mView.findViewById(R.id.txtMessage);
            messageImage = (ImageView) mView.findViewById(R.id.ChatConversationImage);
        }

        public void setThumbImage(String thumbImage) {

            profileImage = (CircleImageView) mView.findViewById(R.id.circleImageView);
            new DownloadImage().execute(thumbImage);
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

        public void setMessageText(String message) {

            messageText.setText(message);
        }

        public void setMessageImage(String msgImage) {

            new DownloadSentImage().execute(msgImage);
        }



        private void setImage(Drawable drawable)
        {

            profileImage.setImageDrawable(drawable);
        }



        public void setSentImage(Drawable sentImage) {

            messageImage.setImageDrawable(sentImage);
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








        public class DownloadSentImage extends AsyncTask<String, Integer, Drawable> {

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
                setSentImage(image);
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
