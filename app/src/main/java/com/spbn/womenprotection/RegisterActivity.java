package com.spbn.womenprotection;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean create;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        ConstraintLayout fl = (ConstraintLayout) findViewById(R.id.register_bg);
        AnimationDrawable ad = (AnimationDrawable) fl.getBackground();
        ad.setEnterFadeDuration(4500);
        ad.setExitFadeDuration(4500);
        ad.start();

        mAuth = FirebaseAuth.getInstance();
    }

    public void openLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void validateRegister(View view) {
        EditText email, pass;
        email = (EditText) findViewById(R.id.reg_email);
        pass = (EditText) findViewById(R.id.reg_pass);
        String s1 = email.getText().toString();
        String s2 = pass.getText().toString();
        if (s1.isEmpty() && s2.isEmpty()) {
            Toast.makeText(getApplicationContext(), "NULL FIELDS", Toast.LENGTH_SHORT).show();
        } else if (s1.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(s1).matches()) {
            Toast.makeText(getApplicationContext(), "INVALID EMAIL", Toast.LENGTH_SHORT).show();

        } else if (s2.isEmpty()) {
            Toast.makeText(getApplicationContext(), "FILL IN PASSWORD", Toast.LENGTH_SHORT).show();

        } else {

            mAuth.createUserWithEmailAndPassword(s1, s2)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            AlertDialog.Builder alert_dialog = new AlertDialog.Builder(RegisterActivity.this);
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                assert firebaseUser != null;
                                firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(RegisterActivity.this, "Verification Email Sent", Toast.LENGTH_SHORT).show();
                                        // setSharedPreferences("yes");
                                        finish();
                                    }
                                });
                                alert_dialog.setMessage("Congrats You have Registered with EagleEye")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                                                startActivity(i);
                                                finish();
                                            }
                                        });

                            } else {
                                alert_dialog.setMessage("Registration Unsuccessful")
                                        .setCancelable(false)
                                        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });

                            }
                            AlertDialog alertDialog = alert_dialog.create();
                            alertDialog.setTitle("Eagle Eye");
                            alertDialog.show();

                        }
                    });
        }
    }

    private void setSharedPreferences(String s) {
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        editor.putString("create_db", s);
        Toast.makeText(this, sharedPreferences.getString("create_db", ""), Toast.LENGTH_SHORT).show();
        editor.commit();
    }

}
