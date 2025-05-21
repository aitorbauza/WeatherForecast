package com.example.weatherforecast.ui.outfit;

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
import java.util.Date;
import java.util.List;

/**
 * ViewModel encargado de gestionar la lógica de la aplicación relacionada con la recomendación de outfit
 */
public class OutfitViewModel extends ViewModel {
    private final OutfitService outfitService;
    private final MutableLiveData<OutfitRecommendation> outfitRecommendation = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<CurrentWeather> currentWeather = new MutableLiveData<>();
    private OutfitRecommendation.Style selectedStyle = OutfitRecommendation.Style.CASUAL; // Estilo predeterminado
    private PreferencesRepository preferencesRepository;

    private OutfitRecommendation originalOutfit;
    // Variables para la puntuación de confort
    //MutableLiveData sirve para notificar a los observadores cuando cambia el valor
    private final MutableLiveData<Integer> comfortRating = new MutableLiveData<>(100);
    private final MutableLiveData<String> ratingMessage = new MutableLiveData<>("Buena elección, tu confort con este outfit será de un 100%");

    public OutfitViewModel(Context context, String username) {
        this.outfitService = new OutfitService();
        this.preferencesRepository = new PreferencesRepository(context, username);
        loadSavedOutfit(context);
    }

    public LiveData<OutfitRecommendation> getOutfitRecommendation() {
    return outfitRecommendation;
    }
    public void setOutfitRecommendation(OutfitRecommendation recommendation) {
        outfitRecommendation.setValue(recommendation);
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public LiveData<CurrentWeather> getCurrentWeather() {
        return currentWeather;
    }

    public void setCurrentWeather(CurrentWeather weather) {
        currentWeather.setValue(weather);
    }
    public void setSelectedStyle(OutfitRecommendation.Style style) {
        this.selectedStyle = style;
    }

    public OutfitRecommendation.Style getSelectedStyle() {
        return selectedStyle;
    }
    public void setCustomizedOutfit(OutfitRecommendation customizedOutfit) {
        outfitRecommendation.setValue(customizedOutfit);
    }

    // Getter para la puntuación de confort
    public LiveData<Integer> getComfortRating() {
        return comfortRating;
    }

    // Getter para el mensaje de calificación
    public LiveData<String> getRatingMessage() {
        return ratingMessage;
    }

    // Método que se encarga de cargar la recomendación de outfit
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
                    outfitService.getOutfitRecommendation(currentWeather.getValue(), selectedStyle, userPreferences);

            // Guardar el outfit original para comparar más tarde
            saveOriginalOutfit(recommendation);

            outfitRecommendation.setValue(recommendation);
        } catch (Exception e) {
            errorMessage.setValue("Error al generar recomendación: " + e.getMessage());
        } finally {
            isLoading.setValue(false);
        }
    }

    // Método para cargar outfit guardado
    public boolean loadSavedOutfit(Context context) {
        // Aquí solo verificamos si existe un outfit guardado, pero no lo mostramos
        OutfitRecommendation savedOutfit = preferencesRepository.getSavedOutfit(context);
        return savedOutfit != null;
        // No hacemos: outfitRecommendation.setValue(savedOutfit);
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

        // Por cada prenda diferente a la inicial, baja un 20%
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
            message = "¡Excelente elección! \n Tu confort con este outfit será de un " + rating + "%";
        } else if (rating >= 60) {
            message = "Buena elección. \n Tu confort con este outfit será de un " + rating + "%";
        } else if (rating >= 40) {
            message = "Elección aceptable. \n Tu confort con este outfit será de un " + rating + "%";
        } else if (rating >= 20) {
            message = "Podrías reconsiderar algunas prendas. \n Tu confort será de un " + rating + "%";
        } else {
            message = "Este outfit podría no ser muy confortable. \n Tu confort será de un " + rating + "%";
        }

        ratingMessage.setValue(message);
    }

}
