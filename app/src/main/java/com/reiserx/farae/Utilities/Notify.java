package com.reiserx.farae.Utilities;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.reiserx.farae.Classes.DBHandler;
import com.reiserx.farae.Models.User;
import com.reiserx.farae.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Notify {

    String TAG = "jkfhsb";

    public void notif(String title, String msg, String fcmToken, Context context, String number, String room, String messageID, String profilePic, String senderUid, String senderToken, String rec_uid) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "jnfijfnfjsn");
        executor.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject json = new JSONObject();
                JSONObject dataJson = new JSONObject();
                dataJson.put("title", title);
                dataJson.put("content", msg);
                dataJson.put("id", String.valueOf(getRandom(0, 100)));
                dataJson.put("number", number);
                dataJson.put("room", room);
                dataJson.put("messageID", messageID);
                dataJson.put("ProfilePic", profilePic);
                dataJson.put("Uid", senderUid);
                dataJson.put("token", senderToken);
                json.put("data", dataJson);
                json.put("to", fcmToken);
                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());
                Request request = new Request.Builder()
                        .header("Authorization", KEYS.FCM_KEY)
                        .url(context.getString(R.string.fcm))
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                String res = Objects.requireNonNull(response.body()).string();
                Log.d(TAG, res);
                if (res.contains("NotRegistered")) {
                    Log.d(TAG, "adding");
                    FirebaseDatabase.getInstance().getReference().child("UserData").child(rec_uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Log.d(TAG, "exist");
                                User user = snapshot.getValue(User.class);
                                if (user != null) {
                                    DBHandler dbHandler = new DBHandler(context);
                                    dbHandler.updateUser(user);
                                    Log.d(TAG, "updated");
                                }
                            } Log.d(TAG, "not exist");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            } catch (Exception e) {
                Log.d(TAG, e.toString());
                Log.d(TAG, e.getMessage());
            }
            handler.post(() -> {
        });
    });
}

    public static int getRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }
}
