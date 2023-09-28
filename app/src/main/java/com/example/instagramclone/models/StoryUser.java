package com.example.instagramclone.models;

import java.util.List;

public class StoryUser {
    String userId;
    String storyList;
    Long modified;

    public StoryUser(){};
    public StoryUser(String userId, String storyList, Long modified) {
        this.userId = userId;
        this.storyList = storyList;
        this.modified = modified;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStoryList() {
        return storyList;
    }

    public void setStoryList(String storyList) {
        this.storyList = storyList;
    }

    public Long getModified() {
        return modified;
    }

    public void setModified(Long modified) {
        this.modified = modified;
    }
}
