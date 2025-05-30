package com.example.weatherforecast.ui.outfit;

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
import com.example.weatherforecast.data.DBHelper;
import com.example.weatherforecast.model.CurrentWeather;
import com.example.weatherforecast.model.DailyForecast;
import com.example.weatherforecast.model.HourlyForecast;
import com.example.weatherforecast.util.OutfitImageMapper;
import com.example.weatherforecast.model.OutfitRecommendation;
import com.example.weatherforecast.model.SavedOutfitEntry;
import com.example.weatherforecast.service.OutfitDisplayHelper;
import com.example.weatherforecast.ui.weather.LocationSuggestionTask;
import com.example.weatherforecast.util.NavigationManager;
import com.example.weatherforecast.ui.settings.SettingsActivity;
import com.example.weatherforecast.ui.forms.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *  Actividad principal del Outfit encargada de mostrar la recomendación de ropa
 */
public class OutfitActivity extends AppCompatActivity implements WeatherController.WeatherView {

    private static final String DEFAULT_CITY = "Palma de Mallorca"; // Ciudad por defecto

    private TextView locationText;
    private TextView weatherEmoji;
    private TextView temperatureText;
    private TextView weatherConditionText;
    private TextView weatherSummaryText;
    private TextView humidityText;
    private RadioGroup radioGroupStyle;
    private RadioButton radioSporty;
    private RadioButton radioCasual;
    private RadioButton radioFormal;
    private Button btnLoadOutfit;
    private CardView cardOutfit;
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

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra("username")) {
            username = getIntent().getStringExtra("username");
        } else {
            // Redireccionar a login si no hay usuario
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_outfit);
        // Recupera los datos de la ciudad enviados desde la actividad anterior
        if (getIntent().hasExtra("CITY_NAME")) {
            currentCity = getIntent().getStringExtra("CITY_NAME");
        } else {
            currentCity = DEFAULT_CITY; // Ciudad por defecto si no se especifica
        }

        // Verifica si debemos forzar la recarga
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

        // Actualiza ciudad si ha cambiado
        if (intent.hasExtra("CITY_NAME")) {
            String newCity = intent.getStringExtra("CITY_NAME");
            if (!currentCity.equals(newCity)) {
                currentCity = newCity;
                loadWeatherData();
            }
        }

        // Verifica si debemos forzar la recarga
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
        weatherSummaryText = findViewById(R.id.weatherSummaryText);
        humidityText = findViewById(R.id.humidityText);
        radioGroupStyle = findViewById(R.id.radioGroupStyle);
        radioSporty = findViewById(R.id.radioSporty);
        radioCasual = findViewById(R.id.radioCasual);
        radioFormal = findViewById(R.id.radioFormal);
        btnLoadOutfit = findViewById(R.id.btnLoadOutfit);
        cardOutfit = findViewById(R.id.cardOutfit);
        progressBar = findViewById(R.id.progressBar);

        btnChangeLocation = findViewById(R.id.btnChangeLocation);
        btnChangeLocation.setOnClickListener(v -> showLocationDialog());

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
                .load(R.drawable.cielo)
                .centerCrop()
                .into(backgroundGif);
    }

    private void initViewModel() {
        OutfitViewModelFactory factory = new OutfitViewModelFactory(this, username);
        outfitViewModel = new ViewModelProvider(this, factory).get(OutfitViewModel.class);

        // Lambda que se ejecuta cuando se actualiza el outfit
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
            intent.putExtra("username", username);
            startActivity(intent);
        });
    }

    private void setupUI() {
        setupToolbar();
        navigationManager.setupBottomNavigation();
        bottomNavigation.setSelectedItemId(R.id.nav_clothing);
    }

    // Listeners para los botones
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

        // Crear el adaptador con todas las categorías de prendas en imágenes
        OutfitCustomizeAdapter adapter = new OutfitCustomizeAdapter(this, currentOutfit, new OutfitImageMapper());
        rvOutfitItems.setAdapter(adapter);

        // Botones de diálogo
        Button btnApply = dialogView.findViewById(R.id.btnApplyCustomization);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelCustomization);

        AlertDialog dialog = builder.create();

        // Botón de aplicar cambios
        btnApply.setOnClickListener(v -> {
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
        CurrentWeather currentWeather = outfitViewModel.getCurrentWeather().getValue();

        DBHelper dbHelper = new DBHelper(this);
        boolean saved = dbHelper.saveOutfit(username, customOutfit, currentWeather, new Date());

        if (customOutfit != null && currentWeather != null) {
            if (saved) {
                Toast.makeText(this, "Outfit guardado correctamente", Toast.LENGTH_SHORT).show();
                btnSaveOutfit.setEnabled(false);
                isCustomized = false;
            } else {
                Toast.makeText(this, "Error al guardar el outfit", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método que carga los datos del clima
    private void loadWeatherData() {
        showLoading(true);

        // Handler que se ejecuta después de 200ms
        new Handler().postDelayed(() -> {
            if (weatherController == null) {
                weatherController = new WeatherController(this);
                weatherController.setView(this);
            }
            weatherController.loadWeatherData(currentCity);
        }, 200); // Pequeña demora para permitir que la UI se actualice
    }


    // Método para mostrar la recomendación de ropa
    private void showOutfitRecommendation(OutfitRecommendation recommendation) {
        cardOutfit.setVisibility(View.VISIBLE);

        // Si la recomendación es null, mostrar un mensaje y ocultar botones relacionados
        if (recommendation == null) {
            Toast.makeText(this, "No se pudo cargar el outfit", Toast.LENGTH_SHORT).show();
            btnViewRating.setVisibility(View.GONE);
            btnSaveOutfit.setEnabled(false);
            return;
        }

        // Mostramos las imágenes del outfit
        outfitDisplayHelper.displayOutfitWithImages(outfitImagesContainer, recommendation);

        // Mostrar botón para ver calificación
        btnViewRating.setVisibility(View.VISIBLE);
    }

    // Método para mostrar el diálogo de calificación
    private void showRatingDialog() {
        // Obtener el outfit actual
        OutfitRecommendation currentOutfit = outfitViewModel.getOutfitRecommendation().getValue();
        if (currentOutfit == null) return;

        // Calcular la puntuación de confort
        outfitViewModel.calculateComfortRating(currentOutfit);

        // Crear el layout para el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_outfit_rating, null);
        builder.setView(dialogView);

        // Configurar vistas del diálogo
        TextView tvRatingMessage = dialogView.findViewById(R.id.tvRatingMessage);
        ProgressBar progressComfort = dialogView.findViewById(R.id.progressComfort);
        LinearLayout outfitRatingImagesContainer = dialogView.findViewById(R.id.outfitRatingImagesContainer);
        Button btnCloseRating = dialogView.findViewById(R.id.btnCloseRating);

        // Puntuación y mensaje
        Integer comfortRating = outfitViewModel.getComfortRating().getValue();
        if (comfortRating != null) {
            progressComfort.setProgress(comfortRating);
        }

        // Mensaje de calificación
        String ratingMessage = outfitViewModel.getRatingMessage().getValue();
        if (ratingMessage != null) {
            tvRatingMessage.setText(ratingMessage);
        }

        // Imágenes del outfit
        outfitDisplayHelper.displayOutfitWithImages(outfitRatingImagesContainer, currentOutfit);

        AlertDialog dialog = builder.create();
        btnCloseRating.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void displayCurrentWeather(CurrentWeather weather) {
        // Este thread se encarga de actualizar la UI en el hilo principal
        runOnUiThread(() -> {
            // Actualizar la ciudad actual con la respuesta
            currentCity = weather.getLocation();

            SimpleDateFormat dateFormat = new SimpleDateFormat("'Hoy', HH:mm", new Locale("es", "ES"));
            String currentDateTime = dateFormat.format(new Date());

            locationText.setText(String.format("%s, %s", weather.getLocation(), weather.getCountry()));
            weatherEmoji.setText(weather.getWeatherIcon());
            temperatureText.setText(String.format("%.1f°C", weather.getTemperature()));
            weatherConditionText.setText(weather.getWeatherCondition());
            humidityText.setText(String.format("Humedad: %d%%", weather.getHumidity()));
            weatherSummaryText.setText(currentDateTime + " - " + weather.getSummary());

            // Actualizar el ViewModel con los datos meteorológicos
            outfitViewModel.setCurrentWeather(weather);

            cardOutfit.setVisibility(View.GONE);

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

    // Método para mostrar el diálogo de búsqueda de ubicación
    private void showLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_location_search, null);
        builder.setView(dialogView);

        AutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.autoCompleteLocation);
        ImageButton btnApply = dialogView.findViewById(R.id.btnApplyLocation);

        // Adapter para el autocompletado
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

        // TextWatcher para el autocompletado
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    // Buscar sugerencias de ciudades cuando hay al menos 3 carácteres
                    searchLocationSuggestions(s.toString(), adapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // Método para buscar sugerencias de ubicaciones
    private void searchLocationSuggestions(String query, ArrayAdapter<String> adapter) {
         new LocationSuggestionTask(adapter).execute(query);
    }

    private void loadSavedOutfit() {
        DBHelper dbHelper = new DBHelper(this);
        SavedOutfitEntry savedOutfit = dbHelper.getLatestOutfit(username);
        if (savedOutfit != null) {
            outfitViewModel.setOutfitRecommendation(savedOutfit.getOutfit());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (forceReload) {
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