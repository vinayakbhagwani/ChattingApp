package com.example.himalayabhagwani.chattingapp;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnReg, btnLoginStartActivity;

    MediaPlayer mp;

    SurfaceView sv;

    SurfaceHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btnReg = (Button) findViewById(R.id.btnReg);
        btnReg.setOnClickListener(this);

        btnLoginStartActivity = (Button) findViewById(R.id.btnLoginStartActivity);
        btnLoginStartActivity.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mp!=null)
        {
            mp.stop();
            mp.release();
            mp = null;
        }

        mp = MediaPlayer.create(this, R.raw.bg_video_chatting_app);
        mp.setVolume(0,0);
        mp.setLooping(true);

        sv = (SurfaceView) findViewById(R.id.surfaceView_playing_video);
        holder = sv.getHolder();
        holder.addCallback(new SurfaceHolder.Callback(){
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mp.setDisplay(holder);
                mp.start();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.btnReg)
        {
            Intent register = new Intent(StartActivity.this, RegisterActivity.class);
            startActivity(register);
        }
        else if(v.getId() == R.id.btnLoginStartActivity) {
            Intent login = new Intent(StartActivity.this, LoginActivity.class);
            startActivity(login);
        }

    }
}
