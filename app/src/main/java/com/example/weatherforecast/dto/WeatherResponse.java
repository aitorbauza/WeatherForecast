package com.example.weatherforecast.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Clase que representa la respuesta de la API OpenWeather
 */
public class WeatherResponse {
    @SerializedName("name")
    private String cityName;

    @SerializedName("sys")
    private Sys sys;

    @SerializedName("main")
    private Main main;

    @SerializedName("weather")
    private List<Weather> weather;

    public static class Sys {
        @SerializedName("country")
        private String country;

        public String getCountry() { return country; }
    }

    public static class Main {
        @SerializedName("temp")
        private double temperature;

        @SerializedName("temp_max")
        private double maxTemperature;

        @SerializedName("temp_min")
        private double minTemperature;

        @SerializedName("humidity")
        private int humidity;

        public double getTemperature() { return temperature; }
        public double getMaxTemperature() { return maxTemperature; }
        public double getMinTemperature() { return minTemperature; }
        public int getHumidity() { return humidity; }
    }

    public static class Weather {
        @SerializedName("main")
        private String main;

        @SerializedName("description")
        private String description;

        @SerializedName("icon")
        private String icon;

        public String getMain() { return main; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }

    public String getCityName() { return cityName; }
    public Sys getSys() { return sys; }
    public Main getMain() { return main; }
    public List<Weather> getWeather() { return weather; }
}
