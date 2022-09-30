package com.reiserx.farae.Models;

public class Message {
    private String messageId, message, senderId, timeStamp, replymsg, replyuid, replyid;
     int status;
    String imageUrl;

    public Message(String message, String senderId, String timeStamp, String replymsg, String replyuid, String replyid, int status) {
        this.message = message;
        this.senderId = senderId;
        this.timeStamp = timeStamp;
        this.replymsg = replymsg;
        this.replyuid = replyuid;
        this.replyid = replyid;
        this.status = status;
    }

    public Message() {
    }

    public Message(String messageId, String message, String senderId, String imageUrl, String timeStamp, String replymsg, String replyuid, String replyid, int status) {
        this.messageId = messageId;
        this.message = message;
        this.senderId = senderId;
        this.timeStamp = timeStamp;
        this.replymsg = replymsg;
        this.replyuid = replyuid;
        this.replyid = replyid;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getReplymsg() {
        return replymsg;
    }

    public void setReplymsg(String replymsg) {
        this.replymsg = replymsg;
    }

    public String getReplyuid() {
        return replyuid;
    }

    public void setReplyuid(String replyuid) {
        this.replyuid = replyuid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getReplyid() {
        return replyid;
    }

    public void setReplyid(String replyid) {
        this.replyid = replyid;
    }
}
