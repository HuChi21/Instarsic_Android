package com.example.instagramclone.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.activities.MainActivity;
import com.example.instagramclone.adapters.PostGridAdapter;
import com.example.instagramclone.adapters.UserAdapter;
import com.example.instagramclone.interfaces.ImageClickListener;
import com.example.instagramclone.interfaces.PostItemClickListener;
import com.example.instagramclone.models.Post;
import com.example.instagramclone.models.User;
import com.example.instagramclone.utilities.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchFragment extends Fragment implements View.OnClickListener {
    private View view;
    private Toolbar toolbarSearch;
    private LinearLayout viewSearchPost,viewSearchAccount;
    private ImageView imgClear;
    private EditText edtSearch;
    private TextView txtNoResult;
    private RecyclerView recyclerSearchPost,recyclerSearch;
    private PostGridAdapter postGridAdapter;
    private UserAdapter userAdapter;
    private DatabaseReference databaseReference;
    private List<User> userList = new ArrayList<>();
    private String currentUid, currentUsername, currentAvt;
    private FragmentManager fragmentManager;
    MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            mainActivity = (MainActivity) activity;
            fragmentManager = mainActivity.getSupportFragmentManager();
        }
        databaseReference = FirebaseDatabase.getInstance().getReference();
        SharedPreferences sharedPreferences = mainActivity.getSharedPreferences(Constants.KEY_CurrentUserPreference, Context.MODE_PRIVATE);
        currentUid = sharedPreferences.getString(Constants.KEY_currentUID, null);
        currentUsername = sharedPreferences.getString(Constants.KEY_currentUsername, null);
        currentAvt = sharedPreferences.getString(Constants.KEY_currentAvt, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_search, container, false);
        getWidget(view);
        getPost();
        actionSearch();
        return view;
    }

    private void getWidget(View view) {
        toolbarSearch = view.findViewById(R.id.toolbarSearch);
        imgClear = view.findViewById(R.id.imgClear);
        edtSearch = view.findViewById(R.id.edtSearch);
        txtNoResult = view.findViewById(R.id.txtNoResult);
        viewSearchPost = view.findViewById(R.id.viewSearchPost);
        recyclerSearchPost = view.findViewById(R.id.recyclerSearchPost);
        viewSearchAccount = view.findViewById(R.id.viewSearchAccount);
        recyclerSearch = view.findViewById(R.id.recyclerSearch);

        imgClear.setOnClickListener(this);

        recyclerSearchPost.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        recyclerSearchPost.setHasFixedSize(true);
        recyclerSearch.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false));
        recyclerSearch.setHasFixedSize(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgClear:
                edtSearch.setText("");
                edtSearch.requestFocus();
                break;
            case R.id.txtNoResult:
                break;

        }
    }
    private void getPost() {
        List<Post> postList = new ArrayList<>();
        databaseReference.child("posts").orderByChild("created")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (snapshot.exists()) {
                            Post post = dataSnapshot.getValue(Post.class);
                            if (!post.getUserId().equals(currentUid)&& post.isArchive()==false)
                                postList.add(post);
                        }
                    }
                    Collections.reverse(postList);
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
                    recyclerSearchPost.setAdapter(postGridAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
    }
    private void actionSearch() {
        edtSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Khi EditText nhận focus (khi người dùng nhấn vào nó để mở bàn phím)
                    // Ẩn view1 và hiển thị view2
                    viewSearchAccount.setVisibility(View.VISIBLE);
                    viewSearchPost.setVisibility(View.GONE);
                    ((AppCompatActivity) mainActivity).setSupportActionBar(toolbarSearch);
                    ((AppCompatActivity) mainActivity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    toolbarSearch.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24);
                    toolbarSearch.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            viewSearchAccount.setVisibility(View.GONE);
                            viewSearchPost.setVisibility(View.VISIBLE);
                            ((AppCompatActivity) mainActivity).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                            edtSearch.setText("");
                            edtSearch.clearFocus();
                        }
                    });
                }
                else{
                    viewSearchAccount.setVisibility(View.GONE);
                    viewSearchPost.setVisibility(View.VISIBLE);
                    ((AppCompatActivity) mainActivity).setSupportActionBar(toolbarSearch);
                    ((AppCompatActivity) mainActivity).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0){
                    userList.clear();
                    userAdapter = new UserAdapter(mainActivity, userList, new ImageClickListener() {
                        @Override
                        public void onImageClick(String uid) {
                            // do nothing
                        }
                    });
                    recyclerSearch.setAdapter(userAdapter);
                }else{
                    String searchTensp = s.toString().trim();
                    timkiemSP(searchTensp);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private void timkiemSP(String text){
        userList.clear();
        databaseReference.child("users").orderByChild("signedin").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if(snapshot.exists()){
                        if(dataSnapshot.hasChild("username")&&dataSnapshot.hasChild("imageUrl")){
                            User user = dataSnapshot.getValue(User.class);
                            if(user.getFullname().toLowerCase().contains(text) || user.getUsername().toLowerCase().contains(text)) {
                                userList.add(user);
                            }
                        }

                    }
                }
                Collections.reverse(userList);
                Log.d("userList", String.valueOf(userList.size()));
                userAdapter = new UserAdapter(mainActivity, userList, new ImageClickListener() {
                    @Override
                    public void onImageClick(String uid) {
                        Bundle bundle = new Bundle();
                        bundle.putString("uid", uid);
                        ProfileFragment profileFragment = new ProfileFragment();
                        profileFragment.setArguments(bundle);
                        fragmentManager.beginTransaction()
                                .add(R.id.fragmentContainer, profileFragment,"ProfileFragment")
                                .addToBackStack("ProfileFragment")
                                .commit();
                    }
                });
                recyclerSearch.setAdapter(userAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

}