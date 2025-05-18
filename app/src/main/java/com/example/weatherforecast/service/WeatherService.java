package com.example.weatherforecast.service;

import com.example.weatherforecast.dto.ForecastResponse;
import com.example.weatherforecast.dto.WeatherResponse;
import com.example.weatherforecast.model.CurrentWeather;
import com.example.weatherforecast.model.DailyForecast;
import com.example.weatherforecast.model.HourlyForecast;
import com.example.weatherforecast.repository.WeatherRepository;
import com.example.weatherforecast.util.WeatherIconMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Clase encargada de obtener datos del clima y pronóstico
 */
public class WeatherService {
    private final WeatherRepository repository;
    private final WeatherIconMapper iconMapper;
    private final WeatherDataProcessor dataProcessor;
    private final ForecastProcessor forecastProcessor;
    private final WeatherTranslator translator;
    private static final String API_KEY = "fd55aeb51961a4033188497fa3b1f146"; // OPENWEATHER API KEY

   public WeatherService() {
        repository = new WeatherRepository();
        iconMapper = new WeatherIconMapper();
        dataProcessor = new WeatherDataProcessor(iconMapper);
        forecastProcessor = new ForecastProcessor(iconMapper);
        translator = new WeatherTranslator();
    }

    // Interfaz para callbacks cuando se cargan los datos del clima
    public interface WeatherCallback {

        void onWeatherLoaded(CurrentWeather currentWeather);
        void onHourlyForecastLoaded(List<HourlyForecast> hourlyForecasts);
        void onDailyForecastLoaded(List<DailyForecast> dailyForecasts);
        void onError(String message);

    }

    // Interfaz para callbacks cuando se cargan los datos de la ubicación
    public interface LocationWeatherCallback {

        void onWeatherLoaded(String locationName, double temperature, String condition, String icon);
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

    // Método para obtener pronóstico
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

    // Método para obtener datos del clima por ubicación
    public void getWeatherForLocation(double lat, double lon, LocationWeatherCallback callback) {
        try {
            // Create API URL for coordinates-based request
            String apiUrl = "https://api.openweathermap.org/data/2.5/weather" +
                    "?lat=" + lat +
                    "&lon=" + lon +
                    "&units=metric" +
                    "&appid=" + API_KEY;

            // Thread encargado de hacer la solicitud en segundo plano
            new Thread(() -> {
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Parseamos la respuesta a un objeto JSON
                    JSONObject jsonObject = new JSONObject(response.toString());

                    String locationName = jsonObject.getString("name");

                    JSONObject mainObject = jsonObject.getJSONObject("main");
                    double temperature = mainObject.getDouble("temp");

                    JSONArray weatherArray = jsonObject.getJSONArray("weather");
                    JSONObject weatherObject = weatherArray.getJSONObject(0);
                    String condition = weatherObject.getString("main");
                    String icon = getWeatherEmoji(weatherObject.getString("icon"));

                    callback.onWeatherLoaded(locationName, temperature, condition, icon);

                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError(e.getMessage());
        }
    }

    private String getWeatherEmoji(String iconCode) {
        switch (iconCode) {
            case "01d": return "☀️";
            case "01n": return "🌙";
            case "02d": case "02n": return "⛅";
            case "03d": case "03n": return "☁️";
            case "04d": case "04n": return "☁️";
            case "09d": case "09n": return "🌧️";
            case "10d": case "10n": return "🌦️";
            case "11d": case "11n": return "⛈️";
            case "13d": case "13n": return "❄️";
            case "50d": case "50n": return "🌫️";
            default: return "❓";
        }
    }

}