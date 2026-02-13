package com.example.minitfg;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;
import com.example.minitfg.utils.SessionManager;

import com.example.minitfg.network.ApiClient;
import com.example.minitfg.network.models.SaveScoreResponse;
import com.example.minitfg.network.models.ScoreRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameActivity extends AppCompatActivity {

    TextView txtPregunta, txtPuntuacion, txtPista;
    EditText edtRespuesta;
    Button btnResponder;
    ImageButton btnHelp;
    android.widget.RelativeLayout rootLayout;

    int num1, num2;
    String operador;
    // Changed to array to support multiple valid answers
    String[] respuestasCorrectas; 
    String subject = "Matemáticas";
    String pistaActual = "";

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
        txtPista = findViewById(R.id.txtPista);
        edtRespuesta = findViewById(R.id.edtRespuesta);
        btnResponder = findViewById(R.id.btnResponder);
        txtPuntuacion = findViewById(R.id.txtPuntuacion);
        rootLayout = findViewById(R.id.rootLayout);
        btnHelp = findViewById(R.id.btnHelp);
        
        TextView txtTitulo = findViewById(R.id.txtTitulo); // Assuming I might add this, but let's just use title bar or toast
        setTitle("Juego: " + subject);

        // Configurar fondo y input type según asignatura
        if (subject.equals("Matemáticas")) {
            rootLayout.setBackgroundResource(R.drawable.fondo_mates);
            edtRespuesta.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        } else if (subject.equals("Lengua")) {
            rootLayout.setBackgroundResource(R.drawable.fondo_lengua);
            edtRespuesta.setInputType(InputType.TYPE_CLASS_TEXT);
        } else if (subject.equals("Inglés")) {
            rootLayout.setBackgroundResource(R.drawable.fondo_ingles);
            edtRespuesta.setInputType(InputType.TYPE_CLASS_TEXT);
        } else if (subject.equals("Conocimiento del Medio") || subject.equals("CM")) {
            rootLayout.setBackgroundResource(R.drawable.fondo_cm);
            edtRespuesta.setInputType(InputType.TYPE_CLASS_TEXT);
        } else {
            rootLayout.setBackgroundResource(R.drawable.fondo_cm);
            edtRespuesta.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        // Animación de entrada para el contenedor (0.3s)
        View mainContainer = findViewById(R.id.mainContainer);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(300);
        mainContainer.startAnimation(fadeIn);

        nuevaPregunta();

        btnResponder.setOnClickListener(v -> comprobarRespuesta());
        btnHelp.setOnClickListener(v -> showHelpModal());
        ImageButton btnPause = findViewById(R.id.btnPause);
        btnPause.setOnClickListener(v -> mostrarMenuPausa());

        iniciarDescansoVista();
    }

    private void showHelpModal() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_help, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        
        TextView tvHelpTitle = dialogView.findViewById(R.id.tvHelpTitle);
        TextView tvHelpContent = dialogView.findViewById(R.id.tvHelpContent);
        Button btnClose = dialogView.findViewById(R.id.btnHelpClose);
        
        tvHelpTitle.setText("Ayuda - Juego: " + subject);
        tvHelpContent.setText("¡Es hora de demostrar lo que sabes!\n\n" +
                "• Responde: Lee la pregunta y escribe tu respuesta en el cuadro.\n" +
                "• Pista: Tienes una pista arriba para ayudarte (" + pistaActual + ").\n" +
                "• Puntuación: Cada respuesta correcta suma puntos.\n" +
                "• Descanso: Cada 5 minutos te avisaremos para que descanses la vista.");

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
            case "+": 
                respuestasCorrectas = new String[]{String.valueOf(num1 + num2)};
                pistaActual = "Pista: Suma las cantidades";
                txtPista.setText(pistaActual);
                break;
            case "-": 
                respuestasCorrectas = new String[]{String.valueOf(num1 - num2)};
                pistaActual = "Pista: Resta el segundo número al primero";
                txtPista.setText(pistaActual);
                break;
            case "×": 
                respuestasCorrectas = new String[]{String.valueOf(num1 * num2)};
                pistaActual = "Pista: Multiplica los números (suma repetida)";
                txtPista.setText(pistaActual);
                break;
        }
        txtPregunta.setText(num1 + " " + operador + " " + num2);
    }

    private void generarPreguntaTexto() {
        if (subject.equals("Inglés")) {
            String[] questions = {
                "One", "Red", "Cat", "Sun", "Dog", "Blue", "Green", "Yellow", "Black", "White",
                "Apple", "Bread", "Water", "House", "Book", "Car", "Chair", "Table", "School", "Teacher",
                "Friend", "Happy", "Sad", "Big", "Small", "Fast", "Slow", "Good", "Bad", "Cold",
                "Hot", "New", "Old", "Easy", "Hard", "Day", "Night", "Boy", "Girl", "Family",
                "Father", "Mother", "Brother", "Sister", "Summer", "Winter", "Spring", "Autumn", "Tree", "Flower",
                "Bird", "Fish", "Milk", "Juice", "Rain", "Snow"
            };
            String[][] answers = {
                {"Uno"}, {"Rojo"}, {"Gato"}, {"Sol"}, {"Perro"}, {"Azul"}, {"Verde"}, {"Amarillo"}, {"Negro"}, {"Blanco"},
                {"Manzana"}, {"Pan"}, {"Agua"}, {"Casa"}, {"Libro"}, {"Coche"}, {"Silla"}, {"Mesa"}, {"Escuela", "Colegio"}, {"Profesor", "Maestra"},
                {"Amigo"}, {"Feliz", "Contento"}, {"Triste"}, {"Grande"}, {"Pequeño"}, {"Rápido"}, {"Lento"}, {"Bueno"}, {"Malo"}, {"Frío"},
                {"Caliente"}, {"Nuevo"}, {"Viejo"}, {"Fácil"}, {"Difícil"}, {"Día"}, {"Noche"}, {"Niño"}, {"Niña"}, {"Familia"},
                {"Padre"}, {"Madre"}, {"Hermano"}, {"Hermana"}, {"Verano"}, {"Invierno"}, {"Primavera"}, {"Otoño"}, {"Árbol"}, {"Flor"},
                {"Pájaro"}, {"Pez"}, {"Leche"}, {"Zumo"}, {"Lluvia"}, {"Nieve"}
            };
            int idx = (int) (Math.random() * questions.length);
            txtPregunta.setText("Traduce: " + questions[idx]);
            respuestasCorrectas = answers[idx];
            pistaActual = "Pista: Escribe la palabra en español";
            txtPista.setText(pistaActual);
            
        } else if (subject.equals("Lengua")) {
            String[] questions = {
                "Sinónimo de Feliz", "Antónimo de Alto", "Plural de Pez", "Sinónimo de Rápido", "Antónimo de Día",
                "Sinónimo de Grande", "Antónimo de Negro", "Plural de Cruz", "Sinónimo de Pequeño", "Antónimo de Frío",
                "Sinónimo de Estudiante", "Antónimo de Reír", "Plural de Luz", "Sinónimo de Caminar", "Antónimo de Cerca",
                "Sinónimo de Hablar", "Antónimo de Silencio", "Plural de Lápiz", "Sinónimo de Bonito", "Antónimo de Feo",
                "Sinónimo de Saltar", "Antónimo de Dormir", "Plural de Sofá", "Sinónimo de Casa", "Antónimo de Entrar",
                "Sinónimo de Regalo", "Antónimo de Ganar", "Plural de Actriz", "Sinónimo de Difícil", "Antónimo de Lento",
                "Sinónimo de Fácil", "Antónimo de Dulce", "Plural de Ciudad", "Sinónimo de Mentira", "Antónimo de Verdad",
                "Sinónimo de Inteligente", "Antónimo de Pobre", "Plural de Nariz", "Sinónimo de Maestro", "Antónimo de Amor",
                "Sinónimo de Escribir", "Antónimo de Encender", "Plural de Reloj", "Sinónimo de Valiente", "Antónimo de Guerra",
                "Sinónimo de Comida", "Antónimo de Abrir", "Plural de Álbum", "Sinónimo de Limpio", "Antónimo de Pesado",
                "¿'Perro' es sustantivo, adjetivo o verbo?", "¿'Correr' es sustantivo, adjetivo o verbo?", "¿'Bonito' es sustantivo, adjetivo o verbo?",
                "¿'Mesa' es sustantivo, adjetivo o verbo?", "¿'Saltar' es sustantivo, adjetivo o verbo?", "¿'Rápido' es sustantivo, adjetivo o verbo?",
                "¿'Cantar' es sustantivo, adjetivo o verbo?", "¿'Azul' es sustantivo, adjetivo o verbo?", "¿'Libro' es sustantivo, adjetivo o verbo?",
                "¿'Estudiar' es sustantivo, adjetivo o verbo?", "¿'Lámpara' es agudda, llana o esdrújula?", "¿'Café' es aguda, llana o esdrújula?",
                "¿'Árbol' es aguda, llana o esdrújula?", "¿'Teléfono' es aguda, llana o esdrújula?", "¿'Camión' es aguda, llana o esdrújula?",
                "¿'Coche' es aguda, llana o esdrújula?", "¿'Médico' es aguda, llana o esdrújula?", "¿'Pared' es aguda, llana o esdrújula?",
                "¿'Silla' es aguda, llana o esdrújula?", "¿'Música' es aguda, llana o esdrújula?", "¿'Reloj' es aguda, llana o esdrújula?",
                "¿'Azúcar' es aguda, llana o esdrújula?", "¿'Plátano' es aguda, llana o esdrújula?", "¿'Sofá' es aguda, llana o esdrújula?",
                "¿'Ventana' es aguda, llana o esdrújula?", "¿'Lápiz' es aguda, llana o esdrújula?", "¿'Pájaro' es aguda, llana o esdrújula?",
                "¿'Ratón' es aguda, llana o esdrújula?", "¿'Libreta' es aguda, llana o esdrújula?", "¿'Esdrújula' es aguda, llana o esdrújula?"
            };
            String[][] answers = {
                {"Contento", "Alegre", "Animado", "Dichoso"}, {"Bajo", "Pequeño", "Enano"}, {"Peces"}, {"Veloz", "Ligero", "Presto"}, {"Noche", "Oscuridad"},
                {"Enorme", "Gigante"}, {"Blanco"}, {"Cruces"}, {"Chico", "Diminuto"}, {"Caliente", "Calor"},
                {"Alumno", "Escolar"}, {"Llorar"}, {"Luces"}, {"Andar", "Pasear"}, {"Lejos"},
                {"Charlar", "Conversar"}, {"Ruido", "Sonido"}, {"Lápices"}, {"Hermoso", "Lindo"}, {"Guapo", "Bello"},
                {"Brincar"}, {"Despertar"}, {"Sofás"}, {"Hogar", "Vivienda"}, {"Salir"},
                {"Obsequio", "Detalle"}, {"Perder"}, {"Actrices"}, {"Complejo", "Complicado"}, {"Rápido"},
                {"Sencillo"}, {"Amargo"}, {"Ciudades"}, {"Engaño", "Falsedad"}, {"Mentira"},
                {"Listo", "Sabio"}, {"Rico"}, {"Narices"}, {"Profesor", "Docente"}, {"Odio"},
                {"Redactar"}, {"Apagar"}, {"Relojes"}, {"Osado", "Atrevido"}, {"Paz"},
                {"Alimento"}, {"Cerrar"}, {"Álbumes"}, {"Aseado", "Puro"}, {"Ligero"},
                {"Sustantivo"}, {"Verbo"}, {"Adjetivo"},
                {"Sustantivo"}, {"Verbo"}, {"Adjetivo"},
                {"Verbo"}, {"Adjetivo"}, {"Sustantivo"},
                {"Verbo"}, {"Esdrújula"}, {"Aguda"},
                {"Llana"}, {"Esdrújula"}, {"Aguda"},
                {"Llana"}, {"Esdrújula"}, {"Aguda"},
                {"Llana"}, {"Esdrújula"}, {"Aguda"},
                {"Llana"}, {"Esdrújula"}, {"Aguda"},
                {"Llana"}, {"Llana"}, {"Esdrújula"},
                {"Aguda"}, {"Llana"}, {"Esdrújula"}
            };
            int idx = (int) (Math.random() * questions.length);
            txtPregunta.setText(questions[idx]);
            respuestasCorrectas = answers[idx];
            
            if (questions[idx].startsWith("Sinónimo")) {
                pistaActual = "Pista: Una palabra con significado similar";
            } else if (questions[idx].startsWith("Antónimo")) {
                pistaActual = "Pista: Lo contrario";
            } else {
                pistaActual = "Pista: Más de uno";
            }
            txtPista.setText(pistaActual);
            
        } else {
            String[] questions = {
                "¿Cuántas patas tiene una araña?", "¿El sol es una estrella?", "¿El agua hierve a 100 grados?", "¿Cuál es el planeta rojo?",
                "¿Qué gas respiramos de los árboles?", "¿Los delfines son peces?", "¿Cuántos continentes hay?", "¿Cuál es el océano más grande?",
                "¿Qué animal es el rey de la selva?", "¿En qué país estamos?", "¿Cuántos huesos tiene un adulto?", "¿Qué órgano bombea sangre?",
                "¿Cuál es el río más largo del mundo?", "¿Quién pintó la Mona Lisa?", "¿En qué año se descubrió América?", "¿Qué planeta está más cerca del sol?",
                "¿Cuántos días tiene un año bisiesto?", "¿Cuál es el metal más caro?", "¿Qué gas necesitan las plantas para la fotosíntesis?", "¿Cuál es el satélite natural de la Tierra?",
                "¿Cuántos dientes tiene un adulto?", "¿Cuál es el país más grande del mundo?", "¿Qué idioma se habla en Brasil?", "¿Cuál es la capital de Francia?",
                "¿Qué instrumento mide la temperatura?", "¿Cuántos minutos tiene una hora?", "¿Qué animal pone el huevo más grande?", "¿Cuál es el animal más rápido del mundo?",
                "¿Qué parte de la planta está bajo tierra?", "¿Cómo se llama el movimiento de la Tierra sobre sí misma?", "¿Y alrededor del Sol?", "¿Qué sentido usamos para oler?",
                "¿Cuántos estados tiene el agua?", "¿Qué fuerza nos mantiene en el suelo?", "¿Cuál es la estrella más cercana a la Tierra?", "¿Qué animal es un anfibio?",
                "¿De qué color es la clorofila?", "¿Qué grupo de animales tiene escamas?", "¿Cómo se llaman los animales que comen carne?", "¿Y los que comen plantas?",
                "¿Qué órgano usamos para pensar?", "¿Cuántos sentidos tenemos?", "¿Cuál es el hueso más largo del cuerpo?", "¿Qué gas compone la mayor parte del aire?",
                "¿Cómo se llama el proceso de cambio de oruga a mariposa?", "¿Qué animal vive en un hormiguero?", "¿Cuál es la capital de España?", "¿Qué continente es el más frío?",
                "¿Qué inventaron los hermanos Wright?", "¿Cómo se llama la cría de la vaca?", "¿Qué animal es conocido por su memoria?"
            };
            String[][] answers = {
                {"8", "Ocho"}, {"Si", "Sí"}, {"Si", "Sí"}, {"Marte"},
                {"Oxigeno", "Oxígeno"}, {"No"}, {"6", "Seis"}, {"Pacífico"},
                {"León"}, {"España"}, {"206"}, {"Corazón"},
                {"Amazonas"}, {"Leonardo da Vinci", "Leonardo"}, {"1492"}, {"Mercurio"},
                {"366"}, {"Oro", "Platino"}, {"Dióxido de carbono", "CO2"}, {"Luna"},
                {"32"}, {"Rusia"}, {"Portugués"}, {"París"},
                {"Termómetro"}, {"60"}, {"Avestruz"}, {"Guepardo"},
                {"Raíz"}, {"Rotación"}, {"Traslación"}, {"Olfato"},
                {"3", "Tres"}, {"Gravedad"}, {"Sol"}, {"Rana", "Sapo"},
                {"Verde"}, {"Reptiles", "Peces"}, {"Carnívoros"}, {"Herbívoros"},
                {"Cerebro"}, {"5", "Cinco"}, {"Fémur"}, {"Nitrógeno"},
                {"Metamorfosis"}, {"Hormiga"}, {"Madrid"}, {"Antártida"},
                {"Avión"}, {"Ternero", "Ternera"}, {"Elefante"}
            };
            int idx = (int) (Math.random() * questions.length);
            txtPregunta.setText(questions[idx]);
            respuestasCorrectas = answers[idx];
            
            pistaActual = "Pista: Piensa en lo que has aprendido en clase";
            txtPista.setText(pistaActual);
        }
    }

    private void comprobarRespuesta() {
        String valor = edtRespuesta.getText().toString().trim();

        if (valor.isEmpty()) {
            edtRespuesta.setError("Escribe una respuesta");
            return;
        }

        boolean esCorrecta = false;
        for (String resp : respuestasCorrectas) {
            if (valor.equalsIgnoreCase(resp)) {
                esCorrecta = true;
                break;
            }
        }

        if (esCorrecta) {
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
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < respuestasCorrectas.length; i++) {
            sb.append(respuestasCorrectas[i]);
            if (i < respuestasCorrectas.length - 1) sb.append(" o ");
        }
        
        dlg.setMessage("Fallaste. La respuesta correcta era: " + sb.toString() + "\nPuntuación final: " + puntuacion);

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
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn() && puntuacion > 0) {
            String token = "Bearer " + sessionManager.getToken();
            ScoreRequest request = new ScoreRequest(subject, puntuacion);

            ApiClient.getApiService().saveScore(token, request).enqueue(new Callback<SaveScoreResponse>() {
                @Override
                public void onResponse(Call<SaveScoreResponse> call, Response<SaveScoreResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(GameActivity.this, "Puntuación guardada en la nube", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(GameActivity.this, "Error al guardar: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SaveScoreResponse> call, Throwable t) {
                    Toast.makeText(GameActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
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
