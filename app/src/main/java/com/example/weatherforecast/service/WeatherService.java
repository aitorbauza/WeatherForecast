package com.example.weatherforecast.service;

import com.example.weatherforecast.dto.ForecastResponse;
import com.example.weatherforecast.dto.WeatherResponse;
import com.example.weatherforecast.model.CurrentWeather;
import com.example.weatherforecast.model.DailyForecast;
import com.example.weatherforecast.model.HourlyForecast;
import com.example.weatherforecast.repository.WeatherRepository;
import com.example.weatherforecast.util.WeatherIconMapper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherService {
    private final WeatherRepository repository;
    private final WeatherIconMapper iconMapper;
    private final WeatherDataProcessor dataProcessor;
    private final ForecastProcessor forecastProcessor;
    private final WeatherTranslator translator;

    public WeatherService() {
        repository = new WeatherRepository();
        iconMapper = new WeatherIconMapper();
        dataProcessor = new WeatherDataProcessor(iconMapper);
        forecastProcessor = new ForecastProcessor(iconMapper);
        translator = new WeatherTranslator();
    }

    public interface WeatherCallback {
        void onWeatherLoaded(CurrentWeather currentWeather);
        void onHourlyForecastLoaded(List<HourlyForecast> hourlyForecasts);
        void onDailyForecastLoaded(List<DailyForecast> dailyForecasts);
        void onError(String message);
    }

    public void getWeatherData(String city, WeatherCallback callback) {
        // Obtener el clima actual
        repository.getCurrentWeather(city).enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse data = response.body();
                    CurrentWeather currentWeather = dataProcessor.processCurrentWeather(data, translator);
                    callback.onWeatherLoaded(currentWeather);

                    // Obtener pronóstico
                    getForecastData(city, callback);
                } else {
                    callback.onError("Error al obtener datos del clima");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                callback.onError("Error de red: " + t.getMessage());
            }
        });
    }

    private void getForecastData(String city, WeatherCallback callback) {
        repository.getForecast(city).enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ForecastResponse data = response.body();

                    // Procesar pronóstico por horas (próximas 24 horas)
                    List<HourlyForecast> hourlyForecasts = forecastProcessor.processHourlyForecast(data);
                    callback.onHourlyForecastLoaded(hourlyForecasts);

                    // Procesar pronóstico diario (próximos 7 días)
                    List<DailyForecast> dailyForecasts = forecastProcessor.processDailyForecast(data);
                    callback.onDailyForecastLoaded(dailyForecasts);
                } else {
                    callback.onError("Error al obtener datos del pronóstico");
                }
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                callback.onError("Error de red: " + t.getMessage());
            }
        });
    }
}