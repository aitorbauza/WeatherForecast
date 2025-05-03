package com.example.weatherforecast.service;

import com.example.weatherforecast.dto.ForecastResponse;
import com.example.weatherforecast.dto.WeatherResponse;
import com.example.weatherforecast.model.CurrentWeather;
import com.example.weatherforecast.model.DailyForecast;
import com.example.weatherforecast.model.HourlyForecast;
import com.example.weatherforecast.repository.WeatherRepository;
import com.example.weatherforecast.util.WeatherIconMapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherService {
    private final WeatherRepository repository;
    private final WeatherIconMapper iconMapper;

    public WeatherService() {
        repository = new WeatherRepository();
        iconMapper = new WeatherIconMapper();
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

                    String weatherCondition = "";
                    String iconCode = "";
                    if (data.getWeather() != null && !data.getWeather().isEmpty()) {
                        weatherCondition = traducirCondicionClimatica(data.getWeather().get(0).getDescription());
                        iconCode = data.getWeather().get(0).getIcon();
                    }

                    String summary = generarResumenClima(data);

                    CurrentWeather currentWeather = new CurrentWeather(
                            data.getCityName(),
                            data.getSys().getCountry(),
                            data.getMain().getTemperature(),
                            data.getMain().getMaxTemperature(),
                            data.getMain().getMinTemperature(),
                            weatherCondition,
                            iconMapper.getEmojiFromIconCode(iconCode),
                            summary
                    );

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
                    List<HourlyForecast> hourlyForecasts = processHourlyForecast(data);
                    callback.onHourlyForecastLoaded(hourlyForecasts);

                    // Procesar pronóstico diario (próximos 7 días)
                    List<DailyForecast> dailyForecasts = processDailyForecast(data);
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

    private List<HourlyForecast> processHourlyForecast(ForecastResponse response) {
        List<HourlyForecast> hourlyList = new ArrayList<>();
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH" + ":00", Locale.getDefault());

        if (response.getList() == null || response.getList().isEmpty()) {
            return hourlyList;
        }

        // Obtener la hora actual
        Calendar currentCal = Calendar.getInstance();
        int currentHour = currentCal.get(Calendar.HOUR_OF_DAY);

        try {
            // Obtener la primera entrada para determinar la fecha base
            ForecastResponse.TimePoint firstPoint = response.getList().get(0);
            Date firstDate = inputFormat.parse(firstPoint.getDateTimeText());
            Calendar firstCal = Calendar.getInstance();
            firstCal.setTime(firstDate);

            // Generar pronóstico por hora interpolando datos
            for (int hour = 0; hour < 24; hour++) {
                // Calcular la hora para este pronóstico (a partir de la hora actual)
                Calendar targetCal = (Calendar) currentCal.clone();
                targetCal.add(Calendar.HOUR_OF_DAY, hour);

                // Encontrar los dos puntos de datos más cercanos para interpolar
                ForecastResponse.TimePoint before = null;
                ForecastResponse.TimePoint after = null;

                for (int i = 0; i < response.getList().size() - 1; i++) {
                    ForecastResponse.TimePoint current = response.getList().get(i);
                    ForecastResponse.TimePoint next = response.getList().get(i + 1);

                    Date currentDate = inputFormat.parse(current.getDateTimeText());
                    Date nextDate = inputFormat.parse(next.getDateTimeText());

                    if (currentDate.getTime() <= targetCal.getTimeInMillis() &&
                            nextDate.getTime() > targetCal.getTimeInMillis()) {
                        before = current;
                        after = next;
                        break;
                    }
                }

                // Si no encontramos puntos para interpolar, usar el más cercano
                if (before == null || after == null) {
                    ForecastResponse.TimePoint nearest = findNearestTimePoint(response.getList(),
                            targetCal.getTimeInMillis(),
                            inputFormat);
                    if (nearest == null) {
                        continue;
                    }

                    String formattedHour = outputFormat.format(targetCal.getTime());
                    String iconCode = "";
                    if (nearest.getWeather() != null && !nearest.getWeather().isEmpty()) {
                        iconCode = nearest.getWeather().get(0).getIcon();
                    }

                    HourlyForecast hourlyForecast = new HourlyForecast(
                            formattedHour,
                            nearest.getMain().getTemperature(),
                            iconMapper.getEmojiFromIconCode(iconCode)
                    );

                    hourlyList.add(hourlyForecast);
                    continue;
                }

                // Interpolar la temperatura entre los dos puntos
                Date beforeDate = inputFormat.parse(before.getDateTimeText());
                Date afterDate = inputFormat.parse(after.getDateTimeText());

                double totalTimeDiff = afterDate.getTime() - beforeDate.getTime();
                double targetTimeDiff = targetCal.getTimeInMillis() - beforeDate.getTime();
                double ratio = targetTimeDiff / totalTimeDiff;

                double interpolatedTemp = before.getMain().getTemperature() +
                        ratio * (after.getMain().getTemperature() - before.getMain().getTemperature());

                String formattedHour = outputFormat.format(targetCal.getTime());
                String iconCode = "";
                if (before.getWeather() != null && !before.getWeather().isEmpty()) {
                    iconCode = before.getWeather().get(0).getIcon();
                }

                HourlyForecast hourlyForecast = new HourlyForecast(
                        formattedHour,
                        interpolatedTemp,
                        iconMapper.getEmojiFromIconCode(iconCode)
                );

                hourlyList.add(hourlyForecast);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return hourlyList;
    }

    private ForecastResponse.TimePoint findNearestTimePoint(List<ForecastResponse.TimePoint> points,
                                                            long targetTime,
                                                            SimpleDateFormat format) {
        ForecastResponse.TimePoint nearest = null;
        long minTimeDiff = Long.MAX_VALUE;

        try {
            for (ForecastResponse.TimePoint point : points) {
                Date pointDate = format.parse(point.getDateTimeText());
                long timeDiff = Math.abs(pointDate.getTime() - targetTime);

                if (timeDiff < minTimeDiff) {
                    minTimeDiff = timeDiff;
                    nearest = point;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return nearest;
    }

    private List<DailyForecast> processDailyForecast(ForecastResponse response) {
        // Agrupar por día
        Map<String, List<ForecastResponse.TimePoint>> dailyPoints = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (ForecastResponse.TimePoint point : response.getList()) {
            try {
                String dateKey = point.getDateTimeText().split(" ")[0]; // Obtener solo la fecha

                if (!dailyPoints.containsKey(dateKey)) {
                    dailyPoints.put(dateKey, new ArrayList<>());
                }

                dailyPoints.get(dateKey).add(point);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Crear pronóstico diario
        List<DailyForecast> dailyList = new ArrayList<>();
        String[] dayAbbreviations = {"Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE", new Locale("es", "ES"));

        // Organizar por fecha
        List<String> sortedDates = new ArrayList<>(dailyPoints.keySet());
        java.util.Collections.sort(sortedDates);

        // Si tenemos menos de 7 días en los datos de la API, generar días adicionales
        if (sortedDates.size() < 7) {
            try {
                // Obtener la última fecha disponible
                String lastDateStr = sortedDates.get(sortedDates.size() - 1);
                Date lastDate = dateFormat.parse(lastDateStr);
                Calendar lastCal = Calendar.getInstance();
                lastCal.setTime(lastDate);

                // Añadir fechas adicionales
                int daysToAdd = 7 - sortedDates.size();
                for (int i = 0; i < daysToAdd; i++) {
                    lastCal.add(Calendar.DAY_OF_MONTH, 1);
                    String newDateStr = dateFormat.format(lastCal.getTime());
                    sortedDates.add(newDateStr);

                    // Añadir una lista vacía para este día (usaremos extrapolación después)
                    if (!dailyPoints.containsKey(newDateStr)) {
                        dailyPoints.put(newDateStr, new ArrayList<>());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < Math.min(7, sortedDates.size()); i++) {
            String dateKey = sortedDates.get(i);
            List<ForecastResponse.TimePoint> points = dailyPoints.get(dateKey);

            try {
                Date date = dateFormat.parse(dateKey);
                calendar.setTime(date);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // Ajustar al índice de 0-6

                // Para el día actual, mostrar "Hoy" en lugar de la abreviatura
                String dayText;
                if (i == 0) {
                    dayText = "Hoy";
                } else {
                    dayText = dayAbbreviations[dayOfWeek];
                }

                // Si no hay puntos de datos para este día (extrapolación), basarse en el día anterior
                if (points.isEmpty() && i > 0) {
                    String prevDateKey = sortedDates.get(i - 1);
                    List<ForecastResponse.TimePoint> prevPoints = dailyPoints.get(prevDateKey);

                    if (!prevPoints.isEmpty()) {
                        // Usar los datos del día anterior con ligeras variaciones
                        double maxTemp = 0;
                        double minTemp = 0;
                        String mostCommonIcon = "";

                        for (ForecastResponse.TimePoint point : prevPoints) {
                            maxTemp = Math.max(maxTemp, point.getMain().getMaxTemperature());
                            minTemp = Math.min(minTemp, point.getMain().getMinTemperature());

                            if (point.getWeather() != null && !point.getWeather().isEmpty()) {
                                mostCommonIcon = point.getWeather().get(0).getIcon();
                            }
                        }

                        // Añadir pequeña variación aleatoria para simular predicción
                        double randVariation = (Math.random() * 2) - 1; // Entre -1 y 1
                        maxTemp += randVariation;
                        minTemp += randVariation;

                        DailyForecast dailyForecast = new DailyForecast(
                                dayText,
                                maxTemp,
                                minTemp,
                                iconMapper.getEmojiFromIconCode(mostCommonIcon)
                        );

                        dailyList.add(dailyForecast);
                        continue;
                    }
                }

                // Procesamiento normal si hay puntos de datos
                if (!points.isEmpty()) {
                    // Calcular temperaturas máxima y mínima del día
                    double maxTemp = Double.MIN_VALUE;
                    double minTemp = Double.MAX_VALUE;
                    String mostCommonIcon = "";
                    Map<String, Integer> iconCounts = new HashMap<>();

                    for (ForecastResponse.TimePoint point : points) {
                        maxTemp = Math.max(maxTemp, point.getMain().getMaxTemperature());
                        minTemp = Math.min(minTemp, point.getMain().getMinTemperature());

                        // Contar el icono más común del día
                        if (point.getWeather() != null && !point.getWeather().isEmpty()) {
                            String icon = point.getWeather().get(0).getIcon();
                            iconCounts.put(icon, iconCounts.getOrDefault(icon, 0) + 1);
                        }
                    }

                    // Encontrar el icono más común
                    int maxCount = 0;
                    for (Map.Entry<String, Integer> entry : iconCounts.entrySet()) {
                        if (entry.getValue() > maxCount) {
                            maxCount = entry.getValue();
                            mostCommonIcon = entry.getKey();
                        }
                    }

                    DailyForecast dailyForecast = new DailyForecast(
                            dayText,
                            maxTemp,
                            minTemp,
                            iconMapper.getEmojiFromIconCode(mostCommonIcon)
                    );

                    dailyList.add(dailyForecast);
                } else {
                    // Si no hay datos y es el primer día (caso poco probable)
                    DailyForecast dailyForecast = new DailyForecast(
                            dayText,
                            20.0, // Temperatura predeterminada
                            15.0, // Temperatura predeterminada
                            "🌤️" // Icono predeterminado
                    );

                    dailyList.add(dailyForecast);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return dailyList;
    }

    private String generarResumenClima(WeatherResponse data) {
        if (data.getWeather() == null || data.getWeather().isEmpty()) {
            return "Sin información disponible";
        }

        String mainCondition = data.getWeather().get(0).getMain();
        double temp = data.getMain().getTemperature();
        double maxTemp = data.getMain().getMaxTemperature();
        double minTemp = data.getMain().getMinTemperature();

        StringBuilder summary = new StringBuilder();

        // Generar un resumen basado en las condiciones climáticas
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

        // Añadir información sobre la temperatura
        if (maxTemp - minTemp > 8) {
            summary.append(" con cambios considerables de temperatura");
        } else if (temp > 30) {
            summary.append(" con temperaturas muy altas");
        } else if (temp < 5) {
            summary.append(" con temperaturas muy bajas");
        }

        return summary.toString();
    }

    // Método para traducir las condiciones climáticas de inglés a español
    private String traducirCondicionClimatica(String enCondition) {
        if (enCondition == null) {
            return "";
        }

        Map<String, String> traducciones = new HashMap<>();
        traducciones.put("clear sky", "Cielo despejado");
        traducciones.put("few clouds", "Pocas nubes");
        traducciones.put("scattered clouds", "Nubes dispersas");
        traducciones.put("broken clouds", "Nubosidad parcial");
        traducciones.put("overcast clouds", "Nubosidad total");
        traducciones.put("light rain", "Lluvia ligera");
        traducciones.put("moderate rain", "Lluvia moderada");
        traducciones.put("heavy intensity rain", "Lluvia intensa");
        traducciones.put("very heavy rain", "Lluvia muy intensa");
        traducciones.put("extreme rain", "Lluvia extrema");
        traducciones.put("freezing rain", "Lluvia helada");
        traducciones.put("light intensity shower rain", "Llovizna ligera");
        traducciones.put("shower rain", "Chubasco");
        traducciones.put("heavy intensity shower rain", "Chubasco intenso");
        traducciones.put("ragged shower rain", "Chubasco irregular");
        traducciones.put("thunderstorm", "Tormenta");
        traducciones.put("thunderstorm with light rain", "Tormenta con lluvia ligera");
        traducciones.put("thunderstorm with rain", "Tormenta con lluvia");
        traducciones.put("thunderstorm with heavy rain", "Tormenta con lluvia intensa");
        traducciones.put("light thunderstorm", "Tormenta ligera");
        traducciones.put("heavy thunderstorm", "Tormenta intensa");
        traducciones.put("ragged thunderstorm", "Tormenta irregular");
        traducciones.put("mist", "Neblina");
        traducciones.put("fog", "Niebla");
        traducciones.put("snow", "Nieve");
        traducciones.put("light snow", "Nieve ligera");
        traducciones.put("heavy snow", "Nieve intensa");
        traducciones.put("sleet", "Aguanieve");
        traducciones.put("shower sleet", "Chubasco de aguanieve");
        traducciones.put("light rain and snow", "Lluvia ligera con nieve");
        traducciones.put("rain and snow", "Lluvia con nieve");
        traducciones.put("light shower snow", "Chubasco de nieve ligero");
        traducciones.put("shower snow", "Chubasco de nieve");
        traducciones.put("heavy shower snow", "Chubasco de nieve intenso");
        traducciones.put("dust", "Polvo");
        traducciones.put("sand", "Arena");
        traducciones.put("volcanic ash", "Ceniza volcánica");
        traducciones.put("squalls", "Ráfagas");
        traducciones.put("tornado", "Tornado");
        traducciones.put("haze", "Calima");
        traducciones.put("smoke", "Humo");
        traducciones.put("drizzle", "Llovizna");
        traducciones.put("light intensity drizzle", "Llovizna ligera");
        traducciones.put("heavy intensity drizzle", "Llovizna intensa");
        traducciones.put("drizzle rain", "Lluvia con llovizna");
        traducciones.put("heavy shower rain and drizzle", "Chubasco intenso con llovizna");
        traducciones.put("shower rain and drizzle", "Chubasco con llovizna");
        traducciones.put("heavy intensity drizzle rain", "Lluvia intensa con llovizna");

        String lowerCaseCondition = enCondition.toLowerCase();
        return traducciones.getOrDefault(lowerCaseCondition, enCondition);
    }
}