package com.example.weatherforecast.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.weatherforecast.data.DBHelper;
import com.example.weatherforecast.model.CurrentWeather;
import com.example.weatherforecast.model.OutfitRecommendation;
import com.example.weatherforecast.model.SavedOutfitEntry;
import com.example.weatherforecast.model.UserPreferences;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Clase encargada de gestionar el almacenamiento y recuperación de las preferencias del usuario.
 */
public class PreferencesRepository {
    // Variables contatantes encargadas de guardar el nombre de las preferencias para cada usuario
    private static final String PREFS_NAME_BASE = "weather_app_preferences_";
    private final String userSpecificPrefsName;
    private static final String KEY_NAME = "user_name";
    private static final String KEY_SURNAME = "user_surname";
    private static final String KEY_GENDER = "user_gender";
    private static final String KEY_COLD_TOLERANCE = "cold_tolerance";
    private static final String KEY_HEAT_TOLERANCE = "heat_tolerance";

    private final SharedPreferences preferences;
    private final DBHelper dbHelper;
    private Context context;

    public PreferencesRepository(Context context, String username) {
        this.userSpecificPrefsName = PREFS_NAME_BASE + username;
        this.preferences = context.getSharedPreferences(userSpecificPrefsName, Context.MODE_PRIVATE);
        this.dbHelper = new DBHelper(context);
        this.context = context;
    }

    // Método que obtiene las preferencias del usuario
    public UserPreferences getUserPreferences(String username) {
        return dbHelper.getUserPreferences(username);
    }

    // Método que guarda las preferencias del usuario en la base de datos
    public boolean saveUserPreferences(UserPreferences userPreferences, String username) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(KEY_NAME, userPreferences.getName());
        editor.putString(KEY_SURNAME, userPreferences.getSurname());
        editor.putString(KEY_GENDER, userPreferences.getGender().name());
        editor.putString(KEY_COLD_TOLERANCE, userPreferences.getColdTolerance().name());
        editor.putString(KEY_HEAT_TOLERANCE, userPreferences.getHeatTolerance().name());

        editor.apply();
        return dbHelper.saveUserPreferences(username, userPreferences);
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


        //Obtiene el outfit guardado
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
    public void saveOutfit(OutfitRecommendation outfit, CurrentWeather weather, Date date, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("OutfitPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        SavedOutfitEntry entry = new SavedOutfitEntry(outfit, weather, date);

        // Formatear la fecha como clave
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateKey = dateFormat.format(date);

        // Usar Gson para serializar el objeto completo
        Gson gson = new Gson();
        String entryJson = gson.toJson(entry);

        // Guardar la entrada asociada a su fecha
        editor.putString("outfit_" + dateKey, entryJson);
        editor.apply();
    }

}