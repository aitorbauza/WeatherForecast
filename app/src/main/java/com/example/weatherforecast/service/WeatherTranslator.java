package com.example.weatherforecast.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase encargada de traducir las condiciones climáticas de inglés a español
 */
public class WeatherTranslator {
    private final Map<String, String> translations;

    public WeatherTranslator() {
        translations = new HashMap<>();
        initializeTranslations();
    }

    public String translateWeatherCondition(String enCondition) {
        if (enCondition == null) {
            return "";
        }

        String lowerCaseCondition = enCondition.toLowerCase();
        return translations.getOrDefault(lowerCaseCondition, enCondition);
    }

    private void initializeTranslations() {
        translations.put("clear sky", "Cielo despejado");
        translations.put("few clouds", "Pocas nubes");
        translations.put("scattered clouds", "Nubes dispersas");
        translations.put("broken clouds", "Nubosidad parcial");
        translations.put("overcast clouds", "Nubosidad total");
        translations.put("light rain", "Lluvia ligera");
        translations.put("moderate rain", "Lluvia moderada");
        translations.put("heavy intensity rain", "Lluvia intensa");
        translations.put("very heavy rain", "Lluvia muy intensa");
        translations.put("extreme rain", "Lluvia extrema");
        translations.put("freezing rain", "Lluvia helada");
        translations.put("light intensity shower rain", "Llovizna ligera");
        translations.put("shower rain", "Chubasco");
        translations.put("heavy intensity shower rain", "Chubasco intenso");
        translations.put("ragged shower rain", "Chubasco irregular");
        translations.put("thunderstorm", "Tormenta");
        translations.put("thunderstorm with light rain", "Tormenta con lluvia ligera");
        translations.put("thunderstorm with rain", "Tormenta con lluvia");
        translations.put("thunderstorm with heavy rain", "Tormenta con lluvia intensa");
        translations.put("light thunderstorm", "Tormenta ligera");
        translations.put("heavy thunderstorm", "Tormenta intensa");
        translations.put("ragged thunderstorm", "Tormenta irregular");
        translations.put("mist", "Neblina");
        translations.put("fog", "Niebla");
        translations.put("snow", "Nieve");
        translations.put("light snow", "Nieve ligera");
        translations.put("heavy snow", "Nieve intensa");
        translations.put("sleet", "Aguanieve");
        translations.put("shower sleet", "Chubasco de aguanieve");
        translations.put("light rain and snow", "Lluvia ligera con nieve");
        translations.put("rain and snow", "Lluvia con nieve");
        translations.put("light shower snow", "Chubasco de nieve ligero");
        translations.put("shower snow", "Chubasco de nieve");
        translations.put("heavy shower snow", "Chubasco de nieve intenso");
        translations.put("dust", "Polvo");
        translations.put("sand", "Arena");
        translations.put("volcanic ash", "Ceniza volcánica");
        translations.put("squalls", "Ráfagas");
        translations.put("tornado", "Tornado");
        translations.put("haze", "Calima");
        translations.put("smoke", "Humo");
        translations.put("drizzle", "Llovizna");
        translations.put("light intensity drizzle", "Llovizna ligera");
        translations.put("heavy intensity drizzle", "Llovizna intensa");
        translations.put("drizzle rain", "Lluvia con llovizna");
        translations.put("heavy shower rain and drizzle", "Chubasco intenso con llovizna");
        translations.put("shower rain and drizzle", "Chubasco con llovizna");
        translations.put("heavy intensity drizzle rain", "Lluvia intensa con llovizna");
    }
}