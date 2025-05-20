package com.example.weatherforecast.ui.outfit;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Factory para crear instancias de OutfitViewModel con los parámetros necesarios.
 * Esta clase permite pasar parámetros al ViewModel durante su creación.
 */
public class OutfitViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;
    private final String username;

    public OutfitViewModelFactory(Context context, String username) {
        this.context = context;
        this.username = username;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(OutfitViewModel.class)) {
            return (T) new OutfitViewModel(context, username);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}