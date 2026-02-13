package com.example.minitfg.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.example.minitfg.AlarmReceiver;
import java.util.Calendar;

public class AlarmHelper {

    private static final String PREFS_NAME = "AlarmPrefs";
    private static final String MASTER_KEY = "alarm_master_enabled";

    public static void scheduleAlarms(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean masterEnabled = prefs.getBoolean(MASTER_KEY, true);
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int hour = prefs.getInt("hour", 9);
        int minute = prefs.getInt("minute", 0);

        int[] calendarDays = {
                Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
        };

        for (int i = 0; i < 7; i++) {
            boolean isDayChecked = prefs.getBoolean("day_" + i, false);
            int alarmId = 100 + i;
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("message", "¡Hora de estudiar!");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, alarmId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (masterEnabled && isDayChecked) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.DAY_OF_WEEK, calendarDays[i]);

                if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 7);
                }

                if (alarmManager != null) {
                    alarmManager.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            AlarmManager.INTERVAL_DAY * 7,
                            pendingIntent
                    );
                }
            } else {
                if (alarmManager != null) {
                    alarmManager.cancel(pendingIntent);
                }
            }
        }
    }

    public static boolean toggleMasterAlarm(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean currentState = prefs.getBoolean(MASTER_KEY, true);
        boolean newState = !currentState;
        
        prefs.edit().putBoolean(MASTER_KEY, newState).apply();
        scheduleAlarms(context);
        return newState;
    }

    public static boolean isAlarmEnabled(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(MASTER_KEY, true);
    }
}
