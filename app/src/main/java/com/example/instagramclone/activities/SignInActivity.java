package com.example.instagramclone.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.instagramclone.R;
import com.example.instagramclone.models.User;
import com.example.instagramclone.utilities.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView imageView;
    private TextView txtSignin, txtSignup, txtForgot;
    private TextInputEditText edtUsername, edtPassword;
    private View loading;
    private String email;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private boolean doubleBackToExitPressedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        email = getIntent().getStringExtra("email");
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        getWidget();

    }
    private void getWidget() {
        imageView = findViewById(R.id.imageView);
        txtSignin = findViewById(R.id.btnSignIn);
        txtSignup = findViewById(R.id.btnSignUp);
        txtForgot = findViewById(R.id.txtForgotPassword);
        edtUsername = findViewById(R.id.edtFullname);
        if (email != null) {
            edtUsername.setText(email);
        }
        edtPassword = findViewById(R.id.edtPassword);
        loading = findViewById(R.id.loading);
        ImageView imageView = loading.findViewById(R.id.imageView);
        Animation rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
        imageView.setAnimation(rotate);

        txtSignin.setOnClickListener(this);
        txtSignup.setOnClickListener(this);
        txtForgot.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignIn:
                actionSignin();
                break;
            case R.id.btnSignUp:
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
                overridePendingTransition(R.anim.nothing, R.anim.slide_in);
                break;
            case R.id.txtForgotPassword:
                actionForgotPassword();
        }
    }

    private void actionSignin() {
        closekeyboard();
        String username, password;
        username = edtUsername.getText().toString();
        password = edtPassword.getText().toString();

        if (username.isEmpty()) {
            edtUsername.setError("Enter your username or email!");
            edtUsername.requestFocus();
        } else if (password.isEmpty()) {
            edtPassword.setError("Enter your password!");
            edtPassword.requestFocus();
        } else if (password.length() < 6) {
            edtPassword.setError("Your password must have at least 6!");
            edtPassword.requestFocus();
        } else {
            loading.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //check if sign in with email or username
                    if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {

                    } else {
                        //sign in firebase with email,password
                        firebaseAuth.signInWithEmailAndPassword(username, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    //if sign in successful, get user detail
                                    if (task.isSuccessful()) {
                                        databaseReference.child("users/"+firebaseAuth.getCurrentUser().getUid())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()) {
                                                        //add token
                                                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<String> task) {
                                                                if (!task.isSuccessful()) {
                                                                    Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                                                                    return;
                                                                }
                                                                // Get new FCM registration token
                                                                String token = task.getResult();
                                                                Map<String,Object> hashMap = new HashMap<>();
                                                                hashMap.put("token",token);
                                                                databaseReference.child("users/"+firebaseAuth.getCurrentUser().getUid()).updateChildren(hashMap);
                                                            }
                                                        });
                                                        SharedPreferences sharedPreferences = getSharedPreferences(Constants.KEY_CurrentUserPreference, Context.MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                                        //if user dont have child(username) or child(image), back to CheckProfileActivity
                                                        if(!snapshot.hasChild("username") || !snapshot.hasChild("imageUrl")){
                                                            String uid = snapshot.getValue(User.class).getUid();
                                                            editor.putString(Constants.KEY_currentUID, uid);
                                                            editor.apply();
                                                            loading.setVisibility(View.GONE);
                                                            finish();
                                                            startActivity(new Intent(getApplicationContext(), CheckProfileActivity.class));
                                                            overridePendingTransition(R.anim.nothing, R.anim.slide_in);
                                                            Log.d("Go","go check profile");

//                                                            Toast.makeText(SignInActivity.this, "go check profile !", Toast.LENGTH_SHORT).show();
                                                        }else{
                                                            //if user have completely info, go to MainActivity
                                                            String uid = snapshot.getValue(User.class).getUid();
                                                            String username = snapshot.getValue(User.class).getUsername();
                                                            String avt = snapshot.getValue(User.class).getImageUrl();

                                                            editor.putString(Constants.KEY_currentUID, uid);
                                                            editor.putString(Constants.KEY_currentUsername, username);
                                                            editor.putString(Constants.KEY_currentAvt, avt);
                                                            editor.apply();
                                                            loading.setVisibility(View.GONE);
                                                            finish();
                                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                                            overridePendingTransition(R.anim.nothing, R.anim.slide_in);
                                                            Log.d("Go","go main");
//                                                            Toast.makeText(SignInActivity.this, "go main!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {}
                                            });
                                    } else {
                                        Toast.makeText(SignInActivity.this, "Email or password is incorrect!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    }
                }
            }, 2000);
        }
    }


    private void actionForgotPassword() {
        closekeyboard();
        String username;
        username = edtUsername.getText().toString();
        if (username.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            edtUsername.setError("Email is invalid !");
            edtUsername.requestFocus();
        } else {
            loading.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loading.setVisibility(View.GONE);
                    firebaseAuth.sendPasswordResetEmail(username)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(SignInActivity.this, "Reset password email sent", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SignInActivity.this, "Something error!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }, 1500);
        }
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
        super.onBackPressed();
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press Back again to exit!", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}