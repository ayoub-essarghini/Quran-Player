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
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
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

public class PlayList extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView titleTv, currentTimeTv, totalTimeTv;
    ImageView pausePlay, nextBtn, previousBtn, back_btn, list_btn, shuffle_btn;
    SeekBar seekBar;
    DBHelper fav;
    ArrayList<AudioModel> songsList = new ArrayList<>();
    LinearLayout cover_img;
    private boolean isBound = false;
    private boolean is_list_open = false;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    AdsControle adsControle;

    private MyMediaPlayer mediaPlayerService;
    private boolean isServiceBound = false;

    SharedPrefData sharedPrefData;

    MusicListAdapter adapter;

    private void loadMusicFromDevice() {
        AssetManager assetManager = getAssets();
        songsList.clear();

        try {
            // Load the JSON file
            InputStream is = assetManager.open("songsdata/songs.json");
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
                String fileName = songObject.getString("fileName");
                String artist = songObject.getString("artist");
                String title = songObject.getString("title");

                // Create asset path
                String assetPath = "file:///android_asset/" + fileName;

                // Use MediaMetadataRetriever to get the duration
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                AssetFileDescriptor afd = assetManager.openFd(fileName);
                mmr.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long duration = Long.parseLong(durationStr); // Get duration

                // Create AudioModel for each file with the actual duration and additional info
                AudioModel songData = new AudioModel(assetPath, fileName, duration, artist, title);
                songData.setId(i + 1);
                songsList.add(songData);

                mmr.release(); // Release the MediaMetadataRetriever
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
            if (!intent.getBooleanExtra("online", false)) {
                int currentPosition = intent.getIntExtra("currentPosition", 0);
                int totalDuration = intent.getIntExtra("totalDuration", 0);
                String songTitle = intent.getStringExtra("songTitle");

                if (mediaPlayerService != null) {    // Update UI elements conditionally
                    if (mediaPlayerService.isPlaying(false)) {
                        pausePlay.setImageResource(R.drawable.pause);
                    } else {
                        pausePlay.setImageResource(R.drawable.play);
                    }
                }
                updateSeekBar(currentPosition, totalDuration);


                if (!titleTv.getText().toString().equals(songTitle)) {
                    titleTv.setText(songTitle);
                }

                updateTime(currentPosition, totalDuration);
            }
            if (sharedPrefData.LoadString("page").equals("fav")) {
                songsList = fav.getAllFavorites();
            } else {
                loadMusicFromDevice();
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
        setContentView(R.layout.activity_playslist);

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

        sharedPrefData = new SharedPrefData(this);
        adsControle = new AdsControle(this);
        adsControle.LoadInterstitial();
        adsControle.ShowBanner(findViewById(R.id.Banner_frame));

        // Bind to MyMediaPlayer service

        Intent data = getIntent();
        Intent serviceIntent = new Intent(this, MyMediaPlayer.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);


        // Initialize data
        fav = new DBHelper(this);
        if (data != null) {

            if (sharedPrefData.LoadString("page").equals("fav")) {
                songsList = fav.getAllFavorites();
            } else {
                loadMusicFromDevice();
            }

        }
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
                cover_img.setVisibility(View.GONE);
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
        }, sharedPrefData.LoadString("page").equals("fav"));
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

        if (sharedPrefData.LoadString("page").equals("online")) {
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
    }

    private void onSongSelected(int position) {
        adsControle.ShowInterstitial(null);
        if (isServiceBound) {
            mediaPlayerService.playSong(songsList.get(position));
        } else {
            Intent serviceIntent = new Intent(this, MyMediaPlayer.class);
            serviceIntent.putExtra("songIndex", position);
            serviceIntent.putExtra("online", false);
            serviceIntent.putParcelableArrayListExtra("songList", songsList);
            startService(serviceIntent);
            pausePlay.setImageResource(R.drawable.pause);

        }
        sharedPrefData.SaveInt("currentSongIndex", position);
        sharedPrefData.SaveString("filename", songsList.get(position).getFileName());

        updateSongDetails(songsList.get(position));

    }

    private void updateSongDetails(AudioModel song) {
        titleTv.setText(song.getTitle());
        currentTimeTv.setText("00:00");
        totalTimeTv.setText(convertToMMSS(String.valueOf(song.getDuration())));
        seekBar.setMax((int) song.getDuration());
    }

    private void pausePlay() {
        if (mediaPlayerService != null && mediaPlayerService.currentIndex != -1 && !mediaPlayerService.isOnline()) {
            if (mediaPlayerService.isPlaying(false)) {
                Log.d("TAKIMED", "   pausePlay  ");
                mediaPlayerService.pause(false);
                pausePlay.setImageResource(R.drawable.play);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(playerUpdatesReceiver);

            } else {
                Log.d("TAKIMED", "   startPlay     ");
                mediaPlayerService.start(false);
                pausePlay.setImageResource(R.drawable.pause);
                LocalBroadcastManager.getInstance(this).registerReceiver(playerUpdatesReceiver, new IntentFilter("PlayerUpdates"));
            }
        } else if (!songsList.isEmpty()) {
            if (sharedPrefData.LoadInt("currentSongIndex") != -1) {
                onSongSelected(sharedPrefData.LoadInt("currentSongIndex"));
            }
            else {
                onSongSelected(0);
            }
        }
    }

    private void playNextSong() {
        if (mediaPlayerService != null) {
            mediaPlayerService.next(false);
            adapter.notifyDataSetChanged();
            if (mediaPlayerService.currentIndex != -1) {

                adapter.setSelectedPosition(mediaPlayerService.currentIndex);
                sharedPrefData.SaveInt("currentSongIndex", mediaPlayerService.currentIndex);
                sharedPrefData.SaveString("filename", songsList.get(mediaPlayerService.currentIndex).getFileName());


            }


        }

    }

    private void playPreviousSong() {
        if (mediaPlayerService != null) {
            mediaPlayerService.previous(false);
            adapter.notifyDataSetChanged();
            if (mediaPlayerService.currentIndex != -1) {
//                adapter.notifyItemChanged(mediaPlayerService.currentIndex);
                adapter.setSelectedPosition(mediaPlayerService.currentIndex);
                sharedPrefData.SaveInt("currentSongIndex", mediaPlayerService.currentIndex);
                sharedPrefData.SaveString("filename", songsList.get(mediaPlayerService.currentIndex).getFileName());

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
        }
        int savedSongIndex = sharedPrefData.LoadInt("currentSongIndex");
        String filename = sharedPrefData.LoadString("filename");

        if (savedSongIndex != -1) {

            if (is_index_exist(savedSongIndex) && !filename.isEmpty()) {
                if (songsList.get(savedSongIndex).getFileName().equals(filename)) {
                    adapter.setSelectedPosition(savedSongIndex);

                }
                titleTv.setText(songsList.get(savedSongIndex).getTitle());
                currentTimeTv.setText("00:00");
                totalTimeTv.setText(convertToMMSS(songsList.get(savedSongIndex).getDuration() + ""));

            }
        }
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


}


