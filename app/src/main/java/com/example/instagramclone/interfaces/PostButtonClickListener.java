package com.example.instagramclone.interfaces;

import com.example.instagramclone.models.Comment;
import com.example.instagramclone.models.Post;

public interface PostButtonClickListener {
    void onItemDoubleClick (Post post);
    void onImageClick(Post post);
    void onLikeClick(Post post);
    void onCommentClick(Post post);
    void onMoreClick(Post post);

    void onShareClick();
    void onSaveClick();

}
