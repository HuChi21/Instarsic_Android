package com.example.instagramclone.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.instagramclone.FCM.FCMController;
import com.example.instagramclone.R;
import com.example.instagramclone.activities.ArchiveActivity;
import com.example.instagramclone.activities.CheckProfileActivity;
import com.example.instagramclone.activities.MainActivity;
import com.example.instagramclone.activities.MessageActivity;
import com.example.instagramclone.activities.SignInActivity;
import com.example.instagramclone.activities.UseractivityActivity;
import com.example.instagramclone.adapters.FollowAdapter;
import com.example.instagramclone.adapters.PostGridAdapter;
import com.example.instagramclone.interfaces.PostItemClickListener;
import com.example.instagramclone.models.Follow;
import com.example.instagramclone.models.Notif;
import com.example.instagramclone.models.Post;
import com.example.instagramclone.models.User;
import com.example.instagramclone.utilities.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    private View view_loading, view_follow,view;
    private Toolbar toolbarProfile, toolbarFollow;
    private LinearLayout layoutEdit, layoutFollow;
    private TableRow btnActivity,btnArchive,btnUnknown,btnSignout;
    private ImageView imgAvatar, btnCreate;
    private TextView txtFullname, txtBio, txtPostNum, txtFollowerNum, txtFollowingNum;
    private MaterialButton btnEditProfile, btnShareProfile, btnFollow, btnChat;
    private RecyclerView recyclerPostGird, recyclerFollow;
    private PostGridAdapter postGridAdapter;
    private FollowAdapter followAdapter;
    private List<Post> postList;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Follow follow;
    private String username, fullname, avatar, uid = "", currentUid, currentUsername, currentAvt;

    private ScrollView scrollView;
    private FragmentManager fragmentManager;
    MainActivity mainActivity;
    BottomNavigationView bottomNavigationView;
    private BottomSheetDialog bottomSheetDialog;
    private View bottomSheetView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            mainActivity = (MainActivity) activity;
            fragmentManager = mainActivity.getSupportFragmentManager();
            bottomNavigationView = mainActivity.findViewById(R.id.bottomNavigationView);
        }

        SharedPreferences sharedPreferences = mainActivity.getSharedPreferences(Constants.KEY_CurrentUserPreference, Context.MODE_PRIVATE);
        currentUid = sharedPreferences.getString(Constants.KEY_currentUID, null);
        currentUsername = sharedPreferences.getString(Constants.KEY_currentUsername, null);
        currentAvt = sharedPreferences.getString(Constants.KEY_currentAvt, null);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        getWidget(view);
        Bundle bundle = getArguments();
        if (bundle != null) {
            uid = bundle.getString("uid");
        } else {
            uid = firebaseUser.getUid();
        }
        getUserInfo(uid);
        //user_post info
        getUserPost(uid);
        //follow info
        getUserFollower(uid);
        //following info
        getUserFollowing(uid);
        return view;
    }

    private void getWidget(View view) {
        scrollView = (ScrollView) view.findViewById(R.id.scrollView);

        toolbarProfile = view.findViewById(R.id.toolbarProfile);
        layoutEdit = view.findViewById(R.id.layoutEdit);
        layoutFollow = view.findViewById(R.id.layoutFollow);

        imgAvatar = view.findViewById(R.id.imgAvatar1);
        btnCreate = view.findViewById(R.id.btnCreate);
        txtFullname = view.findViewById(R.id.txtFullname);
        txtBio = view.findViewById(R.id.txtBio);
        txtPostNum = view.findViewById(R.id.txtPostNum);
        txtFollowerNum = view.findViewById(R.id.txtFollowerNum);
        txtFollowingNum = view.findViewById(R.id.txtFollowingNum);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnShareProfile = view.findViewById(R.id.btnShareProfile);
        btnFollow = view.findViewById(R.id.btnFollow);
        btnChat = view.findViewById(R.id.btnChat);
        recyclerPostGird = view.findViewById(R.id.recyclerPostGrid);

        //view
        view_loading = view.findViewById(R.id.viewLoading);
        ImageView imageView = view_loading.findViewById(R.id.imageView);
        Animation rotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        imageView.setAnimation(rotate);

        view_follow = view.findViewById(R.id.view_follow);
        recyclerFollow = view_follow.findViewById(R.id.recyclerFollow);
        toolbarFollow = view_follow.findViewById(R.id.toolbarFollow);

        btnCreate.setOnClickListener(this);
        txtPostNum.setOnClickListener(this);
        btnEditProfile.setOnClickListener(this);
        btnShareProfile.setOnClickListener(this);
        btnChat.setOnClickListener(this);
        btnFollow.setOnClickListener(this);

        recyclerPostGird.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        recyclerPostGird.setHasFixedSize(true);
        recyclerFollow.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false));
        recyclerFollow.setHasFixedSize(true);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCreate:
                break;
            case R.id.txtPostNum:
                int recyclerViewY = recyclerPostGird.getTop(); // Lấy vị trí Y của RecyclerView
                scrollView.smoothScrollTo(0, recyclerViewY);
                Toast.makeText(mainActivity, "Gone", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnFollow:
                actionFollow(uid);
                break;
            case R.id.btnEditProfile:
                break;
            case R.id.btnShareProfile:
                break;
            case R.id.btnChat:
                String[] uids = {currentUid, uid};
                Arrays.sort(uids);
                String chatId = uids[0] + "_" + uids[1];
                Intent intent = new Intent(mainActivity, MessageActivity.class);
                intent.putExtra("receiverId",uid);
                intent.putExtra("chatId",chatId);
                startActivity(intent);
                mainActivity.overridePendingTransition(R.anim.nothing,R.anim.slide_right);
                break;
        }
    }

    private void getUserInfo(String uid) {
        //user info
        databaseReference.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    username = user.getUsername();
                    fullname = user.getFullname();
                    avatar = user.getImageUrl();
                    if (username == null) {     //Check username: move CheckProfileActivity back if its null
                        mainActivity.finish();
                        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        startActivity(new Intent(mainActivity, CheckProfileActivity.class));
                    }
                    if (username.equals(currentUsername)) {
                        layoutEdit.setVisibility(View.VISIBLE);
                        layoutFollow.setVisibility(View.GONE);
                    } else {
                        layoutEdit.setVisibility(View.GONE);
                        layoutFollow.setVisibility(View.VISIBLE);
                        btnCreate.setVisibility(View.GONE);
                    }

                    txtFullname.setText(fullname);
                    Glide.with(mainActivity).load(avatar).dontAnimate().into(imgAvatar);
                    actionToolbar();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void actionToolbar(){
        ((AppCompatActivity) mainActivity).setSupportActionBar(toolbarProfile);
        toolbarProfile.setTitle(username);
        toolbarProfile.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mainActivity, "menu", Toast.LENGTH_SHORT).show();
                bottomSheetDialog = new BottomSheetDialog(mainActivity,R.style.BottomsheetDialogTheme);
                bottomSheetView = LayoutInflater.from(mainActivity)
                        .inflate(R.layout.settings_bottom_sheet,null);
                btnActivity = bottomSheetView.findViewById(R.id.btnActivity);
                btnArchive = bottomSheetView.findViewById(R.id.btnArchive);
                btnUnknown = bottomSheetView.findViewById(R.id.btnUnknown);
                btnSignout = bottomSheetView.findViewById(R.id.btnSignout);

                btnActivity.setOnClickListener(v1 ->{
                    startActivity(new Intent(mainActivity, UseractivityActivity.class));
                    mainActivity.overridePendingTransition(R.anim.nothing,R.anim.slide_right);
                    bottomSheetDialog.dismiss();});
                btnArchive.setOnClickListener(v1 -> {
                    bottomSheetDialog.dismiss();
                    startActivity(new Intent(mainActivity, ArchiveActivity.class));
                    mainActivity.overridePendingTransition(R.anim.nothing,R.anim.slide_right);
                });
//                btnUnknown.setOnClickListener(this);
                btnSignout.setOnClickListener(v1 -> {
                    bottomSheetDialog.dismiss();
                    firebaseAuth.signOut();
                    SharedPreferences sharedPreferences = mainActivity.getSharedPreferences(Constants.KEY_CurrentUserPreference, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    Toast.makeText(mainActivity, "Signout!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(mainActivity, SignInActivity.class));
                    mainActivity.overridePendingTransition(R.anim.nothing,R.anim.slide_right);
                    fragmentManager.popBackStackImmediate(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    mainActivity.finish();
                });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });
    }
    private void getUserPost(String uid) {
        postList = new ArrayList<>();
        databaseReference.child("posts").orderByChild("created")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (snapshot.exists()) {
                                Post post = dataSnapshot.getValue(Post.class);
                                if (post.getUserId().equals(uid)&& post.isArchive()==false)
                                    postList.add(post);
                            }
                        }
                        Collections.reverse(postList);
                        int postCount = postList.size();
                        txtPostNum.setText(String.valueOf(postCount));
                        postGridAdapter = new PostGridAdapter(mainActivity, postList, new PostItemClickListener() {
                            @Override
                            public void onClick(Post p) {
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("post", p);
                                PostFragment postFragment = new PostFragment();
                                postFragment.setArguments(bundle);
                                fragmentManager.beginTransaction()
                                        .setCustomAnimations(R.anim.nothing, R.anim.slide_right)
                                        .add(R.id.fragmentContainer, postFragment, "PostFragment")
                                        .addToBackStack("PostFragment")
                                        .commit();
                            }
                        });
                        recyclerPostGird.setAdapter(postGridAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
    private void getUserFollower(String uid) {
        toolbarFollow.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24);
        toolbarFollow.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (view_follow.getVisibility() == View.VISIBLE)
                    view_follow.setVisibility(View.GONE);
            }
        });
        List<Follow> followerList = new ArrayList<>();
        databaseReference.child("follows").orderByChild("created")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (snapshot.exists()) {
                                Follow follow1 = dataSnapshot.getValue(Follow.class);
                                if (follow1.getFollowingId().equals(currentUid)) {
                                    btnFollow.setText("Following");
                                    btnFollow.setTextColor(Color.parseColor("#121212"));
                                    btnFollow.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                                }
                                if (follow1.getFollowerId().equals(uid)){
                                    followerList.add(follow1);
                                }
                            }
                        }
                        Collections.reverse(followerList);
                        int followerNum = followerList.size();
                        txtFollowerNum.setText(String.valueOf(followerNum));
                        followAdapter = new FollowAdapter(mainActivity, followerList, currentUid,currentUsername, true);
                        recyclerFollow.setAdapter(followAdapter);
                        txtFollowerNum.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                view_follow.setVisibility(View.VISIBLE);
                                toolbarFollow.setTitle("Follower");
                                Toast.makeText(mainActivity, "Follower", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }
    private void getUserFollowing(String uid) {
        List<Follow> followingList = new ArrayList<>();
        databaseReference.child("follows").orderByChild("created")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (snapshot.exists()) {
                                Follow follow1 = dataSnapshot.getValue(Follow.class);
                                if (follow1.getFollowingId().equals(currentUid)) {
                                    btnFollow.setText("Following");
                                    btnFollow.setTextColor(Color.parseColor("#121212"));
                                    btnFollow.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                                }
                                if (follow1.getFollowingId().equals(uid)) {
                                    followingList.add(follow1);
                                }

                            }
                        }
                        Collections.reverse(followingList);
                        txtFollowingNum.setText(String.valueOf(followingList.size()));
                        followAdapter = new FollowAdapter(mainActivity, followingList, currentUid,currentUsername, false);
                        recyclerFollow.setAdapter(followAdapter);
                        txtFollowingNum.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                view_follow.setVisibility(View.VISIBLE);
                                toolbarFollow.setTitle("Following");
                                Toast.makeText(mainActivity, "Following", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
    private void actionFollow(String uid) {
        DatabaseReference followRef = databaseReference.child("follows").child(currentUid + "_" + uid);
        followRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    followRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(mainActivity, "unfollow chxtdmm", Toast.LENGTH_SHORT).show();
                            btnFollow.setText("Follow");
                            btnFollow.setTextColor(Color.parseColor("#ffffff"));
                            btnFollow.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1a73e8")));
                            getUserFollower(uid);
                            getUserFollowing(uid);
                        }
                    });
                } else {
                    follow = new Follow(uid, currentUid, false, Timestamp.now().getSeconds());
                    followRef.setValue(follow).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(mainActivity, "follow ne", Toast.LENGTH_SHORT).show();
                            btnFollow.setText("Following");
                            btnFollow.setTextColor(Color.parseColor("#121212"));
                            btnFollow.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                            getUserFollower(uid);
                            getUserFollowing(uid);
                        }
                    });
                    //send notification for uid
                    if (!uid.equals(currentUid)){
                        String notifId = databaseReference.child("notifications/"+uid).push().getKey();
                        Notif notif = new Notif(notifId,uid,currentUid,"Follow",currentUid + "_" + uid,Timestamp.now().getSeconds());
                        databaseReference.child("notifications/"+uid).child(notifId).setValue(notif);
                        String body = currentUsername+" started following you.";
                        pushNotifToUid( uid,body);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
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
}