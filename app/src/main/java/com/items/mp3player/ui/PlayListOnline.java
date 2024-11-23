package com.items.mp3player.ui;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.items.mp3player.Ads.AdsControle;
import com.items.mp3player.R;
import com.items.mp3player.adapters.MusicListAdapter;
import com.items.mp3player.db.DBHelper;
import com.items.mp3player.interfaces.OnItemClickListener;
import com.items.mp3player.model.AudioModel;
import com.items.mp3player.services.MyMediaPlayer;
import com.items.mp3player.utils.SharedPrefData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PlayListOnline extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView titleTv, currentTimeTv, totalTimeTv;
    ImageView pausePlay, nextBtn, previousBtn, back_btn, list_btn, shuffle_btn;
    SeekBar seekBar;
    DBHelper fav;
    ArrayList<AudioModel> songsList = new ArrayList<>();
    LinearLayout cover_img;
    public boolean isBound = false;
    private boolean is_list_open = false;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    private MyMediaPlayer mediaPlayerService;
    private boolean isServiceBound = false;
    SharedPrefData sharedPrefData;
    MusicListAdapter adapter;
    AdsControle adsControle;

    private void loadMusicFromDevice() {
        AssetManager assetManager = getAssets();
        songsList.clear();
        try {
            // Load the JSON file
            InputStream is = assetManager.open("songsdata/QuranOnline.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();

            // Parse JSON data
            JSONArray jsonArray = new JSONArray(jsonBuilder.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject songObject = jsonArray.getJSONObject(i);
                String fileName = songObject.getString("name");
                String artist = songObject.getString("reciter");
                String title = songObject.getString("name");
                String assetPath = songObject.getString("recitation");

                // Create AudioModel for each file with the actual duration and additional info
                AudioModel songData = new AudioModel(assetPath, fileName, 0L, artist, title);
                songData.setId(i + 1);
                songsList.add(songData);

            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyMediaPlayer.MyMediaPlayerBinder binder = (MyMediaPlayer.MyMediaPlayerBinder) iBinder;
            mediaPlayerService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    private final BroadcastReceiver playerUpdatesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("online", false)) {
                int currentPosition = intent.getIntExtra("currentPosition", 0);
                int totalDuration = intent.getIntExtra("totalDuration", 0);
                String songTitle = intent.getStringExtra("songTitle");
                loadMusicFromDevice();

                // Update UI elements conditionally
                if (mediaPlayerService != null)
                    if (mediaPlayerService.isPlaying(true)) {
                        pausePlay.setImageResource(R.drawable.pause);
                    } else if (!mediaPlayerService.isPlaying(true)) {
                        pausePlay.setImageResource(R.drawable.play);
                    }

                updateSeekBar(currentPosition, totalDuration);


                if (!titleTv.getText().toString().equals(songTitle)) {
                    titleTv.setText(songTitle);
                }

                updateTime(currentPosition, totalDuration);
            }

        }

    };

    private void updateSeekBar(int currentPosition, int totalDuration) {
        seekBar.setMax(totalDuration);
        seekBar.setProgress(currentPosition, true);
    }

    private void updateTime(int currentPosition, int totalDuration) {

        String currentTime = formatTime(currentPosition);
        String totalTime = formatTime(totalDuration);

        currentTimeTv.setText(currentTime);
        totalTimeTv.setText(totalTime);
    }

    private String formatTime(int milliseconds) {
        int hours = (milliseconds / 1000) / 3600; // Calculate hours
        int minutes = ((milliseconds / 1000) % 3600) / 60; // Calculate remaining minutes
        int seconds = (milliseconds / 1000) % 60; // Calculate remaining seconds

        // Format time string
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds); // HH:MM:SS
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds); // MM:SS
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list_online);

        // Initialize views
        back_btn = findViewById(R.id.back_btn);
        recyclerView = findViewById(R.id.recycler_view);
        titleTv = findViewById(R.id.song_title);
        currentTimeTv = findViewById(R.id.current_time);
        totalTimeTv = findViewById(R.id.total_time);
        pausePlay = findViewById(R.id.pause_play);
        nextBtn = findViewById(R.id.next);
        previousBtn = findViewById(R.id.previous);
        seekBar = findViewById(R.id.seek_bar);
        shuffle_btn = findViewById(R.id.shuffle);
        list_btn = findViewById(R.id.list);
        cover_img = findViewById(R.id.cover_img);
        adsControle = new AdsControle(this);
        adsControle.LoadInterstitial();
        adsControle.ShowBanner(findViewById(R.id.Banner_frame));
        sharedPrefData = new SharedPrefData(this);
        fav = new DBHelper(this);


        // Bind to MyMediaPlayer service
        boolean s = mediaPlayerService == null;
        Log.e("TAKIMED", "  mediaPlayerService strat with " + s);
        if (!isBound) {
            Intent serviceIntent = new Intent(this, MyMediaPlayer.class);
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }


        if (isNetworkAvailable()) {
            loadMusicFromDevice();
        } else
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();

        if (!sharedPrefData.LoadBoolean("permission")) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            REQUEST_NOTIFICATION_PERMISSION);
                }
            }
        }

        if (sharedPrefData.LoadBoolean("shuffle")) {
            shuffle_btn.setColorFilter(getResources().getColor(R.color.main_color));
        } else
            shuffle_btn.setColorFilter(getResources().getColor(R.color.black));


        shuffle_btn.setOnClickListener(view -> {
            if (sharedPrefData.LoadBoolean("shuffle")) {
                sharedPrefData.SaveBoolean("shuffle", false);
                shuffle_btn.setColorFilter(getResources().getColor(R.color.black));
            } else {
                sharedPrefData.SaveBoolean("shuffle", true);
                shuffle_btn.setColorFilter(getResources().getColor(R.color.main_color));
            }
        });

        list_btn.setOnClickListener(view -> {
            if (is_list_open) {
//                cover_img.setVisibility(View.VISIBLE);
                ObjectAnimator animator = ObjectAnimator.ofFloat(cover_img, "translationY", 3000f);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(500); // Animation duration in milliseconds
                animator.start();
                is_list_open = false;
            } else {

                ObjectAnimator animator = ObjectAnimator.ofFloat(cover_img, "translationY", 0f);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(500); // Animation duration in milliseconds
                animator.start();
                is_list_open = true;
            }
        });

        back_btn.setOnClickListener(v -> {
            onBackPressed();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MusicListAdapter(songsList, this, new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                onSongSelected(position);

            }

            @Override
            public void onFavClick(int position) {
                AudioModel selectedSong = songsList.get(position);
                if (!fav.isFavorite(selectedSong.getFileName())) {
                    fav.addFavorite(selectedSong);
                } else {
                    fav.deleteFavorite(selectedSong.getFileName());
                }
            }
        }, false);
        recyclerView.setAdapter(adapter);

        pausePlay.setOnClickListener(v -> pausePlay());
        nextBtn.setOnClickListener(v -> playNextSong());
        previousBtn.setOnClickListener(v -> playPreviousSong());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayerService != null && fromUser) {
                    mediaPlayerService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(playerUpdatesReceiver, new IntentFilter("PlayerUpdates"));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playerUpdatesReceiver);
        // Set first element in the list to the player
        if (!songsList.isEmpty()) {
            updateSongDetails(songsList.get(0));
        }

        // Additional logic to handle animations, etc.
        ObjectAnimator animator = ObjectAnimator.ofFloat(cover_img, "translationY", 0f);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(500); // Animation duration in milliseconds
        animator.start();
        is_list_open = true;
        // stopService(new Intent(this, MyMediaPlayer.class));

    }

    private void onSongSelected(int position) {
        adsControle.ShowInterstitial(null);
        if (!songsList.isEmpty()) {
            if (isServiceBound) {
                mediaPlayerService.playSong(songsList.get(position));
            } else {
                Intent serviceIntent = new Intent(this, MyMediaPlayer.class);
                serviceIntent.putExtra("songIndex", position);
                serviceIntent.putExtra("online", true);
                serviceIntent.putParcelableArrayListExtra("songList", songsList);
                startService(serviceIntent);
                pausePlay.setImageResource(R.drawable.pause);

            }
            sharedPrefData.SaveInt("currentSongIndex", position);
            sharedPrefData.SaveString("filename", songsList.get(position).getFileName());
            Log.d("TakiMed","position "+ position);
            updateSongDetails(songsList.get(position));
            LocalBroadcastManager.getInstance(this).registerReceiver(playerUpdatesReceiver, new IntentFilter("PlayerUpdates"));
        }

    }

    private void updateSongDetails(AudioModel song) {
        titleTv.setText(song.getTitle());
        currentTimeTv.setText("00:00");
        totalTimeTv.setText(convertToMMSS(String.valueOf(song.getDuration())));
        seekBar.setMax((int) song.getDuration());
        Log.d("TakiMed","update to title  : "+ song.getTitle());
    }

    private void pausePlay() {
        if (!songsList.isEmpty()) {
            if (mediaPlayerService != null && mediaPlayerService.currentIndex != -1 && mediaPlayerService.isOnline()) {
                if (mediaPlayerService.isPlaying(true)) {
                    Log.e("TAKIMED", "   click is playing pause");
                    mediaPlayerService.pause(true);
                    pausePlay.setImageResource(R.drawable.play);
                    LocalBroadcastManager.getInstance(this).unregisterReceiver(playerUpdatesReceiver);

                } else {
                    mediaPlayerService.start(true);
                    pausePlay.setImageResource(R.drawable.pause);
                    LocalBroadcastManager.getInstance(this).registerReceiver(playerUpdatesReceiver, new IntentFilter("PlayerUpdates"));


                }
            } else {
                if (sharedPrefData.LoadInt("currentSongIndex") != -1)
                    onSongSelected(sharedPrefData.LoadInt("currentSongIndex"));
                else
                    onSongSelected(0);
            }
        }
    }

    private void playNextSong() {
        if (mediaPlayerService != null) {
            mediaPlayerService.next(true);
            adapter.notifyDataSetChanged();
            if (mediaPlayerService.currentIndex != -1) {
                adapter.setSelectedPosition(mediaPlayerService.currentIndex);
                sharedPrefData.SaveInt("currentSongIndex", mediaPlayerService.currentIndex);
                sharedPrefData.SaveString("filename", songsList.get(mediaPlayerService.currentIndex).getFileName());
                LocalBroadcastManager.getInstance(this).registerReceiver(playerUpdatesReceiver, new IntentFilter("PlayerUpdates"));
            }


        }

    }

    private void playPreviousSong() {
        if (mediaPlayerService != null) {
            mediaPlayerService.previous(true);
            adapter.notifyDataSetChanged();
            if (mediaPlayerService.currentIndex != -1) {
//                adapter.notifyItemChanged(mediaPlayerService.currentIndex);
                adapter.setSelectedPosition(mediaPlayerService.currentIndex);
                sharedPrefData.SaveInt("currentSongIndex", mediaPlayerService.currentIndex);
                sharedPrefData.SaveString("filename", songsList.get(mediaPlayerService.currentIndex).getFileName());
                LocalBroadcastManager.getInstance(this).registerReceiver(playerUpdatesReceiver, new IntentFilter("PlayerUpdates"));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playerUpdatesReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isBound) {
            bindService(new Intent(this, MyMediaPlayer.class), serviceConnection, Context.BIND_AUTO_CREATE);
            LocalBroadcastManager.getInstance(this).registerReceiver(playerUpdatesReceiver, new IntentFilter("PlayerUpdates"));
        } else
            LocalBroadcastManager.getInstance(this).registerReceiver(playerUpdatesReceiver, new IntentFilter("PlayerUpdates"));
    }

    private boolean is_index_exist(int index) {
        for (int i = 0; i < songsList.size(); i++) {
            if (i == index)
                return true;
        }
        return false;
    }

    private String convertToMMSS(String duration) {

        Long millis = Long.parseLong(duration);

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1);

        // Format time string
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds); // HH:MM:SS
        } else {
            return String.format("%02d:%02d", minutes, seconds); // MM:SS
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                sharedPrefData.SaveBoolean("permission", true);
            } else {

                Toast.makeText(this, "Notification permission is required to show notifications", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}


