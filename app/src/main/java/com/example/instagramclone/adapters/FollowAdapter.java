package com.example.instagramclone.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.FCM.FCMController;
import com.example.instagramclone.R;
import com.example.instagramclone.models.Follow;
import com.example.instagramclone.models.Notif;
import com.example.instagramclone.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class FollowAdapter extends RecyclerView.Adapter<FollowAdapter.MyViewHolder>{
    Context context;
    List<Follow> list;
    User user = new User();
    String currentUid,currentUsername;
    Boolean followFlag = false;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public FollowAdapter(Context context, List<Follow> list,String currentUid,String currentUsername, Boolean followFlag) {
        this.context = context;
        this.list = list;
        this.currentUid = currentUid;
        this.currentUsername = currentUsername;
        this.followFlag = followFlag;
    }

    @NonNull
    @Override
    public FollowAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_follow,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowAdapter.MyViewHolder holder, int position) {
        Follow follow = list.get(position);
        if (follow == null) {
            return;
        }
        String uid;
        if(followFlag){
            uid = follow.getFollowingId();

        }else {
            uid = follow.getFollowerId();
            holder.btnFollow.setText("Following");
            holder.btnFollow.setTextColor(Color.parseColor("#121212"));
            holder.btnFollow.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        }
        databaseReference.child("users").child(uid).addValueEventListener(new ValueEventListener() {
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

        databaseReference.child("users").orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    user = dataSnapshot.getValue(User.class);
                }
                holder.txtFullname.setText(user.getFullname());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        databaseReference.child("follows").orderByChild("created")
            .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Follow follow = dataSnapshot.getValue(Follow.class);
                    if (follow.getFollowingId().equals(uid)) {
                        if(follow.getFollowingId().equals(currentUid)){
                            holder.btnFollow.setText("Following");
                            holder.btnFollow.setTextColor(Color.parseColor("#121212"));
                            holder.btnFollow.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        holder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference followRef = databaseReference.child("follows").child(currentUid + "_" + uid);
                followRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            followRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    holder.btnFollow.setText("Follow");
                                    holder.btnFollow.setTextColor(Color.parseColor("#ffffff"));
                                    holder.btnFollow.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1a73e8")));
                                }
                            });
                        } else {
                            Follow follow1 = new Follow(uid, currentUid, false, Timestamp.now().getSeconds());
                            followRef.setValue(follow1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    holder.btnFollow.setText("Following");
                                    holder.btnFollow.setTextColor(Color.parseColor("#121212"));
                                    holder.btnFollow.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                                }
                            });
                            //send notification for uid
                            if(!uid.equals(currentUid)){
                                String notifId = databaseReference.child("notifications/"+uid).push().getKey();
                                Notif notif = new Notif(notifId,uid,currentUid,"Follow",currentUid + "_" + uid,Timestamp.now().getSeconds());
                                databaseReference.child("notifications/"+uid).child(notifId).setValue(notif);
                                String body = currentUsername+" started following you.";
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
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtUsername,txtFullname;
        MaterialButton btnFollow;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatarAuthor);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtFullname = itemView.findViewById(R.id.txtFullname);
            btnFollow = itemView.findViewById(R.id.btnFollow);
        }

    }
}
