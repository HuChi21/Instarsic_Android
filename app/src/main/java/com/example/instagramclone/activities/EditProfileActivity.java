package com.example.instagramclone.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.adapters.UserAdapter;
import com.example.instagramclone.interfaces.ImageClickListener;
import com.example.instagramclone.models.User;
import com.example.instagramclone.utilities.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.net.InternetDomainName;
import com.google.firebase.Timestamp;
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

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE = 100;
    private View loading;
    private Toolbar toolbarEditProfile;
    private ImageView btnComplete, imgAvatar;
    private LinearLayout layoutEditProfile, layoutEditDetail, layoutEditPassword;
    private TextView btnEditAvatar, btnEditFullname, btnEditUsername, btnEditPhone, btnEditDoB, btnEditPassword;
    private EditText edtEditDetail, edtNewPassword, edtConfirmPassword;
    private String currentUid, curentUsername, currentAvt, filename, uid;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private User user;
    private Long birthdateTimestamp;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = getSharedPreferences(Constants.KEY_CurrentUserPreference, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        currentUid = sharedPreferences.getString(Constants.KEY_currentUID, null);
        curentUsername = sharedPreferences.getString(Constants.KEY_currentUsername, null);
        currentAvt = sharedPreferences.getString(Constants.KEY_currentAvt, null);

        getWidget();
        actionToolbar();
    }

    private void getWidget() {
        loading = findViewById(R.id.loading);
        ImageView imageView = loading.findViewById(R.id.imageView);
        Animation rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
        imageView.setAnimation(rotate);

        toolbarEditProfile = findViewById(R.id.toolbarEditProfile);
        btnComplete = findViewById(R.id.btnComplete);
        imgAvatar = findViewById(R.id.imgAvatar);
        layoutEditProfile = findViewById(R.id.layoutEditProfile);
        layoutEditDetail = findViewById(R.id.layoutEditDetail);
        layoutEditPassword = findViewById(R.id.layoutEditPassword);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);
        btnEditFullname = findViewById(R.id.btnEditFullname);
        btnEditUsername = findViewById(R.id.btnEditUsername);
        btnEditPhone = findViewById(R.id.btnEditPhone);
        btnEditDoB = findViewById(R.id.btnEditDoB);
        btnEditPassword = findViewById(R.id.btnEditPassword);
        edtEditDetail = findViewById(R.id.edtEditDetail);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        imgAvatar.setOnClickListener(this);
        btnEditAvatar.setOnClickListener(this);
        btnEditFullname.setOnClickListener(this);
        btnEditUsername.setOnClickListener(this);
        btnEditPhone.setOnClickListener(this);
        btnEditDoB.setOnClickListener(this);
        btnEditPassword.setOnClickListener(this);

        databaseReference.child("users/" + currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    user = snapshot.getValue(User.class);
                    Glide.with(getApplicationContext()).load(currentAvt).dontAnimate().into(imgAvatar);
                    btnEditFullname.setText(user.getFullname());
                    btnEditUsername.setText(user.getUsername());
                    btnEditPhone.setText(user.getPhonenumber());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-dd-yyyy");
                    btnEditDoB.setText(dateFormat.format(user.getBirthdate()));
                    btnEditPassword.setText(user.getPassword());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void actionToolbar() {
        setSupportActionBar(toolbarEditProfile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbarEditProfile.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutEditProfile.getVisibility() == View.GONE) {
                    toolbarEditProfile.setNavigationIcon(R.drawable.baseline_close_24);
                    layoutEditProfile.setVisibility(View.VISIBLE);
                    layoutEditDetail.setVisibility(View.GONE);
                    layoutEditPassword.setVisibility(View.GONE);
                } else {
                    toolbarEditProfile.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24);
                    onBackPressed();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgAvatar:
            case R.id.btnEditAvatar:
                actionChooseImage();
                break;
            case R.id.btnEditFullname:
                actionEdit("Fullname", btnEditFullname.getText().toString());
                break;
            case R.id.btnEditUsername:
                actionEdit("Username", btnEditUsername.getText().toString());
                break;
            case R.id.btnEditPhone:
                actionEdit("Phone number", btnEditPhone.getText().toString());
                break;
            case R.id.btnEditDoB:
                actionEditDob();
                break;
            case R.id.btnEditPassword:
                actionEditPassword();
                break;
        }
    }

    private void actionChooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && data != null && data.getData() != null) {
            loading.setVisibility(View.VISIBLE);
            imageUri = data.getData();
            filename = uid + "_avatar";
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("avatar/" + filename);
            storageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String mediaUrl = uri.toString();
                        Toast.makeText(getApplicationContext(), "Upload successfully!", Toast.LENGTH_SHORT).show();
                        Map<String, Object> userImage = new HashMap<>();
                        userImage.put("image", filename);
                        userImage.put("imageUrl", mediaUrl);
                        databaseReference.child("users/" + uid).updateChildren(userImage).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                loading.setVisibility(View.GONE);
                                imgAvatar.setImageURI(imageUri);

                                editor.putString(Constants.KEY_currentAvt, mediaUrl);
                                editor.apply();
                                Toast.makeText(getApplicationContext(), "Your image has been set up!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }
            });
        }
    }

    private void actionEditDob() {
        toolbarEditProfile.setTitle("Date of Birth");
        layoutEditProfile.setVisibility(View.GONE);
        layoutEditPassword.setVisibility(View.GONE);
        layoutEditDetail.setVisibility(View.VISIBLE);
        btnComplete.setVisibility(View.VISIBLE);
        edtEditDetail.setText(btnEditDoB.getText().toString());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(user.getBirthdate());
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int month = calendar.get(Calendar.MONTH) + 1;
        final int year = calendar.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = new DatePickerDialog(EditProfileActivity.this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
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
                    edtEditDetail.setText(String.valueOf(dateFormat.format(date)));
                    edtEditDetail.setError(null);
                    btnComplete.setClickable(true);
                    Map<String, Object> userDetail = new HashMap<>();
                    userDetail.put("birthdate", birthdateTimestamp);
                    btnComplete.setOnClickListener(v -> {
                        closekeyboard();
                        actionToolbar();
                        databaseReference.child("users/" + currentUid).updateChildren(userDetail);
                        layoutEditDetail.setVisibility(View.GONE);
                        layoutEditProfile.setVisibility(View.VISIBLE);
                        btnComplete.setVisibility(View.GONE);
                    });
                } else {
                    btnComplete.setClickable(false);
                    edtEditDetail.setError("You must be 18 or older!");
                    edtEditDetail.requestFocus();
                }
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    private void actionEdit(String title, String content) {
        toolbarEditProfile.setTitle(title);
        layoutEditProfile.setVisibility(View.GONE);
        layoutEditPassword.setVisibility(View.GONE);
        layoutEditDetail.setVisibility(View.VISIBLE);
        btnComplete.setVisibility(View.VISIBLE);
        edtEditDetail.setText(content);

        edtEditDetail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    btnComplete.setClickable(false);
                    edtEditDetail.setHint(title);
                    edtEditDetail.setHintTextColor(getResources().getColor(R.color.light_grey));
                } else {
                    if (title.equals("Username")) {
                        databaseReference.child("users").orderByChild("username").equalTo(edtEditDetail.getText().toString())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //check if is username used
                                        if (!snapshot.exists()) {
                                            Map<String, Object> userDetail = new HashMap<>();
                                            userDetail.put("username", edtEditDetail.getText().toString());
                                            btnComplete.setClickable(true);
                                            btnComplete.setOnClickListener(v -> {
                                                closekeyboard();
                                                actionToolbar();
                                                databaseReference.child("users/" + currentUid).updateChildren(userDetail);
                                                btnComplete.setVisibility(View.GONE);
                                                layoutEditDetail.setVisibility(View.GONE);
                                                layoutEditProfile.setVisibility(View.VISIBLE);
                                            });
                                        } else {
                                            btnComplete.setClickable(false);
                                            edtEditDetail.setError("Username is already taken!");
                                            edtEditDetail.requestFocus();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                    } else {
                        btnComplete.setClickable(true);
                        Map<String, Object> userDetail = new HashMap<>();
                        userDetail.put(title.toLowerCase(), edtEditDetail.getText().toString());
                        btnComplete.setOnClickListener(v -> {
                            closekeyboard();
                            actionToolbar();
                            databaseReference.child("users/" + currentUid).updateChildren(userDetail);
                            btnComplete.setVisibility(View.GONE);
                            layoutEditDetail.setVisibility(View.GONE);
                            layoutEditProfile.setVisibility(View.VISIBLE);
                        });
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void actionEditPassword() {
        toolbarEditProfile.setTitle("Password");
        layoutEditProfile.setVisibility(View.GONE);
        layoutEditDetail.setVisibility(View.GONE);
        layoutEditPassword.setVisibility(View.VISIBLE);
        btnComplete.setVisibility(View.VISIBLE);

        String password = edtNewPassword.getText().toString();
        edtNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 6 || s.length() > 25) {
                    btnComplete.setClickable(false);
                    edtNewPassword.setError("Your password must have at least 6 to 25!");
                    edtNewPassword.requestFocus();
                } else if (!Pattern.compile("^[a-zA-Z0-9]+$").matcher(s).matches()) {
                    btnComplete.setClickable(false);
                    edtNewPassword.setError("Only letters and numbers are allowed!");
                    edtNewPassword.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.equals(password)) {
                    btnComplete.setClickable(false);
                    edtNewPassword.setError("Your password doesn't match!");
                    edtNewPassword.requestFocus();
                } else {
                    Map<String, Object> userDetail = new HashMap<>();
                    userDetail.put("password", edtNewPassword);
                    btnComplete.setClickable(true);
                    btnComplete.setOnClickListener(v -> {
                        closekeyboard();
                        actionToolbar();
                        databaseReference.child("users/" + currentUid).updateChildren(userDetail);
                        btnComplete.setVisibility(View.GONE);
                        layoutEditPassword.setVisibility(View.GONE);
                        layoutEditProfile.setVisibility(View.VISIBLE);
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void closekeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.nothing, R.anim.slide_out);
        super.onBackPressed();
    }
}