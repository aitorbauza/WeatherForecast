package com.example.weatherforecast.ui.route;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.weatherforecast.R;
import com.example.weatherforecast.model.RoutePoint;
import com.example.weatherforecast.service.WeatherService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RouteWeatherActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String MAPS_API_KEY = "AIzaSyBF5Bi-0WWKdREJqpJwdFMOl1bA5y7Xbv8"; // Replace with your Google Maps API key
    private List<RoutePoint> weatherCheckpoints = new ArrayList<>();
    private GoogleMap mMap;
    private GeoApiContext geoApiContext;
    private TextInputEditText editOrigin;
    private TextInputEditText editDestination;
    private Button btnSearchRoute;
    private TextView weatherSummaryText;
    private WeatherService weatherService;
    private BottomNavigationView bottomNavigation;
    private ImageView toolbarLogo;
    private ImageButton btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_weather);

        // Inicializa primero componentes ligeros
        initViews();
        setupToolbar();
        setupBottomNavigation();
        setupButtonListeners();

        // Prefill origin city (operación ligera)
        if (getIntent().hasExtra("ORIGIN_CITY")) {
            String originCity = getIntent().getStringExtra("ORIGIN_CITY");
            editOrigin.setText(originCity);
        }

        // Inicializa componentes pesados de manera asíncrona
        new Thread(() -> {
            initGeoApiContext();
            weatherService = new WeatherService();

            // Configurar mapa asíncrono (ya se está haciendo con getMapAsync)
            runOnUiThread(this::setupMapFragment);
        }).start();
    }

    private void initGeoApiContext() {
        geoApiContext = new GeoApiContext.Builder()
                .apiKey(MAPS_API_KEY)
                .connectTimeout(2, TimeUnit.SECONDS) // Reduce from 2 to 1
                .readTimeout(2, TimeUnit.SECONDS)    // Reduce from 2 to 1
                .writeTimeout(2, TimeUnit.SECONDS)   // Reduce from 2 to 1
                .build();
    }

    private void initViews() {
        editOrigin = findViewById(R.id.editOrigin);
        editDestination = findViewById(R.id.editDestination);
        btnSearchRoute = findViewById(R.id.btnSearchRoute);
        weatherSummaryText = findViewById(R.id.weatherSummaryText);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        btnSettings = findViewById(R.id.btnSettings);
        toolbarLogo = findViewById(R.id.toolbarLogo);

        btnSearchRoute.setOnClickListener(v -> searchRoute());
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
        btnSettings.setOnClickListener(v -> {
            Toast.makeText(RouteWeatherActivity.this, "Configuración", Toast.LENGTH_SHORT).show();
            // Launch settings activity here
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_route);
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            // Handle navigation selection
            return true;
        });
    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Set default view to Spain
        LatLng spain = new LatLng(40.4637, -3.7492);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(spain, 6));
    }

    private void searchRoute() {
        String origin = editOrigin.getText().toString().trim();
        String destination = editDestination.getText().toString().trim();

        if (origin.isEmpty() || destination.isEmpty()) {
            Toast.makeText(this, "Por favor, introduce origen y destino", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading indicator if needed
        weatherSummaryText.setText("Calculando la ruta y el clima...");

        calculateRoute(origin, destination);
    }

    private void calculateRoute(String origin, String destination) {
        new AsyncTask<Void, Void, DirectionsResult>() {
            @Override
            protected void onPreExecute() {
                weatherSummaryText.setText("Calculando la ruta y el clima...");
            }

            @Override
            protected DirectionsResult doInBackground(Void... voids) {
                try {
                    return DirectionsApi.newRequest(geoApiContext)
                            .mode(TravelMode.DRIVING)
                            .origin(origin)
                            .destination(destination)
                            .await(); // Usa await en lugar de callback
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(DirectionsResult result) {
                if (result != null && result.routes.length > 0) {
                    displayRoute(result.routes[0], origin, destination);
                } else {
                    Toast.makeText(RouteWeatherActivity.this,
                            "No se encontró ninguna ruta", Toast.LENGTH_SHORT).show();
                    weatherSummaryText.setText("No se pudo encontrar una ruta entre estos puntos");
                }
            }
        }.execute();
    }

    private void displayRoute(DirectionsRoute route, String originName, String destinationName) {
        if (mMap == null) return;

        // Operaciones pesadas en un hilo separado
        new Thread(() -> {
            // Clear previous data
            weatherCheckpoints.clear();

            // Extract route points
            List<LatLng> routePoints = new ArrayList<>();
            for (com.google.maps.model.LatLng latLng : route.overviewPolyline.decodePath()) {
                routePoints.add(new LatLng(latLng.lat, latLng.lng));
            }

            // Calculate distance
            double distanceInMeters = route.legs[0].distance.inMeters;
            double distanceInKm = distanceInMeters / 1000;

            // Determinar puntos de clima
            final List<RoutePoint> checkpoints = calculateWeatherCheckpoints(
                    routePoints, distanceInKm, originName, destinationName);

            // UI updates en el hilo principal
            runOnUiThread(() -> {
                // Clear previous markers and routes
                mMap.clear();
                weatherCheckpoints.clear();
                weatherCheckpoints.addAll(checkpoints);

                // Draw route line
                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(routePoints)
                        .color(getResources().getColor(R.color.colorPrimary))
                        .width(10);
                mMap.addPolyline(polylineOptions);

                // Adjust camera
                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                for (LatLng point : routePoints) {
                    boundsBuilder.include(point);
                }
                LatLngBounds bounds = boundsBuilder.build();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

                // Fetch weather data with a small delay
                fetchWeatherDataWithDelay();
            });
        }).start();
    }

    private void fetchWeatherDataWithDelay() {
        // Usar un Handler para escalonar las solicitudes
        Handler handler = new Handler();
        for (int i = 0; i < weatherCheckpoints.size(); i++) {
            final RoutePoint point = weatherCheckpoints.get(i);
            handler.postDelayed(() -> fetchWeatherForPoint(point), i * 300); // 300ms entre cada solicitud
        }
    }

    private List<RoutePoint> calculateWeatherCheckpoints(
            List<LatLng> routePoints, double distanceInKm, String originName, String destinationName) {

        List<RoutePoint> checkpoints = new ArrayList<>();
        int totalPoints = routePoints.size();

        // First point (origin)
        RoutePoint originPoint = new RoutePoint(
                routePoints.get(0), 0, originName);
        checkpoints.add(originPoint);

        // Intermediate points
        if (distanceInKm <= 50) {
            // For routes < 50km: origin, 50%, destination
            int midIndex = totalPoints / 2;
            checkpoints.add(new RoutePoint(
                    routePoints.get(midIndex), 50, "Intermedio"));
        } else if (distanceInKm <= 300) {
            // For routes 50-300km: origin, 33%, 66%, destination
            int firstThird = totalPoints / 3;
            int secondThird = 2 * totalPoints / 3;

            checkpoints.add(new RoutePoint(
                    routePoints.get(firstThird), 33, "Punto 1"));
            checkpoints.add(new RoutePoint(
                    routePoints.get(secondThird), 66, "Punto 2"));
        } else {
            // For routes > 300km: origin, 25%, 50%, 75%, destination
            int quarter = totalPoints / 4;
            int half = totalPoints / 2;
            int threeQuarters = 3 * totalPoints / 4;

            checkpoints.add(new RoutePoint(
                    routePoints.get(quarter), 25, "Punto 1"));
            checkpoints.add(new RoutePoint(
                    routePoints.get(half), 50, "Punto 2"));
            checkpoints.add(new RoutePoint(
                    routePoints.get(threeQuarters), 75, "Punto 3"));
        }

        // Last point (destination)
        RoutePoint destinationPoint = new RoutePoint(
                routePoints.get(totalPoints - 1), 100, destinationName);
        checkpoints.add(destinationPoint);

        return checkpoints;
    }

    private void fetchWeatherForPoint(RoutePoint point) {
        new Handler().postDelayed(() -> {
            weatherService.getWeatherForLocation(
                    point.getLatLng().latitude,
                    point.getLatLng().longitude,
                    new WeatherService.LocationWeatherCallback() {
                        @Override
                        public void onWeatherLoaded(String locationName, double temperature, String condition, String icon) {
                            runOnUiThread(() -> {
                                // Store weather information in the point
                                point.setLocationName(locationName);
                                point.setTemperature(temperature);
                                point.setWeatherCondition(condition);
                                point.setWeatherIcon(icon);

                                // Add weather marker to map
                                addWeatherMarker(point);

                                // Update weather text view
                                updateWeatherSummaryText();
                            });
                        }

                        @Override
                        public void onError(String message) {
                            runOnUiThread(() -> {
                                Toast.makeText(RouteWeatherActivity.this,
                                        "Error al obtener el clima: " + message,
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        }, 200 * weatherCheckpoints.indexOf(point));
    }

    private void addWeatherMarker(RoutePoint point) {
        if (mMap == null) return;

        // Create a weather-specific marker icon
        MarkerOptions markerOptions = new MarkerOptions()
                .position(point.getLatLng())
                .title(point.getLocationName())
                .snippet(String.format("%.1f°C - %s", point.getTemperature(), point.getWeatherCondition()));

        // You might want to change the marker color based on weather or use custom weather icons
        if (point.getTemperature() > 25) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        } else if (point.getTemperature() > 15) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        } else {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }

        mMap.addMarker(markerOptions);
    }

    private void updateWeatherSummaryText() {
        // Store all route points for which we have weather data
        List<RoutePoint> pointsWithWeather = new ArrayList<>();

        // Loop through the route points and collect all those with weather data
        for (RoutePoint point : weatherCheckpoints) {
            if (point.getLocationName() != null && !point.getLocationName().isEmpty()) {
                pointsWithWeather.add(point);
            }
        }

        // If we have enough points with weather data, update the text view
        if (!pointsWithWeather.isEmpty()) {
            StringBuilder weatherSummary = new StringBuilder();

            for (int i = 0; i < pointsWithWeather.size(); i++) {
                RoutePoint point = pointsWithWeather.get(i);
                weatherSummary.append(point.getLocationName())
                        .append(": ")
                        .append(String.format("%.0f°C", point.getTemperature()))
                        .append(" - ")
                        .append(point.getWeatherCondition());

                // Add a new line between each point except for the last one
                if (i < pointsWithWeather.size() - 1) {
                    weatherSummary.append("\n");
                }
            }

            weatherSummaryText.setText(weatherSummary.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (geoApiContext != null) {
            geoApiContext.shutdown();
        }
    }
}