package com.example.minitfg;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class AlarmConfigActivity extends AppCompatActivity {

    private CheckBox[] dayCheckBoxes;
    private TimePicker timePicker;
    private Button btnSaveAlarm;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_config);

        prefs = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);

        dayCheckBoxes = new CheckBox[7];
        dayCheckBoxes[0] = findViewById(R.id.cbMon); // Calendar.MONDAY = 2
        dayCheckBoxes[1] = findViewById(R.id.cbTue);
        dayCheckBoxes[2] = findViewById(R.id.cbWed);
        dayCheckBoxes[3] = findViewById(R.id.cbThu);
        dayCheckBoxes[4] = findViewById(R.id.cbFri);
        dayCheckBoxes[5] = findViewById(R.id.cbSat);
        dayCheckBoxes[6] = findViewById(R.id.cbSun); // Calendar.SUNDAY = 1

        timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        btnSaveAlarm = findViewById(R.id.btnSaveAlarm);

        loadSavedSettings();

        btnSaveAlarm.setOnClickListener(v -> saveAndScheduleAlarms());
    }

    private void loadSavedSettings() {
        for (int i = 0; i < 7; i++) {
            dayCheckBoxes[i].setChecked(prefs.getBoolean("day_" + i, false));
        }
        int hour = prefs.getInt("hour", 9);
        int minute = prefs.getInt("minute", 0);
        timePicker.setHour(hour);
        timePicker.setMinute(minute);
    }

    private void saveAndScheduleAlarms() {
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("hour", hour);
        editor.putInt("minute", minute);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Map array index to Calendar days
        // Index 0 (Mon) -> Calendar.MONDAY (2)
        // Index 6 (Sun) -> Calendar.SUNDAY (1)
        int[] calendarDays = {
                Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
        };

        for (int i = 0; i < 7; i++) {
            boolean isChecked = dayCheckBoxes[i].isChecked();
            editor.putBoolean("day_" + i, isChecked);

            int alarmId = 100 + i; // IDs 100 to 106
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("message", "¡Hora de estudiar!");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, alarmId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (isChecked) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.DAY_OF_WEEK, calendarDays[i]);

                // Check if this time has already passed this week
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

        editor.apply();
        Toast.makeText(this, "Alarmas actualizadas", Toast.LENGTH_SHORT).show();
        finish();
    }
}
