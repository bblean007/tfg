package com.example.minitfg;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.minitfg.network.ApiClient;
import com.example.minitfg.network.models.AuthResponse;
import com.example.minitfg.network.models.RegisterRequest;
import com.example.minitfg.utils.SessionManager;
import com.example.minitfg.utils.ValidationUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etUsername, etPassword;
    private TextInputLayout tilEmail, tilUsername, tilPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private FloatingActionButton fabHelp;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sessionManager = new SessionManager(this);

        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tilEmail = findViewById(R.id.tilEmail);
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        fabHelp = findViewById(R.id.fabHelp);

        setupRealTimeValidation();

        btnRegister.setOnClickListener(v -> register());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

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
        
        tvHelpTitle.setText("Ayuda - Registro");
        tvHelpContent.setText("Crea una cuenta para guardar tu progreso:\n\n" +
                "• Email: Debe ser un formato válido (ej@mail.com).\n" +
                "• Usuario: Al menos 3 caracteres, sin espacios y alfanumérico.\n" +
                "• Contraseña: Al menos 8 caracteres, una mayúscula, una minúscula y un número.\n" +
                "• Validación: Los errores aparecerán en tiempo real bajo cada campo.");

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

    private void setupRealTimeValidation() {
        etEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString();
                if (!ValidationUtils.isValidEmail(email)) {
                    tilEmail.setError("Email inválido");
                } else {
                    tilEmail.setError(null);
                }
            }
        });

        etUsername.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String username = s.toString();
                if (!ValidationUtils.isValidUsername(username)) {
                    tilUsername.setError("Usuario inválido (mín 3 chars, alfanumérico)");
                } else {
                    tilUsername.setError(null);
                }
            }
        });

        etPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();
                if (!ValidationUtils.isValidPassword(password)) {
                    tilPassword.setError("Contraseña débil (mín 8 chars, A, a, 1)");
                } else {
                    tilPassword.setError(null);
                }
            }
        });
    }

    private void register() {
        String email = etEmail.getText().toString();
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        if (!ValidationUtils.isValidEmail(email) || 
            !ValidationUtils.isValidUsername(username) || 
            !ValidationUtils.isValidPassword(password)) {
            Toast.makeText(this, "Por favor, corrige los errores", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest request = new RegisterRequest(email, username, password);
        ApiClient.getApiService().register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    int userId = response.body().getUser().getId();
                    sessionManager.saveSession(token, email, userId);

                    Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finishAffinity();
                } else {
                    String error = "Error en el registro";
                    if (response.code() == 409) {
                        error = "Email o usuario ya registrados";
                    }
                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }
}
