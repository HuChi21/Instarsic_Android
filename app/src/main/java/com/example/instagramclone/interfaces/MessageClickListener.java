package com.example.instagramclone.interfaces;


import com.example.instagramclone.models.Message;

public interface MessageClickListener {
    void onItemDoubleClick (Message message);
    void onSingleClick(Message message);
}
