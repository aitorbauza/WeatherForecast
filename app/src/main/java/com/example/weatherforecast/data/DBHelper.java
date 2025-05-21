package com.example.weatherforecast.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.weatherforecast.model.CurrentWeather;
import com.example.weatherforecast.model.OutfitRecommendation;
import com.example.weatherforecast.model.SavedOutfitEntry;
import com.example.weatherforecast.model.UserPreferences;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Clase encargada de gestionar la base de datos SQLite.
 * Gestiona usuarios, preferencias y outfits guardados.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";

    // Información de la base de datos
    private static final String DATABASE_NAME = "weather_forecast.db";
    private static final int DATABASE_VERSION = 1;

    // Tabla de usuarios
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_LAST_LOGIN = "last_login";

    // Tabla de preferencias de usuario
    public static final String TABLE_PREFERENCES = "preferences";
    public static final String COLUMN_SURNAME = "surname";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_COLD_TOLERANCE = "cold_tolerance";
    public static final String COLUMN_HEAT_TOLERANCE = "heat_tolerance";

    // Tabla de outfits guardados
    public static final String TABLE_SAVED_OUTFITS = "saved_outfits";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_OUTFIT_JSON = "outfit_json";
    public static final String COLUMN_WEATHER_JSON = "weather_json";
    public static final String COLUMN_CITY = "city";

    // Formato de fecha usado en la app
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla de usuarios
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USERNAME + " TEXT PRIMARY KEY, " +
                COLUMN_PASSWORD + " TEXT NOT NULL, " +
                COLUMN_LAST_LOGIN + " TEXT)";

        // Crear tabla de preferencias
        String createPreferencesTable = "CREATE TABLE " + TABLE_PREFERENCES + " (" +
                COLUMN_USERNAME + " TEXT PRIMARY KEY, " +
                COLUMN_SURNAME + " TEXT, " +
                COLUMN_GENDER + " TEXT, " +
                COLUMN_COLD_TOLERANCE + " TEXT, " +
                COLUMN_HEAT_TOLERANCE + " TEXT, " +
                "FOREIGN KEY (" + COLUMN_USERNAME + ") REFERENCES " +
                TABLE_USERS + "(" + COLUMN_USERNAME + "))";

        // Crear tabla de outfits guardados
        String createSavedOutfitsTable = "CREATE TABLE " + TABLE_SAVED_OUTFITS + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT NOT NULL, " +
                COLUMN_DATE + " TEXT NOT NULL, " +
                COLUMN_CITY + " TEXT NOT NULL, " +
                COLUMN_OUTFIT_JSON + " TEXT NOT NULL, " +
                COLUMN_WEATHER_JSON + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_USERNAME + ") REFERENCES " +
                TABLE_USERS + "(" + COLUMN_USERNAME + "))";

        db.execSQL(createUsersTable);
        db.execSQL(createPreferencesTable);
        db.execSQL(createSavedOutfitsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // En caso de actualización de la BD, eliminar tablas y recrearlas
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED_OUTFITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PREFERENCES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Método que registra un nuevo usuario
    public boolean registerUser(String username, String password) {
        // Verifica si el usuario ya existe
        if (checkUserExists(username)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        // Inserta nuevo usuario
        long result = db.insert(TABLE_USERS, null, values);

        // Crear preferencias por defecto para el usuario
        if (result != -1) {
            createDefaultPreferences(username);
        }

        return result != -1;
    }

    // Método que verifica si un usuario ya existe
    private boolean checkUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        //Query
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] {username});

        boolean exists = cursor.getCount() > 0;
        cursor.close();

        return exists;
    }

    // Método que verifica si el usuario y contraseña son válidos
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_USERS +
                " WHERE " + COLUMN_USERNAME + " = ? AND " +
                COLUMN_PASSWORD + " = ?";

        Cursor cursor = db.rawQuery(query, new String[] {username, password});

        // Verifica si el usuario existe
        boolean valid = cursor.getCount() > 0;
        cursor.close();

        return valid;
    }

    // Método que registra el último inicio de sesión de un usuario
    public void recordLogin(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Fecha actual en formato String
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        values.put(COLUMN_LAST_LOGIN, currentDate);

        db.update(TABLE_USERS, values, COLUMN_USERNAME + " = ?",
                new String[] {username});
    }

    // Método que crea las preferencias por defecto para un usuario
    private void createDefaultPreferences(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Y las inserta en la tabla de preferencias
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_SURNAME, "");
        values.put(COLUMN_GENDER, UserPreferences.Gender.OTHER.name());
        values.put(COLUMN_COLD_TOLERANCE, UserPreferences.Tolerance.NORMAL.name());
        values.put(COLUMN_HEAT_TOLERANCE, UserPreferences.Tolerance.NORMAL.name());

        db.insert(TABLE_PREFERENCES, null, values);
    }

    // Método que guarda las preferencias de un usuario
    public boolean saveUserPreferences(String username, UserPreferences preferences) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_SURNAME, preferences.getSurname());
        values.put(COLUMN_GENDER, preferences.getGender().name());
        values.put(COLUMN_COLD_TOLERANCE, preferences.getColdTolerance().name());
        values.put(COLUMN_HEAT_TOLERANCE, preferences.getHeatTolerance().name());

        // Actualizar si existe, insertar si no
        int updated = db.update(TABLE_PREFERENCES, values,
                COLUMN_USERNAME + " = ?",
                new String[] {username});

        if (updated == 0) { // No se actualizó nada, insertar nuevo
            values.put(COLUMN_USERNAME, username);
            long result = db.insert(TABLE_PREFERENCES, null, values);
            return result != -1;
        }

        return true;
    }

    // Método que obtiene las preferencias de un usuario y devuelve un objeto con ellas
    public UserPreferences getUserPreferences(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_PREFERENCES +
                " WHERE " + COLUMN_USERNAME + " = ?";

        Cursor cursor = db.rawQuery(query, new String[] {username});

        UserPreferences preferences = new UserPreferences();
        preferences.setName(username); // Establecer el nombre

        if (cursor.moveToFirst()) {
            preferences.setSurname(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SURNAME)));

            // Convertir string a enum para género
            String genderStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER));
            UserPreferences.Gender gender = UserPreferences.Gender.valueOf(genderStr);
            preferences.setGender(gender);

            // Convertir string a enum para tolerancia al frío
            String coldToleranceStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLD_TOLERANCE));
            UserPreferences.Tolerance coldTolerance = UserPreferences.Tolerance.valueOf(coldToleranceStr);
            preferences.setColdTolerance(coldTolerance);

            // Convertir string a enum para tolerancia al calor
            String heatToleranceStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HEAT_TOLERANCE));
            UserPreferences.Tolerance heatTolerance = UserPreferences.Tolerance.valueOf(heatToleranceStr);
            preferences.setHeatTolerance(heatTolerance);
        }

        cursor.close();
        return preferences;
    }

    // Método que guarda un outfit en la base de datos
    public boolean saveOutfit(String username, OutfitRecommendation outfit, CurrentWeather weather, Date date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Convertir outfit y clima a JSON
        Gson gson = new Gson();
        String outfitJson = gson.toJson(outfit);
        String weatherJson = gson.toJson(weather);
        String dateStr = DATE_FORMAT.format(date);

        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_DATE, dateStr);
        values.put(COLUMN_CITY, weather.getLocation());
        values.put(COLUMN_OUTFIT_JSON, outfitJson);
        values.put(COLUMN_WEATHER_JSON, weatherJson);

        // Verificar si ya existe un outfit para esta fecha y usuario
        String query = "SELECT * FROM " + TABLE_SAVED_OUTFITS +
                " WHERE " + COLUMN_USERNAME + " = ? AND " +
                COLUMN_DATE + " = ?";

        Cursor cursor = db.rawQuery(query, new String[] {username, dateStr});

        long result;
        if (cursor.getCount() > 0) {
            // Actualizar el outfit existente
            result = db.update(TABLE_SAVED_OUTFITS, values,
                    COLUMN_USERNAME + " = ? AND " + COLUMN_DATE + " = ?",
                    new String[] {username, dateStr});
        } else {
            // Insertar nuevo outfit
            result = db.insert(TABLE_SAVED_OUTFITS, null, values);
        }

        cursor.close();
        return result != -1;
    }

    // Método que obtiene un outfit guardado para una fecha
    public SavedOutfitEntry getOutfitByDate(String username, Date date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String dateStr = DATE_FORMAT.format(date);

        String query = "SELECT * FROM " + TABLE_SAVED_OUTFITS +
                " WHERE " + COLUMN_USERNAME + " = ? AND " +
                COLUMN_DATE + " = ?";

        Cursor cursor = db.rawQuery(query, new String[] {username, dateStr});

        SavedOutfitEntry entry = null;

        if (cursor.moveToFirst()) {
            String outfitJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OUTFIT_JSON));
            String weatherJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WEATHER_JSON));

            Gson gson = new Gson();
            OutfitRecommendation outfit = gson.fromJson(outfitJson, OutfitRecommendation.class);
            CurrentWeather weather = gson.fromJson(weatherJson, CurrentWeather.class);

            entry = new SavedOutfitEntry(outfit, weather, date);
        }

        cursor.close();
        return entry;
    }

    // Método que obtiene el último outfit guardado
    public SavedOutfitEntry getLatestOutfit(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        SavedOutfitEntry latestOutfit = null;

        //try-catch que encapsula la consulta a la base de datos y si hay un error lo maneja
        try {
            // Query para obtener el último outfit guardado ordenado por fecha
            String query = "SELECT * FROM outfits WHERE username = ? ORDER BY save_date DESC LIMIT 1";
            Cursor cursor = db.rawQuery(query, new String[]{username});

            if (cursor.moveToFirst()) {
                // Obtener datos de las columnas
                String outfitJson = cursor.getString(cursor.getColumnIndex("outfit_data"));
                String weatherJson = cursor.getString(cursor.getColumnIndex("weather_data"));
                long dateMillis = cursor.getLong(cursor.getColumnIndex("save_date"));

                Gson gson = new Gson();
                OutfitRecommendation outfit = gson.fromJson(outfitJson, OutfitRecommendation.class);
                CurrentWeather weather = gson.fromJson(weatherJson, CurrentWeather.class);
                Date saveDate = new Date(dateMillis);

                latestOutfit = new SavedOutfitEntry(outfit, weather, saveDate);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return latestOutfit;
    }
}