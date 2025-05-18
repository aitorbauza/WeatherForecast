package com.example.weatherforecast.service;

import com.example.weatherforecast.dto.WeatherResponse;
import com.example.weatherforecast.model.CurrentWeather;
import com.example.weatherforecast.util.WeatherIconMapper;

/**
 * Clase encargada de procesar los datos del clima,
 */
public class WeatherDataProcessor {
    private final WeatherIconMapper iconMapper;

    public WeatherDataProcessor(WeatherIconMapper iconMapper) {
        this.iconMapper = iconMapper;
    }

    // Método que procesa los datos del clima
    public CurrentWeather processCurrentWeather(WeatherResponse data, WeatherTranslator translator) {
        String weatherCondition = "";
        String iconCode = "";
        if (data.getWeather() != null && !data.getWeather().isEmpty()) {
            weatherCondition = translator.translateWeatherCondition(data.getWeather().get(0).getDescription());
            iconCode = data.getWeather().get(0).getIcon();
        }

        String summary = generateWeatherSummary(data);

        return new CurrentWeather(
                data.getCityName(),
                data.getSys().getCountry(),
                data.getMain().getTemperature(),
                data.getMain().getMaxTemperature(),
                data.getMain().getMinTemperature(),
                weatherCondition,
                iconMapper.getEmojiFromIconCode(iconCode),
                summary,
                data.getMain().getHumidity()
        );
    }

    // Método que genera un resumen del clima
    private String generateWeatherSummary(WeatherResponse data) {
        if (data.getWeather() == null || data.getWeather().isEmpty()) {
            return "Sin información disponible";
        }

        String mainCondition = data.getWeather().get(0).getMain();
        double temp = data.getMain().getTemperature();
        double maxTemp = data.getMain().getMaxTemperature();
        double minTemp = data.getMain().getMinTemperature();

        StringBuilder summary = new StringBuilder();

        // Resumen basado en las condiciones climáticas
        switch (mainCondition.toLowerCase()) {
            case "clear":
                summary.append("Se prevé un día soleado");
                break;
            case "clouds":
                summary.append("Se esperan nubes durante el día");
                break;
            case "rain":
                summary.append("Se esperan lluvias");
                break;
            case "drizzle":
                summary.append("Se esperan lloviznas ligeras");
                break;
            case "thunderstorm":
                summary.append("Se prevén tormentas eléctricas");
                break;
            case "snow":
                summary.append("Se esperan nevadas");
                break;
            case "mist":
            case "fog":
                summary.append("Se esperan nieblas o brumas");
                break;
            default:
                summary.append("Se prevén condiciones variables");
        }

        // Info extra sobre la temperatura
        if (maxTemp - minTemp > 8) {
            summary.append(" con cambios considerables de temperatura");
        } else if (temp > 30) {
            summary.append(" con temperaturas muy altas");
        } else if (temp < 5) {
            summary.append(" con temperaturas muy bajas");
        }

        return summary.toString();
    }
}
