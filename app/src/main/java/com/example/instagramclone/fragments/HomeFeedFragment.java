package com.example.instagramclone.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.FCM.FCMController;
import com.example.instagramclone.R;
import com.example.instagramclone.activities.ChatActivity;
import com.example.instagramclone.activities.MainActivity;
import com.example.instagramclone.activities.SignInActivity;
import com.example.instagramclone.adapters.PostAdapter;
import com.example.instagramclone.adapters.StoryUserAdapter;
import com.example.instagramclone.interfaces.ImageClickListener;
import com.example.instagramclone.interfaces.PostButtonClickListener;
import com.example.instagramclone.models.Like;
import com.example.instagramclone.models.Notif;
import com.example.instagramclone.models.Post;
import com.example.instagramclone.models.Story;
import com.example.instagramclone.models.User;
import com.example.instagramclone.utilities.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class HomeFeedFragment extends Fragment implements View.OnClickListener {
    private static final long oneDayAgo = Timestamp.now().getSeconds() - (24 * 60 * 60);
    private Toolbar toolbar;
    private TextView txtHome,txtFinish;
    private ImageView btnNotif, btnChat, imgPostPhoto;
    private LinearLayout layoutPrePost;
    private RecyclerView storyRecycler, postRecycler;
    private View loading,view_more,view_yourMore,view_theirMore;
    private CardView btnCloseMore;
    private TableRow btnDelete,btnReport;
    private NestedScrollView nestedScrollView;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private StorageReference storageRef;

    private PostAdapter postAdapter;
    private StoryUserAdapter storyUserAdapter;
    private boolean isReply = false;
    SharedPreferences sharedPreferences;
    String currentUid, currentUsername, currentAvt;
    private ProgressBar progressBar;
    private FragmentManager fragmentManager;
    private MainActivity mainActivity;
    private ArrayList<MyStory> storyList = new ArrayList<>();

    private Story story = new Story();
    private User storyUser = new User();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        if(activity instanceof MainActivity){
            mainActivity = (MainActivity) activity;
            fragmentManager = mainActivity.getSupportFragmentManager();
        }

        sharedPreferences = mainActivity.getSharedPreferences(Constants.KEY_CurrentUserPreference, Context.MODE_PRIVATE);
        currentUid = sharedPreferences.getString(Constants.KEY_currentUID, null);
        currentUsername = sharedPreferences.getString(Constants.KEY_currentUsername, null);
        currentAvt = sharedPreferences.getString(Constants.KEY_currentAvt, null);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_homefeed, container, false);

        getWidget(view);
        loadingPrePost();
        getStoryUser();
        return view;
    }

    private void getWidget(View view) {
        toolbar = view.findViewById(R.id.toolbarHome);
        layoutPrePost = view.findViewById(R.id.layoutPrePost);
        txtFinish = view.findViewById(R.id.txtFinish);
        imgPostPhoto = view.findViewById(R.id.imgPostPhoto);
        progressBar = view.findViewById(R.id.progressBar);
        txtHome = (TextView) view.findViewById(R.id.txtHome);
        btnNotif = (ImageView) view.findViewById(R.id.btnNotif);
        btnChat = (ImageView) view.findViewById(R.id.btnChat);
        storyRecycler = (RecyclerView) view.findViewById(R.id.recyclerStoryUser);
        postRecycler = (RecyclerView) view.findViewById(R.id.recyclerPost);
        nestedScrollView = view.findViewById(R.id.nestedScrollView);

        //view
        loading = view.findViewById(R.id.loading);
        ImageView imageView = loading.findViewById(R.id.imageView);
        Animation rotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        imageView.setAnimation(rotate);

        view_more = view.findViewById(R.id.view_more);
        btnCloseMore = view_more.findViewById(R.id.btnCloseMore);
        view_yourMore = view_more.findViewById(R.id.view_yourMore);
        btnDelete = view_yourMore.findViewById(R.id.btnDelete);
        view_theirMore = view_more.findViewById(R.id.view_theirMore);
        btnReport = view_theirMore.findViewById(R.id.btnReport);


        btnCloseMore.setOnClickListener(this);
        txtHome.setOnClickListener(this);
        btnNotif.setOnClickListener(v -> {
            firebaseAuth.signOut();

            SharedPreferences sharedPreferences = mainActivity.getSharedPreferences(Constants.KEY_CurrentUserPreference, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(mainActivity, "Signout!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(mainActivity, SignInActivity.class));
            fragmentManager.popBackStackImmediate(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
            mainActivity.finish();
        });
        btnChat.setOnClickListener(this);
        nestedScrollView.setOnClickListener(this);

        storyRecycler.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false));
        storyRecycler.setHasFixedSize(true);
        postRecycler.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false));
        postRecycler.setHasFixedSize(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtHome:
                nestedScrollView.smoothScrollTo(0, 0);
                break;

            case R.id.btnChat:
                Intent intent = new Intent(mainActivity, ChatActivity.class);
                startActivity(intent);
                mainActivity.overridePendingTransition(R.anim.nothing,R.anim.slide_in);
                break;

            case R.id.btnCloseMore:
                if(view_more.getVisibility()==View.VISIBLE)view_more.setVisibility(View.GONE);
                break;

        }
    }
    private void loadingPrePost() {
        getPost();
        Bundle bundle = getArguments();
        if (bundle != null) {
            Uri uri = Uri.parse(bundle.getString("mediaUri"));
            Post post = (Post) bundle.getSerializable("post");
            String mediaType = mainActivity.getContentResolver().getType(uri);
            Glide.with(mainActivity).load(uri).dontAnimate().into(imgPostPhoto);

            UploadTask uploadTask = storageRef.child("post_media/" + currentUid + "/" + post.getPostId()).putFile(uri);
            uploadTask.addOnProgressListener(taskSnapshot -> {
                layoutPrePost.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressBar.setProgress((int) progress);
            }).addOnSuccessListener(taskSnapshot -> {
                AlphaAnimation alphaAnimation = new AlphaAnimation(0.8f, 0.0f);
                alphaAnimation.setDuration(1000);
                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        layoutPrePost.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                Drawable drawable = AppCompatResources.getDrawable(mainActivity, R.drawable.baseline_check_24);
                DrawableCompat.setTint(drawable, Color.parseColor("#121212"));
                txtFinish.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null);
                txtFinish.setText("Finished");

                storageRef.child("post_media/" + currentUid + "/" + post.getPostId())
                        .getDownloadUrl().addOnSuccessListener(mediaUri -> {
                            String mediaUrl = mediaUri.toString();
                            post.setMediaUrl(mediaUrl);
                            Log.d("url", mediaUrl);
                            databaseReference.child("posts").child(post.getPostId()).setValue(post)
                                    .addOnSuccessListener(aVoid -> {
                                        layoutPrePost.startAnimation(alphaAnimation);
                                    });
                        });
            });
        } else {
            layoutPrePost.setVisibility(View.GONE);
        }
    }

    private void getStoryUser() {
        List<String> storyUserList = new ArrayList<>();
        storyUserList.add(currentUid);
        //get person's uid who user follows
        databaseReference.child("follows").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (snapshot.exists()) {
                        String followId = dataSnapshot.getKey();
                        if(followId.contains(currentUid+"_")){
                            String uid = followId.replace(currentUid+"_","");
                            //get their stories order by Modified
                            databaseReference.child("stories").orderByChild("modified")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                if (snapshot.exists()) {
                                                    String userId = dataSnapshot.getKey();
                                                    Long modified = dataSnapshot.child("modified").getValue(Long.class);
                                                    if (userId.equals(uid) && modified>oneDayAgo) storyUserList.add(userId);
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                        }
                    }
                }
                storyUserAdapter = new StoryUserAdapter(mainActivity, storyUserList, currentUid, new ImageClickListener() {
                    @Override
                    public void onImageClick(String uid) {
                        if(storyList.size()>0){
                            storyList.clear();
                        }else{
                            getStory(uid);
                        }
                    }
                });
                storyRecycler.setAdapter(storyUserAdapter);
                Log.d("count", String.valueOf(storyUserList.size()));

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }

    private void getStory(String uid) {
        databaseReference.child("users/"+uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    storyUser = snapshot.getValue(User.class);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        databaseReference.child("stories/"+uid+"/storyList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                    if (snapshot.exists()) {
                        story = dataSnapshot1.getValue(Story.class);
                        String mediaUrl = story.getMediaUrl();
                        Long created = story.getCreated();
                        if (created > oneDayAgo) {
                            MyStory story = new MyStory(mediaUrl,new Date(created*1000),"" );
                            storyList.add(story);
                        }
                    }
                }
                if(storyList.size()>0){
                    new StoryView.Builder(mainActivity.getSupportFragmentManager())
                            .setStoriesList(storyList) // Required
                            .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                            .setTitleText(storyUser.getUsername()) // Default is Hidden
                            .setTitleLogoUrl(storyUser.getImageUrl()) // Default is Hidden
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
                }else{
                    fragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.nothing,R.anim.slide_up)
                            .add(R.id.fragmentContainer, new StoryFragment(),"StoryFragment")
                            .addToBackStack("StoryFragment")
                            .commit();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void getPost() {
        DatabaseReference postRef = databaseReference.child("posts");
        postRef.orderByChild("created").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Post> postList = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Post post = dataSnapshot.getValue(Post.class);
                        if(post.isArchive()==false){
                            postList.add(post);
                        }
                    }
                    Collections.reverse(postList);
                    postAdapter = new PostAdapter(getActivity(), postList, user.getUid(), new PostButtonClickListener() {
                        @Override
                        public void onItemDoubleClick(Post post) {
                            actionLike(post);
                        }

                        @Override
                        public void onImageClick(Post post) {
                            actionGetUser(post.getUserId());
                        }
                        @Override
                        public void onLikeClick(Post p) {
                            actionLike(p);
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
                                if (post.getUserId().equals(currentUid)) {
                                    view_theirMore.setVisibility(View.GONE);
                                    view_yourMore.setVisibility(View.VISIBLE);
                                    btnDelete.setOnClickListener(v->actionDeletePost(post));
                                } else {
                                    view_theirMore.setVisibility(View.VISIBLE);
                                    view_yourMore.setVisibility(View.GONE);
                                    btnReport.setOnClickListener(v -> Toast.makeText(mainActivity, "Reported!", Toast.LENGTH_SHORT).show());
                                }
                            }
                        }
                        @Override
                        public void onShareClick() {Toast.makeText(mainActivity, "Share click", Toast.LENGTH_SHORT).show();}
                        @Override
                        public void onSaveClick() {}
                    });
                    postRecycler.setAdapter(postAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void actionLike(Post p) {
        DatabaseReference likesRef = databaseReference.child("post_likes").child(p.getPostId()).child("likes");
        likesRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isLiked = snapshot.exists();
                if (isLiked) {
                    // Người dùng đã thích bài đăng, cho phép bỏ thích bài đăng
                    likesRef.child(currentUid).removeValue();
                } else {
                    // Người dùng chưa thích bài đăng, cho phép thích bài đăng
                    Like like = new Like(currentUid, true, Timestamp.now().getSeconds());
                    likesRef.child(currentUid).setValue(like);
                    //send notification for uid
                    if(!p.getUserId().equals(currentUid)){
                        String body = currentUsername+" liked your post";
                        String notifId = databaseReference.child("notifications/"+p.getUserId()).push().getKey();
                        Notif notif = new Notif(notifId,p.getUserId(),currentUid,"Like_post",p.getPostId(),Timestamp.now().getSeconds());
                        databaseReference.child("notifications/"+p.getUserId()).child(notifId).setValue(notif);
                        pushNotifToUid(p,body);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void pushNotifToUid(Post p,String body){
        databaseReference.child("users/"+p.getUserId()).addValueEventListener(new ValueEventListener() {
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
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);
        ProfileFragment profileFragment = new ProfileFragment();
        profileFragment.setArguments(bundle);
        fragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, profileFragment,"ProfileFragment")
                .setCustomAnimations(R.anim.nothing, R.anim.slide_left)
                .addToBackStack("ProfileFragment")
                .commit();
        Log.d("moveUserUID", "Chuyển sang" + uid);
    }
    private void actionDeletePost(Post post) {
        loading.setVisibility(View.VISIBLE);
        view_more.setVisibility(View.GONE);
        databaseReference.child("comments").orderByChild("postid").equalTo( post.getPostId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    databaseReference.child("comments/" + dataSnapshot.getKey() + "/likes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                databaseReference.child("comments/" + dataSnapshot.getKey() + "/likes").child(dataSnapshot1.getKey()).removeValue();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {  }
                    });
                    databaseReference.child("comments/" + dataSnapshot.getKey() + "/replies").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                databaseReference.child("comments/" + dataSnapshot.getKey() + "/replies").child(dataSnapshot1.getKey()).removeValue();
                                databaseReference.child("comments/" + dataSnapshot.getKey() + "/replies/"+dataSnapshot1.getKey()+"/likes").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot dataSnapshot2 : snapshot.getChildren()) {
                                            databaseReference.child("comments/" + dataSnapshot.getValue() + "/replies/"+dataSnapshot1.getValue()+"/likes").child(dataSnapshot2.getKey()).removeValue();}
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
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
        FirebaseStorage.getInstance().getReference().child(pathMedia).delete();
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
                        loading.setVisibility(View.GONE);
                        mainActivity.getSupportFragmentManager().popBackStack();
                        // tạo loading view và postAdapter.notifyDataSetChanged
                    }
                });
    }

    private void closekeyboard() {
        View view = mainActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}