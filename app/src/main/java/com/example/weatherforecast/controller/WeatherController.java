package com.example.weatherforecast.controller;

import android.content.Context;

import com.example.weatherforecast.model.CurrentWeather;
import com.example.weatherforecast.model.DailyForecast;
import com.example.weatherforecast.model.HourlyForecast;
import com.example.weatherforecast.service.WeatherService;

import java.util.List;

public class WeatherController {
    private final WeatherService weatherService;
    private WeatherView view;

    public WeatherController(Context context) {
        weatherService = new WeatherService();
    }

    public interface WeatherView {
        void displayCurrentWeather(CurrentWeather weather);
        void displayHourlyForecast(List<HourlyForecast> forecasts);
        void displayDailyForecast(List<DailyForecast> forecasts);
        void showError(String message);
        void showLoading(boolean isLoading);
    }

    public void setView(WeatherView view) {
        this.view = view;
    }

    public void loadWeatherData(String city) {
        if (view == null) {
            return;
        }

        view.showLoading(true);

        try {
            weatherService.getWeatherData(city, new WeatherService.WeatherCallback() {
                @Override
                public void onWeatherLoaded(CurrentWeather currentWeather) {
                    if (view != null) {
                        view.displayCurrentWeather(currentWeather);
                    }
                }

                @Override
                public void onHourlyForecastLoaded(List<HourlyForecast> hourlyForecasts) {
                    if (view != null) {
                        view.displayHourlyForecast(hourlyForecasts);
                    }
                }

                @Override
                public void onDailyForecastLoaded(List<DailyForecast> dailyForecasts) {
                    if (view != null) {
                        view.displayDailyForecast(dailyForecasts);
                        view.showLoading(false);
                    }
                }

                @Override
                public void onError(String message) {
                    if (view != null) {
                        view.showError(message);
                        view.showLoading(false);
                    }
                }
            });
        } catch (Exception e) {
            if (view != null) {
                view.showError("Error: " + e.getMessage());
                view.showLoading(false);
            }
        }
    }

    public void onDestroy() {
        view = null;
    }
}