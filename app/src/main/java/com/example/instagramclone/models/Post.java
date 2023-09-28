package com.example.instagramclone.models;

import java.io.Serializable;

public class Post implements Serializable {
    String postId, userId,caption,mediaUrl;
    Long created;
    boolean isArchive;

    public Post() {
    }

    public Post(String postId, String userId, String mediaUrl, String caption, Long created,boolean isArchive) {
        this.postId = postId;
        this.userId = userId;
        this.mediaUrl = mediaUrl;
        this.caption = caption;
        this.created = created;
        this.isArchive = isArchive;
    }

    public boolean isArchive() {
        return isArchive;
    }

    public void setArchive(boolean archive) {
        isArchive = archive;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

}
