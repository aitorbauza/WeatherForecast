package com.example.weatherforecast.model;

/**
 * Modelo del clima actual
 */
public class CurrentWeather {
    private String location;
    private String country;
    private double temperature;
    private double maxTemperature;
    private double minTemperature;
    private String weatherCondition;
    private String weatherIcon;
    private String summary;
    private int humidity;

    public CurrentWeather(String location, String country, double temperature,
                          double maxTemperature, double minTemperature,
                          String weatherCondition, String weatherIcon, String summary, int humidity) {
        this.location = location;
        this.country = country;
        this.temperature = temperature;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.weatherCondition = weatherCondition;
        this.weatherIcon = weatherIcon;
        this.summary = summary;
        this.humidity = humidity;
    }

    public CurrentWeather() {
    }

    // Getters y Setters
    public String getLocation() {
        return location;
    }
    public String getCountry() {
        return country;
    }
    public double getTemperature() {
        return temperature;
    }
    public double getMaxTemperature() {
        return maxTemperature;}

    public double getMinTemperature() {
        return minTemperature;
    }
    public String getWeatherCondition() {
        return weatherCondition;
    }
    public String getWeatherIcon() {
        return weatherIcon;
    }
    public String getSummary() {
        return summary;
    }
    public int getHumidity() {
        return humidity;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    public void setMaxTemperature(double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }
    public void setMinTemperature(double minTemperature) {
        this.minTemperature = minTemperature;
    }
    public void setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
    }
    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }
    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

}
