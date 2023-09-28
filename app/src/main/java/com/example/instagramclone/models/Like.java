package com.example.instagramclone.models;

public class Like {
    private String userId;
    private boolean isliked;
    private Long created;

    public Like(){}

    public Like(String userId, boolean isliked, Long created) {
        this.userId = userId;
        this.isliked = isliked;
        this.created = created;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean getIsLiked() {
        return isliked;
    }

    public void setIsLiked(boolean isLiked) {
        this.isliked = isLiked;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }
}
