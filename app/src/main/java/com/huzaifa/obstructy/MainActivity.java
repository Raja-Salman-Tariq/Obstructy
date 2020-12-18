package com.huzaifa.obstructy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Animation anim;
    ImageView obstruction;
    Handler mhandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        anim = AnimationUtils.loadAnimation(this, R.anim.animation);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {            }

            @Override
            public void onAnimationEnd(Animation animation) {
                obstruction.setVisibility(View.GONE);
                mhandler = new Handler();
                mhandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final Intent mainIntent = new Intent(MainActivity.this, LoadVideo.class);
                        startActivity(mainIntent);
                        finish();
                    }
                }, 0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {          }
        });
        obstruction = findViewById(R.id.fence);
        obstruction.setAnimation(anim);

    }
}


