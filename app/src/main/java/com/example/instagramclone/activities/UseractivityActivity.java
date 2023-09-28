package com.example.instagramclone.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.print.PrintDocumentAdapter;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TableRow;

import com.example.instagramclone.R;
import com.example.instagramclone.adapters.CommentActivitiesAdapter;
import com.example.instagramclone.adapters.CommentAdapter;
import com.example.instagramclone.adapters.PostGridAdapter;
import com.example.instagramclone.interfaces.PostItemClickListener;
import com.example.instagramclone.models.Comment;
import com.example.instagramclone.models.Post;
import com.example.instagramclone.utilities.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UseractivityActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar toolbarActivity;
    private LinearLayout layoutActivity;
    private TableRow btnLikeActivity,btnCommentActivity;
    private RecyclerView recyclerLikes,recyclerComments;
    private List<Post> postList = new ArrayList<>();
    private List<Comment> commentList = new ArrayList<>();
    private CommentActivitiesAdapter commentActivitiesAdapter;
    private PostGridAdapter postGridAdapter;
    private DatabaseReference databaseReference;
    private String currentUid,currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_useractivity);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.KEY_CurrentUserPreference, MODE_PRIVATE);
        currentUid = sharedPreferences.getString(Constants.KEY_currentUID, null);
        currentUsername = sharedPreferences.getString(Constants.KEY_currentUsername, null);
        getWidget();
        actionToolbar();
    }

    private void actionToolbar() {
        setSupportActionBar(toolbarActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarActivity.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24);
        toolbarActivity.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void getWidget() {
        toolbarActivity = findViewById(R.id.toolbarActivity);
        layoutActivity = findViewById(R.id.layoutActivity);
        btnLikeActivity = findViewById(R.id.btnLikeActivity);
        btnCommentActivity = findViewById(R.id.btnCommentActivity);
        recyclerLikes = findViewById(R.id.recyclerLikes);
        recyclerComments = findViewById(R.id.recyclerComments);

        recyclerLikes.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        recyclerLikes.setHasFixedSize(true);
        recyclerComments.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerComments.setHasFixedSize(true);

        btnLikeActivity.setOnClickListener(this);
        btnCommentActivity.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLikeActivity:
                layoutActivity.setVisibility(View.GONE);
                getLike();
                recyclerLikes.setVisibility(View.VISIBLE);
                recyclerComments.setVisibility(View.GONE);
                break;
            case R.id.btnCommentActivity:
                layoutActivity.setVisibility(View.GONE);
                getComment();
                recyclerLikes.setVisibility(View.VISIBLE);
                recyclerComments.setVisibility(View.GONE);
                break;
        }
    }

    private void getLike() {
        databaseReference.child("posts").orderByChild("created").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if(snapshot.exists()){
                                if(dataSnapshot.child("like").hasChild(currentUid)){
                                databaseReference.child("posts/"+dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            Post post = snapshot.getValue(Post.class);
                                            postList.add(post);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                            }
                        }
                        Collections.reverse(postList);
                        postGridAdapter = new PostGridAdapter(getApplicationContext(), postList, new PostItemClickListener() {
                            @Override
                            public void onClick(Post post) {

                            }
                        });
                        recyclerLikes.setAdapter(postGridAdapter);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
    }
    private void getComment() {
        databaseReference.child("comments").orderByChild("created").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (snapshot.exists()) {
                        String userId = dataSnapshot.child("userId").getValue(String.class);
                        if(userId.equals(currentUid)){
                            Comment comment = dataSnapshot.getValue(Comment.class);
                            commentList.add(comment);
                        }
                    }
                }
                Collections.reverse(commentList);
                commentActivitiesAdapter = new CommentActivitiesAdapter(getApplicationContext(),commentList) ;
                recyclerComments.setAdapter(commentActivitiesAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.nothing, R.anim.slide_out);
    }
}