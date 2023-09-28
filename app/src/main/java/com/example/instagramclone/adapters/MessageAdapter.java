package com.example.instagramclone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagramclone.R;
import com.example.instagramclone.interfaces.MessageClickListener;
import com.example.instagramclone.models.Message;
import com.google.firebase.Timestamp;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<Message> messageList;
    private String senderId;
    private MessageClickListener messageClickListener;
    private static final int SEND_TYPE = 1;
    private static final int RECEIVE_TYPE = 2;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Milliseconds
    private static final long FIVE_MIN_SECONDS = 5;
    private long lastClickTime = 0;

    public MessageAdapter(Context context, List<Message> messageList, String senderId, MessageClickListener messageClickListener) {
        this.context = context;
        this.messageList = messageList;
        this.senderId = senderId;
        this.messageClickListener = messageClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == SEND_TYPE) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_send, parent, false);
            return new SendMessageViewHolder(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_receive, parent, false);
            return new ReceiveMessageViewHolder(view);
        }

    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        LocalDateTime nextTime = toLocalDateTime(messageList.get(position).getCreated());
        LocalDateTime currentTime = LocalDateTime.now();
        if (getItemViewType(position) == SEND_TYPE) {
            ((SendMessageViewHolder) holder).txtSendMessage.setText(messageList.get(position).getContent());
            if (messageList.size() >= 0 && messageList.size() < 1) {
                String formattedCreated = getFormattedCreated(nextTime, currentTime);
                ((SendMessageViewHolder) holder).txtTimeAgo.setText(formattedCreated);
                ((SendMessageViewHolder) holder).txtTimeAgo.setVisibility(View.VISIBLE);
            } else if (messageList.size() > 2 && position > 2) {
                LocalDateTime preTime = toLocalDateTime(messageList.get(position - 1).getCreated());
                Duration duration = Duration.between(preTime, nextTime);
                long minutes = duration.toMinutes();
                String formattedCreated = getFormattedCreated(nextTime, currentTime);
                if (minutes >= FIVE_MIN_SECONDS) {
                    ((SendMessageViewHolder) holder).txtTimeAgo.setText(formattedCreated);
                    ((SendMessageViewHolder) holder).txtTimeAgo.setVisibility(View.VISIBLE);
                }
            }
            ((SendMessageViewHolder) holder).txtSendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long clickTime = System.currentTimeMillis();
                    if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                        // Double click
                        if (messageClickListener != null) {
                            messageClickListener.onItemDoubleClick(messageList.get(position));
                        }
                    } else {
                        // Single click
                        if (messageClickListener != null && messageList.size() > 2) {
                            LocalDateTime preTime = toLocalDateTime(messageList.get(position - 1).getCreated());
                            LocalDateTime currentTime = LocalDateTime.now();
                            String created = getFormattedCreated(preTime, currentTime);
                            ((SendMessageViewHolder) holder).txtSendTime.setText(created);
                            if (((SendMessageViewHolder) holder).txtSendTime.getVisibility() == View.GONE) {
                                ((SendMessageViewHolder) holder).txtSendTime.setVisibility(View.VISIBLE);
                                ((SendMessageViewHolder) holder).txtSendTime.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_down));
                            } else {
                                ((SendMessageViewHolder) holder).txtSendTime.setVisibility(View.GONE);
                                ((SendMessageViewHolder) holder).txtSendTime.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_up));
                            }
                        }
                    }
                    lastClickTime = clickTime;
                }
            });
        } else {
            ((ReceiveMessageViewHolder) holder).txtReceiveMessage.setText(messageList.get(position).getContent());
            if (messageList.size() >= 0 && messageList.size() < 1) {
                String formattedCreated = getFormattedCreated(nextTime, currentTime);
                ((ReceiveMessageViewHolder) holder).txtTimeAgo.setText(formattedCreated);
                ((ReceiveMessageViewHolder) holder).txtTimeAgo.setVisibility(View.VISIBLE);
            } else if (messageList.size() > 2 && position > 2) {
                LocalDateTime preTime = toLocalDateTime(messageList.get(position - 1).getCreated());
                Duration duration = Duration.between(preTime, nextTime);
                long minutes = duration.toMinutes();
                String formattedCreated = getFormattedCreated(nextTime, currentTime);
                if (minutes >= FIVE_MIN_SECONDS) {
                    ((ReceiveMessageViewHolder) holder).txtTimeAgo.setText(formattedCreated);
                    ((ReceiveMessageViewHolder) holder).txtTimeAgo.setVisibility(View.VISIBLE);
                }
            }
            ((ReceiveMessageViewHolder) holder).txtReceiveMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long clickTime = System.currentTimeMillis();
                    if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                        // Double click
                        if (messageClickListener != null) {
                            messageClickListener.onItemDoubleClick(messageList.get(position));
                        }
                    } else {
                        // Single click
                        if (messageClickListener != null && messageList.size() > 2) {
                            LocalDateTime preTime = toLocalDateTime(messageList.get(position - 1).getCreated());
                            LocalDateTime currentTime = LocalDateTime.now();
                            String created = getFormattedCreated(preTime, currentTime);
                            ((ReceiveMessageViewHolder) holder).txtReceiveTime.setText(created);
                            if (((ReceiveMessageViewHolder) holder).txtReceiveTime.getVisibility() == View.GONE) {
                                ((ReceiveMessageViewHolder) holder).txtReceiveTime.setVisibility(View.VISIBLE);
                                ((ReceiveMessageViewHolder) holder).txtReceiveTime.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_down));
                            } else {
                                ((ReceiveMessageViewHolder) holder).txtReceiveTime.setVisibility(View.GONE);
                                ((ReceiveMessageViewHolder) holder).txtReceiveTime.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_up));
                            }
                        }
                    }
                    lastClickTime = clickTime;
                }
            });
        }
    }

    private static LocalDateTime toLocalDateTime(long timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private static String getFormattedCreated(LocalDateTime pre, LocalDateTime next) {
        if (pre.isAfter(next.minusSeconds(10))) {
            return pre.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else if (pre.isAfter(next.minusSeconds(30))) {
            return pre.format(DateTimeFormatter.ofPattern("dd, HH:mm"));
        } else if (pre.isAfter(next.minusMinutes(1))) {
            return pre.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
        } else {
            return pre.format(DateTimeFormatter.ofPattern("MMM dd yyyy"));
        }
    }
    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(messageList.get(position).getSenderId().equals(senderId)){
            return SEND_TYPE;
        }
        else{
            return RECEIVE_TYPE;
        }
    }

    class SendMessageViewHolder extends RecyclerView.ViewHolder{
        TextView txtSendMessage,txtSendTime,txtTimeAgo;
        public SendMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSendMessage = itemView.findViewById(R.id.txtSendMessage);
            txtSendTime = itemView.findViewById(R.id.txtSendTime);
            txtTimeAgo = itemView.findViewById(R.id.txtTimeAgo);
        }
    }
    class ReceiveMessageViewHolder extends RecyclerView.ViewHolder{
        TextView txtReceiveMessage,txtReceiveTime,txtTimeAgo;
        public ReceiveMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtReceiveMessage = itemView.findViewById(R.id.txtReceiveMessage);
            txtReceiveTime = itemView.findViewById(R.id.txtReceiveTime);
            txtTimeAgo = itemView.findViewById(R.id.txtTimeAgo);
        }
    }
}