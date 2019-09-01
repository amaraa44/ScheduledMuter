package com.davidhalma.scheduledmuter.notification;

import android.app.NotificationManager;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.davidhalma.scheduledmuter.R;

public class MakeNotification {

    private static final String TAG = "MakeNotification";
    private static final String CHANNEL_ID = "Scheduled Muter";

    private Context context;
    private String contentTitle;
    private String contentText;

    public MakeNotification(Context context, String contentTitle, String contentText) {
        this.context = context;
        this.contentTitle = contentTitle;
        this.contentText = contentText;
    }

    public void send(){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVibrate(new long[] {500, 500, 500, 500})
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
            Log.d(TAG, "onReceive: notification sent");
        }else {
            throw new NullPointerException("Can`t send notification.");
        }
    }
}
