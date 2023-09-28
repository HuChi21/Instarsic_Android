package com.example.instagramclone.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.activities.MessageActivity;
import com.example.instagramclone.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class ActiveUserAdapter extends RecyclerView.Adapter<ActiveUserAdapter.MyViewHolder> {
    Context context;
    List<User> list;
    String currentUid;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public ActiveUserAdapter(Context context, List<User> list,String currentUid) {
        this.context = context;
        this.list = list;
        this.currentUid = currentUid;
    }

    @NonNull
    @Override
    public ActiveUserAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_active_user, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActiveUserAdapter.MyViewHolder holder, int position) {
        User user = list.get(position);
        if (user == null) {
            return;
        }

        databaseReference.child("users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    holder.txtStoryAuthor.setText(user.getUsername() + " ");
                    Glide.with(context).load(user.getImageUrl()).dontAnimate().into(holder.imgAvatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        holder.itemView.setOnClickListener(v -> {
            String[] uids = {currentUid, user.getUid()};
            Arrays.sort(uids);
            String chatId = uids[0] + "_" + uids[1];

            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra("receiverId",user.getUid());
            intent.putExtra("chatId",chatId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Toast.makeText(context, user.getUid(), Toast.LENGTH_SHORT).show();
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtStoryAuthor;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtStoryAuthor = itemView.findViewById(R.id.txtStoryAuthor);
        }
    }
}
