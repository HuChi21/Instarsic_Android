package com.example.instagramclone.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private TextInputEditText edtEmail, edtFullname,edtPassword,edtRePassword;
    private TextView btnSignUp,btnSignIn;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private View loading;
    private User user ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        getWidget();
    }

    private void getWidget() {
        edtEmail = findViewById(R.id.edtEmail);
        edtFullname =findViewById(R.id.edtFullname);
        edtPassword =findViewById(R.id.edtPassword);
        edtRePassword =findViewById(R.id.edtRepassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        loading = findViewById(R.id.loading);
        ImageView imageView = loading.findViewById(R.id.imageView);
        Animation rotate = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate);
        imageView.setAnimation(rotate);
        btnSignIn.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSignIn:
                onBackPressed();
                startActivity(new Intent(getApplicationContext(),SignInActivity.class));
                break;
            case R.id.btnSignUp:
                actionSignUp();
                break;
        }
    }

    private void actionSignUp() {
        closekeyboard();
        String email,fullname,password,repassword;
        email = edtEmail.getText().toString();
        fullname = edtFullname.getText().toString();
        password = edtPassword.getText().toString();
        repassword = edtRePassword.getText().toString();

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.isEmpty()){
            edtEmail.setError("Enter a valid email!");
            edtEmail.requestFocus();
        } else if (fullname.isEmpty()) {
            edtFullname.setError("Enter your fullname!");
            edtFullname.requestFocus();
        } else if (password.isEmpty()) {
            edtPassword.setError("Enter your password!");
            edtPassword.requestFocus();
        } else if (password.length() < 6) {
            edtPassword.setError("Your password must have at least 6!");
            edtPassword.requestFocus();
        } else if (repassword.isEmpty() && !repassword.equals(password)) {
            edtRePassword.setError("Your password doesn't match!");
            edtRePassword.requestFocus();
        }
        else{
            loading.setVisibility(View.VISIBLE);

            //actionCreateUser();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    loading.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        //Log.d(TAG, "createUserWithEmail:success");
                                        String uid = firebaseAuth.getCurrentUser().getUid();
                                        Long timestamp = Timestamp.now().getSeconds();
                                        user = new User(uid,email, null, password,fullname, null, null,timestamp, timestamp, timestamp,"null");

                                        firebaseAuth.signInWithEmailAndPassword(email,password);
                                        databaseReference.child("users").child(uid).setValue(user);
                                        firebaseAuth.signOut();

                                        onBackPressed();
                                        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                                        intent.putExtra("email", email);
                                        startActivity(intent);
                                        Toast.makeText(SignUpActivity.this, "Created your account successfully!", Toast.LENGTH_SHORT).show();
                                    }else {
                                        // If sign in fails, display a message to the user.
                                        edtEmail.setError("Email is invalid or already taken!");
                                        edtEmail.requestFocus();
                                    }
                                };
                            });
                };
            },2000);
        }
    }


    private void closekeyboard() {
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.nothing,R.anim.slide_out);
    }
}