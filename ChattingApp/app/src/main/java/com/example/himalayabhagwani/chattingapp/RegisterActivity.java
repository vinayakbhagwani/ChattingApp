package com.example.himalayabhagwani.chattingapp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    EditText txtName, txtEmail, txtPass;
    TextInputLayout nameLayout, emailLayout, passLayout;
    Button btnSubmit;
    private FirebaseAuth mAuth;

    Toolbar mToolbar;

    private ImageView Img;

    private DatabaseReference mDatabase;

    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        Img=findViewById(R.id.ImgV);
        Img.setVisibility(View.GONE);

        mToolbar = (Toolbar) findViewById(R.id.reg_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtName = (EditText) findViewById(R.id.txtName);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPass = (EditText) findViewById(R.id.txtPass);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(this);

        nameLayout = (TextInputLayout) findViewById(R.id.nameLayout);
        emailLayout = (TextInputLayout) findViewById(R.id.emailLayout);
        passLayout = (TextInputLayout) findViewById(R.id.passLayout);

        mToolbar.setVisibility(View.VISIBLE);

        nameLayout.setVisibility(View.VISIBLE);
        emailLayout.setVisibility(View.VISIBLE);
        passLayout.setVisibility(View.VISIBLE);

        txtName.setVisibility(View.VISIBLE);
        txtEmail.setVisibility(View.VISIBLE);
        txtPass.setVisibility(View.VISIBLE);
        btnSubmit.setVisibility(View.VISIBLE);

        // nameLayout.setErrorEnabled(true);
        // nameLayout.setError("Not allowed to be left null");

        // emailLayout.setErrorEnabled(true);
        // emailLayout.setError("Not allowed to be left null");

        // passLayout.setErrorEnabled(true);
        // passLayout.setError("Not allowed to be left null");

    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.btnSubmit)
        {
            if(TextUtils.isEmpty(txtName.getText().toString().trim())) {
                txtName.setError("Required");
            }
            else if(TextUtils.isEmpty(txtEmail.getText().toString().trim())) {
                txtEmail.setError("Required");
            }
            else if(TextUtils.isEmpty(txtPass.getText().toString().trim())) {
                txtPass.setError("Required");
            }
            else {
                name = txtName.getText().toString().trim();
                String email = txtEmail.getText().toString().trim();
                String pass = txtPass.getText().toString().trim();


                Glide.with(getApplicationContext()).load(R.drawable.reg_dialog).into(Img);
                Img.setVisibility(View.VISIBLE);

                mToolbar.setVisibility(View.INVISIBLE);

                nameLayout.setVisibility(View.INVISIBLE);
                emailLayout.setVisibility(View.INVISIBLE);
                passLayout.setVisibility(View.INVISIBLE);

                txtName.setVisibility(View.INVISIBLE);
                txtEmail.setVisibility(View.INVISIBLE);
                txtPass.setVisibility(View.INVISIBLE);
                btnSubmit.setVisibility(View.INVISIBLE);

                mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful())
                        {
                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = current_user.getUid();
                            Log.d("uid",uid);
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            HashMap<String,String> userMap = new HashMap<>();
                            userMap.put("device_token", FirebaseInstanceId.getInstance().getToken());
                            userMap.put("name",name);
                            userMap.put("status","Hey there... I'm using Chatting App");
                            userMap.put("image","default_dp");
                            userMap.put("thumb_image","default_thumb_image");

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()) {

                                        Img.setVisibility(View.GONE);
                                        Intent openMain = new Intent(RegisterActivity.this,MainActivity.class);
                                        openMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_LONG).show();
                                        startActivity(openMain);
                                    }
                                    else {
                                        Img.setVisibility(View.GONE);

                                        mToolbar.setVisibility(View.VISIBLE);

                                        nameLayout.setVisibility(View.VISIBLE);
                                        emailLayout.setVisibility(View.VISIBLE);
                                        passLayout.setVisibility(View.VISIBLE);

                                        txtName.setVisibility(View.VISIBLE);
                                        txtEmail.setVisibility(View.VISIBLE);
                                        txtPass.setVisibility(View.VISIBLE);
                                        btnSubmit.setVisibility(View.VISIBLE);

                                        Toast.makeText(RegisterActivity.this, "Unable to sign up..Try Again..!! \n Check Email-id and Internet Connection", Toast.LENGTH_LONG).show();

                                    }

                                }
                            });

                        }
                        else {
                            Img.setVisibility(View.GONE);

                            mToolbar.setVisibility(View.VISIBLE);

                            nameLayout.setVisibility(View.VISIBLE);
                            emailLayout.setVisibility(View.VISIBLE);
                            passLayout.setVisibility(View.VISIBLE);

                            txtName.setVisibility(View.VISIBLE);
                            txtEmail.setVisibility(View.VISIBLE);
                            txtPass.setVisibility(View.VISIBLE);
                            btnSubmit.setVisibility(View.VISIBLE);

                            Toast.makeText(RegisterActivity.this, "Unable to sign up..Try Again..!! \n Check Email-id and Internet Connection", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

        }

    }



}
