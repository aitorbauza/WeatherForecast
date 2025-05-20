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
 * Clase encargada de gestionar la obtención del clima para cada punto de la ruta
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

    // Método para procesar la ruta y obtener el clima
    public void processRouteWeather(List<LatLng> routePoints, double distanceInKm, String originName, String destinationName) {
        executorService.execute(() -> {
            List<RoutePoint> checkpoints = calculateWeatherCheckpoints(
                    routePoints, distanceInKm, originName, destinationName);

            fetchWeatherForCheckpoints(checkpoints);
        });
    }

    // Método para calcular los puntos de control del clima, que son: origen, medio, destino
    private List<RoutePoint> calculateWeatherCheckpoints(
            List<LatLng> routePoints, double distanceInKm, String originName, String destinationName) {

        List<RoutePoint> checkpoints = new ArrayList<>();
        int totalPoints = routePoints.size();

        if (totalPoints < 2) {
            return checkpoints;
        }

        // Origen
        RoutePoint originPoint = new RoutePoint(
                routePoints.get(0), 0, originName);
        checkpoints.add(originPoint);

        // Origen, 50%, Destino
        if (distanceInKm <= 50) {
            if (totalPoints > 2) {
                int midIndex = totalPoints / 2;
                checkpoints.add(new RoutePoint(
                        routePoints.get(midIndex), 50, "Intermedio"));
            } // Origen, 33%, 66%, Destino
        } else if (distanceInKm <= 300) {
            if (totalPoints > 3) {
                int firstThird = totalPoints / 3;
                int secondThird = 2 * firstThird;

                checkpoints.add(new RoutePoint(
                        routePoints.get(firstThird), 33, "Punto 1"));
                checkpoints.add(new RoutePoint(
                        routePoints.get(secondThird), 66, "Punto 2"));
            }
        } else {
            // Origen, 25%, 50%, 75%, Destino
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

        // Destino
        RoutePoint destinationPoint = new RoutePoint(
                routePoints.get(totalPoints - 1), 100, destinationName);
        checkpoints.add(destinationPoint);

        return checkpoints;
    }

    // Método que obtiene el clima para cada punto de control
    private void fetchWeatherForCheckpoints(List<RoutePoint> checkpoints) {

        //AtomicInteger sirve para manejar el número de peticiones en paralelo, ya que el clima puede tardar en cargar
        AtomicInteger completedRequests = new AtomicInteger(0);
        int totalCheckpoints = checkpoints.size();

        List<RoutePoint> updatedCheckpoints = new ArrayList<>(checkpoints);

        // Lo procesa en paralelo cono PoolThread para no saturar la memoria
        // PoolThread se encarga de manejar las peticiones en paralelo
        for (int i = 0; i < checkpoints.size(); i++) {
            final int index = i;
            RoutePoint point = checkpoints.get(index);

            weatherService.getWeatherForLocation(
                    point.getLatLng().latitude,
                    point.getLatLng().longitude,
                    new WeatherService.LocationWeatherCallback() {
                        @Override
                        public void onWeatherLoaded(String locationName, double temperature, String condition, String icon) {

                            // Si la respuesta no es nula, actualiza el clima
                            if (locationName != null && !locationName.isEmpty()) {
                                updatedCheckpoints.get(index).setLocationName(locationName);
                                updatedCheckpoints.get(index).setTemperature(temperature);
                                updatedCheckpoints.get(index).setWeatherCondition(condition);
                                updatedCheckpoints.get(index).setWeatherIcon(icon);
                            }

                            checkRequestsCompletion(completedRequests, totalCheckpoints, updatedCheckpoints);
                        }

                        @Override
                        public void onError(String message) {
                            checkRequestsCompletion(completedRequests, totalCheckpoints, updatedCheckpoints);
                        }
                    });
        }
    }

    // Método para verificar si todas las peticiones han terminado
    private void checkRequestsCompletion(AtomicInteger completedRequests, int totalCheckpoints, List<RoutePoint> updatedCheckpoints) {
        if (completedRequests.incrementAndGet() == totalCheckpoints) {

            // Handler con android.os.Looper.getMainLooper()).post sirve para ejecutar en el hilo principal
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                weatherPointsLiveData.setValue(new ArrayList<>(updatedCheckpoints));
            });
        }
    }

    // Método para apagar el ExecutorService
    // ExecutorService se utiliza para manejar las peticiones en paralelo
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