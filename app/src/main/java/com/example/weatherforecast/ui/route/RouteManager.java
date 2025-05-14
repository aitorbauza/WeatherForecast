package com.example.weatherforecast.ui.route;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for route calculation and related operations
 */
public class RouteManager {

    private static final String MAPS_API_KEY = "AIzaSyBF5Bi-0WWKdREJqpJwdFMOl1bA5y7Xbv8"; // Replace with your Google Maps API key

    private final ExecutorService executorService;
    private final GeoApiContext geoApiContext;
    private final Context context;

    public interface RouteCalculationCallback {
        void onRouteCalculated(List<LatLng> routePoints, double distanceInKm);
        void onRouteCalculationFailed(String errorMessage);
    }

    public RouteManager(Context context) {
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();

        // Initialize API context in background
        this.geoApiContext = new GeoApiContext.Builder()
                .apiKey(MAPS_API_KEY)
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .build();
    }

    public void calculateRoute(String origin, String destination, RouteCalculationCallback callback) {
        executorService.execute(() -> {
            try {
                // Añadir timeout más corto para evitar bloqueos prolongados
                DirectionsApi.newRequest(geoApiContext)
                        .mode(TravelMode.DRIVING)
                        .origin(origin)
                        .destination(destination)
                        .setCallback(new PendingResult.Callback<DirectionsResult>() {
                            @Override
                            public void onResult(DirectionsResult result) {
                                if (result.routes.length > 0) {
                                    // Limitar el procesamiento
                                    processRouteResultSimplified(result.routes[0], callback);
                                } else {
                                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                                        callback.onRouteCalculationFailed("No se encontró ninguna ruta");
                                    });
                                }
                            }

                            @Override
                            public void onFailure(Throwable e) {
                                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                                    callback.onRouteCalculationFailed(e.getMessage());
                                });
                            }
                        });

            } catch (Exception e) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    callback.onRouteCalculationFailed(e.getMessage());
                });
            }
        });
    }

    // Versión simplificada del procesador de rutas
    private void processRouteResultSimplified(DirectionsRoute route, RouteCalculationCallback callback) {
        try {
            // Simplificar extracción de puntos - limitar cantidad para mejorar rendimiento
            List<LatLng> routePoints = new ArrayList<>();
            List<com.google.maps.model.LatLng> decodedPath = route.overviewPolyline.decodePath();

            // Limitar a máximo 200 puntos para evitar sobrecarga
            int step = Math.max(1, decodedPath.size() / 200);
            for (int i = 0; i < decodedPath.size(); i += step) {
                com.google.maps.model.LatLng latLng = decodedPath.get(i);
                routePoints.add(new LatLng(latLng.lat, latLng.lng));
            }

            // Asegurarse de incluir el último punto
            if (!decodedPath.isEmpty() && step > 1) {
                com.google.maps.model.LatLng lastPoint = decodedPath.get(decodedPath.size()-1);
                routePoints.add(new LatLng(lastPoint.lat, lastPoint.lng));
            }

            // Calcular distancia
            double distanceInMeters = route.legs[0].distance.inMeters;
            double distanceInKm = distanceInMeters / 1000;

            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                callback.onRouteCalculated(routePoints, distanceInKm);
            });
        } catch (Exception e) {
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                callback.onRouteCalculationFailed(e.getMessage());
            });
        }
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            try {
                // Apaga el ejecutor de manera controlada
                executorService.shutdownNow();
            } catch (Exception e) {
                // Ignora excepciones durante el apagado
            }
        }

        if (geoApiContext != null) {
            try {
                geoApiContext.shutdown();
            } catch (Exception e) {
                // Ignora excepciones durante el apagado
            }
        }
    }
}