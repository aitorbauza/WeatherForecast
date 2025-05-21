package com.example.weatherforecast.ui.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.weatherforecast.R;
import com.example.weatherforecast.model.DailyForecast;

import java.util.List;

/**
 * Componente para mostrar la información diaria del clima
 */
public class DailyForecastComponent {
    private final LinearLayout container;
    private final Context context;

    public DailyForecastComponent(LinearLayout container, Context context) {
        this.container = container;
        this.context = context;
    }

    // Método para mostrar las predicciones diarias
    public void displayForecasts(List<DailyForecast> forecasts) {
        container.removeAllViews();

        for (DailyForecast forecast : forecasts) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_daily_forecast, container, false);

            TextView dayText = itemView.findViewById(R.id.dayText);
            TextView weatherEmojiDaily = itemView.findViewById(R.id.weatherEmojiDaily);
            TextView maxTempDaily = itemView.findViewById(R.id.maxTempDaily);
            TextView minTempDaily = itemView.findViewById(R.id.minTempDaily);

            dayText.setText(forecast.getDay());
            weatherEmojiDaily.setText(forecast.getWeatherIcon());
            maxTempDaily.setText(String.format("%.1f°C", forecast.getMaxTemperature()));
            minTempDaily.setText(String.format("%.1f°C", forecast.getMinTemperature()));

            container.addView(itemView);
        }
    }

}