package com.example.instagramclone.models;

public class Notif {
    String notifId;
    String userId, fromId;
    String typeNotif;
    String content;
    Long created;

    public Notif() {
    }

    public Notif(String notifId,String userId, String fromId, String typeNotif, String content, Long created) {
        this.notifId = notifId;
        this.userId = userId;
        this.fromId = fromId;
        this.typeNotif = typeNotif;
        this.content = content;
        this.created = created;
    }

    public String getUserId() {
        return userId;
    }

    public String getNotifId() {
        return notifId;
    }

    public void setNotifId(String notifId) {
        this.notifId = notifId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getTypeNotif() {
        return typeNotif;
    }

    public void setTypeNotif(String typeNotif) {
        this.typeNotif = typeNotif;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }
}
