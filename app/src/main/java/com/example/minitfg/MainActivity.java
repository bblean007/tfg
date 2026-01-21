package com.example.minitfg;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    Button btnEmpezar, btnConfig, btnAlarm, btnRankings, btnLogout;
    private ActivityResultLauncher<String> requestNotificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Auth Check
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        btnEmpezar = findViewById(R.id.btnEmpezar);
        btnConfig = findViewById(R.id.btnConfig);
        btnAlarm = findViewById(R.id.btnAlarm);
        btnRankings = findViewById(R.id.btnRankings);
        btnLogout = findViewById(R.id.btnLogout);

        btnEmpezar.setOnClickListener(v -> showGameMenu());

        btnConfig.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
        });

        btnAlarm.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, AlarmConfigActivity.class);
            startActivity(i);
        });
        
        btnRankings.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, RankingsActivity.class);
            startActivity(i);
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        requestNotificationPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                });

        createNotificationChannels();
        askNotificationPermission();
    }

    private void showGameMenu() {
        String[] options = {"Matemáticas", "Lengua", "Inglés", "Conocimiento del medio"};
        
        new AlertDialog.Builder(this)
                .setTitle("Elige una asignatura")
                .setItems(options, (dialog, which) -> {
                    Intent intent = new Intent(MainActivity.this, GameActivity.class);
                    intent.putExtra("SUBJECT", options[which]);
                    startActivity(intent);
                })
                .show();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel studyChannel = new NotificationChannel(
                    "study_channel", "Recordatorios de Estudio",
                    NotificationManager.IMPORTANCE_HIGH);
            studyChannel.setDescription("Recordatorios semanales para estudiar");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(studyChannel);
        }
    }
}
