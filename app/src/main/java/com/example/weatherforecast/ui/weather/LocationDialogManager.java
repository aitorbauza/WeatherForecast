package com.example.weatherforecast.ui.weather;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.weatherforecast.R;

import java.util.ArrayList;

/**
 * Manager encargado de gestionar la visualización del diálogo de búsqueda de ubicación
 */
public class LocationDialogManager {
    private final Context context;
    private final OnLocationSelectedListener listener;

    public interface OnLocationSelectedListener {
        void onLocationSelected(String location);
    }

    public LocationDialogManager(Context context, OnLocationSelectedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    // Método que muestra el diálogo de búsqueda de ubicación
    public void showLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_location_search, null);
        builder.setView(dialogView);

        AutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.autoCompleteLocation);
        ImageButton btnApply = dialogView.findViewById(R.id.btnApplyLocation);

        // Adapter para mostrar las sugerencias de ubicación
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>());
        autoCompleteTextView.setAdapter(adapter);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnApply.setOnClickListener(v -> {
            String newLocation = autoCompleteTextView.getText().toString().trim();
            if (!newLocation.isEmpty()) {
                listener.onLocationSelected(newLocation);
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Por favor, introduce una ubicación", Toast.LENGTH_SHORT).show();
            }
        });

        // TextWatcher para manejar los cambios en el campo de texto
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    // En el momento en el que hay más de 2 caracteres, se realiza la búsqueda
                    searchLocationSuggestions(s.toString(), adapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Método para buscar sugerencias de ubicación
    private void searchLocationSuggestions(String query, ArrayAdapter<String> adapter) {
        new LocationSuggestionTask(adapter).execute(query);
    }
}