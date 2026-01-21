package com.example.minitfg;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class GameActivity extends AppCompatActivity {

    TextView txtPregunta, txtPuntuacion;
    EditText edtRespuesta;
    Button btnResponder;

    int num1, num2;
    String operador;
    String respuestaCorrecta;
    String subject = "Matemáticas";

    int puntuacion = 0;
    int nivel = 1;

    private Thread descansoVistaThread;
    private volatile boolean isRunning = false;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final long INTERVALO_DESCANSO = 300000; // 5 minutos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if (getIntent().hasExtra("SUBJECT")) {
            subject = getIntent().getStringExtra("SUBJECT");
        }

        txtPregunta = findViewById(R.id.txtPregunta);
        edtRespuesta = findViewById(R.id.edtRespuesta);
        btnResponder = findViewById(R.id.btnResponder);
        txtPuntuacion = findViewById(R.id.txtPuntuacion);
        
        TextView txtTitulo = findViewById(R.id.txtTitulo); // Assuming I might add this, but let's just use title bar or toast
        setTitle("Juego: " + subject);

        // Configurar input type según asignatura
        if (subject.equals("Matemáticas")) {
            edtRespuesta.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        } else {
            edtRespuesta.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        nuevaPregunta();

        btnResponder.setOnClickListener(v -> comprobarRespuesta());
        ImageButton btnPause = findViewById(R.id.btnPause);
        btnPause.setOnClickListener(v -> mostrarMenuPausa());

        iniciarDescansoVista();
    }

    private void iniciarDescansoVista() {
        pararDescansoVista();
        isRunning = true;
        descansoVistaThread = new Thread(() -> {
            while (isRunning) {
                try {
                    Thread.sleep(INTERVALO_DESCANSO);
                    if (isRunning) {
                        mainHandler.post(this::mostrarAvisoDescansoVista);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        descansoVistaThread.start();
    }

    private void pararDescansoVista() {
        isRunning = false;
        if (descansoVistaThread != null) {
            descansoVistaThread.interrupt();
        }
    }

    private void mostrarAvisoDescansoVista() {
        new AlertDialog.Builder(GameActivity.this)
                .setTitle("⏰ Descansa la vista")
                .setMessage("Deberías descansar la vista\n\nMira a 20 metros durante 20 segundos")
                .setPositiveButton("Entendido", (dialog, which) -> {
                    dialog.dismiss();
                    iniciarDescansoVista();
                })
                .setCancelable(false)
                .show();
    }

    private void nuevaPregunta() {
        edtRespuesta.setText("");

        if (subject.equals("Matemáticas")) {
            generarPreguntaMatematicas();
        } else {
            generarPreguntaTexto();
        }
    }

    private void generarPreguntaMatematicas() {
        String[] ops = {"+", "-", "×"};
        if (nivel < 3) ops = new String[]{"+", "-"};

        operador = ops[(int) (Math.random() * ops.length)];
        int max = nivel * 10;
        num1 = (int) (Math.random() * max) + 1;
        num2 = (int) (Math.random() * max) + 1;

        switch (operador) {
            case "+": respuestaCorrecta = String.valueOf(num1 + num2); break;
            case "-": respuestaCorrecta = String.valueOf(num1 - num2); break;
            case "×": respuestaCorrecta = String.valueOf(num1 * num2); break;
        }
        txtPregunta.setText(num1 + " " + operador + " " + num2);
    }

    private void generarPreguntaTexto() {
        // Simple demo questions
        if (subject.equals("Inglés")) {
            String[] questions = {"One", "Red", "Cat", "Sun"};
            String[] answers = {"Uno", "Rojo", "Gato", "Sol"};
            int idx = (int) (Math.random() * questions.length);
            txtPregunta.setText("Traduce: " + questions[idx]);
            respuestaCorrecta = answers[idx];
        } else if (subject.equals("Lengua")) {
            String[] questions = {"Sinónimo de Feliz", "Antónimo de Alto", "Plural de Pez"};
            String[] answers = {"Contento", "Bajo", "Peces"};
            int idx = (int) (Math.random() * questions.length);
            txtPregunta.setText(questions[idx]);
            respuestaCorrecta = answers[idx];
        } else {
            // Conocimiento del medio / Science
            String[] questions = {"¿Cuántas patas tiene una araña?", "¿El sol es una estrella?", "¿El agua hierve a 100 grados?"};
            String[] answers = {"8", "Si", "Si"};
            int idx = (int) (Math.random() * questions.length);
            txtPregunta.setText(questions[idx]);
            respuestaCorrecta = answers[idx];
        }
    }

    private void comprobarRespuesta() {
        String valor = edtRespuesta.getText().toString().trim();

        if (valor.isEmpty()) {
            edtRespuesta.setError("Escribe una respuesta");
            return;
        }

        if (valor.equalsIgnoreCase(respuestaCorrecta)) {
            puntuacion++;
            txtPuntuacion.setText("Puntuación: " + puntuacion);

            if (puntuacion % 5 == 0) {
                nivel++;
            }
            nuevaPregunta();
        } else {
            mostrarDialogFinal();
        }
    }

    private void mostrarDialogFinal() {
        pararDescansoVista();
        guardarPuntuacion();

        AlertDialog.Builder dlg = new AlertDialog.Builder(GameActivity.this);
        dlg.setTitle("Juego terminado");
        dlg.setMessage("Fallaste. La respuesta era: " + respuestaCorrecta + "\nPuntuación final: " + puntuacion);

        dlg.setPositiveButton("Reintentar", (dialog, which) -> {
            puntuacion = 0;
            nivel = 1;
            txtPuntuacion.setText("Puntuación: 0");
            nuevaPregunta();
            iniciarDescansoVista();
        });

        dlg.setNegativeButton("Volver al menú", (dialog, which) -> finish());
        dlg.setCancelable(false);
        dlg.show();
    }

    private void guardarPuntuacion() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null && puntuacion > 0) {
            Map<String, Object> scoreData = new HashMap<>();
            scoreData.put("userId", auth.getCurrentUser().getUid());
            scoreData.put("email", auth.getCurrentUser().getEmail());
            scoreData.put("subject", subject);
            scoreData.put("score", puntuacion);
            scoreData.put("timestamp", System.currentTimeMillis());

            FirebaseFirestore.getInstance().collection("scores")
                    .add(scoreData)
                    .addOnSuccessListener(ref -> Toast.makeText(this, "Puntuación guardada", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pararDescansoVista();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pararDescansoVista();
    }

    @Override
    protected void onResume() {
        super.onResume();
        iniciarDescansoVista();
    }
    
    private void mostrarMenuPausa() {
        AlertDialog.Builder dlg = new AlertDialog.Builder(GameActivity.this);
        dlg.setTitle("Pausa");
        dlg.setMessage("¿Qué quieres hacer?");
        dlg.setPositiveButton("Seguir", (dialog, which) -> dialog.dismiss());
        dlg.setNegativeButton("Volver al menú", (dialog, which) -> {
            pararDescansoVista();
            finish();
        });
        dlg.show();
    }
}
