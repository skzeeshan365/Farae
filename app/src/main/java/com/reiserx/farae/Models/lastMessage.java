package com.reiserx.farae.Models;

public class lastMessage {
    String roomid, senderid, message, timestamp;
    int status;

    public lastMessage() {
    }

    public lastMessage(String roomid, String senderid, String message, String timestamp, int status) {
        this.roomid = roomid;
        this.senderid = senderid;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }

    public String getSenderid() {
        return senderid;
    }

    public void setSenderid(String senderid) {
        this.senderid = senderid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
