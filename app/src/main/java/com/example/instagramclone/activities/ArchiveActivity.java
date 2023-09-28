package com.example.instagramclone.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.instagramclone.R;
import com.example.instagramclone.adapters.PostGridAdapter;
import com.example.instagramclone.adapters.StoryArchiveAdapter;
import com.example.instagramclone.interfaces.PostItemClickListener;
import com.example.instagramclone.models.Post;
import com.example.instagramclone.models.Story;
import com.example.instagramclone.utilities.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArchiveActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar toolbarArchive;
    private ImageView btnStorySwap,btnPostSwap;
    private RecyclerView recyclerPostArchive,recyclerStoryArchive;
    private PostGridAdapter postGridAdapter;
    private StoryArchiveAdapter storyArchiveAdapter;
    private List<Post> postList = new ArrayList<>();
    private List<Story> storyList = new ArrayList<>();
    private DatabaseReference databaseReference;
    private String currentUid,curentUsername,currentAvt;
//    private
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.KEY_CurrentUserPreference,MODE_PRIVATE);
        currentUid =sharedPreferences.getString(Constants.KEY_currentUID,null);
        curentUsername =sharedPreferences.getString(Constants.KEY_currentUsername,null);
        currentAvt =sharedPreferences.getString(Constants.KEY_currentAvt,null);

        getWidget();
        actionToolbar();
        getArchives();
    }

    private void getWidget() {
        toolbarArchive = findViewById(R.id.toolbarArchive);
        btnStorySwap = findViewById(R.id.btnStorySwap);
        btnPostSwap = findViewById(R.id.btnPostSwap);
        recyclerPostArchive = findViewById(R.id.recyclerPostArchive);
        recyclerStoryArchive = findViewById(R.id.recyclerStoryArchive);

        btnPostSwap.setOnClickListener(this);
        btnStorySwap.setOnClickListener(this);
        recyclerPostArchive.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        recyclerPostArchive.setHasFixedSize(true);
        recyclerStoryArchive.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        recyclerStoryArchive.setHasFixedSize(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnStorySwap:
                btnStorySwap.setVisibility(View.GONE);
                btnPostSwap.setVisibility(View.VISIBLE);
                recyclerPostArchive.setVisibility(View.GONE);
                recyclerStoryArchive.setVisibility(View.VISIBLE);
                toolbarArchive.setTitle("Story archives");
                break;
            case R.id.btnPostSwap:
                btnStorySwap.setVisibility(View.VISIBLE);
                btnPostSwap.setVisibility(View.GONE);
                recyclerPostArchive.setVisibility(View.VISIBLE);
                recyclerStoryArchive.setVisibility(View.VISIBLE);
                toolbarArchive.setTitle("Post archives");
                break;
        }
    }
    private void actionToolbar(){
        setSupportActionBar(toolbarArchive);
        toolbarArchive.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    private void getArchives() {
        btnStorySwap.setVisibility(View.VISIBLE);
        recyclerPostArchive.setVisibility(View.VISIBLE);
        toolbarArchive.setTitle("Post archives");

        //get all post archive
        databaseReference.child("posts").orderByChild("created")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (snapshot.exists()) {
                                Post post = dataSnapshot.getValue(Post.class);
                                if (post.getUserId().equals(currentUid) && post.isArchive() == false)
                                    postList.add(post);
                            }
                        }
                        Collections.reverse(postList);
                        postGridAdapter = new PostGridAdapter(getApplicationContext(), postList, new PostItemClickListener() {
                            @Override
                            public void onClick(Post post) {

                            }
                        });
                        recyclerPostArchive.setAdapter(postGridAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        //get all story archive
        databaseReference.child("stories/" + currentUid + "/storyList").orderByChild("created").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                    if (snapshot.exists()) {
                        Story str = dataSnapshot1.getValue(Story.class);
                        storyList.add(str);
                    }
                }
                Collections.reverse(storyList);
                storyArchiveAdapter = new StoryArchiveAdapter(getApplicationContext(),storyList,getSupportFragmentManager(),curentUsername);
                recyclerStoryArchive.setAdapter(storyArchiveAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.nothing,R.anim.slide_out);
        super.onBackPressed();
    }
}