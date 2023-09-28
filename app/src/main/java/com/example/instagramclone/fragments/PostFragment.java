package com.example.instagramclone.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagramclone.FCM.FCMController;
import com.example.instagramclone.R;
import com.example.instagramclone.activities.MainActivity;
import com.example.instagramclone.adapters.PostAdapter;
import com.example.instagramclone.interfaces.PostButtonClickListener;
import com.example.instagramclone.models.Like;
import com.example.instagramclone.models.Notif;
import com.example.instagramclone.models.Post;
import com.example.instagramclone.utilities.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostFragment extends Fragment implements View.OnClickListener {
    private RecyclerView recyclerPost;
    private Toolbar toolbarPostDetail;
    private PostAdapter postAdapter;
    private Post post;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private View view_loading,view_more;
    private LinearLayout view_yourMore,view_theirMore;
    private TableRow btnDelete,btnReport;
    private CardView btnCloseMore;
    String currentUID,currentUsername;
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        if(activity instanceof MainActivity){
            mainActivity = (MainActivity) activity;
            fragmentManager = mainActivity.getSupportFragmentManager();
            bottomNavigationView = mainActivity.findViewById(R.id.bottomNavigationView);
            bottomNavigationView.setVisibility(View.GONE);
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        SharedPreferences sharedPreferences = mainActivity.getSharedPreferences(Constants.KEY_CurrentUserPreference,Context.MODE_PRIVATE);
        currentUID = sharedPreferences.getString(Constants.KEY_currentUID,null);
        currentUsername = sharedPreferences.getString(Constants.KEY_currentUsername,null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        getWidget(view);
        actionToolbar();
        Bundle bundle = getArguments();
        if (bundle != null) {
            post = (Post) bundle.getSerializable("post");
            getPostDetail(post);
        }

        return view;
    }
    private void getWidget(View view) {
        recyclerPost = view.findViewById(R.id.recyclerPost);
        toolbarPostDetail = view.findViewById(R.id.toolbarPostDetail);

        view_loading = view.findViewById(R.id.loading);
        view_more = view.findViewById(R.id.view_more);
        btnCloseMore = view_more.findViewById(R.id.btnCloseMore);
        view_yourMore = view_more.findViewById(R.id.view_yourMore);
        btnDelete = view_yourMore.findViewById(R.id.btnDelete);
        view_theirMore = view_more.findViewById(R.id.view_theirMore);
        btnReport = view_theirMore.findViewById(R.id.btnReport);


        btnDelete.setOnClickListener(this);
        btnCloseMore.setOnClickListener(this);
        btnReport.setOnClickListener(this);


        recyclerPost.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false));
        recyclerPost.setHasFixedSize(true);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnDelete:
                actionDeletePost(post);
                break;
            case R.id.btnReport:
                Toast.makeText(mainActivity, "Reported!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnCloseMore:
                if(view_more.getVisibility()==View.VISIBLE)view_more.setVisibility(View.GONE);
                break;
        }
    }


    private void actionToolbar() {
        ((AppCompatActivity) mainActivity).setSupportActionBar(toolbarPostDetail);
        ((AppCompatActivity) mainActivity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarPostDetail.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24);
        toolbarPostDetail.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentManager.popBackStack();
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });

    }
    private void getPostDetail(Post p) {
        List<Post> postList = new ArrayList<>();
        databaseReference.child("posts").orderByChild("created")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Post post = dataSnapshot.getValue(Post.class);
                            if (post != null && post.getUserId().equals(p.getUserId())&& post.isArchive()==false) {
                                if(p.getCreated() >= post.getCreated()){
                                    postList.add(post);
                                }
                            }
                        }
                        Collections.reverse(postList);
                        postAdapter = new PostAdapter(mainActivity, postList, currentUID, new PostButtonClickListener() {
                            @Override
                            public void onItemDoubleClick(Post post) {
                                actionLikePost(post);
                            }

                            @Override
                            public void onImageClick(Post p) {
                                actionGetUser(p.getUserId());
                            }
                            @Override
                            public void onLikeClick(Post p) {
                                actionLikePost(p);
                            }
                            @Override
                            public void onCommentClick(Post p) {
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("post", p);
                                CommentFragment commentFragment = new CommentFragment();
                                commentFragment.setArguments(bundle);
                                fragmentManager.beginTransaction()
                                        .setCustomAnimations(R.anim.nothing, R.anim.slide_up)
                                        .add(R.id.fragmentContainer, commentFragment,"CommentFragment")
                                        .addToBackStack("CommentFragment")
                                        .commit();
                            }
                            @Override
                            public void onMoreClick(Post post) {
                                if (view_more.getVisibility() == View.GONE) {
                                    view_more.setVisibility(View.VISIBLE);
                                    if (post.getUserId().equals(currentUID)) {
                                        view_theirMore.setVisibility(View.GONE);
                                        view_yourMore.setVisibility(View.VISIBLE);
                                    } else {
                                        view_theirMore.setVisibility(View.VISIBLE);
                                        view_yourMore.setVisibility(View.GONE);
                                    }
                                }
                            }
                            @Override
                            public void onShareClick() {
                                Toast.makeText(mainActivity, "Share click", Toast.LENGTH_SHORT).show();}
                            @Override
                            public void onSaveClick() {}
                        });
                        recyclerPost.setAdapter(postAdapter);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void actionLikePost(Post p) {
        DatabaseReference likesRef = databaseReference.child("post_likes").child(p.getPostId()).child("likes");
        likesRef.child(currentUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isLiked = snapshot.exists();
                if (isLiked) {
                    // Người dùng đã thích bài đăng, cho phép bỏ thích bài đăng
                    likesRef.child(currentUsername).removeValue();
                } else {
                    // Người dùng chưa thích bài đăng, cho phép thích bài đăng
                    Like like = new Like(currentUsername, true, Timestamp.now().getSeconds());
                    likesRef.child(currentUsername).setValue(like);
                    //send notification for uid
                    if(!p.getUserId().equals(currentUID)){
                        String notifId = databaseReference.child("notifications/"+p.getUserId()).push().getKey();
                        Notif notif = new Notif(notifId,p.getUserId(),currentUID,"Like_post",p.getPostId(),Timestamp.now().getSeconds());
                        databaseReference.child("notifications/"+p.getUserId()).child(notifId).setValue(notif);
                        String body = currentUsername +" liked your post";
                        pushNotifToUid(p.getUserId(),body);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }
    private void pushNotifToUid(String uid,String body){
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
    private void actionGetUser(String uid) {
        DatabaseReference usersRef = databaseReference.child("users");
        usersRef.orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", uid);
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


    private void actionDeletePost(Post post) {
        view_loading.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //delete comment data
                databaseReference.child("comments").orderByChild("postid").equalTo(post.getPostId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            databaseReference.child("comments/" + dataSnapshot.getKey() + "/likes").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                        databaseReference.child("comments/" + dataSnapshot.getKey() + "/likes").child(dataSnapshot1.getKey()).removeValue();}
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { }
                            });
                            databaseReference.child("comments/" + dataSnapshot.getKey() + "/replies").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                        databaseReference.child("comments/" + dataSnapshot.getKey() + "/replies").child(dataSnapshot1.getKey()).removeValue();
                                        databaseReference.child("comments/" + dataSnapshot.getKey() + "/replies/"+dataSnapshot1.getKey()+"/likes").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot dataSnapshot2 : snapshot.getChildren()) {databaseReference.child("comments/" + dataSnapshot.getKey() + "/replies/"+dataSnapshot1.getKey()+"/likes").child(dataSnapshot2.getKey()).removeValue();}
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) { }
                                        });
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                            databaseReference.child("comments").child(dataSnapshot.getKey()).removeValue();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
                //delete post media
                String millisec = String.valueOf(post.getCreated());
                String pathMedia = "post_media/" + post.getUserId() + "_post_" + millisec ;
                storageReference.child(pathMedia).delete();
                //delete post data
                databaseReference.child("posts/"+post.getPostId()+"/likes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            databaseReference.child("posts/"+post.getPostId()+"/likes").child(dataSnapshot.getKey()).removeValue();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
                databaseReference.child("posts").child(post.getPostId())
                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(mainActivity, "Post has been deleted!", Toast.LENGTH_SHORT).show();
                                view_loading.setVisibility(View.GONE);
                                fragmentManager.popBackStack();
                                bottomNavigationView.setVisibility(View.VISIBLE);
                                // tạo loading view và postAdapter.notifyDataSetChanged
                            }
                        });
            }
        }, 1500);
    }

    private void closekeyboard() {
        View view = mainActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}