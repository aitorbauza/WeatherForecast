package com.example.weatherforecast.service;

import com.example.weatherforecast.dto.ForecastResponse;
import com.example.weatherforecast.model.DailyForecast;
import com.example.weatherforecast.model.HourlyForecast;
import com.example.weatherforecast.util.WeatherIconMapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Clase encargada de procesar los datos de pronóstico del clima.
 */
public class ForecastProcessor {
    private final WeatherIconMapper iconMapper;

    public ForecastProcessor(WeatherIconMapper iconMapper) {
        this.iconMapper = iconMapper;
    }

    public List<HourlyForecast> processHourlyForecast(ForecastResponse response) {
        List<HourlyForecast> hourlyList = new ArrayList<>();
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH" + ":00", Locale.getDefault());

        if (response.getList() == null || response.getList().isEmpty()) {
            return hourlyList;
        }

        // Obtener la hora actual
        Calendar currentCal = Calendar.getInstance();
        int currentHour = currentCal.get(Calendar.HOUR_OF_DAY);

        // Filtrar pronóstico por hora
        try {
            ForecastResponse.TimePoint firstPoint = response.getList().get(0);
            Date firstDate = inputFormat.parse(firstPoint.getDateTimeText());
            Calendar firstCal = Calendar.getInstance();
            firstCal.setTime(firstDate);

            // Generar pronóstico por hora
            for (int hour = 0; hour < 24; hour++) {
                Calendar targetCal = (Calendar) currentCal.clone();
                targetCal.add(Calendar.HOUR_OF_DAY, hour);

                ForecastResponse.TimePoint before = null;
                ForecastResponse.TimePoint after = null;

                // Bucle que encuentra los puntos antes y después de la hora actual
                for (int i = 0; i < response.getList().size() - 1; i++) {
                    ForecastResponse.TimePoint current = response.getList().get(i);
                    ForecastResponse.TimePoint next = response.getList().get(i + 1);

                    Date currentDate = inputFormat.parse(current.getDateTimeText());
                    Date nextDate = inputFormat.parse(next.getDateTimeText());

                    // Si la hora actual está entre las dos horas de pronóstico, guardar los puntos
                    if (currentDate.getTime() <= targetCal.getTimeInMillis() &&
                            nextDate.getTime() > targetCal.getTimeInMillis()) {
                        before = current;
                        after = next;
                        break;
                    }
                }

                // Si no encontramos puntos para interpolar, usamos el más cercano
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

    // Método para procesar pronóstico diario
    public List<DailyForecast> processDailyForecast(ForecastResponse response) {
        Map<String, List<ForecastResponse.TimePoint>> dailyPoints = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Agrupar puntos por fecha
        for (ForecastResponse.TimePoint point : response.getList()) {
            try {
                String dateKey = point.getDateTimeText().split(" ")[0];

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

                    // Añadir una lista vacía para este día
                    if (!dailyPoints.containsKey(newDateStr)) {
                        dailyPoints.put(newDateStr, new ArrayList<>());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Generar pronóstico diario
        for (int i = 0; i < Math.min(7, sortedDates.size()); i++) {
            String dateKey = sortedDates.get(i);
            List<ForecastResponse.TimePoint> points = dailyPoints.get(dateKey);

            try {
                Date date = dateFormat.parse(dateKey);
                calendar.setTime(date);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;

                // Para el día actual, mostrar "Hoy"
                String dayText;
                if (i == 0) {
                    dayText = "Hoy";
                } else {
                    dayText = dayAbbreviations[dayOfWeek];
                }

                // Si no hay puntos de datos para este día, basarse en el día anterior
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
                        double randVariation = (Math.random() * 2) - 1;
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

                    // Contar el número de veces que aparece cada icono
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
                    // Si no hay datos y es el primer día
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

    private ForecastResponse.TimePoint findNearestTimePoint(
            List<ForecastResponse.TimePoint> points,
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

}