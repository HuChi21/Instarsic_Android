package com.example.instagramclone.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.interfaces.ImageClickListener;
import com.example.instagramclone.interfaces.ReplyButtonClickLisnter;
import com.example.instagramclone.models.Like;
import com.example.instagramclone.models.Reply;
import com.example.instagramclone.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.MyViewHolder> {
    Context context;
    List<Reply> replyList;
    ReplyButtonClickLisnter replyButtonClickLisnter;
    ImageClickListener imageClickListener;
    String uid;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public ReplyAdapter(Context context, List<Reply> replyList, String uid, ReplyButtonClickLisnter replyButtonClickLisnter,ImageClickListener imageClickListener) {
        this.context = context;
        this.replyList = replyList;
        this.uid = uid;
        this.replyButtonClickLisnter = replyButtonClickLisnter;
        this.imageClickListener = imageClickListener;
    }

    @NonNull
    @Override
    public ReplyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reply, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyAdapter.MyViewHolder holder, int position) {
        Reply reply = replyList.get(position);
        if (reply == null) {
            return;
        }
        databaseReference.child("users").child(reply.getUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String username =  snapshot.getValue(User.class).getUsername();
                    holder.txtUsername.setText(username);
                    FirebaseStorage.getInstance().getReference().child("avatar/" + snapshot.getValue(User.class).getImageUrl())
                            .getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(context).load(uri).dontAnimate().into(holder.imgAvatar);
                                }
                            });
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        Date date = new Date(reply.getCreated()*1000);
        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
        String timeago = prettyTime.format(date);
        holder.txtTime.setText(timeago);

        DatabaseReference likeRef = databaseReference.child("comments")
                .child(reply.getCommentId()).child("replies").child(reply.getReplyId()).child("likes");
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Like> likeList = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Like like = dataSnapshot.getValue(Like.class);
                        likeList.add(like);
                    }
                    holder.txtReplyLike.setText(String.valueOf(likeList.size()));
                    holder.txtReplyLike.setVisibility(View.VISIBLE);
                }else{
                    holder.txtReplyLike.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
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
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        holder.txtContent.setText(reply.getContent());
        holder.btnLike.setOnClickListener(v -> replyButtonClickLisnter.onLikeClick(reply));
        holder.imgAvatar.setOnClickListener(v -> imageClickListener.onImageClick(reply.getUserId()));
        holder.txtUsername.setOnClickListener(v -> imageClickListener.onImageClick(reply.getUserId()));
    }

    @Override
    public int getItemCount() {
        return replyList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imgAvatar, btnLike;
        TextView txtUsername, txtTime, txtContent, txtReplyLike;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatarAuthor);
            btnLike = itemView.findViewById(R.id.btnLike);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtContent = itemView.findViewById(R.id.txtContent);
            txtReplyLike = itemView.findViewById(R.id.txtReplyLike);
        }


    }
}
