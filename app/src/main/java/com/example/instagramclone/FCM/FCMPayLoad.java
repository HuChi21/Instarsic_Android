package com.example.instagramclone.FCM;

import java.util.Map;

public class FCMPayLoad {
    private String to;
    private Map<String,String> notification;

    public FCMPayLoad() {
    }

    public FCMPayLoad(String to, Map<String, String> notification) {
        this.to = to;
        this.notification = notification;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Map<String, String> getNotification() {
        return notification;
    }

    public void setNotification(Map<String, String> notification) {
        this.notification = notification;
    }
}

