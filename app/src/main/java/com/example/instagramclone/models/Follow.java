package com.example.instagramclone.models;

public class Follow {
    String followerId;
    String followingId;
    boolean isblocked;
    Long created;

    public Follow(){};

    public Follow(String followerId, String followingId, boolean isblocked, Long created) {
        this.followerId = followerId;
        this.followingId = followingId;
        this.isblocked = isblocked;
        this.created = created;
    }

    public String getFollowerId() {
        return followerId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }

    public String getFollowingId() {
        return followingId;
    }

    public void setFollowingId(String followingId) {
        this.followingId = followingId;
    }

    public boolean isIsblocked() {
        return isblocked;
    }

    public void setIsblocked(boolean isblocked) {
        this.isblocked = isblocked;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }
}
