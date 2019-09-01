package com.davidhalma.scheduledmuter;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.davidhalma.scheduledmuter.alarmreceiver.NormalAudioAlarmReceiver;
import com.davidhalma.scheduledmuter.alarmreceiver.SilentAudioAlarmReceiver;
import com.davidhalma.scheduledmuter.alarmreceiver.VibrateAudioAlarmReceiver;
import com.davidhalma.scheduledmuter.notification.MakeNotification;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String MORNING_BUTTON_STATE = "MORNING_BUTTON_STATE";
    private static final String NIGHT_BUTTON_STATE = "NIGHT_BUTTON_STATE";
    private static final String  MORNING_CALENDAR_STATE = "MORNING_CALENDAR_STATE";
    private static final String  NIGHT_CALENDAR_STATE = "NIGHT_CALENDAR_STATE";

    public static final String SCHEDULER_PREFERENCES = "SCHEDULER_PREFERENCES" ;
    private static final String STATUS_TEXT = "STATUS_TEXT";

    private Calendar morningCalendar;
    private Calendar nightCalendar;

    private AlarmManager morningAlarmManager;
    private AlarmManager nightAlarmManager;

    private PendingIntent morningPendingIntent;
    private PendingIntent nightPendingIntent;

    private RadioGroup morningRadioGroup;
    private RadioGroup nightRadioGroup;

    private TextView statusTextView;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        morningAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        nightAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        morningRadioGroup = findViewById(R.id.morningRadioGroup);
        nightRadioGroup = findViewById(R.id.nightRadioGroup);

        statusTextView = findViewById(R.id.statusTextView);

        if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
            grantNotificationPolicyAlertDialog();
        }

        loadLatestSettings();
    }

    public void loadLatestSettings(){
        sharedPreferences = getSharedPreferences(SCHEDULER_PREFERENCES, Context.MODE_PRIVATE);

        int checkedMorningButtonId = sharedPreferences.getInt(MORNING_BUTTON_STATE, R.id.morningNormalRadioButton);
        morningRadioGroup.check(checkedMorningButtonId);

        int checkedNightButtonId = sharedPreferences.getInt(NIGHT_BUTTON_STATE, R.id.nightVibrateRadioButton);
        nightRadioGroup.check(checkedNightButtonId);


        if (morningPendingIntent == null && sharedPreferences.contains(MORNING_CALENDAR_STATE)){
            morningPendingIntent = PendingIntent.getBroadcast(this, 0, getMorningIntent(), PendingIntent.FLAG_UPDATE_CURRENT);
        }
        if (nightPendingIntent == null && sharedPreferences.contains(NIGHT_CALENDAR_STATE)) {
            nightPendingIntent = PendingIntent.getBroadcast(this, 1, getNightIntent(), PendingIntent.FLAG_UPDATE_CURRENT);
        }

        long morningCalendarTime = sharedPreferences.getLong(MORNING_CALENDAR_STATE, new Date().getTime());
        morningCalendar = Calendar.getInstance();
        morningCalendar.setTimeInMillis(morningCalendarTime);

        long nightCalendarTime = sharedPreferences.getLong(NIGHT_CALENDAR_STATE, new Date().getTime());
        nightCalendar = Calendar.getInstance();
        nightCalendar.setTimeInMillis(nightCalendarTime);

        String statusText = sharedPreferences.getString(STATUS_TEXT, getString(R.string.status_not_running));
        statusTextView.setText(statusText);
    }

    public void grantNotificationPolicy(){
        Intent intent = new Intent(
                android.provider.Settings
                        .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        startActivity(intent);
    }

    public void grantNotificationPolicyAlertDialog(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(getString(R.string.permission_message));
        alertBuilder.setPositiveButton(
                getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        grantNotificationPolicy();
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    public void startButtonOnClick(View view) {
        Log.d(TAG, "startButtonOnClick: clicked");
        morningChange();
        nightChange();

        Toast.makeText(this, getString(R.string.started), Toast.LENGTH_LONG).show();

        setStatusTextView(getString(R.string.status_running));
    }

    public void stopButtonOnClick(View view) {
        Log.d(TAG, "stopButtonOnClick: stop");

        Log.i(TAG, "stopButtonOnClick: " + morningPendingIntent.toString());
        Log.i(TAG, "stopButtonOnClick: " + nightPendingIntent.toString());

        morningAlarmManager.cancel(morningPendingIntent);
        nightAlarmManager.cancel(nightPendingIntent);

        Toast.makeText(this, getString(R.string.stopped), Toast.LENGTH_LONG).show();

        
        setStatusTextView(getString(R.string.status_not_running));
    }

    private void setStatusTextView(String text){
        statusTextView.setText(text);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(STATUS_TEXT, text);
        editor.apply();
    }

    private void morningChange() {
        Log.d(TAG, "morningCalendar: started");

        Intent morningIntent = getMorningIntent();

        morningPendingIntent = PendingIntent.getBroadcast(this, 0, morningIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d(TAG, "morningCalendar: pendingintent");

        if (morningAlarmManager != null) {
            morningAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, morningCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, morningPendingIntent);
            Log.d(TAG, "morningCalendar: alarmmanager");
        }else {
            new MakeNotification(this, getString(R.string.something_went_wrong), getString(R.string.error_is_here) + "1").send();
        }
        Log.d(TAG, "morningCalendar: end");
    }

    private void nightChange() {
        Log.d(TAG, "nightChange: started");

        Intent nightIntent = getNightIntent();

        nightPendingIntent = PendingIntent.getBroadcast(this, 1, nightIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d(TAG, "nightChange: pendingintent");

        if (nightAlarmManager != null) {
            nightAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, nightCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, nightPendingIntent);
            Log.d(TAG, "nightChange: alarmmanager");
        }else {
            new MakeNotification(this, getString(R.string.something_went_wrong), getString(R.string.error_is_here) + "2").send();
        }
        Log.d(TAG, "nightChange: end");
    }

    private Intent getMorningIntent(){
        Intent morningIntent = null;

        int checkedRadioButtonId = morningRadioGroup.getCheckedRadioButtonId();
        RadioButton morningRadioButton = findViewById(checkedRadioButtonId);
        Log.d(TAG, "morningCalendar: radioButton " + morningRadioButton.getText());

        switch (morningRadioButton.getId()){
            case R.id.morningSilentRadioButton:
                morningIntent = new Intent(this, SilentAudioAlarmReceiver.class);
                Log.d(TAG, "morningCalendar: SilentAudioAlarmReceiver");
                break;
            case R.id.morningVibrateRadioButton:
                morningIntent = new Intent(this, VibrateAudioAlarmReceiver.class);
                Log.d(TAG, "morningCalendar: VibrateAudioAlarmReceiver");
                break;
            case R.id.morningNormalRadioButton:
                morningIntent = new Intent(this, NormalAudioAlarmReceiver.class);
                Log.d(TAG, "morningCalendar: NormalAudioAlarmReceiver");
                break;
        }

        if (morningIntent == null) {
            Toast.makeText(this, getString(R.string.select_ringer_mode), Toast.LENGTH_LONG).show();
            throw new NullPointerException("Intent is null");
       }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(MORNING_BUTTON_STATE, morningRadioButton.getId());
        editor.apply();
        Log.d(TAG, "morningCalendar: SharedPreferences");

        return morningIntent;
    }

    private Intent getNightIntent(){
        Intent nightIntent = null;

        int checkedRadioButtonId = nightRadioGroup.getCheckedRadioButtonId();
        RadioButton nightRadioButton = findViewById(checkedRadioButtonId);
        Log.d(TAG, "nightChange: radiobutton");

        switch (nightRadioButton.getId()){
            case R.id.nightSilentRadioButton:
                nightIntent = new Intent(this, SilentAudioAlarmReceiver.class);
                Log.d(TAG, "nightChange: silentAudioAlarmReceiver");
                break;
            case R.id.nightVibrateRadioButton:
                nightIntent = new Intent(this, VibrateAudioAlarmReceiver.class);
                Log.d(TAG, "nightChange: VibrateAudioAlarmReceiver");
                break;
            case R.id.nightNormalRadioButton:
                nightIntent = new Intent(this, NormalAudioAlarmReceiver.class);
                Log.d(TAG, "nightChange: NormalAudioAlarmReceiver");
                break;
        }

        if (nightIntent == null) {
            Toast.makeText(this, getString(R.string.select_ringer_mode), Toast.LENGTH_LONG).show();
            throw new NullPointerException("Night intent is null");
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(NIGHT_BUTTON_STATE, nightRadioButton.getId());
        editor.apply();
        Log.d(TAG, "nightChange: SharedPreferences");

        return nightIntent;
    }


    public void morningCalendarButtonOnClick(View view) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                Log.d(TAG, "morning onTimeSet: " + hourOfDay + " " + minutes);
                morningCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                morningCalendar.set(Calendar.MINUTE, minutes);
                Log.d(TAG, "onTimeSet: " + morningCalendar);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(MORNING_CALENDAR_STATE, morningCalendar.getTime().getTime());
                editor.apply();
                Log.d(TAG, "onTimeSet: sharedPreferences");
            }
        }, morningCalendar.get(Calendar.HOUR_OF_DAY), morningCalendar.get(Calendar.MINUTE), true);

        timePickerDialog.show();
        Log.d(TAG, "morningCalendarButtonOnClick: end");
    }

    public void nightCalendarButtonOnClick(View view) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                Log.d(TAG, "night onTimeSet: " + hourOfDay + " " + minutes);
                nightCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                nightCalendar.set(Calendar.MINUTE, minutes);
                Log.d(TAG, "onTimeSet: " + nightCalendar);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(NIGHT_CALENDAR_STATE, nightCalendar.getTime().getTime());
                editor.apply();
                Log.d(TAG, "onTimeSet: sharedPreferences");
            }
        }, nightCalendar.get(Calendar.HOUR_OF_DAY), nightCalendar.get(Calendar.MINUTE), true);

        timePickerDialog.show();
        Log.d(TAG, "nightCalendarButtonOnClick: end");
    }
}
