package com.example.weatherforecast.ui.weather;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.weatherforecast.R;
import com.example.weatherforecast.controller.WeatherController;
import com.example.weatherforecast.model.CurrentWeather;
import com.example.weatherforecast.model.DailyForecast;
import com.example.weatherforecast.model.HourlyForecast;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class WeatherActivity extends AppCompatActivity implements WeatherController.WeatherView {
    private WeatherController controller;
    private NavigationManager navigationManager;
    private LocationDialogManager locationDialogManager;
    private WeatherDisplayComponent weatherDisplayComponent;
    private HourlyForecastComponent hourlyForecastComponent;
    private DailyForecastComponent dailyForecastComponent;

    // UI Components
    private ImageView backgroundGif;
    private ImageView toolbarLogo;
    private ImageButton btnChangeLocation;
    private ImageButton btnSettings;
    private BottomNavigationView bottomNavigation;

    private String currentCity = "Palma de Mallorca"; // Default city

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve city data from intent if available
        if (getIntent().hasExtra("CITY_NAME")) {
            currentCity = getIntent().getStringExtra("CITY_NAME");
        }

        initComponents();
        setupUI();
        loadWeatherData();
    }

    private void initComponents() {
        // Initialize UI components
        findViews();

        // Initialize managers and components
        navigationManager = new NavigationManager(this, bottomNavigation, currentCity);
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
            // Launch settings activity here
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
        controller.loadWeatherData(currentCity);
    }

    @Override
    public void displayCurrentWeather(CurrentWeather weather) {
        runOnUiThread(() -> {
            // Update current city with the response
            currentCity = weather.getLocation();
            weatherDisplayComponent.displayWeather(weather);
        });
    }

    @Override
    public void displayHourlyForecast(List<HourlyForecast> forecasts) {
        runOnUiThread(() -> hourlyForecastComponent.displayForecasts(forecasts));
    }

    @Override
    public void displayDailyForecast(List<DailyForecast> forecasts) {
        runOnUiThread(() -> dailyForecastComponent.displayForecasts(forecasts));
    }

    @Override
    public void showError(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }

    @Override
    public void showLoading(boolean isLoading) {
        // Implement loading indicator if needed
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controller != null) {
            controller.onDestroy();
        }
    }
}