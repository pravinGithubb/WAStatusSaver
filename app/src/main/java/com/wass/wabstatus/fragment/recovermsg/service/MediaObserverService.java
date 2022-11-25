package com.wass.wabstatus.fragment.recovermsg.service;

import static android.os.FileObserver.ALL_EVENTS;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.wass.wabstatus.MainActivity;
import com.wass.wabstatus.R;

import java.io.File;

public class MediaObserverService extends Service {

    private MediaObserver mediaObserver = null;

    @Override
    public void onCreate() {
        super.onCreate();
        String whatsappPath = "";
        if (new File(Environment.getExternalStorageDirectory() + File.separator + "Android/media/com.whatsapp/WhatsApp" + File.separator + "Media" + File.separator + "WhatsApp Images").isDirectory()) {
            whatsappPath = "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images";
        } else {
            whatsappPath = "WhatsApp/Media/WhatsApp Images";
        }

        mediaObserver = new MediaObserver(new File(Environment.getExternalStorageDirectory(), whatsappPath).toString(), ALL_EVENTS);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, (Build.VERSION.SDK_INT > 30 ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT));

        String channelId = "10711";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel(channelId, "BlissLife_Status_Saver_MediaObserver");
        } else {
            // If earlier version channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
//            channelId = "";
        }

        Notification notification = new NotificationCompat.Builder(this,
                channelId)
                .setContentTitle("Media Observer")
                .setContentText("Watching for new message")
                .setSmallIcon(R.drawable.ic_delete)
                .setContentIntent(pendingIntent)
                .setTicker("Watching for new message")
                .build();

        startForeground(51, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaObserver.startWatching();
        return super.onStartCommand(intent, flags, startId);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaObserver.stopWatching();
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel channel = new NotificationChannel(channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH);
        //   chan.lightColor = Color.BLUE
        //chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }
}
