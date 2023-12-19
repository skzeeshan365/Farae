package com.reiserx.farae.Activities;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.reiserx.farae.Adapters.UsersAdapter;
import com.reiserx.farae.BuildConfig;
import com.reiserx.farae.Classes.DBHandler;
import com.reiserx.farae.Classes.ExceptionHandler;
import com.reiserx.farae.Models.User;
import com.reiserx.farae.Models.deviceInfo;
import com.reiserx.farae.R;
import com.reiserx.farae.Receivers.AlarmReceiver;
import com.reiserx.farae.Utilities.checkUpdate;
import com.reiserx.farae.Utilities.networkUtils;
import com.reiserx.farae.Utilities.updateFromDeveloper;
import com.reiserx.farae.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Angle;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.Spread;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    FirebaseDatabase database;
    ArrayList<User> users;
    UsersAdapter usersAdapter;
    boolean results = false;

    private static final int MULTIPLE_PERMISSIONS = 123;
    String s = "+91";
    String TAG = "Cghgyg";

    private DBHandler dbHandler;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String[] permissions;

        dbHandler = new DBHandler(MainActivity.this);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            permissions = new String[]{
                    READ_CONTACTS};
        } else {
            permissions = new String[]{
                    READ_CONTACTS,
                    WRITE_EXTERNAL_STORAGE,
                    READ_EXTERNAL_STORAGE};
        }
        if (checkPerm(permissions)) {
            try {
                checkUpdate checkUpdate = new checkUpdate();
                checkUpdate.check(this);

                AlarmReceiver alarm = new AlarmReceiver();
                alarm.setAlarm(this);

                if (!dbHandler.tableExist("rooms")) {
                    dbHandler.createRoomTable();
                }
                dbHandler.createUserTable();
                dbHandler.createLastMessageTable();

                database = FirebaseDatabase.getInstance();
                users = new ArrayList<>();
                usersAdapter = new UsersAdapter(this, users);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                binding.recyclerView.setLayoutManager(layoutManager);
                binding.recyclerView.setNestedScrollingEnabled(false);
                binding.recyclerView.setAdapter(usersAdapter);

                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());

                executor.execute(() -> {
                    cursor = dbHandler.getUser();
                    if (cursor.moveToFirst()) {
                        do {
                            users.add(new User(cursor.getString(0),
                                    cursor.getString(3),
                                    cursor.getString(1),
                                    cursor.getString(2),
                                    cursor.getString(4)));
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                    handler.post(() -> usersAdapter.notifyDataSetChanged());
                });

                if (new networkUtils(this).getNetwork()) {
                    database.getReference().child("UserData").addValueEventListener(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                User user = snapshot1.getValue(User.class);
                                if (user != null && !user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                                    if (contactExists(user.getPhoneNumber())) {
                                        if (!dbHandler.CheckIsDataAlreadyInDBorNot("users", "uid", user.getUid())) {
                                            dbHandler.AddUser(user);
                                        }
                                    }
                                }
                            }
                            executor.execute(() -> {
                                cursor = dbHandler.getUser();
                                users.clear();
                                if (cursor.moveToFirst()) {
                                    do {
                                        users.add(new User(cursor.getString(0),
                                                cursor.getString(3),
                                                cursor.getString(1),
                                                cursor.getString(2),
                                                cursor.getString(4)));
                                    } while (cursor.moveToNext());
                                }
                                cursor.close();
                                handler.post(() -> usersAdapter.notifyDataSetChanged());
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                String currentId = FirebaseAuth.getInstance().getUid();
                updatePersistence(currentId);
                updateFromDeveloper update = new updateFromDeveloper();
                update.announcement(MainActivity.this);
                getInfos(FirebaseAuth.getInstance().getUid());

                remort();
            } catch (Exception e) {
                ExceptionHandler exceptionHandler = new ExceptionHandler(e, FirebaseAuth.getInstance().getUid());
                exceptionHandler.upload();
            }
        } else {
            checkBothPer();
        }
    }

    public Boolean contactExists(String number) {
        if (number != null) {
            ContentResolver cr = getContentResolver();
            @SuppressLint("Recycle") Cursor curContacts = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

            while (curContacts.moveToNext()) {
                @SuppressLint("Range") String contactNumber = curContacts.getString(curContacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                if (contactNumber.replace(" ", "").startsWith(s)) {
                    if (contactNumber.replace(" ", "").equals(number)) {
                        return true;
                    }
                } else {
                    if ((s + contactNumber).replace(" ", "").equals(number)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkPerm(String[] permissions) {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();

        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(getApplicationContext(), p);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    results = false;
                } else results = true;
            }
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
                results = false;
            } else results = true;
        }

        return results;
    }

    private void checkPermissions(String[] permissions) {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();

        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(getApplicationContext(), p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
        }
    }

    public void checkBothPer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Log.d("hvbdhvbdhbv", String.valueOf(checkStoragePermission()));
            if (!checkStoragePermission()) {
                reqStoragePermission();
                Log.d("hvbdhvbdhbv", "hgfytfytftiyfty");
            } else {
                String[] permissions = new String[]{
                        READ_CONTACTS};
                checkPermissions(permissions);
            }
        } else {
            String[] permissions = new String[]{
                    READ_CONTACTS,
                    WRITE_EXTERNAL_STORAGE,
                    READ_EXTERNAL_STORAGE,
            };
            checkPermissions(permissions);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private boolean checkStoragePermission() {
        return Environment.isExternalStorageManager();
    }

    private void reqStoragePermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database = FirebaseDatabase.getInstance();
        if (currentId != null) {
            database.getReference().child("Presence").child(currentId).setValue("Online");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database = FirebaseDatabase.getInstance();
        if (currentId != null) {
            database.getReference().child("Presence").child(currentId).setValue("Offline");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser != null) {
                    auth.signOut();
                    finishAffinity();
                }
                Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
                break;
            case R.id.profile:
                i = new Intent(this, SetupProfileActivity.class);
                startActivity(i);
                break;
            case R.id.check_update:
                i = new Intent(this, updateApp.class);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    users = new ArrayList<>();
                    usersAdapter = new UsersAdapter(this, users);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                    binding.recyclerView.setLayoutManager(layoutManager);
                    binding.recyclerView.setNestedScrollingEnabled(false);
                    binding.recyclerView.setAdapter(usersAdapter);

                    if (!dbHandler.tableExist("rooms")) {
                        dbHandler.createRoomTable();
                    }
                    dbHandler.createUserTable();
                    dbHandler.createLastMessageTable();

                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());
                    database.getReference().child("UserData").addValueEventListener(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                User user = snapshot1.getValue(User.class);
                                if (user != null && !user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                                    if (contactExists(user.getPhoneNumber())) {
                                        if (!dbHandler.CheckIsDataAlreadyInDBorNot("users", "uid", user.getUid())) {
                                            dbHandler.AddUser(user);
                                            Log.d(TAG, "added");
                                        } else Log.d(TAG, "exist");
                                    }
                                }
                            }
                            executor.execute(() -> {
                                cursor = dbHandler.getUser();
                                if (cursor != null && cursor.moveToFirst()) {
                                    do {
                                        users.add(new User(cursor.getString(0),
                                                cursor.getString(3),
                                                cursor.getString(1),
                                                cursor.getString(2),
                                                cursor.getString(4)));
                                    } while (cursor.moveToNext());
                                }
                                cursor.close();
                                handler.post(() -> usersAdapter.notifyDataSetChanged());
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    String perStr = "";
                    for (String per : permissions) {
                        perStr += "\n" + per;
                    }   // permissions list of don't granted permission
                }
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case 2296:
                if (SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        checkBothPer();
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void updatePersistence(String uid) {
        DatabaseReference presenceRef = FirebaseDatabase.getInstance().getReference().child("Presence").child(uid);
        presenceRef.onDisconnect().setValue("Offline");
        networkUtils networkUtils = new networkUtils(this);
        if (networkUtils.getNetwork()) {
            presenceRef.setValue("Online");
        } else {
            presenceRef.onDisconnect().setValue("Offline");
        }
    }

    public void remort() {

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        Log.d(TAG, "1");

        mFirebaseRemoteConfig.fetchAndActivate().addOnSuccessListener(aBoolean -> {
            Log.d(TAG, "2");
            if (mFirebaseRemoteConfig.getBoolean("isBDS")) {
                String title = mFirebaseRemoteConfig.getString("title");
                setTitle(title);
                binding.konfettiView2.setVisibility(View.VISIBLE);
                rain();
                SharedPreferences save = getSharedPreferences("remote", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = save.edit();

                if (save.getInt("id", 0) != mFirebaseRemoteConfig.getLong("id")) {
                    Intent intent = new Intent(MainActivity.this, Splash.class);
                    Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this,
                            android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                    intent.putExtra("message_1", mFirebaseRemoteConfig.getString("Message_1"));
                    intent.putExtra("message_2", mFirebaseRemoteConfig.getString("Message_2"));
                    intent.putExtra("message_3", mFirebaseRemoteConfig.getString("Message_3"));
                    intent.putExtra("message_4", mFirebaseRemoteConfig.getString("Message_4"));
                    startActivity(intent, bundle);
                    myEdit.putInt("id", (int) mFirebaseRemoteConfig.getLong("id"));
                    myEdit.apply();
                }
            } else {
                binding.konfettiView2.setVisibility(View.GONE);
            }
        });
    }

    public void rain() {
        EmitterConfig emitterConfig = new Emitter(1000000, TimeUnit.SECONDS).perSecond(100);
        binding.konfettiView2.start(
                new PartyFactory(emitterConfig)
                        .angle(Angle.BOTTOM)
                        .spread(Spread.ROUND)
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .setSpeedBetween(0f, 15f)
                        .position(new Position.Relative(0.0, 0.0).between(new Position.Relative(1.0, 0.0)))
                        .build()
        );
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