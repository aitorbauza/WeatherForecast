package com.example.weatherforecast.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.weatherforecast.R;
import com.example.weatherforecast.controller.WeatherController;
import com.example.weatherforecast.model.CurrentWeather;
import com.example.weatherforecast.model.DailyForecast;
import com.example.weatherforecast.model.HourlyForecast;
import com.example.weatherforecast.model.OutfitRecommendation;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OutfitActivity extends AppCompatActivity implements WeatherController.WeatherView {

    private static final String DEFAULT_CITY = "Palma de Mallorca"; // Ciudad por defecto, igual que en MainActivity

    private TextView locationText;
    private TextView weatherEmoji;
    private TextView temperatureText;
    private TextView weatherConditionText;
    private TextView maxTempText;
    private TextView minTempText;
    private TextView weatherSummaryText;
    private TextView humidityText;
    private RadioGroup radioGroupStyle;
    private RadioButton radioSporty;
    private RadioButton radioCasual;
    private RadioButton radioFormal;
    private Button btnLoadOutfit;
    private CardView cardOutfit;
    private TextView tvOutfitRecommendation;
    private ProgressBar progressBar;

    private WeatherController weatherController;
    private OutfitViewModel outfitViewModel;
    private String cityName;

    private ImageView backgroundGif;
    private ImageButton btnChangeLocation;
    private ImageButton btnSettings;
    private ImageView toolbarLogo;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit);

        // Recuperar los datos de la ciudad enviados desde la actividad anterior
        if (getIntent().hasExtra("CITY_NAME")) {
            cityName = getIntent().getStringExtra("CITY_NAME");
        } else {
            cityName = "Palma de Mallorca"; // Ciudad por defecto si no se especifica (igual que en MainActivity)
        }

        initViews();
        initViewModel();
        setupListeners();
        loadWeatherData();
        setupToolbar();
        setupBottomNavigation();
        bottomNavigation.setSelectedItemId(R.id.nav_clothing);

    }

    private void initViews() {
        backgroundGif = findViewById(R.id.backgroundGif);
        locationText = findViewById(R.id.locationText);
        weatherEmoji = findViewById(R.id.weatherEmoji);
        temperatureText = findViewById(R.id.temperatureText);
        weatherConditionText = findViewById(R.id.weatherConditionText);
        maxTempText = findViewById(R.id.maxTempText);
        minTempText = findViewById(R.id.minTempText);
        weatherSummaryText = findViewById(R.id.weatherSummaryText);
        humidityText = findViewById(R.id.humidityText);
        radioGroupStyle = findViewById(R.id.radioGroupStyle);
        radioSporty = findViewById(R.id.radioSporty);
        radioCasual = findViewById(R.id.radioCasual);
        radioFormal = findViewById(R.id.radioFormal);
        btnLoadOutfit = findViewById(R.id.btnLoadOutfit);
        cardOutfit = findViewById(R.id.cardOutfit);
        tvOutfitRecommendation = findViewById(R.id.tvOutfitRecommendation);
        progressBar = findViewById(R.id.progressBar);

        // Botón para cambiar ubicación
        btnChangeLocation = findViewById(R.id.btnChangeLocation);
        btnChangeLocation.setOnClickListener(v -> showLocationDialog());

        // Nuevos elementos UI
        btnSettings = findViewById(R.id.btnSettings);
        toolbarLogo = findViewById(R.id.toolbarLogo);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Carga el fondo GIF
        Glide.with(this)
                .asGif()
                .load(R.drawable.cielo) // Asegúrate de tener este GIF en res/drawable
                .centerCrop()
                .into(backgroundGif);
    }

    private void initViewModel() {
        outfitViewModel = new ViewModelProvider(this).get(OutfitViewModel.class);

        outfitViewModel.getOutfitRecommendation().observe(this, recommendation -> {
            showOutfitRecommendation(recommendation);
        });

        outfitViewModel.isLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        outfitViewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Oculta el título predeterminado
        }

        // Configurar logo de la toolbar
        Glide.with(this)
                .load(R.drawable.logo) // Este recurso debe ser añadido
                .into(toolbarLogo);

        // Configurar botón de ajustes
        btnSettings.setOnClickListener(v -> {
            Toast.makeText(OutfitActivity.this, "Configuración", Toast.LENGTH_SHORT).show();
            // Aquí puedes lanzar la actividad de ajustes
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_weather) {
                Intent intent = new Intent(OutfitActivity.this, MainActivity.class);
                intent.putExtra("CITY_NAME", cityName);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_clothing) {
                // Ya estamos en la actividad de ropa
                return true;
            }
            return false;
        });
    }

    private void setupListeners() {
        radioGroupStyle.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioSporty) {
                outfitViewModel.setSelectedStyle(OutfitRecommendation.Style.SPORTY);
            } else if (checkedId == R.id.radioFormal) {
                outfitViewModel.setSelectedStyle(OutfitRecommendation.Style.FORMAL);
            } else {
                outfitViewModel.setSelectedStyle(OutfitRecommendation.Style.CASUAL);
            }
        });

        btnLoadOutfit.setOnClickListener(v -> {
            outfitViewModel.loadOutfitRecommendation();
        });
    }

    private void loadWeatherData() {
        weatherController = new WeatherController(this);
        weatherController.setView(this);
        weatherController.loadWeatherData(cityName);
    }

    private void showOutfitRecommendation(OutfitRecommendation recommendation) {
        cardOutfit.setVisibility(View.VISIBLE);
        tvOutfitRecommendation.setText(recommendation.getFormattedOutfit());
    }

    @Override
    public void displayCurrentWeather(CurrentWeather weather) {
        runOnUiThread(() -> {
            // Actualizar la ciudad actual con la respuesta
            cityName = weather.getLocation();

            // Formato para "Hoy, HH:mm"
            SimpleDateFormat dateFormat = new SimpleDateFormat("'Hoy', HH:mm", new Locale("es", "ES"));
            String currentDateTime = dateFormat.format(new Date());

            locationText.setText(String.format("%s, %s", weather.getLocation(), weather.getCountry()));
            weatherEmoji.setText(weather.getWeatherIcon());
            temperatureText.setText(String.format("%.1f°C", weather.getTemperature()));
            weatherConditionText.setText(weather.getWeatherCondition());
            maxTempText.setText(String.format("Máx: %.1f°C", weather.getMaxTemperature()));
            minTempText.setText(String.format("Mín: %.1f°C", weather.getMinTemperature()));
            humidityText.setText(String.format("%d%%", weather.getHumidity()));
            weatherSummaryText.setText(currentDateTime + " - " + weather.getSummary());

            // Actualizar el ViewModel con los datos meteorológicos
            outfitViewModel.setCurrentWeather(weather);
        });
    }

    @Override
    public void displayHourlyForecast(List<HourlyForecast> forecasts) {
        // No utilizado en esta pantalla
    }

    @Override
    public void displayDailyForecast(List<DailyForecast> forecasts) {
        // No utilizado en esta pantalla
    }

    @Override
    public void showError(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }

    @Override
    public void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void showLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_location_search, null);
        builder.setView(dialogView);

        AutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.autoCompleteLocation);
        ImageButton btnApply = dialogView.findViewById(R.id.btnApplyLocation);

        // Adaptador para el autocompletado (usamos un adaptador simple por ahora)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>());
        autoCompleteTextView.setAdapter(adapter);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Configurar el botón aplicar
        btnApply.setOnClickListener(v -> {
            String newLocation = autoCompleteTextView.getText().toString().trim();
            if (!newLocation.isEmpty()) {
                cityName = newLocation;
                weatherController.loadWeatherData(newLocation);
                dialog.dismiss();
            } else {
                Toast.makeText(OutfitActivity.this, "Por favor, introduce una ubicación", Toast.LENGTH_SHORT).show();
            }
        });

        // Configura un TextWatcher para el autocompletado
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    // Buscar sugerencias de ciudades cuando hay al menos 3 caracteres
                    searchLocationSuggestions(s.toString(), adapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Método para buscar sugerencias de ubicaciones
    private void searchLocationSuggestions(String query, ArrayAdapter<String> adapter) {
        // Aquí implementaremos la búsqueda de sugerencias
        // Por ahora, usaremos una implementación simple
        new LocationSuggestionTask(adapter).execute(query);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (weatherController != null) {
            weatherController.onDestroy();
        }
    }
}