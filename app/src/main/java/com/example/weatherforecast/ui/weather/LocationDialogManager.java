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
import com.example.weatherforecast.ui.LocationSuggestionTask;

import java.util.ArrayList;

/**
 * Component responsible for managing location search dialog.
 * Follows Single Responsibility Principle by handling only location dialog logic.
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

    /**
     * Shows dialog for location search with autocomplete functionality
     */
    public void showLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_location_search, null);
        builder.setView(dialogView);

        AutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.autoCompleteLocation);
        ImageButton btnApply = dialogView.findViewById(R.id.btnApplyLocation);

        // Adapter for autocomplete
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>());
        autoCompleteTextView.setAdapter(adapter);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Configure apply button
        btnApply.setOnClickListener(v -> {
            String newLocation = autoCompleteTextView.getText().toString().trim();
            if (!newLocation.isEmpty()) {
                listener.onLocationSelected(newLocation);
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Por favor, introduce una ubicación", Toast.LENGTH_SHORT).show();
            }
        });

        // Configure text watcher for autocomplete
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    // Search for city suggestions when there are at least 3 characters
                    searchLocationSuggestions(s.toString(), adapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Search for location suggestions based on query
     */
    private void searchLocationSuggestions(String query, ArrayAdapter<String> adapter) {
        new LocationSuggestionTask(adapter).execute(query);
    }
}