package com.codewithshadow.quickchat.SendNotification.NotiModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class NotificationReq {

    @SerializedName("to")
    @Expose
    private String to;
    @SerializedName("notification")
    @Expose
    private Notification notification;


    public NotificationReq(String to, Notification notification) {
        this.to = to;
        this.notification = notification;
    }



    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }




    public static class Notification {

        @SerializedName("title")
        @Expose
        private String title;
        @SerializedName("body")
        @Expose
        private String body;


        public Notification(String title, String body) {
            this.title = title;
            this.body = body;
        }


        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

    }
}
