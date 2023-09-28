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
import androidx.cardview.widget.CardView;
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

public class CommentActivitiesAdapter extends RecyclerView.Adapter<CommentActivitiesAdapter.MyViewHolder> {
    Context context;
    List<Comment> commentList;

    CommentButtonClickLisnter commentButtonClickLisnter;
    public CommentActivitiesAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentActivitiesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_activities, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentActivitiesAdapter.MyViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        if (comment == null) {
            return;
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users/"+comment.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String username = snapshot.getValue(User.class).getUsername();
                    String uri = snapshot.getValue(User.class).getImageUrl();
                    holder.txtName.setText(username+" ");
                    Glide.with(context).load(uri).dontAnimate().into(holder.imgAvatar);
                    Date date = new Date(comment.getCreated() * 1000);
                    PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
                    String timeago = prettyTime.format(date);
                    holder.txtTime.setText(timeago);
                    holder.txtContent.setText(comment.getContent());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        databaseReference.child("posts/"+comment.getPostId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String authorUid = snapshot.child("userId").getValue(String.class);
                    Long created = snapshot.child("created").getValue(Long.class);
                    String image = snapshot.child("mediaUrl").getValue(String.class);
                    Glide.with(context).load(image).dontAnimate().into(holder.imgPost);
                    if(snapshot.hasChild("caption")){
                        String content = snapshot.child("caption").getValue(String.class);
                        holder.txtContent.setText(content);
                    }else{
                        holder.txtContent.setVisibility(View.GONE);
                    }
                    databaseReference.child("users/"+authorUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String username = snapshot.getValue(User.class).getUsername();
                                String uri = snapshot.getValue(User.class).getImageUrl();
                                holder.txtAuthorName.setText(username+" ");
                                Glide.with(context).load(uri).dontAnimate().into(holder.imgAvatarAuthor);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                    Date date = new Date(created * 1000);
                    PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
                    String timeago = prettyTime.format(date);
                    holder.txtAuthorTime.setText(timeago);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatarAuthor, imgAvatar,imgPost;
        TextView txtAuthorName, txtAuthorContent,txtTime, txtName, txtContent, txtAuthorTime;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatarAuthor = itemView.findViewById(R.id.imgAvatarAuthor);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            imgPost = itemView.findViewById(R.id.imgPost);
            txtAuthorName = itemView.findViewById(R.id.txtAuthorName);
            txtAuthorContent = itemView.findViewById(R.id.txtAuthorContent);
            txtAuthorTime = itemView.findViewById(R.id.txtAuthorTime);
            txtName = itemView.findViewById(R.id.txtName);
            txtContent = itemView.findViewById(R.id.txtContent);
            txtTime = itemView.findViewById(R.id.txtTime);

        }


    }
}
