package com.example.weatherforecast.model;

/**
 * Modelo de pronóstico diario
 */
public class DailyForecast {
    private String day;
    private double maxTemperature;
    private double minTemperature;
    private String weatherIcon;

    public DailyForecast(String day, double maxTemperature, double minTemperature, String weatherIcon) {
        this.day = day;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.weatherIcon = weatherIcon;
    }

    public DailyForecast() {
    }

    // Getters y Setters
    public String getDay() { return day; }
    public double getMaxTemperature() { return maxTemperature; }
    public double getMinTemperature() { return minTemperature; }
    public String getWeatherIcon() { return weatherIcon; }

    public void setDay(String day) {
        this.day = day;
    }
    public void setMaxTemperature(double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }
    public void setMinTemperature(double minTemperature) {
        this.minTemperature = minTemperature;
    }
    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

}
