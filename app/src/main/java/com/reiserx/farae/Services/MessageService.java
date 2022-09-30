package com.reiserx.farae.Services;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.reiserx.farae.Classes.DBHandler;
import com.reiserx.farae.Classes.ExceptionHandler;
import com.reiserx.farae.Models.Keys;
import com.reiserx.farae.Models.Message;
import com.reiserx.farae.Receivers.AlarmReceiver;
import com.reiserx.farae.Receivers.DatabaseChangedReceiver;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class MessageService extends Service {

    String TAG = "MessageService";
    private String UserID;
    private DBHandler dbHandler;
    private ArrayList<Keys> keys;

    ChildEventListener childEventListener;

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (getMemory()) {
            //Firebase initialization
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
            }
            Calendar c = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") String senttime = new SimpleDateFormat("hh:mm:ss a").format(c.getTime());
            UserID = FirebaseAuth.getInstance().getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference().child("timeStamp").child(UserID).setValue(senttime);
            dbHandler = new DBHandler(this);
            try {

                keys = dbHandler.getAllRooms();

                if (keys != null) {
                    for (int i = 0; i < keys.size(); i++) {
                        Keys keys1 = keys.get(i);
                        DatabaseReference references = database.getReference().child("Messages").child(keys1.getRoom()).child("Messages");
                        if (childEventListener!=null) {
                            references.removeEventListener(childEventListener);
                        }
                        childEventListener = references.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                if (snapshot.exists()) {
                                    Message message = snapshot.getValue(Message.class);
                                    if (message != null && !message.getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
                                        message.setMessageId(snapshot.getKey());
                                        dbHandler.addData(message, keys1.getRoom());
                                        references.child(snapshot.getKey()).removeValue();
                                        Intent i = new Intent(DatabaseChangedReceiver.ACTION_DATABASE_CHANGED);
                                        i.putExtra("requestCode", 1);
                                        sendBroadcast(i);
                                    }
                                }
                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        updateLastMessage(database, keys1.getRoom(), dbHandler);
                        deleteMessage(database, keys1.getRoom());
                    }
                }
                DatabaseReference sender_ref1 = FirebaseDatabase.getInstance().getReference().child("Messages").child("rooms").child(UserID);
                sender_ref1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                Keys key = snapshot1.getValue(Keys.class);
                                if (key != null) {
                                    dbHandler.createRoom(key.getRoom(), key.getEncryptionKey(), key.getUserid());
                                    if (!dbHandler.tableExist(key.getRoom())) {
                                        dbHandler.createMessageTable(key.getRoom());
                                    } else {
                                        Log.d("rgfsgfhfsgs", "table not exists");
                                    }
                                    sender_ref1.child(snapshot1.getKey()).removeValue();
                                } else {
                                    Log.d("rgfsgfhfsgs", "null");
                                }
                            }
                        } else {
                            Log.d("rgfsgfhfsgs", "snap not exists");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } catch (Exception e) {
                ExceptionHandler exceptionHandler = new ExceptionHandler(e, UserID);
                exceptionHandler.upload();
            }
        }
        return START_STICKY;
    }

    public void onTrimMemory(int level) {

        // Determine which lifecycle or system event was raised.
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:

                /*
                   Release any UI objects that currently hold memory.

                   The user interface has moved to the background.
                */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:

                Log.d(TAG, "low");
                Intent in = new Intent(this, MessageService.class);
                this.stopService(in);
                in = null;

                /*
                   Release any memory that your app doesn't need to run.

                   The device is running low on memory while the app is running.
                   The event raised indicates the severity of the memory-related event.
                   If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will
                   begin killing background processes.
                */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:

                Log.d(TAG, "high");
                Intent ins = new Intent(this, MessageService.class);
                this.stopService(ins);

                /*
                   Release as much memory as the process can.

                   The app is on the LRU list and the system is running low on memory.
                   The event raised indicates where the app sits within the LRU list.
                   If the event is TRIM_MEMORY_COMPLETE, the process will be one of
                   the first to be terminated.
                */

                break;

            default:
                /*
                  Release any non-critical data structures.

                  The app received an unrecognized memory level value
                  from the system. Treat this as a generic low-memory message.
                */
                break;
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (getMemory()) {
        } else {
            Log.d(TAG, "onLowMemory");
            Intent in = new Intent(this, MessageService.class);
            this.stopService(in);
        }
    }

    public Boolean getMemory() {

        ActivityManager.MemoryInfo memoryInfo = getAvailableMemory();

        // Do memory intensive work ...
        return !memoryInfo.lowMemory;
    }

    private ActivityManager.MemoryInfo getAvailableMemory() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        sendBroadcast(new Intent("AlarmReceiverMessage"));
        AlarmReceiver alarm = new AlarmReceiver();
        alarm.setAlarm(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding
        return null;
    }

    public void updateLastMessage(FirebaseDatabase db, String room, DBHandler dbHandler) {
        DatabaseReference reference = db.getReference().child("Messages").child(room).child("lastmessage");
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                    String time = snapshot.child("lastMsgTime").getValue(String.class);
                    String senderID = snapshot.child("senderID").getValue(String.class);
                    int status = snapshot.child("status").getValue(int.class);
                    if (lastMsg != null && time != null && senderID != null && status != 0) {
                        dbHandler.addLastMessage(room, senderID, lastMsg, time, status);
                        if (!senderID.equals(UserID) && (status == 1)) {
                            HashMap<String, Object> lastMsgObj = new HashMap<>();
                            lastMsgObj.put("status", 2);
                            reference.updateChildren(lastMsgObj);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void deleteMessage(FirebaseDatabase database, String room) {
        DatabaseReference references = database.getReference().child("Messages").child(room).child("DeletedMessages");
        references.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message != null && !message.getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
                        message.setMessageId(snapshot.getKey());
                        dbHandler.deleteMessages(message, room);
                        references.child(snapshot.getKey()).removeValue();
                        Intent i = new Intent(DatabaseChangedReceiver.ACTION_DATABASE_CHANGED);
                        i.putExtra("requestCode", 2);
                        i.putExtra("messageID", message.getMessageId());
                        sendBroadcast(i);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}