package com.example.weatherforecast.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.weatherforecast.R;
import com.example.weatherforecast.controller.WeatherController;
import com.example.weatherforecast.model.CurrentWeather;
import com.example.weatherforecast.model.DailyForecast;
import com.example.weatherforecast.model.HourlyForecast;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements WeatherController.WeatherView {
    private WeatherController controller;

    private ImageView backgroundGif;
    private TextView locationText;
    private TextView weatherEmoji;
    private TextView temperatureText;
    private TextView weatherConditionText;
    private TextView maxTempText;
    private TextView minTempText;
    private TextView weatherSummaryText;
    private TextView humidityText;
    private LinearLayout hourlyForecastContainer;
    private LinearLayout dailyForecastContainer;

    private ImageButton btnChangeLocation;
    private ImageButton btnSettings;
    private ImageView toolbarLogo;
    private BottomNavigationView bottomNavigation;

    private static final String DEFAULT_CITY = "Palma de Mallorca"; // Ciudad por defecto
    private String currentCity = DEFAULT_CITY; // Mantener registro de la ciudad actual

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Recuperar los datos de la ciudad enviados desde la actividad anterior
        if (getIntent().hasExtra("CITY_NAME")) {
            currentCity = getIntent().getStringExtra("CITY_NAME");
        }

        initViews();
        setupToolbar();
        setupBottomNavigation();
        bottomNavigation.setSelectedItemId(R.id.nav_weather);
        initController();
        loadWeatherData();
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
        hourlyForecastContainer = findViewById(R.id.hourlyForecastContainer);
        dailyForecastContainer = findViewById(R.id.dailyForecastContainer);

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
            Toast.makeText(MainActivity.this, "Configuración", Toast.LENGTH_SHORT).show();
            // Aquí puedes lanzar la actividad de ajustes
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_weather) {
                Toast.makeText(MainActivity.this, "Clima", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_clothing) {
                Toast.makeText(MainActivity.this, "Ropa", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, OutfitActivity.class);
                intent.putExtra("CITY_NAME", currentCity);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            return false;
        });
    }

    private void initController() {
        controller = new WeatherController(this);
        controller.setView(this);
    }

    private void loadWeatherData() {
        controller.loadWeatherData(currentCity);
    }

    @Override
    public void displayCurrentWeather(CurrentWeather weather) {
        runOnUiThread(() -> {
            // Actualizar la ciudad actual con la respuesta
            currentCity = weather.getLocation();

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
        });
    }

    @Override
    public void displayHourlyForecast(List<HourlyForecast> forecasts) {
        runOnUiThread(() -> {
            hourlyForecastContainer.removeAllViews();

            for (HourlyForecast forecast : forecasts) {
                View itemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_hourly_forecast, hourlyForecastContainer, false);

                TextView hourText = itemView.findViewById(R.id.hourText);
                TextView weatherEmojiHourly = itemView.findViewById(R.id.weatherEmojiHourly);
                TextView tempHourly = itemView.findViewById(R.id.tempHourly);

                hourText.setText(forecast.getHour());
                weatherEmojiHourly.setText(forecast.getWeatherIcon());
                tempHourly.setText(String.format("%.1f°C", forecast.getTemperature()));

                hourlyForecastContainer.addView(itemView);
            }
        });
    }

    @Override
    public void displayDailyForecast(List<DailyForecast> forecasts) {
        runOnUiThread(() -> {
            dailyForecastContainer.removeAllViews();

            for (DailyForecast forecast : forecasts) {
                View itemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_daily_forecast, dailyForecastContainer, false);

                TextView dayText = itemView.findViewById(R.id.dayText);
                TextView weatherEmojiDaily = itemView.findViewById(R.id.weatherEmojiDaily);
                TextView maxTempDaily = itemView.findViewById(R.id.maxTempDaily);
                TextView minTempDaily = itemView.findViewById(R.id.minTempDaily);

                dayText.setText(forecast.getDay());
                weatherEmojiDaily.setText(forecast.getWeatherIcon());
                maxTempDaily.setText(String.format("%.1f°C", forecast.getMaxTemperature()));
                minTempDaily.setText(String.format("%.1f°C", forecast.getMinTemperature()));

                dailyForecastContainer.addView(itemView);
            }
        });
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
                currentCity = newLocation;
                controller.loadWeatherData(newLocation);
                dialog.dismiss();
            } else {
                Toast.makeText(MainActivity.this, "Por favor, introduce una ubicación", Toast.LENGTH_SHORT).show();
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
    public void showError(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }

    @Override
    public void showLoading(boolean isLoading) {
        // Aquí podrías mostrar un indicador de carga si lo deseas
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controller != null) {
            controller.onDestroy();
        }
    }
}