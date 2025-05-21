package com.example.weatherforecast.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase que mapea códigos de iconos de clima a emojis correspondientes.
 */
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

        // Añadir más mappings genéricos para asegurar que todos los códigos tengan un emoji
        iconToEmojiMap.put("clear", "☀️");
        iconToEmojiMap.put("clouds", "☁️");
        iconToEmojiMap.put("rain", "🌧️");
        iconToEmojiMap.put("drizzle", "🌦️");
        iconToEmojiMap.put("thunderstorm", "⛈️");
        iconToEmojiMap.put("snow", "❄️");
        iconToEmojiMap.put("mist", "🌫️");
        iconToEmojiMap.put("fog", "🌫️");
        iconToEmojiMap.put("haze", "🌫️");
        iconToEmojiMap.put("dust", "🌫️");
        iconToEmojiMap.put("sand", "🌫️");
        iconToEmojiMap.put("ash", "🌫️");
        iconToEmojiMap.put("squall", "💨");
        iconToEmojiMap.put("tornado", "🌪️");
    }

    public String getEmojiFromIconCode(String iconCode) {
        if (iconCode == null || iconCode.isEmpty()) {
            return "🌤️"; // Emoji por defecto si no hay código
        }

        // Intenta obtener el emoji basado en el código exacto
        String emoji = iconToEmojiMap.get(iconCode);

        // Si no se encuentra el código exacto, intenta con versiones simplificadas
        if (emoji == null) {
            // Intenta buscar por la descripción del clima (parte del código sin el sufijo d/n)
            if (iconCode.length() > 2) {
                String baseCode = iconCode.substring(0, 2);
                emoji = iconToEmojiMap.get(baseCode);
            }

            // Si todavía es null, intenta buscar por palabras clave
            if (emoji == null) {
                iconCode = iconCode.toLowerCase();

                if (iconCode.contains("clear")) emoji = iconToEmojiMap.get("clear");
                else if (iconCode.contains("cloud")) emoji = iconToEmojiMap.get("clouds");
                else if (iconCode.contains("rain")) emoji = iconToEmojiMap.get("rain");
                else if (iconCode.contains("drizzle")) emoji = iconToEmojiMap.get("drizzle");
                else if (iconCode.contains("thunder")) emoji = iconToEmojiMap.get("thunderstorm");
                else if (iconCode.contains("snow")) emoji = iconToEmojiMap.get("snow");
                else if (iconCode.contains("mist") || iconCode.contains("fog")) emoji = iconToEmojiMap.get("mist");
            }
        }

        // Si aún así no encontramos un emoji, devolvemos uno por defecto
        return emoji != null ? emoji : "🌤️";
    }
}