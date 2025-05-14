package com.example.weatherforecast.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase para almacenar en caché los datos del clima
 * Permite recuperar datos cuando se produce una navegación que pierde el contexto
 */
public class WeatherCache {
    private static final String PREF_NAME = "WeatherCachePrefs";
    private static final String KEY_CURRENT_WEATHER = "current_weather";
    private static final String KEY_HOURLY_FORECAST = "hourly_forecast";
    private static final String KEY_DAILY_FORECAST = "daily_forecast";
    private static final String KEY_LAST_CITY = "last_city";
    private static final String KEY_LAST_UPDATE = "last_update";

    private final SharedPreferences preferences;
    private final Gson gson;

    public WeatherCache(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveCurrentWeather(CurrentWeather weather) {
        if (weather != null) {
            preferences.edit()
                    .putString(KEY_CURRENT_WEATHER, gson.toJson(weather))
                    .putString(KEY_LAST_CITY, weather.getLocation())
                    .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
                    .apply();
        }
    }

    public void saveHourlyForecast(List<HourlyForecast> forecasts) {
        if (forecasts != null && !forecasts.isEmpty()) {
            preferences.edit()
                    .putString(KEY_HOURLY_FORECAST, gson.toJson(forecasts))
                    .apply();
        }
    }

    public void saveDailyForecast(List<DailyForecast> forecasts) {
        if (forecasts != null && !forecasts.isEmpty()) {
            preferences.edit()
                    .putString(KEY_DAILY_FORECAST, gson.toJson(forecasts))
                    .apply();
        }
    }

    public CurrentWeather getCurrentWeather() {
        String json = preferences.getString(KEY_CURRENT_WEATHER, null);
        if (json != null) {
            try {
                return gson.fromJson(json, CurrentWeather.class);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public List<HourlyForecast> getHourlyForecast() {
        String json = preferences.getString(KEY_HOURLY_FORECAST, null);
        if (json != null) {
            try {
                return gson.fromJson(json, new com.google.gson.reflect.TypeToken<List<HourlyForecast>>(){}.getType());
            } catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    public List<DailyForecast> getDailyForecast() {
        String json = preferences.getString(KEY_DAILY_FORECAST, null);
        if (json != null) {
            try {
                return gson.fromJson(json, new com.google.gson.reflect.TypeToken<List<DailyForecast>>(){}.getType());
            } catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    public String getLastCity() {
        return preferences.getString(KEY_LAST_CITY, null);
    }

    public long getLastUpdateTime() {
        return preferences.getLong(KEY_LAST_UPDATE, 0);
    }

    public boolean hasRecentData(int maxAgeMinutes) {
        long now = System.currentTimeMillis();
        long lastUpdate = getLastUpdateTime();
        long maxAgeMillis = maxAgeMinutes * 60 * 1000;

        return (now - lastUpdate) < maxAgeMillis;
    }
}