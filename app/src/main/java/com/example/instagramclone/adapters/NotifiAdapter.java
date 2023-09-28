package com.example.instagramclone.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.FCM.FCMController;
import com.example.instagramclone.R;
import com.example.instagramclone.fragments.PostFragment;
import com.example.instagramclone.fragments.ProfileFragment;
import com.example.instagramclone.models.Comment;
import com.example.instagramclone.models.Follow;
import com.example.instagramclone.models.Notif;
import com.example.instagramclone.models.Post;
import com.example.instagramclone.models.Reply;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class NotifiAdapter extends RecyclerView.Adapter<NotifiAdapter.MyViewHolder> {
    private Context context;
    private List<Notif> list;
    private DatabaseReference databaseReference =FirebaseDatabase.getInstance().getReference();
    String username, currentUid;
    FragmentManager fragmentManager;

    public NotifiAdapter(Context context, List<Notif> list,String currentUid,FragmentManager fragmentManager) {
        this.context = context;
        this.list = list;
        this.currentUid = currentUid;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notif,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Notif notif = list.get(position);
        if(notif==null) return;
        databaseReference.child("users/"+notif.getFromId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    username = snapshot.child("username").getValue(String.class);
                    Glide.with(context)
                            .load(snapshot.child("imageUrl").getValue(String.class))
                            .dontAnimate()
                            .into(holder.imgAvatarAuthor);
                    holder.imgAvatarAuthor.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            databaseReference.child("users/"+notif.getFromId())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                Bundle bundle = new Bundle();
                                                bundle.putString("uid", notif.getFromId());
                                                ProfileFragment profileFragment = new ProfileFragment();
                                                profileFragment.setArguments(bundle);
                                                fragmentManager.beginTransaction()
                                                        .add(R.id.fragmentContainer, profileFragment,"ProfileFragment")
                                                        .addToBackStack("ProfileFragment")
                                                        .commit();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                        }
                    });
                    String typeNotif = notif.getTypeNotif();
                    switch (typeNotif){
                        case "Follow":
                            actionFollow(holder,notif);
                            break;
                        case "Like_post":
                            actionLikePost(holder,notif);
                            break;
                        case "Like_comment":
                            actionLikeComment(holder,notif);
                            break;
                        case "Like_reply":
                            actionLikeReply(holder,notif);
                            break;
                        case "Comment":
                            actionComment(holder,notif);
                            break;
                        case "Reply":
                            actionReply(holder,notif);
                            break;
                        default:
                            break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgAvatarAuthor,imgPost;
        private TextView txtContent;
        private MaterialButton btnFollow;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatarAuthor = itemView.findViewById(R.id.imgAvatarAuthor);
            imgPost = itemView.findViewById(R.id.imgPost);
            txtContent = itemView.findViewById(R.id.txtContent);
            btnFollow = itemView.findViewById(R.id.btnFollow);
        }
    }
    private void actionFollow(MyViewHolder holder, Notif notif) {
        holder.btnFollow.setVisibility(View.VISIBLE);
        holder.imgPost.setVisibility(View.GONE);
        holder.txtContent.setText(username+" started following you.");
        String followId = currentUid + "_" + notif.getUserId();
        databaseReference.child("follows").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if(dataSnapshot.getKey().equals(followId)) {
                        holder.btnFollow.setText("Following");
                        holder.btnFollow.setTextColor(Color.parseColor("#121212"));
                        holder.btnFollow.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child("users/"+notif.getUserId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Sử dụng UID ở đây
                            Bundle bundle = new Bundle();
                            bundle.putString("uid", notif.getUserId());
                            ProfileFragment profileFragment = new ProfileFragment();
                            profileFragment.setArguments(bundle);
                            fragmentManager.beginTransaction()
                                    .setCustomAnimations(R.anim.nothing,R.anim.slide_right)
                                    .add(R.id.fragmentContainer, profileFragment,"ProfileFragment")
                                    .addToBackStack("ProfileFragment")
                                    .commitAllowingStateLoss();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        });
    }
    private void actionLikePost(MyViewHolder holder, Notif notif) {
        holder.btnFollow.setVisibility(View.GONE);
        holder.imgPost.setVisibility(View.VISIBLE);
        holder.txtContent.setText(username +" liked your post.");
        databaseReference.child("posts/"+notif.getContent()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Post p = snapshot.getValue(Post.class);
                    Glide.with(context).load(p.getMediaUrl())
                            .dontAnimate().into(holder.imgPost);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        holder.imgPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("post", notif.getContent());
                PostFragment postFragment = new PostFragment();
                postFragment.setArguments(bundle);
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.nothing, R.anim.slide_right)
                        .add(R.id.fragmentContainer, postFragment, "PostFragment")
                        .addToBackStack("PostFragment")
                        .commit();
            }
        });
    }
    private void actionLikeComment(MyViewHolder holder,Notif notif){
        holder.btnFollow.setVisibility(View.GONE);
        holder.imgPost.setVisibility(View.VISIBLE);
        databaseReference.child("comments/"+notif.getContent()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Comment c = snapshot.getValue(Comment.class);
                    holder.txtContent.setText(username +" liked your comment: "+c.getContent());
                    databaseReference.child("posts/"+c.getPostId()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String mediaUrl = snapshot.child("mediaUrl").getValue(String.class);
                                Glide.with(context).load(mediaUrl)
                                        .dontAnimate().into(holder.imgPost);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        holder.imgPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("post", notif.getContent());
                PostFragment postFragment = new PostFragment();
                postFragment.setArguments(bundle);
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.nothing, R.anim.slide_right)
                        .add(R.id.fragmentContainer, postFragment, "PostFragment")
                        .addToBackStack("PostFragment")
                        .commit();
            }
        });
    }
    private void actionLikeReply(MyViewHolder holder,Notif notif){
        holder.btnFollow.setVisibility(View.GONE);
        holder.imgPost.setVisibility(View.VISIBLE);
        databaseReference.child("comments").orderByChild("replies").equalTo(notif.getContent())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            if(snapshot.exists()){
                                Reply r = dataSnapshot.getValue(Reply.class);
                                holder.txtContent.setText(username +" liked your comment: "+r.getContent());
                                databaseReference.child("posts/"+dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            String mediaUrl = snapshot.child("mediaUrl").getValue(String.class);
                                            Glide.with(context).load(mediaUrl)
                                                    .dontAnimate().into(holder.imgPost);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) { }
                                });
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
        holder.imgPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("post", notif.getContent());
                PostFragment postFragment = new PostFragment();
                postFragment.setArguments(bundle);
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.nothing, R.anim.slide_right)
                        .add(R.id.fragmentContainer, postFragment, "PostFragment")
                        .addToBackStack("PostFragment")
                        .commit();
            }
        });
    }
    private void actionComment(MyViewHolder holder, Notif notif) {
        holder.btnFollow.setVisibility(View.GONE);
        holder.imgPost.setVisibility(View.VISIBLE);
        databaseReference.child("comments/"+notif.getContent()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Comment c = snapshot.getValue(Comment.class);
                    holder.txtContent.setText(username + " commented: "+c.getContent());
                    databaseReference.child("posts/"+c.getPostId()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String mediaUrl = snapshot.child("mediaUrl").getValue(String.class);
                                Glide.with(context).load(mediaUrl)
                                        .dontAnimate().into(holder.imgPost);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        holder.imgPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("post", notif.getContent());
                PostFragment postFragment = new PostFragment();
                postFragment.setArguments(bundle);
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.nothing, R.anim.slide_right)
                        .add(R.id.fragmentContainer, postFragment, "PostFragment")
                        .addToBackStack("PostFragment")
                        .commit();
            }
        });
    }
    private void actionReply(MyViewHolder holder, Notif notif) {
        holder.btnFollow.setVisibility(View.GONE);
        holder.imgPost.setVisibility(View.VISIBLE);
        databaseReference.child("comments").orderByChild("replies").equalTo(notif.getContent())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            if(snapshot.exists()){
                                Reply r = dataSnapshot.getValue(Reply.class);
                                holder.txtContent.setText(username +" commented: "+r.getContent());
                                databaseReference.child("posts/"+dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            String mediaUrl = snapshot.child("mediaUrl").getValue(String.class);
                                            Glide.with(context).load(mediaUrl)
                                                    .dontAnimate().into(holder.imgPost);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) { }
                                });
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
        holder.imgPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("post", notif.getContent());
                PostFragment postFragment = new PostFragment();
                postFragment.setArguments(bundle);
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.nothing, R.anim.slide_right)
                        .add(R.id.fragmentContainer, postFragment, "PostFragment")
                        .addToBackStack("PostFragment")
                        .commit();
            }
        });
    }
}