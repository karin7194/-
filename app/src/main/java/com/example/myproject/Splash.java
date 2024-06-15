package com.example.myproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class Splash extends AppCompatActivity {
    private ImageView iv;
    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        iv=findViewById(R.id.imageview);
        tv=findViewById(R.id.textView10);
        Thread mySplash = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        Animation mFiA= AnimationUtils.loadAnimation(Splash.this,R.anim.tween);
                        iv.startAnimation(mFiA);
                        tv.startAnimation(mFiA);
                        wait(3000);
                    }
                } catch (InterruptedException ex) {
                }
                SharedPreferences sp=getSharedPreferences("user",0);
                boolean isChecked= sp.getBoolean("isChecked",false);
                boolean isChecked2= sp.getBoolean("isChecked2",false);
                if(isChecked2){
                    Intent go = new Intent(Splash.this, MainPage.class);
                    startActivity(go);
                    finish();
                }
                else if(isChecked){
                    Intent go = new Intent(Splash.this, MainPage.class);
                    startActivity(go);
                    finish();
                }
                else{
                    Intent go = new Intent(Splash.this, Login.class);
                    startActivity(go);
                    finish();
                }
            }
        };
        mySplash.start();
    }

}

