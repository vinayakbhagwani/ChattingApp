package com.example.himalayabhagwani.chattingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusChangeActivity extends AppCompatActivity implements View.OnClickListener {

    EditText txtStatus;
    Button btnDone;

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    ProgressDialog progressDialog;

    String oldStatus;

    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_change);

        mToolbar = (Toolbar) findViewById(R.id.status_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Update Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtStatus = (EditText) findViewById(R.id.txtStatus);
        oldStatus = getIntent().getStringExtra("oldStatus");
        txtStatus.setText(oldStatus);

        btnDone = (Button) findViewById(R.id.btnDone);
        btnDone.setOnClickListener(this);

        progressDialog = new ProgressDialog(StatusChangeActivity.this);

    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.btnDone)
        {

            if(TextUtils.isEmpty(txtStatus.getText().toString().trim())) {
                txtStatus.setError("Required");
            }
            else {

                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setTitle("Updating Profile Status");
                progressDialog.setMessage("Please Wait...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                String current_uid = mCurrentUser.getUid();

                mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
                mUserDatabase.child("status").setValue(txtStatus.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(StatusChangeActivity.this, "Status Updated Successfully", Toast.LENGTH_LONG).show();
                            Intent goBack = new Intent(StatusChangeActivity.this, SettingsActivity.class);
                            startActivity(goBack);
                            finish();
                        }
                        else {
                            progressDialog.dismiss();
                            Toast.makeText(StatusChangeActivity.this, "Unable to Update Status... \n Try Again..", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
