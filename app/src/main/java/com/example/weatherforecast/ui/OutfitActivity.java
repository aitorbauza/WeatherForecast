package com.example.weatherforecast.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherforecast.R;
import com.example.weatherforecast.controller.WeatherController;
import com.example.weatherforecast.model.CurrentWeather;
import com.example.weatherforecast.model.DailyForecast;
import com.example.weatherforecast.model.HourlyForecast;
import com.example.weatherforecast.model.OutfitImageMapper;
import com.example.weatherforecast.model.OutfitRecommendation;
import com.example.weatherforecast.model.UserPreferences;
import com.example.weatherforecast.service.OutfitDisplayHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OutfitActivity extends AppCompatActivity implements WeatherController.WeatherView {

    private static final String DEFAULT_CITY = "Palma de Mallorca"; // Ciudad por defecto, igual que en MainActivity

    private TextView locationText;
    private TextView weatherEmoji;
    private TextView temperatureText;
    private TextView weatherConditionText;
    private TextView maxTempText;
    private TextView minTempText;
    private TextView weatherSummaryText;
    private TextView humidityText;
    private RadioGroup radioGroupStyle;
    private RadioButton radioSporty;
    private RadioButton radioCasual;
    private RadioButton radioFormal;
    private Button btnLoadOutfit;
    private CardView cardOutfit;
    private TextView tvOutfitRecommendation;
    private ProgressBar progressBar;

    private WeatherController weatherController;
    private OutfitViewModel outfitViewModel;
    private String currentCity;
    private boolean forceReload = false;  // Añadido

    private ImageView backgroundGif;
    private ImageButton btnChangeLocation;
    private ImageButton btnSettings;
    private ImageView toolbarLogo;
    private BottomNavigationView bottomNavigation;

    private NavigationManager navigationManager;

    private Button btnCustomizeOutfit;
    private Button btnSaveOutfit;
    private boolean isCustomized = false;

    private OutfitDisplayHelper outfitDisplayHelper;
    private LinearLayout outfitImagesContainer;

    private Button btnViewRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit);

        // Recuperar los datos de la ciudad enviados desde la actividad anterior
        if (getIntent().hasExtra("CITY_NAME")) {
            currentCity = getIntent().getStringExtra("CITY_NAME");
        } else {
            currentCity = DEFAULT_CITY; // Ciudad por defecto si no se especifica
        }

        // Verificar si debemos forzar la recarga
        forceReload = NavigationManager.shouldForceReload(getIntent());

        initViews();
        initViewModel();
        setupListeners();
        setupUI();
        loadWeatherData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Actualizar ciudad si ha cambiado
        if (intent.hasExtra("CITY_NAME")) {
            String newCity = intent.getStringExtra("CITY_NAME");
            if (!currentCity.equals(newCity)) {
                currentCity = newCity;
                loadWeatherData();
            }
        }

        // Verificar si debemos forzar la recarga
        if (NavigationManager.shouldForceReload(intent)) {
            loadWeatherData();
        }
    }

    private void initViews() {
        backgroundGif = findViewById(R.id.backgroundGif);
        locationText = findViewById(R.id.locationText);
        weatherEmoji = findViewById(R.id.weatherEmoji);
        temperatureText = findViewById(R.id.temperatureText);
        weatherConditionText = findViewById(R.id.weatherConditionText);
        maxTempText = findViewById(R.id.maxTempText);
        minTempText = findViewById(R.id.minTempText);
        weatherSummaryText = findViewById(R.id.weatherSummaryText);
        humidityText = findViewById(R.id.humidityText);
        radioGroupStyle = findViewById(R.id.radioGroupStyle);
        radioSporty = findViewById(R.id.radioSporty);
        radioCasual = findViewById(R.id.radioCasual);
        radioFormal = findViewById(R.id.radioFormal);
        btnLoadOutfit = findViewById(R.id.btnLoadOutfit);
        cardOutfit = findViewById(R.id.cardOutfit);
        progressBar = findViewById(R.id.progressBar);

        // Botón para cambiar ubicación
        btnChangeLocation = findViewById(R.id.btnChangeLocation);
        btnChangeLocation.setOnClickListener(v -> showLocationDialog());

        // Nuevos elementos UI
        btnSettings = findViewById(R.id.btnSettings);
        toolbarLogo = findViewById(R.id.toolbarLogo);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        btnCustomizeOutfit = findViewById(R.id.btnCustomizeOutfit);
        btnSaveOutfit = findViewById(R.id.btnSaveOutfit);

        btnSaveOutfit.setEnabled(false);

        outfitImagesContainer = findViewById(R.id.outfitImagesContainer);
        outfitDisplayHelper = new OutfitDisplayHelper(this);

        btnViewRating = findViewById(R.id.btnViewRating);
        btnViewRating.setVisibility(View.GONE);

        navigationManager = new NavigationManager(
                this,
                bottomNavigation,
                currentCity,
                NavigationManager.ActivityType.OUTFIT
        );

        // Carga el fondo GIF
        Glide.with(this)
                .asGif()
                .load(R.drawable.cielo) // Asegúrate de tener este GIF en res/drawable
                .centerCrop()
                .into(backgroundGif);
    }

    private void initViewModel() {
        OutfitViewModelFactory factory = new OutfitViewModelFactory(this);
        outfitViewModel = new ViewModelProvider(this, factory).get(OutfitViewModel.class);

        outfitViewModel.getOutfitRecommendation().observe(this, this::showOutfitRecommendation);

        outfitViewModel.isLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        outfitViewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Toolbar
        Glide.with(this)
                .load(R.drawable.logo)
                .into(toolbarLogo);

        // Botón de ajustes
        btnSettings.setOnClickListener(v -> {
            Toast.makeText(OutfitActivity.this, "Configuración", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void setupUI() {
        setupToolbar();
        navigationManager.setupBottomNavigation();
        bottomNavigation.setSelectedItemId(R.id.nav_clothing);
    }

    private void setupListeners() {
        radioGroupStyle.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioSporty) {
                outfitViewModel.setSelectedStyle(OutfitRecommendation.Style.SPORTY);
            } else if (checkedId == R.id.radioFormal) {
                outfitViewModel.setSelectedStyle(OutfitRecommendation.Style.FORMAL);
            } else {
                outfitViewModel.setSelectedStyle(OutfitRecommendation.Style.CASUAL);
            }
        });

        btnLoadOutfit.setOnClickListener(v -> {
            outfitViewModel.loadOutfitRecommendation();
        });

        btnCustomizeOutfit.setOnClickListener(v -> {
            showCustomizeDialog();
        });

        btnSaveOutfit.setOnClickListener(v -> {
            saveCustomizedOutfit();
        });

        btnViewRating.setOnClickListener(v -> {
            showRatingDialog();
        });
    }

    // Método para mostrar el dialog de personalización
    private void showCustomizeDialog() {
        // Obtenemos la recomendación actual
        OutfitRecommendation currentOutfit = outfitViewModel.getOutfitRecommendation().getValue();
        if (currentOutfit == null) return;

        // Crear el layout para el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_customize_outfit, null);
        builder.setView(dialogView);

        // Configurar el RecyclerView para mostrar los items del outfit
        RecyclerView rvOutfitItems = dialogView.findViewById(R.id.rvOutfitItems);
        rvOutfitItems.setLayoutManager(new LinearLayoutManager(this));

        // Crear el adaptador con todas las categorías de prendas
        // Adaptador modificado para usar imágenes en lugar de texto
        OutfitCustomizeAdapter adapter = new OutfitCustomizeAdapter(this, currentOutfit, new OutfitImageMapper());
        rvOutfitItems.setAdapter(adapter);

        // Botones del diálogo
        Button btnApply = dialogView.findViewById(R.id.btnApplyCustomization);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelCustomization);

        AlertDialog dialog = builder.create();

        // Configurar acciones de los botones
        btnApply.setOnClickListener(v -> {
            // Aplicar cambios de personalización
            OutfitRecommendation customizedOutfit = adapter.getCustomizedOutfit();
            outfitViewModel.setCustomizedOutfit(customizedOutfit);
            showOutfitRecommendation(customizedOutfit);

            // Activar el botón de guardar
            btnSaveOutfit.setEnabled(true);
            isCustomized = true;

            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Método para guardar el outfit personalizado
    private void saveCustomizedOutfit() {
        if (!isCustomized) return;

        OutfitRecommendation customOutfit = outfitViewModel.getOutfitRecommendation().getValue();
        if (customOutfit != null) {
            // Usar el repositorio para guardar
            boolean saved = outfitViewModel.saveCustomizedOutfit(customOutfit, this);

            if (saved) {
                Toast.makeText(this, "Outfit guardado correctamente", Toast.LENGTH_SHORT).show();
                btnSaveOutfit.setEnabled(false);
                isCustomized = false;
            } else {
                Toast.makeText(this, "Error al guardar el outfit", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadWeatherData() {
        showLoading(true);

        // Añadir pequeña demora para permitir que la UI se actualice
        new Handler().postDelayed(() -> {
            if (weatherController == null) {
                weatherController = new WeatherController(this);
                weatherController.setView(this);
            }
            weatherController.loadWeatherData(currentCity);
        }, 100);
    }

    private void showOutfitRecommendation(OutfitRecommendation recommendation) {
        cardOutfit.setVisibility(View.VISIBLE);

        // Mostramos las imágenes del outfit
        outfitDisplayHelper.displayOutfitWithImages(outfitImagesContainer, recommendation);

        btnViewRating.setVisibility(View.VISIBLE);
    }

    // Método para mostrar el diálogo de calificación
    private void showRatingDialog() {
        // Obtener el outfit actual
        OutfitRecommendation currentOutfit = outfitViewModel.getOutfitRecommendation().getValue();
        if (currentOutfit == null) return;

        // Calcular la puntuación de confort
        outfitViewModel.calculateComfortRating(currentOutfit);

        // Crear el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_outfit_rating, null);
        builder.setView(dialogView);

        // Configurar vistas del diálogo
        TextView tvRatingMessage = dialogView.findViewById(R.id.tvRatingMessage);
        ProgressBar progressComfort = dialogView.findViewById(R.id.progressComfort);
        LinearLayout outfitRatingImagesContainer = dialogView.findViewById(R.id.outfitRatingImagesContainer);
        Button btnCloseRating = dialogView.findViewById(R.id.btnCloseRating);

        // Mostrar la puntuación y el mensaje
        Integer comfortRating = outfitViewModel.getComfortRating().getValue();
        if (comfortRating != null) {
            progressComfort.setProgress(comfortRating);
        }

        String ratingMessage = outfitViewModel.getRatingMessage().getValue();
        if (ratingMessage != null) {
            tvRatingMessage.setText(ratingMessage);
        }

        // Mostrar las imágenes del outfit
        outfitDisplayHelper.displayOutfitWithImages(outfitRatingImagesContainer, currentOutfit);

        AlertDialog dialog = builder.create();

        // Configurar el botón de cerrar
        btnCloseRating.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void displayCurrentWeather(CurrentWeather weather) {
        runOnUiThread(() -> {
            // Actualizar la ciudad actual con la respuesta
            currentCity = weather.getLocation();

            // Formato para "Hoy, HH:mm"
            SimpleDateFormat dateFormat = new SimpleDateFormat("'Hoy', HH:mm", new Locale("es", "ES"));
            String currentDateTime = dateFormat.format(new Date());

            locationText.setText(String.format("%s, %s", weather.getLocation(), weather.getCountry()));
            weatherEmoji.setText(weather.getWeatherIcon());
            temperatureText.setText(String.format("%.1f°C", weather.getTemperature()));
            weatherConditionText.setText(weather.getWeatherCondition());
            maxTempText.setText(String.format("Máx: %.1f°C", weather.getMaxTemperature()));
            minTempText.setText(String.format("Mín: %.1f°C", weather.getMinTemperature()));
            humidityText.setText(String.format("%d%%", weather.getHumidity()));
            weatherSummaryText.setText(currentDateTime + " - " + weather.getSummary());

            // Actualizar el ViewModel con los datos meteorológicos
            outfitViewModel.setCurrentWeather(weather);

            // Ocultar indicador de carga
            showLoading(false);
        });
    }

    @Override
    public void displayHourlyForecast(List<HourlyForecast> forecasts) {
        // No utilizado en esta pantalla
    }

    @Override
    public void displayDailyForecast(List<DailyForecast> forecasts) {
        // No utilizado en esta pantalla
    }

    @Override
    public void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            // Ocultar indicador de carga en caso de error
            showLoading(false);
        });
    }

    @Override
    public void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void showLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_location_search, null);
        builder.setView(dialogView);

        AutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.autoCompleteLocation);
        ImageButton btnApply = dialogView.findViewById(R.id.btnApplyLocation);

        // Adaptador para el autocompletado (usamos un adaptador simple por ahora)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>());
        autoCompleteTextView.setAdapter(adapter);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Configurar el botón aplicar
        btnApply.setOnClickListener(v -> {
            String newLocation = autoCompleteTextView.getText().toString().trim();
            if (!newLocation.isEmpty()) {
                currentCity = newLocation;
                weatherController.loadWeatherData(newLocation);
                dialog.dismiss();
            } else {
                Toast.makeText(OutfitActivity.this, "Por favor, introduce una ubicación", Toast.LENGTH_SHORT).show();
            }
        });

        // Configura un TextWatcher para el autocompletado
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    // Buscar sugerencias de ciudades cuando hay al menos 3 caracteres
                    searchLocationSuggestions(s.toString(), adapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Método para buscar sugerencias de ubicaciones
    private void searchLocationSuggestions(String query, ArrayAdapter<String> adapter) {
         new LocationSuggestionTask(adapter).execute(query);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Intentar cargar outfit guardado si no estamos forzando recarga
        if (!forceReload) {
            boolean outfitLoaded = outfitViewModel.loadSavedOutfit(this);

            // Si no hay outfit guardado o si se forzó la recarga, cargar datos del clima
            if (!outfitLoaded && forceReload) {
                forceReload = false;
                loadWeatherData();
            }
        } else {
            // Si se forzó la recarga, cargar datos del clima
            forceReload = false;
            loadWeatherData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (weatherController != null) {
            weatherController.onDestroy();
        }
    }
}