package com.example.weatherforecast.util;

import java.util.HashMap;
import java.util.Map;

public class WeatherIconMapper {
    private final Map<String, String> iconToEmojiMap;

    public WeatherIconMapper() {
        iconToEmojiMap = new HashMap<>();

        // Día claro
        iconToEmojiMap.put("01d", "☀️");
        // Noche clara
        iconToEmojiMap.put("01n", "🌙");
        // Parcialmente nublado día
        iconToEmojiMap.put("02d", "⛅");
        // Parcialmente nublado noche
        iconToEmojiMap.put("02n", "☁️");
        // Nublado
        iconToEmojiMap.put("03d", "☁️");
        iconToEmojiMap.put("03n", "☁️");
        // Muy nublado
        iconToEmojiMap.put("04d", "☁️");
        iconToEmojiMap.put("04n", "☁️");
        // Lluvia ligera
        iconToEmojiMap.put("09d", "🌧️");
        iconToEmojiMap.put("09n", "🌧️");
        // Lluvia
        iconToEmojiMap.put("10d", "🌧️");
        iconToEmojiMap.put("10n", "🌧️");
        // Tormenta
        iconToEmojiMap.put("11d", "⛈️");
        iconToEmojiMap.put("11n", "⛈️");
        // Nieve
        iconToEmojiMap.put("13d", "❄️");
        iconToEmojiMap.put("13n", "❄️");
        // Niebla
        iconToEmojiMap.put("50d", "🌫️");
        iconToEmojiMap.put("50n", "🌫️");
    }

    public String getEmojiFromIconCode(String iconCode) {
        if (iconCode == null || iconCode.isEmpty()) {
            return "❓"; // Emoji por defecto si no hay código
        }

        return iconToEmojiMap.getOrDefault(iconCode, "❓");
    }
}