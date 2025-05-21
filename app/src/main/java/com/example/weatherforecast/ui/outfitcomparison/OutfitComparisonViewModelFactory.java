package com.example.weatherforecast.ui.outfitcomparison;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Factory para crear instancias de OutfitComparisonViewModel
 */
public class OutfitComparisonViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public OutfitComparisonViewModelFactory(Context context) {
        this.context = context;
    }

    // Método para crear instancias de OutfitComparisonViewModel
    @NonNull
    @Override // <T> significa que la clase generada puede ser de cualquier tipo
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(OutfitComparisonViewModel.class)) {
            return (T) new OutfitComparisonViewModel(context);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}