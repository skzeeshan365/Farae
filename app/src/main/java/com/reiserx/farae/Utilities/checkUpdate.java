package com.reiserx.farae.Utilities;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.reiserx.farae.Activities.updateApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class checkUpdate {

    public void check(Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        database.getReference("Administration").child("App").child("Update").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String updateAp = snapshot.getValue(String.class);
                    if (updateAp != null && !updateAp.equals(com.reiserx.farae.BuildConfig.VERSION_NAME)) {
                        Intent i = new Intent(context, updateApp.class);
                        context.startActivity(i);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
