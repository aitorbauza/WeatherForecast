package com.example.weatherforecast.ui.weather;

import android.widget.TextView;

import com.example.weatherforecast.model.CurrentWeather;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Component responsible for displaying current weather information.
 * Follows Single Responsibility Principle by handling only weather display logic.
 */
public class WeatherDisplayComponent {
    private final TextView locationText;
    private final TextView weatherEmoji;
    private final TextView temperatureText;
    private final TextView weatherConditionText;
    private final TextView maxTempText;
    private final TextView minTempText;
    private final TextView weatherSummaryText;
    private final TextView humidityText;

    public WeatherDisplayComponent(
            TextView locationText,
            TextView weatherEmoji,
            TextView temperatureText,
            TextView weatherConditionText,
            TextView maxTempText,
            TextView minTempText,
            TextView weatherSummaryText,
            TextView humidityText) {
        this.locationText = locationText;
        this.weatherEmoji = weatherEmoji;
        this.temperatureText = temperatureText;
        this.weatherConditionText = weatherConditionText;
        this.maxTempText = maxTempText;
        this.minTempText = minTempText;
        this.weatherSummaryText = weatherSummaryText;
        this.humidityText = humidityText;
    }

    /**
     * Updates the UI with current weather information
     * @param weather The current weather data
     */
    public void displayWeather(CurrentWeather weather) {
        // Format for "Today, HH:mm"
        SimpleDateFormat dateFormat = new SimpleDateFormat("'Hoy', HH:mm", new Locale("es", "ES"));
        String currentDateTime = dateFormat.format(new Date());

        locationText.setText(String.format("%s, %s", weather.getLocation(), weather.getCountry()));
        weatherEmoji.setText(weather.getWeatherIcon());
        temperatureText.setText(String.format("%.1f°C", weather.getTemperature()));
        weatherConditionText.setText(weather.getWeatherCondition());
        maxTempText.setText(String.format("Máx: %.1f°C", weather.getMaxTemperature()));
        minTempText.setText(String.format("Mín: %.1f°C", weather.getMinTemperature()));
        humidityText.setText(String.format("%d%%", weather.getHumidity()));
        weatherSummaryText.setText(currentDateTime + " - " + weather.getSummary());
    }
}