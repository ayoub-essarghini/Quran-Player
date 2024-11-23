package com.items.mp3player.services;

import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.items.mp3player.R;
import com.items.mp3player.model.AudioModel;
import com.items.mp3player.ui.PlayList;
import com.items.mp3player.ui.PlayListOnline;

public class NotificationManager {

    Context context;

    public NotificationManager(Context context) {
        this.context = context;
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "custom_notification_channel";
            String channelName = "Custom Notification";
            NotificationChannel channel = new NotificationChannel(channelId, channelName, android.app.NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel for custom notification");
            android.app.NotificationManager notificationManager = context.getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showNotification(AudioModel song, boolean mediaStatus) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Permission not granted to post notifications", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Set the custom layout for the notification
        RemoteViews customView = new RemoteViews(context.getPackageName(), R.layout.custom_notification);
        customView.setTextViewText(R.id.custom_title, song.getArtist());
        customView.setTextViewText(R.id.small_title, song.getArtist());
        if (mediaStatus)
            customView.setTextViewText(R.id.body_notif, song.getTitle() + " (Playing)");
        else
            customView.setTextViewText(R.id.body_notif, song.getTitle() + " (Paused)");

        // Create an Intent to launch the app when notification is clicked
        Intent intent;
        if (MyMediaPlayer.IsOnline)
            intent = new Intent(context, PlayListOnline.class);
        else {
            intent = new Intent(context, PlayList.class);
        } // Change MainActivity to the activity you want to open
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Create the PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "custom_notification_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Notification icon
//                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(customView)
                .setCustomBigContentView(customView) // For expanded notification
                .setPriority(NotificationCompat.PRIORITY_HIGH) // High importance
                .setAutoCancel(false) // Notification won't disappear when clicked
                .setOngoing(true) // Non-removable notification
                .setContentIntent(pendingIntent); // Set the PendingIntent for notification click

        // Show the notification with a unique ID
        notificationManager.notify(1, builder.build());
    }
}
