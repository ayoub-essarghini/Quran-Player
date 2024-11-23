package com.items.mp3player.ui;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.facebook.ads.BuildConfig;
import com.items.mp3player.R;

public class About extends AppCompatActivity {

    TextView version;
    ImageButton backBtn;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        version = findViewById(R.id.version);
        backBtn = findViewById(R.id.back_btn);
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = pInfo.versionName;

            version.setText("Version: "+versionName);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        backBtn.setOnClickListener(v->{
            onBackPressed();
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
