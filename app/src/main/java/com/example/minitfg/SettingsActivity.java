package com.example.minitfg;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    Button btnTerminos, btnGuardar, btnVolver;
    Switch switchDarkMode;

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
}
