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
import com.example.weatherforecast.ui.weather.WeatherActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
//    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Inicializar la base de datos
//        dbHelper = new DBHelper(this);

        // Encontrar los elementos de la interfaz
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);

        // Configurar el botón de inicio de sesión
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Rellena los campos", Toast.LENGTH_SHORT).show();
                } else {
                    // Verificar credenciales contra la base de datos
                //boolean isValid = dbHelper.checkUser(username, password);

                    if (true) { //isValid
                        // Registrar el inicio de sesión
//                        dbHelper.recordLogin(username);

                        // Ir a la pantalla de juegos
                        Intent intent = new Intent(LoginActivity.this, WeatherActivity.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                        finish(); // Opcional: cerrar la actividad de login
                    } else {
                        Toast.makeText(LoginActivity.this, "Usuario o contraseña incorrectos",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ir a la pantalla de registro
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}