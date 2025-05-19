package com.example.weatherforecast.ui.outfit;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class OutfitViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public OutfitViewModelFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(OutfitViewModel.class)) {
            return (T) new OutfitViewModel(context);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
