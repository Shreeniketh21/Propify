package com.shreeniketh.propify;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.shreeniketh.propify.activities.LoginEmailActivity;
import com.shreeniketh.propify.activities.LoginOptionsActivity;
import com.shreeniketh.propify.activities.MainActivity;

import maes.tech.intentanim.CustomIntent;

public class SplashScreenActivity extends Activity {

    //View Variables
    private ImageView appLogo;
    private TextView appName,appSlogan, poweredBy1, poweredBy2;

    //Other Variables
    private Animation topAnimation, bottomAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        initViews();
        initAnimation();
    }

    private void initAnimation() {
        //Initialize Animations
        topAnimation = AnimationUtils.loadAnimation(SplashScreenActivity.this, R.anim.splash_top_animation);
        bottomAnimation = AnimationUtils.loadAnimation(SplashScreenActivity.this, R.anim.splash_bottom_animation);
    }

    private void initViews() {
        appName=findViewById(R.id.app_name);
        appLogo = findViewById(R.id.app_logo);
        appSlogan = findViewById(R.id.app_slogan);
        poweredBy1 = findViewById(R.id.app_powered_by1);
        poweredBy2 = findViewById(R.id.app_powered_by2);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Set Animation To Views
        appLogo.setAnimation(topAnimation);
        appName.setAnimation(bottomAnimation);
        appSlogan.setAnimation(bottomAnimation);
        poweredBy1.setAnimation(bottomAnimation);
        poweredBy2.setAnimation(bottomAnimation);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashScreenActivity.this, LoginOptionsActivity.class));
            CustomIntent.customType(SplashScreenActivity.this, "fadein-to-fadeout");
            finish();
        },4000);
    }
}