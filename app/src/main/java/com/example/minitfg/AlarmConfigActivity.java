package com.example.minitfg;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.minitfg.utils.AlarmHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Calendar;

public class AlarmConfigActivity extends AppCompatActivity {

    private CheckBox[] dayCheckBoxes;
    private TimePicker timePicker;
    private Button btnSaveAlarm;
    private FloatingActionButton fabHelp;
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
        fabHelp = findViewById(R.id.fabHelp);

        loadSavedSettings();

        btnSaveAlarm.setOnClickListener(v -> saveAndScheduleAlarms());
        fabHelp.setOnClickListener(v -> showHelpModal());
    }

    private void showHelpModal() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_help, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        
        TextView tvHelpTitle = dialogView.findViewById(R.id.tvHelpTitle);
        TextView tvHelpContent = dialogView.findViewById(R.id.tvHelpContent);
        Button btnClose = dialogView.findViewById(R.id.btnHelpClose);
        
        tvHelpTitle.setText("Ayuda - Configurar Alarma");
        tvHelpContent.setText("Configura tus recordatorios de estudio:\n\n" +
                "• Días: Selecciona qué días de la semana quieres recibir el aviso.\n" +
                "• Hora: Desliza para elegir la hora y el minuto exacto.\n" +
                "• Guardar: Pulsa el botón para activar la programación.\n" +
                "• Acceso rápido: Recuerda que puedes activar/desactivar todas las alarmas agitando el móvil en el menú principal.");

        btnClose.setOnClickListener(v -> {
            AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
            fadeOut.setDuration(300);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}
                @Override public void onAnimationEnd(Animation animation) {
                    dialog.dismiss();
                }
            });
            dialogView.startAnimation(fadeOut);
        });

        dialog.show();
        
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500);
        dialogView.startAnimation(fadeIn);
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

        for (int i = 0; i < 7; i++) {
            editor.putBoolean("day_" + i, dayCheckBoxes[i].isChecked());
        }
        editor.apply();

        AlarmHelper.scheduleAlarms(this);

        Toast.makeText(this, "Alarmas actualizadas", Toast.LENGTH_SHORT).show();
        finish();
    }
}
