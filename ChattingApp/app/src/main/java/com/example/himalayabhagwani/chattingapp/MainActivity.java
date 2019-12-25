package com.example.himalayabhagwani.chattingapp;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    private ViewPager viewPager;

    private SectionPagerAdapter sectionPagerAdapter;

    private TabLayout tabLayout;

    ChattingApp chattingApp;

    DatabaseReference mUsersDatabase;

    int flag = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Chatting App");

        viewPager = (ViewPager) findViewById(R.id.tabPager);
        sectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(sectionPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            logOut();
        }
        else {

            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mUsersDatabase.child("online").setValue(true);
            chattingApp = new ChattingApp();
            chattingApp.onCreate();

        }

    }

    public void logOut()
    {
        Intent i = new Intent(MainActivity.this,StartActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // so that the user cannot come back to  the MainActivity by using Back Button....if he is not registered he should signUp/signIn first
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.item_Logout)
        {
            chattingApp.removeListener();

            mUsersDatabase.child("online").setValue(false);
            mUsersDatabase.child("lastSeen").setValue(ServerValue.TIMESTAMP);

            mAuth.signOut();

            logOut();
        }
        else if (item.getItemId() == R.id.item_acc_settings)
        {
            Intent openSettings = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(openSettings);
        }
        else if (item.getItemId() == R.id.item_all_users)
        {
            Intent openAllUsers = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(openAllUsers);
        }

        return true;
    }


}
