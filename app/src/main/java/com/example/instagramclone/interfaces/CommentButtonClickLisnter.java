package com.example.instagramclone.interfaces;

import com.example.instagramclone.models.Comment;

public interface CommentButtonClickLisnter {
    void onLikeClick(Comment comment);
    void onReplyClick(Comment comment);
}
