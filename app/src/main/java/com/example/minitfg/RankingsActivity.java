package com.example.minitfg;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.minitfg.network.ApiClient;
import com.example.minitfg.network.models.Score;
import com.example.minitfg.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RankingsActivity extends AppCompatActivity {

    private Spinner spinnerSubject, spinnerPeriod;
    private CheckBox cbMyScores;
    private TextView tvRankings;
    private Button btnBackToMenu;
    private FloatingActionButton fabHelp;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rankings);
        
        sessionManager = new SessionManager(this);

        tvRankings = findViewById(R.id.tvRankings);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        spinnerPeriod = findViewById(R.id.spinnerPeriod);
        cbMyScores = findViewById(R.id.cbMyScores);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);
        fabHelp = findViewById(R.id.fabHelp);

        setupSpinners();
        
        cbMyScores.setOnCheckedChangeListener((buttonView, isChecked) -> loadRankings());

        btnBackToMenu.setOnClickListener(v -> finish());

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
        
        tvHelpTitle.setText("Ayuda - Rankings");
        tvHelpContent.setText("Consulta las mejores puntuaciones:\n\n" +
                "• Asignatura: Filtra los resultados por materia.\n" +
                "• Periodo: Elige entre ver puntuaciones diarias, semanales o mensuales.\n" +
                "• Mis puntuaciones: Activa esta casilla para ver solo tus propios resultados.\n" +
                "• Los datos se actualizan automáticamente al cambiar cualquier filtro.");

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

    private void setupSpinners() {
        String[] subjects = {"Todos", "Matemáticas", "Lengua", "Inglés", "Conocimiento del medio"};
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subjectAdapter);

        String[] periods = {"Todos", "Diario", "Semanal", "Mensual"};
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, periods);
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(periodAdapter);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadRankings();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerSubject.setOnItemSelectedListener(listener);
        spinnerPeriod.setOnItemSelectedListener(listener);
    }

    private void loadRankings() {
        tvRankings.setText("Cargando...");
        
        String subject = spinnerSubject.getSelectedItem().toString();
        String period = spinnerPeriod.getSelectedItem().toString();
        boolean myScoresOnly = cbMyScores.isChecked();
        
        Integer userId = null;
        if (myScoresOnly && sessionManager.isLoggedIn()) {
            userId = sessionManager.getUserId();
        }

        ApiClient.getApiService().getScores(subject, period, userId).enqueue(new Callback<List<Score>>() {
            @Override
            public void onResponse(Call<List<Score>> call, Response<List<Score>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Score> scores = response.body();
                    
                    if (scores.isEmpty()) {
                        tvRankings.setText("No hay puntuaciones registradas.");
                        return;
                    }

                    StringBuilder sb = new StringBuilder();
                    int rank = 1;
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());

                    for (Score s : scores) {
                        String displayName = s.getUsername();
                        
                        // Log para depuración
                        android.util.Log.d("Rankings", "Score: " + s.getScore() + ", User: " + s.getUsername() + ", Email: " + s.getEmail());

                        if (displayName == null || displayName.isEmpty() || displayName.equals("null")) {
                            displayName = s.getEmail();
                        }
                        if (displayName == null || displayName.isEmpty()) displayName = "Anónimo";
                        
                        sb.append(rank).append(". ").append(displayName)
                                .append("\n   Puntos: ").append(s.getScore())
                                .append(" - ").append(s.getSubject())
                                .append("\n   Fecha: ").append(sdf.format(new Date(s.getTimestamp())))
                                .append("\n\n");
                        rank++;
                    }
                    tvRankings.setText(sb.toString());

                } else {
                    tvRankings.setText("Error al cargar rankings: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Score>> call, Throwable t) {
                tvRankings.setText("Error de conexión: " + t.getMessage());
            }
        });
    }
}
