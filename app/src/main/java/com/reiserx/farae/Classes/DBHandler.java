package com.reiserx.farae.Classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.reiserx.farae.Models.Keys;
import com.reiserx.farae.Models.Message;
import com.reiserx.farae.Models.User;
import com.reiserx.farae.Models.lastMessage;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "reiserx.db";
    private static final int DB_VERSION = 2;

    private static final String messageId = "messageId";
    private static final String message = "message";
    private static final String senderId = "senderId";
    private static final String imageUrl = "imageUrl";
    private static final String timeStamp = "timeStamp";
    private static final String replymsg = "replymsg";
    private static final String replyuid = "replyuid";
    private static final String replyid = "replyid";
    private static final String status = "status";

    // creating a constructor for our database handler.
    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // below method is for creating a database by running a sqlite query
    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    // this method is use to add new course to our sqlite database.
    public void addData(Message messages, String tableName) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(messageId, messages.getMessageId());
        values.put(message, messages.getMessage());
        values.put(senderId, messages.getSenderId());
        values.put(imageUrl, messages.getImageUrl());
        values.put(timeStamp, messages.getTimeStamp());
        values.put(replymsg, messages.getReplymsg());
        values.put(replyuid, messages.getReplyuid());
        values.put(replyid, messages.getReplyid());
        values.put(status, messages.getStatus());

        try {
            db.insertWithOnConflict(tableName, null, values, 0);
        } catch (Exception e) {
            ExceptionHandler exceptionHandler = new ExceptionHandler(e, messages.getSenderId());
            exceptionHandler.upload();
        }
        db.close();
    }

    public void deleteMessages(Message messages, String tableName) {

        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(message, messages.getMessage());
            values.put(imageUrl, messages.getImageUrl());
            values.put(timeStamp, messages.getTimeStamp());
            values.put(replymsg, messages.getReplymsg());
            values.put(replyuid, messages.getReplyuid());
            values.put(replyid, messages.getReplyid());

            db.update(tableName, values, "messageId=?", new String[]{messages.getMessageId()});
            db.close();
        } catch (Exception e) {
            ExceptionHandler exceptionHandler = new ExceptionHandler(e, FirebaseAuth.getInstance().getUid());
            exceptionHandler.upload();
        }
    }

    public Cursor readCourses(String tableName, long limit) {
        SQLiteDatabase db = this.getReadableDatabase();
        long i = DatabaseUtils.queryNumEntries(db, tableName);
        Cursor cursorCourses;
        if (i > limit) {
            cursorCourses = db.rawQuery("SELECT * FROM " + tableName + " LIMIT " + limit + " OFFSET " + (i - limit), null);
        } else {
            cursorCourses = db.rawQuery("SELECT * FROM " + tableName, null);
        }
        return cursorCourses;
    }

    public Cursor readAll(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorCourses;
        cursorCourses = db.rawQuery("SELECT * FROM " + tableName, null);
        return cursorCourses;
    }

    public Cursor paginate(String tableName, long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorCourses = db.rawQuery("SELECT * FROM " + tableName + " ORDER  BY messageId DESC" + " LIMIT " + 5 + " OFFSET " + (id), null);
        return cursorCourses;
    }

    public Cursor readOneMessage(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        long i = DatabaseUtils.queryNumEntries(db, tableName);
        Cursor cursorCourses = db.rawQuery("SELECT * FROM " + tableName + " LIMIT " + 1 + " OFFSET " + (i - 1), null);
        return cursorCourses;
    }

    public Cursor getDataByID(String room, String FieldID, String fieldValue) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + room + " WHERE messageId = " + "'" + fieldValue + "'", null);

        return cursor;
    }

    public void delete(String messageIDs, String tableName) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(tableName, "messageId=?", new String[]{messageIDs});
            db.close();
        } catch (Exception e) {
            ExceptionHandler exceptionHandler = new ExceptionHandler(e, FirebaseAuth.getInstance().getUid());
            exceptionHandler.upload();
        }
    }

    public void createRoom(String room, String encryptionkey, String uid) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("UserID", uid);
            values.put("room", room);
            values.put("encryptionKey", encryptionkey);

            db.insert("rooms", null, values);
            db.close();
        } catch (Exception e) {
            ExceptionHandler exceptionHandler = new ExceptionHandler(e, FirebaseAuth.getInstance().getUid());
            exceptionHandler.upload();
        }
    }

    public Keys getRoom(String uid) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + "rooms" + " WHERE " + "UserID" + " = '" + uid + "'", null);
        Keys keys = null;
        if (cursor.moveToFirst()) {
            keys = new Keys(cursor.getString(1), cursor.getString(2), cursor.getString(0));
        }
        cursor.close();
        return keys;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        onCreate(db);
    }

    public void createMessageTable(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "CREATE TABLE IF NOT EXISTS '" + tableName + "' ("
                + messageId + " TEXT PRIMARY KEY,"
                + message + " TEXT,"
                + senderId + " TEXT,"
                + imageUrl + " TEXT,"
                + timeStamp + " TEXT,"
                + replymsg + " TEXT,"
                + replyuid + " TEXT,"
                + replyid + " TEXT,"
                + status + " INTEGER" + ");";
        db.execSQL(query);
    }

    public void createUserTable() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String query3 = "CREATE TABLE IF NOT EXISTS " + "users" + " ("
                    + "uid" + " TEXT PRIMARY KEY,"
                    + "phoneNumber" + " TEXT,"
                    + "profilePicture" + " TEXT,"
                    + "name" + " TEXT,"
                    + "token" + " TEXT" + ");";
            db.execSQL(query3);
        } catch (Exception e) {
            ExceptionHandler exceptionHandler = new ExceptionHandler(e, FirebaseAuth.getInstance().getUid());
            exceptionHandler.upload();
        }
    }

    public void AddUser(User user) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("uid", user.getUid());
            values.put("phoneNumber", user.getPhoneNumber());
            values.put("profilePicture", user.getProfilePicture());
            values.put("name", user.getName());
            values.put("token", user.getToken());

            db.insert("users", null, values);
            db.close();
        } catch (Exception e) {
            ExceptionHandler exceptionHandler = new ExceptionHandler(e, FirebaseAuth.getInstance().getUid());
            exceptionHandler.upload();
        }
    }

    public void updateUser(User user) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("uid", user.getUid());
            values.put("phoneNumber", user.getPhoneNumber());
            values.put("profilePicture", user.getProfilePicture());
            values.put("name", user.getName());
            values.put("token", user.getToken());

            db.update("users", values, "uid=?", new String[]{user.getUid()});
            db.close();
        } catch (Exception e) {
            ExceptionHandler exceptionHandler = new ExceptionHandler(e, FirebaseAuth.getInstance().getUid());
            exceptionHandler.upload();
        }
    }

    public Cursor getUser() {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery("SELECT * FROM users", null);
    }

    public void createRoomTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query3 = "CREATE TABLE IF NOT EXISTS " + "rooms" + " ("
                + "UserID" + " TEXT PRIMARY KEY,"
                + "room" + " TEXT,"
                + "encryptionKey" + " TEXT" + ");";
        db.execSQL(query3);
    }

    public boolean tableExist(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public boolean CheckIsDataAlreadyInDBorNot(String TableName, String dbfield, String fieldValue) {
        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "Select * from " + TableName + " where " + dbfield + " = '" + fieldValue + "'";
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public void deleteRoom(String uid) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete("rooms", "UserID=?", new String[]{uid});
            db.close();
        } catch (Exception e) {
            ExceptionHandler exceptionHandler = new ExceptionHandler(e, FirebaseAuth.getInstance().getUid());
            exceptionHandler.upload();
        }
    }

    public ArrayList<Keys> getAllRooms() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorCourses = db.rawQuery("SELECT * FROM " + "rooms", null);
        ArrayList<Keys> courseModalArrayList = new ArrayList<>();
        if (cursorCourses.moveToFirst()) {
            do {
                // on below line we are adding the data from cursor to our array list.
                courseModalArrayList.add(new Keys(cursorCourses.getString(1),
                        cursorCourses.getString(2),
                        cursorCourses.getString(0)));
            } while (cursorCourses.moveToNext());
            // moving our cursor to next.
        }

        cursorCourses.close();
        return courseModalArrayList;
    }

    public void createLastMessageTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query3 = "CREATE TABLE IF NOT EXISTS " + "lastmessage" + " ("
                + "roomid" + " TEXT PRIMARY KEY,"
                + "senderid" + " TEXT,"
                + "message" + " TEXT,"
                + "timestamp" + " TEXT,"
                + "status" + " INTEGER" + ");";
        db.execSQL(query3);
    }

    public void addLastMessage(String room, String senderId, String message, String timeStamp, int status) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("roomid", room);
            values.put("senderid", senderId);
            values.put("message", message);
            values.put("timestamp", timeStamp);
            values.put("status", status);

            if (!CheckIsDataAlreadyInDBorNot("lastmessage", "roomid", room)) {

                db.insert("lastmessage", null, values);
            } else {
                db.update("lastmessage", values, "roomid=?", new String[]{room});
            }
            db.close();
        } catch (Exception e) {
            ExceptionHandler exceptionHandler = new ExceptionHandler(e, FirebaseAuth.getInstance().getUid());
            exceptionHandler.upload();
        }
    }

    public lastMessage getLastMessage(String roomid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + "lastmessage" + " WHERE " + "roomid" + " = '" + roomid + "'", null);
        lastMessage lastMessage = null;
        if (cursor.moveToFirst()) {
            lastMessage = new lastMessage(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4));
        }
        cursor.close();
        return lastMessage;
    }
}