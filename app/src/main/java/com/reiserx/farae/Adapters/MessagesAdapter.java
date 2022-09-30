package com.reiserx.farae.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.reiserx.farae.Activities.view_media;
import com.reiserx.farae.Classes.DBHandler;
import com.reiserx.farae.Models.Message;
import com.reiserx.farae.R;
import com.reiserx.farae.Utilities.Encryption;
import com.reiserx.farae.databinding.ItemReceiveBinding;
import com.reiserx.farae.databinding.ItemReceiveImageBinding;
import com.reiserx.farae.databinding.ItemSendBinding;
import com.reiserx.farae.databinding.ItemSendImageBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vdurmont.emoji.EmojiManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MessagesAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Message> messages;
    RecyclerView recyclerView;
    ImageView delete;
    String room;
    String uid, rec_uid, rec_name;
    private final String key;

    Cursor cursor;
    DBHandler dbHandler;
    ArrayList<Message> finder;

    final int ITEM_SENT_MESSAGE = 1;
    final int ITEM_RECEIVE_MESSAGE = 2;
    final int ITEM_SENT_IMAGE = 3;
    final int ITEM_RECEIVE_IMAGE = 4;

    public MessagesAdapter(Context context, ArrayList<Message> messages, RecyclerView recyclerView, ImageView delete, String room, String uid, String key, String rec_uid, String rec_name) {
        this.context = context;
        this.messages = messages;
        this.recyclerView = recyclerView;
        this.delete = delete;
        this.room = room;
        this.uid = uid;
        this.key = key;
        this.rec_uid = rec_uid;
        this.rec_name = rec_name;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SENT_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_send, parent, false);
            return new SentViewHolder(view);
        } else if (viewType == ITEM_SENT_IMAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_send_image, parent, false);
            return new SentViewHolderImage(view);
        } else if (viewType == ITEM_RECEIVE_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive_image, parent, false);
            return new ReceiverViewHolderImage(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (Objects.equals(FirebaseAuth.getInstance().getUid(), message.getSenderId())) {
            if (message.getImageUrl().equals("null")) {
                return ITEM_SENT_MESSAGE;
            } else return ITEM_SENT_IMAGE;
        } else {
            if (message.getImageUrl().equals("null")) {
                return ITEM_RECEIVE_MESSAGE;
            } else return ITEM_RECEIVE_IMAGE;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        delete.setVisibility(View.GONE);

        if (holder.getClass() == SentViewHolder.class) {
            SentViewHolder viewHolder = (SentViewHolder) holder;

            if (holder.getAdapterPosition() == messages.size() - 1) {
                viewHolder.binding.status.setVisibility(View.VISIBLE);
            } else viewHolder.binding.status.setVisibility(View.GONE);

            viewHolder.binding.timeSent.setText(message.getTimeStamp());
            viewHolder.binding.message.setText(message.getMessage());

            if (message.getReplymsg() != null && !message.getReplyuid().equals("null") && !message.getReplyid().equals("null")) {
                viewHolder.binding.replyName.setVisibility(View.VISIBLE);
                viewHolder.binding.replyMsgAdapter.setVisibility(View.VISIBLE);
                viewHolder.binding.replyMsgAdapter.setText(message.getReplymsg());
                viewHolder.binding.replyMsgAdapter.setOnClickListener(view -> recyclerView.smoothScrollToPosition(getTargetPosition(message.getReplyid())));
                viewHolder.binding.replyName.setOnClickListener(view -> recyclerView.smoothScrollToPosition(getTargetPosition(message.getReplyid())));
                if (message.getReplyuid().equals(rec_uid)) {
                    viewHolder.binding.replyName.setText(rec_name);
                } else viewHolder.binding.replyName.setText("me");
            } else {
                viewHolder.binding.replyName.setVisibility(View.GONE);
                viewHolder.binding.replyMsgAdapter.setVisibility(View.GONE);
            }
            if (message.getMessage() != null && !message.getMessage().equals("This message was deleted")) {
                viewHolder.itemView.setOnLongClickListener(view -> {
                    deletes(message, position);
                    return false;
                });
            } else {
                viewHolder.binding.message.setTextColor(Color.parseColor("#8E8E8E"));
            }
            if (EmojiManager.isOnlyEmojis(message.getMessage())) {
                viewHolder.binding.message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
            } else viewHolder.binding.message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            getStatus(viewHolder.binding.status);
        } else if (holder.getClass() == ReceiverViewHolder.class) {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;

            viewHolder.binding.timeRec.setText(message.getTimeStamp());
            viewHolder.binding.messageReceive.setText(message.getMessage());
            if (viewHolder.binding.messageReceive.getText().toString().equals("")) {
                viewHolder.binding.messageReceive.setText(message.getMessage());
            }
            if (EmojiManager.isOnlyEmojis(message.getMessage())) {
                viewHolder.binding.messageReceive.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
            } else viewHolder.binding.messageReceive.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            if (message.getReplymsg() != null && !message.getReplyuid().equals("null") && !message.getReplyid().equals("null")) {
                viewHolder.binding.replyMsgAdapter.setVisibility(View.VISIBLE);
                viewHolder.binding.replyName.setVisibility(View.VISIBLE);
                viewHolder.binding.replyMsgAdapter.setText(message.getReplymsg());
                viewHolder.binding.replyMsgAdapter.setOnClickListener(view -> recyclerView.smoothScrollToPosition(getTargetPosition(message.getReplyid())));
                viewHolder.binding.replyName.setOnClickListener(view -> recyclerView.smoothScrollToPosition(getTargetPosition(message.getReplyid())));

                if (message.getReplyuid().equals(rec_uid)) {
                    viewHolder.binding.replyName.setText(rec_name);
                } else viewHolder.binding.replyName.setText("me");
            } else {
                viewHolder.binding.replyMsgAdapter.setVisibility(View.GONE);
                viewHolder.binding.replyName.setVisibility(View.GONE);
            }
        } else if (holder.getClass() == SentViewHolderImage.class) {
            SentViewHolderImage viewHolder = (SentViewHolderImage) holder;
            Glide.with(context)
                    .load(message.getImageUrl())
                    .thumbnail(0.01f)
                    .into(viewHolder.binding.img);
            viewHolder.binding.img.setOnClickListener(view -> {
                Intent i = new Intent(context, view_media.class);
                i.putExtra("url", message.getImageUrl());
                context.startActivity(i);
            });
            if (message.getReplymsg() != null && !message.getReplyuid().equals("null") && !message.getReplyid().equals("null")) {
                viewHolder.binding.replyName.setVisibility(View.VISIBLE);
                viewHolder.binding.replyMsgAdapter.setVisibility(View.VISIBLE);
                viewHolder.binding.replyMsgAdapter.setText(message.getReplymsg());
                viewHolder.binding.replyMsgAdapter.setOnClickListener(view -> recyclerView.smoothScrollToPosition(getTargetPosition(message.getReplyid())));
                viewHolder.binding.replyName.setOnClickListener(view -> recyclerView.smoothScrollToPosition(getTargetPosition(message.getReplyid())));
                if (message.getReplyuid().equals(rec_uid)) {
                    viewHolder.binding.replyName.setText(rec_name);
                } else viewHolder.binding.replyName.setText("me");
            } else {
                viewHolder.binding.replyName.setVisibility(View.GONE);
                viewHolder.binding.replyMsgAdapter.setVisibility(View.GONE);
            }
        } else if (holder.getClass() == ReceiverViewHolderImage.class) {
            ReceiverViewHolderImage viewHolder = (ReceiverViewHolderImage) holder;
            Glide.with(context)
                    .load(message.getImageUrl())
                    .placeholder(R.drawable.image_placeholder)
                    .into(viewHolder.binding.imgRec);
            viewHolder.binding.imgRec.setOnClickListener(view -> {
                Intent i = new Intent(context, view_media.class);
                i.putExtra("url", message.getImageUrl());
                context.startActivity(i);
            });
            if (message.getReplymsg() != null && !message.getReplyuid().equals("null") && !message.getReplyid().equals("null")) {
                viewHolder.binding.replyMsgAdapter.setVisibility(View.VISIBLE);
                viewHolder.binding.replyName.setVisibility(View.VISIBLE);
                viewHolder.binding.replyMsgAdapter.setText(message.getReplymsg());
                viewHolder.binding.replyMsgAdapter.setOnClickListener(view -> recyclerView.smoothScrollToPosition(getTargetPosition(message.getReplyid())));
                viewHolder.binding.replyName.setOnClickListener(view -> recyclerView.smoothScrollToPosition(getTargetPosition(message.getReplyid())));
                if (message.getReplyuid().equals(rec_uid)) {
                    viewHolder.binding.replyName.setText(rec_name);
                } else viewHolder.binding.replyName.setText("me");
            } else {
                viewHolder.binding.replyMsgAdapter.setVisibility(View.GONE);
                viewHolder.binding.replyName.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {

        ItemSendBinding binding;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSendBinding.bind(itemView);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        ItemReceiveBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveBinding.bind(itemView);
        }
    }

    public class SentViewHolderImage extends RecyclerView.ViewHolder {

        ItemSendImageBinding binding;

        public SentViewHolderImage(@NonNull View itemView) {
            super(itemView);
            binding = ItemSendImageBinding.bind(itemView);
        }
    }

    public class ReceiverViewHolderImage extends RecyclerView.ViewHolder {

        ItemReceiveImageBinding binding;

        public ReceiverViewHolderImage(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveImageBinding.bind(itemView);
        }
    }

    public void deletes(Message message, int pos) {
        delete.setVisibility(View.VISIBLE);
        Calendar c = Calendar.getInstance();
        String senttime = new SimpleDateFormat("hh:mm a").format(c.getTime());
        HashMap<String, Object> map = new HashMap<>();
        Encryption encryption = new Encryption();
        if (message.getImageUrl() != null) {
            map.put("message", encryption.Encrypt("This message was deleted", key));
            map.put("senderId", message.getSenderId());
            map.put("timeStamp", senttime);
            map.put("replymsg", "");
            map.put("imageUrl", "null");
            map.put("replyuid", "null");
            map.put("replyid", "null");
        }
        delete.setOnClickListener(view1 -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle("Delete message");
            alert.setMessage("The message will be deleted for everyone, by default");
            alert.setPositiveButton("delete", (dialogInterface, i) -> {
                FirebaseDatabase.getInstance().getReference()
                        .child("Messages")
                        .child(room)
                        .child("DeletedMessages")
                        .child(message.getMessageId())
                        .updateChildren(map);

                HashMap<String, Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("senderID", message.getSenderId());
                lastMsgObj.put("lastMsg", encryption.Encrypt("This message was deleted", key));
                lastMsgObj.put("lastMsgTime", senttime);
                lastMsgObj.put("status", 1);

                FirebaseDatabase.getInstance().getReference().child("Messages").child(room).child("lastmessage").updateChildren(lastMsgObj);

                Message message1 = new Message(encryption.Encrypt("This message was deleted", key), message.getSenderId(), senttime, "", "null", "null", 0);
                message1.setMessageId(message.getMessageId());
                message1.setImageUrl("null");
                DBHandler dbHandler = new DBHandler(context);
                dbHandler.deleteMessages(message1, room);
                delete.setVisibility(View.GONE);
                map.clear();
                messages.set(pos, message1);
                message1.setMessage("This message was deleted");
                recyclerView.getAdapter().notifyItemChanged(pos, message1);
            });
            alert.setNegativeButton("cancel", (dialogInterface, i) -> {
                delete.setVisibility(View.GONE);
                map.clear();
            });
            alert.show();
        });
    }

    public void setFilter(List<Message> FilteredDataList) {
        messages = (ArrayList<Message>) FilteredDataList;
        notifyDataSetChanged();
    }

    public int getTargetPosition(String messageID) {
        Message msg;
        for (int i = 0; i < messages.size() - 1; i++) {
            msg = messages.get(i);
            if (msg.getMessageId().equals(messageID)) {
                return i;
            }
        }
        return getTargetPosition2(messageID);
    }

    public int getTargetPosition2(String messageID) {
        Message msg;
        dbHandler = new DBHandler(context);
        Encryption encryption = new Encryption();
        finder = new ArrayList<>();
        cursor = dbHandler.readAll(room);
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
        recyclerView.getAdapter().notifyDataSetChanged();
        for (int i = 0; i < messages.size() - 1; i++) {
            msg = messages.get(i);
            if (msg.getMessageId().equals(messageID)) {
                return i;
            }
        }
        return -1;
    }

    public void getStatus(ImageView imageView) {
        new Thread(() -> {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Messages").child(room).child("lastmessage");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String senderID = snapshot.child("senderID").getValue(String.class);
                        int status = snapshot.child("status").getValue(int.class);
                        if (senderID != null && status != 0) {
                            if (senderID.equals(uid)) {
                                if (status == 3) {
                                    imageView.setImageResource(R.drawable.ic_baseline_done_all_24);
                                    imageView.setColorFilter(context.getColor(R.color.delivered));
                                } else if (status == 2) {
                                    imageView.setImageResource(R.drawable.ic_baseline_done_all_24);
                                    imageView.setColorFilter(context.getColor(R.color.delivered));
                                    imageView.setColorFilter(context.getColor(R.color.checl));
                                } else if (status == 1) {
                                    imageView.setImageResource(R.drawable.ic_baseline_check_24);
                                }
                            }
                        } else imageView.setImageResource(R.drawable.ic_baseline_check_24);
                    } else imageView.setImageResource(R.drawable.ic_baseline_check_24);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }).start();
    }
}
