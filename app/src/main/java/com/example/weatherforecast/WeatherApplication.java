package com.example.weatherforecast;

import android.app.Application;

import com.example.weatherforecast.model.CurrentWeather;
import com.example.weatherforecast.model.DailyForecast;
import com.example.weatherforecast.model.HourlyForecast;
import com.example.weatherforecast.data.WeatherCache;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase de aplicación principal que inicializa componentes globales
 * y proporciona valores predeterminados para cuando no hay datos disponibles
 */
public class WeatherApplication extends Application {
    private WeatherCache weatherCache;

    @Override
    public void onCreate() {
        super.onCreate();

        // Inicializar la caché
        weatherCache = new WeatherCache(this);

        // Si no hay datos en caché, inicializar con valores predeterminados
        if (weatherCache.getCurrentWeather() == null) {
            initializeDefaultWeatherData();
        }
    }

    /**
     * Inicializa datos meteorológicos por defecto para que siempre haya
     * alguna información disponible en la aplicación
     */
    private void initializeDefaultWeatherData() {
        // Crear datos del clima actual por defecto
        CurrentWeather defaultWeather = new CurrentWeather();
        defaultWeather.setLocation("Palma de Mallorca");
        defaultWeather.setCountry("España");
        defaultWeather.setTemperature(22.5f);
        defaultWeather.setMaxTemperature(25.0f);
        defaultWeather.setMinTemperature(18.5f);
        defaultWeather.setHumidity(65);
        defaultWeather.setWeatherCondition("Soleado");
        defaultWeather.setWeatherIcon("☀️");
        defaultWeather.setSummary("Día soleado con algunas nubes");

        // Guardar en caché
        weatherCache.saveCurrentWeather(defaultWeather);

        // Crear pronóstico por horas por defecto
        List<HourlyForecast> hourlyForecasts = new ArrayList<>();
        String[] hours = {"09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00"};
        String[] icons = {"🌤️", "☀️", "☀️", "☀️", "🌤️", "🌤️", "🌥️", "🌥️"};
        float[] temps = {19.5f, 21.0f, 22.5f, 23.8f, 24.5f, 24.2f, 23.0f, 21.5f};

        for (int i = 0; i < hours.length; i++) {
            HourlyForecast forecast = new HourlyForecast();
            forecast.setHour(hours[i]);
            forecast.setWeatherIcon(icons[i]);
            forecast.setTemperature(temps[i]);
            hourlyForecasts.add(forecast);
        }

        // Guardar en caché
        weatherCache.saveHourlyForecast(hourlyForecasts);

        // Crear pronóstico diario por defecto
        List<DailyForecast> dailyForecasts = new ArrayList<>();
        String[] days = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};
        String[] dailyIcons = {"☀️", "🌤️", "🌥️", "🌦️", "🌧️"};
        float[] maxTemps = {25.0f, 24.5f, 23.0f, 22.0f, 20.0f};
        float[] minTemps = {18.5f, 19.0f, 17.5f, 16.0f, 15.0f};

        for (int i = 0; i < days.length; i++) {
            DailyForecast forecast = new DailyForecast();
            forecast.setDay(days[i]);
            forecast.setWeatherIcon(dailyIcons[i]);
            forecast.setMaxTemperature(maxTemps[i]);
            forecast.setMinTemperature(minTemps[i]);
            dailyForecasts.add(forecast);
        }

        // Guardar en caché
        weatherCache.saveDailyForecast(dailyForecasts);
    }

    public WeatherCache getWeatherCache() {
        return weatherCache;
    }
}