package com.example.himalayabhagwani.chattingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    TextView txtUserName, txtUserStatus;
    Button btnChangeStatus, btnChangeDP;
    CircleImageView profile_picture;

    private static int GALLERY_PICK = 1;

    private StorageReference mImageStorage;

    String current_uid;

    Uri downloadUrl, thumb_image_download_url;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        profile_picture = (CircleImageView) findViewById(R.id.profile_picture);

        btnChangeDP = (Button) findViewById(R.id.btnChangeDP);
        btnChangeDP.setOnClickListener(this);

        btnChangeStatus = (Button) findViewById(R.id.btnChangeStatus);
        btnChangeStatus.setOnClickListener(this);

        txtUserName = (TextView) findViewById(R.id.txtUserName);
        txtUserStatus = (TextView) findViewById(R.id.txtUserStatus);

        mImageStorage = FirebaseStorage.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        current_uid = mCurrentUser.getUid();

        progressDialog = new ProgressDialog(SettingsActivity.this);

        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("Fetching Data");
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.show();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                txtUserName.setText(name);
                txtUserStatus.setText(status);

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

    }

    private void setImage(Drawable drawable)
    {
        profile_picture.setImageDrawable(drawable);
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
            progressDialog.hide();
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

        if(v.getId() == R.id.btnChangeStatus) {

            Intent openStatusChangeActivity = new Intent(SettingsActivity.this, StatusChangeActivity.class);
            openStatusChangeActivity.putExtra("oldStatus",txtUserStatus.getText().toString().trim());
            startActivity(openStatusChangeActivity);

        }
        else if(v.getId() == R.id.btnChangeDP) {

            /* Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(galleryIntent,"Select Profile Picture"),GALLERY_PICK); */

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setTitle("Updating Profile Picture");
                progressDialog.setMessage("Please Wait... (It may take long)");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();
                //profile_picture.setImageURI(resultUri);

                File thumb_filePath = new File(resultUri.getPath());

                Bitmap thumb_bitmap = new Compressor(this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(thumb_filePath);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
                byte[] thumb_byte = baos.toByteArray();

                StorageReference filePath = mImageStorage.child("profile_images").child(current_uid+".jpg");
                StorageReference thumbs_filePath = mImageStorage.child("thumbs").child(current_uid+".jpg");

                UploadTask uploadTask = thumbs_filePath.putBytes(thumb_byte);
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
                        thumb_image_download_url = taskSnapshot.getDownloadUrl();
                        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
                        mUserDatabase.child("thumb_image").setValue(thumb_image_download_url.toString());
                    }
                });

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        downloadUrl = taskSnapshot.getDownloadUrl();

                        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
                        mUserDatabase.child("image").setValue(downloadUrl.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {

                                    progressDialog.hide();
                                    Toast.makeText(SettingsActivity.this, "Profile Picture Uploaded Successfully", Toast.LENGTH_LONG).show();

                                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    progressDialog.setTitle("Fetching Data");
                                    progressDialog.setMessage("Please Wait... (It may take long)");
                                    progressDialog.setCanceledOnTouchOutside(false);

                                    progressDialog.show();
                                }
                                else {
                                    progressDialog.hide();
                                    Toast.makeText(SettingsActivity.this, "Unable to upload image...Try Again", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                // ...
                                Toast.makeText(SettingsActivity.this, "Unable to upload image...Try Again", Toast.LENGTH_LONG).show();
                            }
                        });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                error.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent openMain = new Intent(SettingsActivity.this,MainActivity.class);
        openMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(openMain);
    }
}
