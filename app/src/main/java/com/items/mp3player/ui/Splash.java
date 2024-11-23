package com.items.mp3player.ui;

import static com.items.mp3player.Constants.Enable_Ads;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.items.mp3player.Ads.ActionListener;
import com.items.mp3player.Ads.GetLoadAds;
import com.items.mp3player.R;
import com.items.mp3player.utils.SharedPrefData;

public class Splash extends AppCompatActivity {

    SharedPrefData sharedPrefData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sharedPrefData = new SharedPrefData(this);

        new GetLoadAds(Splash.this, new ActionListener() {
            @Override
            public void onDone() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sharedPrefData.SaveBoolean(Enable_Ads, true);
                        startActivity(new Intent(Splash.this,MainActivity.class));
                        finish();
                    }
                }, 1500);

            }

            @Override
            public void onFailed() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sharedPrefData.SaveBoolean(Enable_Ads, false);
                        startActivity(new Intent(Splash.this,MainActivity.class));
                        finish();
                    }
                }, 1500);
            }
        });


    }
}
