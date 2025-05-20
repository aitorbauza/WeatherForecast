package com.example.weatherforecast.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.weatherforecast.R;
import com.example.weatherforecast.model.UserPreferences;
import com.example.weatherforecast.repository.PreferencesRepository;
import com.example.weatherforecast.ui.forms.LoginActivity;
import com.example.weatherforecast.ui.outfit.OutfitActivity;
import com.example.weatherforecast.ui.weather.WeatherActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Actividad para la pantalla de ajustes donde el usuario puede configurar sus preferencias.
 */
public class SettingsActivity extends AppCompatActivity {

    private TextInputEditText nameEditText;
    private TextInputEditText surnameEditText;
    private RadioGroup genderRadioGroup;
    private RadioGroup coldToleranceRadioGroup;
    private RadioGroup heatToleranceRadioGroup;
    private MaterialButton saveButton;
    private MaterialButton resetButton;
    private ImageButton homeButton;
    private ImageView toolbarLogo;
    private Button viewOutfitButton;

    private PreferencesRepository preferencesRepository;
    private UserPreferences currentPreferences;
    private UserPreferences originalPreferences;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra("username")) {
            username = getIntent().getStringExtra("username");
        } else {
            // Si no hay usuario, volver a la pantalla de login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_settings);

        initRepository();
        initViews();
        loadPreferences();
        setupListeners();
        setupToolbar();
    }

    /**
     * Inicializa el repositorio de preferencias.
     */
    private void initRepository() {
        preferencesRepository = new PreferencesRepository(this, username);
    }

    /**
     * Inicializa las referencias a las vistas.
     */
    private void initViews() {
        nameEditText = findViewById(R.id.nameEditText);
        surnameEditText = findViewById(R.id.surnameEditText);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        coldToleranceRadioGroup = findViewById(R.id.coldToleranceRadioGroup);
        heatToleranceRadioGroup = findViewById(R.id.heatToleranceRadioGroup);
        saveButton = findViewById(R.id.saveButton);
        resetButton = findViewById(R.id.resetButton);
        homeButton = findViewById(R.id.btnHome);
        toolbarLogo = findViewById(R.id.toolbarLogo);
        viewOutfitButton = findViewById(R.id.viewOutfitButton);
    }

    /**
     * Inicializa la barra de herramientas.
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        Glide.with(this)
                .load(R.drawable.logo)
                .into(toolbarLogo);
    }

    /**
     * Carga las preferencias del usuario desde el repositorio.
     */
    private void loadPreferences() {
        // Get user preferences from DB with username
        originalPreferences = preferencesRepository.getUserPreferences(username);
        currentPreferences = originalPreferences.copy();

        // Update UI
        updateUIFromPreferences();
    }

    /**
     * Actualiza la interfaz con las preferencias cargadas.
     */
    private void updateUIFromPreferences() {
        nameEditText.setText(currentPreferences.getName());
        surnameEditText.setText(currentPreferences.getSurname());

        // Establecer género
        int genderId;
        switch (currentPreferences.getGender()) {
            case MALE:
                genderId = R.id.maleRadioButton;
                break;
            case FEMALE:
                genderId = R.id.femaleRadioButton;
                break;
            default:
                genderId = R.id.otherRadioButton;
                break;
        }
        genderRadioGroup.check(genderId);

        // Establecer tolerancia al frío
        int coldToleranceId;
        switch (currentPreferences.getColdTolerance()) {
            case LOW:
                coldToleranceId = R.id.lowColdRadioButton;
                break;
            case HIGH:
                coldToleranceId = R.id.highColdRadioButton;
                break;
            default:
                coldToleranceId = R.id.normalColdRadioButton;
                break;
        }
        coldToleranceRadioGroup.check(coldToleranceId);

        // Establecer tolerancia al calor
        int heatToleranceId;
        switch (currentPreferences.getHeatTolerance()) {
            case LOW:
                heatToleranceId = R.id.lowHeatRadioButton;
                break;
            case HIGH:
                heatToleranceId = R.id.highHeatRadioButton;
                break;
            default:
                heatToleranceId = R.id.normalHeatRadioButton;
                break;
        }
        heatToleranceRadioGroup.check(heatToleranceId);

        // Actualizar estado del botón de guardar
        updateSaveButtonState();
    }

    /**
     * Configura los listeners para los componentes de la UI.
     */
    private void setupListeners() {
        // TextWatcher para los campos de texto
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se necesita implementación
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No se necesita implementación
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Actualizar las preferencias actuales
                currentPreferences.setName(nameEditText.getText().toString().trim());
                currentPreferences.setSurname(surnameEditText.getText().toString().trim());

                // Actualizar el estado del botón de guardar
                updateSaveButtonState();
            }
        };

        nameEditText.addTextChangedListener(textWatcher);
        surnameEditText.addTextChangedListener(textWatcher);

        // Listener para el RadioGroup de género
        genderRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            UserPreferences.Gender gender;

            if (checkedId == R.id.maleRadioButton) {
                gender = UserPreferences.Gender.MALE;
            } else if (checkedId == R.id.femaleRadioButton) {
                gender = UserPreferences.Gender.FEMALE;
            } else {
                gender = UserPreferences.Gender.OTHER;
            }

            currentPreferences.setGender(gender);
            updateSaveButtonState();
        });

        // Listener para el RadioGroup de tolerancia al frío
        coldToleranceRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            UserPreferences.Tolerance tolerance;

            if (checkedId == R.id.lowColdRadioButton) {
                tolerance = UserPreferences.Tolerance.LOW;
            } else if (checkedId == R.id.highColdRadioButton) {
                tolerance = UserPreferences.Tolerance.HIGH;
            } else {
                tolerance = UserPreferences.Tolerance.NORMAL;
            }

            currentPreferences.setColdTolerance(tolerance);
            updateSaveButtonState();
        });

        // Listener para el RadioGroup de tolerancia al calor
        heatToleranceRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            UserPreferences.Tolerance tolerance;

            if (checkedId == R.id.lowHeatRadioButton) {
                tolerance = UserPreferences.Tolerance.LOW;
            } else if (checkedId == R.id.highHeatRadioButton) {
                tolerance = UserPreferences.Tolerance.HIGH;
            } else {
                tolerance = UserPreferences.Tolerance.NORMAL;
            }

            currentPreferences.setHeatTolerance(tolerance);
            updateSaveButtonState();
        });

        viewOutfitButton.setOnClickListener(v -> goToOutfitScreen());

        // Listener para el botón de guardar
        saveButton.setOnClickListener(v -> savePreferences());

        // Listener para el botón de restablecer
        resetButton.setOnClickListener(v -> resetPreferences());

        // Listener para el botón de inicio
        homeButton.setOnClickListener(v -> navigateToHome());
    }

    /**
     * Actualiza el estado del botón de guardar según si hay cambios.
     */
    private void updateSaveButtonState() {
        boolean hasChanges = !currentPreferences.equals(originalPreferences);
        saveButton.setEnabled(hasChanges);
    }

    /**
     * Guarda las preferencias actuales.
     */
    private void savePreferences() {
        preferencesRepository.saveUserPreferences(currentPreferences, username);
        originalPreferences = currentPreferences.copy();
        updateSaveButtonState();
        Toast.makeText(this, R.string.settings_saved_message, Toast.LENGTH_SHORT).show();
    }

    private void goToOutfitScreen() {
        Intent intent = new Intent(this, OutfitActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    /**
     * Restablece las preferencias a los valores predeterminados.
     */
    private void resetPreferences() {
        // Crear una nueva instancia de preferencias predeterminadas
        currentPreferences = new UserPreferences();

        nameEditText.setText("Nombre");
        surnameEditText.setText("¡Introduce tus apellidos!");

        // Actualizar la UI con los valores restablecidos
        updateUIFromPreferences();
        Toast.makeText(this, R.string.settings_reset_message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Navega a la pantalla principal.
     */
    private void navigateToHome() {
        Intent intent = new Intent(this, WeatherActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}