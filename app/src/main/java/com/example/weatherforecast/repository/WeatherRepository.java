package com.example.weatherforecast.repository;

import com.example.weatherforecast.dto.ForecastResponse;
import com.example.weatherforecast.dto.WeatherResponse;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Clase encargada de gestionar las llamadas a la API de OpenWeather.
 */
public class WeatherRepository {
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static final String API_KEY = "fd55aeb51961a4033188497fa3b1f146"; // API Key de OpenWeather

    private final WeatherApiService apiService;

    public WeatherRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(WeatherApiService.class);
    }

    // Units se encarga de devolver la temperatura en Celsius
    public Call<WeatherResponse> getCurrentWeather(String city) {
        return apiService.getCurrentWeather(city, "metric", API_KEY);
    }

    public Call<ForecastResponse> getForecast(String city) {
        return apiService.getForecast(city, "metric", API_KEY);
    }

    // Interfaz para definir las llamadas a la API
    public interface WeatherApiService {
        @GET("weather")
        Call<WeatherResponse> getCurrentWeather(
                @Query("q") String city,
                @Query("units") String units,
                @Query("appid") String apiKey
        );

        @GET("forecast")
        Call<ForecastResponse> getForecast(
                @Query("q") String city,
                @Query("units") String units,
                @Query("appid") String apiKey
        );
    }
}
