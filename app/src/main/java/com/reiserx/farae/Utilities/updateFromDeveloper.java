package com.reiserx.farae.Utilities;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.reiserx.farae.Models.mail;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class updateFromDeveloper {

    public void announcement(Context context) {
        SharedPreferences save = context.getSharedPreferences("Announcement", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = save.edit();

        FirebaseDatabase.getInstance().getReference("Administration").child("Mail").child("From developer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    mail mail = snapshot.getValue(mail.class);
                    if (mail != null) {
                        if (save.getInt("id", 0) != mail.getId()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle(mail.getTitle());
                            builder.setMessage(mail.getMessage());
                            builder.setPositiveButton("ok", null);
                            builder.show();
                            myEdit.putInt("id", mail.getId());
                            myEdit.apply();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
