package com.example.weatherforecast.ui.outfitcomparison;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.weatherforecast.data.DBHelper;
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

    private String username;

    private final PreferencesRepository preferencesRepository;

    public OutfitComparisonViewModel(Context context) {
        this.preferencesRepository = new PreferencesRepository(context, username);
    }

    // Método para cargar el primer outfit por fecha
    public void loadFirstOutfitByDate(Date date, Context context) {
        DBHelper dbHelper = new DBHelper(context);
        SavedOutfitEntry outfitEntry = dbHelper.getOutfitByDate(username, date);

        if (outfitEntry != null) {
            firstOutfitEntry.setValue(outfitEntry);
        } else {
            firstOutfitEntry.setValue(null);
            errorMessage.setValue("No hay outfit guardado para esta fecha");
        }
    }

    // Igual que loadFirstOutfitByDate pero para el segundo outfit
    public void loadSecondOutfitByDate(Date date, Context context) {
        DBHelper dbHelper = new DBHelper(context);
        SavedOutfitEntry outfitEntry = dbHelper.getOutfitByDate(username, date);

        if (outfitEntry != null) {
            secondOutfitEntry.setValue(outfitEntry);
        } else {
            secondOutfitEntry.setValue(null);
            errorMessage.setValue("No hay outfit guardado para esta fecha");
        }
    }

    // Getters y Setters
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
    public void setUsername(String username) {
        this.username = username;
    }
}