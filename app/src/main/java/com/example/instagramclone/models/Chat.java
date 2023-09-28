package com.example.instagramclone.models;

public class Chat {
    String userId,username, imageUrl;
    Long signedin;

    public Chat(){}

    public Chat(String userId, String username, String image, Long signedin) {
        this.userId = userId;
        this.username = username;
        this.imageUrl = image;
        this.signedin = signedin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public Long getSignedin() {
        return signedin;
    }

    public void setSignedin(Long signedin) {
        this.signedin = signedin;
    }
}
