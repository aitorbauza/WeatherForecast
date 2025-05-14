package com.example.weatherforecast.ui.route;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.example.weatherforecast.R;
import com.example.weatherforecast.model.RoutePoint;
import com.example.weatherforecast.ui.weather.NavigationManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RouteWeatherActivity extends AppCompatActivity implements OnMapReadyCallback {

    // UI components
    private TextInputEditText editOrigin;
    private TextInputEditText editDestination;
    private Button btnSearchRoute;
    private TextView weatherSummaryText;
    private BottomNavigationView bottomNavigation;
    private ImageView toolbarLogo;
    private ImageButton btnSettings;

    // Managers
    private RouteManager routeManager;
    private MapManager mapManager;
    private WeatherRouteManager weatherRouteManager;
    private NavigationManager navigationManager;

    private String currentCity = "Palma de Mallorca";
    private boolean isCalculatingRoute = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_weather);

        // Inicialización básica de vistas
        initViews();

        // Posponer operaciones pesadas
        new Handler().post(() -> {
            setUpUI();
            setupMapFragment();

            // Inicializa los managers en segundo plano
            mapManager = new MapManager();
            mapManager.setContext(this);
            routeManager = new RouteManager(this);
            weatherRouteManager = new WeatherRouteManager(this);

            setupObservers();

            // Pre-fill origin city if available
            if (getIntent().hasExtra("ORIGIN_CITY")) {
                currentCity = getIntent().getStringExtra("ORIGIN_CITY");
                editOrigin.setText(currentCity);
            }
        });
    }

    private void initViews() {
        editOrigin = findViewById(R.id.editOrigin);
        editDestination = findViewById(R.id.editDestination);
        btnSearchRoute = findViewById(R.id.btnSearchRoute);
        weatherSummaryText = findViewById(R.id.weatherSummaryText);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnSettings = findViewById(R.id.btnSettings);
        toolbarLogo = findViewById(R.id.toolbarLogo);

        if (getIntent().hasExtra("ORIGIN_CITY")) {
            currentCity = getIntent().getStringExtra("ORIGIN_CITY");
            editOrigin.setText(currentCity);
        }

        navigationManager = new NavigationManager(
                this,
                bottomNavigation,
                currentCity,
                NavigationManager.ActivityType.ROUTE
        );
        navigationManager.setupBottomNavigation();

        btnSearchRoute.setOnClickListener(v -> {
            if (!isCalculatingRoute) {
                searchRoute();
            } else {
                Toast.makeText(this, "Ya se está calculando una ruta, espere por favor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Load logo using Glide with placeholder and error handling
        Glide.with(this)
                .load(R.drawable.logo)
                .into(toolbarLogo);

        btnSettings.setOnClickListener(v -> {
            Toast.makeText(RouteWeatherActivity.this, "Configuración", Toast.LENGTH_SHORT).show();
            // Launch settings activity here
        });
    }

    public void setUpUI(){
        setupToolbar();
        navigationManager.setupBottomNavigation();
    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupObservers() {
        // Observe weather points changes
        weatherRouteManager.getWeatherPointsLiveData().observe(this, routePoints -> {
            // Ya estamos en el hilo principal gracias a LiveData
            mapManager.updateWeatherMarkers(routePoints);
            updateWeatherSummaryText(routePoints);
            resetSearchState();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapManager.setGoogleMap(googleMap);

        // Asegurarse de que la navegación está correctamente inicializada
        if (navigationManager == null && currentCity != null) {
            navigationManager = new NavigationManager(
                    this,
                    bottomNavigation,
                    currentCity,
                    NavigationManager.ActivityType.ROUTE
            );
            navigationManager.setupBottomNavigation();
        }
    }

    private void searchRoute() {
        String origin = editOrigin.getText().toString().trim();
        String destination = editDestination.getText().toString().trim();

        if (origin.isEmpty() || destination.isEmpty()) {
            Toast.makeText(this, "Por favor, introduce origen y destino", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update UI to show loading
        weatherSummaryText.setText("Calculando la ruta y el clima...");
        isCalculatingRoute = true;

        // Disable search button while calculating
        btnSearchRoute.setEnabled(false);

        // Request route calculation
        routeManager.calculateRoute(origin, destination, new RouteManager.RouteCalculationCallback() {
            @Override
            public void onRouteCalculated(List<com.google.android.gms.maps.model.LatLng> routePoints, double distanceInKm) {
                // Estas operaciones ya están en el hilo principal gracias a los cambios en RouteManager
                mapManager.drawRoute(routePoints);
                mapManager.adjustCameraToRoute(routePoints);

                updateNavigationManager(origin);

                // Calculate weather for route points
                weatherRouteManager.processRouteWeather(routePoints, distanceInKm, origin, destination);
            }

            @Override
            public void onRouteCalculationFailed(String errorMessage) {
                // Ya estamos en el hilo principal gracias a los cambios en RouteManager
                Toast.makeText(RouteWeatherActivity.this,
                        "Error al calcular la ruta: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
                weatherSummaryText.setText("Error al calcular la ruta");
                resetSearchState();
            }
        });
    }

    private void updateWeatherSummaryText(List<RoutePoint> points) {
        // Filter out points without weather data
        List<RoutePoint> pointsWithWeather = new ArrayList<>();
        for (RoutePoint point : points) {
            if (point.getLocationName() != null && !point.getLocationName().isEmpty()) {
                pointsWithWeather.add(point);
            }
        }

        // Update the summary text
        if (!pointsWithWeather.isEmpty()) {
            StringBuilder weatherSummary = new StringBuilder();
            for (int i = 0; i < pointsWithWeather.size(); i++) {
                RoutePoint point = pointsWithWeather.get(i);
                weatherSummary.append(point.getLocationName())
                        .append(": ")
                        .append(String.format("%.0f°C", point.getTemperature()))
                        .append(" - ")
                        .append(point.getWeatherCondition());

                if (i < pointsWithWeather.size() - 1) {
                    weatherSummary.append("\n");
                }
            }
            weatherSummaryText.setText(weatherSummary.toString());
        }
    }

    private void updateNavigationManager(String locationName) {
        if (locationName != null && !locationName.isEmpty()) {
            currentCity = locationName;
            if (navigationManager != null) {
                navigationManager.updateCurrentCity(locationName);
            }
        }
    }

    private void resetSearchState() {
        isCalculatingRoute = false;
        btnSearchRoute.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Llama a los métodos de apagado directamente, sin crear un nuevo executor
        if (routeManager != null) {
            routeManager.shutdown();
        }

        if (weatherRouteManager != null) {
            weatherRouteManager.shutdown();
        }
    }
}