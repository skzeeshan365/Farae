package com.reiserx.farae.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.reiserx.farae.Utilities.fileDownloader;
import com.reiserx.farae.databinding.ActivityUpdateAppBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class updateApp extends AppCompatActivity {

    ActivityUpdateAppBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseStorage storage = FirebaseStorage.getInstance();

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        database.getReference("Administration").child("App").child("Update").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String updateAp = snapshot.getValue(String.class);
                    if (updateAp != null) {
                        binding.version.setText("Version ".concat(updateAp));
                        if (updateAp.equals(com.reiserx.farae.BuildConfig.VERSION_NAME)) {
                            binding.button5.setText("Installed");
                            binding.button5.setEnabled(false);
                            binding.update.setText("Latest version is already installed");
                        } else {
                            binding.update.setText("Update available");
                            binding.button5.setText("Install");
                            binding.button5.setEnabled(true);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.button5.setOnClickListener(view -> {
            Toast.makeText(this, "preparing...", Toast.LENGTH_SHORT).show();
            database.getReference("Administration").child("App").child("Link").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String link = snapshot.getValue(String.class);
                        if (link != null) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                            startActivity(browserIntent);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
    }
}