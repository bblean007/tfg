package com.example.minitfg;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Handler;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.minitfg.utils.SessionManager;
import com.example.minitfg.utils.ShakeDetector;
import com.example.minitfg.utils.AlarmHelper;

public class MainActivity extends AppCompatActivity {

    Button btnEmpezar, btnConfig, btnAlarm, btnRankings, btnLogout;
    FloatingActionButton fabHelp;
    ImageView ivSpriteCharacter;
    private static final int TOTAL_FRAMES = 3; // Ajustado a 3 frames reales
    private int currentFrame = 0;
    private Handler spriteHandler = new Handler();
    private Bitmap spriteSheet;
    private int frameWidth, frameHeight;
    
    private ActivityResultLauncher<String> requestNotificationPermissionLauncher;
    private SessionManager sessionManager;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Auth Check
        if (!sessionManager.isLoggedIn()) {
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
        fabHelp = findViewById(R.id.fabHelp);
        ivSpriteCharacter = findViewById(R.id.ivSpriteCharacter);

        setupSpriteAnimation();

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
            sessionManager.logout();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        fabHelp.setOnClickListener(v -> showHelpModal());

        requestNotificationPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                });

        createNotificationChannels();
        askNotificationPermission();

        // Shake Detector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mShakeDetector = new ShakeDetector();
            mShakeDetector.setOnShakeListener(count -> handleShake());
        }
    }

    private void setupSpriteAnimation() {
        try {
            spriteSheet = BitmapFactory.decodeResource(getResources(), R.drawable.sprite);
            if (spriteSheet != null) {
                frameWidth = spriteSheet.getWidth() / TOTAL_FRAMES;
                frameHeight = spriteSheet.getHeight();
                ivSpriteCharacter.setVisibility(View.VISIBLE);
                startAnimationLoop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startAnimationLoop() {
        spriteHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateFrame();
                spriteHandler.postDelayed(this, 50); // 50ms para una animación ultra rápida
            }
        }, 50);
    }

    private void updateFrame() {
        if (spriteSheet == null) return;
        
        int x = currentFrame * frameWidth;
        if (x + frameWidth <= spriteSheet.getWidth()) {
            Bitmap frame = Bitmap.createBitmap(spriteSheet, x, 0, frameWidth, frameHeight);
            ivSpriteCharacter.setImageBitmap(frame);
        }
        
        currentFrame = (currentFrame + 1) % TOTAL_FRAMES;
    }

    private void handleShake() {
        boolean isEnabled = AlarmHelper.toggleMasterAlarm(this);
        
        // Feedback háptico
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(200);
            }
        }

        // Toast informativo
        String message = isEnabled ? "Alarmas ACTIVADAS ⏰" : "Alarmas DESACTIVADAS 🔇";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorManager != null && mAccelerometer != null) {
            mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mShakeDetector);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        spriteHandler.removeCallbacksAndMessages(null);
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

    private void showHelpModal() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_help, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        
        // Configurar contenido contextual
        TextView tvHelpTitle = dialogView.findViewById(R.id.tvHelpTitle);
        TextView tvHelpContent = dialogView.findViewById(R.id.tvHelpContent);
        Button btnClose = dialogView.findViewById(R.id.btnHelpClose);
        
        tvHelpTitle.setText("Ayuda - Pantalla Principal");
        tvHelpContent.setText("Bienvenido a miniTFG. Aquí puedes:\n\n" +
                "• Empezar: Jugar partidas de diferentes asignaturas.\n" +
                "• Configuración: Ajustar preferencias de la app.\n" +
                "• Alarmas: Gestionar tus recordatorios de estudio.\n" +
                "• Rankings: Ver las puntuaciones más altas.\n" +
                "• Agitar: Activa/Desactiva las alarmas rápidamente.");

        btnClose.setOnClickListener(v -> {
            // Animación de salida (fade out)
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
        
        // Animación de entrada (fade in)
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500);
        dialogView.startAnimation(fadeIn);
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
