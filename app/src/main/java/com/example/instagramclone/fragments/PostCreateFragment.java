package com.example.instagramclone.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.activities.MainActivity;
import com.example.instagramclone.models.Post;
import com.example.instagramclone.utilities.Constants;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PostCreateFragment extends Fragment implements View.OnClickListener {
    private static final int REQUEST_CODE = 100;
    private View viewLoading;
    private TextView txtUsername, btnPost;
    private EditText edtCaption;
    private ImageView imgAvatar, imgPost;
    private VideoView videoPost;
    private Toolbar toolbarCreatenew;
    private DatabaseReference databaseReference;

    private Uri imageUri, videoUri, mediaUri;
    private String postId, currentUid, currentUsername,currentAvt;

    private FragmentManager fragmentManager;
    MainActivity mainActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        if(activity instanceof MainActivity){
            mainActivity = (MainActivity) activity;
            fragmentManager = mainActivity.getSupportFragmentManager();
        }

        SharedPreferences sharedPreferences = mainActivity.getSharedPreferences(Constants.KEY_CurrentUserPreference, Context.MODE_PRIVATE);
        currentUid = sharedPreferences.getString(Constants.KEY_currentUID, null);
        currentUsername = sharedPreferences.getString(Constants.KEY_currentUsername, null);
        currentAvt = sharedPreferences.getString(Constants.KEY_currentAvt, null);

        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_postcreate, container, false);

        getWidget(view);
        actionToolbar();
        return view;
    }

    private void getWidget(View view) {
        toolbarCreatenew = view.findViewById(R.id.toolbarCreateNew);
        viewLoading = view.findViewById(R.id.viewLoading);
        ImageView imageView = viewLoading.findViewById(R.id.imageView);
        Animation rotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        imageView.setAnimation(rotate);

        txtUsername = view.findViewById(R.id.txtUsername);
        edtCaption = view.findViewById(R.id.edtCaption);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        imgPost = view.findViewById(R.id.imagePost);
        videoPost = view.findViewById(R.id.videoPost);
        btnPost = view.findViewById(R.id.btnPost);

        imgPost.setOnClickListener(this);
        btnPost.setOnClickListener(this);

        Glide.with(mainActivity).load(currentAvt).dontAnimate().into(imgAvatar);
        txtUsername.setText(currentUsername);
    }

    private void actionToolbar() {
        ((AppCompatActivity) mainActivity).setSupportActionBar(toolbarCreatenew);
        ((AppCompatActivity) mainActivity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarCreatenew.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24);
        toolbarCreatenew.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentManager.popBackStack();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPost:
                actionCreatePost();
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
                imgPost.setVisibility(View.VISIBLE);
                imgPost.setImageURI(imageUri);

                videoUri = null;
                videoPost.setVisibility(View.GONE);
            } else if (mediaType != null && mediaType.startsWith("video/")) {
                videoUri = mediaUri;
                long videoSize = getVideoSize(videoUri);
                long maxSizeInBytes = 10 * 1024 * 1024; // 10MB

                //resize video
                if (videoSize <= maxSizeInBytes && videoPost.getDuration() <= 15000) {
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    WindowManager windowManager = (WindowManager) mainActivity.getSystemService(Context.WINDOW_SERVICE);
                    if (windowManager != null) {
                        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
                        int screenWidth = displayMetrics.widthPixels;
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        try {
                            // Đặt data source là URL của video
                            HashMap<String, String> headers = new HashMap<>();
                            retriever.setDataSource(String.valueOf(videoUri),headers);

                            int width = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                            int height = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

                            float aspectRatio = (float) width/height;
                            if(aspectRatio > 1 && width!=screenWidth) videoPost.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, (int) (screenWidth/aspectRatio)));
                            else if(aspectRatio == 1) videoPost.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenWidth));
                            else if (aspectRatio < 1 && height != screenWidth) videoPost.setLayoutParams(new LinearLayout.LayoutParams((int) (screenWidth / aspectRatio), screenWidth));

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
                    videoPost.setVisibility(View.VISIBLE);
                    videoPost.setVideoURI(videoUri);
                    videoPost.start();

                    imageUri = null;
                    imgPost.setVisibility(View.GONE);
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

    private void actionCreatePost() {
        viewLoading.setVisibility(View.VISIBLE);
        closekeyboard();
        String caption = edtCaption.getText().toString();
        Timestamp timestamp = Timestamp.now();
        String millisec = String.valueOf(timestamp.toDate().getTime());
        if (imageUri == null && videoUri == null) {
            Toast.makeText(mainActivity, "Please select your image or video!", Toast.LENGTH_SHORT).show();
            viewLoading.setVisibility(View.GONE);
        } else {
            if (imageUri != null) {
                postId = currentUid + "_image_" + millisec;
                createPost(postId,caption);
            } else if (videoUri != null) {
                videoPost.pause();
                postId = currentUid + "_video_" + millisec;
                createPost(postId,caption);
            }
        }

    }
    private void createPost(String postId,  String caption) {
        Post post;
        if (caption.isEmpty()) {
            post = new Post(postId, currentUid, null, null, Timestamp.now().getSeconds(),false);
        } else {
            post = new Post(postId, currentUid, null, caption, Timestamp.now().getSeconds(),false);
        }
        edtCaption.setText("");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewLoading.setVisibility(View.GONE);
                Bundle bundle = new Bundle();
                bundle.putString("mediaUri", mediaUri.toString());
                bundle.putSerializable("post", post);
                HomeFeedFragment homeFeedFragment = new HomeFeedFragment();
                homeFeedFragment.setArguments(bundle);
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.nothing, R.anim.slide_left)
                        .add(R.id.fragmentContainer, homeFeedFragment, "HomeFragment")
                        .addToBackStack("HomeFragment")
                        .commit();
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