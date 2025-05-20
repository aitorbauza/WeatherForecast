package com.example.weatherforecast.ui.weather;

import android.widget.TextView;

import com.example.weatherforecast.model.CurrentWeather;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Componente encargado de mostrar la información del clima en la UI
 */
public class WeatherDisplayComponent {
    private final TextView locationText;
    private final TextView weatherEmoji;
    private final TextView temperatureText;
    private final TextView weatherConditionText;
    private final TextView weatherSummaryText;
    private final TextView humidityText;

    public WeatherDisplayComponent(
            TextView locationText,
            TextView weatherEmoji,
            TextView temperatureText,
            TextView weatherConditionText,
            TextView weatherSummaryText,
            TextView humidityText) {
        this.locationText = locationText;
        this.weatherEmoji = weatherEmoji;
        this.temperatureText = temperatureText;
        this.weatherConditionText = weatherConditionText;
        this.weatherSummaryText = weatherSummaryText;
        this.humidityText = humidityText;
    }


    public void displayWeather(CurrentWeather weather) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("'Hoy', HH:mm", new Locale("es", "ES"));
        String currentDateTime = dateFormat.format(new Date());

        locationText.setText(String.format("%s, %s", weather.getLocation(), weather.getCountry()));
        weatherEmoji.setText(weather.getWeatherIcon());
        temperatureText.setText(String.format("%.1f°C", weather.getTemperature()));
        weatherConditionText.setText(weather.getWeatherCondition());
        humidityText.setText(String.format("Humedad: %d%%", weather.getHumidity()));
        weatherSummaryText.setText(currentDateTime + " - " + weather.getSummary());
    }

}