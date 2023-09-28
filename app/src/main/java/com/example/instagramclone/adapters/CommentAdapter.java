package com.example.instagramclone.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.FCM.FCMController;
import com.example.instagramclone.R;
import com.example.instagramclone.fragments.ProfileFragment;
import com.example.instagramclone.interfaces.CommentButtonClickLisnter;
import com.example.instagramclone.interfaces.ImageClickListener;
import com.example.instagramclone.interfaces.ReplyButtonClickLisnter;
import com.example.instagramclone.models.Comment;
import com.example.instagramclone.models.Like;
import com.example.instagramclone.models.Notif;
import com.example.instagramclone.models.Reply;
import com.example.instagramclone.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {
    Context context;
    List<Comment> commentList;

    CommentButtonClickLisnter commentButtonClickLisnter;
    ImageClickListener imageClickListener;
    String uid,username;
    public CommentAdapter(Context context, List<Comment> commentList, String currentUid,String username, ImageClickListener imageClickListener,CommentButtonClickLisnter commentButtonClickLisnter) {
        this.context = context;
        this.commentList = commentList;
        this.uid = currentUid;
        this.username = username;
        this.imageClickListener = imageClickListener;
        this.commentButtonClickLisnter = commentButtonClickLisnter;
    }

    @NonNull
    @Override
    public CommentAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.MyViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        if (comment == null) {
            return;
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(comment.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String username = snapshot.getValue(User.class).getUsername();
                    String uri = snapshot.getValue(User.class).getImageUrl();
                    holder.txtUsername.setText(username);
                    Glide.with(context).load(uri).dontAnimate().into(holder.imgAvatar);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        Date date = new Date(comment.getCreated() * 1000);
        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
        String timeago = prettyTime.format(date);
        holder.txtTime.setText(timeago);

        DatabaseReference likeRef = databaseReference.child("comments").child(comment.getCommentId()).child("likes");
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    List<Like> likeList = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Like like = dataSnapshot.getValue(Like.class);
                        likeList.add(like);
                    }
                    holder.txtCommentLike.setText(String.valueOf(likeList.size()));
                    holder.txtCommentLike.setVisibility(View.VISIBLE);
                }else{
                    holder.txtCommentLike.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        likeRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isLiked = snapshot.exists();
                        if (isLiked) {
                            holder.btnLike.setImageResource(R.drawable.loved);
                        } else {
                            holder.btnLike.setImageResource(R.drawable.like);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        holder.txtContent.setText(comment.getContent());
        holder.imgAvatar.setOnClickListener(v -> imageClickListener.onImageClick(comment.getUserId()));
        holder.txtUsername.setOnClickListener(v -> imageClickListener.onImageClick(comment.getUserId()));
        holder.btnLike.setOnClickListener(v -> commentButtonClickLisnter.onLikeClick(comment));
        holder.btnReply.setOnClickListener(v -> commentButtonClickLisnter.onReplyClick(comment));
        //chưa có recycle reply ,like comment
        DatabaseReference replyRef = databaseReference.child("comments")
                .child(comment.getCommentId()).child("replies");
        replyRef.orderByChild("created").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.replyList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Reply reply = dataSnapshot.getValue(Reply.class);
                    holder.replyList.add(reply);
                }
                if (holder.replyList.size() == 0)
                    holder.recyclerReply.setVisibility(View.GONE);
                else {
                    holder.recyclerReply.setVisibility(View.VISIBLE);
                    holder.replyAdapter = new ReplyAdapter(context, holder.replyList, uid, new ReplyButtonClickLisnter() {
                        @Override
                        public void onLikeClick(Reply reply) {
                            DatabaseReference likesRef = databaseReference.child("comments").child(reply.getCommentId())
                                    .child("replies").child(reply.getReplyId())
                                    .child("likes");
                            likesRef.child(reply.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    boolean isLiked = snapshot.exists();
                                    if (isLiked) {
                                        // Người dùng đã thích bài đăng, cho phép bỏ thích bài đăng
                                        likesRef.child(uid).removeValue();
                                    } else {
                                        // Người dùng chưa thích bài đăng, cho phép thích bài đăng
                                        Like like = new Like(uid, true, Timestamp.now().getSeconds());
                                        likesRef.child(uid).setValue(like)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        holder.replyAdapter.notifyDataSetChanged();
                                                    }
                                                });
                                        if(!reply.getUserId().equals(uid)){
                                            String notifId = databaseReference.child("notifications/"+reply.getUserId()).push().getKey();
                                            Notif notif = new Notif(notifId,reply.getUserId(),uid,"Like_reply",reply.getReplyId(),Timestamp.now().getSeconds());
                                            databaseReference.child("notifications/"+reply.getUserId()).child(notifId).setValue(notif);
                                            databaseReference.child("users/"+reply.getUserId()).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.exists()&& snapshot.hasChild("token")){
                                                        String token = snapshot.child("token").getValue(String.class);
                                                        String title = snapshot.child("username").getValue(String.class);
                                                        String body = username+" like your comment: "+reply.getContent();
                                                        FCMController fcmController = new FCMController();
                                                        fcmController.sendNotification(token,title,body);

                                                    }
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) { }
                                            });
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        }
                    }, new ImageClickListener() {
                        @Override
                        public void onImageClick(String uid) {
                            DatabaseReference usersRef = databaseReference.child("users");
                            usersRef.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        if (snapshot.exists()) {
                                        // Sử dụng UID ở đây
                                        Bundle bundle = new Bundle();
                                        bundle.putString("uid", uid);
                                        ProfileFragment profileFragment = new ProfileFragment();
                                        profileFragment.setArguments(bundle);
                                        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                                        fragmentManager.beginTransaction()
                                                .setCustomAnimations(R.anim.nothing,R.anim.slide_right)
                                                .add(R.id.fragmentContainer, profileFragment,"ProfileFragment")
                                                .addToBackStack("ProfileFragment")
                                                .commitAllowingStateLoss();
                                    }
                                }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                        }
                    });
                    holder.recyclerReply.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
                    holder.recyclerReply.setAdapter(holder.replyAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imgAvatar, btnLike;
        TextView txtUsername, txtTime, txtContent, btnReply, txtCommentLike;
        RecyclerView recyclerReply;
        ReplyAdapter replyAdapter;
        List<Reply> replyList;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatarAuthor);
            btnLike = itemView.findViewById(R.id.btnLike);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtContent = itemView.findViewById(R.id.txtContent);
            btnReply = itemView.findViewById(R.id.btnReply);
            txtCommentLike = itemView.findViewById(R.id.txtCommentLike);
            recyclerReply = itemView.findViewById(R.id.recyclerReply);
        }


    }
}
