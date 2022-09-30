package com.reiserx.farae.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.reiserx.farae.Activities.ChatActivity;
import com.reiserx.farae.Classes.DBHandler;
import com.reiserx.farae.Models.Keys;
import com.reiserx.farae.Models.User;
import com.reiserx.farae.Models.lastMessage;
import com.reiserx.farae.R;
import com.reiserx.farae.Utilities.Encryption;
import com.reiserx.farae.Utilities.generateKey;
import com.reiserx.farae.databinding.RowConversationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    Context context;
    ArrayList<User> users;
    Encryption encryption;
    String name = "null";
    String s = "+91";
    private FirebaseDatabase db;
    private DBHandler dbHandler;
    ValueEventListener valueEventListener;


    public UsersAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UsersAdapter.UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);
        return new UsersViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.UsersViewHolder holder, int position) {
        User user = users.get(position);

        dbHandler = new DBHandler(context);

        String senderId = FirebaseAuth.getInstance().getUid();
        String number = user.getPhoneNumber();
        encryption = new Encryption();

        db = FirebaseDatabase.getInstance();

            Keys data = dbHandler.getRoom(user.getUid());
            if (data != null) {
                lastMessage lastMessage = dbHandler.getLastMessage(data.getRoom());
                if (lastMessage != null) {
                    if (lastMessage.getMessage().equals("sent a photo")) {
                        holder.binding.lastMessage.setText("photo");
                    } else {
                        holder.binding.lastMessage.setText(encryption.Decrypt(lastMessage.getMessage(), data.getEncryptionKey()));
                        holder.binding.msgTime.setText(lastMessage.getTimestamp());
                        if (holder.binding.lastMessage.getText().toString().equals("")) {
                            holder.binding.lastMessage.setText(lastMessage.getMessage());
                        }
                    }
                } else {
                    holder.binding.lastMessage.setText("Tap to chat");
                }
            }

        holder.binding.username.setText(contactExists(number));
        if (!user.getProfilePicture().equals("No Image")) {
            Glide.with(context).load(user.getProfilePicture())
                    .placeholder(R.drawable.avatar)
                    .into(holder.binding.imageView2);
        }

        holder.itemView.setOnClickListener(v -> new Thread(() -> {
        DatabaseReference rec_ref = FirebaseDatabase.getInstance().getReference().child("Messages").child("rooms").child(user.getUid()).child(senderId);
        DatabaseReference sender_ref1 = FirebaseDatabase.getInstance().getReference().child("Messages").child("rooms").child(senderId).child(user.getUid());
            Keys keyss = dbHandler.getRoom(user.getUid());
            if (keyss != null && keyss.getUserid().equals(user.getUid())) {
                if (!dbHandler.tableExist(keyss.getRoom())) {
                    dbHandler.createMessageTable(keyss.getRoom());
                }
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("Username", holder.binding.username.getText().toString());
                intent.putExtra("Uid", user.getUid());
                intent.putExtra("number", user.getPhoneNumber());
                intent.putExtra("ProfilePic", user.getProfilePicture());
                intent.putExtra("room", keyss.getRoom());
                intent.putExtra("encryptionKey", keyss.getEncryptionKey());
                intent.putExtra("token", user.getToken());
                context.startActivity(intent);
            } else {
                valueEventListener = sender_ref1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Keys keys1 = snapshot.getValue(Keys.class);

                            if (keys1 != null && !dbHandler.tableExist(keys1.getRoom())) {
                                dbHandler.createMessageTable(keys1.getRoom());
                            }
                            dbHandler.createRoom(keys1.getRoom(), keys1.getEncryptionKey(), keys1.getUserid());
                            sender_ref1.removeValue();
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("Username", holder.binding.username.getText().toString());
                            intent.putExtra("Uid", user.getUid());
                            intent.putExtra("number", user.getPhoneNumber());
                            intent.putExtra("ProfilePic", user.getProfilePicture());
                            intent.putExtra("room", keys1.getRoom());
                            intent.putExtra("encryptionKey", keys1.getEncryptionKey());
                            intent.putExtra("token", user.getToken());
                            context.startActivity(intent);
                        } else {
                            generateKey generateKey = new generateKey();
                            String room = generateKey.randomString(20);
                            String key = generateKey.randomString(20);
                            Keys keys = new Keys(room, key, FirebaseAuth.getInstance().getUid());
                            if (!dbHandler.tableExist(room)) {
                                dbHandler.createMessageTable(room);
                            }
                            dbHandler.createRoom(room, key, user.getUid());
                            rec_ref.setValue(keys);
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("Username", holder.binding.username.getText().toString());
                            intent.putExtra("Uid", user.getUid());
                            intent.putExtra("number", user.getPhoneNumber());
                            intent.putExtra("ProfilePic", user.getProfilePicture());
                            intent.putExtra("room", room);
                            intent.putExtra("encryptionKey", key);
                            intent.putExtra("token", user.getToken());
                            context.startActivity(intent);
                        }
                        sender_ref1.removeEventListener(valueEventListener);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).start());
        holder.itemView.setOnLongClickListener(view -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setMessage("deleted");
            alert.setPositiveButton("delete", (dialogInterface, i) -> {
                if (dbHandler.CheckIsDataAlreadyInDBorNot("rooms", "UserID", user.getUid())) {
                    dbHandler.deleteRoom(user.getUid());
                    Toast.makeText(context, "deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "not exist", Toast.LENGTH_SHORT).show();
                }
            });
            alert.show();
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {

        RowConversationBinding binding;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConversationBinding.bind(itemView);
        }
    }

    public String contactExists(String number) {
        if (number != null) {
            ContentResolver cr = context.getContentResolver();
            @SuppressLint("Recycle") Cursor curContacts = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

            while (curContacts.moveToNext()) {
                @SuppressLint("Range") String contactNumber = curContacts.getString(curContacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                @SuppressLint("Range") String display_name = curContacts.getString(curContacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                if (contactNumber.replace(" ", "").startsWith(s)) {
                    if (contactNumber.replace(" ", "").equals(number)) {
                        name = display_name;
                    }
                } else {
                    if ((s + contactNumber).replace(" ", "").equals(number)) {
                        name = display_name;
                    }
                }
            }
        }
        return name;
    }
}
