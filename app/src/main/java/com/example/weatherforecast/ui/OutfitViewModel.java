package com.example.weatherforecast.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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

    public OutfitViewModel() {
        this.outfitService = new OutfitService();
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
            OutfitRecommendation recommendation = outfitService.getOutfitRecommendation(currentWeather, selectedStyle);
            outfitRecommendation.setValue(recommendation);
        } catch (Exception e) {
            errorMessage.setValue("Error al generar recomendación: " + e.getMessage());
        } finally {
            isLoading.setValue(false);
        }
    }
}
