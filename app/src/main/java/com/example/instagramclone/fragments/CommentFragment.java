package com.example.instagramclone.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.FCM.FCMController;
import com.example.instagramclone.R;
import com.example.instagramclone.activities.MainActivity;
import com.example.instagramclone.adapters.CommentAdapter;
import com.example.instagramclone.interfaces.CommentButtonClickLisnter;
import com.example.instagramclone.interfaces.ImageClickListener;
import com.example.instagramclone.models.Comment;
import com.example.instagramclone.models.Like;
import com.example.instagramclone.models.Notif;
import com.example.instagramclone.models.Post;
import com.example.instagramclone.models.Reply;
import com.example.instagramclone.models.User;
import com.example.instagramclone.utilities.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommentFragment extends Fragment implements View.OnClickListener {
    private LinearLayout btnClose;
    private ImageView imgAvatarComment;
    private EditText edtComment;
    private TextView btnComment;
    private Post post;
    private RecyclerView recyclerComment;
    private CommentAdapter commentAdapter;

    private DatabaseReference databaseReference;
    private String currentUid,currentUsername,currentAvt;
    boolean isReply = false;
    BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    MainActivity mainActivity;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        if(activity instanceof MainActivity){
            mainActivity = (MainActivity) activity;
            fragmentManager = mainActivity.getSupportFragmentManager();
            bottomNavigationView = mainActivity.findViewById(R.id.bottomNavigationView);
            bottomNavigationView.setVisibility(View.GONE);
        }

        SharedPreferences sharedPreferences = mainActivity.getSharedPreferences(Constants.KEY_CurrentUserPreference,Context.MODE_PRIVATE);
        currentUid = sharedPreferences.getString(Constants.KEY_currentUID,null);
        currentUsername = sharedPreferences.getString(Constants.KEY_currentUsername,null);
        currentAvt = sharedPreferences.getString(Constants.KEY_currentAvt,null);
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comment, container, false);


        getWidget(view);
        Bundle bundle = getArguments();
        if (bundle != null) {
            post = (Post) bundle.getSerializable("post");
            getComment(post);
        }

        return view;
    }

    private void getWidget(View view) {
        btnClose = view.findViewById(R.id.btnClose);
        btnComment = view.findViewById(R.id.btnComment);
        imgAvatarComment = view.findViewById(R.id.imgAvatarComment);
        edtComment = view.findViewById(R.id.edtComment);
        recyclerComment = view.findViewById(R.id.recyclerComment);

        btnComment.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        recyclerComment.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false));
        recyclerComment.setHasFixedSize(true);
        ((LinearLayoutManager)recyclerComment.getLayoutManager()).setStackFromEnd(true);
        Glide.with(mainActivity).load(currentAvt).dontAnimate().into(imgAvatarComment);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnComment:
                isReply = false;
                btnComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isReply == false && !edtComment.getText().toString().isEmpty()) {
                            actionComment(post);
                        }
                    }
                });
                break;
            case R.id.btnClose:
                fragmentManager.popBackStack();
                closekeyboard();
                mainActivity.overridePendingTransition(R.anim.nothing,R.anim.slide_down);
                bottomNavigationView.setVisibility(View.VISIBLE);
                break;
        }
    }
    private void getComment(Post p) {
        DatabaseReference commentsRef = databaseReference.child("comments");
        commentsRef.orderByChild("created").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Comment> commentList = new ArrayList<>();
                if(snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Comment comment = dataSnapshot.getValue(Comment.class);
                        if(comment.getPostId().equals(p.getPostId())) commentList.add(comment);
                    }
                }
                Collections.reverse(commentList);
                Log.d("tag", String.valueOf(commentList.size()));
                commentAdapter = new CommentAdapter(mainActivity, commentList, currentUid,currentUsername, new ImageClickListener() {
                    @Override
                    public void onImageClick(String uid) {
                        Bundle bundle = new Bundle();
                        bundle.putString("uid", uid);
                        ProfileFragment profileFragment = new ProfileFragment();
                        profileFragment.setArguments(bundle);
                        fragmentManager.beginTransaction()
                                .add(R.id.fragmentContainer, profileFragment,"ProfileFragment")
                                .addToBackStack("ProfileFragment")
                                .commit();
                    }
                }, new CommentButtonClickLisnter() {
                    @Override
                    public void onLikeClick(Comment comment) {
                        actionLikeComment(comment);
                    }

                    @Override
                    public void onReplyClick(Comment comment) {
                        isReply = true;
                        edtComment.requestFocus();
                        databaseReference.child("users").child(comment.getUserId()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    edtComment.setText("@" + snapshot.getValue(User.class).getUsername() + " ");
                                    edtComment.setSelection(edtComment.getText().length());
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });
                        InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(edtComment, InputMethodManager.SHOW_IMPLICIT);
                       {
                        btnComment.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (isReply == true&&!edtComment.getText().toString().isEmpty()) actionReply(comment);
                                isReply = false;
                            }
                        });
                        }

                    }
                });
                recyclerComment.setAdapter(commentAdapter);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void actionComment(Post p) {
        // Lấy tham chiếu đến collection "comments" của tài liệu Post
        DatabaseReference commentsRef = databaseReference.child("comments");
        final String commentId = commentsRef.push().getKey();
        // Tạo một tài liệu mới để lưu trữ bình luận
        String content = edtComment.getText().toString();
        Comment comment = new Comment(commentId, p.getPostId(), currentUid, content, Timestamp.now().getSeconds());
        commentsRef.child(commentId).setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(mainActivity, "Comment successfully", Toast.LENGTH_SHORT).show();
                edtComment.setText(null);
                isReply = false;
                recyclerComment.smoothScrollToPosition(1);
            }
        });
        //send notification for uid
        if(!p.getUserId().equals(currentUid)){
            String body = currentUsername+" commented: "+content;
            String notifId = databaseReference.child("notifications/"+p.getUserId()).push().getKey();
            Notif notif = new Notif(notifId,p.getUserId(),currentUid,"Comment",p.getPostId(),Timestamp.now().getSeconds());
            databaseReference.child("notifications/"+p.getUserId()).child(notifId).setValue(notif);
            pushNotifToUid(p.getUserId(),body);
        }
    }
    private void pushNotifToUid(String uid ,String body){
        databaseReference.child("users/"+uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()&& snapshot.hasChild("token")){
                    String token = snapshot.child("token").getValue(String.class);
                    String title = snapshot.child("username").getValue(String.class);
                    FCMController fcmController = new FCMController();
                    fcmController.sendNotification(token,title,body);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
    private void actionLikeComment(Comment c) {
        DatabaseReference likesRef = databaseReference.child("comments").child(c.getCommentId()).child("likes");
        likesRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isLiked = snapshot.exists();
                if (isLiked) {
                    // Người dùng đã thích bài đăng, cho phép bỏ thích bài đăng
                    likesRef.child(currentUid).removeValue();
                } else {
                    // Người dùng chưa thích bài đăng, cho phép thích bài đăng
                    Like like = new Like(currentUid, true, Timestamp.now().getSeconds());
                    likesRef.child(currentUid).setValue(like);
                    //send notification for uid
                    if(!c.getUserId().equals(currentUid)){
                        String notifId = databaseReference.child("notifications/"+c.getUserId()).push().getKey();
                        Notif notif = new Notif(notifId,c.getUserId(),currentUid,"Like_comment",c.getCommentId(),Timestamp.now().getSeconds());
                        databaseReference.child("notifications/"+c.getUserId()).child(notifId).setValue(notif);
                        String body = currentUsername+" liked your comment: "+c.getContent();
                        pushNotifToUid(c.getUserId(),body);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void actionReply(Comment c) {
        // Lấy tham chiếu đến collection "comments" của tài liệu Post
        DatabaseReference replyRef = databaseReference.child("comments").child(c.getCommentId()).child("replies");
        final String replyID = replyRef.push().getKey();
        String content = edtComment.getText().toString();
        // Tạo một tài liệu mới để lưu trữ bình luận
        Reply reply = new Reply(replyID, c.getCommentId(), currentUid, c.getUserId(), content, Timestamp.now().getSeconds());
        replyRef.child(replyID).setValue(reply).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(mainActivity, "Comment successfully", Toast.LENGTH_SHORT).show();
                edtComment.setText(null);
            }
        });
        //send notification for uid
        if(!c.getUserId().equals(currentUid)){
            String notifId = databaseReference.child("notifications/"+c.getUserId()).push().getKey();
            Notif notif = new Notif(notifId,c.getUserId(),currentUid,"Reply",replyID,Timestamp.now().getSeconds());
            databaseReference.child("notifications/"+c.getUserId()).child(notifId).setValue(notif);
            String body = currentUsername+" commented: "+content;
            pushNotifToUid(c.getUserId(),body);
        }
    }

    private void closekeyboard() {
        View view = mainActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


}