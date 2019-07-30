package com.spbn.womenprotection;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity implements Animation.AnimationListener {
    ImageView logo;
    TextView app_name;
    Animation animation_slide, animation_from_up;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        app_name = (TextView) findViewById(R.id.app_name);
        logo = (ImageView) findViewById(R.id.logo_splash);

        animation_slide = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_down);

        animation_from_up = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_from_up);

        animation_slide.setAnimationListener(this);
        animation_from_up.setAnimationListener(this);

        app_name.setVisibility(View.GONE);

        logo.startAnimation(animation_from_up);
    }

    @Override
    public void onAnimationStart(Animation animation) {
        if (animation == animation_from_up) {
            app_name.setVisibility(View.VISIBLE);
            app_name.startAnimation(animation_slide);
        }
    }

    @Override
    public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(SplashActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
