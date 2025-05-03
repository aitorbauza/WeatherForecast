package com.example.weatherforecast.model;

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

    // Getters
    public String getDay() { return day; }
    public double getMaxTemperature() { return maxTemperature; }
    public double getMinTemperature() { return minTemperature; }
    public String getWeatherIcon() { return weatherIcon; }
}
