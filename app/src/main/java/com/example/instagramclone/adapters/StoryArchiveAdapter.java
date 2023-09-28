package com.example.instagramclone.adapters;

import android.content.Context;
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
import com.example.instagramclone.R;
import com.example.instagramclone.models.Post;
import com.example.instagramclone.models.Story;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StoryArchiveAdapter extends RecyclerView.Adapter<StoryArchiveAdapter.MyViewHolder> {
    Context context;
    List<Story> list;
    FragmentManager fragmentManager ;
    String username;

    public StoryArchiveAdapter(Context context, List<Story> list,FragmentManager fragmentManager,String username) {
        this.context = context;
        this.list = list;
        this.fragmentManager = fragmentManager;
        this.username = username;
    }
    @NonNull
    @Override
    public StoryArchiveAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_story_archive,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryArchiveAdapter.MyViewHolder holder, int position) {
        Story story = list.get(position);
        if(story == null){
            return;
        }
        Glide.with(context).load(story.getMediaUrl()).dontAnimate().into(holder.imgPostPhoto);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(story.getCreated());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH)+ 1;
        holder.txtDay.setText(String.valueOf(day));
        holder.txtMonth.setText(String.valueOf(month));
        holder.imgPostPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<MyStory> storyList = new ArrayList<>();
                MyStory myStory = new MyStory(story.getMediaUrl(),new Date(story.getCreated()*1000),"" );
                storyList.add(myStory);
                new StoryView.Builder(fragmentManager)
                    .setStoriesList(storyList) // Required
                    .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                    .setTitleText(username) // Default is Hidden
                    .setTitleLogoUrl("") // Default is Hidden
                    .setStoryClickListeners(new StoryClickListeners() {
                        @Override
                        public void onDescriptionClickListener(int position) {
                            //your action
                        }
                        @Override
                        public void onTitleIconClickListener(int position) {
                            //your action
                        }
                    }) // Optional Listeners
                    .build() // Must be called before calling show method
                    .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder  {
        ImageView imgPostPhoto;
        TextView txtDay,txtMonth;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPostPhoto = itemView.findViewById(R.id.imgPostPhoto);
            txtDay = itemView.findViewById(R.id.txtDay);
            txtMonth = itemView.findViewById(R.id.txtMonth);
        }


    }
}
