package com.example.weatherforecast.model;

/**
 * Modelo de pronóstico horario
 */
public class HourlyForecast {
    private String hour;
    private double temperature;
    private String weatherIcon;

    public HourlyForecast(String hour, double temperature, String weatherIcon) {
        this.hour = hour;
        this.temperature = temperature;
        this.weatherIcon = weatherIcon;
    }

    public HourlyForecast() {
    }

    // Getters y Setters
    public String getHour() { return hour; }
    public double getTemperature() { return temperature; }
    public String getWeatherIcon() { return weatherIcon; }

    public void setHour(String hour) {
        this.hour = hour;
    }
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

}
