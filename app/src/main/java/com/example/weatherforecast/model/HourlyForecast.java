package com.example.weatherforecast.model;

public class HourlyForecast {
    private String hour;
    private double temperature;
    private String weatherIcon;

    public HourlyForecast(String hour, double temperature, String weatherIcon) {
        this.hour = hour;
        this.temperature = temperature;
        this.weatherIcon = weatherIcon;
    }

    // Getters
    public String getHour() { return hour; }
    public double getTemperature() { return temperature; }
    public String getWeatherIcon() { return weatherIcon; }
}
