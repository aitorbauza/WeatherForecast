package com.example.weatherforecast.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Clase que representa la respuesta de la API OpenWeatherMap
 */
public class ForecastResponse {
    // @SerializedName indica que el nombre del campo en la respuesta JSON
    @SerializedName("list")
    private List<TimePoint> list;

    public static class TimePoint {
        @SerializedName("dt")
        private long timestamp;

        @SerializedName("dt_txt")
        private String dateTimeText;

        @SerializedName("main")
        private Main main;

        @SerializedName("weather")
        private List<Weather> weather;

        public long getTimestamp() { return timestamp; }
        public String getDateTimeText() { return dateTimeText; }
        public Main getMain() { return main; }
        public List<Weather> getWeather() { return weather; }
    }

    public static class Main {
        @SerializedName("temp")
        private double temperature;

        @SerializedName("temp_max")
        private double maxTemperature;

        @SerializedName("temp_min")
        private double minTemperature;

        public double getTemperature() { return temperature; }
        public double getMaxTemperature() { return maxTemperature; }
        public double getMinTemperature() { return minTemperature; }
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

    public List<TimePoint> getList() { return list; }
}