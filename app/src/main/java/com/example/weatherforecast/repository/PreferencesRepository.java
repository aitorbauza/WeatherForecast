package com.example.weatherforecast.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.weatherforecast.model.OutfitRecommendation;
import com.example.weatherforecast.model.UserPreferences;
import com.google.gson.Gson;

/**
 * Clase encargada de gestionar el almacenamiento y recuperación de las preferencias del usuario.
 */
public class PreferencesRepository {
    private static final String PREFS_NAME = "weather_app_preferences";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_SURNAME = "user_surname";
    private static final String KEY_GENDER = "user_gender";
    private static final String KEY_COLD_TOLERANCE = "cold_tolerance";
    private static final String KEY_HEAT_TOLERANCE = "heat_tolerance";

    private final SharedPreferences preferences;

    public PreferencesRepository(Context context) {
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Método que guarda las preferencias del usuario
    public void saveUserPreferences(UserPreferences userPreferences) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(KEY_NAME, userPreferences.getName());
        editor.putString(KEY_SURNAME, userPreferences.getSurname());
        editor.putString(KEY_GENDER, userPreferences.getGender().name());
        editor.putString(KEY_COLD_TOLERANCE, userPreferences.getColdTolerance().name());
        editor.putString(KEY_HEAT_TOLERANCE, userPreferences.getHeatTolerance().name());

        editor.apply();
    }

    // Las recupera
    public UserPreferences getUserPreferences() {
        String name = preferences.getString(KEY_NAME, "");
        String surname = preferences.getString(KEY_SURNAME, "");

        UserPreferences.Gender gender;
        try {
            gender = UserPreferences.Gender.valueOf(
                    preferences.getString(KEY_GENDER, UserPreferences.Gender.OTHER.name())
            );
        } catch (IllegalArgumentException e) {
            gender = UserPreferences.Gender.OTHER;
        }

        UserPreferences.Tolerance coldTolerance;
        try {
            coldTolerance = UserPreferences.Tolerance.valueOf(
                    preferences.getString(KEY_COLD_TOLERANCE, UserPreferences.Tolerance.NORMAL.name())
            );
        } catch (IllegalArgumentException e) {
            coldTolerance = UserPreferences.Tolerance.NORMAL;
        }

        UserPreferences.Tolerance heatTolerance;
        try {
            heatTolerance = UserPreferences.Tolerance.valueOf(
                    preferences.getString(KEY_HEAT_TOLERANCE, UserPreferences.Tolerance.NORMAL.name())
            );
        } catch (IllegalArgumentException e) {
            heatTolerance = UserPreferences.Tolerance.NORMAL;
        }

        return new UserPreferences(name, surname, gender, coldTolerance, heatTolerance);
    }


        // Método para obtener el outfit guardado
        public OutfitRecommendation getSavedOutfit(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("OutfitPrefs", Context.MODE_PRIVATE);
        String outfitJson = prefs.getString("saved_outfit", null);

        // Si no hay un outfit guardado, devuelve null
        if (outfitJson != null && !outfitJson.isEmpty()) {
            try {
                Gson gson = new Gson();
                return gson.fromJson(outfitJson, OutfitRecommendation.class);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    // Método para guardar outfit
    public void saveOutfit(OutfitRecommendation outfit, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("OutfitPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String outfitJson = gson.toJson(outfit);
        editor.putString("saved_outfit", outfitJson);
        editor.apply();
    }
}