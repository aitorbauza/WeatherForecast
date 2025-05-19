package com.example.weatherforecast.ui.outfitcomparison;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.weatherforecast.R;
import com.example.weatherforecast.model.CurrentWeather;
import com.example.weatherforecast.model.OutfitRecommendation;
import com.example.weatherforecast.model.SavedOutfitEntry;
import com.example.weatherforecast.service.OutfitDisplayHelper;
import com.example.weatherforecast.ui.NavigationManager;
import com.example.weatherforecast.ui.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Actividad encargada de comparar outfits
 */
public class OutfitComparisonActivity extends AppCompatActivity {

    private OutfitComparisonViewModel viewModel;
    private BottomNavigationView bottomNavigation;
    private NavigationManager navigationManager;
    private String currentCity;

    // Primer Outfit
    private CardView cardFirstOutfit;
    private Button btnSelectFirstDate;
    private TextView tvFirstDate;
    private LinearLayout firstOutfitContainer;
    private TextView tvFirstWeatherInfo;

    // Segundo Outfit
    private CardView cardSecondOutfit;
    private Button btnCompare;
    private Button btnSelectSecondDate;
    private TextView tvSecondDate;
    private LinearLayout secondOutfitContainer;
    private TextView tvSecondWeatherInfo;
    private TextView tvComparisonResult;

    private OutfitDisplayHelper outfitDisplayHelper;

