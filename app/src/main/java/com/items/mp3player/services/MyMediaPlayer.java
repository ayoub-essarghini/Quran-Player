package com.items.mp3player.services;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.items.mp3player.model.AudioModel;
import com.items.mp3player.utils.SharedPrefData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class MyMediaPlayer extends Service {

    public static MediaPlayer mediaPlayer;
    public static MediaPlayer mediaPlayerOnline;
    private ArrayList<AudioModel> songList;
    public int currentIndex =0;
    public static boolean IsOnline = false;
    private final IBinder binder = new MyMediaPlayerBinder();
    private Handler handler = new Handler();

    private NotificationManager notificationManager;

    SharedPrefData sharedPrefData;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("TAKIMED", "   onCreate service");
        notificationManager = new NotificationManager(this);
        notificationManager.createNotificationChannel(); // Create the notification channel
        sharedPrefData = new SharedPrefData(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            currentIndex = intent.getIntExtra("songIndex", -1);
            IsOnline = intent.getBooleanExtra("online", false);
            if (intent.hasExtra("songList")) {
                songList = intent.getParcelableArrayListExtra("songList");
            }
            if (songList != null && currentIndex != -1) {
                playSong(songList.get(currentIndex));
            }
        }
        return START_STICKY;
    }

    public void stop() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null; // Release the MediaPlayer resources
            handler.removeCallbacksAndMessages(null); // Stop updating progress

            // Broadcast that playback has stopped
            Intent intent = new Intent("PlayerUpdates");
            intent.putExtra("isPlaying", false);
            intent.putExtra("currentPosition", 0);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            // Optionally, update the notification to reflect the stopped state
            notificationManager.showNotification(null, false); // Or clear it
        }
    }

    public void playSong(AudioModel song) {
        if (!IsOnline) {
            if (mediaPlayerOnline != null) {
                mediaPlayerOnline.release();
                mediaPlayerOnline=null;
            }
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            try {
                if (song.getPath().startsWith("file:///android_asset/")) {
                    AssetFileDescriptor afd = getAssets().openFd(song.getPath().replace("file:///android_asset/", ""));
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    afd.close();
                } else {
                    mediaPlayer.setDataSource(song.getPath());
                }
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        MyMediaPlayer.this.mediaPlayer = mediaPlayer;
                        mediaPlayer.start();
                        notificationManager.showNotification(song, true);
                        broadcastSongChange(song);
                        startUpdatingProgress();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer=null;
            }
            if (mediaPlayerOnline != null) {
                mediaPlayerOnline.stop();
                mediaPlayerOnline.release();
            }
            mediaPlayerOnline = new MediaPlayer();

            try {
                mediaPlayerOnline.setDataSource(song.getPath());
                mediaPlayerOnline.prepare();
                mediaPlayerOnline.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        MyMediaPlayer.this.mediaPlayerOnline = mediaPlayer;
                        mediaPlayer.start();
                        notificationManager.showNotification(song, true);
                        broadcastSongChange(song);
                        startUpdatingProgress();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void pause(boolean isOnline) {
        if (!isOnline) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                broadcastPlaybackState();

            }
        } else {
            if (mediaPlayerOnline != null && mediaPlayerOnline.isPlaying()) {
                mediaPlayerOnline.pause();
                broadcastPlaybackState();
            }
        }

    }

    public void start(boolean isOnline) {
        if (!isOnline) {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                broadcastPlaybackState();
            }
        } else {
            if (mediaPlayerOnline != null && !mediaPlayerOnline.isPlaying()) {
                mediaPlayerOnline.start();
                broadcastPlaybackState();
            }
        }
    }

    public boolean isPlaying(boolean isOnline) {
        if (!isOnline) {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        }
        else {
            return mediaPlayerOnline != null && mediaPlayerOnline.isPlaying();
        }
    }

    public void seekTo(int position) {
        if (!IsOnline)
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(position);
            } else if (mediaPlayerOnline != null) {
                mediaPlayerOnline.seekTo(position);
            }
    }

    private static final int HISTORY_SIZE = 3; // Adjust this size as needed
    private Queue<Integer> recentlyPlayed = new LinkedList<>(); // Stores recently played indices

    public void next(boolean isOnline) {
        if (this.IsOnline == isOnline)
            if (currentIndex != -1) {
                if (!sharedPrefData.LoadBoolean("shuffle")) {
                    if (currentIndex < songList.size() - 1) {
                        currentIndex++;
                        playSong(songList.get(currentIndex));

                    }
                } else {
                    Random random = new Random();
                    int newIndex;


                    do {
                        newIndex = random.nextInt(songList.size());
                    } while (newIndex == currentIndex || recentlyPlayed.contains(newIndex));


                    currentIndex = newIndex;
                    playSong(songList.get(currentIndex));


                    recentlyPlayed.add(currentIndex);
                    if (recentlyPlayed.size() > HISTORY_SIZE) {
                        recentlyPlayed.poll();
                    }
                }
            }
    }

    public void previous(boolean isOnline) {
        if (this.IsOnline == isOnline)
            if (currentIndex != -1) {
                if (!sharedPrefData.LoadBoolean("shuffle")) {
                    if (currentIndex > 0) {
                        currentIndex--;
                        playSong(songList.get(currentIndex));
                    }
                } else {
                    Random random = new Random();
                    int newIndex;


                    do {
                        newIndex = random.nextInt(songList.size());
                    } while (newIndex == currentIndex || recentlyPlayed.contains(newIndex));


                    currentIndex = newIndex;
                    playSong(songList.get(currentIndex));


                    recentlyPlayed.add(currentIndex);
                    if (recentlyPlayed.size() > HISTORY_SIZE) {
                        recentlyPlayed.poll();
                    }
                }
            }

    }

    private void broadcastPlaybackState() {
        Intent intent = new Intent("PlayerUpdates");
        intent.putExtra("isPlaying", isPlaying(IsOnline));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastSongChange(AudioModel song) {
        Intent intent = new Intent("PlayerUpdates");
        intent.putExtra("songChanged", song);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public boolean isOnline() {
        return IsOnline;
    }

    private void startUpdatingProgress() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!IsOnline) {
                    Intent intent = new Intent("PlayerUpdates");
                    intent.putExtra("currentPosition", mediaPlayer.getCurrentPosition());
                    intent.putExtra("totalDuration", mediaPlayer.getDuration());
                    intent.putExtra("songTitle", songList.get(currentIndex).getTitle());
                    intent.putExtra("online", IsOnline);
                    LocalBroadcastManager.getInstance(MyMediaPlayer.this).sendBroadcast(intent);
                } else {
                    Intent intent = new Intent("PlayerUpdates");
                    intent.putExtra("currentPosition", mediaPlayerOnline.getCurrentPosition());
                    intent.putExtra("totalDuration", mediaPlayerOnline.getDuration());
                    intent.putExtra("songTitle", songList.get(currentIndex).getTitle());
                    intent.putExtra("online", IsOnline);
                    LocalBroadcastManager.getInstance(MyMediaPlayer.this).sendBroadcast(intent);
                }
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    public class MyMediaPlayerBinder extends Binder {
        public MyMediaPlayer getService() {
            return MyMediaPlayer.this;
        }
    }
}
