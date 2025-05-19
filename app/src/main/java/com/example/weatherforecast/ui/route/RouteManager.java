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
import com.google.maps.model.Bounds;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Clase encargada de calcular la ruta entre dos puntos
 */
public class RouteManager {

    private static final String MAPS_API_KEY = "AIzaSyBF5Bi-0WWKdREJqpJwdFMOl1bA5y7Xbv8"; // Google Maps API key

    // Definir los límites de España como constantes
    private static final com.google.maps.model.LatLng SPAIN_NORTHEAST = new com.google.maps.model.LatLng(43.7902, 3.3350);
    private static final com.google.maps.model.LatLng SPAIN_SOUTHWEST = new com.google.maps.model.LatLng(36.0001, -9.3000);
    private static final String SPAIN_REGION = "es";

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

        this.geoApiContext = new GeoApiContext.Builder()
                .apiKey(MAPS_API_KEY)
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .build();
    }

    // Método para calcular la ruta entre dos puntos
    public void calculateRoute(String origin, String destination, RouteCalculationCallback callback) {
        executorService.execute(() -> {
            try {
                // Validar que el origen y destino son lugares en España
                if (!validateSpainLocation(origin, destination)) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        callback.onRouteCalculationFailed("La ruta debe estar dentro de España");
                    });
                    return;
                }

                // Configurar la solicitud de ruta con restricciones para España
                DirectionsApi.newRequest(geoApiContext)
                        .mode(TravelMode.DRIVING)
                        .origin(origin)
                        .destination(destination)
                        .region(SPAIN_REGION) // Limitar a España
                        .alternatives(false) // Desactivar rutas alternativas para mejor rendimiento
                        .optimizeWaypoints(true)
                        .setCallback(new PendingResult.Callback<DirectionsResult>() {
                            @Override
                            public void onResult(DirectionsResult result) {
                                if (result.routes.length > 0) {
                                    // Verificar que la ruta está en España antes de procesarla
                                    if (isRouteInSpain(result.routes[0])) {
                                        processRouteResultSimplified(result.routes[0], callback);
                                    } else {
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            callback.onRouteCalculationFailed("La ruta está fuera de España");
                                        });
                                    }
                                } else {
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        callback.onRouteCalculationFailed("No se encontró ninguna ruta");
                                    });
                                }
                            }

                            @Override
                            public void onFailure(Throwable e) {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    callback.onRouteCalculationFailed(e.getMessage());
                                });
                            }
                        });

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onRouteCalculationFailed(e.getMessage());
                });
            }
        });
    }

    // Método para validar que las ubicaciones están en España
    private boolean validateSpainLocation(String origin, String destination) {

        String lowerOrigin = origin.toLowerCase();
        String lowerDest = destination.toLowerCase();

        // Lista de ciudades principales de España para optimizar la validación
        String[] spanishCities = {"madrid", "barcelona", "valencia", "sevilla", "zaragoza",
                "málaga", "murcia", "palma", "bilbao", "alicante",
                "córdoba", "valladolid", "vigo", "gijón", "españa", "spain"};

        boolean originInSpain = false;
        boolean destInSpain = false;

        // Verificar si el origen es una ciudad española
        for (String city : spanishCities) {
            if (lowerOrigin.contains(city)) {
                originInSpain = true;
                break;
            }
        }

        // Verificar si el destino es una ciudad española
        for (String city : spanishCities) {
            if (lowerDest.contains(city)) {
                destInSpain = true;
                break;
            }
        }

        // Aceptar la ruta si ambos puntos parecen estar en España
        return originInSpain && destInSpain;
    }

    // Verificar si la ruta calculada está dentro de España
    private boolean isRouteInSpain(DirectionsRoute route) {
        Bounds bounds = route.bounds;

        boolean isNortheastInSpain = bounds.northeast.lat <= SPAIN_NORTHEAST.lat &&
                bounds.northeast.lng <= SPAIN_NORTHEAST.lng;
        boolean isSouthwestInSpain = bounds.southwest.lat >= SPAIN_SOUTHWEST.lat &&
                bounds.southwest.lng >= SPAIN_SOUTHWEST.lng;

        return isNortheastInSpain && isSouthwestInSpain;
    }

    // Método para procesar el resultado de la ruta simplificada
    private void processRouteResultSimplified(DirectionsRoute route, RouteCalculationCallback callback) {
        try {
            List<LatLng> routePoints = new ArrayList<>();
            List<com.google.maps.model.LatLng> decodedPath = route.overviewPolyline.decodePath();

            // Limitar a máximo 100 puntos para evitar sobrecarga
            int step = Math.max(1, decodedPath.size() / 100);
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

            new Handler(Looper.getMainLooper()).post(() -> {
                callback.onRouteCalculated(routePoints, distanceInKm);
            });
        } catch (Exception e) {
            new Handler(Looper.getMainLooper()).post(() -> {
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
            }
        }

        if (geoApiContext != null) {
            try {
                geoApiContext.shutdown();
            } catch (Exception e) {
            }
        }
    }

}