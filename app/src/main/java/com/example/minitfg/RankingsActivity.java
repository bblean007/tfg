package com.example.minitfg;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RankingsActivity extends AppCompatActivity {

    private Spinner spinnerSubject, spinnerPeriod;
    private CheckBox cbMyScores;
    private TextView tvRankings;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rankings);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        tvRankings = findViewById(R.id.tvRankings);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        spinnerPeriod = findViewById(R.id.spinnerPeriod);
        cbMyScores = findViewById(R.id.cbMyScores);

        setupSpinners();
        
        cbMyScores.setOnCheckedChangeListener((buttonView, isChecked) -> loadRankings());
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

        Query query = db.collection("scores")
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(20);

        if (myScoresOnly && auth.getCurrentUser() != null) {
            query = query.whereEqualTo("userId", auth.getCurrentUser().getUid());
        }

        if (!subject.equals("Todos")) {
            query = query.whereEqualTo("subject", subject);
        }

        long startTime = 0;
        Calendar calendar = Calendar.getInstance();
        
        // Reset to start of day
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (period.equals("Diario")) {
            startTime = calendar.getTimeInMillis();
        } else if (period.equals("Semanal")) {
            calendar.add(Calendar.DAY_OF_YEAR, -7);
            startTime = calendar.getTimeInMillis();
        } else if (period.equals("Mensual")) {
            calendar.add(Calendar.DAY_OF_YEAR, -30);
            startTime = calendar.getTimeInMillis();
        }

        if (!period.equals("Todos")) {
            query = query.whereGreaterThanOrEqualTo("timestamp", startTime);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            StringBuilder sb = new StringBuilder();
            int rank = 1;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());

            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String email = doc.getString("email");
                Long score = doc.getLong("score");
                String subj = doc.getString("subject");
                Long timestamp = doc.getLong("timestamp");

                if (email == null) email = "Anónimo";
                if (score == null) score = 0L;
                if (timestamp == null) timestamp = 0L;

                sb.append(rank).append(". ").append(email)
                        .append("\n   Puntos: ").append(score)
                        .append(" - ").append(subj)
                        .append("\n   Fecha: ").append(sdf.format(new Date(timestamp)))
                        .append("\n\n");
                rank++;
            }

            if (sb.length() == 0) {
                tvRankings.setText("No hay puntuaciones registradas.");
            } else {
                tvRankings.setText(sb.toString());
            }
        }).addOnFailureListener(e -> {
            tvRankings.setText("Error al cargar rankings: " + e.getMessage() + "\n\nNota: Es posible que necesites crear un índice en Firebase Console si combinas filtros.");
        });
    }
}
