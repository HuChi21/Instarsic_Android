package com.example.instagramclone.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.FCM.FCMController;
import com.example.instagramclone.R;
import com.example.instagramclone.adapters.MessageAdapter;
import com.example.instagramclone.interfaces.MessageClickListener;
import com.example.instagramclone.models.Message;
import com.example.instagramclone.models.User;
import com.example.instagramclone.utilities.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MessageActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar toolbarChatDetail;
    private ImageView imgAvatar,btnPhoto,btnSend;
    private CardView imgIsActive;
    private TextView txtFullname,txtSignin;
    private RecyclerView messageRecycler;
    private MessageAdapter messageAdapter;
    private EditText edtMessage;
    private DatabaseReference databaseReference;
    String currentUid,currentUsername,currentAvt,currentSignin,receiverId;
    String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.KEY_CurrentUserPreference,MODE_PRIVATE);
        currentUid = sharedPreferences.getString(Constants.KEY_currentUID,null);
        currentUsername = sharedPreferences.getString(Constants.KEY_currentUsername,null);
        currentAvt = sharedPreferences.getString(Constants.KEY_currentAvt,null);
        currentSignin = sharedPreferences.getString(Constants.KEY_currentSignin,null);

        receiverId = getIntent().getStringExtra("receiverId");
        chatId = getIntent().getStringExtra("chatId");
        databaseReference = FirebaseDatabase.getInstance().getReference();
        getWidget();
        getReceiverDetail();
        actionToolbar();
        getMessage();
    }

    private void getWidget() {
        toolbarChatDetail = findViewById(R.id.toolbarChatDetail);
        imgAvatar = findViewById(R.id.imgAvatar);
        imgIsActive = findViewById(R.id.imgIsActive);
        btnPhoto = findViewById(R.id.btnPhoto);
        btnSend = findViewById(R.id.btnSend);
        txtFullname = findViewById(R.id.txtFullname);
        txtSignin = findViewById(R.id.txtSignin);
        messageRecycler = findViewById(R.id.recyclerChatDetail);
        edtMessage = findViewById(R.id.edtMessage);

        btnPhoto.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        imgAvatar.setOnClickListener(this);
        txtFullname.setOnClickListener(this);

        messageRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        messageRecycler.setHasFixedSize(true);
        ((LinearLayoutManager)messageRecycler.getLayoutManager()).setStackFromEnd(true);
        
    }
    private void getReceiverDetail(){
        databaseReference.child("users").child(receiverId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        User user = snapshot.getValue(User.class);
                        txtFullname.setText(user.getFullname());
                        Glide.with(getApplicationContext()).load(user.getImageUrl()).dontAnimate().into(imgAvatar);

                        long differenceInMillis = Timestamp.now().getSeconds() - user.getSignedin();
                        if(differenceInMillis<60){
                            txtSignin.setText("Active");
                            imgIsActive.setVisibility(View.VISIBLE);
                        }
                        else{
                            Date date = new Date(user.getSignedin() * 1000);
                            PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
                            String timeago = prettyTime.format(date);
                            txtSignin.setText(timeago);
                            imgIsActive.setVisibility(View.GONE);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnPhoto:
                actionSendImage();
                break;
            case R.id.btnSend:
                actionSendMessage();
                break;
            case R.id.imgAvatar:
                actionViewProfile();
                break;
            case R.id.txtFullname:
                actionViewProfile();
                break;
        }
    }
    private void actionToolbar() {
        setSupportActionBar(toolbarChatDetail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarChatDetail.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24);
        toolbarChatDetail.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }
    private void getMessage() {
        databaseReference.child("chats/"+chatId+"/messages").orderByChild("created").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Message> messageList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if(snapshot.exists()){
                        Message message = dataSnapshot.getValue(Message.class);
                        messageList.add(message);
                    }
                }
                // Cập nhật giao diện recyclerChat với danh sách tin nhắn
                messageAdapter = new MessageAdapter(getApplicationContext(), messageList, currentUid, new MessageClickListener() {
                    @Override
                    public void onItemDoubleClick(Message message) {
                        databaseReference.child("chats").child(chatId).child(message.getMessageId()).removeValue();
                    }
                    @Override
                    public void onSingleClick(Message message) {}
                });
                messageRecycler.setAdapter(messageAdapter);
                if(messageAdapter.getItemCount()<=0){
                    return;
                }else{
                    int newPosition = messageAdapter.getItemCount() - 1;
                    messageRecycler.smoothScrollToPosition(newPosition);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void actionSendImage() {
    }

    private void actionSendMessage() {
        ///, chưa có media và  sửa lại chatAdapter và thêm storyAdapter,shortAdapter
        String messageId = databaseReference.child("chats").child(chatId).push().getKey();
        Long created = Timestamp.now().getSeconds();
        String stringMessage = edtMessage.getText().toString();
        if(!TextUtils.isEmpty(stringMessage)){
            //update modified chat time
            Map<String,Object> chatModified = new HashMap<>();
            chatModified.put("modified",created);
            databaseReference.child("chats/"+chatId).updateChildren(chatModified);
            //add new message
            Message message = new Message(messageId,currentUid,receiverId,stringMessage, created);
            databaseReference.child("chats/"+chatId+"/messages").child(messageId).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    pushNotificationToUser(stringMessage);
                    edtMessage.setText("");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MessageActivity.this, "Cant sent message now!", Toast.LENGTH_SHORT).show();
                }
            });
            //send notification
//            String notifId = databaseReference.child("notifications/"+receiverId).push().getKey();
//            Notif notif = new Notif(notifId,receiverId,currentUid,"Reply","Reply",Timestamp.now().getSeconds());
//            databaseReference.child("notifications/"+receiverId).child(notifId).setValue(notif);
        }

    }

    private void pushNotificationToUser(String stringMessage) {
        databaseReference.child("users/"+receiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()&& snapshot.hasChild("token")){
                    if(!snapshot.getKey().equals(receiverId)){
                        String username = snapshot.child("username").getValue(String.class);
                        String token = snapshot.child("token").getValue(String.class);
                        String title = snapshot.child("username").getValue(String.class);
                        String body = "("+currentUsername+")"+username+": "+ stringMessage;
                        FCMController fcmController = new FCMController();
                        fcmController.sendNotification(token,title,body);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void actionViewProfile(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("uid", receiverId);

        intent.putExtra("fragment", "ProfileFragment");
        intent.putExtra("bundle", bundle);

        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.nothing,R.anim.nothing);
    }
}