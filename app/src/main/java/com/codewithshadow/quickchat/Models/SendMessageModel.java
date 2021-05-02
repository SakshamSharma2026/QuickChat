package com.codewithshadow.quickchat.Models;

public class SendMessageModel {
    private String sender;
    private String receiver;

    public String getMsg_img() {
        return msg_img;
    }

    public SendMessageModel(String sender, String receiver, String msg_img, String msg, String type, boolean isseen, String key) {
        this.sender = sender;
        this.receiver = receiver;
        this.msg_img = msg_img;
        this.msg = msg;
        this.type = type;
        this.isseen = isseen;
        this.key = key;
    }

    public void setMsg_img(String msg_img) {
        this.msg_img = msg_img;
    }

    private String msg_img;

    public SendMessageModel(String sender, String receiver, String msg_img, String msg, String type, boolean isseen, String key,String time) {
        this.sender = sender;
        this.receiver = receiver;
        this.msg_img = msg_img;
        this.msg = msg;
        this.type = type;
        this.isseen = isseen;
        this.key = key;
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private String time;

    private String msg;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type;

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    private boolean isseen;


    public SendMessageModel()
    {

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    private String key;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


}
