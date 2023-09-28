package com.example.instagramclone.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagramclone.R;
import com.example.instagramclone.adapters.ActiveUserAdapter;
import com.example.instagramclone.adapters.ChatAdapter;
import com.example.instagramclone.adapters.StoryUserAdapter;
import com.example.instagramclone.models.User;
import com.example.instagramclone.utilities.Constants;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private View view_newchat;
    private Toolbar toolbarChat, toolbarNewChat;
    private ImageView btnNewChat;
    private RecyclerView activeUserRecycler, chatRecycler, newChatRecycler;
    private ActiveUserAdapter activeUserAdapter;
    private ChatAdapter chatAdapter;
    private DatabaseReference databaseReference;
    String currentUid, currentUsername, currentAvt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.KEY_CurrentUserPreference, MODE_PRIVATE);
        currentUid = sharedPreferences.getString(Constants.KEY_currentUID, null);
        currentUsername = sharedPreferences.getString(Constants.KEY_currentUsername, null);
        currentAvt = sharedPreferences.getString(Constants.KEY_currentAvt, null);
        if(currentUid==null){
            currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        getWidget();
        actionToolbar();
        getChat();
    }

    private void getWidget() {
        view_newchat = findViewById(R.id.view_newchat);
        newChatRecycler = view_newchat.findViewById(R.id.recyclerNewChat);
        toolbarNewChat = view_newchat.findViewById(R.id.toolbarNewChat);
        toolbarChat = findViewById(R.id.toolbarChat);
        btnNewChat = findViewById(R.id.btnNewChat);
        activeUserRecycler = findViewById(R.id.recyclerActiveUser);
        chatRecycler = findViewById(R.id.recyclerChat);

        btnNewChat.setOnClickListener(this);

        activeUserRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        activeUserRecycler.setHasFixedSize(true);
        chatRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        chatRecycler.setHasFixedSize(true);
        newChatRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnNewChat:
                actionNewChat();
                break;
        }
    }

    private void actionToolbar() {
        setSupportActionBar(toolbarChat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarChat.setTitle(currentUsername);
        toolbarChat.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24);
        toolbarChat.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void getChat() {
        databaseReference.child("chats").orderByChild("modified").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> uidList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (snapshot.exists()) {
                        String chatid = dataSnapshot.getKey();
                        if (chatid.contains(currentUid)) {
                            String uid = chatid.replace("_", "").replace(currentUid, "");
                            uidList.add(uid);
                            //get active user
                            databaseReference.child("users/"+uid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    List<User> activeUserList = new ArrayList<>();
                                    if(snapshot.exists()){
                                        User user = snapshot.getValue(User.class);
                                        Long signIn = user.getSignedin();
                                        long differenceInMillis = Timestamp.now().getSeconds() - signIn;
                                        if(differenceInMillis<60){
                                           activeUserList.add(user);
                                        }
                                    }
                                    Collections.reverse(activeUserList);
                                    activeUserAdapter = new ActiveUserAdapter(getApplicationContext(),activeUserList,currentUid);
                                    activeUserRecycler.setAdapter(activeUserAdapter);

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { }
                            });
                        }
                    }
                }
                for(int i=0;i<uidList.size();i++){
                    Log.d("name",uidList.get(i));
                }
                Collections.reverse(uidList);
                chatAdapter = new ChatAdapter(getApplicationContext(), uidList, currentUid);
                chatRecycler.setAdapter(chatAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void actionNewChat() {
        view_newchat.setVisibility(View.VISIBLE);

        toolbarNewChat.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24);
        toolbarNewChat.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view_newchat.setVisibility(View.GONE);
            }
        });
        databaseReference.child("follows").orderByChild("created")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<String> uidList = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (snapshot.exists()) {
                            String followId = dataSnapshot.getKey();
                            if(followId.contains(currentUid+"_")){
                                String uid = followId.replace(currentUid+"_","");
                                uidList.add(uid);
                            }
                        }
                    }
                    Collections.reverse(uidList);
                    chatAdapter = new ChatAdapter(getApplicationContext(), uidList, currentUid);
                    newChatRecycler.setAdapter(chatAdapter);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.nothing, R.anim.slide_out);
    }
}