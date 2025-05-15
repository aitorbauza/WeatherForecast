package com.example.weatherforecast.ui.route;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import android.os.Handler;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import com.example.weatherforecast.R;
import com.example.weatherforecast.model.RoutePoint;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
// Importar estas clases para las restricciones
import com.google.android.gms.maps.model.MapStyleOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for all map-related operations
 */
public class MapManager{

    private GoogleMap googleMap;
    private Context context;

    // Definir los límites de España como constantes
    private static final LatLng SPAIN_NORTHEAST = new LatLng(43.7902, 3.3350);  // Aproximadamente Cataluña/Francia
    private static final LatLng SPAIN_SOUTHWEST = new LatLng(36.0001, -9.3000); // Incluye Portugal y Galicia
    private static final LatLngBounds SPAIN_BOUNDS = new LatLngBounds(SPAIN_SOUTHWEST, SPAIN_NORTHEAST);

    // Zoom mínimo y máximo para España
    private static final float MIN_ZOOM_LEVEL = 5.0f;
    private static final float MAX_ZOOM_LEVEL = 15.0f;
    private static final float DEFAULT_ZOOM_LEVEL = 6.0f;

    public MapManager() {
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // Configurar la vista para España con un nivel de zoom óptimo
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.4637, -3.7492), DEFAULT_ZOOM_LEVEL));

        // Optimizar mapa para rendimiento
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        // Reducir los elementos del mapa para mejorar rendimiento
        // Esto requiere un archivo de estilo JSON en los recursos
        try {
            if (context != null) {
                boolean success = googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(context, 0));
                if (!success) {
                    // El estilo no se pudo cargar, seguir con el mapa normal
                }
            }
        } catch (Exception e) {
            // Error cargando el estilo, ignorar
        }

        // Limitar el área de visualización a España
        googleMap.setLatLngBoundsForCameraTarget(SPAIN_BOUNDS);

        // Establecer límites de zoom para evitar cargar demasiados detalles
        googleMap.setMinZoomPreference(MIN_ZOOM_LEVEL);
        googleMap.setMaxZoomPreference(MAX_ZOOM_LEVEL);

        // Desactivar algunas características del mapa para mejorar rendimiento
        googleMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        // Establecer límites de caching para evitar consumo excesivo de memoria
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(false); // Desactivar ubicación actual para ahorrar recursos
    }

    public void drawRoute(List<LatLng> routePoints) {
        if (googleMap == null) return;

        // Limitar puntos para mejor rendimiento
        List<LatLng> optimizedPoints = simplifyRoutePoints(routePoints, 50); // Reducido a 50 puntos máximo

        // Filtrar puntos fuera de España para evitar cálculos innecesarios
        optimizedPoints = filterPointsWithinSpain(optimizedPoints);

        // Usar Handler para no bloquear el hilo principal
        List<LatLng> finalOptimizedPoints = optimizedPoints;
        new Handler(Looper.getMainLooper()).post(() -> {
            // Clear previous map data
            googleMap.clear();

            // Draw route polyline
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(finalOptimizedPoints)
                    .color(context != null ?
                            context.getResources().getColor(R.color.colorPrimary) :
                            0xFF3F51B5) // Default primary color if context is null
                    .width(10);
            googleMap.addPolyline(polylineOptions);
        });
    }

    // Nuevo método para filtrar puntos solo dentro de España
    private List<LatLng> filterPointsWithinSpain(List<LatLng> points) {
        List<LatLng> filteredPoints = new ArrayList<>();

        for (LatLng point : points) {
            if (isPointInSpain(point)) {
                filteredPoints.add(point);
            }
        }

        // Si no quedan puntos después del filtrado, devolver la lista original
        return filteredPoints.isEmpty() ? points : filteredPoints;
    }

    // Comprobar si un punto está dentro de los límites de España
    private boolean isPointInSpain(LatLng point) {
        return SPAIN_BOUNDS.contains(point);
    }

    // Método para simplificar la ruta y reducir puntos
    private List<LatLng> simplifyRoutePoints(List<LatLng> points, int maxPoints) {
        if (points.size() <= maxPoints) return points;

        List<LatLng> result = new ArrayList<>();
        double step = (double) points.size() / maxPoints;

        for (int i = 0; i < maxPoints; i++) {
            int index = Math.min((int)(i * step), points.size() - 1);
            result.add(points.get(index));
        }

        // Asegurar que siempre tenemos el punto final
        if (!result.isEmpty() && !points.isEmpty() && !result.get(result.size()-1).equals(points.get(points.size()-1))) {
            result.add(points.get(points.size()-1));
        }

        return result;
    }

    public void adjustCameraToRoute(List<LatLng> routePoints) {
        if (googleMap == null || routePoints.isEmpty()) return;

        try {
            // Filtrar puntos para que estén dentro de España
            List<LatLng> filteredPoints = filterPointsWithinSpain(routePoints);

            // Si todos los puntos fueron filtrados, usar los originales
            if (filteredPoints.isEmpty()) filteredPoints = routePoints;

            // Build bounds for camera
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for (LatLng point : filteredPoints) {
                boundsBuilder.include(point);
            }

            // Asegurar que los límites estén dentro de España
            LatLngBounds routeBounds = boundsBuilder.build();
            LatLngBounds constrainedBounds = constrainBoundsToSpain(routeBounds);

            // Apply padding based on display metrics for better visualization
            int padding = context != null ?
                    context.getResources().getDimensionPixelSize(R.dimen.map_padding) : 100;
            if (padding <= 0) padding = 100; // Fallback padding

            // Animar la cámara de forma rápida para evitar bloqueos
            googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(constrainedBounds, padding),
                    300, // Duración corta de la animación (300ms)
                    null
            );
        } catch (Exception e) {
            // Fallback to moving camera to first point in Spain or center of Spain
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.4637, -3.7492), DEFAULT_ZOOM_LEVEL));
        }
    }

    // Método para asegurar que los límites están dentro de España
    private LatLngBounds constrainBoundsToSpain(LatLngBounds bounds) {
        LatLng northeast = bounds.northeast;
        LatLng southwest = bounds.southwest;

        // Limitar las coordenadas a los límites de España
        northeast = new LatLng(
                Math.min(northeast.latitude, SPAIN_NORTHEAST.latitude),
                Math.min(northeast.longitude, SPAIN_NORTHEAST.longitude)
        );

        southwest = new LatLng(
                Math.max(southwest.latitude, SPAIN_SOUTHWEST.latitude),
                Math.max(southwest.longitude, SPAIN_SOUTHWEST.longitude)
        );

        return new LatLngBounds(southwest, northeast);
    }

    public void updateWeatherMarkers(List<RoutePoint> points) {
        if (googleMap == null) return;

        // Limitar el número de marcadores para mejorar rendimiento
        List<RoutePoint> limitedPoints = new ArrayList<>();
        int maxMarkers = 5; // Limitar a 5 marcadores como máximo

        if (points.size() <= maxMarkers) {
            limitedPoints = points;
        } else {
            // Estrategia: tomar el primero, último y algunos intermedios
            limitedPoints.add(points.get(0)); // Origen

            // Algunos puntos intermedios
            for (int i = 1; i < points.size() - 1; i += points.size() / maxMarkers) {
                if (limitedPoints.size() < maxMarkers - 1) {
                    limitedPoints.add(points.get(i));
                }
            }

            // Último punto (destino)
            if (points.size() > 1) {
                limitedPoints.add(points.get(points.size() - 1));
            }
        }

        // Update markers on map
        for (RoutePoint point : limitedPoints) {
            if (point.getLocationName() != null && isPointInSpain(point.getLatLng())) {
                addWeatherMarker(point);
            }
        }
    }

    private void addWeatherMarker(RoutePoint point) {
        if (googleMap == null || point.getLocationName() == null) return;

        // Create marker for this weather point
        MarkerOptions markerOptions = new MarkerOptions()
                .position(point.getLatLng())
                .title(point.getLocationName())
                .snippet(String.format("%.1f°C - %s", point.getTemperature(), point.getWeatherCondition()));

        // Choose color based on temperature
        if (point.getTemperature() > 25) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        } else if (point.getTemperature() > 15) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        } else {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }

        googleMap.addMarker(markerOptions);
    }
}