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

public class OutfitViewModel extends ViewModel {
    private final OutfitService outfitService;
    private final MutableLiveData<OutfitRecommendation> outfitRecommendation = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private CurrentWeather currentWeather;
    private OutfitRecommendation.Style selectedStyle = OutfitRecommendation.Style.CASUAL; // Estilo predeterminado
    private PreferencesRepository preferencesRepository;

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
}
