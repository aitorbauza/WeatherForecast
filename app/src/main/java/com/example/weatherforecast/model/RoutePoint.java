package com.example.weatherforecast.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Clase que representa un punto de la ruta
 */
public class RoutePoint {
    private LatLng latLng;
    private int percentageAlongRoute;
    private String locationName;
    private double temperature;
    private String weatherCondition;
    private String weatherIcon;

    public RoutePoint(LatLng latLng, int percentageAlongRoute, String locationName) {
        this.latLng = latLng;
        this.percentageAlongRoute = percentageAlongRoute;
        this.locationName = locationName;
    }

    //Getters y Setters
    public LatLng getLatLng() {
        return latLng;
    }
    public int getPercentageAlongRoute() {
        return percentageAlongRoute;
    }
    public String getLocationName() {
        return locationName;
    }
    public double getTemperature() {
        return temperature;
    }
    public String getWeatherCondition() {
        return weatherCondition;
    }
    public String getWeatherIcon() {
        return weatherIcon;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
    public void setPercentageAlongRoute(int percentageAlongRoute) {
        this.percentageAlongRoute = percentageAlongRoute;
    }
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    public void setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
    }
    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

}