package com.example.weatherforecast.ui.forms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherforecast.R;
import com.example.weatherforecast.data.DBHelper;

/**
 * Clase encargada de manejar el registro de usuarios
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText editTextRegUsername;
    private EditText editTextRegPassword;
    private EditText editTextConfirmPassword;
    private Button buttonRegister;
    private TextView textViewLogin;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        // Inicializa la base de datos
        dbHelper = new DBHelper(this);

        editTextRegUsername = findViewById(R.id.editTextRegUsername);
        editTextRegPassword = findViewById(R.id.editTextRegPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextRegUsername.getText().toString().trim();
                String password = editTextRegPassword.getText().toString().trim();
                String confirmPassword = editTextConfirmPassword.getText().toString().trim();

                // Validaciones básicas
                if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Por favor, completa todos los campos",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Las contraseñas no coinciden",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Intenta registrar el usuario en la base de datos
                boolean success = dbHelper.registerUser(username, password);

                if (success) {
                    Toast.makeText(RegisterActivity.this, "Registro exitoso",
                            Toast.LENGTH_SHORT).show();
                    // Regresar a la pantalla de login
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "El nombre de usuario ya existe",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ir a la pantalla de inicio de sesión
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}