package com.example.instagramclone.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.instagramclone.R;
import com.example.instagramclone.activities.MainActivity;
import com.example.instagramclone.adapters.NotifiAdapter;
import com.example.instagramclone.models.Notif;
import com.example.instagramclone.utilities.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationFragment extends Fragment {
    private View view;
    private Toolbar toolbarNotif;
    private TextView txtNoNotif;
    private RecyclerView notifRecycler;
    private NotifiAdapter notifiAdapter;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    String currentUID,currentUsername;
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        if(activity instanceof MainActivity){
            mainActivity = (MainActivity) activity;
            fragmentManager = mainActivity.getSupportFragmentManager();
            bottomNavigationView = mainActivity.findViewById(R.id.bottomNavigationView);
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        SharedPreferences sharedPreferences = mainActivity.getSharedPreferences(Constants.KEY_CurrentUserPreference, Context.MODE_PRIVATE);
        currentUID = sharedPreferences.getString(Constants.KEY_currentUID,null);
        currentUsername = sharedPreferences.getString(Constants.KEY_currentUsername,null);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_notification, container, false);
        getWidget(view);
        getNotification();
        return view;
    }

    private void getWidget(View v) {
        toolbarNotif = v.findViewById(R.id.toolbarNotification);
        notifRecycler = v.findViewById(R.id.recyclerNotification);
        txtNoNotif = v.findViewById(R.id.txtNoNotif);
        ((AppCompatActivity) mainActivity).setSupportActionBar(toolbarNotif);
        toolbarNotif.setTitleTextColor(getResources().getColor(R.color.black_121212));

        notifRecycler.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false));
        notifRecycler.setHasFixedSize(true);
    }

    private void getNotification() {
        databaseReference.child("notifications/"+currentUID).orderByChild("created").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Notif> notifList = new ArrayList<>();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if(snapshot.exists()){
                        Notif notif = dataSnapshot.getValue(Notif.class);
                        notifList.add(notif);
                    }
                }
                Collections.reverse(notifList);
                Log.d("notifList", String.valueOf(notifList.size()));
                if(notifList.size()==0){
                    txtNoNotif.setVisibility(View.VISIBLE);
                    notifRecycler.setVisibility(View.GONE);
                }else{
                    notifiAdapter = new NotifiAdapter(mainActivity,notifList,currentUID,fragmentManager);
                    notifRecycler.setAdapter(notifiAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}