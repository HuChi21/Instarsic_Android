package com.example.instagramclone.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.activities.MainActivity;
import com.example.instagramclone.models.Story;
import com.example.instagramclone.utilities.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StoryFragment extends Fragment implements View.OnClickListener {
    private static final int REQUEST_CODE = 100;
    private View view,viewLoading;
    private ProgressBar progressBar;
    private TextView txtUsername, btnPost;
    private ImageView imgAvatar, imgStory;
    private VideoView videoStory;
    private CardView btnBack;
    private DatabaseReference databaseReference;
    private StorageReference storage;
    private Uri imageUri, videoUri, mediaUri;
    private String storyId,currentUid, currentUsername,currentAvt;
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    MainActivity mainActivity;
    private static final long ONE_DAY_SECOND = 24*60*60;

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
        SharedPreferences sharedPreferences = mainActivity.getSharedPreferences(Constants.KEY_CurrentUserPreference, Context.MODE_PRIVATE);
        currentUid = sharedPreferences.getString(Constants.KEY_currentUID, null);
        currentUsername = sharedPreferences.getString(Constants.KEY_currentUsername, null);
        currentAvt = sharedPreferences.getString(Constants.KEY_currentAvt, null);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_story, container, false);

        getWidget(view);

        Glide.with(mainActivity).load(currentAvt).dontAnimate().into(imgAvatar);
        txtUsername.setText(currentUsername);

        return view;
    }

    private void getWidget(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        progressBar = view.findViewById(R.id.progressBar);
        viewLoading = view.findViewById(R.id.viewLoading);
        ImageView imageView = viewLoading.findViewById(R.id.imageView);
        Animation rotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        imageView.setAnimation(rotate);

        txtUsername = view.findViewById(R.id.txtUsername);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        imgStory = view.findViewById(R.id.imagePost);
        videoStory = view.findViewById(R.id.videoPost);
        btnPost = view.findViewById(R.id.btnPost);

        imgStory.setOnClickListener(this);
        btnPost.setOnClickListener(this);
        btnBack.setOnClickListener(this);


    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPost:
                actionCreateStory();
                break;
            case R.id.btnBack:
                fragmentManager.popBackStack();
                bottomNavigationView.setVisibility(View.VISIBLE);
                break;
            case R.id.imagePost:
                actionChooseMedia();
                break;
        }
    }

    private void actionChooseMedia() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_CODE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == -1 && data != null && data.getData() != null) {
            mediaUri = data.getData();
            String mediaType = mainActivity.getContentResolver().getType(mediaUri);
            if (mediaType != null && mediaType.startsWith("image/")) {
                imageUri = mediaUri;
                imgStory.setVisibility(View.VISIBLE);
                imgStory.setImageURI(imageUri);

                videoUri = null;
                videoStory.setVisibility(View.GONE);
            } else if (mediaType != null && mediaType.startsWith("video/")) {
                videoUri = mediaUri;
                long videoSize = getVideoSize(videoUri);
                long maxSizeInBytes = 10 * 1024 * 1024; // 10MB

                //resize video
                if (videoSize <= maxSizeInBytes && videoStory.getDuration() <= 15000) {
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    WindowManager windowManager = (WindowManager) mainActivity.getSystemService(Context.WINDOW_SERVICE);
                    if (windowManager != null) {
                        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
                        int screenWidth = displayMetrics.widthPixels;
                        int screenHeight = displayMetrics.heightPixels;
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        try {
                            // Đặt data source là URL của video
                            HashMap<String, String> headers = new HashMap<>();
                            retriever.setDataSource(String.valueOf(videoUri),headers);

                            int width = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                            int height = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

                            float aspectRatio = (float) width/height;
                            Log.d("dimension",width+"_"+height+"_"+aspectRatio);
                            if (aspectRatio > 1)
                                if (width != screenWidth) videoStory.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, (int) (screenWidth / aspectRatio)));
                                else if(width == screenWidth) videoStory.setLayoutParams(new LinearLayout.LayoutParams(width, height));
                            else if (aspectRatio == 1)
                                if (width != screenWidth) videoStory.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenWidth));
                                else if(width == screenWidth) videoStory.setLayoutParams(new LinearLayout.LayoutParams(width, height));
                            else if (aspectRatio < 1 )
                                if(height != screenHeight) videoStory.setLayoutParams(new LinearLayout.LayoutParams((int) (screenWidth / aspectRatio), screenHeight));
                                else if(height == screenHeight) videoStory.setLayoutParams(new LinearLayout.LayoutParams(width, height));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("a", "Failed to retrieve video metadata");
                        } finally {
                            try {
                                retriever.release();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    videoStory.setVisibility(View.VISIBLE);
                    videoStory.setVideoURI(videoUri);
                    videoStory.start();

                    imageUri = null;
                    imgStory.setVisibility(View.GONE);
                } else {
                    Toast.makeText(mainActivity, "Video must not be longer than 15s or larger than 10MB ", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private long getVideoSize(Uri videoUri) {
        Cursor cursor = mainActivity.getContentResolver().query(videoUri, null, null, null, null);
        long size = 0;
        if (cursor != null && cursor.moveToFirst()) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            size = cursor.getLong(sizeIndex);
            cursor.close();
        }
        return size;
    }
    private void actionCreateStory() {
        viewLoading.setVisibility(View.VISIBLE);
        Timestamp timestamp = Timestamp.now();
        String millisec = String.valueOf(timestamp.toDate().getTime());
        if (imageUri == null && videoUri == null) {
            Toast.makeText(mainActivity, "Please select your image or video!", Toast.LENGTH_SHORT).show();
            viewLoading.setVisibility(View.GONE);
        } else {
            if (imageUri != null) {
                storyId = currentUid + "_image_" +"story_"+ millisec;
                createStory(storyId,imageUri);
            } else if (videoUri != null) {
                videoStory.pause();
                storyId = currentUid + "_video_" +"story_"+ millisec;
                createStory(storyId,videoUri);
            }
        }

    }
    //chưa set view cho create Story,
    private void createStory(String storyId,Uri mediaUri) {
        databaseReference.child("chats").child(currentUid).push();

        Long create = Timestamp.now().getSeconds();
        Map<String,Object> storyUser = new HashMap<>();
        storyUser.put("userId",currentUid);
        storyUser.put("modified",create);
        databaseReference.child("stories").child(currentUid).updateChildren(storyUser);

        storage.child("story_media/" + currentUid + "/" + storyId).putFile(mediaUri)
            .addOnSuccessListener(taskSnapshot -> {
                if (taskSnapshot.getTask().isSuccessful()) {
                    storage.child("story_media/" + currentUid + "/" + storyId).getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            Story story = new Story(storyId, currentUid, currentAvt, uri.toString(), create);
                            databaseReference.child("stories/" + currentUid+"/storyList").child(storyId).setValue(story)
                                .addOnSuccessListener(unused -> {
                                    viewLoading.setVisibility(View.GONE);
                                    fragmentManager.popBackStack();
                                    bottomNavigationView.setVisibility(View.VISIBLE);

                                });
                        });
                }
            });
    }
}