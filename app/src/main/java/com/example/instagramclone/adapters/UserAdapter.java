package com.example.instagramclone.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import com.example.instagramclone.models.Follow;
import com.example.instagramclone.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder>{
    Context context;
    List<User> list;
    ImageClickListener imageClickListener;

    public UserAdapter(Context context, List<User> list,ImageClickListener imageClickListener) {
        this.context = context;
        this.list = list;
        this.imageClickListener = imageClickListener;
    }

    @NonNull
    @Override
    public UserAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.MyViewHolder holder, int position) {
        User user = list.get(position);
        if (user == null) {
            return;
        }
        holder.txtUsername.setText(user.getUsername());
        holder.txtFullname.setText(user.getFullname());
        Glide.with(context).load(user.getImageUrl()).dontAnimate().into(holder.imgAvatar);

        holder.itemView.setOnClickListener(v -> imageClickListener.onImageClick(user.getUid()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtUsername,txtFullname;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatarAuthor);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtFullname = itemView.findViewById(R.id.txtFullname);
        }

    }
}
