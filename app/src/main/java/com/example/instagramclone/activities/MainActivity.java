package com.example.instagramclone.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.instagramclone.R;
import com.example.instagramclone.fragments.HomeFeedFragment;
import com.example.instagramclone.fragments.PostCreateFragment;
import com.example.instagramclone.fragments.ProfileFragment;
import com.example.instagramclone.fragments.SearchFragment;
import com.example.instagramclone.fragments.NotificationFragment;
import com.example.instagramclone.models.User;
import com.example.instagramclone.utilities.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private View loading;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private String currentUid, currentUsername, currentAvt, currentSignin;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private SharedPreferences sharedPreferences;
    private boolean doubleBackToExitPressedOnce;
    private FragmentManager fragmentManager;
    private HomeFeedFragment homeFeedFragment;
    private SearchFragment searchFragment;
    private PostCreateFragment postCreateFragment;
    private NotificationFragment notificationFragment;
    private ProfileFragment profileFragment;
    private String fragment;
    private Bundle bundle;
    private int selectItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        user = firebaseAuth.getCurrentUser();
        sharedPreferences = getSharedPreferences(Constants.KEY_CurrentUserPreference, Context.MODE_PRIVATE);
        currentUid = sharedPreferences.getString(Constants.KEY_currentUID, null);
        currentUsername = sharedPreferences.getString(Constants.KEY_currentUsername, null);
        currentAvt = sharedPreferences.getString(Constants.KEY_currentAvt, null);
        fragment = getIntent().getStringExtra("fragment");
        bundle = getIntent().getBundleExtra("bundle");
        getWidget();
        getToken();

    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        // Get new FCM registration token
                        String token = task.getResult();
                        Map<String,Object> hashMap = new HashMap<>();
                        hashMap.put("token",token);
                        databaseReference.child("users/"+user.getUid()).updateChildren(hashMap);
                    }
                });

    }

    private void actionCheckUser() {
        databaseReference.child("users/"+user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(!snapshot.hasChild("username")||!snapshot.hasChild("imageUrl")){
                        loading.setVisibility(View.VISIBLE);
                        finish();
                        startActivity(new Intent(getApplicationContext(), CheckProfileActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        Log.d("SignInActivity.this", "Chuyển màn!");
                    }else{
                        loading.setVisibility(View.GONE);
                        initFragment();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        actionCheckUser();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Long signedin = Timestamp.now().getSeconds();
                    Map<String, Object> userSignin = new HashMap<>();
                    userSignin.put("signedin", signedin);
                    userSignin.put("uid", user.getUid());
                    databaseReference.child("users").child(user.getUid()).updateChildren(userSignin);
                }
            }
        };
        firebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.addAuthStateListener(mAuthListener);
        firebaseAuth.removeAuthStateListener(mAuthListener);
    }

    private void getWidget() {
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);

        //view
        loading = findViewById(R.id.loading);
        ImageView imageView = loading.findViewById(R.id.imageView);
        Animation rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
        imageView.setAnimation(rotate);


    }

    private void initFragment() {
        homeFeedFragment = new HomeFeedFragment();
        searchFragment = new SearchFragment();
        postCreateFragment = new PostCreateFragment();
        notificationFragment = new NotificationFragment();
        profileFragment = new ProfileFragment();

        fragmentManager = getSupportFragmentManager();
        if(fragment!= null && !fragment.isEmpty() && bundle != null) {
            if(fragment.equals("ProfileFragment")){
                profileFragment.setArguments(bundle);
                fragmentManager.beginTransaction()
                        .add(R.id.fragmentContainer, profileFragment, fragment)
                        .addToBackStack(fragment)
                        .commitAllowingStateLoss();
            }else{
                fragmentManager.beginTransaction()
                        .add(R.id.fragmentContainer, homeFeedFragment, fragment)
                        .addToBackStack(fragment)
                        .commitAllowingStateLoss();
            }

        }
        else{
            fragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, homeFeedFragment, "HomeFragment")
                    .addToBackStack("HomeFragment")
                    .commitAllowingStateLoss();
        }
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menuHome:
                                openFragment("HomeFragment");
                                return true;
                            case R.id.menuSearch:
                                openFragment("SearchFragment");
                                return true;
                            case R.id.menuCreate:
                                openFragment("PostCreateFragment");
                                return true;
                            case R.id.menuNotif:
                                openFragment("NotificationFragment");
                                return true;
                            case R.id.menuProfile:
                                openFragment("ProfileFragment");
                                return true;
                        }
                        return false;
                    }
                }
        );
    }
    private void openFragment(String tag) {
        Fragment fragment = fragmentManager.findFragmentByTag(tag);

        if (fragment == null) {
            // Fragment is not in backstack, add it and set the item as selected
            switch (tag){
                case "HomeFragment":
                    fragment = new HomeFeedFragment();
                    break;
                case "SearchFragment":
                    fragment = new SearchFragment();
                    break;
                case "PostCreateFragment":
                    fragment = new PostCreateFragment();
                    break;
                case "NotificationFragment":
                    fragment = new NotificationFragment();
                    break;
                case "ProfileFragment":
                    fragment = new ProfileFragment();
                    break;
            }

            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.nothing,R.anim.slide_right)
                    .add(R.id.fragmentContainer, fragment, tag)
                    .addToBackStack(tag)
                    .commit();
            Log.d("Checker","frag=null");

        } else {
            fragmentManager.popBackStack(tag,0);
            Log.d("Checker","frag=backstackFrag");
        }
        int backStackEntryCount = fragmentManager.getBackStackEntryCount();
        for (int i = 0; i < backStackEntryCount; i++) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(i);
            String fragmentTag = backStackEntry.getName();
            Log.d("BackStack", "Fragment tag: " + fragmentTag);
        }

    }
    @Override
    public void onBackPressed() {
        bottomNavigationView.setVisibility(View.VISIBLE);
        if (fragmentManager.getBackStackEntryCount() > 1) {
            bottomNavigationView.setSelectedItemId(selectItem);
            fragmentManager.popBackStackImmediate(); // Quay lại fragment trước đó trong back stack
        } else if (doubleBackToExitPressedOnce) {
            //clear all fragment in backstack
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            super.onBackPressed(); // Thoát ứng dụng
        } else {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press Back again to exit!", Toast.LENGTH_SHORT).show();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

}