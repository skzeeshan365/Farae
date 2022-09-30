package com.reiserx.farae.Models;

public class Keys {
    String room, encryptionKey, userid;

    public Keys() {

    }

    public Keys(String room, String encryptionKey, String userid) {
        this.room = room;
        this.encryptionKey = encryptionKey;
        this.userid = userid;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
