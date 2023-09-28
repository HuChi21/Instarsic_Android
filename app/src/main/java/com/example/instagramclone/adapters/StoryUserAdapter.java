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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.fragments.ProfileFragment;
import com.example.instagramclone.fragments.StoryFragment;
import com.example.instagramclone.interfaces.ImageClickListener;
import com.example.instagramclone.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class StoryUserAdapter extends RecyclerView.Adapter<StoryUserAdapter.MyViewHolder> {
    Context context;
    List<String> list;
    ImageClickListener imageClickListener;
    String currentUid;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public StoryUserAdapter(Context context, List<String> list, String currentUid,ImageClickListener imageClickListener) {
        this.context = context;
        this.list = list;
        this.currentUid = currentUid;
        this.imageClickListener = imageClickListener;
    }

    @NonNull
    @Override
    public StoryUserAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_story_user, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryUserAdapter.MyViewHolder holder, int position) {
        String userId = list.get(position);
        if (userId == null) {
            return;
        }
        if (position == 0) {
            holder.btnStoryCreate.setVisibility(View.VISIBLE);
            holder.btnStoryCreate.setOnClickListener( v -> {
                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.nothing,R.anim.slide_up)
                    .add(R.id.fragmentContainer, new StoryFragment(),"StoryFragment")
                    .addToBackStack("StoryFragment")
                    .commit();
            });
        }
        databaseReference.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    holder.txtStoryAuthor.setText(user.getUsername() + " ");
                    Glide.with(context).load(user.getImageUrl()).dontAnimate().into(holder.imgStory);
                    holder.txtStoryAuthor.setOnClickListener(v ->{
                        Bundle bundle = new Bundle();
                        bundle.putString("uid",user.getUid());
                        ProfileFragment profileFragment = new ProfileFragment();
                        profileFragment.setArguments(bundle);
                        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.nothing,R.anim.slide_right)
                            .add(R.id.fragmentContainer, profileFragment,"ProfileFragment")
                            .addToBackStack("ProfileFragment")
                            .commit();
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        holder.itemView.setOnClickListener(v -> imageClickListener.onImageClick(userId));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imgStory;
        TextView txtStoryAuthor;
        CardView btnStoryCreate;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgStory = itemView.findViewById(R.id.imgStory);
            txtStoryAuthor = itemView.findViewById(R.id.txtStoryAuthor);
            btnStoryCreate = itemView.findViewById(R.id.btnStoryCreate);
        }
    }
}
