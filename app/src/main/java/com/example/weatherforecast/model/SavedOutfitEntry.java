package com.example.weatherforecast.model;

import java.util.Date;

/**
 * Clase que representa un outfit guardado junto con la información meteorológica
 * y la fecha en que se guardó
 */
public class SavedOutfitEntry {
    private final OutfitRecommendation outfit;
    private final CurrentWeather weather;
    private final Date savedDate;

    public SavedOutfitEntry(OutfitRecommendation outfit, CurrentWeather weather, Date savedDate) {
        this.outfit = outfit;
        this.weather = weather;
        this.savedDate = savedDate;
    }

    public OutfitRecommendation getOutfit() {
        return outfit;
    }
    public CurrentWeather getWeather() {
        return weather;
    }
    public Date getSavedDate() {
        return savedDate;
    }

}