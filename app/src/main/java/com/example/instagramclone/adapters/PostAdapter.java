package com.example.instagramclone.adapters;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.interfaces.PostButtonClickListener;
import com.example.instagramclone.models.Like;
import com.example.instagramclone.models.Post;
import com.example.instagramclone.models.User;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {
    Context context;
    List<Post> list;
    private PostButtonClickListener buttonClickListener;
    String uid;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();


    public PostAdapter(Context context, List<Post> list, String uid, PostButtonClickListener buttonClickListener) {
        this.context = context;
        this.list = list;
        this.uid = uid;
        this.buttonClickListener = buttonClickListener;
    }

    @NonNull
    @Override
    public PostAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.MyViewHolder holder, int position) {
        Post post = list.get(position);
        if (post == null && uid == null) {
            return;
        }
        databaseReference.child("users").child(post.getUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String username = snapshot.getValue(User.class).getUsername();
                    String uri = snapshot.getValue(User.class).getImageUrl();
                    holder.txtAuthorName.setText(username);
                    holder.txtAuthorName2.setText(username);
                    Glide.with(context).load(uri).dontAnimate().into(holder.imgAuthorAvatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        if(post.getPostId().contains(post.getUserId()+"_image_")){
            holder.imgAuthorPost.setVisibility(View.VISIBLE);
            holder.videoAuthorPost.setVisibility(View.GONE);
            Glide.with(context).load(post.getMediaUrl()).dontAnimate().into(holder.imgAuthorPost);
        }
        else if(post.getPostId().contains(post.getUserId()+"_video_")){
            holder.imgAuthorPost.setVisibility(View.GONE);
            holder.videoAuthorPost.setVisibility(View.VISIBLE);
            String uri = post.getMediaUrl();
             if(!uri.isEmpty()){
                 ExoPlayer player;
                 player = new ExoPlayer.Builder(context).build();
                 MediaItem mediaItem = MediaItem.fromUri(uri);
                 player.setMediaItem(mediaItem);
                 holder.videoAuthorPost.setPlayer(player);
                 player.setRepeatMode(Player.REPEAT_MODE_ALL);

                 DisplayMetrics displayMetrics = new DisplayMetrics();
                 WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                 if (windowManager != null) {
                     windowManager.getDefaultDisplay().getMetrics(displayMetrics);
                     int screenWidth = displayMetrics.widthPixels;
                     int screenHeight = displayMetrics.heightPixels;

                     MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                     try {
                         // Đặt data source là URL của video
                         HashMap<String, String> headers = new HashMap<>();
                         retriever.setDataSource(uri, headers);

                         int width = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                         int height = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

                         float aspectRatio = (float) width/height;
                         if(aspectRatio > 1 && width!=screenWidth) holder.videoAuthorPost.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, (int) (screenWidth/aspectRatio)));
                         else if(aspectRatio == 1) holder.videoAuthorPost.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenWidth));
                         else if (aspectRatio < 1 )
                             if(height != screenWidth)holder.videoAuthorPost.setLayoutParams(new LinearLayout.LayoutParams((int) (screenWidth / aspectRatio), screenWidth));
                             else if(height == screenWidth)holder.videoAuthorPost.setLayoutParams(new LinearLayout.LayoutParams((int) (screenWidth / aspectRatio), screenWidth));

                     } catch (Exception e) {
                         e.printStackTrace();
                         Log.d("a", "Failed to retrieve video metadata");
                     } finally {
                         try {
                             retriever.release();
                         } catch (IOException e) {
                             throw new RuntimeException(e);
                         }
                     }
                 }
                 player.prepare();
                 player.setPlayWhenReady(false);
                 player.setRepeatMode(Player.REPEAT_MODE_OFF);
             }

        }

        String captionText = post.getCaption() != null ? post.getCaption() : "";
        if(post.getCaption() != null){
            holder.txtCaption.setText(captionText);
        }else{
            holder.tbrCaption.setVisibility(View.GONE);
        }
        Date date = new Date(post.getCreated()*1000);
        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
        String timeago = prettyTime.format(date);
        holder.txtCreated.setText(timeago);

        DatabaseReference postRef = databaseReference.child("post_likes").child(post.getPostId());
        DatabaseReference likesRef = postRef.child("likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Like> likeList = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Like like = dataSnapshot.getValue(Like.class);
                        likeList.add(like);
                    }
                    holder.txtLikes.setText("Liked by " + likeList.size() + " others");
                    holder.txtLikes.setVisibility(View.VISIBLE);
                }
                else{
                    holder.txtLikes.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        likesRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isLiked = snapshot.exists();
                if (isLiked) {
                    holder.btnLove.setImageResource(R.drawable.loved);
                } else {
                    holder.btnLove.setImageResource(R.drawable.like);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        DatabaseReference commentRef = databaseReference.child("comments");
        commentRef.orderByChild("postId").equalTo(post.getPostId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int commentNum = (int) snapshot.getChildrenCount();
                    if(commentNum == 0){
                        holder.txtCommentNum.setVisibility(View.GONE);
                    }
                    holder.txtCommentNum.setText("View all "+String.valueOf(commentNum)+ " comments");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        holder.imgAuthorAvatar.setOnClickListener(v -> buttonClickListener.onImageClick(post));
        holder.txtAuthorName.setOnClickListener(v -> buttonClickListener.onImageClick(post));
        holder.txtAuthorName2.setOnClickListener(v -> buttonClickListener.onImageClick(post));
        holder.btnLove.setOnClickListener(v -> buttonClickListener.onLikeClick(post));
        holder.btnMore.setOnClickListener(v -> buttonClickListener.onMoreClick(post));
        holder.btnComment.setOnClickListener(v -> buttonClickListener.onCommentClick(post));
        holder.txtCommentNum.setOnClickListener(v -> buttonClickListener.onCommentClick(post));
        holder.imgAuthorPost.setOnClickListener(new View.OnClickListener() {
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Milliseconds
            private long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    // Double click
                    if (buttonClickListener != null) {
                        buttonClickListener.onItemDoubleClick(post);
                    }
                }
                lastClickTime = clickTime;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imgAuthorAvatar, imgAuthorPost, btnLove, btnComment,btnMore;
        StyledPlayerView videoAuthorPost;
        TextView txtAuthorName, txtLikes, txtAuthorName2, txtCaption, txtCommentNum, txtCreated;
        TableRow tbrCaption;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAuthorAvatar = itemView.findViewById(R.id.imgAuthorAvatar);
            imgAuthorPost = itemView.findViewById(R.id.imgAuthorPost);

            videoAuthorPost = itemView.findViewById(R.id.videoAuthorPost);
            btnLove = itemView.findViewById(R.id.btnLove);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnMore = itemView.findViewById(R.id.btnMore);
            txtAuthorName = itemView.findViewById(R.id.txtAuthorName);
            txtLikes = itemView.findViewById(R.id.txtLikes);
            txtAuthorName2 = itemView.findViewById(R.id.txtAuthorName2);
            txtCaption = itemView.findViewById(R.id.txtCaption);
            tbrCaption = itemView.findViewById(R.id.tbrCaption);
            txtCommentNum = itemView.findViewById(R.id.txtCommentNum);
            txtCreated = itemView.findViewById(R.id.txtCreated);
        }


    }
}
