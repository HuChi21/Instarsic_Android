package com.example.instagramclone.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instagramclone.R;
import com.example.instagramclone.models.User;
import com.example.instagramclone.utilities.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CheckProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE = 100;
    private View loading;
    private ConstraintLayout user_detail,user_choose_image,user_done;

    //khai bao
    private TextView btnContinue, btnSkip, btnDone, btnChooseBirthdate, btnStart;
    private com.google.android.material.textfield.TextInputEditText edtUsername, edtBirthdate, edtPhoneNum;
    private ImageView imgAvatar;
    private DatabaseReference databaseReference;
    private StorageReference storageRef;
    private SharedPreferences sharedPreferences;
    private String filename,uid;
    private Long birthdateTimestamp;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkprofile);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        sharedPreferences = getSharedPreferences(Constants.KEY_CurrentUserPreference, Context.MODE_PRIVATE);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            uid = firebaseUser.getUid();
        }


        getWidget();
        actionCheckProfile();
    }

    private void getWidget() {
        loading = findViewById(R.id.loading);
        ImageView imageView = loading.findViewById(R.id.imageView);
        Animation rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
        imageView.setAnimation(rotate);

        user_detail = findViewById(R.id.user_detail);
        edtUsername = findViewById(R.id.edtUsername);
        edtBirthdate = findViewById(R.id.edtBirthdate);
        btnChooseBirthdate = findViewById(R.id.btnChooseBirthdate);
        edtPhoneNum = findViewById(R.id.edtPhoneNum);
        btnContinue = findViewById(R.id.btnContinue);

        user_choose_image = findViewById(R.id.user_choose_image);
        btnSkip = findViewById(R.id.btnSkip);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnDone = findViewById(R.id.btnDone);

        user_done = findViewById(R.id.user_done);
        btnStart = findViewById(R.id.btnStart);

        btnContinue.setOnClickListener(this);
        btnSkip.setOnClickListener(this);
        btnDone.setOnClickListener(this);
        btnChooseBirthdate.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        imgAvatar.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnChooseBirthdate:
                getDoB();
                break;
            case R.id.btnContinue:
                actionUpdateUser();
                break;
            case R.id.btnSkip:
                loading.setVisibility(View.VISIBLE);
                //if user dont want to set avatar, set user's image into default avatar
                Map<String, Object> userImage = new HashMap<>();
                userImage.put("image", "def_avatar.png");
                userImage.put("imageUrl", Constants.default_avatar);
                databaseReference.child("users/"+uid).updateChildren(userImage).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loading.setVisibility(View.GONE);
                                user_choose_image.setVisibility(View.GONE);
                                user_done.setVisibility(View.VISIBLE);
                                Toast.makeText(getApplicationContext(), "Your profile has been set up!", Toast.LENGTH_SHORT).show();
                            }
                        }, 1000);
                    }
                });

                break;
            case R.id.imgAvatar:
                actionChooseImage();
            case R.id.btnDone:
                actionUpdateImage();
                break;
            case R.id.btnStart:
                loading.setVisibility(View.VISIBLE);
                databaseReference.child("users/"+uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            User user1 = snapshot.getValue(User.class);
                            loading.setVisibility(View.GONE);
                            user_done.setVisibility(View.GONE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(Constants.KEY_currentUID, user1.getUid());
                            editor.putString(Constants.KEY_currentUsername, user1.getUsername());
                            editor.putString(Constants.KEY_currentAvt, user1.getImageUrl());
                            editor.apply();

                            finish();
                            Toast.makeText(CheckProfileActivity.this, "Ready", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
                break;

        }
    }
    private void actionCheckProfile() {
        loading.setVisibility(View.VISIBLE);
        databaseReference.child("users/"+uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(!snapshot.hasChild("username")){
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {      //check loading again
                                loading.setVisibility(View.GONE);
                                user_detail.setVisibility(View.VISIBLE);
                            }
                        }, 1500);
                    } else if (!snapshot.hasChild("imageUrl")) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {      //check loading again
                                loading.setVisibility(View.GONE);
                                user_choose_image.setVisibility(View.VISIBLE);
                            }
                        }, 1500);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void getDoB() {
        closekeyboard();
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR) - 18;
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(CheckProfileActivity.this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                Date date = calendar.getTime(); // pick user birthdate

                birthdateTimestamp = new Timestamp(date).getSeconds();
                LocalDate currentDate = LocalDate.now(); //get date now
                //calculate millis 18years
                LocalDate date18YearsAgo = currentDate.minusYears(18);
                long daysBetween = ChronoUnit.DAYS.between(date18YearsAgo, currentDate);
                long ageInMillis = daysBetween * 24L * 60L * 60L * 1000L;
                //calculate millis between today and birthday
                long diffInMillis = (new Date()).getTime() - date.getTime();

                if (diffInMillis >= ageInMillis) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-dd-yyyy");
                    edtBirthdate.setText(String.valueOf(dateFormat.format(date)));
                    edtBirthdate.setError(null);
                } else {
                    edtBirthdate.setError("You must be 18 or older!");
                    edtBirthdate.requestFocus();
                }
            }
        }, year, month, day);
        datePickerDialog.show();
    }
    private void actionUpdateUser() {
        closekeyboard();
        String username, birthdate, phonenumber;
        username = edtUsername.getText().toString();
        birthdate = edtBirthdate.getText().toString();
        phonenumber = edtPhoneNum.getText().toString();
        if (username.isEmpty()) {
            edtUsername.setError("Enter your username!");
            edtUsername.requestFocus();
        } else if (username.length() < 6 || username.length() > 25) {
            edtUsername.setError("Your username must have at least 6 to 25!");
            edtUsername.requestFocus();
        }else if (!Pattern.compile("^[a-zA-Z0-9]+$").matcher(username).matches()) {
            edtUsername.setError("Only letters and numbers are allowed!");
            edtUsername.requestFocus();
        } else if (birthdate.isEmpty()) {
            edtBirthdate.setError("Choose your birthdate!");
            edtBirthdate.requestFocus();
        } else if (!Patterns.PHONE.matcher(phonenumber).matches() && phonenumber.isEmpty()) {
            edtPhoneNum.setError("Enter your phone number!");
            edtPhoneNum.requestFocus();
        } else {
            loading.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    databaseReference.child("users").orderByChild("username").equalTo(username)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                //check if is username used
                                if (!snapshot.exists()) {
                                    Map<String, Object> userDetail = new HashMap<>();
                                    userDetail.put("username", username);
                                    userDetail.put("birthdate", birthdateTimestamp);
                                    userDetail.put("phonenumber", phonenumber);
                                    databaseReference.child("users/"+uid).updateChildren(userDetail)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d("TAG", "User data has been updated successfully");
                                                    loading.setVisibility(View.GONE);
                                                    user_detail.setVisibility(View.GONE);
                                                    user_choose_image.setVisibility(View.VISIBLE);
                                                }
                                            });
                                } else {
                                    loading.setVisibility(View.GONE);
                                    edtUsername.setError("Username is already taken!");
                                    edtUsername.requestFocus();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });
                }
            }, 2000);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgAvatar.setImageURI(imageUri);
        }
    }
    private void actionChooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_CODE);
    }
    private void actionUpdateImage() {
        loading.setVisibility(View.VISIBLE);
        if (imageUri == null) {
            Toast.makeText(getApplicationContext(), "Please select your image!", Toast.LENGTH_SHORT).show();
            loading.setVisibility(View.GONE);
        }else{
            filename = uid + "_avatar";
            storageRef = FirebaseStorage.getInstance().getReference("avatar/" + filename);
            storageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String mediaUrl = uri.toString();
                        imgAvatar.setImageURI(null);
                        Toast.makeText(getApplicationContext(), "Upload successfully!", Toast.LENGTH_SHORT).show();
                        Map<String, Object> userImage = new HashMap<>();
                        userImage.put("image", filename);
                        userImage.put("imageUrl",mediaUrl);
                        databaseReference.child("users/"+uid).updateChildren(userImage).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                loading.setVisibility(View.GONE);
                                user_choose_image.setVisibility(View.GONE);
                                user_done.setVisibility(View.VISIBLE);
                                Toast.makeText(getApplicationContext(), "Your profile has been set up!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }
            });
        }

    }
    private void closekeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}