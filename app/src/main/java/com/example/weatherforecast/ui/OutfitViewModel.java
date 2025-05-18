package com.example.weatherforecast.ui;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.weatherforecast.model.UserPreferences;
import com.example.weatherforecast.repository.PreferencesRepository;
import com.example.weatherforecast.service.OutfitService;
import com.example.weatherforecast.model.CurrentWeather;
import com.example.weatherforecast.model.OutfitRecommendation;

import java.util.ArrayList;
import java.util.List;

public class OutfitViewModel extends ViewModel {
    private final OutfitService outfitService;
    private final MutableLiveData<OutfitRecommendation> outfitRecommendation = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private CurrentWeather currentWeather;
    private OutfitRecommendation.Style selectedStyle = OutfitRecommendation.Style.CASUAL; // Estilo predeterminado
    private PreferencesRepository preferencesRepository;

    private OutfitRecommendation originalOutfit;
    private final MutableLiveData<Integer> comfortRating = new MutableLiveData<>(100);
    private final MutableLiveData<String> ratingMessage = new MutableLiveData<>("Buena elección, tu confort con este outfit será de un 100%");


    public OutfitViewModel(Context context) {
        this.outfitService = new OutfitService();
        this.preferencesRepository = new PreferencesRepository(context);
    }

    public LiveData<OutfitRecommendation> getOutfitRecommendation() {
        return outfitRecommendation;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setCurrentWeather(CurrentWeather weather) {
        this.currentWeather = weather;
    }

    public void setSelectedStyle(OutfitRecommendation.Style style) {
        this.selectedStyle = style;
    }

    public OutfitRecommendation.Style getSelectedStyle() {
        return selectedStyle;
    }

    /**
     * Genera una recomendación de outfit según el clima actual y el estilo seleccionado
     */
    public void loadOutfitRecommendation() {
        if (currentWeather == null) {
            errorMessage.setValue("No hay datos meteorológicos disponibles");
            return;
        }

        isLoading.setValue(true);
        try {
            // Obtener las preferencias del usuario
            UserPreferences userPreferences = preferencesRepository.getUserPreferences();

            // Pasar las preferencias al servicio
            OutfitRecommendation recommendation =
                    outfitService.getOutfitRecommendation(currentWeather, selectedStyle, userPreferences);

            // Guardar el outfit original para comparar más tarde
            saveOriginalOutfit(recommendation);

            outfitRecommendation.setValue(recommendation);
        } catch (Exception e) {
            errorMessage.setValue("Error al generar recomendación: " + e.getMessage());
        } finally {
            isLoading.setValue(false);
        }
    }

    public void setCustomizedOutfit(OutfitRecommendation customizedOutfit) {
        outfitRecommendation.setValue(customizedOutfit);
    }

    // Método para cargar outfit guardado
    public boolean loadSavedOutfit(Context context) {

        OutfitRecommendation savedOutfit = preferencesRepository.getSavedOutfit(context);
        if (savedOutfit != null) {
            outfitRecommendation.setValue(savedOutfit);
            return true;
        }
        return false;
    }

    public boolean saveCustomizedOutfit(OutfitRecommendation outfit, Context context) {
        try {
            preferencesRepository.saveOutfit(outfit, context);
            return true;
        } catch (Exception e) {
            errorMessage.setValue("Error al guardar outfit: " + e.getMessage());
            return false;
        }
    }

    // Getter para la puntuación de confort
    public LiveData<Integer> getComfortRating() {
        return comfortRating;
    }

    // Getter para el mensaje de calificación
    public LiveData<String> getRatingMessage() {
        return ratingMessage;
    }

    // Guarda el outfit original generado
    public void saveOriginalOutfit(OutfitRecommendation outfit) {
        this.originalOutfit = new OutfitRecommendation(
                new ArrayList<>(outfit.getTopItems()),
                new ArrayList<>(outfit.getBottomItems()),
                new ArrayList<>(outfit.getFootwear()),
                new ArrayList<>(outfit.getOuterWear()),
                new ArrayList<>(outfit.getAccessories()),
                outfit.getStyle()
        );
    }

    // Calcula la puntuación de confort basada en las diferencias con el outfit original
    public void calculateComfortRating(OutfitRecommendation currentOutfit) {
        if (originalOutfit == null) {
            comfortRating.setValue(100);
            ratingMessage.setValue("Buena elección, tu confort con este outfit será de un 100%");
            return;
        }

        int differences = 0;

        // Compara prendas superiores
        if (!compareItems(originalOutfit.getTopItems(), currentOutfit.getTopItems())) {
            differences++;
        }

        // Compara prendas inferiores
        if (!compareItems(originalOutfit.getBottomItems(), currentOutfit.getBottomItems())) {
            differences++;
        }

        // Compara calzado
        if (!compareItems(originalOutfit.getFootwear(), currentOutfit.getFootwear())) {
            differences++;
        }

        // Compara prendas exteriores
        if (!compareItems(originalOutfit.getOuterWear(), currentOutfit.getOuterWear())) {
            differences++;
        }

        // Compara accesorios
        if (!compareItems(originalOutfit.getAccessories(), currentOutfit.getAccessories())) {
            differences++;
        }

        // Calcula el porcentaje (cada diferencia resta un 20%)
        int rating = Math.max(0, 100 - (differences * 20));
        comfortRating.setValue(rating);

        // Actualiza el mensaje según la puntuación
        updateRatingMessage(rating);
    }

    // Compara listas de prendas
    private boolean compareItems(List<String> original, List<String> current) {
        if (original.size() != current.size()) {
            return false;
        }

        for (int i = 0; i < original.size(); i++) {
            if (!original.get(i).equals(current.get(i))) {
                return false;
            }
        }

        return true;
    }

    // Actualiza el mensaje según la puntuación
    private void updateRatingMessage(int rating) {
        String message;

        if (rating >= 80) {
            message = "¡Excelente elección! Tu confort con este outfit será de un " + rating + "%";
        } else if (rating >= 60) {
            message = "Buena elección. Tu confort con este outfit será de un " + rating + "%";
        } else if (rating >= 40) {
            message = "Elección aceptable. Tu confort con este outfit será de un " + rating + "%";
        } else if (rating >= 20) {
            message = "Podrías reconsiderar algunas prendas. Tu confort será de un " + rating + "%";
        } else {
            message = "Este outfit podría no ser muy confortable. Tu confort será de un " + rating + "%";
        }

        ratingMessage.setValue(message);
    }
}
