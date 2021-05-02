package com.codewithshadow.quickchat.Activites;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.codewithshadow.quickchat.R;

public class SplashActivity extends AppCompatActivity {

    Handler mhandler;
    Runnable mrunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mrunnable = new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(SplashActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        };
        mhandler = new Handler();
        mhandler.postDelayed(mrunnable,1000);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mhandler!=null && mrunnable!=null)
        {
            mhandler.removeCallbacks(mrunnable);
        }
    }

    public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}