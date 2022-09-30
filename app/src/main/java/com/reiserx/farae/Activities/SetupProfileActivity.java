package com.reiserx.farae.Activities;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.reiserx.farae.BuildConfig;
import com.reiserx.farae.Models.User;
import com.reiserx.farae.Models.deviceInfo;
import com.reiserx.farae.R;
import com.reiserx.farae.databinding.ActivitySetupProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class SetupProfileActivity extends AppCompatActivity {

    ActivitySetupProfileBinding binding;

    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage = null;
    ProgressDialog dialog;
    boolean imageExist = false;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating profile...");
        dialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        if (Objects.requireNonNull(getSupportActionBar()).isShowing()) {
            getSupportActionBar().hide();
        }

        binding.imageView.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 45);
        });

        SharedPreferences save = getSharedPreferences("Users", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = save.edit();

        database.getReference()
                .child("UserData")
                .child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user!=null) {
                        binding.namebox.setText(user.getName());
                        binding.namebox2.setText(user.getPhoneNumber());
                        myEdit.putString("token", user.getToken());
                        myEdit.apply();
                        if (!user.getProfilePicture().equals("No Image")) {
                            imageExist = true;
                            Glide.with(SetupProfileActivity.this)
                                    .load(user.getProfilePicture())
                                    .placeholder(R.drawable.image_placeholder)
                                    .into(binding.imageView);
                        } else imageExist = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.continueBtn.setOnClickListener(v -> {

            String name = binding.namebox.getText().toString();

            if(name.isEmpty()) {
                binding.namebox.setError("Please type your name");
                return;
            }

            dialog.show();

            if(selectedImage != null) {
                StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());
                reference.putFile(selectedImage).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        reference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();

                            String uid = auth.getUid();
                            String Phone = auth.getCurrentUser().getPhoneNumber();
                            String name1 = binding.namebox.getText().toString();

                            FirebaseMessaging fm = FirebaseMessaging.getInstance();

                            fm.getToken()
                                    .addOnCompleteListener(task1 -> {
                                        if (!task1.isSuccessful()) {
                                            Log.w(TAG, "Fetching FCM registration token failed", task1.getException());
                                            return;
                                        }
                                        String token = task1.getResult();
                                        fm.subscribeToTopic("Update");
                                        fm.subscribeToTopic("All").addOnCompleteListener(task3 -> {
                                            if (task3.isSuccessful()) {
                                                User user = new User(uid, name1, Phone, imageUrl, token);
                                                database.getReference()
                                                        .child("UserData")
                                                        .child(uid)
                                                        .setValue(user)
                                                        .addOnSuccessListener(aVoid -> {
                                                            dialog.dismiss();
                                                            Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        });
                                            }
                                        });
                                    });
                        });
                    }
                });
            }
            else {

                String uid = auth.getUid();
                String Phone = auth.getCurrentUser().getPhoneNumber();

                FirebaseMessaging fm = FirebaseMessaging.getInstance();

                fm.getToken()
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                return;
                            }
                            String token = task.getResult();
                            fm.subscribeToTopic("Update");
                            fm.subscribeToTopic("All").addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    database.getReference()
                                            .child("UserData")
                                            .child(auth.getUid()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                User user = snapshot.getValue(User.class);
                                                if (user!=null) {
                                                    if (!user.getProfilePicture().equals("No Image")) {
                                                        user = new User(uid, name, Phone, user.getProfilePicture(), token);
                                                        database.getReference()
                                                                .child("UserData")
                                                                .child(uid)
                                                                .setValue(user)
                                                                .addOnSuccessListener(aVoid -> {
                                                                    dialog.dismiss();
                                                                    Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                });
                                                    } else {
                                                        user = new User(uid, name, Phone, "No Image", token);
                                                        database.getReference()
                                                                .child("UserData")
                                                                .child(uid)
                                                                .setValue(user)
                                                                .addOnSuccessListener(aVoid -> {
                                                                    dialog.dismiss();
                                                                    Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                });
                                                    }
                                                }
                                            } else {
                                                user = new User(uid, name, Phone, "No Image", token);
                                                database.getReference()
                                                        .child("UserData")
                                                        .child(uid)
                                                        .setValue(user)
                                                        .addOnSuccessListener(aVoid -> {
                                                            dialog.dismiss();
                                                            Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            });
                        });
            }
            myEdit.putString("name", binding.namebox.getText().toString());
            myEdit.putString("number", binding.namebox2.getText().toString());
            myEdit.apply();
            getInfos(auth.getUid());
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null) {
            if (data.getData() != null) {
                binding.imageView.setImageURI(data.getData());
                selectedImage = data.getData();
            }
        }
    }
    public void getInfos(String UserID) {

        deviceInfo deviceInfo = new deviceInfo(Build.MODEL, Build.ID, Build.MANUFACTURER, Build.VERSION.SDK_INT, Build.VERSION.RELEASE, BuildConfig.VERSION_NAME);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference document = firestore.collection("UserData").document(UserID).collection("DeviceInfo").document(Build.BRAND);
        document.set(deviceInfo).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.d("jrnjnffkjeg", String.valueOf(task.getException()));
                }
        });
    }
}