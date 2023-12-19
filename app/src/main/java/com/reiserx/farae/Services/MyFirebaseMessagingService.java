package com.reiserx.farae.Services;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;

import com.reiserx.farae.Classes.DBHandler;
import com.reiserx.farae.Models.Keys;
import com.reiserx.farae.Receivers.AlarmReceiver;
import com.reiserx.farae.Utilities.JobUtil;
import com.reiserx.farae.Utilities.NotificationUtils;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    String s = "+91";
    String name = "null";
    String TAG = "gffsgdfgrs";
    DBHandler dbHandler;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        remoteMessage.getData();


        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String content = data.get("content");
        String id = data.get("id");

        Log.d(TAG, title);

        JobUtil.scheduleJob(this);
        AlarmReceiver alarm = new AlarmReceiver();
        alarm.setAlarm(this);

        NotificationUtils notificationUtils = new NotificationUtils();

        if (remoteMessage.getFrom().equals("/topics/Update")) {
            notificationUtils.sendUpdateNotification(this, title, content, Integer.parseInt(Objects.requireNonNull(id)));

        } else if (data.get("number")!=null && data.get("room")!=null && data.get("messageID")!=null) {

            String number = data.get("number");
            String names = contactExists(number);

            dbHandler = new DBHandler(this);
            Keys keys = dbHandler.getRoom(data.get("Uid"));
            if (keys!=null) {
                String titles;
                if (names.equals("null")) {
                    titles = number + " " + title;
                } else {
                    titles = names + " " + title;
                }
                notificationUtils.sendNotification(this, titles, content, Integer.parseInt(Objects.requireNonNull(id)), data, names, keys.getEncryptionKey());
            }
        }
    }

    public String contactExists(String number) {
        if (number != null) {
            ContentResolver cr = getContentResolver();
            @SuppressLint("Recycle") Cursor curContacts = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

            while (curContacts.moveToNext()) {
                @SuppressLint("Range") String contactNumber = curContacts.getString(curContacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                @SuppressLint("Range") String display_name = curContacts.getString(curContacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                if (contactNumber.replace(" ", "").startsWith(s)) {
                    if (contactNumber.replace(" ", "").equals(number)) {
                        name = display_name;
                        Log.d("hdsbfhdhius", contactNumber);
                    }
                } else {
                    if ((s+contactNumber).replace(" ", "").equals(number)) {
                        name = display_name;
                        Log.d("hdsbfhdhius", contactNumber);
                    }
                }
            }
        }
        return name;
    }
    public void updateStatus(String room, String messageID) {
        HashMap<String , Object> map = new HashMap<>();
        map.put("status", 2);
        FirebaseDatabase.getInstance().getReference()
                .child("Chats")
                .child(room)
                .child("Messages")
                .child(messageID)
                .updateChildren(map);
    }
}
