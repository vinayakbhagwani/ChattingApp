package com.example.himalayabhagwani.chattingapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Toolbar mToolbar;

    EditText txtPass, txtEmail;
    TextInputLayout passLayout, emailLayout;
    Button btnLogin;
    private FirebaseAuth mAuth;

    private ImageView Img;

    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.login_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        Img=findViewById(R.id.ImgV);
        Img.setVisibility(View.GONE);

        txtEmail = (EditText) findViewById(R.id.LoginTxtEmail);
        txtPass = (EditText) findViewById(R.id.LoginTxtPass);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        emailLayout = (TextInputLayout) findViewById(R.id.LoginEmailLayout);
        passLayout = (TextInputLayout) findViewById(R.id.LoginPassLayout);

        mToolbar.setVisibility(View.VISIBLE);

        emailLayout.setVisibility(View.VISIBLE);
        passLayout.setVisibility(View.VISIBLE);

        txtEmail.setVisibility(View.VISIBLE);
        txtPass.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.VISIBLE);

    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.btnLogin)
        {
            if(TextUtils.isEmpty(txtEmail.getText().toString().trim())) {
                txtEmail.setError("Required");
            }
            else if(TextUtils.isEmpty(txtPass.getText().toString().trim())) {
                txtPass.setError("Required");
            }
            else {
                String email = txtEmail.getText().toString().trim();
                String pass = txtPass.getText().toString().trim();

                Glide.with(getApplicationContext()).load(R.drawable.reg_dialog).into(Img);
                Img.setVisibility(View.VISIBLE);

                mToolbar.setVisibility(View.INVISIBLE);

                emailLayout.setVisibility(View.INVISIBLE);
                passLayout.setVisibility(View.INVISIBLE);

                txtEmail.setVisibility(View.INVISIBLE);
                txtPass.setVisibility(View.INVISIBLE);
                btnLogin.setVisibility(View.INVISIBLE);

                mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful())
                        {
                            String current_user_id = mAuth.getCurrentUser().getUid();

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            mUserDatabase.child(current_user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Img.setVisibility(View.GONE);
                                    Intent openMain = new Intent(LoginActivity.this,MainActivity.class);
                                    openMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(openMain);
                                }
                            });

                        }
                        else {
                            Img.setVisibility(View.GONE);

                            mToolbar.setVisibility(View.VISIBLE);

                            emailLayout.setVisibility(View.VISIBLE);
                            passLayout.setVisibility(View.VISIBLE);

                            txtEmail.setVisibility(View.VISIBLE);
                            txtPass.setVisibility(View.VISIBLE);
                            btnLogin.setVisibility(View.VISIBLE);

                            Toast.makeText(LoginActivity.this, "Unable to log in..Try Again..!!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }

    }

}
