package com.reiserx.farae.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.capybaralabs.swipetoreply.SwipeController;
import com.reiserx.farae.Adapters.MessagesAdapter;
import com.reiserx.farae.Classes.DBHandler;
import com.reiserx.farae.Classes.DatabaseListener;
import com.reiserx.farae.Classes.DatabaseObserver;
import com.reiserx.farae.Classes.ExceptionHandler;
import com.reiserx.farae.Models.Message;
import com.reiserx.farae.R;
import com.reiserx.farae.Receivers.DatabaseChangedReceiver;
import com.reiserx.farae.Utilities.BitmapUtility;
import com.reiserx.farae.Utilities.Encryption;
import com.reiserx.farae.Utilities.Notify;
import com.reiserx.farae.Utilities.generateKey;
import com.reiserx.farae.databinding.ActivityChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity implements DatabaseObserver {

    ActivityChatBinding binding;

    MessagesAdapter adapter;
    ArrayList<Message> messages;
    ArrayList<HashMap<String, Object>> listMap = new ArrayList<>();

    String receiverUid;
    String senderUid;
    String room;
    String TAG = "jkfhsb";
    String token;
    String name;
    String sender_name;
    String replyID = "null";
    String replyUId = "null";
    String phoneNumber;
    String myToken;
    String ProfilePic;

    FirebaseDatabase database;
    FirebaseStorage storage;

    Notify notify;

    Encryption encryption;
    private String key;

    ArrayList<Message> dataList, filteredDataList;
    Message message;


    String intentFilter = "com.reiserx.DATABASE_CHANGED";
    DBHandler dbHandler;
    Cursor cursor;

    Bitmap bitmap;

    DatabaseChangedReceiver receiver;
    ValueEventListener statusListener;
    DatabaseReference statusReference;

    LinearLayoutManager layoutManager;

    int pastVisiblesItems, totalItemCount, lastItem;

    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.replyHolder.setVisibility(View.GONE);
        binding.imageView5.setVisibility(View.GONE);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        room = getIntent().getStringExtra("room");
        key = getIntent().getStringExtra("encryptionKey");
        name = getIntent().getStringExtra("Username");
        ProfilePic = getIntent().getStringExtra("ProfilePic");
        receiverUid = getIntent().getStringExtra("Uid");
        senderUid = FirebaseAuth.getInstance().getUid();
        token = getIntent().getStringExtra("token");

        statusReference = FirebaseDatabase.getInstance().getReference().child("Messages").child(room).child("lastmessage");

        SharedPreferences save = getSharedPreferences("Users", MODE_PRIVATE);
        sender_name = save.getString("name", "");
        phoneNumber = save.getString("number", "");
        myToken = save.getString("token", "");

        messages = new ArrayList<>();
        filteredDataList = new ArrayList<>();
        dataList = new ArrayList<>();
        adapter = new MessagesAdapter(this, messages, binding.recyclerView, binding.imageView5, room, senderUid, key, receiverUid, name);
        listMap = new ArrayList<>();

        layoutManager = new LinearLayoutManager(ChatActivity.this);
        layoutManager.setReverseLayout(false);
        layoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);

        dbHandler = new DBHandler(ChatActivity.this);

        DatabaseListener listener = new DatabaseListener(this);

        loadInitialMessages();

        loadRecMessages();

        sear();

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy < 0) { //check for scroll down
                    totalItemCount = layoutManager.getItemCount();
                    pastVisiblesItems = layoutManager.findFirstCompletelyVisibleItemPosition();

                    Log.d(TAG, "first: " + layoutManager.findFirstCompletelyVisibleItemPosition());

                    if (pastVisiblesItems == 0) {
                        if (lastItem != totalItemCount) {
                            try {

                            cursor = dbHandler.paginate(room, totalItemCount);
                            if (cursor.moveToFirst()) {
                                do {
                                    messages.add(0, new Message(cursor.getString(0),
                                            encryption.Decrypt(cursor.getString(1), key),
                                            cursor.getString(2),
                                            cursor.getString(3),
                                            cursor.getString(4),
                                            encryption.Decrypt(cursor.getString(5), key),
                                            cursor.getString(6),
                                            cursor.getString(7),
                                            cursor.getInt(8)));
                                } while (cursor.moveToNext());
                                // moving our cursor to next.
                            }
                            cursor.close();
                            layoutManager.setStackFromEnd(false);
                            binding.recyclerView.setAdapter(adapter);
                            adapter.notifyItemRangeInserted(0, adapter.getItemCount());
                            if (lastItem != totalItemCount && pastVisiblesItems == 0) {
                                layoutManager.scrollToPosition(5);
                            }
                            lastItem = totalItemCount;
                            } catch(Exception e) {
                            ExceptionHandler exceptionHandler = new ExceptionHandler(e, senderUid);
                            exceptionHandler.upload();
                        }
                        } else
                            Toast.makeText(ChatActivity.this, "Loaded all messages", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        encryption = new Encryption();

        notify = new Notify();

        database.getReference().child("Presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.getValue(String.class);
                    if (status != null && !status.isEmpty()) {
                        binding.statuss.setText(status);
                        binding.statuss.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        statusUpdates();

        binding.name.setText(name);
        Glide.with(ChatActivity.this).load(ProfilePic)
                .placeholder(R.drawable.image_placeholder)
                .into(binding.imgUser);

        binding.backButton.setOnClickListener(v -> finish());

        binding.sendButton.setOnClickListener(v -> {
            String MessageTxt = binding.messageBox.getText().toString();

            if (!MessageTxt.equals("")) {
                Calendar c = Calendar.getInstance();
                @SuppressLint("SimpleDateFormat") String senttime = new SimpleDateFormat("hh:mm a").format(c.getTime());
                String randomKey = database.getReference().push().getKey();
                message = new Message(encryption.Encrypt(MessageTxt, key), senderUid, senttime, encryption.Encrypt(binding.replyMsg.getText().toString(), key), replyUId, replyID, 1);
                message.setImageUrl("null");
                HashMap<String, Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("senderID", senderUid);
                lastMsgObj.put("lastMsg", message.getMessage());
                lastMsgObj.put("lastMsgTime", senttime);
                lastMsgObj.put("status", 1);

                FirebaseDatabase.getInstance().getReference().child("Messages").child(room).child("lastmessage").updateChildren(lastMsgObj);

                FirebaseDatabase.getInstance().getReference().child("Messages")
                        .child(room)
                        .child("Messages")
                        .child(randomKey).setValue(message).addOnSuccessListener(unused -> {
                    if (binding.statuss.getText().toString().equals("Offline")) {
                        notify.notif("sent a message", MessageTxt, token, this, phoneNumber, room, randomKey, ProfilePic, senderUid, myToken, receiverUid);
                    }
                });
                message.setMessageId(randomKey);
                dbHandler.addData(message, room);
                listener.update();
                binding.messageBox.setText("");
                binding.replyHolder.setVisibility(View.GONE);
                binding.replyMsg.setText("");
                binding.replyNameTxt.setText("");
                replyID = "null";
                replyUId = "null";
            } else Toast.makeText(this, "Please type a message", Toast.LENGTH_SHORT).show();
        });

        binding.attachImg.setOnClickListener(v -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("Send a photo");
            alert.setPositiveButton("gallery", (dialogInterface, i) -> {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 25);
            });
            alert.setNegativeButton("camera", (dialogInterface, i) -> {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else
                {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 25);
                }
            });
            alert.show();
        });

        final Handler handler = new Handler();
        binding.messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("Presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 1000);
            }

            final Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("Presence").child(senderUid).setValue("Online");
                }
            };

        });
    }

    private void statusUpdates() {
        new Thread(() -> {
            statusListener = statusReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        try {

                        String senderID = snapshot.child("senderID").getValue(String.class);
                        int status = snapshot.child("status").getValue(int.class);
                        if (senderID != null && status != 0) {
                            if (!senderID.equals(senderUid) && (status == 1 || status == 2)) {
                                HashMap<String, Object> lastMsgObj = new HashMap<>();
                                lastMsgObj.put("status", 3);
                                statusReference.updateChildren(lastMsgObj);
                            }
                        }
                    } catch(Exception e) {
                        ExceptionHandler exceptionHandler = new ExceptionHandler(e, senderUid);
                        exceptionHandler.upload();
                    }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 25) {
            if (data != null) {
                if (data.getData() != null) {
                    Uri selectedImage = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    BitmapUtility bitmapUtility = new BitmapUtility();
                    Toast.makeText(this, "sending media", Toast.LENGTH_SHORT).show();
                    generateKey generateKey = new generateKey();
                    String filename = generateKey.randomString(15);
                    try {

                    new Thread(() -> {

                        Calendar c = Calendar.getInstance();
                        @SuppressLint("SimpleDateFormat") String senttime = new SimpleDateFormat("hh:mm a").format(c.getTime());
                        String MessageTxt = binding.messageBox.getText().toString();
                        StorageReference reference = FirebaseStorage.getInstance().getReference().child("Messages").child(room).child("Messages").child(filename);
                        String randomKey = database.getReference().push().getKey();
                        message = new Message(encryption.Encrypt(MessageTxt, key), senderUid, senttime, encryption.Encrypt(binding.replyMsg.getText().toString(), key), replyUId, replyID, 1);
                        reference.putBytes(bitmapUtility.getBytes(bitmap)).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String url = uri.toString();
                                    Log.d(TAG, url);
                                    message.setImageUrl(url);
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("Messages")
                                            .child(room)
                                            .child("Messages")
                                            .child(randomKey)
                                            .setValue(message).addOnSuccessListener(unused -> {
                                        if (binding.statuss.getText().toString().equals("Offline")) {
                                            notify.notif("sent a photo", MessageTxt, token, ChatActivity.this, phoneNumber, room, randomKey, ProfilePic, senderUid, myToken, receiverUid);
                                        }
                                    });
                                    HashMap<String, Object> lastMsgObj = new HashMap<>();
                                    lastMsgObj.put("senderID", senderUid);
                                    lastMsgObj.put("lastMsg", "sent a photo");
                                    lastMsgObj.put("lastMsgTime", senttime);
                                    lastMsgObj.put("status", 1);

                                    database.getReference().child("Messages").child(room).child("lastmessage").updateChildren(lastMsgObj);
                                    message.setMessageId(randomKey);
                                    dbHandler.addData(message, room);
                                    Intent i = new Intent(DatabaseChangedReceiver.ACTION_DATABASE_CHANGED);
                                    i.putExtra("requestCode", 1);
                                    sendBroadcast(i);
                                });
                            }
                        })
                                .addOnProgressListener(snapshot -> {

                                });

                        runOnUiThread(() -> {
                            binding.messageBox.setText("");
                            binding.replyHolder.setVisibility(View.GONE);
                            binding.replyMsg.setText("");
                            binding.replyNameTxt.setText("");
                            replyID = "null";
                            replyUId = "null";
                        });
                    }).start();
                } catch(Exception e) {
                    ExceptionHandler exceptionHandler = new ExceptionHandler(e, senderUid);
                    exceptionHandler.upload();
                }
                }
            }
            if (statusListener == null) {
                statusUpdates();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("Presence").child(currentId).setValue("Online");
        loadRecMessages();
        if (statusListener == null) {
            statusUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("Presence").child(currentId).setValue("Offline");
        unregisterReceiver(receiver);
        if (statusListener != null) {
            statusReference.removeEventListener(statusListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (statusListener != null) {
            statusReference.removeEventListener(statusListener);
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void sear() {

        binding.searchv.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        filteredDataList = filter(dataList, newText);
                        adapter.setFilter(filteredDataList);
                        return false;
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private ArrayList<Message> filter(ArrayList<Message> dataList, String newText) {
        newText = newText.toLowerCase();
        String text;
        filteredDataList = new ArrayList<>();
        for (Message dataFromDataList : dataList) {
            text = dataFromDataList.getMessage().toLowerCase(Locale.ROOT);

            if (text.contains(newText)) {
                filteredDataList.add(dataFromDataList);
            }
        }
        if (filteredDataList.isEmpty()) {
            try {
                cursor = dbHandler.paginate(room, dataList.size()-1);
                if (cursor.moveToFirst()) {
                    do {
                        dataList.add(0, new Message(cursor.getString(0),
                                encryption.Decrypt(cursor.getString(1), key),
                                cursor.getString(2),
                                cursor.getString(3),
                                cursor.getString(4),
                                encryption.Decrypt(cursor.getString(5), key),
                                cursor.getString(6),
                                cursor.getString(7),
                                cursor.getInt(8)));
                    } while (cursor.moveToNext());
                    // moving our cursor to next.
                }
                cursor.close();
                filter(dataList, newText);
            } catch (Exception e) {
                ExceptionHandler exceptionHandler = new ExceptionHandler(e, senderUid);
                exceptionHandler.upload();
            }
        }

        return filteredDataList;
    }

    public void swipeController() {
        @SuppressLint("SetTextI18n") SwipeController controller = new SwipeController(ChatActivity.this, position -> {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (!inputMethodManager.isAcceptingText()) {
                inputMethodManager.toggleSoftInputFromWindow(binding.messageBox.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                binding.messageBox.requestFocus();
            }
            if (binding.replyHolder.getVisibility() == View.VISIBLE) {
                TransitionManager.beginDelayedTransition(binding.cardView,
                        new AutoTransition());
                binding.replyHolder.setVisibility(View.GONE);
            } else {
                TransitionManager.beginDelayedTransition(binding.cardView,
                        new AutoTransition());
                binding.replyHolder.setVisibility(View.VISIBLE);
            }
            Message message = messages.get(position);
            replyID = message.getMessageId();
            if (message.getSenderId().equals(senderUid)) {
                binding.replyNameTxt.setText("me");
                replyUId = senderUid;
            } else if (message.getSenderId().equals(receiverUid)) {
                binding.replyNameTxt.setText(name);
                replyUId = receiverUid;
            }
            if (!message.getImageUrl().equals("null")) {
                binding.replyMsg.setText("Photo");
            } else {
                binding.replyMsg.setText(message.getMessage());
            }
            binding.imageView4.setOnClickListener(view -> {
                binding.replyHolder.setVisibility(View.GONE);
                binding.replyMsg.setText("");
                binding.replyNameTxt.setText("");
                replyID = "null";
                replyUId = "null";
            });
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(controller);
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);
    }

    @Override
    public void onSuccess() {
        try {
            if (messages.size()-1<100) {
                cursor = dbHandler.readOneMessage(room);
                if (cursor.moveToFirst()) {
                    do {
                        if (messages.size() != 0) {
                            message = messages.get(messages.size() - 1);
                            if (!message.getMessageId().equals(cursor.getString(0))) {
                                messages.add(new Message(cursor.getString(0),
                                        encryption.Decrypt(cursor.getString(1), key),
                                        cursor.getString(2),
                                        cursor.getString(3),
                                        cursor.getString(4),
                                        encryption.Decrypt(cursor.getString(5), key),
                                        cursor.getString(6),
                                        cursor.getString(7),
                                        cursor.getInt(8)));
                            }
                        } else {
                            messages.add(new Message(cursor.getString(0),
                                    encryption.Decrypt(cursor.getString(1), key),
                                    cursor.getString(2),
                                    cursor.getString(3),
                                    cursor.getString(4),
                                    encryption.Decrypt(cursor.getString(5), key),
                                    cursor.getString(6),
                                    cursor.getString(7),
                                    cursor.getInt(8)));
                        }
                    } while (cursor.moveToNext());
                    // moving our cursor to next.
                }
                cursor.close();
                runOnUiThread(() -> {
                    layoutManager.setStackFromEnd(true);
                    binding.recyclerView.setAdapter(adapter);
                    binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());
                    adapter.notifyItemInserted(messages.size() - 1);
                    swipeController();
                });
            } else {
                messages.clear();
                loadInitialMessages();
            }
    } catch(Exception e) {
        ExceptionHandler exceptionHandler = new ExceptionHandler(e, senderUid);
        exceptionHandler.upload();
    }
    }

    @Override
    public void onFailure(String error) {

    }
    private void loadRecMessages() {
        receiver = new DatabaseChangedReceiver() {
            @SuppressLint("Range")
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    int value = intent.getExtras().getInt("requestCode");
                    if (value == 1) {
                        cursor = dbHandler.readOneMessage(room);
                        if (cursor.moveToFirst()) {
                            do {
                                if (messages.size() != 0) {
                                    message = messages.get(messages.size() - 1);
                                    if (!message.getMessageId().equals(cursor.getString(0))) {
                                        messages.add(new Message(cursor.getString(0),
                                                encryption.Decrypt(cursor.getString(1), key),
                                                cursor.getString(2),
                                                cursor.getString(3),
                                                cursor.getString(4),
                                                encryption.Decrypt(cursor.getString(5), key),
                                                cursor.getString(6),
                                                cursor.getString(7),
                                                cursor.getInt(8)));
                                    }
                                } else {
                                    messages.add(new Message(cursor.getString(0),
                                            encryption.Decrypt(cursor.getString(1), key),
                                            cursor.getString(2),
                                            cursor.getString(3),
                                            cursor.getString(4),
                                            encryption.Decrypt(cursor.getString(5), key),
                                            cursor.getString(6),
                                            cursor.getString(7),
                                            cursor.getInt(8)));
                                }
                            } while (cursor.moveToNext());
                            // moving our cursor to next.
                        }
                        cursor.close();
                        layoutManager.setStackFromEnd(true);
                        binding.recyclerView.setAdapter(adapter);
                        binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());
                        adapter.notifyItemInserted(messages.size() - 1);
                        swipeController();
                    } else if (value == 2) {
                        String messageID = intent.getStringExtra("messageID");
                        cursor = dbHandler.getDataByID(room, "messageId", messageID);
                        int i;
                        if (adapter.getTargetPosition(messageID) < 0) {
                            i = messages.size() - 1;
                        } else i = adapter.getTargetPosition(messageID);

                        if (cursor.moveToFirst()) {
                            messages.set(i, new Message(cursor.getString(0),
                                    encryption.Decrypt(cursor.getString(1), key),
                                    cursor.getString(2),
                                    cursor.getString(3),
                                    cursor.getString(4),
                                    encryption.Decrypt(cursor.getString(5), key),
                                    cursor.getString(6),
                                    cursor.getString(7),
                                    cursor.getInt(8)));
                        }
                        cursor.close();
                        binding.recyclerView.setAdapter(adapter);
                        binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());
                        adapter.notifyItemChanged(adapter.getTargetPosition(messageID));
                        swipeController();
                    }
                } catch (Exception e) {
                    ExceptionHandler exceptionHandler = new ExceptionHandler(e, senderUid);
                    exceptionHandler.upload();
                }
            }
        };

        IntentFilter filter = new IntentFilter(intentFilter);
        registerReceiver(receiver, filter);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 25);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadInitialMessages() {

            new Thread(() -> {
                try {
                cursor = dbHandler.readCourses(room, 15);
                if (cursor.moveToFirst()) {
                    do {
                        messages.add(new Message(cursor.getString(0),
                                encryption.Decrypt(cursor.getString(1), key),
                                cursor.getString(2),
                                cursor.getString(3),
                                cursor.getString(4),
                                encryption.Decrypt(cursor.getString(5), key),
                                cursor.getString(6),
                                cursor.getString(7),
                                cursor.getInt(8)));
                        dataList.add(new Message(cursor.getString(0),
                                encryption.Decrypt(cursor.getString(1), key),
                                cursor.getString(2),
                                cursor.getString(3),
                                cursor.getString(4),
                                encryption.Decrypt(cursor.getString(5), key),
                                cursor.getString(6),
                                cursor.getString(7),
                                cursor.getInt(8)));
                    } while (cursor.moveToNext());
                    // moving our cursor to next.
                }
                cursor.close();
                } catch (Exception e) {
                    ExceptionHandler exceptionHandler = new ExceptionHandler(e, senderUid);
                    exceptionHandler.upload();
                }
                runOnUiThread(() -> {
                    layoutManager.setStackFromEnd(true);
                    binding.recyclerView.setAdapter(adapter);
                    binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());
                    adapter.notifyItemInserted(messages.size() - 1);
                    swipeController();
                });
            }).start();
    }
}