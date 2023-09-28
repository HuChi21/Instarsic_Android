package com.example.instagramclone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.interfaces.PostItemClickListener;
import com.example.instagramclone.models.Post;

import java.util.List;

public class PostGridAdapter extends RecyclerView.Adapter<PostGridAdapter.MyViewHolder> {
    Context context;
    List<Post> list;
    private PostItemClickListener itemClickListener;


    public PostGridAdapter(Context context, List<Post> list, PostItemClickListener itemClickListener) {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }
    @NonNull
    @Override
    public PostGridAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_grid,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostGridAdapter.MyViewHolder holder, int position) {
        Post post = list.get(position);
        if(post == null){
            return;
        }
        Glide.with(context).load(post.getMediaUrl()).dontAnimate().into(holder.imgPostPhoto);
        //is video
        if(post.getPostId().contains(post.getUserId()+"_video_")){
            holder.imgVid.setVisibility(View.VISIBLE);
        }

        holder.imgPostPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onClick(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder  {
        ImageView imgPostPhoto,imgVid;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPostPhoto = itemView.findViewById(R.id.imgPostPhoto);
            imgVid = itemView.findViewById(R.id.imgVid);
        }


    }
}
