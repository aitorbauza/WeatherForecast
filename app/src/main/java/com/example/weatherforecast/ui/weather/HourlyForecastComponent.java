package com.example.weatherforecast.ui.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.weatherforecast.R;
import com.example.weatherforecast.model.HourlyForecast;

import java.util.List;

/**
 * Componente para mostrar la información horaria del clima
 */
public class HourlyForecastComponent {
    private final LinearLayout container;
    private final Context context;

    public HourlyForecastComponent(LinearLayout container, Context context) {
        this.container = container;
        this.context = context;
    }

    // Método para mostrar las predicciones horarias
    public void displayForecasts(List<HourlyForecast> forecasts) {
        container.removeAllViews();

        for (HourlyForecast forecast : forecasts) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_hourly_forecast, container, false);

            TextView hourText = itemView.findViewById(R.id.hourText);
            TextView weatherEmojiHourly = itemView.findViewById(R.id.weatherEmojiHourly);
            TextView tempHourly = itemView.findViewById(R.id.tempHourly);

            hourText.setText(forecast.getHour());
            weatherEmojiHourly.setText(forecast.getWeatherIcon());
            tempHourly.setText(String.format("%.1f°C", forecast.getTemperature()));

            container.addView(itemView);
        }
    }

}
