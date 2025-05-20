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
 * Clase encargada de gestionar la base de datos SQLite para la aplicación.
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

    // Constructor
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

    /**
     * Registra un nuevo usuario en la base de datos.
     * @param username Nombre de usuario
     * @param password Contraseña
     * @return true si el registro fue exitoso, false si el usuario ya existe
     */
    public boolean registerUser(String username, String password) {
        // Verificar si el usuario ya existe
        if (checkUserExists(username)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        // Insertar nuevo usuario
        long result = db.insert(TABLE_USERS, null, values);

        // Crear preferencias por defecto para el usuario
        if (result != -1) {
            createDefaultPreferences(username);
        }

        return result != -1;
    }

    /**
     * Verifica si un usuario ya existe en la base de datos.
     * @param username Nombre de usuario a verificar
     * @return true si el usuario existe, false en caso contrario
     */
    private boolean checkUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] {username});

        boolean exists = cursor.getCount() > 0;
        cursor.close();

        return exists;
    }

    /**
     * Verifica las credenciales de un usuario.
     * @param username Nombre de usuario
     * @param password Contraseña
     * @return true si las credenciales son correctas, false en caso contrario
     */
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_USERS +
                " WHERE " + COLUMN_USERNAME + " = ? AND " +
                COLUMN_PASSWORD + " = ?";

        Cursor cursor = db.rawQuery(query, new String[] {username, password});

        boolean valid = cursor.getCount() > 0;
        cursor.close();

        return valid;
    }

    /**
     * Registra la fecha de último inicio de sesión del usuario.
     * @param username Nombre de usuario
     */
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

    /**
     * Crea preferencias predeterminadas para un nuevo usuario.
     * @param username Nombre de usuario
     */
    private void createDefaultPreferences(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_SURNAME, "");
        values.put(COLUMN_GENDER, UserPreferences.Gender.OTHER.name());
        values.put(COLUMN_COLD_TOLERANCE, UserPreferences.Tolerance.NORMAL.name());
        values.put(COLUMN_HEAT_TOLERANCE, UserPreferences.Tolerance.NORMAL.name());

        db.insert(TABLE_PREFERENCES, null, values);
    }

    /**
     * Guarda las preferencias de un usuario.
     * @param username Nombre de usuario
     * @param preferences Objeto con las preferencias del usuario
     * @return true si la operación fue exitosa
     */
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

    /**
     * Obtiene las preferencias de un usuario.
     * @param username Nombre de usuario
     * @return Objeto UserPreferences con las preferencias del usuario
     */
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

    /**
     * Guarda un outfit junto con la información meteorológica para una fecha específica.
     * @param username Nombre de usuario
     * @param outfit Recomendación de outfit
     * @param weather Información del clima
     * @param date Fecha del outfit
     * @return true si se guardó correctamente
     */
    public boolean saveOutfit(String username, OutfitRecommendation outfit, CurrentWeather weather, Date date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

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

    /**
     * Obtiene un outfit guardado para una fecha específica.
     * @param username Nombre de usuario
     * @param date Fecha del outfit
     * @return SavedOutfitEntry con la información del outfit y el clima, o null si no existe
     */
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

    /**
     * Obtiene todos los outfits guardados para un usuario.
     * @param username Nombre de usuario
     * @return Lista de SavedOutfitEntry con la información de outfits y clima
     */
    public List<SavedOutfitEntry> getAllOutfits(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<SavedOutfitEntry> outfits = new ArrayList<>();

        String query = "SELECT * FROM " + TABLE_SAVED_OUTFITS +
                " WHERE " + COLUMN_USERNAME + " = ?" +
                " ORDER BY " + COLUMN_DATE + " DESC";

        Cursor cursor = db.rawQuery(query, new String[] {username});

        if (cursor.moveToFirst()) {
            Gson gson = new Gson();

            do {
                String outfitJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OUTFIT_JSON));
                String weatherJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WEATHER_JSON));
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));

                OutfitRecommendation outfit = gson.fromJson(outfitJson, OutfitRecommendation.class);
                CurrentWeather weather = gson.fromJson(weatherJson, CurrentWeather.class);

                try {
                    Date date = DATE_FORMAT.parse(dateStr);
                    SavedOutfitEntry entry = new SavedOutfitEntry(outfit, weather, date);
                    outfits.add(entry);
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date: " + dateStr, e);
                }

            } while (cursor.moveToNext());
        }

        cursor.close();
        return outfits;
    }

    /**
     * Elimina un outfit guardado para una fecha específica.
     * @param username Nombre de usuario
     * @param date Fecha del outfit
     * @return true si se eliminó correctamente
     */
    public boolean deleteOutfit(String username, Date date) {
        SQLiteDatabase db = this.getWritableDatabase();
        String dateStr = DATE_FORMAT.format(date);

        int result = db.delete(TABLE_SAVED_OUTFITS,
                COLUMN_USERNAME + " = ? AND " + COLUMN_DATE + " = ?",
                new String[] {username, dateStr});

        return result > 0;
    }

    public SavedOutfitEntry getLatestOutfit(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        SavedOutfitEntry latestOutfit = null;

        try {
            // Consulta SQL para obtener el último outfit guardado ordenado por fecha
            String query = "SELECT * FROM outfits WHERE username = ? ORDER BY save_date DESC LIMIT 1";
            Cursor cursor = db.rawQuery(query, new String[]{username});

            if (cursor.moveToFirst()) {
                // Obtener datos de las columnas
                String outfitJson = cursor.getString(cursor.getColumnIndex("outfit_data"));
                String weatherJson = cursor.getString(cursor.getColumnIndex("weather_data"));
                long dateMillis = cursor.getLong(cursor.getColumnIndex("save_date"));

                // Convertir JSON a objetos
                Gson gson = new Gson();
                OutfitRecommendation outfit = gson.fromJson(outfitJson, OutfitRecommendation.class);
                CurrentWeather weather = gson.fromJson(weatherJson, CurrentWeather.class);
                Date saveDate = new Date(dateMillis);

                // Crear entrada de outfit guardado
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