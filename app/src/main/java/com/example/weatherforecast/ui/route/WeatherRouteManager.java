package com.example.weatherforecast.ui.route;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.weatherforecast.model.RoutePoint;
import com.example.weatherforecast.service.WeatherService;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Responsible for weather-related operations along a route
 */
public class WeatherRouteManager {

    private final ExecutorService executorService;
    private final WeatherService weatherService;
    private final MutableLiveData<List<RoutePoint>> weatherPointsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final Context context;

    public WeatherRouteManager(Context context) {
        this.context = context;
        this.executorService = Executors.newFixedThreadPool(2);
        this.weatherService = new WeatherService();
    }

    public LiveData<List<RoutePoint>> getWeatherPointsLiveData() {
        return weatherPointsLiveData;
    }

    public void processRouteWeather(List<LatLng> routePoints, double distanceInKm, String originName, String destinationName) {
        executorService.execute(() -> {
            // Calculate checkpoints based on route characteristics
            List<RoutePoint> checkpoints = calculateWeatherCheckpoints(
                    routePoints, distanceInKm, originName, destinationName);

            // Fetch weather for each checkpoint in parallel
            fetchWeatherForCheckpoints(checkpoints);
        });
    }

    private List<RoutePoint> calculateWeatherCheckpoints(
            List<LatLng> routePoints, double distanceInKm, String originName, String destinationName) {

        List<RoutePoint> checkpoints = new ArrayList<>();
        int totalPoints = routePoints.size();

        // Ensure we have enough points
        if (totalPoints < 2) {
            return checkpoints;
        }

        // First point (origin)
        RoutePoint originPoint = new RoutePoint(
                routePoints.get(0), 0, originName);
        checkpoints.add(originPoint);

        // Limit the number of checkpoints based on route length
        // to avoid overwhelming the app with API calls
        if (distanceInKm <= 50) {
            // For short routes: origin, middle, destination
            if (totalPoints > 2) {
                int midIndex = totalPoints / 2;
                checkpoints.add(new RoutePoint(
                        routePoints.get(midIndex), 50, "Intermedio"));
            }
        } else if (distanceInKm <= 300) {
            // For medium routes: origin, 33%, 66%, destination
            if (totalPoints > 3) {
                int firstThird = totalPoints / 3;
                int secondThird = 2 * firstThird;

                checkpoints.add(new RoutePoint(
                        routePoints.get(firstThird), 33, "Punto 1"));
                checkpoints.add(new RoutePoint(
                        routePoints.get(secondThird), 66, "Punto 2"));
            }
        } else {
            // For long routes: origin, 25%, 50%, 75%, destination
            if (totalPoints > 4) {
                int quarter = totalPoints / 4;
                int half = totalPoints / 2;
                int threeQuarters = 3 * quarter;

                checkpoints.add(new RoutePoint(
                        routePoints.get(quarter), 25, "Punto 1"));
                checkpoints.add(new RoutePoint(
                        routePoints.get(half), 50, "Punto 2"));
                checkpoints.add(new RoutePoint(
                        routePoints.get(threeQuarters), 75, "Punto 3"));
            }
        }

        // Last point (destination)
        RoutePoint destinationPoint = new RoutePoint(
                routePoints.get(totalPoints - 1), 100, destinationName);
        checkpoints.add(destinationPoint);

        return checkpoints;
    }

    private void fetchWeatherForCheckpoints(List<RoutePoint> checkpoints) {
        // Use AtomicInteger to track completed requests
        AtomicInteger completedRequests = new AtomicInteger(0);
        int totalCheckpoints = checkpoints.size();

        // Create mutable copy for updates
        List<RoutePoint> updatedCheckpoints = new ArrayList<>(checkpoints);

        // Process each checkpoint in parallel using a thread pool
        for (int i = 0; i < checkpoints.size(); i++) {
            final int index = i;
            RoutePoint point = checkpoints.get(index);

            weatherService.getWeatherForLocation(
                    point.getLatLng().latitude,
                    point.getLatLng().longitude,
                    new WeatherService.LocationWeatherCallback() {
                        @Override
                        public void onWeatherLoaded(String locationName, double temperature, String condition, String icon) {
                            // Update point with weather data
                            if (locationName != null && !locationName.isEmpty()) {
                                updatedCheckpoints.get(index).setLocationName(locationName);
                                updatedCheckpoints.get(index).setTemperature(temperature);
                                updatedCheckpoints.get(index).setWeatherCondition(condition);
                                updatedCheckpoints.get(index).setWeatherIcon(icon);
                            }

                            // Check if all requests have completed
                            checkRequestsCompletion(completedRequests, totalCheckpoints, updatedCheckpoints);
                        }

                        @Override
                        public void onError(String message) {
                            // Handle error but still count as completed
                            checkRequestsCompletion(completedRequests, totalCheckpoints, updatedCheckpoints);
                        }
                    });
        }
    }

    private void checkRequestsCompletion(AtomicInteger completedRequests, int totalCheckpoints, List<RoutePoint> updatedCheckpoints) {
        if (completedRequests.incrementAndGet() == totalCheckpoints) {
            // Ensure UI updates happen on the main thread
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                weatherPointsLiveData.setValue(new ArrayList<>(updatedCheckpoints));
            });
        }
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            try {
                // Apaga inmediatamente para evitar fugas de memoria
                executorService.shutdownNow();
            } catch (Exception e) {
                // Ignora excepciones durante el apagado
            }
        }
    }
}