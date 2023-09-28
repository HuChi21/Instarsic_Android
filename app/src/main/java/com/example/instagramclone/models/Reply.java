package com.example.instagramclone.models;

import com.google.firebase.Timestamp;

public class Reply {
    private String replyId;
    private String commentId;
    private String userId;
    private String replytoId;
    private String content;
    private Long created;

    public Reply() {}

    public Reply(String replyId, String commentId, String userId, String replytoId, String content, Long created) {
        this.replyId = replyId;
        this.commentId = commentId;
        this.userId = userId;
        this.replytoId = replytoId;
        this.content = content;
        this.created = created;
    }

    public String getReplytoId() {
        return replytoId;
    }

    public void setReplytoId(String replytoId) {
        this.replytoId = replytoId;
    }

    public String getReplyId() {
        return replyId;
    }

    public void setReplyId(String replyId) {
        this.replyId = replyId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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