package com.example.himalayabhagwani.chattingapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class ChattingApp {

    DatabaseReference mUsersDatabase;
    FirebaseAuth mAuth;

    ValueEventListener listener;

    public void onCreate() {


        mAuth = FirebaseAuth.getInstance();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        listener = mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot!= null) {

                    mUsersDatabase.child("online").onDisconnect().setValue(false);
                    mUsersDatabase.child("lastSeen").onDisconnect().setValue(ServerValue.TIMESTAMP);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {


            }
        });
    }

    public void removeListener() {

        mUsersDatabase.removeEventListener(listener);
    }
}
