package com.example.weatherforecast.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Model class representing a geographic point along a route with weather information
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

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public int getPercentageAlongRoute() {
        return percentageAlongRoute;
    }

    public void setPercentageAlongRoute(int percentageAlongRoute) {
        this.percentageAlongRoute = percentageAlongRoute;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }
}