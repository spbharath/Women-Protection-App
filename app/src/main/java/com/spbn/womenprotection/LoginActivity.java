package com.spbn.womenprotection;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSharedPreferences()) {
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
            this.finish();
        } else {
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            mAuth = FirebaseAuth.getInstance();
            ConstraintLayout fl = (ConstraintLayout) findViewById(R.id.log_bg);
            AnimationDrawable ad = (AnimationDrawable) fl.getBackground();
            ad.setEnterFadeDuration(4500);
            ad.setExitFadeDuration(4500);
            ad.start();
        }
    }

    public void openRegister(View view) {
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }

    public void validateLogin(View view) {
        final EditText email, pass;
        email = (EditText) findViewById(R.id.log_email);
        pass = (EditText) findViewById(R.id.log_pass);
        final String s1 = email.getText().toString();
        final String s2 = pass.getText().toString();
        if (s1.isEmpty() && s2.isEmpty()) {
            Toast.makeText(getApplicationContext(), "NULL FIELDS", Toast.LENGTH_SHORT).show();
        } else if (s1.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(s1).matches()) {
            Toast.makeText(getApplicationContext(), "INVALID EMAIL", Toast.LENGTH_SHORT).show();

        } else if (s2.isEmpty()) {
            Toast.makeText(getApplicationContext(), "FILL IN PASSWORD", Toast.LENGTH_SHORT).show();

        } else {

            mAuth.signInWithEmailAndPassword(s1, s2)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                assert firebaseUser != null;
                                if (firebaseUser.isEmailVerified()) {

                                    setSharedPreferences(s1);
                                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);
                                    finish();


                                } else {
                                    Toast.makeText(LoginActivity.this, "EMAIL NOT VERIFIED PLEASE VERIFY YOUR EMAIL", Toast.LENGTH_SHORT).show();
                                    firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(LoginActivity.this, "Verification Email Sent", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                            } else {
                                Toast.makeText(LoginActivity.this, "INVALID CREDENTIALS", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }

    }


    private void setSharedPreferences(String s) {
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        String email = s.toLowerCase();
        editor.putString("email", email);
        editor.putString("sms_name","unknown");
        editor.commit();
    }

    private boolean getSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String email = sharedPreferences.getString("email", null);
        if (email != null) {
            return true;
        } else {
            return false;
        }
    }
}
