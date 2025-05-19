package com.example.weatherforecast.ui.outfitcomparison;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.weatherforecast.model.SavedOutfitEntry;
import com.example.weatherforecast.repository.PreferencesRepository;

import java.util.Date;

/**
 * ViewModel que maneja la lógica de la comparación de outfits
 */
public class OutfitComparisonViewModel extends ViewModel {

    private final MutableLiveData<SavedOutfitEntry> firstOutfitEntry = new MutableLiveData<>();
    private final MutableLiveData<SavedOutfitEntry> secondOutfitEntry = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final PreferencesRepository preferencesRepository;

    public OutfitComparisonViewModel(Context context) {
        this.preferencesRepository = new PreferencesRepository(context);
    }

    // Método para cargar el primer outfit por fecha
    public void loadFirstOutfitByDate(Date date, Context context) {
        try {
            SavedOutfitEntry outfitEntry = preferencesRepository.getOutfitByDate(date, context);
            firstOutfitEntry.setValue(outfitEntry);
        } catch (Exception e) {
            errorMessage.setValue("Error al cargar outfit: " + e.getMessage());
        }
    }

    // Método para cargar el segundo outfit por fecha
    public void loadSecondOutfitByDate(Date date, Context context) {
        try {
            SavedOutfitEntry outfitEntry = preferencesRepository.getOutfitByDate(date, context);
            secondOutfitEntry.setValue(outfitEntry);
        } catch (Exception e) {
            errorMessage.setValue("Error al cargar outfit de comparación: " + e.getMessage());
        }
    }

    // Getters
    public LiveData<SavedOutfitEntry> getFirstOutfitEntry() {
        return firstOutfitEntry;
    }
    public LiveData<SavedOutfitEntry> getSecondOutfitEntry() {
        return secondOutfitEntry;
    }
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(String message) {
        errorMessage.setValue(message);
    }

}