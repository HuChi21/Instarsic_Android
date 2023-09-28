package com.example.instagramclone.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.activities.MessageActivity;
import com.example.instagramclone.models.User;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHold> {
    Context context;
    List<String> list;
    User user;
    String currentUid;
    public ChatAdapter(Context context, List<String> list,String currentUid) {
        this.context = context;
        this.list = list;
        this.currentUid = currentUid;
    }

    @NonNull
    @Override
    public ChatAdapter.MyViewHold onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat,parent,false);
        return new MyViewHold(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.MyViewHold holder, int position) {
        String uid = list.get(position);
        if(uid == null){
            return;
        }
        FirebaseDatabase.getInstance().getReference().child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    user = snapshot.getValue(User.class);
                    holder.txtUsername.setText(user.getFullname());
                    long differenceInMillis = Timestamp.now().getSeconds() - user.getSignedin();
                    if(differenceInMillis<60){
                        holder.txtSignedin.setText("Active");
                        holder.imgIsActive.setVisibility(View.VISIBLE);
                    }else{
                        Date date = new Date(user.getSignedin() * 1000);
                        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
                        String timeago = prettyTime.format(date);
                        holder.txtSignedin.setText(timeago);
                    }
                    Glide.with(context).load(user.getImageUrl()).dontAnimate().into(holder.imgAvatar);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] uids = {currentUid, uid};
                Arrays.sort(uids);
                String chatId = uids[0] + "_" + uids[1];

                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("receiverId",uid);
                intent.putExtra("chatId",chatId);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Toast.makeText(context, uid, Toast.LENGTH_SHORT).show();
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHold extends RecyclerView.ViewHolder {
        private ImageView imgAvatar;
        private TextView txtUsername,txtSignedin;
        private CardView imgIsActive;
        public MyViewHold(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtSignedin = itemView.findViewById(R.id.txtSignedIn);
            imgIsActive = itemView.findViewById(R.id.imgIsActive);
        }
    }
}
