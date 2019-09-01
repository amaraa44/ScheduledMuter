package com.davidhalma.scheduledmuter.alarmreceiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.widget.Toast;

import com.davidhalma.scheduledmuter.R;
import com.davidhalma.scheduledmuter.notification.MakeNotification;

public class VibrateAudioAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "VibrateAudioAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "ALARM", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onReceive: ALARM SENT");

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        Log.d(TAG, "onReceive: audiomanager");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d(TAG, "onReceive: notificationmanager");

        if (audioManager != null && notificationManager != null) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            Log.d(TAG, "onReceive: new AudioSetting: " + audioManager.getRingerMode());

            new MakeNotification(context, context.getString(R.string.vibrate_alarm_started),
                    context.getString(R.string.from_now_it_will_be_almost_silent))
                    .send();
        }
        Log.d(TAG, "onReceive: end");
    }
}
