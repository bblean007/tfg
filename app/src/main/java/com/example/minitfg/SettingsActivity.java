package com.example.minitfg;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SettingsActivity extends AppCompatActivity {

    Button btnTerminos, btnGuardar, btnVolver;
    Switch switchDarkMode;
    FloatingActionButton fabHelp;

    boolean cambiosRealizados = false;
    boolean modoOscuroActivado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnTerminos = findViewById(R.id.btnTerminos);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnVolver = findViewById(R.id.btnVolver);
        switchDarkMode = findViewById(R.id.switchDarkMode);

        // Detectar estado actual del modo oscuro
        int currentNightMode = getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK;

        if (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            switchDarkMode.setChecked(true);
            modoOscuroActivado = true;
        }

        // Detecta cambios
        switchDarkMode.setOnCheckedChangeListener((btn, checked) -> {
            cambiosRealizados = true;
            modoOscuroActivado = checked;
        });

        // Abrir términos y servicios
        btnTerminos.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://es.wikipedia.org/wiki/Términos_y_condiciones_de_uso"));
            startActivity(i);
        });

        // Guardar la configuración
        btnGuardar.setOnClickListener(v -> guardarConfiguracion());

        // Volver al menú con aviso
        btnVolver.setOnClickListener(v -> volverConAviso());

        fabHelp = findViewById(R.id.fabHelp);
        fabHelp.setOnClickListener(v -> showHelpModal());

        // Intercepta el boton de atrás
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                volverConAviso();
            }
        });
    }

    private void guardarConfiguracion() {
        if (modoOscuroActivado) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        cambiosRealizados = false;

        AlertDialog.Builder dlg = new AlertDialog.Builder(SettingsActivity.this);
        dlg.setTitle("Configuración");
        dlg.setMessage("Se guardaron las configuraciones correctamente");
        dlg.setPositiveButton("OK", null);
        dlg.show();
    }

    private void volverConAviso() {
        if (!cambiosRealizados) {
            finish();
            return;
        }

        AlertDialog.Builder dlg = new AlertDialog.Builder(SettingsActivity.this);
        dlg.setTitle("Cambios sin guardar");
        dlg.setMessage("¿Quieres guardar los cambios antes de salir?");

        dlg.setPositiveButton("Sí, guardar", (dialog, which) -> {
            guardarConfiguracion();
            finish();
        });

        dlg.setNegativeButton("Salir sin guardar", (dialog, which) -> finish());

        dlg.setNeutralButton("Cancelar", null);

        dlg.show();
    }

    private void showHelpModal() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_help, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        
        TextView tvHelpTitle = dialogView.findViewById(R.id.tvHelpTitle);
        TextView tvHelpContent = dialogView.findViewById(R.id.tvHelpContent);
        Button btnClose = dialogView.findViewById(R.id.btnHelpClose);
        
        tvHelpTitle.setText("Ayuda - Ajustes");
        tvHelpContent.setText("Personaliza tu experiencia:\n\n" +
                "• Modo oscuro: Cambia el tema de la aplicación para descansar la vista en entornos oscuros.\n" +
                "• Términos: Consulta las condiciones legales de uso.\n" +
                "• Guardar: No olvides pulsar aquí para aplicar tus cambios.\n" +
                "• Salir: Si intentas salir sin guardar, te avisaremos para que no pierdas tus cambios.");

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
}
