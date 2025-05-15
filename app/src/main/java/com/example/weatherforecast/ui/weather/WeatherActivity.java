package com.example.weatherforecast.ui.weather;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.weatherforecast.R;
import com.example.weatherforecast.controller.WeatherController;
import com.example.weatherforecast.model.CurrentWeather;
import com.example.weatherforecast.model.DailyForecast;
import com.example.weatherforecast.model.HourlyForecast;
import com.example.weatherforecast.model.WeatherCache;
import com.example.weatherforecast.ui.NavigationManager;
import com.example.weatherforecast.ui.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class WeatherActivity extends AppCompatActivity implements WeatherController.WeatherView {
    private WeatherController controller;
    private NavigationManager navigationManager;
    private LocationDialogManager locationDialogManager;
    private WeatherDisplayComponent weatherDisplayComponent;
    private HourlyForecastComponent hourlyForecastComponent;
    private DailyForecastComponent dailyForecastComponent;
    private WeatherCache weatherCache;

    // UI Components
    private ImageView backgroundGif;
    private ImageView toolbarLogo;
    private ImageButton btnChangeLocation;
    private ImageButton btnSettings;
    private BottomNavigationView bottomNavigation;

    private String currentCity = "Palma de Mallorca"; // Default city
    private boolean forceReload = false;
    private ProgressBar progressBar;
    private static final int CACHE_MAX_AGE_MINUTES = 30; // Cache válido por 30 minutos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // Inicializar caché
        weatherCache = new WeatherCache(this);

        // Recuperar la ciudad desde el intent o usar la última ciudad en caché
        if (getIntent().hasExtra("CITY_NAME")) {
            currentCity = getIntent().getStringExtra("CITY_NAME");
        } else {
            String cachedCity = weatherCache.getLastCity();
            if (cachedCity != null && !cachedCity.isEmpty()) {
                currentCity = cachedCity;
            }
        }

        // Verificar si debemos forzar la recarga de datos
        forceReload = NavigationManager.shouldForceReload(getIntent());

        initComponents();
        setupUI();

        // Verificar si hay datos en caché recientes
        if (!forceReload && weatherCache.hasRecentData(CACHE_MAX_AGE_MINUTES)) {
            // Cargar datos de la caché
            loadFromCache();
        } else {
            // Cargar datos frescos
            loadWeatherData();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Importante: actualiza el intent

        // Actualizar la ciudad si ha cambiado
        if (intent.hasExtra("CITY_NAME")) {
            String newCity = intent.getStringExtra("CITY_NAME");
            if (newCity != null && !currentCity.equals(newCity)) {
                currentCity = newCity;
                navigationManager.updateCurrentCity(newCity);
                loadWeatherData();
            }
        }

        // Verificar si debemos forzar la recarga
        if (NavigationManager.shouldForceReload(intent)) {
            loadWeatherData();
        }
    }

    private void initComponents() {
        // Initialize UI components
        findViews();

        // Initialize managers and components
        navigationManager = new NavigationManager(
                this,
                bottomNavigation,
                currentCity,
                NavigationManager.ActivityType.WEATHER
        );
        locationDialogManager = new LocationDialogManager(this, newLocation -> {
            currentCity = newLocation;
            loadWeatherData();
        });

        weatherDisplayComponent = new WeatherDisplayComponent(
                findViewById(R.id.locationText),
                findViewById(R.id.weatherEmoji),
                findViewById(R.id.temperatureText),
                findViewById(R.id.weatherConditionText),
                findViewById(R.id.maxTempText),
                findViewById(R.id.minTempText),
                findViewById(R.id.weatherSummaryText),
                findViewById(R.id.humidityText)
        );

        hourlyForecastComponent = new HourlyForecastComponent(
                (LinearLayout) findViewById(R.id.hourlyForecastContainer),
                this
        );

        dailyForecastComponent = new DailyForecastComponent(
                (LinearLayout) findViewById(R.id.dailyForecastContainer),
                this
        );

        // Initialize controller
        controller = new WeatherController(this);
        controller.setView(this);
    }

    private void findViews() {
        backgroundGif = findViewById(R.id.backgroundGif);
        btnChangeLocation = findViewById(R.id.btnChangeLocation);
        btnSettings = findViewById(R.id.btnSettings);
        toolbarLogo = findViewById(R.id.toolbarLogo);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupUI() {
        setupToolbar();
        setupButtonListeners();
        loadBackgroundImage();
        navigationManager.setupBottomNavigation();
    }

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

    private void setupButtonListeners() {
        btnChangeLocation.setOnClickListener(v -> locationDialogManager.showLocationDialog());

        btnSettings.setOnClickListener(v -> {
            Toast.makeText(WeatherActivity.this, "Configuración", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(WeatherActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void loadBackgroundImage() {
        Glide.with(this)
                .asGif()
                .load(R.drawable.cielo)
                .centerCrop()
                .into(backgroundGif);
    }

    private void loadWeatherData() {
        // Mostrar indicador de carga si es necesario
        showLoading(true);

        // Cargar datos del clima con una pequeña demora para permitir que la UI se actualice
        new Handler().postDelayed(() -> {
            controller.loadWeatherData(currentCity);
        }, 100);
    }

    private void loadFromCache() {
        CurrentWeather currentWeather = weatherCache.getCurrentWeather();
        List<HourlyForecast> hourlyForecasts = weatherCache.getHourlyForecast();
        List<DailyForecast> dailyForecasts = weatherCache.getDailyForecast();

        if (currentWeather != null) {
            displayCurrentWeather(currentWeather);
        }

        if (hourlyForecasts != null && !hourlyForecasts.isEmpty()) {
            displayHourlyForecast(hourlyForecasts);
        }

        if (dailyForecasts != null && !dailyForecasts.isEmpty()) {
            displayDailyForecast(dailyForecasts);
        }

        // Si la caché está muy antigua, cargar datos frescos en segundo plano
        if (!weatherCache.hasRecentData(CACHE_MAX_AGE_MINUTES / 2)) {
            new Handler().postDelayed(this::loadWeatherData, 1000);
        }
    }

    @Override
    public void displayCurrentWeather(CurrentWeather weather) {
        runOnUiThread(() -> {
            // Update current city with the response
            currentCity = weather.getLocation();
            weatherDisplayComponent.displayWeather(weather);
            // Ocultar indicador de carga
            showLoading(false);

            // Guardar en caché
            weatherCache.saveCurrentWeather(weather);
        });
    }

    @Override
    public void displayHourlyForecast(List<HourlyForecast> forecasts) {
        runOnUiThread(() -> {
            hourlyForecastComponent.displayForecasts(forecasts);

            // Guardar en caché
            weatherCache.saveHourlyForecast(forecasts);
        });
    }

    @Override
    public void displayDailyForecast(List<DailyForecast> forecasts) {
        runOnUiThread(() -> {
            dailyForecastComponent.displayForecasts(forecasts);

            // Guardar en caché
            weatherCache.saveDailyForecast(forecasts);
        });
    }

    @Override
    public void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            // Ocultar indicador de carga en caso de error
            showLoading(false);

            // En caso de error, intentar cargar desde la caché si no se hizo antes
            if (weatherCache.getCurrentWeather() != null &&
                    weatherDisplayComponent != null) {
                loadFromCache();
                Toast.makeText(this, "Mostrando datos guardados anteriormente", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void showLoading(boolean isLoading) {
        runOnUiThread(() -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos solo si se forzó la recarga al iniciar la actividad
        if (forceReload) {
            forceReload = false;
            loadWeatherData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controller != null) {
            controller.onDestroy();
        }
    }
}