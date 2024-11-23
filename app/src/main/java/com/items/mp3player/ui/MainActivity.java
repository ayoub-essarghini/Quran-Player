package com.items.mp3player.ui;


import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.appbar.MaterialToolbar;
import com.items.mp3player.Ads.ActionShowAds;
import com.items.mp3player.Ads.AdsControle;
import com.items.mp3player.R;
import com.items.mp3player.utils.SharedPrefData;

public class MainActivity extends AppCompatActivity {

    private CardView offline, online, read, fav, more, rate;
    SharedPrefData sharedPrefData;
    AdsControle adsControle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        init();
        events();

    }

    private void init()
    {
        sharedPrefData = new SharedPrefData(this);
        adsControle = new AdsControle(this);
        adsControle.LoadInterstitial();
        adsControle.ShowBanner(findViewById(R.id.Banner_frame));
        offline = findViewById(R.id.offline_btn);
        online = findViewById(R.id.online_btn);
        read = findViewById(R.id.read_btn);
        fav = findViewById(R.id.fav_btn);
        more = findViewById(R.id.more_btn);
        rate = findViewById(R.id.rate_btn);
    }

    private void events()
    {

        offline.setOnClickListener(view -> {
            adsControle.ShowInterstitial(new ActionShowAds() {
                @Override
                public void onDone() {
                    Intent intent = new Intent(MainActivity.this,PlayList.class);
                    sharedPrefData.SaveString("page","offline");
                    startActivity(intent);
                }
            });

        });

        online.setOnClickListener(view -> {
            adsControle.ShowInterstitial(new ActionShowAds() {
                @Override
                public void onDone() {
                    Intent intent =  new Intent(MainActivity.this,PlayListOnline.class);
                    startActivity(intent);
                }
            });


        });
        read.setOnClickListener(view -> {
            adsControle.ShowInterstitial(new ActionShowAds() {
                @Override
                public void onDone() {
                    Intent intent =  new Intent(MainActivity.this,OnlinePlayer.class);
                    startActivity(intent);
                }
            });


        });
        fav.setOnClickListener(view -> {
            adsControle.ShowInterstitial(new ActionShowAds() {
                @Override
                public void onDone() {
                    Intent intent = new Intent(MainActivity.this,PlayList.class);
                    sharedPrefData.SaveString("page","fav");
                    startActivity(intent);
                }
            });


        });
        more.setOnClickListener(view -> {
            openMoreApps();

        });
        rate.setOnClickListener(view -> {
            openRatePage();

        });
    }
    private void openRatePage() {
        // Use your app's package name
        String packageName = getPackageName();
        String rateUrl = "https://play.google.com/store/apps/details?id=" + packageName;

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(rateUrl));
            intent.setPackage("com.android.vending");
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(rateUrl));
            startActivity(intent);
        }
    }
    private void openMoreApps() {
        // Use the URL of your developer page on the Play Store
        String developerPageUrl = "https://play.google.com/store/apps/developer?id=YourDeveloperID";

        // Create an intent to open the Play Store
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(developerPageUrl));
            intent.setPackage("com.android.vending"); // Ensure the Play Store app opens
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // If Play Store is not installed, open in a browser
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(developerPageUrl));
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adsControle != null)
            adsControle.LoadInterstitial();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int itemId = item.getItemId();
        if (itemId == R.id.share_btn) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "download app to listen and read quran";
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
            return true;
        } else if (itemId == R.id.menu_about) {
            startActivity(new Intent(MainActivity.this, About.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
