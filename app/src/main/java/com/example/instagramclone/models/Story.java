package com.example.instagramclone.models;


public class Story {
    String storyId;
    String userId,userAvt;
    String mediaUrl;
    Long created;

    public Story(){};
    public Story(String storyId,String userId,String userAvt,String mediaUrl, Long created) {
        this.storyId = storyId;
        this.userId = userId;
        this.userAvt = userAvt;
        this.mediaUrl = mediaUrl;
        this.created = created;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserAvt() {
        return userAvt;
    }

    public void setUserAvt(String userAvt) {
        this.userAvt = userAvt;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }
}