    private ImageButton btnSettings;
    private ImageView toolbarLogo;

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_comparison);

        if (getIntent().hasExtra("CITY_NAME")) {
            currentCity = getIntent().getStringExtra("CITY_NAME");
        }

        initViews();
        setupViewModel();
        setupListeners();
        setupNavigation();
        setupToolbar();

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        tvFirstDate.setText(dateFormatter.format(currentDate));

        viewModel.loadFirstOutfitByDate(currentDate, this);
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);

        cardFirstOutfit = findViewById(R.id.cardFirstOutfit);
        btnSelectFirstDate = findViewById(R.id.btnSelectFirstDate);
        tvFirstDate = findViewById(R.id.tvFirstDate);
        firstOutfitContainer = findViewById(R.id.firstOutfitContainer);
        tvFirstWeatherInfo = findViewById(R.id.tvFirstWeatherInfo);

        cardSecondOutfit = findViewById(R.id.cardSecondOutfit);
        cardSecondOutfit.setVisibility(View.GONE);
        btnCompare = findViewById(R.id.btnCompare);
        btnSelectSecondDate = findViewById(R.id.btnSelectSecondDate);
        tvSecondDate = findViewById(R.id.tvSecondDate);
        secondOutfitContainer = findViewById(R.id.secondOutfitContainer);
        tvSecondWeatherInfo = findViewById(R.id.tvSecondWeatherInfo);

        tvComparisonResult = findViewById(R.id.tvComparisonResult);
        tvComparisonResult.setVisibility(View.GONE);

        btnSettings = findViewById(R.id.btnSettings);
        toolbarLogo = findViewById(R.id.toolbarLogo);

        outfitDisplayHelper = new OutfitDisplayHelper(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        Glide.with(this)
                .load(R.drawable.logo)
                .into(toolbarLogo);
    }

    // Inicializa el ViewModel
    private void setupViewModel() {
        OutfitComparisonViewModelFactory factory = new OutfitComparisonViewModelFactory(this);
        viewModel = new ViewModelProvider(this, factory).get(OutfitComparisonViewModel.class);

        viewModel.getFirstOutfitEntry().observe(this, outfitEntry -> {
            if (outfitEntry != null) {
                displayFirstOutfit(outfitEntry);
            } else {
                showNoOutfitForDate(tvFirstDate.getText().toString(), firstOutfitContainer, tvFirstWeatherInfo);
            }
        });

        viewModel.getSecondOutfitEntry().observe(this, outfitEntry -> {
            if (outfitEntry != null) {
                displaySecondOutfit(outfitEntry);
                // Generate comparison when both outfits are loaded
                generateComparison();
            } else if (cardSecondOutfit.getVisibility() == View.VISIBLE) {
                showNoOutfitForDate(tvSecondDate.getText().toString(), secondOutfitContainer, tvSecondWeatherInfo);
                tvComparisonResult.setVisibility(View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Listener para los botones
    private void setupListeners() {
        btnSelectFirstDate.setOnClickListener(v -> showDatePickerDialog(true));

        btnSettings.setOnClickListener(v -> goToSettings());

        btnCompare.setOnClickListener(v -> {
            cardSecondOutfit.setVisibility(View.VISIBLE);
            // Segunda fecha: un día más tarde (default)
            Calendar calendar = Calendar.getInstance();
            try {
                Date firstDate = dateFormatter.parse(tvFirstDate.getText().toString());
                calendar.setTime(firstDate);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            } catch (Exception e) {
            }
            String defaultSecondDate = dateFormatter.format(calendar.getTime());
            tvSecondDate.setText(defaultSecondDate);

            try {
                Date secondDate = dateFormatter.parse(defaultSecondDate);
                viewModel.loadSecondOutfitByDate(secondDate, this);
            } catch (Exception e) {
                viewModel.setErrorMessage("Error al cargar la fecha: " + e.getMessage());
            }
        });

        btnSelectSecondDate.setOnClickListener(v -> showDatePickerDialog(false));
    }

    private void setupNavigation() {
        navigationManager = new NavigationManager(
                this,
                bottomNavigation,
                currentCity,
                NavigationManager.ActivityType.OUTFIT_COMPARISON
        );
        navigationManager.setupBottomNavigation();
    }

    private void goToSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    // Método encargado de mostrar el DatePicker (Calendario)
    private void showDatePickerDialog(boolean isFirstDate) {
        Calendar calendar = Calendar.getInstance();
        try {
            String currentDateText = isFirstDate ?
                    tvFirstDate.getText().toString() :
                    tvSecondDate.getText().toString();
            Date date = dateFormatter.parse(currentDateText);
            calendar.setTime(date);
        } catch (Exception e) {
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay);
                    String formattedDate = dateFormatter.format(selectedCalendar.getTime());

                    if (isFirstDate) {
                        tvFirstDate.setText(formattedDate);
                        viewModel.loadFirstOutfitByDate(selectedCalendar.getTime(), this);
                    } else {
                        tvSecondDate.setText(formattedDate);
                        viewModel.loadSecondOutfitByDate(selectedCalendar.getTime(), this);
                    }
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    // Muestra el primer outfit
    private void displayFirstOutfit(SavedOutfitEntry outfitEntry) {
        firstOutfitContainer.removeAllViews();

        outfitDisplayHelper.displayOutfitWithImages(firstOutfitContainer, outfitEntry.getOutfit());

        // Mostrar la información del clima
        CurrentWeather weather = outfitEntry.getWeather();
        displayWeatherInfo(weather, tvFirstWeatherInfo);

        // Mostrar la fecha guardada en lugar de la fecha seleccionada manualmente
        if (outfitEntry.getSavedDate() != null) {
            tvFirstDate.setText(dateFormatter.format(outfitEntry.getSavedDate()));
        }
    }

    // Muestra el segundo outfit (igual que displayFirstOutfit)
    private void displaySecondOutfit(SavedOutfitEntry outfitEntry) {
        secondOutfitContainer.removeAllViews();

        outfitDisplayHelper.displayOutfitWithImages(secondOutfitContainer, outfitEntry.getOutfit());

        CurrentWeather weather = outfitEntry.getWeather();
        displayWeatherInfo(weather, tvSecondWeatherInfo);

        // Mostrar la fecha guardada en el TextView correcto
        if (outfitEntry.getSavedDate() != null) {
            tvSecondDate.setText(dateFormatter.format(outfitEntry.getSavedDate()));
        }
    }

    // Muestra la información del clima
    private void displayWeatherInfo(CurrentWeather weather, TextView textView) {
        if (weather != null) {
            String weatherInfo = String.format(
                    "%s %s\nTemperatura: %.1f°C \nHumedad: %d%%",
                    weather.getLocation(),
                    weather.getWeatherIcon(),
                    weather.getTemperature(),
                    weather.getHumidity()
            );
            textView.setText(weatherInfo);
        } else {
            textView.setText("No hay información del clima disponible");
        }
    }

    // Muestra un mensaje si no hay un outfit guardado para la fecha
    private void showNoOutfitForDate(String date, LinearLayout container, TextView weatherInfo) {
        container.removeAllViews();
        TextView noOutfitText = new TextView(this);
        noOutfitText.setText("No hay outfit guardado para la fecha: " + date);
        noOutfitText.setPadding(16, 16, 16, 16);
        container.addView(noOutfitText);
        weatherInfo.setText("No hay información del clima disponible");
    }

    // Método encargado de generar la comparación entre outfits
    private void generateComparison() {
        SavedOutfitEntry firstEntry = viewModel.getFirstOutfitEntry().getValue();
        SavedOutfitEntry secondEntry = viewModel.getSecondOutfitEntry().getValue();

        if (firstEntry != null && secondEntry != null) {
            CurrentWeather firstWeather = firstEntry.getWeather();
            CurrentWeather secondWeather = secondEntry.getWeather();

            if (firstWeather != null && secondWeather != null) {

                // Calcula la diferencia de temperatura
                float tempDiff = (float) (secondWeather.getTemperature() - firstWeather.getTemperature());

                StringBuilder comparison = new StringBuilder();
                comparison.append("Comparación climatológica:\n\n");

                if (Math.abs(tempDiff) < 1) {
                    comparison.append("• Las temperaturas son similares en ambos días.\n");
                } else if (tempDiff > 0) {
                    comparison.append(String.format(Locale.getDefault(),
                            "• El segundo día es %.1f°C más caluroso que el primero.\n", Math.abs(tempDiff)));
                } else {
                    comparison.append(String.format(Locale.getDefault(),
                            "• El segundo día es %.1f°C más frío que el primero.\n", Math.abs(tempDiff)));
                }

                // Compara condiciones climáticas
                if (firstWeather.getWeatherCondition().equals(secondWeather.getWeatherCondition())) {
                    comparison.append("• Las condiciones climáticas son iguales: \n ")
                            .append(firstWeather.getWeatherCondition()).append(".\n");
                } else {
                    comparison.append("• Las condiciones climáticas son diferentes:\n  - Día 1: ")
                            .append(firstWeather.getWeatherCondition())
                            .append("\n  - Día 2: ")
                            .append(secondWeather.getWeatherCondition()).append("\n");
                }

                // Compara niveles de humedad
                int humidityDiff = secondWeather.getHumidity() - firstWeather.getHumidity();
                if (Math.abs(humidityDiff) < 5) {
                    comparison.append("• Los niveles de humedad son similares en ambos días.\n");
                } else if (humidityDiff > 0) {
                    comparison.append(String.format(Locale.getDefault(),
                            "• El segundo día tiene %d%% más de humedad que el primero.\n", Math.abs(humidityDiff)));
                } else {
                    comparison.append(String.format(Locale.getDefault(),
                            "• El segundo día tiene %d%% menos de humedad que el primero.\n", Math.abs(humidityDiff)));
                }

                // Compara outfit
                OutfitRecommendation firstOutfit = firstEntry.getOutfit();
                OutfitRecommendation secondOutfit = secondEntry.getOutfit();

                comparison.append("\nComparación de outfits:\n");

                // Compara estilos (deportivo, casual, formal)
                if (firstOutfit.getStyle() == secondOutfit.getStyle()) {
                    comparison.append("• Ambos outfits son de estilo ")
                            .append(getStyleInSpanish(firstOutfit.getStyle())).append(".\n");
                } else {
                    comparison.append("• Los estilos son diferentes:\n  - Día 1: ")
                            .append(getStyleInSpanish(firstOutfit.getStyle()))
                            .append("\n  - Día 2: ")
                            .append(getStyleInSpanish(secondOutfit.getStyle())).append("\n");
                }

                // Otros...
                if (tempDiff > 5) {
                    comparison.append("• El segundo outfit debería ser más ligero debido a la mayor temperatura.\n");
                } else if (tempDiff < -5) {
                    comparison.append("• El segundo outfit debería incluir más abrigo debido a la menor temperatura.\n");
                }

                tvComparisonResult.setText(comparison.toString());
                tvComparisonResult.setVisibility(View.VISIBLE);
            } else {
                tvComparisonResult.setText("No hay suficiente información para generar una comparación.");
                tvComparisonResult.setVisibility(View.VISIBLE);
            }
        }
    }

    // Método auxiliar para obtener el nombre del estilo en español
    private String getStyleInSpanish(OutfitRecommendation.Style style) {
        switch (style) {
            case CASUAL: return "Casual";
            case FORMAL: return "Formal";
            case SPORTY: return "Deportivo";
            default: return "Desconocido";
        }
    }
}
