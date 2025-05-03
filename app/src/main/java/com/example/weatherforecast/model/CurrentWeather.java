package com.example.weatherforecast.model;

public class CurrentWeather {
    private String location;
    private String country;
    private double temperature;
    private double maxTemperature;
    private double minTemperature;
    private String weatherCondition;
    private String weatherIcon;
    private String summary;

    public CurrentWeather(String location, String country, double temperature,
                          double maxTemperature, double minTemperature,
                          String weatherCondition, String weatherIcon, String summary) {
        this.location = location;
        this.country = country;
        this.temperature = temperature;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.weatherCondition = weatherCondition;
        this.weatherIcon = weatherIcon;
        this.summary = summary;
    }

    // Getters
    public String getLocation() { return location; }
    public String getCountry() { return country; }
    public double getTemperature() { return temperature; }
    public double getMaxTemperature() { return maxTemperature; }
    public double getMinTemperature() { return minTemperature; }
    public String getWeatherCondition() { return weatherCondition; }
    public String getWeatherIcon() { return weatherIcon; }
    public String getSummary() { return summary; }
}
