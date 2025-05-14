package com.example.weatherforecast.ui.route;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.weatherforecast.R;
import com.example.weatherforecast.model.RoutePoint;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for all map-related operations
 */
public class MapManager {

    private GoogleMap googleMap;
    private Context context;

    public MapManager() {
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // Set default view to Spain with lower zoom level for performance
        LatLng spain = new LatLng(40.4637, -3.7492);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(spain, 5));

        // Optimize map for performance
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
    }

    public void drawRoute(List<LatLng> routePoints) {
        if (googleMap == null) return;

        // Limitar puntos para mejor rendimiento
        List<LatLng> optimizedPoints = simplifyRoutePoints(routePoints, 100); // Máximo 100 puntos

        // Usar Handler para no bloquear el hilo principal
        new Handler(Looper.getMainLooper()).post(() -> {
            // Clear previous map data
            googleMap.clear();

            // Draw route polyline
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(optimizedPoints)
                    .color(context != null ?
                            context.getResources().getColor(R.color.colorPrimary) :
                            0xFF3F51B5) // Default primary color if context is null
                    .width(10);
            googleMap.addPolyline(polylineOptions);
        });
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
            // Build bounds for camera
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for (LatLng point : routePoints) {
                boundsBuilder.include(point);
            }
            LatLngBounds bounds = boundsBuilder.build();

            // Apply padding based on display metrics for better visualization
            int padding = context != null ?
                    context.getResources().getDimensionPixelSize(R.dimen.map_padding) : 100;
            if (padding <= 0) padding = 100; // Fallback padding

            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        } catch (Exception e) {
            // Fallback to moving camera to the first point
            if (!routePoints.isEmpty()) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routePoints.get(0), 10));
            }
        }
    }

    public void updateWeatherMarkers(List<RoutePoint> points) {
        if (googleMap == null) return;

        // Update markers on map
        for (RoutePoint point : points) {
            if (point.getLocationName() != null) {
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